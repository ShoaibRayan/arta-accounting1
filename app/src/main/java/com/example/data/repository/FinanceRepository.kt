package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AccountCardEntity
import com.example.data.local.AccountDao
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.CurrencyDao
import com.example.data.local.CurrencyEntity
import com.example.data.local.FinancialGoalEntity
import com.example.data.local.GoalDao
import com.example.data.local.GoalStatus
import com.example.data.local.GoalTransactionDao
import com.example.data.local.GoalTransactionEntity
import com.example.data.local.GoalTransactionType
import com.example.data.local.QuickActionDao
import com.example.data.local.QuickActionEntity
import com.example.data.local.RecipientDao
import com.example.data.local.RecipientEntity
import com.example.data.local.ShoppingListDao
import com.example.data.local.ShoppingListEntity
import com.example.data.local.ShoppingListItemDao
import com.example.data.local.ShoppingListItemEntity
import com.example.data.local.ShoppingListWithItems
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionKind
import com.example.data.local.TransactionType
import com.example.ui.components.cleanTransactionNote
import com.example.util.BackupData
import com.example.util.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FinanceRepository(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val recipientDao: RecipientDao,
    private val currencyDao: CurrencyDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val goalTransactionDao: GoalTransactionDao,
    private val quickActionDao: QuickActionDao,
    private val shoppingListDao: ShoppingListDao,
    private val shoppingListItemDao: ShoppingListItemDao
) {
    private val balanceLock = Mutex()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allAccounts: Flow<List<AccountCardEntity>> = accountDao.getAllAccounts()
    val allRecipients: Flow<List<RecipientEntity>> = recipientDao.getAllRecipients()
    val allCurrencies: Flow<List<CurrencyEntity>> = currencyDao.getAllCurrencies()
    val activeCurrencies: Flow<List<CurrencyEntity>> = currencyDao.getActiveCurrencies()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val activeCategories: Flow<List<CategoryEntity>> = categoryDao.getActiveCategories()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<FinancialGoalEntity>> = goalDao.getAllGoals()
    val activeGoals: Flow<List<FinancialGoalEntity>> = goalDao.getActiveGoals()
    val completedGoals: Flow<List<FinancialGoalEntity>> = goalDao.getCompletedGoals()
    val archivedGoals: Flow<List<FinancialGoalEntity>> = goalDao.getArchivedGoals()
    val allGoalTransactions: Flow<List<GoalTransactionEntity>> = goalTransactionDao.getAllGoalTransactions()
    val allQuickActions: Flow<List<QuickActionEntity>> = quickActionDao.getAllQuickActions()
    val homeQuickActions: Flow<List<QuickActionEntity>> = quickActionDao.getHomeQuickActions()
    val allShoppingLists: Flow<List<ShoppingListEntity>> = shoppingListDao.getAllShoppingLists()

    val allShoppingListsWithItems: Flow<List<ShoppingListWithItems>> = combine(
        shoppingListDao.getAllShoppingLists(),
        shoppingListItemDao.getAllItems()
    ) { lists, items ->
        val itemsByList = items.groupBy { it.listId }
        lists.map { list ->
            ShoppingListWithItems(
                list = list,
                items = itemsByList[list.id] ?: emptyList()
            )
        }
    }

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        transactionDao.searchTransactions(query)

    private suspend fun roundToCurrencyPrecision(amount: Double, currencyCode: String): Double {
        val decimals = maxOf(currencyDao.getCurrencyByCode(currencyCode)?.decimalPlaces ?: 3, 3)
        val factor = Math.pow(10.0, decimals.coerceIn(3, 6).toDouble())
        return kotlin.math.round(amount * factor) / factor
    }

    suspend fun getCashBalance(currencyCode: String): Double {
        val curr = currencyDao.getCurrencyByCode(currencyCode)
        return if (curr != null) {
            transactionDao.getCashBalanceForCurrencyIdOrCode(curr.id, curr.code)
        } else {
            transactionDao.getCashBalanceForCurrency(currencyCode)
        }
    }

    suspend fun getCashBalance(currencyId: Long, currencyCode: String): Double {
        val resolvedId = if (currencyId > 0L) currencyId else (currencyDao.getCurrencyByCode(currencyCode)?.id ?: 0L)
        return transactionDao.getCashBalanceForCurrencyIdOrCode(resolvedId, currencyCode)
    }

    suspend fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        accountId: Long = 0,
        recipientName: String? = null,
        recipientId: Long? = null,
        categoryId: Long? = null,
        kind: TransactionKind? = null,
        note: String? = null,
        calculationExpression: String? = null,
        currencyId: Long = 0L,
        currencyCode: String = "AFN",
        currencySymbol: String = "؋",
        exchangeRate: Double = 1.0,
        ledgerCurrencyId: Long? = null,
        ledgerCurrencyCode: String? = null,
        ledgerCurrencySymbol: String? = null,
        ledgerAmount: Double? = null,
        dueDate: Long? = null,
        timestamp: Long = System.currentTimeMillis(),
        affectsBalance: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                require(amount > 0.0) { "مبلغ تراکنش باید بزرگتر از صفر باشد" }

                // Currency resolution strictly by ID if provided, otherwise resolve from code
                val allCurrs = currencyDao.getAllCurrenciesList()
                val currEntity = if (currencyId > 0L) {
                    currencyDao.getCurrencyById(currencyId) ?: allCurrs.firstOrNull { it.id == currencyId }
                } else {
                    allCurrs.firstOrNull { it.code.equals(currencyCode, ignoreCase = true) }
                }
                val finalCurrencyId = currEntity?.id ?: currencyId
                val finalCurrencyCode = currEntity?.code ?: currencyCode
                val finalCurrencySymbol = currEntity?.symbol ?: currencySymbol

                val ledgerCurrEntity = if (ledgerCurrencyId != null && ledgerCurrencyId > 0L) {
                    currencyDao.getCurrencyById(ledgerCurrencyId) ?: allCurrs.firstOrNull { it.id == ledgerCurrencyId }
                } else if (!ledgerCurrencyCode.isNullOrBlank()) {
                    allCurrs.firstOrNull { it.code.equals(ledgerCurrencyCode, ignoreCase = true) }
                } else null
                val finalLedgerCurrencyId = ledgerCurrEntity?.id ?: ledgerCurrencyId
                val finalLedgerCurrencyCode = ledgerCurrEntity?.code ?: ledgerCurrencyCode
                val finalLedgerCurrencySymbol = ledgerCurrEntity?.symbol ?: ledgerCurrencySymbol

                val cleanAmount = roundToCurrencyPrecision(amount, finalCurrencyCode)
                val cleanLedgerAmount = ledgerAmount?.let {
                    roundToCurrencyPrecision(it, finalLedgerCurrencyCode ?: finalCurrencyCode)
                }
                val cleanNote = cleanTransactionNote(note)

                // Account validations
                if (accountId > 0) {
                    val account = accountDao.getAccountById(accountId)
                        ?: error("حساب بانکی مورد نظر یافت نشد")
                    if (account.isFrozen) {
                        error("کارت انتخابی (${account.name}) منجمد است و امکان ثبت تراکنش روی آن وجود ندارد")
                    }
                    val isCurrencyMatch = (account.currencyId > 0 && finalCurrencyId > 0 && account.currencyId == finalCurrencyId) ||
                        account.currencyCode.equals(finalCurrencyCode, ignoreCase = true)
                    if (!isCurrencyMatch) {
                        error("ارز حساب (${account.currencyCode}) با ارز تراکنش ($finalCurrencyCode) همخوانی ندارد")
                    }
                    if (type == TransactionType.EXPENSE && affectsBalance && account.balance < cleanAmount) {
                        error("موجودی کارت (${account.balance} ${account.currencyCode}) برای این هزینه کافی نیست")
                    }
                } else if (accountId == 0L && type == TransactionType.EXPENSE && affectsBalance) {
                    val cashBal = getCashBalance(finalCurrencyId, finalCurrencyCode)
                    if (cashBal < cleanAmount) {
                        error("موجودی نقدی ($cashBal $finalCurrencyCode) برای این هزینه کافی نیست")
                    }
                }

                // Resolve Recipient strictly by ID (recipientId = تنها Identity)
                // If recipientId is null, it remains null and is never attributed to any person.
                var finalRecipientId: Long? = null
                var finalRecipientName = recipientName?.trim()?.ifBlank { null }
                if (recipientId != null && recipientId > 0) {
                    val r = recipientDao.getRecipientById(recipientId)
                    if (r != null) {
                        finalRecipientId = r.id
                        finalRecipientName = r.name
                    } else {
                        finalRecipientId = recipientId
                    }
                }

                // Resolve Category strictly by ID (categoryId = تنها Identity)
                var finalCategoryId: Long? = null
                var finalCategoryName = category.trim().ifBlank { "عمومی" }
                if (categoryId != null && categoryId > 0) {
                    val cat = categoryDao.getCategoryById(categoryId)
                    if (cat != null) {
                        finalCategoryId = cat.id
                        finalCategoryName = cat.name
                    } else {
                        finalCategoryId = categoryId
                    }
                }

                // Infer TransactionKind if not explicitly specified
                val resolvedKind = kind ?: when {
                    finalRecipientId != null -> {
                        if (type == TransactionType.INCOME) TransactionKind.PERSON_RECEIVE
                        else TransactionKind.PERSON_PAYMENT
                    }
                    category == "انتقالات" -> TransactionKind.TRANSFER
                    category == "Exchange" || category == "تبدیل اسعار" -> TransactionKind.CURRENCY_EXCHANGE
                    category == "واریز به هدف" -> TransactionKind.GOAL_DEPOSIT
                    category == "برداشت از هدف" -> TransactionKind.GOAL_WITHDRAW
                    else -> TransactionKind.NORMAL
                }

                // Get historical exchange rate
                val baseCurr = allCurrs.firstOrNull { it.isBaseCurrency } ?: allCurrs.firstOrNull()
                val histBaseCode = baseCurr?.code ?: "AFN"
                val histBaseRate = currEntity?.exchangeRateToBase ?: exchangeRate

                val txn = TransactionEntity(
                    title = title,
                    amount = cleanAmount,
                    type = type,
                    categoryId = finalCategoryId,
                    category = finalCategoryName,
                    accountId = accountId,
                    recipientName = finalRecipientName,
                    recipientId = finalRecipientId,
                    kind = resolvedKind,
                    timestamp = timestamp,
                    note = cleanNote,
                    calculationExpression = calculationExpression,
                    currencyId = finalCurrencyId,
                    currencyCode = finalCurrencyCode,
                    currencySymbol = finalCurrencySymbol,
                    exchangeRate = exchangeRate,
                    ledgerCurrencyId = finalLedgerCurrencyId,
                    ledgerCurrencyCode = finalLedgerCurrencyCode,
                    ledgerCurrencySymbol = finalLedgerCurrencySymbol,
                    ledgerAmount = cleanLedgerAmount,
                    historicalBaseCurrencyCode = histBaseCode,
                    historicalExchangeRateToBase = histBaseRate,
                    dueDate = dueDate,
                    affectsBalance = affectsBalance
                )
                val id = transactionDao.insertTransaction(txn)
                if (affectsBalance && accountId > 0) {
                    val delta = if (type == TransactionType.INCOME) cleanAmount else -cleanAmount
                    val updatedRows = accountDao.updateBalanceAtomic(accountId, delta)
                    if (updatedRows == 0) {
                        error("موجودی کارت برای این تراکنش کافی نیست")
                    }
                }

                if (finalRecipientId != null) {
                    recipientDao.incrementTransactionCountById(finalRecipientId)
                }
                id
            }
        }
    }

    suspend fun insertRawTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val cleanAmount = roundToCurrencyPrecision(transaction.amount, transaction.currencyCode)
            val cleanLedgerAmount = transaction.ledgerAmount?.let {
                roundToCurrencyPrecision(it, transaction.ledgerCurrencyCode ?: transaction.currencyCode)
            }
            val cleanTxn = transaction.copy(amount = cleanAmount, ledgerAmount = cleanLedgerAmount)
            val id = transactionDao.insertTransaction(cleanTxn)
            if (cleanTxn.recipientId != null) {
                recipientDao.incrementTransactionCountById(cleanTxn.recipientId)
            }
            id
        }
    }

    suspend fun executeCurrencyExchange(
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        fromAccountId: Long = 0L,
        toAccountId: Long = fromAccountId,
        note: String? = null
    ) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                require(fromAmount > 0.0 && toAmount > 0.0 && rate > 0.0) {
                    "مقادیر تبدیل و نرخ تبدیل باید بزرگتر از صفر باشند"
                }
                if ((fromCurrency.id > 0 && toCurrency.id > 0 && fromCurrency.id == toCurrency.id) ||
                    fromCurrency.code.equals(toCurrency.code, ignoreCase = true)) {
                    error("امکان تبدیل یک ارز به همان ارز وجود ندارد")
                }

                // Validate source account or cash
                if (fromAccountId > 0) {
                    val fromAccount = accountDao.getAccountById(fromAccountId) ?: error("حساب مبدأ یافت نشد")
                    if (fromAccount.isFrozen) error("حساب مبدأ منجمد است")
                    val isFromMatch = (fromAccount.currencyId > 0 && fromCurrency.id > 0 && fromAccount.currencyId == fromCurrency.id) ||
                        fromAccount.currencyCode.equals(fromCurrency.code, ignoreCase = true)
                    if (!isFromMatch) {
                        error("ارز کارت مبدأ (${fromAccount.currencyCode}) با ارز انتخابی (${fromCurrency.code}) مطابقت ندارد")
                    }
                    if (fromAccount.balance < fromAmount) {
                        error("موجودی حساب مبدأ (${fromAccount.balance} ${fromCurrency.code}) کافی نیست")
                    }
                    val updatedRows = accountDao.updateBalanceAtomic(fromAccountId, -fromAmount)
                    if (updatedRows == 0) {
                        error("موجودی حساب مبدأ (${fromAccount.balance} ${fromCurrency.code}) کافی نیست")
                    }
                } else {
                    val cashBal = getCashBalance(fromCurrency.id, fromCurrency.code)
                    if (cashBal < fromAmount) {
                        error("موجودی نقدی ${fromCurrency.name} ($cashBal ${fromCurrency.code}) کافی نیست")
                    }
                }

                // Validate destination account
                if (toAccountId > 0) {
                    val toAccount = accountDao.getAccountById(toAccountId) ?: error("حساب مقصد یافت نشد")
                    if (toAccount.isFrozen) error("حساب مقصد منجمد است")
                    val isToMatch = (toAccount.currencyId > 0 && toCurrency.id > 0 && toAccount.currencyId == toCurrency.id) ||
                        toAccount.currencyCode.equals(toCurrency.code, ignoreCase = true)
                    if (!isToMatch) {
                        error("ارز کارت مقصد (${toAccount.currencyCode}) با ارز دریافتی (${toCurrency.code}) مطابقت ندارد")
                    }
                    accountDao.updateBalance(toAccountId, toAmount)
                }

            val now = System.currentTimeMillis()
            val cleanNote = cleanTransactionNote(note)
            val allCurrs = currencyDao.getAllCurrenciesList()
            val baseCurr = allCurrs.firstOrNull { it.isBaseCurrency } ?: allCurrs.firstOrNull()
            val histBaseCode = baseCurr?.code ?: "AFN"

            // Outflow from fromCurrency
            val id1 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = "تبدیل به ${toCurrency.name}",
                    amount = fromAmount,
                    type = TransactionType.EXPENSE,
                    category = "Exchange",
                    accountId = fromAccountId,
                    recipientName = null,
                    kind = TransactionKind.CURRENCY_EXCHANGE,
                    timestamp = now,
                    note = cleanNote,
                    calculationExpression = "$fromAmount ${fromCurrency.symbol}",
                    currencyId = fromCurrency.id,
                    currencyCode = fromCurrency.code,
                    currencySymbol = fromCurrency.symbol,
                    exchangeRate = fromCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    historicalBaseCurrencyCode = histBaseCode,
                    historicalExchangeRateToBase = fromCurrency.exchangeRateToBase,
                    affectsBalance = true
                )
            )

            // Inflow to toCurrency
            val id2 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = "دریافت از تبدیل ${fromCurrency.name}",
                    amount = toAmount,
                    type = TransactionType.INCOME,
                    category = "Exchange",
                    accountId = toAccountId,
                    recipientName = null,
                    kind = TransactionKind.CURRENCY_EXCHANGE,
                    timestamp = now + 1,
                    note = cleanNote,
                    calculationExpression = "$toAmount ${toCurrency.symbol}",
                    currencyId = toCurrency.id,
                    currencyCode = toCurrency.code,
                    currencySymbol = toCurrency.symbol,
                    exchangeRate = toCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    historicalBaseCurrencyCode = histBaseCode,
                    historicalExchangeRateToBase = toCurrency.exchangeRateToBase,
                    relatedTransactionId = id1,
                    affectsBalance = true
                )
            )
            transactionDao.updateRelatedTransactionId(id1, id2)
        }
    }
}

    suspend fun executeCashCardTransfer(
        isCashToCard: Boolean,
        cashCurrency: CurrencyEntity,
        cardAccount: AccountCardEntity,
        cashAmount: Double,
        cardAmount: Double,
        rate: Double,
        targetCurrency: CurrencyEntity? = null,
        note: String? = null
    ) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                require(cashAmount > 0.0 && cardAmount > 0.0 && rate > 0.0) {
                    "مبالغ انتقال باید بزرگتر از صفر باشند"
                }
                val card = accountDao.getAccountById(cardAccount.id) ?: error("کارت انتخابی یافت نشد")
                if (card.isFrozen) error("کارت مورد نظر منجمد است")

                val cardCurrencyCode = card.currencyCode
                val cardCurrencySymbol = card.currencySymbol
                val effCashCurrency = if (isCashToCard) cashCurrency else (targetCurrency ?: cashCurrency)

                if (isCashToCard) {
                    // Cash -> Card
                    val cashBal = getCashBalance(cashCurrency.id, cashCurrency.code)
                    if (cashBal < cashAmount) {
                        error("موجودی نقد ${cashCurrency.name} ($cashBal ${cashCurrency.code}) برای این انتقال کافی نیست")
                    }
                    val isSameCurrency = (cashCurrency.id > 0 && card.currencyId > 0 && cashCurrency.id == card.currencyId) ||
                        cashCurrency.code.equals(card.currencyCode, ignoreCase = true)
                    if (isSameCurrency) {
                        require(kotlin.math.abs(cashAmount - cardAmount) < 0.0001) {
                            "در انتقال هم‌ارز، مبلغ نقد و کارت باید برابر باشد"
                        }
                    }
                    accountDao.updateBalance(card.id, cardAmount)
                } else {
                    // Card -> Cash
                    if (card.balance < cardAmount) {
                        error("موجودی کارت (${card.balance} ${card.currencyCode}) کافی نیست")
                    }
                    val isSameCurrency = (effCashCurrency.id > 0 && card.currencyId > 0 && effCashCurrency.id == card.currencyId) ||
                        cardCurrencyCode.equals(effCashCurrency.code, ignoreCase = true)
                    if (isSameCurrency) {
                        require(kotlin.math.abs(cashAmount - cardAmount) < 0.0001) {
                            "در انتقال هم‌ارز، مبلغ کارت و نقد باید برابر باشد"
                        }
                    }
                    val updatedRows = accountDao.updateBalanceAtomic(card.id, -cardAmount)
                    if (updatedRows == 0) {
                        error("موجودی کارت (${card.balance} ${card.currencyCode}) کافی نیست")
                    }
                }

                val now = System.currentTimeMillis()
            val cleanNote = cleanTransactionNote(note)
            val allCurrs = currencyDao.getAllCurrenciesList()
            val baseCurr = allCurrs.firstOrNull { it.isBaseCurrency } ?: allCurrs.firstOrNull()
            val histBaseCode = baseCurr?.code ?: "AFN"

            val cardCurrId = if (card.currencyId > 0L) card.currencyId else (currencyDao.getCurrencyByCode(cardCurrencyCode)?.id ?: 0L)
            if (isCashToCard) {
                val id1 = transactionDao.insertTransaction(
                    TransactionEntity(
                        title = "انتقال به کارت ${card.name}",
                        amount = cashAmount,
                        type = TransactionType.EXPENSE,
                        category = "انتقالات",
                        accountId = 0L,
                        recipientName = null,
                        kind = TransactionKind.TRANSFER,
                        timestamp = now,
                        note = cleanNote,
                        calculationExpression = "$cashAmount ${cashCurrency.symbol}",
                        currencyId = cashCurrency.id,
                        currencyCode = cashCurrency.code,
                        currencySymbol = cashCurrency.symbol,
                        exchangeRate = rate,
                        sourceCurrencyId = cashCurrency.id,
                        sourceCurrencyCode = cashCurrency.code,
                        sourceCurrencySymbol = cashCurrency.symbol,
                        sourceAmount = cashAmount,
                        targetCurrencyId = cardCurrId,
                        targetCurrencyCode = cardCurrencyCode,
                        targetCurrencySymbol = cardCurrencySymbol,
                        targetAmount = cardAmount,
                        conversionRate = rate,
                        historicalBaseCurrencyCode = histBaseCode,
                        historicalExchangeRateToBase = cashCurrency.exchangeRateToBase,
                        affectsBalance = true
                    )
                )

                val id2 = transactionDao.insertTransaction(
                    TransactionEntity(
                        title = "واریز از نقد (${cashCurrency.name})",
                        amount = cardAmount,
                        type = TransactionType.INCOME,
                        category = "انتقالات",
                        accountId = card.id,
                        recipientName = null,
                        kind = TransactionKind.TRANSFER,
                        timestamp = now + 1,
                        note = cleanNote,
                        calculationExpression = "$cardAmount $cardCurrencySymbol",
                        currencyId = cardCurrId,
                        currencyCode = cardCurrencyCode,
                        currencySymbol = cardCurrencySymbol,
                        exchangeRate = rate,
                        sourceCurrencyId = cashCurrency.id,
                        sourceCurrencyCode = cashCurrency.code,
                        sourceCurrencySymbol = cashCurrency.symbol,
                        sourceAmount = cashAmount,
                        targetCurrencyId = cardCurrId,
                        targetCurrencyCode = cardCurrencyCode,
                        targetCurrencySymbol = cardCurrencySymbol,
                        targetAmount = cardAmount,
                        conversionRate = rate,
                        historicalBaseCurrencyCode = histBaseCode,
                        historicalExchangeRateToBase = currencyDao.getCurrencyByCode(cardCurrencyCode)?.exchangeRateToBase ?: 1.0,
                        affectsBalance = true,
                        relatedTransactionId = id1
                    )
                )
                transactionDao.updateRelatedTransactionId(id1, id2)
            } else {
                val id1 = transactionDao.insertTransaction(
                    TransactionEntity(
                        title = "انتقال به نقد (${effCashCurrency.name})",
                        amount = cardAmount,
                        type = TransactionType.EXPENSE,
                        category = "انتقالات",
                        accountId = card.id,
                        recipientName = null,
                        kind = TransactionKind.TRANSFER,
                        timestamp = now,
                        note = cleanNote,
                        calculationExpression = "$cardAmount $cardCurrencySymbol",
                        currencyId = cardCurrId,
                        currencyCode = cardCurrencyCode,
                        currencySymbol = cardCurrencySymbol,
                        exchangeRate = rate,
                        sourceCurrencyId = cardCurrId,
                        sourceCurrencyCode = cardCurrencyCode,
                        sourceCurrencySymbol = cardCurrencySymbol,
                        sourceAmount = cardAmount,
                        targetCurrencyId = effCashCurrency.id,
                        targetCurrencyCode = effCashCurrency.code,
                        targetCurrencySymbol = effCashCurrency.symbol,
                        targetAmount = cashAmount,
                        conversionRate = rate,
                        historicalBaseCurrencyCode = histBaseCode,
                        historicalExchangeRateToBase = currencyDao.getCurrencyByCode(cardCurrencyCode)?.exchangeRateToBase ?: 1.0,
                        affectsBalance = true
                    )
                )

                val id2 = transactionDao.insertTransaction(
                    TransactionEntity(
                        title = "دریافت از کارت ${card.name}",
                        amount = cashAmount,
                        type = TransactionType.INCOME,
                        category = "انتقالات",
                        accountId = 0L,
                        recipientName = null,
                        kind = TransactionKind.TRANSFER,
                        timestamp = now + 1,
                        note = cleanNote,
                        calculationExpression = "$cashAmount ${effCashCurrency.symbol}",
                        currencyId = effCashCurrency.id,
                        currencyCode = effCashCurrency.code,
                        currencySymbol = effCashCurrency.symbol,
                        exchangeRate = rate,
                        sourceCurrencyId = cardCurrId,
                        sourceCurrencyCode = cardCurrencyCode,
                        sourceCurrencySymbol = cardCurrencySymbol,
                        sourceAmount = cardAmount,
                        targetCurrencyId = effCashCurrency.id,
                        targetCurrencyCode = effCashCurrency.code,
                        targetCurrencySymbol = effCashCurrency.symbol,
                        targetAmount = cashAmount,
                        conversionRate = rate,
                        historicalBaseCurrencyCode = histBaseCode,
                        historicalExchangeRateToBase = effCashCurrency.exchangeRateToBase,
                        affectsBalance = true,
                        relatedTransactionId = id1
                    )
                )
                transactionDao.updateRelatedTransactionId(id1, id2)
            }
        }
    }
}

    suspend fun updateCashCardTransfer(
        txn1Id: Long,
        txn2Id: Long,
        oldIsCashToCard: Boolean,
        oldCardAccountId: Long,
        oldCardAmount: Double,
        newIsCashToCard: Boolean,
        newCashCurrency: CurrencyEntity,
        newCardAccount: AccountCardEntity,
        newCashAmount: Double,
        newCardAmount: Double,
        newRate: Double,
        newTargetCurrency: CurrencyEntity? = null,
        newNote: String? = null
    ) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                val newCard = accountDao.getAccountById(newCardAccount.id) ?: error("کارت انتخابی یافت نشد")
                if (newCard.isFrozen) error("کارت مورد نظر (${newCard.name}) منجمد است")

                // 1. Revert previous card balance
                if (oldCardAccountId > 0L) {
                    val oldCard = accountDao.getAccountById(oldCardAccountId)
                    if (oldCard != null && oldCard.isFrozen && oldCard.id != newCard.id) {
                        error("کارت قبلی (${oldCard.name}) منجمد است")
                    }
                    val oldCardDelta = if (oldIsCashToCard) -oldCardAmount else oldCardAmount
                    if (oldCardAccountId == newCard.id) {
                        val newCardDelta = if (newIsCashToCard) newCardAmount else -newCardAmount
                        val netDelta = oldCardDelta + newCardDelta
                        if (newCard.balance + netDelta < 0.0) {
                            error("موجودی کارت پس از ویرایش منفی می‌شود")
                        }
                        if (netDelta != 0.0) {
                            val updated = accountDao.updateBalanceAtomic(newCard.id, netDelta)
                            if (updated == 0) error("موجودی کارت برای این ویرایش کافی نیست")
                        }
                    } else {
                        if (oldCardDelta < 0) {
                            val updated = accountDao.updateBalanceAtomic(oldCardAccountId, oldCardDelta)
                            if (updated == 0) error("موجودی کارت قبلی برای بازگردانی کافی نیست")
                        } else {
                            accountDao.updateBalance(oldCardAccountId, oldCardDelta)
                        }
                        val newCardDelta = if (newIsCashToCard) newCardAmount else -newCardAmount
                        if (newCardDelta < 0) {
                            val updated = accountDao.updateBalanceAtomic(newCard.id, newCardDelta)
                            if (updated == 0) error("موجودی کارت جدید برای این انتقال کافی نیست")
                        } else {
                            accountDao.updateBalance(newCard.id, newCardDelta)
                        }
                    }
                } else {
                    val newCardDelta = if (newIsCashToCard) newCardAmount else -newCardAmount
                    if (newCardDelta < 0) {
                        val updated = accountDao.updateBalanceAtomic(newCard.id, newCardDelta)
                        if (updated == 0) error("موجودی کارت برای این انتقال کافی نیست")
                    } else {
                        accountDao.updateBalance(newCard.id, newCardDelta)
                    }
                }

                val cardCurrencyCode = newCardAccount.currencyCode
            val cardCurrencySymbol = newCardAccount.currencySymbol
            val effCashCurrency = if (newIsCashToCard) newCashCurrency else (newTargetCurrency ?: newCashCurrency)

            val t1 = transactionDao.getTransactionById(txn1Id)
            val t2 = transactionDao.getTransactionById(txn2Id)

            val expenseTxn = if (t1?.type == TransactionType.EXPENSE) t1 else if (t2?.type == TransactionType.EXPENSE) t2 else t1
            val incomeTxn = if (t1?.type == TransactionType.INCOME) t1 else if (t2?.type == TransactionType.INCOME) t2 else t2

            val expId = expenseTxn?.id ?: txn1Id
            val incId = incomeTxn?.id ?: txn2Id

            val cleanNote = cleanTransactionNote(newNote) ?: cleanTransactionNote(expenseTxn?.note) ?: cleanTransactionNote(incomeTxn?.note)
            val newCardCurrId = if (newCard.currencyId > 0L) newCard.currencyId else (currencyDao.getCurrencyByCode(cardCurrencyCode)?.id ?: 0L)
            if (newIsCashToCard) {
                val updatedExp = (expenseTxn ?: TransactionEntity(id = expId, title = "", amount = 0.0, type = TransactionType.EXPENSE, category = "انتقالات")).copy(
                    title = "انتقال به کارت ${newCardAccount.name}",
                    amount = newCashAmount,
                    type = TransactionType.EXPENSE,
                    category = "انتقالات",
                    accountId = 0L,
                    recipientName = null,
                    kind = TransactionKind.TRANSFER,
                    note = cleanNote,
                    calculationExpression = "$newCashAmount ${newCashCurrency.symbol}",
                    currencyId = newCashCurrency.id,
                    currencyCode = newCashCurrency.code,
                    currencySymbol = newCashCurrency.symbol,
                    exchangeRate = newRate,
                    sourceCurrencyId = newCashCurrency.id,
                    sourceCurrencyCode = newCashCurrency.code,
                    sourceCurrencySymbol = newCashCurrency.symbol,
                    sourceAmount = newCashAmount,
                    targetCurrencyId = newCardCurrId,
                    targetCurrencyCode = cardCurrencyCode,
                    targetCurrencySymbol = cardCurrencySymbol,
                    targetAmount = newCardAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = incId
                )
                transactionDao.updateTransaction(updatedExp)

                val updatedInc = (incomeTxn ?: TransactionEntity(id = incId, title = "", amount = 0.0, type = TransactionType.INCOME, category = "انتقالات")).copy(
                    title = "واریز از نقد (${newCashCurrency.name})",
                    amount = newCardAmount,
                    type = TransactionType.INCOME,
                    category = "انتقالات",
                    accountId = newCardAccount.id,
                    recipientName = null,
                    kind = TransactionKind.TRANSFER,
                    note = cleanNote,
                    calculationExpression = "$newCardAmount $cardCurrencySymbol",
                    currencyId = newCardCurrId,
                    currencyCode = cardCurrencyCode,
                    currencySymbol = cardCurrencySymbol,
                    exchangeRate = newRate,
                    sourceCurrencyId = newCashCurrency.id,
                    sourceCurrencyCode = newCashCurrency.code,
                    sourceCurrencySymbol = newCashCurrency.symbol,
                    sourceAmount = newCashAmount,
                    targetCurrencyId = newCardCurrId,
                    targetCurrencyCode = cardCurrencyCode,
                    targetCurrencySymbol = cardCurrencySymbol,
                    targetAmount = newCardAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = expId
                )
                transactionDao.updateTransaction(updatedInc)
            } else {
                val updatedExp = (expenseTxn ?: TransactionEntity(id = expId, title = "", amount = 0.0, type = TransactionType.EXPENSE, category = "انتقالات")).copy(
                    title = "انتقال به نقد (${effCashCurrency.name})",
                    amount = newCardAmount,
                    type = TransactionType.EXPENSE,
                    category = "انتقالات",
                    accountId = newCardAccount.id,
                    recipientName = null,
                    kind = TransactionKind.TRANSFER,
                    note = cleanNote,
                    calculationExpression = "$newCardAmount $cardCurrencySymbol",
                    currencyId = newCardCurrId,
                    currencyCode = cardCurrencyCode,
                    currencySymbol = cardCurrencySymbol,
                    exchangeRate = newRate,
                    sourceCurrencyId = newCardCurrId,
                    sourceCurrencyCode = cardCurrencyCode,
                    sourceCurrencySymbol = cardCurrencySymbol,
                    sourceAmount = newCardAmount,
                    targetCurrencyId = effCashCurrency.id,
                    targetCurrencyCode = effCashCurrency.code,
                    targetCurrencySymbol = effCashCurrency.symbol,
                    targetAmount = newCashAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = incId
                )
                transactionDao.updateTransaction(updatedExp)

                val updatedInc = (incomeTxn ?: TransactionEntity(id = incId, title = "", amount = 0.0, type = TransactionType.INCOME, category = "انتقالات")).copy(
                    title = "دریافت از کارت ${newCardAccount.name}",
                    amount = newCashAmount,
                    type = TransactionType.INCOME,
                    category = "انتقالات",
                    accountId = 0L,
                    recipientName = null,
                    kind = TransactionKind.TRANSFER,
                    note = cleanNote,
                    calculationExpression = "$newCashAmount ${effCashCurrency.symbol}",
                    currencyId = effCashCurrency.id,
                    currencyCode = effCashCurrency.code,
                    currencySymbol = effCashCurrency.symbol,
                    exchangeRate = newRate,
                    sourceCurrencyId = newCardCurrId,
                    sourceCurrencyCode = cardCurrencyCode,
                    sourceCurrencySymbol = cardCurrencySymbol,
                    sourceAmount = newCardAmount,
                    targetCurrencyId = effCashCurrency.id,
                    targetCurrencyCode = effCashCurrency.code,
                    targetCurrencySymbol = effCashCurrency.symbol,
                    targetAmount = newCashAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = expId
                )
                transactionDao.updateTransaction(updatedInc)
            }
        }
        }
    }

    suspend fun addCurrency(
        name: String,
        code: String,
        symbol: String,
        flagEmoji: String,
        colorHex: Long,
        rate: Double,
        isActive: Boolean = true,
        decimalPlaces: Int = 0
    ): Long = withContext(Dispatchers.IO) {
        currencyDao.insertCurrency(
            CurrencyEntity(
                name = name,
                code = code.uppercase(),
                symbol = symbol,
                flagEmoji = flagEmoji,
                colorHex = colorHex,
                exchangeRateToBase = rate,
                isActive = isActive,
                isBaseCurrency = false,
                decimalPlaces = decimalPlaces
            )
        )
    }

    suspend fun updateCurrency(currency: CurrencyEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val oldCurrency = currencyDao.getCurrencyById(currency.id)
            currencyDao.updateCurrency(currency)
            if (oldCurrency != null) {
                val oldCode = oldCurrency.code
                val newCode = currency.code
                val newSymbol = currency.symbol
                transactionDao.updateTransactionsCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                transactionDao.updateTransactionsLedgerCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                transactionDao.updateTransactionsSourceCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                transactionDao.updateTransactionsTargetCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                if (oldCurrency.isBaseCurrency && !oldCode.equals(newCode, ignoreCase = true)) {
                    transactionDao.updateHistoricalBaseCurrencyCode(oldCode, newCode)
                }
                accountDao.updateAccountsCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                budgetDao.updateBudgetsCurrencyReference(currency.id, oldCode, newCode)
                goalDao.updateGoalsCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                goalTransactionDao.updateGoalTransactionsCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                quickActionDao.updateQuickActionsCurrencyReference(currency.id, oldCode, newCode, newSymbol)
                shoppingListDao.updateShoppingListsCurrencyReference(currency.id, oldCode, newCode, newSymbol)
            }
        }
    }

    suspend fun toggleCurrencyActive(currency: CurrencyEntity) = withContext(Dispatchers.IO) {
        if (!currency.isBaseCurrency) {
            currencyDao.updateCurrency(currency.copy(isActive = !currency.isActive))
        }
    }

    suspend fun deleteCurrency(currency: CurrencyEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (currency.isBaseCurrency) {
                error("ارز پایه قابل حذف نیست")
            }
            val usage = countUsageForCurrency(currency.id, currency.code)
            if (usage > 0) {
                error("امکان حذف ارز '${currency.name}' به دلیل وجود $usage حساب یا تراکنش وابسته وجود ندارد.")
            }
            currencyDao.deleteCurrency(currency)
        }
    }

    suspend fun getCurrencyByCode(code: String): CurrencyEntity? = withContext(Dispatchers.IO) {
        currencyDao.getCurrencyByCode(code)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                val deletedIds = mutableSetOf<Long>()

                // Check if this transaction is linked to a goal transaction
                val linkedGoalTxn = goalTransactionDao.getGoalTransactionByLinkedTxnId(transaction.id)
                    ?: if (transaction.category == "واریز به هدف" || transaction.category == "برداشت از هدف") {
                        goalTransactionDao.getAllGoalTransactionsList().firstOrNull {
                            it.timestamp == transaction.timestamp || (it.linkedTransactionId == transaction.id)
                        }
                    } else null

                if (linkedGoalTxn != null) {
                    deleteGoalTransactionInternal(linkedGoalTxn.id, revertBalance = true)
                    return@withTransaction
                }

                // Revert any linked shopping list so it returns to pending/unlogged state
                val linkedShoppingList = shoppingListDao.getShoppingListByLinkedTransactionId(transaction.id)
                    ?: shoppingListDao.getAllShoppingListsList().firstOrNull { it.linkedTransactionId == transaction.id }
                if (linkedShoppingList != null) {
                    shoppingListDao.updateShoppingList(
                        linkedShoppingList.copy(
                            isLoggedAsExpense = false,
                            linkedTransactionId = null
                        )
                    )
                }

                // 1. Delete main transaction
                transactionDao.deleteTransaction(transaction)
                deletedIds.add(transaction.id)

                // Decrement recipient transaction count strictly by recipientId
                if (transaction.recipientId != null) {
                    recipientDao.decrementTransactionCountById(transaction.recipientId)
                }

                // Revert balance for main transaction
                val isTxnTransferOrExchange = transaction.category == "انتقالات" || transaction.category == "Exchange" ||
                    transaction.kind == TransactionKind.TRANSFER || transaction.kind == TransactionKind.CURRENCY_EXCHANGE ||
                    transaction.type == TransactionType.TRANSFER
                val txnAffectsAccount = (transaction.affectsBalance || isTxnTransferOrExchange) && transaction.accountId > 0
                if (txnAffectsAccount) {
                    val reverseDelta = if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
                    val updatedRows = accountDao.updateBalanceAtomic(transaction.accountId, reverseDelta)
                    if (updatedRows == 0) {
                        error("موجودی کارت برای لغو این تراکنش کافی نیست")
                    }
                }

                // 2. Cascade delete linked exchange/transfer transaction
                if (transaction.relatedTransactionId != null) {
                    val related = transactionDao.getTransactionById(transaction.relatedTransactionId)
                    if (related != null && !deletedIds.contains(related.id)) {
                        deletedIds.add(related.id)
                        transactionDao.deleteTransaction(related)
                        if (related.recipientId != null) {
                            recipientDao.decrementTransactionCountById(related.recipientId)
                        }
                        val relAffectsAccount = (related.affectsBalance || isTxnTransferOrExchange) && related.accountId > 0
                        if (relAffectsAccount) {
                            val relReverseDelta = if (related.type == TransactionType.INCOME) -related.amount else related.amount
                            val updatedRel = accountDao.updateBalanceAtomic(related.accountId, relReverseDelta)
                            if (updatedRel == 0) {
                                error("موجودی کارت برای لغو تراکنش وابسته کافی نیست")
                            }
                        }
                    }
                }
                val reverseRelated = transactionDao.getTransactionsByRelatedId(transaction.id)
                reverseRelated.forEach { rel ->
                    if (!deletedIds.contains(rel.id)) {
                        deletedIds.add(rel.id)
                        transactionDao.deleteTransaction(rel)
                        if (rel.recipientId != null) {
                            recipientDao.decrementTransactionCountById(rel.recipientId)
                        }
                        val relAffectsAccount = (rel.affectsBalance || isTxnTransferOrExchange) && rel.accountId > 0
                        if (relAffectsAccount) {
                            val relReverseDelta = if (rel.type == TransactionType.INCOME) -rel.amount else rel.amount
                            val updatedRel = accountDao.updateBalanceAtomic(rel.accountId, relReverseDelta)
                            if (updatedRel == 0) {
                                error("موجودی کارت برای لغو تراکنش وابسته کافی نیست")
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun getLinkedTransferTransactions(txn: TransactionEntity): Pair<TransactionEntity, TransactionEntity>? = withContext(Dispatchers.IO) {
        if (txn.category != "انتقالات" && txn.kind != TransactionKind.TRANSFER && txn.type != TransactionType.TRANSFER && txn.relatedTransactionId == null) {
            return@withContext null
        }
        var related: TransactionEntity? = null
        if (txn.relatedTransactionId != null) {
            related = transactionDao.getTransactionById(txn.relatedTransactionId)
        }
        if (related == null) {
            val list = transactionDao.getTransactionsByRelatedId(txn.id)
            related = list.firstOrNull()
        }
        if (related == null) return@withContext null

        if (txn.type == TransactionType.EXPENSE) {
            Pair(txn, related)
        } else {
            Pair(related, txn)
        }
    }

    suspend fun executeWalletTransfer(
        fromAccount: AccountCardEntity,
        toAccount: AccountCardEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        note: String? = null
    ) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                require(fromAmount > 0.0 && toAmount > 0.0 && rate > 0.0) {
                    "مبالغ و نرخ انتقال بین کارت‌ها باید بزرگتر از صفر باشند"
                }
                val fAcc = accountDao.getAccountById(fromAccount.id) ?: error("کارت مبدأ یافت نشد")
                val tAcc = accountDao.getAccountById(toAccount.id) ?: error("کارت مقصد یافت نشد")
                if (fAcc.isFrozen) error("کارت مبدأ (${fAcc.name}) منجمد است")
                if (tAcc.isFrozen) error("کارت مقصد (${tAcc.name}) منجمد است")

                if (fAcc.balance < fromAmount) {
                    error("موجودی کارت مبدأ (${fAcc.balance} ${fAcc.currencyCode}) کافی نیست")
                }

                if (fAcc.currencyCode.equals(tAcc.currencyCode, ignoreCase = true)) {
                    require(kotlin.math.abs(fromAmount - toAmount) < 0.0001) {
                        "در انتقال بین دو کارت با ارز یکسان، مبالغ مبدأ و مقصد باید برابر باشند"
                    }
                }

                // Deduct from fromAccount atomically and add to toAccount
                val updatedRows = accountDao.updateBalanceAtomic(fAcc.id, -fromAmount)
                if (updatedRows == 0) {
                    error("موجودی کارت مبدأ (${fAcc.balance} ${fAcc.currencyCode}) کافی نیست")
                }
                accountDao.updateBalance(tAcc.id, toAmount)

            val now = System.currentTimeMillis()
            val cleanNote = cleanTransactionNote(note)
            val allCurrs = currencyDao.getAllCurrenciesList()
            val baseCurr = allCurrs.firstOrNull { it.isBaseCurrency } ?: allCurrs.firstOrNull()
            val histBaseCode = baseCurr?.code ?: "AFN"

            val fCurrId = if (fAcc.currencyId > 0L) fAcc.currencyId else (currencyDao.getCurrencyByCode(fAcc.currencyCode)?.id ?: 0L)
            val tCurrId = if (tAcc.currencyId > 0L) tAcc.currencyId else (currencyDao.getCurrencyByCode(tAcc.currencyCode)?.id ?: 0L)

            val id1 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = "انتقال به ${tAcc.name}",
                    amount = fromAmount,
                    type = TransactionType.EXPENSE,
                    category = "انتقالات",
                    accountId = fAcc.id,
                    recipientName = null,
                    kind = TransactionKind.TRANSFER,
                    timestamp = now,
                    note = cleanNote,
                    calculationExpression = "${fAcc.currencySymbol} $fromAmount",
                    currencyId = fCurrId,
                    currencyCode = fAcc.currencyCode,
                    currencySymbol = fAcc.currencySymbol,
                    exchangeRate = rate,
                    sourceCurrencyId = fCurrId,
                    sourceCurrencyCode = fAcc.currencyCode,
                    sourceCurrencySymbol = fAcc.currencySymbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = tCurrId,
                    targetCurrencyCode = tAcc.currencyCode,
                    targetCurrencySymbol = tAcc.currencySymbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    historicalBaseCurrencyCode = histBaseCode,
                    historicalExchangeRateToBase = currencyDao.getCurrencyByCode(fAcc.currencyCode)?.exchangeRateToBase ?: 1.0,
                    affectsBalance = true
                )
            )

            val id2 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = "دریافت از ${fAcc.name}",
                    amount = toAmount,
                    type = TransactionType.INCOME,
                    category = "انتقالات",
                    accountId = tAcc.id,
                    recipientName = null,
                    kind = TransactionKind.TRANSFER,
                    timestamp = now + 1,
                    note = cleanNote,
                    calculationExpression = "${tAcc.currencySymbol} $toAmount",
                    currencyId = tCurrId,
                    currencyCode = tAcc.currencyCode,
                    currencySymbol = tAcc.currencySymbol,
                    exchangeRate = rate,
                    sourceCurrencyId = fCurrId,
                    sourceCurrencyCode = fAcc.currencyCode,
                    sourceCurrencySymbol = fAcc.currencySymbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = tCurrId,
                    targetCurrencyCode = tAcc.currencyCode,
                    targetCurrencySymbol = tAcc.currencySymbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    historicalBaseCurrencyCode = histBaseCode,
                    historicalExchangeRateToBase = currencyDao.getCurrencyByCode(tAcc.currencyCode)?.exchangeRateToBase ?: 1.0,
                    affectsBalance = true,
                    relatedTransactionId = id1
                )
            )
            transactionDao.updateRelatedTransactionId(id1, id2)
        }
    }
}

    suspend fun updateWalletTransfer(
        txn1Id: Long,
        txn2Id: Long,
        oldFromAccountId: Long,
        oldToAccountId: Long,
        oldFromAmount: Double,
        oldToAmount: Double,
        newFromAccount: AccountCardEntity,
        newToAccount: AccountCardEntity,
        newFromAmount: Double,
        newToAmount: Double,
        newRate: Double,
        newNote: String?
    ) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                require(newFromAccount.id != newToAccount.id) {
                    "حساب مبدأ و مقصد نمی‌توانند یکسان باشند"
                }
                val newFrom = accountDao.getAccountById(newFromAccount.id) ?: error("حساب مبدأ یافت نشد")
                val newTo = accountDao.getAccountById(newToAccount.id) ?: error("حساب مقصد یافت نشد")
                if (newFrom.isFrozen) error("حساب مبدأ (${newFrom.name}) منجمد است")
                if (newTo.isFrozen) error("حساب مقصد (${newTo.name}) منجمد است")

                if (oldFromAccountId > 0 && oldFromAccountId != newFrom.id && oldFromAccountId != newTo.id) {
                    val oldFrom = accountDao.getAccountById(oldFromAccountId)
                    if (oldFrom != null && oldFrom.isFrozen) error("حساب مبدأ قبلی (${oldFrom.name}) منجمد است")
                }
                if (oldToAccountId > 0 && oldToAccountId != newFrom.id && oldToAccountId != newTo.id) {
                    val oldTo = accountDao.getAccountById(oldToAccountId)
                    if (oldTo != null && oldTo.isFrozen) error("حساب مقصد قبلی (${oldTo.name}) منجمد است")
                }

                // Compute net deltas per account to prevent negative balances
                val deltas = mutableMapOf<Long, Double>()
                if (oldFromAccountId > 0) deltas[oldFromAccountId] = (deltas[oldFromAccountId] ?: 0.0) + oldFromAmount
                if (oldToAccountId > 0) deltas[oldToAccountId] = (deltas[oldToAccountId] ?: 0.0) - oldToAmount
                deltas[newFromAccount.id] = (deltas[newFromAccount.id] ?: 0.0) - newFromAmount
                deltas[newToAccount.id] = (deltas[newToAccount.id] ?: 0.0) + newToAmount

                for ((accId, delta) in deltas) {
                    if (delta < 0.0) {
                        val currentAcc = accountDao.getAccountById(accId) ?: error("حساب یافت نشد")
                        if (currentAcc.balance + delta < 0.0) {
                            error("موجودی حساب ${currentAcc.name} برای این تغییر کافی نیست")
                        }
                        val updated = accountDao.updateBalanceAtomic(accId, delta)
                        if (updated == 0) error("موجودی حساب ${currentAcc.name} برای این تغییر کافی نیست")
                    } else if (delta > 0.0) {
                        accountDao.updateBalance(accId, delta)
                    }
                }

                val txn1 = transactionDao.getTransactionById(txn1Id)
                val txn2 = transactionDao.getTransactionById(txn2Id)

                val noteToSet = cleanTransactionNote(newNote) ?: cleanTransactionNote(txn1?.note) ?: cleanTransactionNote(txn2?.note)

                val updatedTxn1 = (txn1 ?: TransactionEntity(id = txn1Id, title = "", amount = 0.0, type = TransactionType.EXPENSE, category = "انتقالات")).copy(
                    title = "انتقال به ${newToAccount.name}",
                    amount = newFromAmount,
                    type = TransactionType.EXPENSE,
                    category = "انتقالات",
                    accountId = newFromAccount.id,
                    kind = TransactionKind.TRANSFER,
                    note = noteToSet,
                    calculationExpression = "${newFromAccount.currencySymbol} $newFromAmount",
                    currencyId = newFrom.currencyId,
                    currencyCode = newFromAccount.currencyCode,
                    currencySymbol = newFromAccount.currencySymbol,
                    exchangeRate = newRate,
                    sourceCurrencyId = newFrom.currencyId,
                    sourceCurrencyCode = newFromAccount.currencyCode,
                    sourceCurrencySymbol = newFromAccount.currencySymbol,
                    sourceAmount = newFromAmount,
                    targetCurrencyId = newTo.currencyId,
                    targetCurrencyCode = newToAccount.currencyCode,
                    targetCurrencySymbol = newToAccount.currencySymbol,
                    targetAmount = newToAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = txn2Id
                )
                val updatedTxn2 = (txn2 ?: TransactionEntity(id = txn2Id, title = "", amount = 0.0, type = TransactionType.INCOME, category = "انتقالات")).copy(
                    title = "دریافت از ${newFromAccount.name}",
                    amount = newToAmount,
                    type = TransactionType.INCOME,
                    category = "انتقالات",
                    accountId = newToAccount.id,
                    kind = TransactionKind.TRANSFER,
                    note = noteToSet,
                    calculationExpression = "${newToAccount.currencySymbol} $newToAmount",
                    currencyId = newTo.currencyId,
                    currencyCode = newToAccount.currencyCode,
                    currencySymbol = newToAccount.currencySymbol,
                    exchangeRate = newRate,
                    sourceCurrencyId = newFrom.currencyId,
                    sourceCurrencyCode = newFromAccount.currencyCode,
                    sourceCurrencySymbol = newFromAccount.currencySymbol,
                    sourceAmount = newFromAmount,
                    targetCurrencyId = newTo.currencyId,
                    targetCurrencyCode = newToAccount.currencyCode,
                    targetCurrencySymbol = newToAccount.currencySymbol,
                    targetAmount = newToAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = txn1Id
                )
                transactionDao.updateTransaction(updatedTxn1)
                transactionDao.updateTransaction(updatedTxn2)
            }
        }
    }

    suspend fun updateCurrencyExchange(
        txn1Id: Long,
        txn2Id: Long,
        oldFromAccountId: Long,
        oldToAccountId: Long,
        oldFromAmount: Double,
        oldToAmount: Double,
        newFromAccountId: Long,
        newToAccountId: Long,
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        newFromAmount: Double,
        newToAmount: Double,
        newRate: Double,
        newNote: String?
    ) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                val involvedAccIds = listOf(oldFromAccountId, oldToAccountId, newFromAccountId, newToAccountId).filter { it > 0 }.distinct()
                for (accId in involvedAccIds) {
                    val acc = accountDao.getAccountById(accId) ?: error("حساب یافت نشد")
                    if (acc.isFrozen) error("حساب ${acc.name} منجمد است")
                }

                val deltas = mutableMapOf<Long, Double>()
                if (oldFromAccountId > 0) deltas[oldFromAccountId] = (deltas[oldFromAccountId] ?: 0.0) + oldFromAmount
                if (oldToAccountId > 0) deltas[oldToAccountId] = (deltas[oldToAccountId] ?: 0.0) - oldToAmount
                if (newFromAccountId > 0) deltas[newFromAccountId] = (deltas[newFromAccountId] ?: 0.0) - newFromAmount
                if (newToAccountId > 0) deltas[newToAccountId] = (deltas[newToAccountId] ?: 0.0) + newToAmount

                if (newFromAccountId == 0L) {
                    val netCashDelta = (if (oldFromAccountId == 0L) oldFromAmount else 0.0) - newFromAmount
                    val cashBal = getCashBalance(fromCurrency.id, fromCurrency.code)
                    if (cashBal + netCashDelta < 0.0) {
                        error("موجودی نقدی ${fromCurrency.name} کافی نیست")
                    }
                }

                for ((accId, delta) in deltas) {
                    if (delta < 0.0) {
                        val currentAcc = accountDao.getAccountById(accId) ?: error("حساب یافت نشد")
                        if (currentAcc.balance + delta < 0.0) {
                            error("موجودی حساب ${currentAcc.name} برای این تغییر کافی نیست")
                        }
                        val updated = accountDao.updateBalanceAtomic(accId, delta)
                        if (updated == 0) error("موجودی حساب ${currentAcc.name} برای این تغییر کافی نیست")
                    } else if (delta > 0.0) {
                        accountDao.updateBalance(accId, delta)
                    }
                }

                val txn1 = transactionDao.getTransactionById(txn1Id)
                val txn2 = transactionDao.getTransactionById(txn2Id)

                val noteToSet = cleanTransactionNote(newNote) ?: cleanTransactionNote(txn1?.note) ?: cleanTransactionNote(txn2?.note)

                val updatedTxn1 = (txn1 ?: TransactionEntity(id = txn1Id, title = "", amount = 0.0, type = TransactionType.EXPENSE, category = "Exchange")).copy(
                    title = "تبدیل به ${toCurrency.name}",
                    amount = newFromAmount,
                    type = TransactionType.EXPENSE,
                    category = "Exchange",
                    accountId = newFromAccountId,
                    kind = TransactionKind.CURRENCY_EXCHANGE,
                    note = noteToSet,
                    calculationExpression = "$newFromAmount ${fromCurrency.symbol}",
                    currencyId = fromCurrency.id,
                    currencyCode = fromCurrency.code,
                    currencySymbol = fromCurrency.symbol,
                    exchangeRate = fromCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = newFromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = newToAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = txn2Id
                )
                val updatedTxn2 = (txn2 ?: TransactionEntity(id = txn2Id, title = "", amount = 0.0, type = TransactionType.INCOME, category = "Exchange")).copy(
                    title = "دریافت از تبدیل ${fromCurrency.name}",
                    amount = newToAmount,
                    type = TransactionType.INCOME,
                    category = "Exchange",
                    accountId = newToAccountId,
                    kind = TransactionKind.CURRENCY_EXCHANGE,
                    note = noteToSet,
                    calculationExpression = "$newToAmount ${toCurrency.symbol}",
                    currencyId = toCurrency.id,
                    currencyCode = toCurrency.code,
                    currencySymbol = toCurrency.symbol,
                    exchangeRate = toCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = newFromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = newToAmount,
                    conversionRate = newRate,
                    affectsBalance = true,
                    relatedTransactionId = txn1Id
                )
                transactionDao.updateTransaction(updatedTxn1)
                transactionDao.updateTransaction(updatedTxn2)
            }
        }
    }

    suspend fun transferPersonDebt(
        fromRecipientId: Long? = null,
        fromPersonName: String,
        toRecipientId: Long? = null,
        toPersonName: String,
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        note: String? = null,
        isDebtor: Boolean = true
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            val offsetType = if (isDebtor) TransactionType.INCOME else TransactionType.EXPENSE
            val transferType = if (isDebtor) TransactionType.EXPENSE else TransactionType.INCOME

            val fromRec = if (fromRecipientId != null && fromRecipientId > 0) {
                recipientDao.getRecipientById(fromRecipientId)
            } else null
            if (fromRec == null) error("شناسه شخص مبدأ (fromRecipientId) الزامی است")

            val toRec = if (toRecipientId != null && toRecipientId > 0) {
                recipientDao.getRecipientById(toRecipientId)
            } else null
            if (toRec == null) error("شناسه شخص مقصد (toRecipientId) الزامی است")

            val actualFromPersonName = fromRec.name
            val actualToPersonName = toRec.name

            val title1 = if (isDebtor) "تسویه بدهی و انتقال به $actualToPersonName" else "تسویه طلب و حواله به $actualToPersonName"
            val title2 = if (isDebtor) "انتقال بدهی از $actualFromPersonName" else "انتقال طلب از $actualFromPersonName"

            val cleanNote = cleanTransactionNote(note)
            val id1 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = title1,
                    amount = fromAmount,
                    type = offsetType,
                    category = "انتقال حساب اشخاص",
                    accountId = 0L,
                    recipientName = actualFromPersonName,
                    recipientId = fromRec.id,
                    kind = TransactionKind.PERSON_TRANSFER,
                    timestamp = now,
                    note = cleanNote,
                    calculationExpression = "$fromAmount ${fromCurrency.symbol}",
                    currencyId = fromCurrency.id,
                    currencyCode = fromCurrency.code,
                    currencySymbol = fromCurrency.symbol,
                    exchangeRate = fromCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    affectsBalance = false
                )
            )

            val id2 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = title2,
                    amount = toAmount,
                    type = transferType,
                    category = "انتقال حساب اشخاص",
                    accountId = 0L,
                    recipientName = actualToPersonName,
                    recipientId = toRec.id,
                    kind = TransactionKind.PERSON_TRANSFER,
                    timestamp = now + 1,
                    note = cleanNote,
                    calculationExpression = "$toAmount ${toCurrency.symbol}",
                    currencyId = toCurrency.id,
                    currencyCode = toCurrency.code,
                    currencySymbol = toCurrency.symbol,
                    exchangeRate = toCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    affectsBalance = false,
                    relatedTransactionId = id1
                )
            )
            transactionDao.updateRelatedTransactionId(id1, id2)
            recipientDao.incrementTransactionCountById(fromRec.id)
            recipientDao.incrementTransactionCountById(toRec.id)
        }
    }

    suspend fun settleRecipientDebt(
        recipientId: Long,
        recipientName: String,
        debtCurrencyCode: String,
        debtAmountToSettle: Double,
        paymentAccountId: Long = 0L,
        paymentCurrencyCode: String,
        paymentAmount: Double,
        exchangeRate: Double = 1.0,
        isClaimSettlement: Boolean,
        originalTransactionId: Long? = null,
        note: String? = null,
        debtCurrencyId: Long = 0L,
        paymentCurrencyId: Long = 0L
    ): Long = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                require(debtAmountToSettle > 0.0 && paymentAmount > 0.0) {
                    "مبالغ تسویه باید بزرگتر از صفر باشند"
                }

                val allCurrs = currencyDao.getAllCurrenciesList()
                val pCurr = if (paymentCurrencyId > 0L) {
                    currencyDao.getCurrencyById(paymentCurrencyId) ?: allCurrs.firstOrNull { it.id == paymentCurrencyId }
                } else {
                    allCurrs.firstOrNull { it.code.equals(paymentCurrencyCode, ignoreCase = true) }
                }
                val finalPaymentCurrId = pCurr?.id ?: paymentCurrencyId
                val finalPaymentCode = pCurr?.code ?: paymentCurrencyCode
                val finalPaymentSymbol = pCurr?.symbol ?: finalPaymentCode

                val dCurr = if (debtCurrencyId > 0L) {
                    currencyDao.getCurrencyById(debtCurrencyId) ?: allCurrs.firstOrNull { it.id == debtCurrencyId }
                } else {
                    allCurrs.firstOrNull { it.code.equals(debtCurrencyCode, ignoreCase = true) }
                }
                val finalDebtCurrId = dCurr?.id ?: debtCurrencyId
                val finalDebtCode = dCurr?.code ?: debtCurrencyCode
                val finalDebtSymbol = dCurr?.symbol ?: finalDebtCode

                if (paymentAccountId > 0) {
                    val acc = accountDao.getAccountById(paymentAccountId) ?: error("کارت انتخابی یافت نشد")
                    if (acc.isFrozen) error("کارت انتخابی منجمد است")
                    val isCurrencyMatch = (acc.currencyId > 0 && finalPaymentCurrId > 0 && acc.currencyId == finalPaymentCurrId) ||
                        acc.currencyCode.equals(finalPaymentCode, ignoreCase = true)
                    if (!isCurrencyMatch) {
                        error("ارز کارت (${acc.currencyCode}) با ارز پرداخت (${finalPaymentCode}) همخوانی ندارد")
                    }
                    if (!isClaimSettlement && acc.balance < paymentAmount) {
                        error("موجودی کارت (${acc.balance} ${acc.currencyCode}) برای پرداخت تسویه کافی نیست")
                    }
                    val delta = if (isClaimSettlement) paymentAmount else -paymentAmount
                    val updatedRows = accountDao.updateBalanceAtomic(paymentAccountId, delta)
                    if (updatedRows == 0) {
                        error("موجودی کارت برای پرداخت تسویه کافی نیست")
                    }
                } else {
                    if (!isClaimSettlement) {
                        val cashBal = getCashBalance(finalPaymentCurrId, finalPaymentCode)
                        if (cashBal < paymentAmount) {
                            error("موجودی نقدی $finalPaymentCode ($cashBal) برای پرداخت تسویه کافی نیست")
                        }
                    }
                }

                if (originalTransactionId != null && originalTransactionId > 0) {
                    val orig = transactionDao.getTransactionById(originalTransactionId)
                    if (orig != null) {
                        val newSettled = (orig.settledAmount ?: 0.0) + debtAmountToSettle
                        val isFullySettled = newSettled >= orig.amount - 0.0001
                        transactionDao.updateTransaction(
                            orig.copy(
                                settledAmount = newSettled,
                                isSettled = isFullySettled
                            )
                        )
                    }
                }

                val cleanNote = cleanTransactionNote(note)
                val now = System.currentTimeMillis()
                val baseCurr = allCurrs.firstOrNull { it.isBaseCurrency } ?: allCurrs.firstOrNull()
                val histBaseCode = baseCurr?.code ?: "AFN"
                val histBaseRate = pCurr?.exchangeRateToBase ?: 1.0

                val rec = recipientDao.getRecipientById(recipientId) ?: error("شخص مورد نظر با شناسه $recipientId یافت نشد")
                val finalRecipientName = rec.name

                val txnType = if (isClaimSettlement) TransactionType.INCOME else TransactionType.EXPENSE
                val title = if (isClaimSettlement) "تسویه طلب از $finalRecipientName" else "تسویه بدهی به $finalRecipientName"

                val isCrossCurrency = (finalPaymentCurrId > 0 && finalDebtCurrId > 0 && finalPaymentCurrId != finalDebtCurrId) ||
                    !finalPaymentCode.equals(finalDebtCode, ignoreCase = true)

                val txn = TransactionEntity(
                    title = title,
                    amount = paymentAmount,
                    type = txnType,
                    category = "تسویه حساب",
                    accountId = paymentAccountId,
                    recipientName = finalRecipientName,
                    recipientId = rec.id,
                    kind = TransactionKind.PERSON_SETTLEMENT,
                    timestamp = now,
                    note = cleanNote,
                    currencyId = finalPaymentCurrId,
                    currencyCode = finalPaymentCode,
                    currencySymbol = finalPaymentSymbol,
                    exchangeRate = exchangeRate,
                    ledgerCurrencyId = if (isCrossCurrency) finalDebtCurrId else null,
                    ledgerCurrencyCode = if (isCrossCurrency) finalDebtCode else null,
                    ledgerCurrencySymbol = if (isCrossCurrency) finalDebtSymbol else null,
                    ledgerAmount = if (isCrossCurrency) debtAmountToSettle else null,
                    sourceCurrencyId = finalPaymentCurrId,
                    sourceCurrencyCode = finalPaymentCode,
                    sourceCurrencySymbol = finalPaymentSymbol,
                    sourceAmount = paymentAmount,
                    targetCurrencyId = finalDebtCurrId,
                    targetCurrencyCode = finalDebtCode,
                    targetCurrencySymbol = finalDebtSymbol,
                    targetAmount = debtAmountToSettle,
                    conversionRate = exchangeRate,
                    originalTransactionId = originalTransactionId,
                    settledAmount = debtAmountToSettle,
                    isSettled = true,
                    historicalBaseCurrencyCode = histBaseCode,
                    historicalExchangeRateToBase = histBaseRate,
                    affectsBalance = true
                )
                val id = transactionDao.insertTransaction(txn)
                recipientDao.incrementTransactionCountById(rec.id)
                id
            }
        }
    }

    suspend fun countTransactionsForRecipient(recipientId: Long, name: String? = null): Int = withContext(Dispatchers.IO) {
        transactionDao.countTransactionsByRecipientId(recipientId)
    }

    suspend fun countTransactionsForCategory(categoryId: Long, category: String? = null): Int = withContext(Dispatchers.IO) {
        transactionDao.countTransactionsByCategoryId(categoryId)
    }

    suspend fun countTransactionsForCategory(category: String): Int = withContext(Dispatchers.IO) {
        transactionDao.countTransactionsByCategory(category)
    }

    suspend fun countUsageForCurrency(currencyId: Long, code: String): Int = withContext(Dispatchers.IO) {
        transactionDao.countTransactionsByCurrencyIdOrCode(currencyId, code) + accountDao.countAccountsByCurrencyIdOrCode(currencyId, code)
    }

    suspend fun countUsageForCurrency(code: String): Int = countUsageForCurrency(0L, code)

    suspend fun hasCurrencyDependentData(): Boolean = withContext(Dispatchers.IO) {
        val hasTxns = transactionDao.hasAnyTransactions()
        val hasAccountBalances = accountDao.getAllAccountsList().any { kotlin.math.abs(it.balance) > 0.0001 }
        hasTxns || hasAccountBalances
    }

    suspend fun setBaseCurrency(currencyCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (hasCurrencyDependentData()) {
                return@withTransaction Result.failure(
                    IllegalStateException("امکان تغییر ارز پایه وجود ندارد زیرا اطلاعات مالی وابسته (تراکنش یا مانده حساب) در برنامه ثبت شده است.")
                )
            }
            val all = currencyDao.getAllCurrenciesList()
            val target = all.find { it.code.equals(currencyCode, ignoreCase = true) }
                ?: return@withTransaction Result.failure(IllegalArgumentException("ارز مورد نظر با کد $currencyCode یافت نشد."))

            val oldTargetRate = target.exchangeRateToBase

            val updated = all.map { c ->
                if (c.code.equals(currencyCode, ignoreCase = true)) {
                    c.copy(isBaseCurrency = true, exchangeRateToBase = 1.0, isActive = true)
                } else {
                    val newRate = if (oldTargetRate > 0.0) {
                        c.exchangeRateToBase / oldTargetRate
                    } else {
                        c.exchangeRateToBase
                    }
                    c.copy(isBaseCurrency = false, exchangeRateToBase = newRate)
                }
            }
            currencyDao.updateAllCurrencies(updated)

            // Also if default accounts exist with 0 balance, sync their currency
            val accounts = accountDao.getAllAccountsList()
            accounts.forEach { acc ->
                if (kotlin.math.abs(acc.balance) < 0.0001) {
                    accountDao.updateAccount(acc.copy(currencyId = target.id, currencyCode = target.code, currencySymbol = target.symbol))
                }
            }

            Result.success(Unit)
        }
    }

    suspend fun clearTransactionsAndBalancesOnly(): Unit = withContext(Dispatchers.IO) {
        database.withTransaction {
            transactionDao.deleteAllTransactions()
            val accounts = accountDao.getAllAccountsList()
            accounts.forEach { accountDao.updateAccount(it.copy(balance = 0.0)) }
            val recipients = recipientDao.getAllRecipientsList()
            recipients.forEach { recipientDao.updateRecipient(it.copy(transactionCount = 0)) }
        }
    }

    suspend fun countTransactionsForAccount(accountId: Long): Int = withContext(Dispatchers.IO) {
        transactionDao.countTransactionsByAccount(accountId)
    }

    suspend fun updateTransaction(oldTxn: TransactionEntity, newTxn: TransactionEntity) = withContext(Dispatchers.IO) {
        balanceLock.withLock {
            database.withTransaction {
                val cleanAmount = roundToCurrencyPrecision(newTxn.amount, newTxn.currencyCode)
                val cleanLedgerAmount = newTxn.ledgerAmount?.let {
                    roundToCurrencyPrecision(it, newTxn.ledgerCurrencyCode ?: newTxn.currencyCode)
                }

                // Check card conditions if applicable
                if (newTxn.accountId > 0) {
                    val acc = accountDao.getAccountById(newTxn.accountId) ?: error("کارت انتخابی یافت نشد")
                    if (acc.isFrozen) {
                        error("کارت انتخابی منجمد است")
                    }
                    val isMatch = (acc.currencyId > 0 && newTxn.currencyId > 0 && acc.currencyId == newTxn.currencyId) ||
                        acc.currencyCode.equals(newTxn.currencyCode, ignoreCase = true)
                    if (!isMatch) {
                        error("ارز کارت (${acc.currencyCode}) با ارز تراکنش (${newTxn.currencyCode}) همخوانی ندارد")
                    }
                }
                if (oldTxn.accountId > 0 && oldTxn.accountId != newTxn.accountId) {
                    val oldAcc = accountDao.getAccountById(oldTxn.accountId)
                    if (oldAcc != null && oldAcc.isFrozen) {
                        error("کارت قبلی (${oldAcc.name}) منجمد است")
                    }
                }

            // Check for negative balance before applying changes
            if (oldTxn.accountId == newTxn.accountId && newTxn.accountId > 0) {
                val acc = accountDao.getAccountById(newTxn.accountId)!!
                val oldDelta = if (oldTxn.affectsBalance) (if (oldTxn.type == TransactionType.INCOME) -oldTxn.amount else oldTxn.amount) else 0.0
                val newDelta = if (newTxn.affectsBalance) (if (newTxn.type == TransactionType.INCOME) cleanAmount else -cleanAmount) else 0.0
                val projected = acc.balance + oldDelta + newDelta
                if (projected < 0.0) {
                    error("موجودی کارت پس از ویرایش منفی می‌شود ($projected ${acc.currencyCode})")
                }
            } else {
                if (oldTxn.affectsBalance && oldTxn.accountId > 0 && oldTxn.type == TransactionType.INCOME) {
                    val oldAcc = accountDao.getAccountById(oldTxn.accountId)
                    if (oldAcc != null && oldAcc.balance - oldTxn.amount < 0.0) {
                        error("موجودی کارت قبلی با بازگشت این تراکنش منفی می‌شود")
                    }
                }
                if (newTxn.affectsBalance && newTxn.accountId > 0 && newTxn.type == TransactionType.EXPENSE) {
                    val newAcc = accountDao.getAccountById(newTxn.accountId)
                    if (newAcc != null && newAcc.balance - cleanAmount < 0.0) {
                        error("موجودی کارت جدید برای این هزینه کافی نیست")
                    }
                }
            }

            val isSameCashCurr = (oldTxn.currencyId > 0 && newTxn.currencyId > 0 && oldTxn.currencyId == newTxn.currencyId) ||
                oldTxn.currencyCode.equals(newTxn.currencyCode, ignoreCase = true)

            if (newTxn.accountId == 0L && newTxn.type == TransactionType.EXPENSE && newTxn.affectsBalance) {
                val currentCash = getCashBalance(newTxn.currencyId, newTxn.currencyCode)
                val oldRevertCash = if (oldTxn.accountId == 0L && oldTxn.affectsBalance && isSameCashCurr) {
                    if (oldTxn.type == TransactionType.EXPENSE) oldTxn.amount else -oldTxn.amount
                } else 0.0
                if (currentCash + oldRevertCash < cleanAmount) {
                    error("موجودی نقدی برای این ویرایش هزینه کافی نیست")
                }
            } else if (oldTxn.accountId == 0L && oldTxn.affectsBalance && oldTxn.type == TransactionType.INCOME) {
                val currentCash = getCashBalance(oldTxn.currencyId, oldTxn.currencyCode)
                val newCashIncome = if (newTxn.accountId == 0L && newTxn.affectsBalance && isSameCashCurr) {
                    if (newTxn.type == TransactionType.INCOME) cleanAmount else -cleanAmount
                } else 0.0
                if (currentCash - oldTxn.amount + newCashIncome < 0.0) {
                    error("موجودی نقدی پس از ویرایش منفی می‌شود")
                }
            }

            val cleanTxn = newTxn.copy(amount = cleanAmount, ledgerAmount = cleanLedgerAmount)
            transactionDao.updateTransaction(cleanTxn)

            if (oldTxn.recipientId != cleanTxn.recipientId) {
                if (oldTxn.recipientId != null) recipientDao.decrementTransactionCountById(oldTxn.recipientId)
                if (cleanTxn.recipientId != null) recipientDao.incrementTransactionCountById(cleanTxn.recipientId)
            }

            // Revert old transaction effect
            if (oldTxn.affectsBalance && oldTxn.accountId > 0) {
                val oldRevertDelta = if (oldTxn.type == TransactionType.INCOME) -oldTxn.amount else oldTxn.amount
                accountDao.updateBalance(oldTxn.accountId, oldRevertDelta)
            }
            // Apply new transaction effect
            if (cleanTxn.affectsBalance && cleanTxn.accountId > 0) {
                val newApplyDelta = if (cleanTxn.type == TransactionType.INCOME) cleanTxn.amount else -cleanTxn.amount
                accountDao.updateBalance(cleanTxn.accountId, newApplyDelta)
            }

            // Synchronize linked Financial Goal Transaction across the entire app
            val linkedGoalTxn = goalTransactionDao.getGoalTransactionByLinkedTxnId(oldTxn.id)
                ?: if (oldTxn.category == "واریز به هدف" || oldTxn.category == "برداشت از هدف" ||
                    cleanTxn.category == "واریز به هدف" || cleanTxn.category == "برداشت از هدف" ||
                    oldTxn.title.startsWith("انتقال به هدف") || oldTxn.title.startsWith("انتقال از هدف")) {
                    goalTransactionDao.getAllGoalTransactionsList().firstOrNull {
                        it.linkedTransactionId == oldTxn.id || (it.timestamp == oldTxn.timestamp && kotlin.math.abs(it.amount - oldTxn.amount) < 0.01)
                    }
                } else null

            if (linkedGoalTxn != null) {
                val goal = goalDao.getGoalById(linkedGoalTxn.goalId)
                if (goal != null) {
                    // Revert previous effect on goal
                    val withoutOld = if (linkedGoalTxn.type == GoalTransactionType.DEPOSIT) {
                        maxOf(0.0, goal.currentAmount - linkedGoalTxn.amount)
                    } else {
                        goal.currentAmount + linkedGoalTxn.amount
                    }

                    val newGoalTxnType = if (cleanTxn.category == "واریز به هدف" || cleanTxn.type == TransactionType.EXPENSE) {
                        GoalTransactionType.DEPOSIT
                    } else {
                        GoalTransactionType.WITHDRAWAL
                    }

                    val finalGoalAmount = if (newGoalTxnType == GoalTransactionType.DEPOSIT) {
                        withoutOld + cleanAmount
                    } else {
                        maxOf(0.0, withoutOld - cleanAmount)
                    }

                    val isCompleted = finalGoalAmount >= goal.targetAmount
                    val newStatus = if (isCompleted && goal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED
                    else if (!isCompleted && goal.status == GoalStatus.COMPLETED) GoalStatus.ACTIVE
                    else goal.status
                    val completedDate = if (isCompleted && goal.status == GoalStatus.ACTIVE) System.currentTimeMillis()
                    else if (!isCompleted && goal.status == GoalStatus.COMPLETED) null
                    else goal.completedDate

                    goalDao.updateGoal(
                        goal.copy(
                            currentAmount = finalGoalAmount,
                            status = newStatus,
                            completedDate = completedDate
                        )
                    )

                    var accountName = "بیلانس کل"
                    val newAccId = if (cleanTxn.accountId > 0) cleanTxn.accountId else null
                    if (newAccId != null) {
                        accountDao.getAccountById(newAccId)?.let { accountName = it.name }
                    }

                    goalTransactionDao.updateGoalTransaction(
                        linkedGoalTxn.copy(
                            amount = cleanAmount,
                            type = newGoalTxnType,
                            accountId = newAccId,
                            accountName = accountName,
                            note = cleanTxn.note ?: "",
                            timestamp = cleanTxn.timestamp,
                            linkedTransactionId = cleanTxn.id
                        )
                    )
                }
            }
        }
    }
}

    suspend fun addAccount(
        name: String,
        cardNumberMasked: String,
        balance: Double,
        theme: String,
        holder: String = "M. SHOAIB RAYAN",
        expiry: String = "12/28",
        currencyCode: String = "AFN",
        currencySymbol: String = "؋",
        currencyId: Long = 0L
    ): Long = withContext(Dispatchers.IO) {
        val curr = if (currencyId > 0L) currencyDao.getCurrencyById(currencyId) else currencyDao.getCurrencyByCode(currencyCode)
        val finalCurrId = curr?.id ?: currencyId
        val finalCurrCode = curr?.code ?: currencyCode
        val finalCurrSymbol = curr?.symbol ?: currencySymbol
        accountDao.insertAccount(
            AccountCardEntity(
                name = name,
                cardNumberMasked = cardNumberMasked,
                cardHolder = holder,
                expiry = expiry,
                balance = balance,
                cardColorTheme = theme,
                isFrozen = false,
                isDefault = false,
                currencyId = finalCurrId,
                currencyCode = finalCurrCode,
                currencySymbol = finalCurrSymbol
            )
        )
    }

    suspend fun updateAccount(account: AccountCardEntity) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountCardEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val txnCount = transactionDao.countTransactionsByAccount(account.id)
            if (txnCount > 0) {
                error("امکان حذف کارت '${account.name}' به دلیل وجود $txnCount تراکنش وابسته وجود ندارد.")
            }
            if (kotlin.math.abs(account.balance) > 0.0001) {
                error("امکان حذف کارت با مانده غیرصفر (${account.balance} ${account.currencyCode}) وجود ندارد.")
            }
            accountDao.deleteAccount(account)
        }
    }

    suspend fun toggleFreezeCard(account: AccountCardEntity) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account.copy(isFrozen = !account.isFrozen))
    }

    suspend fun addCategory(
        name: String,
        type: TransactionType,
        iconName: String,
        colorHex: Long,
        isActive: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(
            CategoryEntity(
                name = name,
                type = type,
                iconName = iconName,
                colorHex = colorHex,
                isActive = isActive,
                isDefault = false
            )
        )
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            categoryDao.updateCategory(category)
            transactionDao.updateCategoryNameByCategoryId(category.id, category.name)
            budgetDao.updateCategoryNameByCategoryId(category.id, category.name)
            quickActionDao.updateCategoryNameByCategoryId(category.id, category.name)
            shoppingListDao.updateCategoryNameByCategoryId(category.id, category.name)
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val count = transactionDao.countTransactionsByCategoryId(category.id)
            if (count > 0) {
                error("امکان حذف دسته‌بندی «${category.name}» وجود ندارد زیرا $count تراکنش به این دسته‌بندی متصل است.")
            }
            categoryDao.deleteCategory(category)
            budgetDao.deleteBudgetByCategoryId(category.id)
        }
    }

    suspend fun setBudget(
        category: String,
        limit: Double,
        monthYear: String = "2026-09",
        currencyCode: String = "AFN",
        categoryId: Long? = null,
        currencyId: Long = 0L
    ) = withContext(Dispatchers.IO) {
        val curr = if (currencyId > 0L) currencyDao.getCurrencyById(currencyId) else currencyDao.getCurrencyByCode(currencyCode)
        val finalCurrId = curr?.id ?: currencyId
        val finalCurrCode = curr?.code ?: currencyCode
        val existing = if (categoryId != null && categoryId > 0) {
            budgetDao.getBudgetByCategoryId(categoryId)
        } else {
            budgetDao.getBudgetByCategory(category)
        }
        if (existing != null) {
            budgetDao.updateBudget(
                existing.copy(
                    categoryId = categoryId ?: existing.categoryId,
                    category = category,
                    monthlyLimit = limit,
                    monthYear = monthYear,
                    currencyId = finalCurrId,
                    currencyCode = finalCurrCode
                )
            )
        } else {
            budgetDao.insertBudget(
                BudgetEntity(
                    categoryId = categoryId,
                    category = category,
                    monthlyLimit = limit,
                    monthYear = monthYear,
                    currencyId = finalCurrId,
                    currencyCode = finalCurrCode
                )
            )
        }
    }

    suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun convertPersonDebtCurrency(
        recipientId: Long? = null,
        personName: String,
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        accountId: Long,
        isDebtor: Boolean = true
    ) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val now = System.currentTimeMillis()
            val offsetType = if (isDebtor) TransactionType.INCOME else TransactionType.EXPENSE
            val newDebtType = if (isDebtor) TransactionType.EXPENSE else TransactionType.INCOME

            val rec = if (recipientId != null && recipientId > 0) {
                recipientDao.getRecipientById(recipientId)
            } else null
            if (rec == null) error("شناسه مخاطب (recipientId) برای تبدیل ارز شخص الزامی است")

            val actualPersonName = rec.name
            val actualRecipientId = rec.id

            val id1 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = "تسویه و تبدیل (${fromCurrency.code})",
                    amount = fromAmount,
                    type = offsetType,
                    category = "PersonExchange",
                    accountId = accountId,
                    recipientName = actualPersonName,
                    recipientId = actualRecipientId,
                    kind = TransactionKind.PERSON_CURRENCY_EXCHANGE,
                    timestamp = now,
                    note = null,
                    calculationExpression = "$fromAmount ${fromCurrency.symbol}",
                    currencyId = fromCurrency.id,
                    currencyCode = fromCurrency.code,
                    currencySymbol = fromCurrency.symbol,
                    exchangeRate = fromCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    affectsBalance = false
                )
            )

            val id2 = transactionDao.insertTransaction(
                TransactionEntity(
                    title = "ثبت معادل تبدیل (${toCurrency.code})",
                    amount = toAmount,
                    type = newDebtType,
                    category = "PersonExchange",
                    accountId = accountId,
                    recipientName = actualPersonName,
                    recipientId = actualRecipientId,
                    kind = TransactionKind.PERSON_CURRENCY_EXCHANGE,
                    timestamp = now + 1,
                    note = null,
                    calculationExpression = "$toAmount ${toCurrency.symbol}",
                    currencyId = toCurrency.id,
                    currencyCode = toCurrency.code,
                    currencySymbol = toCurrency.symbol,
                    exchangeRate = toCurrency.exchangeRateToBase,
                    sourceCurrencyId = fromCurrency.id,
                    sourceCurrencyCode = fromCurrency.code,
                    sourceCurrencySymbol = fromCurrency.symbol,
                    sourceAmount = fromAmount,
                    targetCurrencyId = toCurrency.id,
                    targetCurrencyCode = toCurrency.code,
                    targetCurrencySymbol = toCurrency.symbol,
                    targetAmount = toAmount,
                    conversionRate = rate,
                    affectsBalance = false,
                    relatedTransactionId = id1
                )
            )
            transactionDao.updateRelatedTransactionId(id1, id2)
            recipientDao.incrementTransactionCountById(actualRecipientId)
            recipientDao.incrementTransactionCountById(actualRecipientId)
        }
    }

    suspend fun addRecipient(
        name: String,
        handleOrPhone: String,
        avatarColorHex: Long = 0xFF21C6D8,
        iconName: String = "Person",
        isActive: Boolean = true,
        notes: String = "",
        isFavorite: Boolean = false,
        isAmountProtected: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        recipientDao.insertRecipient(
            RecipientEntity(
                name = name,
                handleOrPhone = handleOrPhone,
                avatarColorHex = avatarColorHex,
                iconName = iconName,
                isActive = isActive,
                notes = notes,
                isFavorite = isFavorite,
                isAmountProtected = isAmountProtected,
                transactionCount = 0
            )
        )
    }

    suspend fun updateRecipient(recipient: RecipientEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            recipientDao.updateRecipient(recipient)
            transactionDao.updateRecipientNameByRecipientId(recipient.id, recipient.name)
            quickActionDao.updateRecipientNameByRecipientId(recipient.id, recipient.name)
        }
    }

    suspend fun updateRecipientAmountProtection(id: Long, isProtected: Boolean) = withContext(Dispatchers.IO) {
        recipientDao.updateAmountProtection(id, isProtected)
    }

    suspend fun deleteRecipient(recipient: RecipientEntity) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val count = transactionDao.countTransactionsByRecipientId(recipient.id)
            if (count > 0) {
                error("امکان حذف مخاطب '${recipient.name}' به دلیل وجود $count تراکنش وابسته وجود ندارد.")
            }
            recipientDao.deleteRecipient(recipient)
        }
    }

    data class AccountBalanceIntegrityResult(
        val accountId: Long,
        val storedBalance: Double,
        val calculatedBalance: Double,
        val difference: Double,
        val isConsistent: Boolean
    )

    suspend fun verifyAccountBalanceIntegrity(accountId: Long, initialSeedBalance: Double = 0.0): AccountBalanceIntegrityResult = withContext(Dispatchers.IO) {
        val account = accountDao.getAccountById(accountId) ?: error("حساب یافت نشد")
        val netDelta = transactionDao.calculateAccountNetBalance(accountId)
        val calculated = roundToCurrencyPrecision(initialSeedBalance + netDelta, account.currencyCode)
        val diff = kotlin.math.abs(account.balance - calculated)
        AccountBalanceIntegrityResult(
            accountId = accountId,
            storedBalance = account.balance,
            calculatedBalance = calculated,
            difference = diff,
            isConsistent = diff < 0.001
        )
    }

    suspend fun seedInitialDataIfEmpty(force: Boolean = false) = withContext(Dispatchers.IO) {
        // Seed standard currencies if empty
        val existingCurrencies = currencyDao.getAllCurrencies().firstOrNull()
        if (existingCurrencies.isNullOrEmpty() || force) {
            if (force) currencyDao.deleteAllCurrencies()
            currencyDao.insertCurrency(
                CurrencyEntity(
                    name = "افغانی",
                    code = "AFN",
                    symbol = "؋",
                    flagEmoji = "🇦🇫",
                    colorHex = 0xFF10B981,
                    exchangeRateToBase = 1.0,
                    isActive = true,
                    isBaseCurrency = true,
                    decimalPlaces = 0
                )
            )
            currencyDao.insertCurrency(
                CurrencyEntity(
                    name = "دالر آمریکایی",
                    code = "USD",
                    symbol = "$",
                    flagEmoji = "🇺🇸",
                    colorHex = 0xFF3B82F6,
                    exchangeRateToBase = 70.0,
                    isActive = true,
                    isBaseCurrency = false,
                    decimalPlaces = 3
                )
            )
            currencyDao.insertCurrency(
                CurrencyEntity(
                    name = "یورو",
                    code = "EUR",
                    symbol = "€",
                    flagEmoji = "🇪🇺",
                    colorHex = 0xFF8B5CF6,
                    exchangeRateToBase = 76.5,
                    isActive = true,
                    isBaseCurrency = false,
                    decimalPlaces = 3
                )
            )
            currencyDao.insertCurrency(
                CurrencyEntity(
                    name = "تومان ایران",
                    code = "IRR",
                    symbol = "تومان",
                    flagEmoji = "🇮🇷",
                    colorHex = 0xFFEC4899,
                    exchangeRateToBase = 0.00116,
                    isActive = true,
                    isBaseCurrency = false,
                    decimalPlaces = 0
                )
            )
            currencyDao.insertCurrency(
                CurrencyEntity(
                    name = "کلدار پاکستان",
                    code = "PKR",
                    symbol = "₨",
                    flagEmoji = "🇵🇰",
                    colorHex = 0xFFF59E0B,
                    exchangeRateToBase = 0.25,
                    isActive = true,
                    isBaseCurrency = false,
                    decimalPlaces = 0
                )
            )
        } else {
            // Ensure decimal places are correctly initialized for existing USD/EUR
            existingCurrencies.forEach { c ->
                if ((c.code == "USD" || c.code == "EUR") && c.decimalPlaces < 3) {
                    currencyDao.updateCurrency(c.copy(decimalPlaces = 3))
                }
            }
        }

        // Seed default categories if empty
        val existingCategories = categoryDao.getAllCategories().firstOrNull()
        if (existingCategories.isNullOrEmpty() || force) {
            if (force) categoryDao.deleteAllCategories()
            val defaultCategories = listOf(
                CategoryEntity(name = "خوراک و غذا", type = TransactionType.EXPENSE, iconName = "Fastfood", colorHex = 0xFFEA580C, isDefault = true),
                CategoryEntity(name = "خرید و پوشاک", type = TransactionType.EXPENSE, iconName = "ShoppingBag", colorHex = 0xFF9333EA, isDefault = true),
                CategoryEntity(name = "کرایه و خانه", type = TransactionType.EXPENSE, iconName = "ReceiptLong", colorHex = 0xFFD97706, isDefault = true),
                CategoryEntity(name = "ترانسپورت و سفر", type = TransactionType.EXPENSE, iconName = "DirectionsCar", colorHex = 0xFF4F46E5, isDefault = true),
                CategoryEntity(name = "تلفن و اینترنت", type = TransactionType.EXPENSE, iconName = "PhoneAndroid", colorHex = 0xFF0284C7, isDefault = true),
                CategoryEntity(name = "اشتراک و نرم‌افزار", type = TransactionType.EXPENSE, iconName = "Subscriptions", colorHex = 0xFFDB2777, isDefault = true),
                CategoryEntity(name = "صحت و درمان", type = TransactionType.EXPENSE, iconName = "HealthAndSafety", colorHex = 0xFF10B981, isDefault = true),
                CategoryEntity(name = "مصارف عمومی", type = TransactionType.EXPENSE, iconName = "Category", colorHex = 0xFF64748B, isDefault = true),
                // Incomes
                CategoryEntity(name = "معاش و حقوق", type = TransactionType.INCOME, iconName = "Work", colorHex = 0xFF10B981, isDefault = true),
                CategoryEntity(name = "فروش و درآمد", type = TransactionType.INCOME, iconName = "Storefront", colorHex = 0xFF3B82F6, isDefault = true),
                CategoryEntity(name = "سرمایه‌گذاری", type = TransactionType.INCOME, iconName = "TrendingUp", colorHex = 0xFF8B5CF6, isDefault = true),
                CategoryEntity(name = "هدیه و پاداش", type = TransactionType.INCOME, iconName = "CardGiftcard", colorHex = 0xFFEC4899, isDefault = true),
                CategoryEntity(name = "عاید عمومی", type = TransactionType.INCOME, iconName = "Payments", colorHex = 0xFF06B6D4, isDefault = true)
            )
            for (c in defaultCategories) {
                categoryDao.insertCategory(c)
            }
        }
    }

    suspend fun wipeAllData(): Unit = withContext(Dispatchers.IO) {
        transactionDao.deleteAllTransactions()
        accountDao.deleteAllAccounts()
        recipientDao.deleteAllRecipients()
        categoryDao.deleteAllCategories()
        currencyDao.deleteAllCurrencies()
        budgetDao.deleteAllBudgets()
        goalDao.deleteAllGoals()
        goalTransactionDao.deleteAllGoalTransactions()
        quickActionDao.deleteAllQuickActions()
        shoppingListDao.deleteAllShoppingLists()
        shoppingListItemDao.deleteAllItems()
    }


    suspend fun insertQuickAction(quickAction: QuickActionEntity): Long = withContext(Dispatchers.IO) {
        quickActionDao.insertQuickAction(quickAction)
    }

    suspend fun updateQuickAction(quickAction: QuickActionEntity) = withContext(Dispatchers.IO) {
        quickActionDao.updateQuickAction(quickAction)
    }

    suspend fun deleteQuickAction(quickAction: QuickActionEntity) = withContext(Dispatchers.IO) {
        quickActionDao.deleteQuickAction(quickAction)
    }

    suspend fun deleteQuickActionById(id: Long) = withContext(Dispatchers.IO) {
        quickActionDao.deleteById(id)
    }

    suspend fun incrementQuickActionUsage(id: Long) = withContext(Dispatchers.IO) {
        quickActionDao.incrementUsage(id)
    }

    suspend fun updateQuickActionOrder(id: Long, order: Int) = withContext(Dispatchers.IO) {
        quickActionDao.updateOrder(id, order)
    }

    suspend fun reorderQuickActions(quickActions: List<QuickActionEntity>) = withContext(Dispatchers.IO) {
        quickActions.forEachIndexed { index, item ->
            quickActionDao.updateOrder(item.id, index)
        }
    }

    suspend fun getBackupData(): BackupData = withContext(Dispatchers.IO) {
        val txns = transactionDao.getAllTransactionsList()
        val accts = accountDao.getAllAccountsList()
        val recs = recipientDao.getAllRecipientsList()
        val currs = currencyDao.getAllCurrenciesList()
        val cats = categoryDao.getAllCategoriesList()
        val buds = budgetDao.getAllBudgetsList()
        val goals = goalDao.getAllGoalsList()
        val goalTxns = goalTransactionDao.getAllGoalTransactionsList()
        val quickActions = quickActionDao.getAllQuickActionsList()
        val shoppingLists = shoppingListDao.getAllShoppingListsList()
        val shoppingListItems = shoppingListItemDao.getAllItemsList()

        BackupData(
            transactions = txns,
            accounts = accts,
            recipients = recs,
            currencies = currs,
            categories = cats,
            budgets = buds,
            financialGoals = goals,
            goalTransactions = goalTxns,
            quickActions = quickActions,
            shoppingLists = shoppingLists,
            shoppingListItems = shoppingListItems,
            exportedAt = System.currentTimeMillis()
        )
    }

    suspend fun exportBackup(): String = withContext(Dispatchers.IO) {
        val data = getBackupData()
        BackupManager.exportToBakString(data)
    }

    suspend fun exportBackupBytes(): ByteArray = withContext(Dispatchers.IO) {
        val data = getBackupData()
        BackupManager.exportToBakBytes(data)
    }

    suspend fun restoreBackup(backupString: String): Result<String> = withContext(Dispatchers.IO) {
        val parseResult = BackupManager.parseFromAny(backupString)
        parseResult.mapCatching { data ->
            restoreBackupData(data).getOrThrow()
        }
    }

    suspend fun restoreBackupData(data: BackupData): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                // Clear existing data
                transactionDao.deleteAllTransactions()
                accountDao.deleteAllAccounts()
                recipientDao.deleteAllRecipients()
                currencyDao.deleteAllCurrencies()
                categoryDao.deleteAllCategories()
                budgetDao.deleteAllBudgets()
                goalDao.deleteAllGoals()
                goalTransactionDao.deleteAllGoalTransactions()
                quickActionDao.deleteAllQuickActions()
                shoppingListDao.deleteAllShoppingLists()
                shoppingListItemDao.deleteAllItems()

                // Insert restored entities
                if (data.currencies.isNotEmpty()) currencyDao.insertAllCurrencies(data.currencies)
                if (data.categories.isNotEmpty()) categoryDao.insertAllCategories(data.categories)
                if (data.accounts.isNotEmpty()) accountDao.insertAllAccounts(data.accounts)
                if (data.recipients.isNotEmpty()) recipientDao.insertAllRecipients(data.recipients)
                if (data.transactions.isNotEmpty()) transactionDao.insertAllTransactions(data.transactions)
                if (data.budgets.isNotEmpty()) budgetDao.insertAllBudgets(data.budgets)
                if (data.financialGoals.isNotEmpty()) goalDao.insertAllGoals(data.financialGoals)
                if (data.goalTransactions.isNotEmpty()) goalTransactionDao.insertAllGoalTransactions(data.goalTransactions)
                if (data.quickActions.isNotEmpty()) quickActionDao.insertAll(data.quickActions)
                if (data.shoppingLists.isNotEmpty()) shoppingListDao.insertAllShoppingLists(data.shoppingLists)
                if (data.shoppingListItems.isNotEmpty()) shoppingListItemDao.insertItems(data.shoppingListItems)

                // Atomic safe upgrade for legacy backups (if currencyId == 0, resolve from single unambiguous match)
                val allCurrs = currencyDao.getAllCurrenciesList()
                val currsByCode = allCurrs.groupBy { it.code.uppercase() }

                // Check and fix accounts
                val currentAccts = accountDao.getAllAccountsList()
                currentAccts.filter { it.currencyId == 0L }.forEach { acct ->
                    val matches = currsByCode[acct.currencyCode.uppercase()]
                    if (matches?.size == 1) {
                        accountDao.updateAccount(acct.copy(currencyId = matches[0].id))
                    }
                }

                // Check and fix transactions
                val currentTxns = transactionDao.getAllTransactionsList()
                currentTxns.filter { it.currencyId == 0L || (it.ledgerCurrencyCode != null && it.ledgerCurrencyId == null) }.forEach { txn ->
                    var updated = txn
                    if (txn.currencyId == 0L) {
                        val matches = currsByCode[txn.currencyCode.uppercase()]
                        if (matches?.size == 1) {
                            updated = updated.copy(currencyId = matches[0].id)
                        }
                    }
                    if (txn.ledgerCurrencyId == null && !txn.ledgerCurrencyCode.isNullOrBlank()) {
                        val matches = currsByCode[txn.ledgerCurrencyCode.uppercase()]
                        if (matches?.size == 1) {
                            updated = updated.copy(ledgerCurrencyId = matches[0].id)
                        }
                    }
                    if (updated != txn) {
                        transactionDao.updateTransaction(updated)
                    }
                }

                // Check and fix budgets
                val currentBudgets = budgetDao.getAllBudgetsList()
                currentBudgets.filter { it.currencyId == 0L }.forEach { b ->
                    val matches = currsByCode[b.currencyCode.uppercase()]
                    if (matches?.size == 1) {
                        budgetDao.updateBudget(b.copy(currencyId = matches[0].id))
                    }
                }

                // Check and fix goals
                val currentGoals = goalDao.getAllGoalsList()
                currentGoals.filter { it.currencyId == 0L }.forEach { g ->
                    val matches = currsByCode[g.currencyCode.uppercase()]
                    if (matches?.size == 1) {
                        goalDao.updateGoal(g.copy(currencyId = matches[0].id))
                    }
                }

                // Check and fix goal transactions
                val currentGoalTxns = goalTransactionDao.getAllGoalTransactionsList()
                currentGoalTxns.filter { it.currencyId == 0L }.forEach { gt ->
                    val matches = currsByCode[gt.currencyCode.uppercase()]
                    if (matches?.size == 1) {
                        goalTransactionDao.updateGoalTransaction(gt.copy(currencyId = matches[0].id))
                    }
                }

                // Check and fix quick actions
                val currentQas = quickActionDao.getAllQuickActionsList()
                currentQas.filter { it.currencyId == 0L }.forEach { qa ->
                    val matches = currsByCode[qa.currencyCode.uppercase()]
                    if (matches?.size == 1) {
                        quickActionDao.updateQuickAction(qa.copy(currencyId = matches[0].id))
                    }
                }

                // Check and fix shopping lists
                val currentSls = shoppingListDao.getAllShoppingListsList()
                currentSls.filter { it.currencyId == 0L }.forEach { sl ->
                    val matches = currsByCode[sl.currencyCode.uppercase()]
                    if (matches?.size == 1) {
                        shoppingListDao.updateShoppingList(sl.copy(currencyId = matches[0].id))
                    }
                }
            }

            "${data.transactions.size} معامله، ${data.accounts.size} حساب، ${data.financialGoals.size} هدف مالی و ${data.recipients.size} مخاطب با موفقیت کامل بازیابی شدند."
        }
    }

    // ==========================================
    // FINANCIAL GOALS OPERATIONS (اهداف مالی)
    // ==========================================

    fun getGoalByIdFlow(id: Long): Flow<FinancialGoalEntity?> = goalDao.getGoalByIdFlow(id)

    suspend fun getGoalById(id: Long): FinancialGoalEntity? = withContext(Dispatchers.IO) {
        goalDao.getGoalById(id)
    }

    fun getTransactionsForGoal(goalId: Long): Flow<List<GoalTransactionEntity>> =
        goalTransactionDao.getTransactionsForGoal(goalId)

    suspend fun addGoal(goal: FinancialGoalEntity, initialAccountId: Long? = null): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val goalCurr = if (goal.currencyId > 0L) currencyDao.getCurrencyById(goal.currencyId) else currencyDao.getCurrencyByCode(goal.currencyCode)
            val finalGoalCurrId = goalCurr?.id ?: goal.currencyId
            val finalGoalCurrCode = goalCurr?.code ?: goal.currencyCode
            val finalGoalCurrSymbol = goalCurr?.symbol ?: goal.currencySymbol

            if (goal.currentAmount > 0) {
                if (initialAccountId != null && initialAccountId > 0) {
                    val account = accountDao.getAccountById(initialAccountId)
                        ?: error("حساب انتخاب شده یافت نشد")
                    val isCurrMatch = (account.currencyId > 0 && finalGoalCurrId > 0 && account.currencyId == finalGoalCurrId) ||
                        account.currencyCode.equals(finalGoalCurrCode, ignoreCase = true)
                    require(isCurrMatch) {
                        "ارز حساب (${account.currencyCode}) با ارز هدف ($finalGoalCurrCode) مطابقت ندارد"
                    }
                    require(account.balance >= goal.currentAmount) {
                        "موجودی حساب ${account.name} (${account.balance} ${account.currencyCode}) برای واریز به هدف کافی نیست"
                    }
                } else {
                    val cashBal = getCashBalance(finalGoalCurrId, finalGoalCurrCode)
                    require(cashBal >= goal.currentAmount) {
                        "موجودی نقدی ($cashBal $finalGoalCurrCode) برای واریز به هدف کافی نیست"
                    }
                }
            }
            val goalToSave = goal.copy(
                currencyId = finalGoalCurrId,
                currencyCode = finalGoalCurrCode,
                currencySymbol = finalGoalCurrSymbol
            )
            val insertedGoalId = goalDao.insertGoal(goalToSave)
            if (goal.currentAmount > 0) {
                var accountName = "بیلانس کل"
                var txnId: Long? = null
                if (initialAccountId != null && initialAccountId > 0) {
                    val account = accountDao.getAccountById(initialAccountId)!!
                    accountName = account.name
                    accountDao.updateBalance(initialAccountId, -goal.currentAmount)
                    txnId = transactionDao.insertTransaction(
                        TransactionEntity(
                            title = "انتقال به هدف: ${goal.title}",
                            amount = goal.currentAmount,
                            type = TransactionType.TRANSFER,
                            category = "واریز به هدف",
                            accountId = initialAccountId,
                            currencyId = finalGoalCurrId,
                            currencyCode = finalGoalCurrCode,
                            currencySymbol = finalGoalCurrSymbol,
                            note = null,
                            timestamp = System.currentTimeMillis(),
                            affectsBalance = true,
                            kind = TransactionKind.GOAL_DEPOSIT
                        )
                    )
                } else {
                    txnId = transactionDao.insertTransaction(
                        TransactionEntity(
                            title = "انتقال به هدف: ${goal.title}",
                            amount = goal.currentAmount,
                            type = TransactionType.TRANSFER,
                            category = "واریز به هدف",
                            accountId = 0L,
                            currencyId = finalGoalCurrId,
                            currencyCode = finalGoalCurrCode,
                            currencySymbol = finalGoalCurrSymbol,
                            note = null,
                            timestamp = System.currentTimeMillis(),
                            affectsBalance = true,
                            kind = TransactionKind.GOAL_DEPOSIT
                        )
                    )
                }
                goalTransactionDao.insertGoalTransaction(
                    GoalTransactionEntity(
                        goalId = insertedGoalId,
                        amount = goal.currentAmount,
                        currencyId = finalGoalCurrId,
                        currencyCode = finalGoalCurrCode,
                        currencySymbol = finalGoalCurrSymbol,
                        type = GoalTransactionType.DEPOSIT,
                        accountId = initialAccountId,
                        accountName = accountName,
                        note = "",
                        timestamp = System.currentTimeMillis(),
                        linkedTransactionId = txnId
                    )
                )
            }
            insertedGoalId
        }
    }

    suspend fun updateGoal(goal: FinancialGoalEntity) = withContext(Dispatchers.IO) {
        val goalCurr = if (goal.currencyId > 0L) currencyDao.getCurrencyById(goal.currencyId) else currencyDao.getCurrencyByCode(goal.currencyCode)
        val finalGoalCurrId = goalCurr?.id ?: goal.currencyId
        val finalGoalCurrCode = goalCurr?.code ?: goal.currencyCode
        val finalGoalCurrSymbol = goalCurr?.symbol ?: goal.currencySymbol
        goalDao.updateGoal(
            goal.copy(
                currencyId = finalGoalCurrId,
                currencyCode = finalGoalCurrCode,
                currencySymbol = finalGoalCurrSymbol
            )
        )
    }

    suspend fun deleteGoal(id: Long) = withContext(Dispatchers.IO) {
        database.withTransaction {
            goalTransactionDao.deleteTransactionsByGoalId(id)
            goalDao.deleteGoalById(id)
        }
    }

    suspend fun depositToGoal(
        goalId: Long,
        amount: Double,
        accountId: Long?,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            balanceLock.withLock {
                database.withTransaction {
                    val goal = goalDao.getGoalById(goalId) ?: error("هدف مالی یافت نشد")
                    val goalCurr = if (goal.currencyId > 0L) currencyDao.getCurrencyById(goal.currencyId) else currencyDao.getCurrencyByCode(goal.currencyCode)
                    val goalCurrId = goalCurr?.id ?: goal.currencyId
                    val goalCurrCode = goalCurr?.code ?: goal.currencyCode
                    val goalCurrSymbol = goalCurr?.symbol ?: goal.currencySymbol

                    require(amount > 0) { "مبلغ واریزی باید بزرگتر از صفر باشد" }

                    val cleanGoalNote = cleanTransactionNote(note)
                    var accountName = "بیلانس کل"
                    var txnId: Long? = null
                    if (accountId != null && accountId > 0) {
                        val account = accountDao.getAccountById(accountId) ?: error("حساب انتخاب شده معتبر نیست")
                        if (account.isFrozen) error("کارت انتخابی منجمد است")
                        val isCurrMatch = (account.currencyId > 0 && goalCurrId > 0 && account.currencyId == goalCurrId) ||
                            account.currencyCode.equals(goalCurrCode, ignoreCase = true)
                        require(isCurrMatch) {
                            "ارز حساب (${account.currencyCode}) با ارز هدف ($goalCurrCode) مطابقت ندارد"
                        }
                        require(account.balance >= amount) { "موجودی حساب ${account.name} (${account.balance} ${account.currencyCode}) برای واریز به هدف کافی نیست" }
                        accountName = account.name
                        val updatedRows = accountDao.updateBalanceAtomic(accountId, -amount)
                        if (updatedRows == 0) {
                            error("موجودی حساب ${account.name} برای واریز به هدف کافی نیست")
                        }
                        txnId = transactionDao.insertTransaction(
                            TransactionEntity(
                                title = "انتقال به هدف: ${goal.title}",
                                amount = amount,
                                type = TransactionType.TRANSFER,
                                category = "واریز به هدف",
                                accountId = accountId,
                                currencyId = goalCurrId,
                                currencyCode = goalCurrCode,
                                currencySymbol = goalCurrSymbol,
                                note = cleanGoalNote,
                                timestamp = System.currentTimeMillis(),
                                affectsBalance = true,
                                kind = TransactionKind.GOAL_DEPOSIT
                            )
                        )
                    } else {
                        // کسر از بیلانس کل (موجودی عمومی / نقد)
                        val cashBal = getCashBalance(goalCurrId, goalCurrCode)
                        require(cashBal >= amount) { "موجودی نقدی ($cashBal $goalCurrCode) برای واریز به این هدف کافی نیست" }
                        accountName = "بیلانس کل"
                        txnId = transactionDao.insertTransaction(
                            TransactionEntity(
                                title = "انتقال به هدف: ${goal.title}",
                                amount = amount,
                                type = TransactionType.TRANSFER,
                                category = "واریز به هدف",
                                accountId = 0L,
                                currencyId = goalCurrId,
                                currencyCode = goalCurrCode,
                                currencySymbol = goalCurrSymbol,
                                note = cleanGoalNote,
                                timestamp = System.currentTimeMillis(),
                                affectsBalance = true,
                                kind = TransactionKind.GOAL_DEPOSIT
                            )
                        )
                    }

                    goalTransactionDao.insertGoalTransaction(
                        GoalTransactionEntity(
                            goalId = goalId,
                            amount = amount,
                            currencyId = goalCurrId,
                            currencyCode = goalCurrCode,
                            currencySymbol = goalCurrSymbol,
                            type = GoalTransactionType.DEPOSIT,
                            accountId = accountId,
                            accountName = accountName,
                            note = cleanGoalNote ?: "",
                            timestamp = System.currentTimeMillis(),
                            linkedTransactionId = txnId
                        )
                    )

                    val updatedAmount = goal.currentAmount + amount
                    val isCompleted = updatedAmount >= goal.targetAmount
                    val newStatus = if (isCompleted && goal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED else goal.status
                    val completedDate = if (isCompleted && goal.status == GoalStatus.ACTIVE) System.currentTimeMillis() else goal.completedDate

                    goalDao.updateGoal(
                        goal.copy(
                            currencyId = goalCurrId,
                            currencyCode = goalCurrCode,
                            currencySymbol = goalCurrSymbol,
                            currentAmount = updatedAmount,
                            status = newStatus,
                            completedDate = completedDate
                        )
                    )
                }
            }
        }
    }

    suspend fun withdrawFromGoal(
        goalId: Long,
        amount: Double,
        destinationAccountId: Long?,
        note: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            balanceLock.withLock {
                database.withTransaction {
                    val goal = goalDao.getGoalById(goalId) ?: error("هدف مالی یافت نشد")
                    val goalCurr = if (goal.currencyId > 0L) currencyDao.getCurrencyById(goal.currencyId) else currencyDao.getCurrencyByCode(goal.currencyCode)
                    val goalCurrId = goalCurr?.id ?: goal.currencyId
                    val goalCurrCode = goalCurr?.code ?: goal.currencyCode
                    val goalCurrSymbol = goalCurr?.symbol ?: goal.currencySymbol

                    require(amount > 0) { "مبلغ برداشت باید بزرگتر از صفر باشد" }
                    require(goal.currentAmount >= amount) { "موجودی هدف کافی نیست. موجودی فعلی: ${goal.currentAmount}" }

                    val cleanGoalNote = cleanTransactionNote(note)
                    var accountName = "بیلانس کل"
                    var txnId: Long? = null
                    if (destinationAccountId != null && destinationAccountId > 0) {
                        val account = accountDao.getAccountById(destinationAccountId) ?: error("حساب مقصد معتبر نیست")
                        if (account.isFrozen) error("کارت مقصد منجمد است")
                        accountName = account.name
                        accountDao.updateBalance(destinationAccountId, amount)
                        txnId = transactionDao.insertTransaction(
                            TransactionEntity(
                                title = "انتقال از هدف: ${goal.title}",
                                amount = amount,
                                type = TransactionType.TRANSFER,
                                category = "برداشت از هدف",
                                accountId = destinationAccountId,
                                currencyId = goalCurrId,
                                currencyCode = goalCurrCode,
                                currencySymbol = goalCurrSymbol,
                                note = cleanGoalNote,
                                timestamp = System.currentTimeMillis(),
                                affectsBalance = true,
                                kind = TransactionKind.GOAL_WITHDRAW
                            )
                        )
                    } else {
                        // واریز برگشتی به بیلانس کل (موجودی عمومی / نقد)
                        accountName = "بیلانس کل"
                        txnId = transactionDao.insertTransaction(
                            TransactionEntity(
                                title = "انتقال از هدف: ${goal.title}",
                                amount = amount,
                                type = TransactionType.TRANSFER,
                                category = "برداشت از هدف",
                                accountId = 0L,
                                currencyId = goalCurrId,
                                currencyCode = goalCurrCode,
                                currencySymbol = goalCurrSymbol,
                                note = cleanGoalNote,
                                timestamp = System.currentTimeMillis(),
                                affectsBalance = true,
                                kind = TransactionKind.GOAL_WITHDRAW
                            )
                        )
                    }

                    goalTransactionDao.insertGoalTransaction(
                        GoalTransactionEntity(
                            goalId = goalId,
                            amount = amount,
                            currencyId = goalCurrId,
                            currencyCode = goalCurrCode,
                            currencySymbol = goalCurrSymbol,
                            type = GoalTransactionType.WITHDRAWAL,
                            accountId = destinationAccountId,
                            accountName = accountName,
                            note = cleanGoalNote ?: "",
                            timestamp = System.currentTimeMillis(),
                            linkedTransactionId = txnId
                        )
                    )

                    val updatedAmount = maxOf(0.0, goal.currentAmount - amount)
                    val newStatus = if (goal.status == GoalStatus.COMPLETED && updatedAmount < goal.targetAmount) GoalStatus.ACTIVE else goal.status
                    val completedDate = if (newStatus == GoalStatus.ACTIVE) null else goal.completedDate

                    goalDao.updateGoal(
                        goal.copy(
                            currencyId = goalCurrId,
                            currencyCode = goalCurrCode,
                            currencySymbol = goalCurrSymbol,
                            currentAmount = updatedAmount,
                            status = newStatus,
                            completedDate = completedDate
                        )
                    )
                }
            }
        }
    }

    suspend fun updateGoalStatus(goalId: Long, newStatus: GoalStatus) = withContext(Dispatchers.IO) {
        val completedDate = if (newStatus == GoalStatus.COMPLETED) System.currentTimeMillis() else null
        goalDao.updateGoalStatus(goalId, newStatus, completedDate)
    }

    suspend fun deleteGoalTransaction(txnId: Long, revertBalance: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            balanceLock.withLock {
                database.withTransaction {
                    deleteGoalTransactionInternal(txnId, revertBalance)
                }
            }
        }
    }

    private suspend fun deleteGoalTransactionInternal(txnId: Long, revertBalance: Boolean = true) {
        val txn = goalTransactionDao.getGoalTransactionById(txnId) ?: error("تراکنش هدف یافت نشد")
        val goal = goalDao.getGoalById(txn.goalId) ?: error("هدف مالی یافت نشد")

        if (revertBalance) {
            if (txn.type == GoalTransactionType.DEPOSIT) {
                // واریز به هدف بوده است؛ پس برای بازگردانی، موجودی هدف کم می‌شود و حساب شارژ/برگردانده می‌شود
                val updatedAmount = maxOf(0.0, goal.currentAmount - txn.amount)
                val newStatus = if (goal.status == GoalStatus.COMPLETED && updatedAmount < goal.targetAmount) GoalStatus.ACTIVE else goal.status
                val completedDate = if (newStatus == GoalStatus.ACTIVE) null else goal.completedDate
                goalDao.updateGoal(
                    goal.copy(
                        currentAmount = updatedAmount,
                        status = newStatus,
                        completedDate = completedDate
                    )
                )

                if (txn.accountId != null && txn.accountId > 0) {
                    val acc = accountDao.getAccountById(txn.accountId)
                    if (acc != null && acc.isFrozen) error("حساب مرتبط منجمد است")
                    accountDao.updateBalance(txn.accountId, txn.amount)
                }
                txn.linkedTransactionId?.let { linkId ->
                    transactionDao.deleteById(linkId)
                }
            } else {
                // برداشت از هدف بوده است؛ پس برای بازگردانی، موجودی هدف زیاد می‌شود و حساب کسر می‌شود
                val updatedAmount = goal.currentAmount + txn.amount
                val isCompleted = updatedAmount >= goal.targetAmount
                val newStatus = if (isCompleted && goal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED else goal.status
                val completedDate = if (isCompleted && goal.status == GoalStatus.ACTIVE) System.currentTimeMillis() else goal.completedDate
                goalDao.updateGoal(
                    goal.copy(
                        currentAmount = updatedAmount,
                        status = newStatus,
                        completedDate = completedDate
                    )
                )

                if (txn.accountId != null && txn.accountId > 0) {
                    val acc = accountDao.getAccountById(txn.accountId)
                    if (acc != null && acc.isFrozen) error("حساب مرتبط منجمد است")
                    val updated = accountDao.updateBalanceAtomic(txn.accountId, -txn.amount)
                    if (updated == 0) error("موجودی حساب برای بازگردانی کافی نیست")
                }
                txn.linkedTransactionId?.let { linkId ->
                    transactionDao.deleteById(linkId)
                }
            }
        }

        goalTransactionDao.deleteGoalTransaction(txn)
    }

    suspend fun clearGoalTransactions(goalId: Long, revertBalance: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                if (revertBalance) {
                    val list = goalTransactionDao.getAllGoalTransactionsList().filter { it.goalId == goalId }
                    for (txn in list) {
                        txn.linkedTransactionId?.let { linkId ->
                            transactionDao.deleteById(linkId)
                        }
                        if (txn.accountId != null && txn.accountId > 0) {
                            val delta = if (txn.type == GoalTransactionType.DEPOSIT) txn.amount else -txn.amount
                            accountDao.updateBalance(txn.accountId, delta)
                        }
                    }
                    val goal = goalDao.getGoalById(goalId)
                    if (goal != null) {
                        goalDao.updateGoal(goal.copy(currentAmount = 0.0, status = GoalStatus.ACTIVE, completedDate = null))
                    }
                }
                goalTransactionDao.deleteTransactionsByGoalId(goalId)
            }
        }
    }

    suspend fun findGoalTransactionForTransaction(txn: TransactionEntity): GoalTransactionEntity? = withContext(Dispatchers.IO) {
        goalTransactionDao.getGoalTransactionByLinkedTxnId(txn.id) ?: run {
            if (txn.category == "واریز به هدف" || txn.category == "برداشت از هدف" || txn.title.startsWith("انتقال به هدف") || txn.title.startsWith("انتقال از هدف")) {
                val list = goalTransactionDao.getAllGoalTransactionsList()
                list.firstOrNull { it.linkedTransactionId == txn.id || (it.timestamp == txn.timestamp && kotlin.math.abs(it.amount - txn.amount) < 0.01) }
            } else null
        }
    }

    suspend fun updateGoalTransaction(
        goalTransactionId: Long,
        newGoalId: Long,
        newAmount: Double,
        newType: GoalTransactionType,
        newAccountId: Long?,
        newNote: String,
        newTimestamp: Long = System.currentTimeMillis()
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                require(newAmount > 0) { "مبلغ باید بزرگتر از صفر باشد" }
                val newGoal = goalDao.getGoalById(newGoalId) ?: error("هدف مالی انتخابی یافت نشد")
                var oldGoalTxn = if (goalTransactionId > 0) goalTransactionDao.getGoalTransactionById(goalTransactionId) else null
                if (oldGoalTxn == null) {
                    val list = goalTransactionDao.getAllGoalTransactionsList()
                    oldGoalTxn = list.firstOrNull { it.id == goalTransactionId }
                        ?: list.firstOrNull { it.goalId == newGoalId && kotlin.math.abs(it.amount - newAmount) < 0.01 }
                        ?: list.firstOrNull { it.goalId == newGoalId }
                }
                val oldGoal = if (oldGoalTxn != null) (goalDao.getGoalById(oldGoalTxn.goalId) ?: newGoal) else newGoal

                // 1. Revert effect of oldGoalTxn on accounts
                if (oldGoalTxn != null && oldGoalTxn.accountId != null && oldGoalTxn.accountId > 0) {
                    val oldRevertDelta = if (oldGoalTxn.type == GoalTransactionType.DEPOSIT) oldGoalTxn.amount else -oldGoalTxn.amount
                    accountDao.updateBalance(oldGoalTxn.accountId, oldRevertDelta)
                }

                // 2. Apply effect of new transaction on account
                var accountName = "بیلانس کل"
                if (newAccountId != null && newAccountId > 0) {
                    val acc = accountDao.getAccountById(newAccountId) ?: error("حساب انتخابی معتبر نیست")
                    accountName = acc.name
                    val newApplyDelta = if (newType == GoalTransactionType.DEPOSIT) -newAmount else newAmount
                    accountDao.updateBalance(newAccountId, newApplyDelta)
                }

                // 3. Revert and apply effect on goals
                if (oldGoalTxn == null) {
                    // Completely new goal operation
                    val finalAmount = if (newType == GoalTransactionType.DEPOSIT) newGoal.currentAmount + newAmount
                    else maxOf(0.0, newGoal.currentAmount - newAmount)
                    val isCompleted = finalAmount >= newGoal.targetAmount
                    val newStatus = if (isCompleted && newGoal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED
                    else if (!isCompleted && newGoal.status == GoalStatus.COMPLETED) GoalStatus.ACTIVE
                    else newGoal.status
                    val completedDate = if (isCompleted && newGoal.status == GoalStatus.ACTIVE) System.currentTimeMillis()
                    else if (!isCompleted && newGoal.status == GoalStatus.COMPLETED) null
                    else newGoal.completedDate
                    goalDao.updateGoal(newGoal.copy(currentAmount = finalAmount, status = newStatus, completedDate = completedDate))
                } else if (oldGoal.id == newGoal.id) {
                    // Same goal
                    val withoutOld = if (oldGoalTxn.type == GoalTransactionType.DEPOSIT) {
                        maxOf(0.0, oldGoal.currentAmount - oldGoalTxn.amount)
                    } else {
                        oldGoal.currentAmount + oldGoalTxn.amount
                    }
                    val finalAmount = if (newType == GoalTransactionType.DEPOSIT) {
                        withoutOld + newAmount
                    } else {
                        maxOf(0.0, withoutOld - newAmount)
                    }
                    val isCompleted = finalAmount >= oldGoal.targetAmount
                    val newStatus = if (isCompleted && oldGoal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED
                        else if (!isCompleted && oldGoal.status == GoalStatus.COMPLETED) GoalStatus.ACTIVE
                        else oldGoal.status
                    val completedDate = if (isCompleted && oldGoal.status == GoalStatus.ACTIVE) System.currentTimeMillis()
                        else if (!isCompleted && oldGoal.status == GoalStatus.COMPLETED) null
                        else oldGoal.completedDate

                    goalDao.updateGoal(
                        oldGoal.copy(
                            currentAmount = finalAmount,
                            status = newStatus,
                            completedDate = completedDate
                        )
                    )
                } else {
                    // Different goal
                    // Revert old goal
                    val revertedOldAmount = if (oldGoalTxn.type == GoalTransactionType.DEPOSIT) {
                        maxOf(0.0, oldGoal.currentAmount - oldGoalTxn.amount)
                    } else {
                        oldGoal.currentAmount + oldGoalTxn.amount
                    }
                    val oldCompleted = revertedOldAmount >= oldGoal.targetAmount
                    val oldStatus = if (oldCompleted && oldGoal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED
                        else if (!oldCompleted && oldGoal.status == GoalStatus.COMPLETED) GoalStatus.ACTIVE
                        else oldGoal.status
                    val oldCompDate = if (!oldCompleted && oldGoal.status == GoalStatus.COMPLETED) null else oldGoal.completedDate
                    goalDao.updateGoal(oldGoal.copy(currentAmount = revertedOldAmount, status = oldStatus, completedDate = oldCompDate))

                    // Apply to new goal
                    val newFinalAmount = if (newType == GoalTransactionType.DEPOSIT) {
                        newGoal.currentAmount + newAmount
                    } else {
                        maxOf(0.0, newGoal.currentAmount - newAmount)
                    }
                    val newCompleted = newFinalAmount >= newGoal.targetAmount
                    val newGoalStatus = if (newCompleted && newGoal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED
                        else if (!newCompleted && newGoal.status == GoalStatus.COMPLETED) GoalStatus.ACTIVE
                        else newGoal.status
                    val newCompDate = if (newCompleted && newGoal.status == GoalStatus.ACTIVE) System.currentTimeMillis()
                        else if (!newCompleted && newGoal.status == GoalStatus.COMPLETED) null
                        else newGoal.completedDate
                    goalDao.updateGoal(newGoal.copy(currentAmount = newFinalAmount, status = newGoalStatus, completedDate = newCompDate))
                }

                // 4. Update or Insert linked TransactionEntity
                val cleanEditNote = cleanTransactionNote(newNote)
                val linkedTxn = oldGoalTxn?.linkedTransactionId?.let { transactionDao.getTransactionById(it) }
                val txnTitle = if (newType == GoalTransactionType.DEPOSIT) "انتقال به هدف: ${newGoal.title}" else "انتقال از هدف: ${newGoal.title}"
                val txnCategory = if (newType == GoalTransactionType.DEPOSIT) "واریز به هدف" else "برداشت از هدف"
                val txnType = TransactionType.TRANSFER
                val txnKind = if (newType == GoalTransactionType.DEPOSIT) TransactionKind.GOAL_DEPOSIT else TransactionKind.GOAL_WITHDRAW

                val finalTxnId: Long = if (linkedTxn != null) {
                    transactionDao.updateTransaction(
                        linkedTxn.copy(
                            title = txnTitle,
                            amount = newAmount,
                            type = txnType,
                            kind = txnKind,
                            category = txnCategory,
                            accountId = newAccountId ?: 0L,
                            currencyCode = newGoal.currencyCode,
                            currencySymbol = newGoal.currencySymbol,
                            note = cleanEditNote,
                            timestamp = newTimestamp,
                            affectsBalance = true
                        )
                    )
                    linkedTxn.id
                } else {
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            title = txnTitle,
                            amount = newAmount,
                            type = txnType,
                            kind = txnKind,
                            category = txnCategory,
                            accountId = newAccountId ?: 0L,
                            currencyCode = newGoal.currencyCode,
                            currencySymbol = newGoal.currencySymbol,
                            note = cleanEditNote,
                            timestamp = newTimestamp,
                            affectsBalance = true
                        )
                    )
                }

                // 5. Update or Insert GoalTransactionEntity
                if (oldGoalTxn != null) {
                    goalTransactionDao.updateGoalTransaction(
                        oldGoalTxn.copy(
                            goalId = newGoal.id,
                            amount = newAmount,
                            currencyCode = newGoal.currencyCode,
                            currencySymbol = newGoal.currencySymbol,
                            type = newType,
                            accountId = newAccountId,
                            accountName = accountName,
                            note = cleanEditNote ?: "",
                            timestamp = newTimestamp,
                            linkedTransactionId = finalTxnId
                        )
                    )
                } else {
                    goalTransactionDao.insertGoalTransaction(
                        GoalTransactionEntity(
                            goalId = newGoal.id,
                            amount = newAmount,
                            currencyCode = newGoal.currencyCode,
                            currencySymbol = newGoal.currencySymbol,
                            type = newType,
                            accountId = newAccountId,
                            accountName = accountName,
                            note = cleanEditNote ?: "",
                            timestamp = newTimestamp,
                            linkedTransactionId = finalTxnId
                        )
                    )
                }
                Unit
            }
        }
    }

    // ==========================================
    // SHOPPING LISTS (لیست‌های خرید و ثبت مصارف مرتبط)
    // ==========================================

    suspend fun saveShoppingList(
        list: ShoppingListEntity,
        items: List<ShoppingListItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        database.withTransaction {
            val calculatedItemsSum = items.sumOf { it.price }
            val finalTotal = if (!list.useItemizedSum && (list.manualTotalAmount ?: 0.0) > 0.0) {
                list.manualTotalAmount ?: 0.0
            } else {
                calculatedItemsSum
            }
            val allPurchased = items.isNotEmpty() && items.all { it.isPurchased }
            val listToSave = list.copy(
                totalAmount = finalTotal,
                isCompleted = if (items.isNotEmpty()) allPurchased else list.isCompleted
            )

            val listId = if (listToSave.id == 0L) {
                shoppingListDao.insertShoppingList(listToSave)
            } else {
                shoppingListDao.updateShoppingList(listToSave)
                listToSave.id
            }

            // Replace items for this list
            shoppingListItemDao.deleteItemsByListId(listId)
            val itemsWithListId = items.mapIndexed { index, item ->
                item.copy(id = 0L, listId = listId, displayOrder = index)
            }
            shoppingListItemDao.insertItems(itemsWithListId)

            // If this list has a linked expense transaction, automatically sync amount and details!
            if (listToSave.linkedTransactionId != null) {
                syncLinkedExpenseTransaction(listId, listToSave, itemsWithListId, finalTotal)
            }

            listId
        }
    }

    suspend fun logShoppingListAsExpense(
        listId: Long,
        accountId: Long?,
        category: String,
        timestamp: Long,
        customNote: String? = null,
        categoryId: Long? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                val list = shoppingListDao.getShoppingListById(listId)
                    ?: error("لیست خرید یافت نشد")
                val items = shoppingListItemDao.getItemsForListSync(listId)

                val calculatedItemsSum = items.sumOf { it.price }
                val totalAmount = if (!list.useItemizedSum && (list.manualTotalAmount ?: 0.0) > 0.0) {
                    list.manualTotalAmount ?: 0.0
                } else {
                    calculatedItemsSum
                }

                if (totalAmount <= 0.0) {
                    error("مبلغ لیست خرید صفر است. لطفاً قیمت اقلام یا قیمت کلی را وارد نمایید.")
                }

                val finalNote = cleanTransactionNote(customNote)

                val accId = accountId ?: 0L
                val accountName = if (accId > 0) {
                    accountDao.getAccountById(accId)?.name ?: "کارت بانکی"
                } else {
                    "بیلانس کل"
                }

                val resolvedCatId = categoryId ?: list.categoryId

                val txnId = addTransaction(
                    title = "خرید: ${list.title}",
                    amount = totalAmount,
                    type = TransactionType.EXPENSE,
                    category = category.ifBlank { list.category },
                    accountId = accId,
                    recipientName = null,
                    categoryId = resolvedCatId,
                    note = finalNote,
                    calculationExpression = "${list.currencySymbol} $totalAmount",
                    currencyId = list.currencyId,
                    currencyCode = list.currencyCode,
                    currencySymbol = list.currencySymbol,
                    exchangeRate = (if (list.currencyId > 0L) currencyDao.getCurrencyById(list.currencyId) else currencyDao.getCurrencyByCode(list.currencyCode))?.exchangeRateToBase ?: 1.0,
                    timestamp = timestamp,
                    affectsBalance = true
                )

                val updatedList = list.copy(
                    isLoggedAsExpense = true,
                    linkedTransactionId = txnId,
                    accountId = if (accId > 0) accId else null,
                    accountName = accountName,
                    categoryId = resolvedCatId,
                    category = category.ifBlank { list.category },
                    purchaseDate = timestamp,
                    totalAmount = totalAmount,
                    isCompleted = true
                )
                shoppingListDao.updateShoppingList(updatedList)

                txnId
            }
        }
    }

    private suspend fun syncLinkedExpenseTransaction(
        listId: Long,
        list: ShoppingListEntity,
        items: List<ShoppingListItemEntity>,
        newTotal: Double
    ) {
        val txnId = list.linkedTransactionId ?: return
        val oldTxn = transactionDao.getTransactionById(txnId) ?: return

        val accId = list.accountId ?: 0L
        val accountName = if (accId > 0) {
            accountDao.getAccountById(accId)?.name ?: "کارت بانکی"
        } else {
            "بیلانس کل"
        }

        // Revert old transaction effect on previous account
        if (oldTxn.affectsBalance && oldTxn.accountId > 0) {
            accountDao.updateBalance(oldTxn.accountId, oldTxn.amount)
        }

        // Apply new transaction effect on new account
        if (accId > 0) {
            accountDao.updateBalance(accId, -newTotal)
        }

        val updatedTxn = oldTxn.copy(
            title = "خرید: ${list.title}",
            amount = newTotal,
            category = list.category,
            accountId = accId,
            note = cleanTransactionNote(oldTxn.note),
            calculationExpression = "${list.currencySymbol} $newTotal",
            currencyCode = list.currencyCode,
            currencySymbol = list.currencySymbol,
            timestamp = list.purchaseDate
        )
        transactionDao.updateTransaction(updatedTxn)
    }

    suspend fun deleteShoppingList(listId: Long, deleteLinkedExpense: Boolean = false): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            database.withTransaction {
                val list = shoppingListDao.getShoppingListById(listId)
                    ?: error("لیست یافت نشد")

                if (deleteLinkedExpense && list.linkedTransactionId != null) {
                    val txn = transactionDao.getTransactionById(list.linkedTransactionId)
                    if (txn != null) {
                        deleteTransaction(txn)
                    }
                }

                shoppingListItemDao.deleteItemsByListId(listId)
                shoppingListDao.deleteShoppingListById(listId)
            }
        }
    }

    suspend fun toggleShoppingListItemPurchased(itemId: Long, isPurchased: Boolean) = withContext(Dispatchers.IO) {
        shoppingListItemDao.updatePurchasedStatus(itemId, isPurchased)
    }

    suspend fun findShoppingListForTransaction(transactionId: Long): ShoppingListWithItems? = withContext(Dispatchers.IO) {
        val list = shoppingListDao.getShoppingListByLinkedTransactionId(transactionId) ?: return@withContext null
        val items = shoppingListItemDao.getItemsForListSync(list.id)
        ShoppingListWithItems(list, items)
    }

    suspend fun getShoppingListWithItemsById(listId: Long): ShoppingListWithItems? = withContext(Dispatchers.IO) {
        val list = shoppingListDao.getShoppingListById(listId) ?: return@withContext null
        val items = shoppingListItemDao.getItemsForListSync(listId)
        ShoppingListWithItems(list, items)
    }
}


