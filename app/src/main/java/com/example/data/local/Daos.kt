package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR recipientName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    fun getTotalAmountByType(type: TransactionType): Flow<Double?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE relatedTransactionId = :relatedId")
    suspend fun getTransactionsByRelatedId(relatedId: Long): List<TransactionEntity>

    @Query("UPDATE transactions SET relatedTransactionId = :relatedId WHERE id = :id")
    suspend fun updateRelatedTransactionId(id: Long, relatedId: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE recipientId = :recipientId")
    suspend fun countTransactionsByRecipientId(recipientId: Long): Int

    @Query("SELECT * FROM transactions WHERE recipientId = :recipientId ORDER BY timestamp DESC")
    fun getTransactionsByRecipientId(recipientId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE recipientId = :recipientId ORDER BY timestamp DESC")
    suspend fun getTransactionsListByRecipientId(recipientId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE originalTransactionId = :originalId")
    suspend fun getTransactionsByOriginalId(originalId: Long): List<TransactionEntity>

    @Query("UPDATE transactions SET settledAmount = :settledAmount, isSettled = :isSettled WHERE id = :id")
    suspend fun updateSettledAmount(id: Long, settledAmount: Double, isSettled: Boolean)

    @Query("UPDATE transactions SET recipientName = :newName WHERE recipientId = :recipientId")
    suspend fun updateRecipientNameByRecipientId(recipientId: Long, newName: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countTransactionsByCategoryId(categoryId: Long): Int

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    fun getTransactionsByCategoryId(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY timestamp DESC")
    suspend fun getTransactionsListByCategoryId(categoryId: Long): List<TransactionEntity>

    @Query("UPDATE transactions SET category = :newName WHERE categoryId = :categoryId")
    suspend fun updateCategoryNameByCategoryId(categoryId: Long, newName: String)

    @Query("SELECT COUNT(*) FROM transactions WHERE category = :category")
    suspend fun countTransactionsByCategory(category: String): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE currencyCode = :code OR ledgerCurrencyCode = :code")
    suspend fun countTransactionsByCurrency(code: String): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE currencyId = :id OR ledgerCurrencyId = :id OR (currencyId = 0 AND (UPPER(currencyCode) = UPPER(:code) OR UPPER(ledgerCurrencyCode) = UPPER(:code)))")
    suspend fun countTransactionsByCurrencyIdOrCode(id: Long, code: String): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countTransactionsByAccount(accountId: Long): Int

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN (type = 'INCOME' OR kind = 'GOAL_WITHDRAW' OR (type = 'TRANSFER' AND (category = 'برداشت از هدف' OR title LIKE 'انتقال از هدف%'))) THEN amount
                WHEN (type = 'EXPENSE' OR kind = 'GOAL_DEPOSIT' OR (type = 'TRANSFER' AND (category = 'واریز به هدف' OR title LIKE 'انتقال به هدف%'))) THEN -amount
                ELSE 0.0
            END
        ), 0.0)
        FROM transactions
        WHERE UPPER(currencyCode) = UPPER(:currencyCode)
          AND accountId = 0
          AND affectsBalance = 1
          AND kind != 'PERSON_TRANSFER'
          AND kind != 'PERSON_CURRENCY_EXCHANGE'
    """)
    suspend fun getCashBalanceForCurrency(currencyCode: String): Double

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN (type = 'INCOME' OR kind = 'GOAL_WITHDRAW' OR (type = 'TRANSFER' AND (category = 'برداشت از هدف' OR title LIKE 'انتقال از هدف%'))) THEN amount
                WHEN (type = 'EXPENSE' OR kind = 'GOAL_DEPOSIT' OR (type = 'TRANSFER' AND (category = 'واریز به هدف' OR title LIKE 'انتقال به هدف%'))) THEN -amount
                ELSE 0.0
            END
        ), 0.0)
        FROM transactions
        WHERE ((:currencyId > 0 AND currencyId = :currencyId) OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:currencyCode)))
          AND accountId = 0
          AND affectsBalance = 1
          AND kind != 'PERSON_TRANSFER'
          AND kind != 'PERSON_CURRENCY_EXCHANGE'
    """)
    suspend fun getCashBalanceForCurrencyIdOrCode(currencyId: Long, currencyCode: String): Double

    @Query("""
        UPDATE transactions
        SET currencyCode = :newCode, currencySymbol = :newSymbol, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateTransactionsCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Query("""
        UPDATE transactions
        SET ledgerCurrencyCode = :newCode, ledgerCurrencySymbol = :newSymbol, ledgerCurrencyId = :currencyId
        WHERE ledgerCurrencyId = :currencyId OR (ledgerCurrencyId IS NULL AND UPPER(ledgerCurrencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateTransactionsLedgerCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Query("""
        UPDATE transactions
        SET sourceCurrencyCode = :newCode, sourceCurrencySymbol = :newSymbol, sourceCurrencyId = :currencyId
        WHERE sourceCurrencyId = :currencyId OR (sourceCurrencyId IS NULL AND UPPER(sourceCurrencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateTransactionsSourceCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Query("""
        UPDATE transactions
        SET targetCurrencyCode = :newCode, targetCurrencySymbol = :newSymbol, targetCurrencyId = :currencyId
        WHERE targetCurrencyId = :currencyId OR (targetCurrencyId IS NULL AND UPPER(targetCurrencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateTransactionsTargetCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Query("""
        UPDATE transactions
        SET historicalBaseCurrencyCode = :newCode
        WHERE UPPER(historicalBaseCurrencyCode) = UPPER(:oldCode)
    """)
    suspend fun updateHistoricalBaseCurrencyCode(oldCode: String, newCode: String)

    @Query("""
        SELECT COALESCE(SUM(
            CASE 
                WHEN type = 'INCOME' THEN amount
                WHEN type = 'EXPENSE' THEN -amount
                ELSE 0.0
            END
        ), 0.0)
        FROM transactions
        WHERE accountId = :accountId AND affectsBalance = 1
    """)
    suspend fun calculateAccountNetBalance(accountId: Long): Double

    @Query("SELECT EXISTS(SELECT 1 FROM transactions LIMIT 1)")
    suspend fun hasAnyTransactions(): Boolean

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsList(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>)
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, id ASC")
    fun getAllAccounts(): Flow<List<AccountCardEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountCardEntity): Long

    @Update
    suspend fun updateAccount(account: AccountCardEntity)

    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Double)

    @Query("UPDATE accounts SET balance = ROUND(balance + :delta, 4) WHERE id = :id AND (:delta >= 0 OR balance + :delta >= -0.0001)")
    suspend fun updateBalanceAtomic(id: Long, delta: Double): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE currencyCode = :code")
    suspend fun countAccountsByCurrency(code: String): Int

    @Query("SELECT COUNT(*) FROM accounts WHERE currencyId = :id OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:code))")
    suspend fun countAccountsByCurrencyIdOrCode(id: Long, code: String): Int

    @Query("""
        UPDATE accounts
        SET currencyCode = :newCode, currencySymbol = :newSymbol, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateAccountsCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Delete
    suspend fun deleteAccount(account: AccountCardEntity)

    @Query("SELECT * FROM accounts ORDER BY id ASC")
    suspend fun getAllAccountsList(): List<AccountCardEntity>

    @Query("DELETE FROM accounts")
    suspend fun deleteAllAccounts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAccounts(accounts: List<AccountCardEntity>)
}

@Dao
interface RecipientDao {
    @Query("SELECT * FROM recipients ORDER BY isFavorite DESC, transactionCount DESC, name ASC")
    fun getAllRecipients(): Flow<List<RecipientEntity>>

    @Query("SELECT * FROM recipients WHERE id = :id LIMIT 1")
    suspend fun getRecipientById(id: Long): RecipientEntity?

    @Query("SELECT * FROM recipients WHERE name = :name")
    suspend fun getRecipientsByName(name: String): List<RecipientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipient(recipient: RecipientEntity): Long

    @Update
    suspend fun updateRecipient(recipient: RecipientEntity)

    @Query("UPDATE recipients SET isAmountProtected = :isProtected WHERE id = :id")
    suspend fun updateAmountProtection(id: Long, isProtected: Boolean)

    @Query("UPDATE recipients SET transactionCount = transactionCount + 1 WHERE id = :id")
    suspend fun incrementTransactionCountById(id: Long)

    @Query("UPDATE recipients SET transactionCount = CASE WHEN transactionCount > 0 THEN transactionCount - 1 ELSE 0 END WHERE id = :id")
    suspend fun decrementTransactionCountById(id: Long)

    @Delete
    suspend fun deleteRecipient(recipient: RecipientEntity)

    @Query("SELECT * FROM recipients ORDER BY id ASC")
    suspend fun getAllRecipientsList(): List<RecipientEntity>

    @Query("DELETE FROM recipients")
    suspend fun deleteAllRecipients()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecipients(recipients: List<RecipientEntity>)
}

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies ORDER BY isBaseCurrency DESC, id ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE isActive = 1 ORDER BY isBaseCurrency DESC, id ASC")
    fun getActiveCurrencies(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    suspend fun getCurrencyByCode(code: String): CurrencyEntity?

    @Query("SELECT * FROM currencies WHERE id = :id LIMIT 1")
    suspend fun getCurrencyById(id: Long): CurrencyEntity?

    @Query("SELECT COUNT(*) FROM currencies WHERE UPPER(code) = UPPER(:code) AND id != :excludeId")
    suspend fun countOtherCurrenciesWithCode(code: String, excludeId: Long): Int

    @Query("SELECT * FROM currencies WHERE UPPER(code) = UPPER(:code)")
    suspend fun getCurrenciesByCode(code: String): List<CurrencyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencyEntity): Long

    @Update
    suspend fun updateCurrency(currency: CurrencyEntity)

    @Update
    suspend fun updateAllCurrencies(currencies: List<CurrencyEntity>)

    @Delete
    suspend fun deleteCurrency(currency: CurrencyEntity)

    @Query("SELECT * FROM currencies ORDER BY id ASC")
    suspend fun getAllCurrenciesList(): List<CurrencyEntity>

    @Query("DELETE FROM currencies")
    suspend fun deleteAllCurrencies()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCurrencies(currencies: List<CurrencyEntity>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY isDefault DESC, id ASC")
    fun getActiveCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type AND isActive = 1 ORDER BY isDefault DESC, id ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE id = :id")
    suspend fun countCategoryById(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllCategoriesList(): List<CategoryEntity>

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY id ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getBudgetByCategoryId(categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetByCategory(category: String): BudgetEntity?

    @Query("UPDATE budgets SET category = :newName WHERE categoryId = :categoryId")
    suspend fun updateCategoryNameByCategoryId(categoryId: Long, newName: String)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun deleteBudgetByCategoryId(categoryId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets ORDER BY id ASC")
    suspend fun getAllBudgetsList(): List<BudgetEntity>

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("""
        UPDATE budgets
        SET currencyCode = :newCode, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateBudgetsCurrencyReference(currencyId: Long, oldCode: String, newCode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBudgets(budgets: List<BudgetEntity>)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM financial_goals ORDER BY status ASC, id DESC")
    fun getAllGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE status = 'ACTIVE' ORDER BY id DESC")
    fun getActiveGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE status = 'COMPLETED' ORDER BY completedDate DESC, id DESC")
    fun getCompletedGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE status = 'ARCHIVED' ORDER BY id DESC")
    fun getArchivedGoals(): Flow<List<FinancialGoalEntity>>

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): FinancialGoalEntity?

    @Query("SELECT * FROM financial_goals WHERE id = :id")
    fun getGoalByIdFlow(id: Long): Flow<FinancialGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: FinancialGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoalEntity)

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("UPDATE financial_goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateGoalAmount(id: Long, amount: Double)

    @Query("UPDATE financial_goals SET status = :status, completedDate = :completedDate WHERE id = :id")
    suspend fun updateGoalStatus(id: Long, status: GoalStatus, completedDate: Long?)

    @Query("SELECT * FROM financial_goals ORDER BY id ASC")
    suspend fun getAllGoalsList(): List<FinancialGoalEntity>

    @Query("DELETE FROM financial_goals")
    suspend fun deleteAllGoals()

    @Query("""
        UPDATE financial_goals
        SET currencyCode = :newCode, currencySymbol = :newSymbol, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateGoalsCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<FinancialGoalEntity>)
}

@Dao
interface GoalTransactionDao {
    @Query("SELECT * FROM goal_transactions WHERE id = :id")
    suspend fun getGoalTransactionById(id: Long): GoalTransactionEntity?

    @Query("SELECT * FROM goal_transactions WHERE linkedTransactionId = :txnId LIMIT 1")
    suspend fun getGoalTransactionByLinkedTxnId(txnId: Long): GoalTransactionEntity?

    @Query("SELECT * FROM goal_transactions WHERE goalId = :goalId ORDER BY timestamp DESC, id DESC")
    fun getTransactionsForGoal(goalId: Long): Flow<List<GoalTransactionEntity>>

    @Query("SELECT * FROM goal_transactions ORDER BY timestamp DESC, id DESC")
    fun getAllGoalTransactions(): Flow<List<GoalTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalTransaction(txn: GoalTransactionEntity): Long

    @Update
    suspend fun updateGoalTransaction(txn: GoalTransactionEntity)

    @Delete
    suspend fun deleteGoalTransaction(txn: GoalTransactionEntity)

    @Query("DELETE FROM goal_transactions WHERE goalId = :goalId")
    suspend fun deleteTransactionsByGoalId(goalId: Long)

    @Query("SELECT * FROM goal_transactions ORDER BY id ASC")
    suspend fun getAllGoalTransactionsList(): List<GoalTransactionEntity>

    @Query("DELETE FROM goal_transactions")
    suspend fun deleteAllGoalTransactions()

    @Query("""
        UPDATE goal_transactions
        SET currencyCode = :newCode, currencySymbol = :newSymbol, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateGoalTransactionsCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoalTransactions(txns: List<GoalTransactionEntity>)
}

@Dao
interface QuickActionDao {
    @Query("SELECT * FROM quick_actions ORDER BY displayOrder ASC, id ASC")
    fun getAllQuickActions(): Flow<List<QuickActionEntity>>

    @Query("SELECT * FROM quick_actions WHERE showOnHome = 1 ORDER BY displayOrder ASC, id ASC")
    fun getHomeQuickActions(): Flow<List<QuickActionEntity>>

    @Query("SELECT * FROM quick_actions WHERE id = :id")
    suspend fun getQuickActionById(id: Long): QuickActionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickAction(quickAction: QuickActionEntity): Long

    @Update
    suspend fun updateQuickAction(quickAction: QuickActionEntity)

    @Delete
    suspend fun deleteQuickAction(quickAction: QuickActionEntity)

    @Query("DELETE FROM quick_actions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE quick_actions SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("UPDATE quick_actions SET displayOrder = :order WHERE id = :id")
    suspend fun updateOrder(id: Long, order: Int)

    @Query("UPDATE quick_actions SET categoryName = :newName WHERE categoryId = :categoryId")
    suspend fun updateCategoryNameByCategoryId(categoryId: Long, newName: String)

    @Query("UPDATE quick_actions SET recipientName = :newName WHERE recipientId = :recipientId")
    suspend fun updateRecipientNameByRecipientId(recipientId: Long, newName: String)

    @Query("SELECT COUNT(*) FROM quick_actions")
    suspend fun getCount(): Int

    @Query("SELECT * FROM quick_actions ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllQuickActionsList(): List<QuickActionEntity>

    @Query("DELETE FROM quick_actions")
    suspend fun deleteAllQuickActions()

    @Query("""
        UPDATE quick_actions
        SET currencyCode = :newCode, currencySymbol = :newSymbol, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateQuickActionsCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quickActions: List<QuickActionEntity>)
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun getShoppingListById(id: Long): ShoppingListEntity?

    @Query("SELECT * FROM shopping_lists WHERE linkedTransactionId = :transactionId LIMIT 1")
    suspend fun getShoppingListByLinkedTransactionId(transactionId: Long): ShoppingListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingList(list: ShoppingListEntity): Long

    @Update
    suspend fun updateShoppingList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteShoppingList(list: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun deleteShoppingListById(id: Long)

    @Query("UPDATE shopping_lists SET isLoggedAsExpense = :isLogged, linkedTransactionId = :transactionId, totalAmount = :totalAmount WHERE id = :id")
    suspend fun updateExpenseStatus(id: Long, isLogged: Boolean, transactionId: Long?, totalAmount: Double)

    @Query("UPDATE shopping_lists SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("UPDATE shopping_lists SET category = :newName WHERE categoryId = :categoryId")
    suspend fun updateCategoryNameByCategoryId(categoryId: Long, newName: String)

    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    suspend fun getAllShoppingListsList(): List<ShoppingListEntity>

    @Query("DELETE FROM shopping_lists")
    suspend fun deleteAllShoppingLists()

    @Query("""
        UPDATE shopping_lists
        SET currencyCode = :newCode, currencySymbol = :newSymbol, currencyId = :currencyId
        WHERE currencyId = :currencyId OR (currencyId = 0 AND UPPER(currencyCode) = UPPER(:oldCode))
    """)
    suspend fun updateShoppingListsCurrencyReference(currencyId: Long, oldCode: String, newCode: String, newSymbol: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllShoppingLists(lists: List<ShoppingListEntity>)
}

@Dao
interface ShoppingListItemDao {
    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY displayOrder ASC, id ASC")
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_list_items WHERE listId = :listId ORDER BY displayOrder ASC, id ASC")
    suspend fun getItemsForListSync(listId: Long): List<ShoppingListItemEntity>

    @Query("SELECT * FROM shopping_list_items ORDER BY id ASC")
    fun getAllItems(): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Delete
    suspend fun deleteItem(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM shopping_list_items WHERE listId = :listId")
    suspend fun deleteItemsByListId(listId: Long)

    @Query("UPDATE shopping_list_items SET isPurchased = :isPurchased WHERE id = :id")
    suspend fun updatePurchasedStatus(id: Long, isPurchased: Boolean)

    @Query("SELECT * FROM shopping_list_items ORDER BY id ASC")
    suspend fun getAllItemsList(): List<ShoppingListItemEntity>

    @Query("DELETE FROM shopping_list_items")
    suspend fun deleteAllItems()
}





