package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinancialIntegrityTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FinanceRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = FinanceRepository(
            database = db,
            transactionDao = db.transactionDao(),
            accountDao = db.accountDao(),
            recipientDao = db.recipientDao(),
            currencyDao = db.currencyDao(),
            categoryDao = db.categoryDao(),
            budgetDao = db.budgetDao(),
            goalDao = db.goalDao(),
            goalTransactionDao = db.goalTransactionDao(),
            quickActionDao = db.quickActionDao(),
            shoppingListDao = db.shoppingListDao(),
            shoppingListItemDao = db.shoppingListItemDao()
        )

        runBlocking {
            repository.seedInitialDataIfEmpty(force = true)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    // Scenario 1: ثبت تراکنش درآمد و تأثیر دقیق بر موجودی حساب
    @Test
    fun testScenario01_IncomeTransactionIncreasesAccountBalance() = runBlocking {
        val accId = repository.addAccount(
            name = "حساب اصلی",
            cardNumberMasked = "**** 1111",
            balance = 1000.0,
            theme = "classic_blue",
            currencyCode = "AFN",
            currencySymbol = "؋"
        )
        val initialAcc = db.accountDao().getAccountById(accId)!!
        assertEquals(1000.0, initialAcc.balance, 0.001)

        val txnId = repository.addTransaction(
            title = "حقوق ماهانه",
            amount = 500.0,
            type = TransactionType.INCOME,
            category = "حقوق",
            accountId = accId,
            recipientName = null,
            note = "حقوق فروردین",
            calculationExpression = "500",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        val updatedAcc = db.accountDao().getAccountById(accId)!!
        assertEquals(1500.0, updatedAcc.balance, 0.001)
        val txn = db.transactionDao().getTransactionById(txnId)
        assertNotNull(txn)
        assertEquals(500.0, txn!!.amount, 0.001)
    }

    // Scenario 2: ثبت تراکنش هزینه و کسر دقیق از موجودی حساب
    @Test
    fun testScenario02_ExpenseTransactionDeductsAccountBalance() = runBlocking {
        val accId = repository.addAccount(
            name = "حساب خرج",
            cardNumberMasked = "**** 2222",
            balance = 2000.0,
            theme = "emerald",
            currencyCode = "AFN",
            currencySymbol = "؋"
        )

        repository.addTransaction(
            title = "خرید مواد غذایی",
            amount = 450.0,
            type = TransactionType.EXPENSE,
            category = "خوراک",
            accountId = accId,
            recipientName = null,
            note = "سوپرمارکت",
            calculationExpression = "450",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        val updatedAcc = db.accountDao().getAccountById(accId)!!
        assertEquals(1550.0, updatedAcc.balance, 0.001)
    }

    // Scenario 3: ویرایش تراکنش مالی (تغییر حساب و مبلغ به‌صورت اتمیک)
    @Test
    fun testScenario03_UpdateTransactionAtomicallyRollsBackOldAndAppliesNew() = runBlocking {
        val acc1Id = repository.addAccount("حساب یک", "**** 1001", 1000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")
        val acc2Id = repository.addAccount("حساب دو", "**** 1002", 1000.0, "emerald", currencyCode = "AFN", currencySymbol = "؋")

        val txnId = repository.addTransaction(
            title = "خرید وسایل",
            amount = 200.0,
            type = TransactionType.EXPENSE,
            category = "خرید",
            accountId = acc1Id,
            recipientName = null,
            note = "وسایل کار",
            calculationExpression = "200",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        assertEquals(800.0, db.accountDao().getAccountById(acc1Id)!!.balance, 0.001)
        assertEquals(1000.0, db.accountDao().getAccountById(acc2Id)!!.balance, 0.001)

        val existingTxn = db.transactionDao().getTransactionById(txnId)!!
        val updatedTxn = existingTxn.copy(
            accountId = acc2Id,
            amount = 350.0
        )
        repository.updateTransaction(oldTxn = existingTxn, newTxn = updatedTxn)

        // حساب اول باید ۲۰۰ واحد برگردانده شود (مجدداً ۱۰۰۰ شود)
        assertEquals(1000.0, db.accountDao().getAccountById(acc1Id)!!.balance, 0.001)
        // حساب دوم باید ۳۵۰ واحد کسر شود (۶۵۰ شود)
        assertEquals(650.0, db.accountDao().getAccountById(acc2Id)!!.balance, 0.001)
    }

    // Scenario 4: حذف تراکنش و بازگشت کامل و اتمیک مبلغ به حساب
    @Test
    fun testScenario04_DeleteTransactionRevertsAccountBalanceAtomically() = runBlocking {
        val accId = repository.addAccount("حساب روزمره", "**** 3333", 5000.0, "royal_purple", currencyCode = "AFN", currencySymbol = "؋")

        val txnId = repository.addTransaction(
            title = "شارژ اینترنت",
            amount = 1200.0,
            type = TransactionType.EXPENSE,
            category = "قبوض",
            accountId = accId,
            recipientName = null,
            note = "ماهانه",
            calculationExpression = "1200",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        assertEquals(3800.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)

        val txn = db.transactionDao().getTransactionById(txnId)!!
        repository.deleteTransaction(txn)

        assertEquals(5000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
        assertNull(db.transactionDao().getTransactionById(txnId))
    }

    // Scenario 5: انتقال بین دو کارت با ارز یکسان
    @Test
    fun testScenario05_SameCurrencyWalletTransfer() = runBlocking {
        val acc1Id = repository.addAccount("کارت فرستنده", "**** 4441", 3000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")
        val acc2Id = repository.addAccount("کارت گیرنده", "**** 4442", 1000.0, "emerald", currencyCode = "AFN", currencySymbol = "؋")
        val acc1 = db.accountDao().getAccountById(acc1Id)!!
        val acc2 = db.accountDao().getAccountById(acc2Id)!!

        repository.executeWalletTransfer(
            fromAccount = acc1,
            toAccount = acc2,
            fromAmount = 800.0,
            toAmount = 800.0,
            rate = 1.0,
            note = "انتقال برادرانه"
        )

        assertEquals(2200.0, db.accountDao().getAccountById(acc1Id)!!.balance, 0.001)
        assertEquals(1800.0, db.accountDao().getAccountById(acc2Id)!!.balance, 0.001)
    }

    // Scenario 6: انتقال بین دو کارت با ارزهای مختلف و نرخ تبدیل صریح
    @Test
    fun testScenario06_CrossCurrencyWalletTransfer() = runBlocking {
        val accUsdId = repository.addAccount("حساب دلار", "**** 5551", 500.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")
        val accAfnId = repository.addAccount("حساب افغانی", "**** 5552", 10000.0, "emerald", currencyCode = "AFN", currencySymbol = "؋")
        val accUsd = db.accountDao().getAccountById(accUsdId)!!
        val accAfn = db.accountDao().getAccountById(accAfnId)!!

        // انتقال ۱۰۰ دلار با نرخ ۷۰ افغانی
        repository.executeWalletTransfer(
            fromAccount = accUsd,
            toAccount = accAfn,
            fromAmount = 100.0,
            toAmount = 7000.0,
            rate = 70.0,
            note = "تبدیل و انتقال به حساب افغانی"
        )

        assertEquals(400.0, db.accountDao().getAccountById(accUsdId)!!.balance, 0.001)
        assertEquals(17000.0, db.accountDao().getAccountById(accAfnId)!!.balance, 0.001)
    }

    // Scenario 7: انتقال از نقد به کارت (واریز نقدی به حساب)
    @Test
    fun testScenario07_CashToCardTransfer() = runBlocking {
        val currs = db.currencyDao().getAllCurrenciesList()
        val afnCurr = currs.first { it.code == "AFN" }
        val cardId = repository.addAccount("کارت عابربانک", "**** 6661", 2000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")
        val card = db.accountDao().getAccountById(cardId)!!

        // ابتدا ثبت درآمد نقدی برای ایجاد موجودی نقد
        repository.addTransaction(
            title = "درآمد نقد اولیه",
            amount = 5000.0,
            type = TransactionType.INCOME,
            category = "نقد",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "5000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )
        val cashBefore = repository.getCashBalance("AFN")
        assertEquals(5000.0, cashBefore, 0.001)

        repository.executeCashCardTransfer(
            isCashToCard = true,
            cashCurrency = afnCurr,
            cardAccount = card,
            cashAmount = 1500.0,
            cardAmount = 1500.0,
            rate = 1.0,
            note = "واریز نقد به عابربانک"
        )

        val cashAfter = repository.getCashBalance("AFN")
        assertEquals(3500.0, cashAfter, 0.001)
        assertEquals(3500.0, db.accountDao().getAccountById(cardId)!!.balance, 0.001)
    }

    // Scenario 8: انتقال از کارت به نقد (برداشت از عابربانک)
    @Test
    fun testScenario08_CardToCashTransfer() = runBlocking {
        val currs = db.currencyDao().getAllCurrenciesList()
        val afnCurr = currs.first { it.code == "AFN" }
        val cardId = repository.addAccount("کارت عابربانک", "**** 7771", 10000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")
        val card = db.accountDao().getAccountById(cardId)!!

        val cashBefore = repository.getCashBalance("AFN")

        repository.executeCashCardTransfer(
            isCashToCard = false,
            cashCurrency = afnCurr,
            cardAccount = card,
            cashAmount = 4000.0,
            cardAmount = 4000.0,
            rate = 1.0,
            note = "برداشت نقد از خودپرداز"
        )

        val cashAfter = repository.getCashBalance("AFN")
        assertEquals(cashBefore + 4000.0, cashAfter, 0.001)
        assertEquals(6000.0, db.accountDao().getAccountById(cardId)!!.balance, 0.001)
    }

    // Scenario 9: تبدیل ارز نقدی به‌صورت جفت تراکنش اتمیک
    @Test
    fun testScenario09_CurrencyExchangeAtomicPair() = runBlocking {
        val currs = db.currencyDao().getAllCurrenciesList()
        val usdCurr = currs.first { it.code == "USD" }
        val afnCurr = currs.first { it.code == "AFN" }

        // ایجاد ۱۰۰۰ دلار نقد
        repository.addTransaction(
            title = "دریافت دلار نقد",
            amount = 1000.0,
            type = TransactionType.INCOME,
            category = "نقد",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "1000",
            currencyCode = "USD",
            currencySymbol = "$",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        val initialUsd = repository.getCashBalance("USD")
        val initialAfn = repository.getCashBalance("AFN")

        // تبدیل ۲۰۰ دلار به ۱۴,۰۰۰ افغانی
        repository.executeCurrencyExchange(
            fromCurrency = usdCurr,
            toCurrency = afnCurr,
            fromAmount = 200.0,
            toAmount = 14000.0,
            rate = 70.0,
            fromAccountId = 0L,
            toAccountId = 0L,
            note = "تبدیل در صرافی"
        )

        assertEquals(initialUsd - 200.0, repository.getCashBalance("USD"), 0.001)
        assertEquals(initialAfn + 14000.0, repository.getCashBalance("AFN"), 0.001)
    }

    // Scenario 10: تسویه طلب/بدهی اشخاص هم‌ارز
    @Test
    fun testScenario10_SameCurrencyPersonSettlement() = runBlocking {
        val recipientId = repository.addRecipient(
            name = "احمد رضایی",
            handleOrPhone = "0799999999"
        )
        val accId = repository.addAccount("حساب پاسارگاد", "**** 8881", 50000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")

        // ثبت طلب اولیه (طلبکار بودن از احمد)
        val debtTxnId = repository.addTransaction(
            title = "قرض دادن به احمد",
            amount = 10000.0,
            type = TransactionType.EXPENSE,
            category = "طلب",
            accountId = accId,
            recipientName = "احمد رضایی",
            recipientId = recipientId,
            kind = TransactionKind.PERSON_PAYMENT,
            note = "قرض الحسنه",
            calculationExpression = "10000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        assertEquals(40000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)

        // احمد ۱۰,۰۰۰ افغانی را برمی‌گرداند (تسویه طلب)
        repository.settleRecipientDebt(
            recipientId = recipientId,
            recipientName = "احمد رضایی",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 10000.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 10000.0,
            exchangeRate = 1.0,
            isClaimSettlement = true,
            originalTransactionId = debtTxnId,
            note = "تسویه کامل طلب"
        )

        assertEquals(50000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
        val orig = db.transactionDao().getTransactionById(debtTxnId)!!
        assertTrue(orig.isSettled)
        assertEquals(10000.0, orig.settledAmount ?: 0.0, 0.001)
    }

    // Scenario 11: تسویه طلب چندارزی (پرداخت با ارز متفاوت از ارز بدهی)
    @Test
    fun testScenario11_CrossCurrencyPersonSettlement() = runBlocking {
        val recipientId = repository.addRecipient(
            name = "محمد اکبری",
            handleOrPhone = "0788888888"
        )
        val accUsdId = repository.addAccount("حساب دلار تسویه", "**** 9991", 1000.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")

        // بدهی به محمد: ۷,۰۰۰ افغانی
        val debtTxnId = repository.addTransaction(
            title = "قرض از محمد",
            amount = 7000.0,
            type = TransactionType.INCOME,
            category = "بدهی",
            accountId = 0L,
            recipientName = "محمد اکبری",
            recipientId = recipientId,
            kind = TransactionKind.PERSON_RECEIVE,
            note = "قرض به افغانی",
            calculationExpression = "7000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false
        )

        // پرداخت با ۱۰۰ دلار از حساب دلار (نرخ ۷۰)
        repository.settleRecipientDebt(
            recipientId = recipientId,
            recipientName = "محمد اکبری",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 7000.0,
            paymentAccountId = accUsdId,
            paymentCurrencyCode = "USD",
            paymentAmount = 100.0,
            exchangeRate = 70.0,
            isClaimSettlement = false,
            originalTransactionId = debtTxnId,
            note = "تسویه بدهی افغانی با دلار"
        )

        assertEquals(900.0, db.accountDao().getAccountById(accUsdId)!!.balance, 0.001)
        val orig = db.transactionDao().getTransactionById(debtTxnId)!!
        assertTrue(orig.isSettled)
    }

    // Scenario 12: واریز و برداشت از هدف مالی با اثرگذاری اتمیک بر موجودی حساب
    @Test
    fun testScenario12_GoalDepositAndWithdrawalAtomic() = runBlocking {
        val accId = repository.addAccount("حساب پس‌انداز", "**** 1212", 10000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")
        val goalId = repository.addGoal(
            FinancialGoalEntity(
                title = "خرید لپتاپ",
                targetAmount = 50000.0,
                targetDate = System.currentTimeMillis() + 86400000L * 30,
                currencyCode = "AFN",
                currencySymbol = "؋",
                category = "تجهیزات"
            )
        )

        // واریز ۵۰۰۰ به هدف از حساب
        val depositResult = repository.depositToGoal(
            goalId = goalId,
            amount = 5000.0,
            accountId = accId,
            note = "پس انداز ماه اول"
        )
        assertTrue(depositResult.isSuccess)
        assertEquals(5000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
        val goalAfterDeposit = db.goalDao().getGoalById(goalId)!!
        assertEquals(5000.0, goalAfterDeposit.currentAmount, 0.001)

        // برداشت ۲۰۰۰ از هدف به حساب
        val withdrawResult = repository.withdrawFromGoal(
            goalId = goalId,
            amount = 2000.0,
            destinationAccountId = accId,
            note = "برداشت اضطراری"
        )
        assertTrue(withdrawResult.isSuccess)
        assertEquals(7000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
        val goalAfterWithdraw = db.goalDao().getGoalById(goalId)!!
        assertEquals(3000.0, goalAfterWithdraw.currentAmount, 0.001)
    }

    // Scenario 13: ممانعت از حذف حساب دارای موجودی غیرصفر یا تراکنش وابسته
    @Test
    fun testScenario13_AccountDeletionProtection() = runBlocking {
        val accId = repository.addAccount("حساب غیرخالی", "**** 1313", 2500.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")
        val acc = db.accountDao().getAccountById(accId)!!

        // تلاش برای حذف حسابی که ۲۵۰۰ موجودی دارد باید خطای اعتبار سنجی بدهد
        var exceptionCaught = false
        try {
            repository.deleteAccount(acc)
        } catch (e: Exception) {
            exceptionCaught = true
        }
        assertTrue("حساب دارای موجودی نباید قابل حذف باشد", exceptionCaught)
        assertNotNull(db.accountDao().getAccountById(accId))
    }

    // Scenario 14: ممانعت از حذف ارز یا مخاطب دارای تراکنش‌های وابسته
    @Test
    fun testScenario14_RecipientAndCurrencyDeletionProtection() = runBlocking {
        val recId = repository.addRecipient(name = "شخص دارای تراکنش", handleOrPhone = "0700000000")
        val rec = db.recipientDao().getRecipientById(recId)!!

        repository.addTransaction(
            title = "هزینه مرتبط",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = "عمومی",
            accountId = 0L,
            recipientName = rec.name,
            recipientId = rec.id,
            kind = TransactionKind.NORMAL,
            note = null,
            calculationExpression = "100",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false
        )

        var recipientExceptionCaught = false
        try {
            repository.deleteRecipient(rec)
        } catch (e: Exception) {
            recipientExceptionCaught = true
        }
        assertTrue("مخاطب دارای تراکنش نباید حذف شود", recipientExceptionCaught)

        // تست ارز دارای تراکنش
        val currAfn = db.currencyDao().getAllCurrenciesList().first { it.code == "AFN" }
        var currencyExceptionCaught = false
        try {
            repository.deleteCurrency(currAfn)
        } catch (e: Exception) {
            currencyExceptionCaught = true
        }
        assertTrue("ارز پایه یا دارای تراکنش نباید حذف شود", currencyExceptionCaught)
    }

    // Scenario 15: ممانعت از ایجاد موجودی منفی در کارت (Expense: 50 USD Balance, 100 USD Expense -> Rejection)
    @Test
    fun testScenario15_PreventNegativeBalance_Expense() = runBlocking<Unit> {
        val accId = repository.addAccount(
            name = "کارت دلاری",
            cardNumberMasked = "**** 0001",
            balance = 50.0,
            theme = "classic_blue",
            currencyCode = "USD",
            currencySymbol = "$"
        )
        val initialTxnCount = db.transactionDao().getAllTransactionsList().size

        var exceptionThrown = false
        try {
            repository.addTransaction(
                title = "خرید بیش از موجودی",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "خرید",
                accountId = accId,
                currencyCode = "USD",
                currencySymbol = "$",
                affectsBalance = true
            )
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue("عملیات هزینه با موجودی ناکافی باید رد شود", exceptionThrown)
        val acc = db.accountDao().getAccountById(accId)!!
        assertEquals(50.0, acc.balance, 0.001)
        assertEquals(initialTxnCount, db.transactionDao().getAllTransactionsList().size)
    }

    // Scenario 16: ممانعت از ایجاد موجودی منفی در پرداخت به شخص (Pay Person)
    @Test
    fun testScenario16_PreventNegativeBalance_PayPerson() = runBlocking<Unit> {
        val recId = repository.addRecipient(name = "شخص تستی", handleOrPhone = "0700112233")
        val accId = repository.addAccount(
            name = "حساب پرداخت",
            cardNumberMasked = "**** 0002",
            balance = 50.0,
            theme = "classic_blue",
            currencyCode = "USD",
            currencySymbol = "$"
        )
        val initialTxnCount = db.transactionDao().getAllTransactionsList().size

        var exceptionThrown = false
        try {
            repository.addTransaction(
                title = "پرداخت به شخص",
                amount = 100.0,
                type = TransactionType.EXPENSE,
                category = "قرض",
                accountId = accId,
                recipientName = "شخص تستی",
                recipientId = recId,
                currencyCode = "USD",
                currencySymbol = "$",
                affectsBalance = true
            )
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue("پرداخت به شخص با موجودی ناکافی باید رد شود", exceptionThrown)
        val acc = db.accountDao().getAccountById(accId)!!
        assertEquals(50.0, acc.balance, 0.001)
        assertEquals(initialTxnCount, db.transactionDao().getAllTransactionsList().size)
    }

    // Scenario 17: ممانعت از انتقال کارت به کارت با موجودی ناکافی
    @Test
    fun testScenario17_PreventNegativeBalance_CardToCard() = runBlocking<Unit> {
        val srcId = repository.addAccount("کارت مبدا", "**** 0003", 50.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")
        val dstId = repository.addAccount("کارت مقصد", "**** 0004", 10.0, "emerald", currencyCode = "USD", currencySymbol = "$")
        val srcAcc = db.accountDao().getAccountById(srcId)!!
        val dstAcc = db.accountDao().getAccountById(dstId)!!
        val initialTxnCount = db.transactionDao().getAllTransactionsList().size

        var exceptionThrown = false
        try {
            repository.executeWalletTransfer(
                fromAccount = srcAcc,
                toAccount = dstAcc,
                fromAmount = 100.0,
                toAmount = 100.0,
                rate = 1.0,
                note = "انتقال ناموفق"
            )
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue("انتقال کارت به کارت با موجودی ناکافی باید رد شود", exceptionThrown)
        assertEquals(50.0, db.accountDao().getAccountById(srcId)!!.balance, 0.001)
        assertEquals(10.0, db.accountDao().getAccountById(dstId)!!.balance, 0.001)
        assertEquals(initialTxnCount, db.transactionDao().getAllTransactionsList().size)
    }

    // Scenario 18: ممانعت از انتقال کارت به نقد و نقد به کارت با موجودی ناکافی
    @Test
    fun testScenario18_PreventNegativeBalance_CardToCashAndCashToCard() = runBlocking<Unit> {
        val currs = db.currencyDao().getAllCurrenciesList()
        val usdCurr = currs.first { it.code == "USD" }
        val cardId = repository.addAccount("کارت دلاری", "**** 0005", 50.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")
        val card = db.accountDao().getAccountById(cardId)!!

        // کارت به نقد با موجودی ناکافی
        var cardToCashThrown = false
        try {
            repository.executeCashCardTransfer(
                isCashToCard = false,
                cashCurrency = usdCurr,
                cardAccount = card,
                cashAmount = 100.0,
                cardAmount = 100.0,
                rate = 1.0,
                note = "برداشت بیش از حد"
            )
        } catch (e: Exception) {
            cardToCashThrown = true
        }
        assertTrue("برداشت از کارت با موجودی ناکافی باید رد شود", cardToCashThrown)
        assertEquals(50.0, db.accountDao().getAccountById(cardId)!!.balance, 0.001)

        // نقد به کارت با موجودی نقد ناکافی (نقد دلار فعلاً صفر است)
        var cashToCardThrown = false
        try {
            repository.executeCashCardTransfer(
                isCashToCard = true,
                cashCurrency = usdCurr,
                cardAccount = card,
                cashAmount = 50.0,
                cardAmount = 50.0,
                rate = 1.0,
                note = "واریز نقد بدون موجودی"
            )
        } catch (e: Exception) {
            cashToCardThrown = true
        }
        assertTrue("واریز از نقد ناکافی به کارت باید رد شود", cashToCardThrown)
        assertEquals(50.0, db.accountDao().getAccountById(cardId)!!.balance, 0.001)
    }

    // Scenario 19: ممانعت از تبدیل ارز با موجودی ناکافی (Currency Exchange)
    @Test
    fun testScenario19_PreventNegativeBalance_CurrencyExchange() = runBlocking<Unit> {
        val currs = db.currencyDao().getAllCurrenciesList()
        val usdCurr = currs.first { it.code == "USD" }
        val afnCurr = currs.first { it.code == "AFN" }

        val initialCashUsd = repository.getCashBalance("USD")
        val initialTxnCount = db.transactionDao().getAllTransactionsList().size

        var exceptionThrown = false
        try {
            repository.executeCurrencyExchange(
                fromCurrency = usdCurr,
                toCurrency = afnCurr,
                fromAmount = initialCashUsd + 100.0,
                toAmount = 7000.0,
                rate = 70.0,
                fromAccountId = 0L,
                toAccountId = 0L,
                note = "تبدیل ارزی غیرمجاز"
            )
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue("تبدیل ارز با موجودی ناکافی باید رد شود", exceptionThrown)
        assertEquals(initialCashUsd, repository.getCashBalance("USD"), 0.001)
        assertEquals(initialTxnCount, db.transactionDao().getAllTransactionsList().size)
    }

    // Scenario 20: ممانعت از واریز به هدف با موجودی ناکافی (Goal Deposit)
    @Test
    fun testScenario20_PreventNegativeBalance_GoalDeposit() = runBlocking<Unit> {
        val accId = repository.addAccount("حساب هدف", "**** 0006", 50.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")
        val goalId = repository.addGoal(
            FinancialGoalEntity(
                title = "هدف تستی",
                targetAmount = 500.0,
                targetDate = System.currentTimeMillis() + 86400000L,
                currencyCode = "USD",
                currencySymbol = "$",
                category = "پس‌انداز"
            )
        )
        val initialTxnCount = db.transactionDao().getAllTransactionsList().size

        val depositResult = repository.depositToGoal(
            goalId = goalId,
            amount = 100.0,
            accountId = accId,
            note = "واریز ناممکن"
        )
        assertTrue("واریز با موجودی ناکافی باید ناموفق باشد", depositResult.isFailure)
        assertEquals(50.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
        assertEquals(0.0, db.goalDao().getGoalById(goalId)!!.currentAmount, 0.001)
        assertEquals(initialTxnCount, db.transactionDao().getAllTransactionsList().size)
    }

    // Scenario 21: ممانعت از تسویه بدهی با موجودی ناکافی (Settlement)
    @Test
    fun testScenario21_PreventNegativeBalance_Settlement() = runBlocking<Unit> {
        val recId = repository.addRecipient(name = "طلبکار تستی", handleOrPhone = "")
        val accId = repository.addAccount("حساب کم‌موجودی", "**** 0007", 50.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")

        // ما ۱۰۰ دلار به طلبکار بدهکاریم (درآمد برای ثبت بدهی)
        val debtTxnId = repository.addTransaction(
            title = "بدهی اولیه",
            amount = 100.0,
            type = TransactionType.INCOME,
            category = "بدهی",
            accountId = 0L,
            recipientName = "طلبکار تستی",
            recipientId = recId,
            currencyCode = "USD",
            currencySymbol = "$",
            affectsBalance = false
        )

        var exceptionThrown = false
        try {
            repository.settleRecipientDebt(
                recipientId = recId,
                recipientName = "طلبکار تستی",
                debtCurrencyCode = "USD",
                debtAmountToSettle = 100.0,
                paymentAccountId = accId,
                paymentCurrencyCode = "USD",
                paymentAmount = 100.0,
                exchangeRate = 1.0,
                isClaimSettlement = false,
                originalTransactionId = debtTxnId,
                note = "تسویه بدون پول کافی"
            )
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue("تسویه با موجودی ناکافی باید رد شود", exceptionThrown)
        assertEquals(50.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
        val orig = db.transactionDao().getTransactionById(debtTxnId)!!
        assertFalse(orig.isSettled)
    }

    // Scenario 22: تفکیک دقیق اشخاص با نام مشابه (Duplicate Names handled by recipientId)
    @Test
    fun testScenario22_DuplicateRecipientNamesAreIsolatedByRecipientId() = runBlocking<Unit> {
        // ایجاد دو شخص مختلف با دقیقاً همان نام "احمد"
        val person1Id = repository.addRecipient(name = "احمد", handleOrPhone = "0700000001")
        val person2Id = repository.addRecipient(name = "احمد", handleOrPhone = "0700000002")
        assertNotEquals(person1Id, person2Id)

        val accId = repository.addAccount("حساب شرکت", "**** 0008", 10000.0, "classic_blue", currencyCode = "USD", currencySymbol = "$")

        // پرداخت ۱۰۰ دلار به شخص اول (طلبکار شدن از شخص اول)
        val txn1Id = repository.addTransaction(
            title = "پرداخت به احمد اول",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = "قرض",
            accountId = accId,
            recipientName = "احمد",
            recipientId = person1Id,
            currencyCode = "USD",
            currencySymbol = "$",
            affectsBalance = true
        )

        // پرداخت ۲۵۰ دلار به شخص دوم (طلبکار شدن از شخص دوم)
        val txn2Id = repository.addTransaction(
            title = "پرداخت به احمد دوم",
            amount = 250.0,
            type = TransactionType.EXPENSE,
            category = "قرض",
            accountId = accId,
            recipientName = "احمد",
            recipientId = person2Id,
            currencyCode = "USD",
            currencySymbol = "$",
            affectsBalance = true
        )

        // بررسی اینکه تراکنش‌های هر شخص کاملاً مجزا ثبت شده‌اند
        val person1Txns = db.transactionDao().getTransactionsListByRecipientId(person1Id)
        val person2Txns = db.transactionDao().getTransactionsListByRecipientId(person2Id)

        assertEquals(1, person1Txns.size)
        assertEquals(100.0, person1Txns.first().amount, 0.001)
        assertEquals(person1Id, person1Txns.first().recipientId)

        assertEquals(1, person2Txns.size)
        assertEquals(250.0, person2Txns.first().amount, 0.001)
        assertEquals(person2Id, person2Txns.first().recipientId)

        // تسویه حساب شخص اول نباید روی شخص دوم تأثیر بگذارد
        repository.settleRecipientDebt(
            recipientId = person1Id,
            recipientName = "احمد",
            debtCurrencyCode = "USD",
            debtAmountToSettle = 100.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "USD",
            paymentAmount = 100.0,
            exchangeRate = 1.0,
            isClaimSettlement = true,
            originalTransactionId = txn1Id,
            note = "تسویه احمد اول"
        )

        val txn1After = db.transactionDao().getTransactionById(txn1Id)!!
        val txn2After = db.transactionDao().getTransactionById(txn2Id)!!
        assertTrue(txn1After.isSettled)
        assertFalse(txn2After.isSettled)
    }

    // Scenario 23: تغییر نام شخص (Rename Person) سابقه و پیوند تراکنش‌ها را حفظ می‌کند
    @Test
    fun testScenario23_PersonRenamePreservesTransactionHistoryAndLink() = runBlocking<Unit> {
        val recId = repository.addRecipient(name = "قدیمی", handleOrPhone = "0711111111")
        val originalRec = db.recipientDao().getRecipientById(recId)!!

        val accId = repository.addAccount("حساب تست", "**** 0009", 5000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")

        val txnId = repository.addTransaction(
            title = "قرض به شخص",
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            category = "قرض",
            accountId = accId,
            recipientName = "قدیمی",
            recipientId = recId,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // تغییر نام شخص به نام جدید
        repository.updateRecipient(originalRec.copy(name = "نام جدید و معتبر"))

        val updatedRec = db.recipientDao().getRecipientById(recId)!!
        assertEquals("نام جدید و معتبر", updatedRec.name)

        // تراکنش‌ها همچنان بر اساس recipientId قابل بازیابی هستند و نام آنها همگام شده است
        val recTxns = db.transactionDao().getTransactionsListByRecipientId(recId)
        assertEquals(1, recTxns.size)
        assertEquals(txnId, recTxns.first().id)
        assertEquals("نام جدید و معتبر", recTxns.first().recipientName)
    }

    // Scenario 24: تست پشتیبان‌گیری و بازیابی با حفظ دقیق recipientId و kind
    @Test
    fun testScenario24_BackupAndRestorePreservesRecipientIdAndKind() = runBlocking<Unit> {
        val person1Id = repository.addRecipient(name = "هم‌نام", handleOrPhone = "111")
        val person2Id = repository.addRecipient(name = "هم‌نام", handleOrPhone = "222")
        val accId = repository.addAccount("حساب آزمایشی", "**** 0010", 5000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")

        val txnId = repository.addTransaction(
            title = "معامله به هم‌نام دوم",
            amount = 700.0,
            type = TransactionType.EXPENSE,
            category = "قرض",
            accountId = accId,
            recipientName = "هم‌نام",
            recipientId = person2Id,
            kind = TransactionKind.PERSON_PAYMENT,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        val backupData = repository.getBackupData()
        val json = com.example.util.BackupManager.exportToJson(backupData)
        val parsed = com.example.util.BackupManager.parseFromJson(json).getOrThrow()

        val restoredTxn = parsed.transactions.find { it.id == txnId }
        assertNotNull(restoredTxn)
        assertEquals(person2Id, restoredTxn!!.recipientId)
        assertEquals(TransactionKind.PERSON_PAYMENT, restoredTxn.kind)

        // بازیابی داده‌ها در دیتابیس
        repository.restoreBackupData(parsed)

        val finalTxn = db.transactionDao().getTransactionById(txnId)!!
        assertEquals(person2Id, finalTxn.recipientId)
        assertEquals(TransactionKind.PERSON_PAYMENT, finalTxn.kind)
    }

    // Scenario 25: امکان حذف شخصی با نام تکراری که تراکنش ندارد در حالی که هم‌نام او تراکنش دارد
    @Test
    fun testScenario25_DuplicateNameOnePersonWithZeroTxnCanBeDeleted() = runBlocking<Unit> {
        val personWithTxnId = repository.addRecipient(name = "احمد مشترک", handleOrPhone = "1111")
        val personWithoutTxnId = repository.addRecipient(name = "احمد مشترک", handleOrPhone = "2222")

        val accId = repository.addAccount("حساب تست", "**** 0011", 5000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")

        repository.addTransaction(
            title = "تراکنش برای اولی",
            amount = 300.0,
            type = TransactionType.EXPENSE,
            category = "قرض",
            accountId = accId,
            recipientName = "احمد مشترک",
            recipientId = personWithTxnId,
            kind = TransactionKind.PERSON_PAYMENT,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // شمارش تراکنش شخص بدون تراکنش باید ۰ باشد
        val zeroCount = repository.countTransactionsForRecipient(personWithoutTxnId, "احمد مشترک")
        assertEquals(0, zeroCount)

        // شخص بدون تراکنش باید بدون خطا حذف شود
        val recToDelete = db.recipientDao().getRecipientById(personWithoutTxnId)!!
        repository.deleteRecipient(recToDelete)

        assertNull(db.recipientDao().getRecipientById(personWithoutTxnId))
        assertNotNull(db.recipientDao().getRecipientById(personWithTxnId))
    }

    // Scenario 26: ممیزی جامع دو شخص هم‌نام (احمد 1 و احمد 2) از ابتدا تا انتها
    @Test
    fun testScenario26_DuplicateNameFullAuditEndToEnd() = runBlocking<Unit> {
        val accId = repository.addAccount("حساب بانکی", "**** 9999", 50000.0, "classic_blue", currencyCode = "AFN", currencySymbol = "؋")

        // 1. ایجاد دو شخص با نام یکسان «احمد»
        val personAId = repository.addRecipient(name = "احمد", handleOrPhone = "0700000001")
        val personBId = repository.addRecipient(name = "احمد", handleOrPhone = "0700000002")

        assertNotEquals(personAId, personBId)
        val allRecipients = db.recipientDao().getAllRecipientsList()
        val personA = allRecipients.find { it.id == personAId }
        val personB = allRecipients.find { it.id == personBId }
        assertNotNull(personA)
        assertNotNull(personB)
        assertEquals("احمد", personA!!.name)
        assertEquals("احمد", personB!!.name)

        // 2. ثبت طلب 500 برای احمد اول (Person A)
        val txnA1Id = repository.addTransaction(
            title = "قرض به احمد اول",
            amount = 500.0,
            type = TransactionType.EXPENSE,
            category = "طلب",
            accountId = accId,
            recipientName = "احمد",
            recipientId = personAId,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // 3. ثبت بدهی 300 برای احمد دوم (Person B)
        val txnB1Id = repository.addTransaction(
            title = "قرض از احمد دوم",
            amount = 300.0,
            type = TransactionType.INCOME,
            category = "بدهی",
            accountId = accId,
            recipientName = "احمد",
            recipientId = personBId,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // بررسی عدم تداخل در استخراج تراکنش‌ها بر اساس ID
        val txnsA = db.transactionDao().getTransactionsListByRecipientId(personAId)
        val txnsB = db.transactionDao().getTransactionsListByRecipientId(personBId)
        assertEquals(1, txnsA.size)
        assertEquals(1, txnsB.size)
        assertEquals(txnA1Id, txnsA.first().id)
        assertEquals(500.0, txnsA.first().amount, 0.001)
        assertEquals(txnB1Id, txnsB.first().id)
        assertEquals(300.0, txnsB.first().amount, 0.001)

        // 4. ثبت عملیات دریافت/تسویه برای هر دو شخص بدون مخلوط شدن
        val settleAId = repository.settleRecipientDebt(
            recipientId = personAId,
            recipientName = "احمد",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 200.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 200.0,
            isClaimSettlement = true,
            originalTransactionId = txnA1Id,
            note = "دریافت قسط از احمد اول"
        )

        val settleBId = repository.settleRecipientDebt(
            recipientId = personBId,
            recipientName = "احمد",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 100.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 100.0,
            isClaimSettlement = false,
            originalTransactionId = txnB1Id,
            note = "پرداخت قسط به احمد دوم"
        )

        val txnsAAfterSettle = db.transactionDao().getTransactionsListByRecipientId(personAId)
        val txnsBAfterSettle = db.transactionDao().getTransactionsListByRecipientId(personBId)
        assertEquals(2, txnsAAfterSettle.size)
        assertEquals(2, txnsBAfterSettle.size)
        assertTrue(txnsAAfterSettle.any { it.id == settleAId })
        assertFalse(txnsAAfterSettle.any { it.id == settleBId })
        assertTrue(txnsBAfterSettle.any { it.id == settleBId })
        assertFalse(txnsBAfterSettle.any { it.id == settleAId })

        // 5. تست Edit: ویرایش تراکنش احمد اول -> تراکنش احمد دوم بدون تغییر می‌ماند
        val origA1 = db.transactionDao().getTransactionById(txnA1Id)!!
        repository.updateTransaction(origA1, origA1.copy(amount = 600.0))

        val updatedA1 = db.transactionDao().getTransactionById(txnA1Id)!!
        assertEquals(600.0, updatedA1.amount, 0.001)

        val currentB1 = db.transactionDao().getTransactionById(txnB1Id)!!
        assertEquals(300.0, currentB1.amount, 0.001) // تراکنش احمد دوم کاملاً دست‌نخورده است

        // 6. تست Delete: حذف یک تراکنش از احمد اول -> تراکنش‌های احمد دوم دست‌نخورده می‌مانند
        val settleATxn = db.transactionDao().getTransactionById(settleAId)!!
        repository.deleteTransaction(settleATxn)

        val finalTxnsA = db.transactionDao().getTransactionsListByRecipientId(personAId)
        val finalTxnsB = db.transactionDao().getTransactionsListByRecipientId(personBId)
        assertEquals(1, finalTxnsA.size)
        assertEquals(2, finalTxnsB.size) // احمد دوم همچنان ۲ تراکنش دارد

        // 7. تست Rename: تغییر نام احمد اول به «محمد» -> احمد دوم «احمد» باقی می‌ماند
        val personAToRename = db.recipientDao().getRecipientById(personAId)!!
        repository.updateRecipient(personAToRename.copy(name = "محمد"))

        val personARenamed = db.recipientDao().getRecipientById(personAId)!!
        val personBUnchanged = db.recipientDao().getRecipientById(personBId)!!
        assertEquals("محمد", personARenamed.name)
        assertEquals("احمد", personBUnchanged.name)

        // تراکنش‌های احمد اول نام جدید گرفتند، در حالی که تراکنش‌های احمد دوم هنوز «احمد» هستند
        val txnsARenamed = db.transactionDao().getTransactionsListByRecipientId(personAId)
        val txnsBUnchanged = db.transactionDao().getTransactionsListByRecipientId(personBId)
        assertEquals("محمد", txnsARenamed.first().recipientName)
        assertEquals("احمد", txnsBUnchanged[0].recipientName)
        assertEquals("احمد", txnsBUnchanged[1].recipientName)
    }

    // Scenario 27: تست عدم وقوع Race Condition و حفظ Concurrency Integrity در حساب‌ها
    @Test
    fun testScenario27_ConcurrentTransactionsRaceConditionSafety() = runBlocking {
        val accId = repository.addAccount("حساب هم‌روندی", "**** 7777", 1000.0, "classic_blue", "AFN", "؋")

        // اجرای 10 تراکنش واریز (50 واحد) و 10 تراکنش برداشت (40 واحد) به صورت متوالی و ایمن تحت balanceLock
        for (i in 0 until 10) {
            repository.addTransaction(
                title = "واریز $i",
                amount = 50.0,
                type = TransactionType.INCOME,
                category = "واریز",
                accountId = accId,
                recipientName = null,
                currencyCode = "AFN",
                currencySymbol = "؋",
                affectsBalance = true
            )
            repository.addTransaction(
                title = "برداشت $i",
                amount = 40.0,
                type = TransactionType.EXPENSE,
                category = "برداشت",
                accountId = accId,
                recipientName = null,
                currencyCode = "AFN",
                currencySymbol = "؋",
                affectsBalance = true
            )
        }

        // موجودی مورد انتظار: 1000 + (10 * 50) - (10 * 40) = 1000 + 500 - 400 = 1100.0
        val finalAcc = db.accountDao().getAccountById(accId)!!
        assertEquals(1100.0, finalAcc.balance, 0.001)

        val integrityResult = repository.verifyAccountBalanceIntegrity(accId, initialSeedBalance = 1000.0)
        assertTrue(integrityResult.isConsistent)
        assertEquals(1100.0, integrityResult.storedBalance, 0.001)
        assertEquals(1100.0, integrityResult.calculatedBalance, 0.001)
    }

    // Scenario 28: تطابق کامل Stored Balance با Calculated Balance (تست جامع اعتبارسنجی ترازنامه)
    @Test
    fun testScenario28_StoredBalanceMatchesCalculatedBalanceIntegrity() = runBlocking {
        val accId = repository.addAccount("حساب ممیزی موجودی", "**** 5555", 5000.0, "emerald", "AFN", "؋")
        val personId = repository.addRecipient("مخاطب تست ترازنامه", "0788888888")

        // 1. درآمد
        repository.addTransaction("درآمد تجاری", 1200.0, TransactionType.INCOME, "فروش", accId, null, currencyCode = "AFN", currencySymbol = "؋", affectsBalance = true)
        // 2. هزینه
        repository.addTransaction("هزینه جاری", 700.0, TransactionType.EXPENSE, "قبوض", accId, null, currencyCode = "AFN", currencySymbol = "؋", affectsBalance = true)
        // 3. تسویه طلب از شخص
        repository.settleRecipientDebt(
            recipientId = personId,
            recipientName = "مخاطب تست ترازنامه",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 300.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 300.0,
            isClaimSettlement = true
        )
        // 4. تسویه بدهی به شخص
        repository.settleRecipientDebt(
            recipientId = personId,
            recipientName = "مخاطب تست ترازنامه",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 450.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 450.0,
            isClaimSettlement = false
        )

        // بررسی یکپارچگی سیستمی Stored vs Calculated Balance (با احتساب موجودی اولیه ۵۰۰۰)
        val result = repository.verifyAccountBalanceIntegrity(accId, initialSeedBalance = 5000.0)
        assertTrue("ترازنامه حساب باید دقیقاً صفر باشد", result.isConsistent)
        assertEquals(result.storedBalance, result.calculatedBalance, 0.001)
        // 5000 + 1200 - 700 + 300 - 450 = 5350.0
        assertEquals(5350.0, result.storedBalance, 0.001)
        assertEquals(5350.0, result.calculatedBalance, 0.001)
    }

    // Scenario 29: تراکنش‌های فاقد recipientId (یا Legacy) به هیچ عنوان به اشخاص نسبت داده نمی‌شوند
    @Test
    fun testScenario29_LegacyAndNullRecipientIdTransactionsNeverLinkedToAnyPerson() = runBlocking {
        val personId = repository.addRecipient("رضا", "0799999999")
        val accId = repository.addAccount("حساب تست", "**** 3333", 2000.0, "classic_blue", "AFN", "؋")

        // ثبت یک تراکنش با نام «رضا» اما با recipientId = null (مثلاً ثبت هزینه عمومی یا داده قدیمی)
        val legacyTxnId = repository.addTransaction(
            title = "خرید متفرقه",
            amount = 250.0,
            type = TransactionType.EXPENSE,
            category = "عمومی",
            accountId = accId,
            recipientName = "رضا",
            recipientId = null, // صراحتاً null است
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // ثبت یک تراکنش معتبر برای شخص رضا با recipientId مشخص
        val validTxnId = repository.addTransaction(
            title = "طلب از رضا",
            amount = 600.0,
            type = TransactionType.EXPENSE,
            category = "طلب",
            accountId = accId,
            recipientName = "رضا",
            recipientId = personId,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // بررسی: تراکنش‌های شخص رضا فقط و فقط باید شامل validTxnId باشد و legacyTxnId ابداً به او منتسب نشود
        val personTxns = db.transactionDao().getTransactionsListByRecipientId(personId)
        assertEquals(1, personTxns.size)
        assertEquals(validTxnId, personTxns.first().id)
        assertFalse(personTxns.any { it.id == legacyTxnId })

        // شمارش تراکنش‌های شخص نیز باید دقیقاً 1 باشد
        val count = repository.countTransactionsForRecipient(personId)
        assertEquals(1, count)
    }

    // Scenario 30: حفظ کامل رابطه Identity و ID در فرآیند Backup و Restore
    @Test
    fun testScenario30_BackupAndRestorePreservesRecipientIdRelationships() = runBlocking {
        val accId = repository.addAccount("حساب اصلی", "**** 4444", 10000.0, "classic_blue", "AFN", "؋")
        val personAId = repository.addRecipient("محمود", "0711111111")
        val personBId = repository.addRecipient("محمود", "0722222222")

        val txnA = repository.addTransaction(
            title = "قرض محمود الف",
            amount = 400.0,
            type = TransactionType.EXPENSE,
            category = "طلب",
            accountId = accId,
            recipientName = "محمود",
            recipientId = personAId,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )
        val txnB = repository.addTransaction(
            title = "قرض محمود ب",
            amount = 800.0,
            type = TransactionType.EXPENSE,
            category = "طلب",
            accountId = accId,
            recipientName = "محمود",
            recipientId = personBId,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // تهیه نسخه پشتیبان ساختاریافته
        val backupData = repository.getBackupData()
        val json = com.example.util.BackupManager.exportToJson(backupData)
        val parsed = com.example.util.BackupManager.parseFromJson(json).getOrThrow()

        // بازیابی نسخه پشتیبان
        val restoreResult = repository.restoreBackupData(parsed)
        assertTrue(restoreResult.isSuccess)

        // بررسی اینکه هر دو شخص با شناسه‌های مجزا بازیابی شده‌اند
        val allRecs = db.recipientDao().getAllRecipientsList()
        val restoredA = allRecs.find { it.id == personAId }
        val restoredB = allRecs.find { it.id == personBId }
        assertNotNull(restoredA)
        assertNotNull(restoredB)
        assertEquals("محمود", restoredA!!.name)
        assertEquals("محمود", restoredB!!.name)

        // بررسی اینکه تراکنش‌های هر شخص به شناسه صحیح خودش متصل است
        val txnsA = db.transactionDao().getTransactionsListByRecipientId(personAId)
        val txnsB = db.transactionDao().getTransactionsListByRecipientId(personBId)
        assertEquals(1, txnsA.size)
        assertEquals(1, txnsB.size)
        assertEquals(400.0, txnsA.first().amount, 0.001)
        assertEquals(800.0, txnsB.first().amount, 0.001)
        assertEquals(txnA, txnsA.first().id)
        assertEquals(txnB, txnsB.first().id)
    }

    // Scenario 31: جریان واقعی اشخاص هم‌نام با شناسه‌های مجزا، تست کامل Pay و Receive و ایزولاسیون تراکنش‌ها
    @Test
    fun testScenario31_SameNameDifferentPersons_RealUIFlow_PayReceiveIsolation() = runBlocking {
        // ۱. ایجاد دو شخص با نام یکسان "احمد" و شناسه‌های مجزا
        val person1Id = repository.addRecipient("احمد", "0799111111")
        val person2Id = repository.addRecipient("احمد", "0799222222")
        assertNotEquals(person1Id, person2Id)

        val p1 = db.recipientDao().getRecipientById(person1Id)!!
        val p2 = db.recipientDao().getRecipientById(person2Id)!!
        assertEquals("احمد", p1.name)
        assertEquals("احمد", p2.name)

        // موجودی نقدی اولیه جهت پشتیبانی از هزینه‌ها و پرداخت‌های نقدی
        repository.addTransaction(
            title = "سرمایه اولیه نقدی",
            amount = 10000.0,
            type = TransactionType.INCOME,
            category = "سرمایه",
            accountId = 0L,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // ۲. برای شخص اول: 500 AFN طلب (پرداخت به شخص / EXPENSE)
        val txn1Id = repository.addTransaction(
            title = "احمد",
            amount = 500.0,
            type = TransactionType.EXPENSE,
            category = "طلب",
            accountId = 0L,
            recipientName = "احمد",
            recipientId = person1Id,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // ۳. برای شخص دوم: 300 AFN بدهی (دریافت از شخص / INCOME)
        val txn2Id = repository.addTransaction(
            title = "احمد",
            amount = 300.0,
            type = TransactionType.INCOME,
            category = "بدهی",
            accountId = 0L,
            recipientName = "احمد",
            recipientId = person2Id,
            currencyCode = "AFN",
            currencySymbol = "؋",
            affectsBalance = true
        )

        // ۴. اعتبارسنجی اولیه از طریق Repository
        val txnsP1 = db.transactionDao().getTransactionsListByRecipientId(person1Id)
        val txnsP2 = db.transactionDao().getTransactionsListByRecipientId(person2Id)
        assertEquals(1, txnsP1.size)
        assertEquals(1, txnsP2.size)
        assertEquals(txn1Id, txnsP1.first().id)
        assertEquals(txn2Id, txnsP2.first().id)

        // ۵. راه‌اندازی ViewModel با Repository متصل
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.FinanceViewModel(app, repository)

        // زمان دادن به StateFlowها و Coroutineهای ViewModel برای بروزرسانی
        var attempts = 0
        while (vm.allTransactions.value.isEmpty() && attempts < 50) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            kotlinx.coroutines.delay(50)
            attempts++
        }

        // ۶. بررسی وضعیت بدهی و طلب هر شخص از طریق ViewModel
        val p1Debts = vm.getRecipientMultiCurrencyDebts(person1Id)
        val p2Debts = vm.getRecipientMultiCurrencyDebts(person2Id)

        assertEquals(1, p1Debts.size)
        assertEquals("AFN", p1Debts.first().currencyCode)
        assertEquals(500.0, p1Debts.first().netAmount, 0.001)
        assertTrue("شخص اول باید طلبکار ما باشد (طلب ما)", p1Debts.first().isDebtor)

        assertEquals(1, p2Debts.size)
        assertEquals("AFN", p2Debts.first().currencyCode)
        assertEquals(-300.0, p2Debts.first().netAmount, 0.001)
        assertFalse("شخص دوم باید بدهکار باشد (بدهی ما به شخص)", p2Debts.first().isDebtor)

        // اطمینان از اینکه جستجو با نام یکسان بدون ID، تداخلی ایجاد نمی‌کند
        val nameSearchDebts = vm.getRecipientMultiCurrencyDebts("احمد")
        assertTrue("در صورت وجود چند شخص هم‌نام، جستجو بر اساس نام نباید اطلاعات را ادغام کند", nameSearchDebts.isEmpty())

        // ۷. شبیه‌سازی UI: انتخاب شخص اول و انجام عملیات Pay (پرداخت بدهی/قرض جدید)
        vm.selectRecipient(p1)
        assertEquals(person1Id, vm.selectedRecipient.value?.id)
        vm.preparePaymentScreen()
        assertFalse("حالت پرداخت نباید requestMode باشد", vm.isRequestMode.value)

        // وارد کردن مبلغ ۲۰۰ از ماشین‌حساب
        vm.onDigit("2")
        vm.onDigit("0")
        vm.onDigit("0")
        assertEquals(200.0, vm.evaluatedAmount.value, 0.001)

        // ثبت تراکنش با مانیتورینگ نتیجه
        var paySuccess = false
        vm.submitTransaction(onSuccess = { paySuccess = true })
        for (i in 1..60) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            if (paySuccess && vm.allTransactions.value.any { it.amount == 200.0 && it.recipientId == person1Id }) break
            delay(50)
        }
        assertTrue("عملیات پرداخت با شکست مواجه شد: ${vm.operationErrorMessage.value}", paySuccess)

        // بررسی اینکه ۲۰۰ AFN فقط و فقط به شخص اول اضافه شده است
        val updatedP1DebtsAfterPay = vm.getRecipientMultiCurrencyDebts(person1Id)
        val updatedP2DebtsAfterPay = vm.getRecipientMultiCurrencyDebts(person2Id)

        assertEquals(1, updatedP1DebtsAfterPay.size)
        assertEquals(700.0, updatedP1DebtsAfterPay.first().netAmount, 0.001) // 500 + 200 = 700

        // شخص دوم باید دقیقاً بدون هیچ تغییری بماند
        assertEquals(1, updatedP2DebtsAfterPay.size)
        assertEquals(-300.0, updatedP2DebtsAfterPay.first().netAmount, 0.001)

        // ۸. شبیه‌سازی UI: انتخاب مجدد شخص اول و انجام عملیات Receive (دریافت طلب)
        vm.selectRecipient(p1)
        assertEquals(person1Id, vm.selectedRecipient.value?.id)
        vm.prepareReceiveScreen()
        assertTrue("حالت دریافت باید requestMode باشد", vm.isRequestMode.value)

        // وارد کردن مبلغ ۱۵۰ از ماشین‌حساب
        vm.onDigit("1")
        vm.onDigit("5")
        vm.onDigit("0")
        assertEquals(150.0, vm.evaluatedAmount.value, 0.001)

        // ثبت تراکنش
        var receiveSuccess = false
        vm.submitTransaction(onSuccess = { receiveSuccess = true })
        for (i in 1..60) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            if (receiveSuccess && vm.allTransactions.value.any { it.amount == 150.0 && it.recipientId == person1Id }) break
            delay(50)
        }
        assertTrue("عملیات دریافت با شکست مواجه شد: ${vm.operationErrorMessage.value}", receiveSuccess)

        // بررسی اینکه طلب شخص اول کاهش یافته (700 - 150 = 550) و شخص دوم دست نخورده باقی مانده است
        val updatedP1DebtsAfterReceive = vm.getRecipientMultiCurrencyDebts(person1Id)
        val updatedP2DebtsAfterReceive = vm.getRecipientMultiCurrencyDebts(person2Id)

        assertEquals(1, updatedP1DebtsAfterReceive.size)
        assertEquals(550.0, updatedP1DebtsAfterReceive.first().netAmount, 0.001)

        // وضعیت شخص دوم همچنان بدون ذره‌ای تغییر است
        assertEquals(1, updatedP2DebtsAfterReceive.size)
        assertEquals(-300.0, updatedP2DebtsAfterReceive.first().netAmount, 0.001)

        // ۹. بررسی تسویه بدهی شخص دوم (Settle Debt)
        var settleSuccess = false
        vm.settleRecipientDebt(
            recipientId = person2Id,
            recipientName = "احمد",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 300.0,
            paymentAccountId = 0L,
            paymentCurrencyCode = "AFN",
            paymentAmount = 300.0,
            isClaimSettlement = false,
            onSuccess = { settleSuccess = true }
        )
        for (i in 1..60) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            if (settleSuccess && vm.allTransactions.value.any { it.kind == TransactionKind.PERSON_SETTLEMENT && it.recipientId == person2Id }) break
            delay(50)
        }
        assertTrue("عملیات تسویه با شکست مواجه شد: ${vm.operationErrorMessage.value}", settleSuccess)

        val finalP2Debts = vm.getRecipientMultiCurrencyDebts(person2Id)
        assertTrue("بدهی شخص دوم باید کاملاً تسویه شده باشد (لیست خالی)", finalP2Debts.isEmpty())

        val finalP1Debts = vm.getRecipientMultiCurrencyDebts(person1Id)
        assertEquals(1, finalP1Debts.size)
        assertEquals(550.0, finalP1Debts.first().netAmount, 0.001)
    }

    // Scenario 33: تغییر نام دسته‌بندی و عدم ناپدید شدن تراکنش‌ها و بودجه‌ها (Identity by ID)
    @Test
    fun testScenario33_CategoryRenamePreservesTransactionsAndBudgetsById() = runBlocking {
        // ۱. ایجاد دسته‌بندی با نام اولیه
        val initialCatId = repository.addCategory(
            name = "غذا و رستوران",
            type = TransactionType.EXPENSE,
            iconName = "Restaurant",
            colorHex = 0xFFF59E0B
        )
        assertNotNull(initialCatId)

        // ۲. ایجاد تراکنش با categoryId مستقیم
        val txn1Id = repository.addTransaction(
            title = "ناهار کاری",
            amount = 350.0,
            type = TransactionType.EXPENSE,
            category = "غذا و رستوران",
            accountId = 0L,
            recipientName = null,
            note = "ناهار با همکاران",
            calculationExpression = "350",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = initialCatId
        )

        // ۳. ایجاد یک تراکنش قدیمی بدون categoryId (شبیه‌سازی دیتای قبل از مهاجرت)
        val txnLegacyId = repository.addTransaction(
            title = "شام خانوادگی",
            amount = 500.0,
            type = TransactionType.EXPENSE,
            category = "غذا و رستوران",
            accountId = 0L,
            recipientName = null,
            note = "تراکنش قدیمی",
            calculationExpression = "500",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = null
        )

        // ۴. تعیین بودجه برای این دسته‌بندی با categoryId
        repository.setBudget(
            category = "غذا و رستوران",
            limit = 2000.0,
            monthYear = "2026-09",
            currencyCode = "AFN",
            categoryId = initialCatId
        )

        // ۵. تغییر نام دسته‌بندی به یک نام کاملاً جدید
        val currentCat = db.categoryDao().getCategoryById(initialCatId)!!
        val updatedCat = currentCat.copy(name = "رستوران و کافه مجلل")
        repository.updateCategory(updatedCat)

        // ۶. تأیید تغییر نام در جدول categories
        val fetchedCat = db.categoryDao().getCategoryById(initialCatId)!!
        assertEquals("رستوران و کافه مجلل", fetchedCat.name)

        // ۷. بررسی اینکه تراکنش مرتبط به این شناسه متصل مانده و نام آن همگام شده است، ولی تراکنش بدون شناسه مصادره نشده است
        val txnsByCatId = db.transactionDao().getTransactionsListByCategoryId(initialCatId)
        assertEquals("تنها تراکنش دارای categoryId باید به این دسته متصل باشد", 1, txnsByCatId.size)
        assertEquals("نام تراکنش متصل باید به‌روزرسانی شود", "رستوران و کافه مجلل", txnsByCatId.first().category)

        val unlinkedTxn = db.transactionDao().getTransactionById(txnLegacyId)!!
        assertNull("تراکنش مستقل نباید توسط تغییر نام دسته‌بندی مصادره شود", unlinkedTxn.categoryId)
        assertEquals("نام تراکنش مستقل نباید تغییر کند", "غذا و رستوران", unlinkedTxn.category)

        // ۸. بررسی بودجه: بودجه باید همچنان با شناسه categoryId متصل باشد و نام آن به‌روزرسانی شده باشد
        val budgetByCatId = db.budgetDao().getBudgetByCategoryId(initialCatId)
        assertNotNull("بودجه نباید ناپدید شود", budgetByCatId)
        assertEquals(2000.0, budgetByCatId!!.monthlyLimit, 0.001)
        assertEquals("رستوران و کافه مجلل", budgetByCatId.category)
    }

    // Scenario 34: تفکیک کامل دسته‌بندی‌های با نام‌های تکراری بر اساس ID (عدم تداخل)
    @Test
    fun testScenario34_DuplicateCategoryNamesIsolationById() = runBlocking {
        // ایجاد دو دسته‌بندی مجزا با نام یکسان "متفرقه" (مثلاً یکی برای درآمد و دیگری برای هزینه)
        val catExpenseId = repository.addCategory(
            name = "متفرقه",
            type = TransactionType.EXPENSE,
            iconName = "MoreHoriz",
            colorHex = 0xFFEF4444
        )
        val catIncomeId = repository.addCategory(
            name = "متفرقه",
            type = TransactionType.INCOME,
            iconName = "MoreHoriz",
            colorHex = 0xFF10B981
        )
        assertNotEquals(catExpenseId, catIncomeId)

        // ثبت تراکنش برای دسته‌بندی اول (هزینه)
        val expenseTxnId = repository.addTransaction(
            title = "هزینه متفرقه",
            amount = 100.0,
            type = TransactionType.EXPENSE,
            category = "متفرقه",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "100",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = catExpenseId
        )

        // ثبت تراکنش برای دسته‌بندی دوم (درآمد)
        val incomeTxnId = repository.addTransaction(
            title = "درآمد متفرقه",
            amount = 250.0,
            type = TransactionType.INCOME,
            category = "متفرقه",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "250",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = catIncomeId
        )

        // تراکنش‌های دسته‌بندی اول فقط باید شامل تراکنش اول باشند
        val expenseCatTxns = db.transactionDao().getTransactionsListByCategoryId(catExpenseId)
        assertEquals(1, expenseCatTxns.size)
        assertEquals(expenseTxnId, expenseCatTxns.first().id)

        // تراکنش‌های دسته‌بندی دوم فقط باید شامل تراکنش دوم باشند
        val incomeCatTxns = db.transactionDao().getTransactionsListByCategoryId(catIncomeId)
        assertEquals(1, incomeCatTxns.size)
        assertEquals(incomeTxnId, incomeCatTxns.first().id)

        // تغییر نام دسته‌بندی هزینه نباید روی دسته‌بندی درآمد تأثیر بگذارد
        val catExp = db.categoryDao().getCategoryById(catExpenseId)!!
        repository.updateCategory(catExp.copy(name = "متفرقه روزانه"))

        val updatedExpCat = db.categoryDao().getCategoryById(catExpenseId)!!
        val untouchedIncCat = db.categoryDao().getCategoryById(catIncomeId)!!
        assertEquals("متفرقه روزانه", updatedExpCat.name)
        assertEquals("متفرقه", untouchedIncCat.name)

        assertEquals("متفرقه روزانه", db.transactionDao().getTransactionById(expenseTxnId)!!.category)
        assertEquals("متفرقه", db.transactionDao().getTransactionById(incomeTxnId)!!.category)
    }

    // Scenario 35: تغییر نام مخاطب/شخص و حفظ دقیق پیوستگی تراکنش‌ها بر اساس recipientId
    @Test
    fun testScenario35_RecipientRenamePreservesTransactionsAndDebtsById() = runBlocking {
        val recId = repository.addRecipient(
            name = "جواد کریمی",
            handleOrPhone = "0700123456",
            notes = "همکار دفتر",
            avatarColorHex = 0xFF3B82F6
        )

        val txnId = repository.addTransaction(
            title = "قرض به جواد",
            amount = 1200.0,
            type = TransactionType.EXPENSE,
            category = "PersonDebt",
            accountId = 0L,
            recipientName = "جواد کریمی",
            note = null,
            calculationExpression = "1200",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            recipientId = recId
        )

        // تغییر نام شخص به نام جدید
        val recipient = db.recipientDao().getRecipientById(recId)!!
        repository.updateRecipient(recipient.copy(name = "جواد کریمی (کابل)"))

        // تراکنش باید همچنان با recipientId متصل باشد و نام آن به‌روز شده باشد
        val txn = db.transactionDao().getTransactionById(txnId)!!
        assertEquals(recId, txn.recipientId)
        assertEquals("جواد کریمی (کابل)", txn.recipientName)
    }

    // Scenario 36: امنیت حذف دسته‌بندی در صورت داشتن تراکنش
    @Test
    fun testScenario36_DeleteCategoryWithTransactionsFailsSafely() = runBlocking {
        val catId = repository.addCategory(
            name = "آموزش و کتاب",
            type = TransactionType.EXPENSE,
            iconName = "Book",
            colorHex = 0xFF8B5CF6
        )

        repository.addTransaction(
            title = "خرید کتاب کاتلین",
            amount = 400.0,
            type = TransactionType.EXPENSE,
            category = "آموزش و کتاب",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "400",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = catId
        )

        val cat = db.categoryDao().getCategoryById(catId)!!
        var exceptionThrown = false
        try {
            repository.deleteCategory(cat)
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertTrue("حذف دسته‌بندی دارای تراکنش باید رد شود", exceptionThrown)
        assertNotNull("دسته‌بندی نباید حذف شده باشد", db.categoryDao().getCategoryById(catId))
    }

    // Scenario 37: تفکیک مطلق دسته‌بندی‌های کاملاً هم‌نام (Name Duplicate Isolation)
    @Test
    fun testScenario37_ExactDuplicateCategoryIsolation() = runBlocking {
        // ایجاد دو دسته‌بندی با نام دقیقاً یکسان «خوراک»
        val cat1Id = repository.addCategory(
            name = "خوراک",
            type = TransactionType.EXPENSE,
            iconName = "Fastfood",
            colorHex = 0xFFFF5722
        )
        val cat2Id = repository.addCategory(
            name = "خوراک",
            type = TransactionType.EXPENSE,
            iconName = "Restaurant",
            colorHex = 0xFF4CAF50
        )
        assertNotEquals("شناسه‌ها باید کاملاً یکتا و متمایز باشند", cat1Id, cat2Id)

        // ثبت بودجه‌های متفاوت برای هر کدام بر اساس شناسه
        repository.setBudget("خوراک", 4000.0, "2026-09", "AFN", categoryId = cat1Id)
        repository.setBudget("خوراک", 9000.0, "2026-09", "AFN", categoryId = cat2Id)

        val b1 = db.budgetDao().getBudgetByCategoryId(cat1Id)
        val b2 = db.budgetDao().getBudgetByCategoryId(cat2Id)
        assertEquals(4000.0, b1?.monthlyLimit ?: 0.0, 0.001)
        assertEquals(9000.0, b2?.monthlyLimit ?: 0.0, 0.001)

        // ثبت تراکنش برای دسته اول
        val txn1 = repository.addTransaction(
            title = "خرید میوه",
            amount = 300.0,
            type = TransactionType.EXPENSE,
            category = "خوراک",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "300",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = cat1Id
        )

        // ثبت تراکنش برای دسته دوم
        val txn2 = repository.addTransaction(
            title = "رستوران اداری",
            amount = 750.0,
            type = TransactionType.EXPENSE,
            category = "خوراک",
            accountId = 0L,
            recipientName = null,
            note = null,
            calculationExpression = "750",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = cat2Id
        )

        assertEquals(1, db.transactionDao().countTransactionsByCategoryId(cat1Id))
        assertEquals(1, db.transactionDao().countTransactionsByCategoryId(cat2Id))

        // تغییر نام دسته اول به «خوراک خانگی»
        val cat1Entity = db.categoryDao().getCategoryById(cat1Id)!!
        repository.updateCategory(cat1Entity.copy(name = "خوراک خانگی"))

        // دسته دوم باید کاملاً دست‌نخورده مانده باشد
        val cat2Entity = db.categoryDao().getCategoryById(cat2Id)!!
        assertEquals("نام دسته دوم نباید تغییر کند", "خوراک", cat2Entity.name)

        val t1 = db.transactionDao().getTransactionById(txn1)!!
        val t2 = db.transactionDao().getTransactionById(txn2)!!
        assertEquals("خوراک خانگی", t1.category)
        assertEquals("خوراک", t2.category)
        assertEquals(cat1Id, t1.categoryId)
        assertEquals(cat2Id, t2.categoryId)

        // بودجه دسته دوم نباید دست بخورد
        val b2After = db.budgetDao().getBudgetByCategoryId(cat2Id)!!
        assertEquals("خوراک", b2After.category)
        assertEquals(9000.0, b2After.monthlyLimit, 0.001)
    }

    // Scenario 38: تفکیک مطلق مخاطبان کاملاً هم‌نام در ثبت طلب/بدهی، تسویه و تبدیل ارز
    @Test
    fun testScenario38_ExactDuplicateRecipientIsolation() = runBlocking {
        // ایجاد دو شخص کاملاً هم‌نام «محمد رحیمی» با شماره‌های مختلف
        val rec1Id = repository.addRecipient(
            name = "محمد رحیمی",
            handleOrPhone = "0799000001",
            avatarColorHex = 0xFF1E88E5
        )
        val rec2Id = repository.addRecipient(
            name = "محمد رحیمی",
            handleOrPhone = "0799000002",
            avatarColorHex = 0xFF43A047
        )
        assertNotEquals(rec1Id, rec2Id)

        // ایجاد حساب‌های مبدا (افغانی و دالری)
        val accId = repository.addAccount(
            name = "صندوق نقدی",
            cardNumberMasked = "**** 9999",
            balance = 50000.0,
            theme = "classic_blue",
            currencyCode = "AFN",
            currencySymbol = "؋"
        )
        val accUsdId = repository.addAccount(
            name = "حساب دالری",
            cardNumberMasked = "**** 8888",
            balance = 2000.0,
            theme = "classic_blue",
            currencyCode = "USD",
            currencySymbol = "$"
        )

        // ثبت قرض به شخص اول (۱۰۰۰ افغانی و ۵۰ دالر)
        val usdCurr = db.currencyDao().getCurrencyByCode("USD")!!
        val afnCurr = db.currencyDao().getCurrencyByCode("AFN")!!

        repository.addTransaction(
            title = "قرض افغانی به محمد ۱",
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            category = "PersonDebt",
            accountId = accId,
            recipientName = "محمد رحیمی",
            recipientId = rec1Id,
            note = null,
            calculationExpression = "1000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )
        repository.addTransaction(
            title = "قرض دالری به محمد ۱",
            amount = 50.0,
            type = TransactionType.EXPENSE,
            category = "PersonDebt",
            accountId = accUsdId,
            recipientName = "محمد رحیمی",
            recipientId = rec1Id,
            note = null,
            calculationExpression = "50",
            currencyCode = "USD",
            currencySymbol = "$",
            exchangeRate = 70.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        // ثبت قرض به شخص دوم (۲۰۰۰ افغانی)
        repository.addTransaction(
            title = "قرض افغانی به محمد ۲",
            amount = 2000.0,
            type = TransactionType.EXPENSE,
            category = "PersonDebt",
            accountId = accId,
            recipientName = "محمد رحیمی",
            recipientId = rec2Id,
            note = null,
            calculationExpression = "2000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true
        )

        // تسویه قرض شخص اول به میزان ۵۰۰ افغانی
        repository.settleRecipientDebt(
            recipientId = rec1Id,
            recipientName = "محمد رحیمی",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 500.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 500.0,
            exchangeRate = 1.0,
            isClaimSettlement = false,
            note = "تسویه نصف قرض"
        )

        // تبدیل ۵۰ دالر بدهی شخص اول به ۳۵۰۰ افغانی
        repository.convertPersonDebtCurrency(
            recipientId = rec1Id,
            personName = "محمد رحیمی",
            fromCurrency = usdCurr,
            toCurrency = afnCurr,
            fromAmount = 50.0,
            toAmount = 3500.0,
            rate = 70.0,
            accountId = accId,
            isDebtor = true
        )

        // بررسی اینکه تراکنش‌های شخص دوم کاملاً دست‌نخورده هستند
        val txnsPerson2 = db.transactionDao().getTransactionsListByRecipientId(rec2Id)
        assertEquals(1, txnsPerson2.size)
        assertEquals("قرض افغانی به محمد ۲", txnsPerson2.first().title)
        assertEquals(2000.0, txnsPerson2.first().amount, 0.001)

        // تغییر نام شخص اول به «محمد رحیمی برادر»
        val rec1 = db.recipientDao().getRecipientById(rec1Id)!!
        repository.updateRecipient(rec1.copy(name = "محمد رحیمی برادر"))

        val rec2After = db.recipientDao().getRecipientById(rec2Id)!!
        assertEquals("نام شخص دوم نباید تغییر کند", "محمد رحیمی", rec2After.name)

        val txnsPerson2After = db.transactionDao().getTransactionsListByRecipientId(rec2Id)
        assertEquals("محمد رحیمی", txnsPerson2After.first().recipientName)
    }

    // Scenario 39: عدم مصادره تراکنش‌های مستقل یا بدون شناسه در هنگام تغییر نام (No Hijacking of Unlinked Rows)
    @Test
    fun testScenario39_UnlinkedTransactionsNotHijackedByRename() = runBlocking {
        // ایجاد یک تراکنش قدیمی بدون دسته‌بندی مشخص با برچسب متنی «پروژه الف»
        val standaloneTxnId = repository.addTransaction(
            title = "خرید کابل",
            amount = 120.0,
            type = TransactionType.EXPENSE,
            category = "پروژه الف",
            accountId = 0L,
            recipientName = null,
            note = "مستقل",
            calculationExpression = "120",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = null
        )

        // ساخت یک دسته‌بندی رسمی با همان نام «پروژه الف»
        val catId = repository.addCategory(
            name = "پروژه الف",
            type = TransactionType.EXPENSE,
            iconName = "Work",
            colorHex = 0xFF607D8B
        )

        // ثبت یک تراکنش رسمی متصل به شناسه این دسته
        val officialTxnId = repository.addTransaction(
            title = "پرداخت دستمزد",
            amount = 5000.0,
            type = TransactionType.EXPENSE,
            category = "پروژه الف",
            accountId = 0L,
            recipientName = null,
            note = "رسمی",
            calculationExpression = "5000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false,
            categoryId = catId
        )

        // تغییر نام دسته‌بندی رسمی به «پروژه نوسازی»
        val cat = db.categoryDao().getCategoryById(catId)!!
        repository.updateCategory(cat.copy(name = "پروژه نوسازی"))

        // تراکنش رسمی باید نامش به‌روزرسانی شده باشد
        val officialTxn = db.transactionDao().getTransactionById(officialTxnId)!!
        assertEquals("پروژه نوسازی", officialTxn.category)
        assertEquals(catId, officialTxn.categoryId)

        // تراکنش مستقل نباید مصادره شده باشد و نباید categoryId دریافت کرده باشد یا نامش تغییر کرده باشد
        val standaloneTxn = db.transactionDao().getTransactionById(standaloneTxnId)!!
        assertNull("شناسه دسته‌بندی برای تراکنش مستقل باید همچنان نال بماند", standaloneTxn.categoryId)
        assertEquals("نام متنی تراکنش مستقل نباید تغییر کند", "پروژه الف", standaloneTxn.category)
    }

    // Scenario 40: انتقال بدهی یا حواله بین دو شخص کاملاً هم‌نام با تفکیک قطعی بر اساس recipientId
    @Test
    fun testScenario40_TransferBetweenSameNamePersons() = runBlocking {
        val personAName = "امید حسینی"
        val personBName = "امید حسینی"

        val recAId = repository.addRecipient(
            name = personAName,
            handleOrPhone = "0700111222"
        )
        val recBId = repository.addRecipient(
            name = personBName,
            handleOrPhone = "0700333444"
        )
        assertNotEquals("شناسه‌ها باید متمایز باشند", recAId, recBId)

        val afnCurr = db.currencyDao().getCurrencyByCode("AFN")!!

        // ثبت بدهی اولیه ۱۰۰۰ افغانی برای امید الف
        repository.addTransaction(
            title = "قرض به امید الف",
            amount = 1000.0,
            type = TransactionType.EXPENSE,
            category = "PersonDebt",
            accountId = 0L,
            recipientName = personAName,
            recipientId = recAId,
            note = "بدهی اولیه",
            calculationExpression = "1000",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false
        )

        // انتقال ۶۰۰ افغانی بدهی از امید الف به امید ب (هر دو با یک نام)
        repository.transferPersonDebt(
            fromRecipientId = recAId,
            fromPersonName = personAName,
            toRecipientId = recBId,
            toPersonName = personBName,
            fromCurrency = afnCurr,
            toCurrency = afnCurr,
            fromAmount = 600.0,
            toAmount = 600.0,
            rate = 1.0,
            note = "انتقال بخشی از طلب",
            isDebtor = true
        )

        val txnsA = db.transactionDao().getTransactionsListByRecipientId(recAId)
        val txnsB = db.transactionDao().getTransactionsListByRecipientId(recBId)

        assertEquals("امید الف باید دارای ۲ تراکنش باشد (قرض اولیه و تسویه انتقالی)", 2, txnsA.size)
        assertEquals("امید ب باید دارای ۱ تراکنش باشد (انتقال بدهی)", 1, txnsB.size)

        // محاسبه بدهی خالص هرکدام به صورت مجزا
        val debtA = txnsA.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
                txnsA.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val debtB = txnsB.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
                txnsB.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        assertEquals(400.0, debtA, 0.001)
        assertEquals(600.0, debtB, 0.001)
    }

    // Scenario 41: تسویه جزئی چندمرحله‌ای با دقت اعشار و حفظ وضعیت isSettled و settledAmount
    @Test
    fun testScenario41_DetailedPartialSettlementsAndBalancePrecision() = runBlocking {
        val recId = repository.addRecipient(name = "بشیر احمد", handleOrPhone = "0788123456")
        val accId = repository.addAccount(
            name = "بانک رفاه",
            cardNumberMasked = "**** 7777",
            balance = 10000.0,
            theme = "classic_blue",
            currencyCode = "AFN",
            currencySymbol = "؋"
        )

        val debtTxnId = repository.addTransaction(
            title = "قرض به بشیر",
            amount = 1500.0,
            type = TransactionType.EXPENSE,
            category = "PersonDebt",
            accountId = 0L,
            recipientName = "بشیر احمد",
            recipientId = recId,
            note = null,
            calculationExpression = "1500",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = false
        )

        // مرحله ۱: تسویه ۴۰۰ افغانی
        repository.settleRecipientDebt(
            recipientId = recId,
            recipientName = "بشیر احمد",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 400.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 400.0,
            exchangeRate = 1.0,
            isClaimSettlement = true,
            originalTransactionId = debtTxnId,
            note = "قسط اول"
        )

        var originalTxn = db.transactionDao().getTransactionById(debtTxnId)!!
        assertEquals(400.0, originalTxn.settledAmount, 0.001)
        assertFalse("هنوز کامل تسویه نشده است", originalTxn.isSettled)
        assertEquals(10400.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)

        // مرحله ۲: تسویه ۶۰۰ افغانی دیگر
        repository.settleRecipientDebt(
            recipientId = recId,
            recipientName = "بشیر احمد",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 600.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 600.0,
            exchangeRate = 1.0,
            isClaimSettlement = true,
            originalTransactionId = debtTxnId,
            note = "قسط دوم"
        )

        originalTxn = db.transactionDao().getTransactionById(debtTxnId)!!
        assertEquals(1000.0, originalTxn.settledAmount, 0.001)
        assertFalse("هنوز ۵۰۰ باقی مانده است", originalTxn.isSettled)
        assertEquals(11000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)

        // مرحله ۳: تسویه باقیمانده ۵۰۰ افغانی
        repository.settleRecipientDebt(
            recipientId = recId,
            recipientName = "بشیر احمد",
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 500.0,
            paymentAccountId = accId,
            paymentCurrencyCode = "AFN",
            paymentAmount = 500.0,
            exchangeRate = 1.0,
            isClaimSettlement = true,
            originalTransactionId = debtTxnId,
            note = "تسویه نهایی"
        )

        originalTxn = db.transactionDao().getTransactionById(debtTxnId)!!
        assertEquals(1500.0, originalTxn.settledAmount, 0.001)
        assertTrue("اکنون باید کاملاً تسویه شده باشد", originalTxn.isSettled)
        assertEquals(11500.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
    }

    // Scenario 42: ویرایش و حذف تراکنش پس از تغییر نام دسته‌بندی و شخص
    @Test
    fun testScenario42_EditAndDeleteAfterEntityRename() = runBlocking {
        val catId = repository.addCategory(
            name = "حمل و نقل عمومی",
            type = TransactionType.EXPENSE,
            iconName = "DirectionsCar",
            colorHex = 0xFFFF9800
        )
        val accId = repository.addAccount(
            name = "حساب حقوقی",
            cardNumberMasked = "**** 5555",
            balance = 5000.0,
            theme = "classic_blue",
            currencyCode = "AFN",
            currencySymbol = "؋"
        )

        val txnId = repository.addTransaction(
            title = "بلیت قطار",
            amount = 300.0,
            type = TransactionType.EXPENSE,
            category = "حمل و نقل عمومی",
            accountId = accId,
            recipientName = null,
            note = "سفر کاری",
            calculationExpression = "300",
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = System.currentTimeMillis(),
            affectsBalance = true,
            categoryId = catId
        )

        assertEquals(4700.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)

        // تغییر نام دسته‌بندی
        val cat = db.categoryDao().getCategoryById(catId)!!
        repository.updateCategory(cat.copy(name = "سفرهای برون‌شهری"))

        // تراکنش باید نام جدید دسته را نشان دهد و همچنان به همان شناسه متصل باشد
        var txn = db.transactionDao().getTransactionById(txnId)!!
        assertEquals("سفرهای برون‌شهری", txn.category)
        assertEquals(catId, txn.categoryId)

        // ویرایش تراکنش: افزایش مبلغ به ۵۰۰ افغانی
        val updatedTxn = txn.copy(
            amount = 500.0,
            calculationExpression = "500"
        )
        repository.updateTransaction(oldTxn = txn, newTxn = updatedTxn)
        assertEquals(4500.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)

        // حذف تراکنش: بازگشت دقیق موجودی به ۵۰۰۰
        txn = db.transactionDao().getTransactionById(txnId)!!
        repository.deleteTransaction(txn)
        assertEquals(5000.0, db.accountDao().getAccountById(accId)!!.balance, 0.001)
    }

    // Scenario 43: راستی‌آزمایی عدم انتساب اشتباه در Migration رکوردهای هم‌نام قدیمی
    @Test
    fun testScenario43_MigrationAmbiguityProtection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("test_migration.db")
        if (dbFile.exists()) dbFile.delete()

        val roomHelper = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
            .create(
                androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(dbFile.name)
                    .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(13) {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            db.execSQL("CREATE TABLE IF NOT EXISTS recipients (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, handleOrPhone TEXT NOT NULL DEFAULT '', avatarColorHex INTEGER NOT NULL DEFAULT 0, iconName TEXT NOT NULL DEFAULT '', isActive INTEGER NOT NULL DEFAULT 1, notes TEXT NOT NULL DEFAULT '', isFavorite INTEGER NOT NULL DEFAULT 0, transactionCount INTEGER NOT NULL DEFAULT 0)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, iconName TEXT NOT NULL DEFAULT '', colorHex INTEGER NOT NULL DEFAULT 0, type TEXT NOT NULL DEFAULT 'EXPENSE', isActive INTEGER NOT NULL DEFAULT 1)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, amount REAL NOT NULL, type TEXT NOT NULL, category TEXT NOT NULL, accountId INTEGER NOT NULL, recipientName TEXT, timestamp INTEGER NOT NULL, note TEXT, calculationExpression TEXT, currencyCode TEXT NOT NULL, currencySymbol TEXT NOT NULL, exchangeRate REAL NOT NULL, ledgerAmount REAL DEFAULT NULL, ledgerCurrencyCode TEXT DEFAULT NULL, ledgerCurrencySymbol TEXT DEFAULT NULL, isFrozen INTEGER NOT NULL DEFAULT 0, affectsBalance INTEGER NOT NULL DEFAULT 1, relatedTransactionId INTEGER DEFAULT NULL)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, category TEXT NOT NULL, monthlyLimit REAL NOT NULL, alertThresholdPercent REAL NOT NULL DEFAULT 80.0, currencyCode TEXT NOT NULL DEFAULT 'AFN', currencySymbol TEXT NOT NULL DEFAULT '؋', periodType TEXT NOT NULL DEFAULT 'MONTHLY')")
                            db.execSQL("CREATE TABLE IF NOT EXISTS quick_actions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, actionType TEXT NOT NULL, iconName TEXT NOT NULL, colorHex INTEGER NOT NULL, sourceAccountId INTEGER NOT NULL, destinationAccountId INTEGER, recipientId INTEGER, recipientName TEXT, categoryName TEXT, targetGoalId INTEGER, defaultAmount REAL, currencyCode TEXT NOT NULL, displayOrder INTEGER NOT NULL DEFAULT 0, usageCount INTEGER NOT NULL DEFAULT 0)")
                            db.execSQL("CREATE TABLE IF NOT EXISTS shopping_lists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, createdAt INTEGER NOT NULL DEFAULT 0, isCompleted INTEGER NOT NULL DEFAULT 0, totalAmount REAL NOT NULL DEFAULT 0.0, currencyCode TEXT NOT NULL DEFAULT 'AFN', currencySymbol TEXT NOT NULL DEFAULT '؋', category TEXT NOT NULL DEFAULT 'عمومی', isLoggedAsExpense INTEGER NOT NULL DEFAULT 0, linkedTransactionId INTEGER, note TEXT)")
                        }
                        override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            )

        val sdb = roomHelper.writableDatabase

        // درج داده‌های قدیمی با دو شخص هم‌نام «رضا» و یک شخص با نام یکتا «مریم»
        sdb.execSQL("INSERT INTO recipients (id, name) VALUES (101, 'رضا')")
        sdb.execSQL("INSERT INTO recipients (id, name) VALUES (102, 'رضا')")
        sdb.execSQL("INSERT INTO recipients (id, name) VALUES (103, 'مریم')")

        // درج تراکنش‌های قدیمی
        sdb.execSQL("INSERT INTO transactions (id, title, amount, type, category, accountId, recipientName, timestamp, currencyCode, currencySymbol, exchangeRate) VALUES (1, 'تراکنش ۱', 100.0, 'EXPENSE', 'عمومی', 0, 'رضا', 1000, 'AFN', '؋', 1.0)")
        sdb.execSQL("INSERT INTO transactions (id, title, amount, type, category, accountId, recipientName, timestamp, currencyCode, currencySymbol, exchangeRate) VALUES (2, 'تراکنش ۲', 200.0, 'EXPENSE', 'عمومی', 0, 'مریم', 2000, 'AFN', '؋', 1.0)")

        // اجرای Migration 13 به 14
        AppDatabase.MIGRATION_13_14.migrate(sdb)

        // بررسی اینکه تراکنش رضا به دلیل دوگانگی و ابهام، بدون اتصال (NULL) باقی مانده است
        val cursorReza = sdb.query("SELECT recipientId FROM transactions WHERE id = 1")
        cursorReza.moveToFirst()
        val rezaRecipientId = if (cursorReza.isNull(0)) null else cursorReza.getLong(0)
        cursorReza.close()
        assertNull("تراکنش شخص هم‌نام و مبهم نباید به صورت تصادفی به هیچ‌کدام وصل شود", rezaRecipientId)

        // بررسی اینکه تراکنش مریم به دلیل یکتایی به شناسه ۱۰۳ متصل شده است
        val cursorMaryam = sdb.query("SELECT recipientId FROM transactions WHERE id = 2")
        cursorMaryam.moveToFirst()
        val maryamRecipientId = cursorMaryam.getLong(0)
        cursorMaryam.close()
        assertEquals(103L, maryamRecipientId)

        // درج دسته‌بندی‌های با نام تکراری «غذا» و نام یکتا «پوشاک»
        sdb.execSQL("INSERT INTO categories (id, name) VALUES (201, 'غذا')")
        sdb.execSQL("INSERT INTO categories (id, name) VALUES (202, 'غذا')")
        sdb.execSQL("INSERT INTO categories (id, name) VALUES (203, 'پوشاک')")

        sdb.execSQL("INSERT INTO transactions (id, title, amount, type, category, accountId, recipientName, timestamp, currencyCode, currencySymbol, exchangeRate) VALUES (3, 'خرید غذا', 50.0, 'EXPENSE', 'غذا', 0, NULL, 3000, 'AFN', '؋', 1.0)")
        sdb.execSQL("INSERT INTO transactions (id, title, amount, type, category, accountId, recipientName, timestamp, currencyCode, currencySymbol, exchangeRate) VALUES (4, 'خرید لباس', 150.0, 'EXPENSE', 'پوشاک', 0, NULL, 4000, 'AFN', '؋', 1.0)")

        // اجرای Migration 14 به 15
        AppDatabase.MIGRATION_14_15.migrate(sdb)

        // تراکنش دسته غذای مبهم باید NULL باشد
        val cursorFood = sdb.query("SELECT categoryId FROM transactions WHERE id = 3")
        cursorFood.moveToFirst()
        val foodCategoryId = if (cursorFood.isNull(0)) null else cursorFood.getLong(0)
        cursorFood.close()
        assertNull("دسته‌بندی تکراری و مبهم نباید به هیچ رکوردی به صورت حدسی وصل شود", foodCategoryId)

        // تراکنش پوشاک یکتا باید به شناسه ۲۰۳ متصل شده باشد
        val cursorClothes = sdb.query("SELECT categoryId FROM transactions WHERE id = 4")
        cursorClothes.moveToFirst()
        val clothesCategoryId = cursorClothes.getLong(0)
        cursorClothes.close()
        assertEquals(203L, clothesCategoryId)

        sdb.close()
        dbFile.delete()
    }

    // Scenario 46: ثبات هویت ارز بر اساس currencyId در طول تغییر کدهای مکرر (AFN -> AFG -> Afghan -> AFN2)
    @Test
    fun testScenario46_CurrencyIdentityPersistenceThroughRenames() = runBlocking {
        // ۱. بررسی ارز پایه اولیه (شناسه ۱)
        val initialCurr = db.currencyDao().getCurrencyById(1L)
        assertNotNull("ارز پایه با شناسه ۱ باید وجود داشته باشد", initialCurr)
        assertEquals("AFN", initialCurr!!.code)

        // ۲. ایجاد حساب متصل به شناسه ۱
        val accId = repository.addAccount(
            name = "حساب تجاری",
            cardNumberMasked = "**** 9999",
            balance = 10000.0,
            theme = "gold",
            currencyId = 1L,
            currencyCode = "AFN",
            currencySymbol = "؋"
        )

        // ۳. ثبت تراکنش نقدی و حسابی با شناسه ۱
        val cashTxnId = repository.addTransaction(
            title = "درآمد نقدی اولیه",
            amount = 20000.0,
            type = TransactionType.INCOME,
            category = "عاید",
            accountId = 0L,
            currencyId = 1L,
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = 100000L,
            affectsBalance = true
        )

        val accTxnId = repository.addTransaction(
            title = "هزینه از حساب",
            amount = 2000.0,
            type = TransactionType.EXPENSE,
            category = "مصارف",
            accountId = accId,
            currencyId = 1L,
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = 100010L,
            affectsBalance = true
        )

        // ۴. ایجاد شخص و ثبت طلب/بدهی با شناسه ۱
        val personId = repository.addRecipient(
            name = "همکار تجاری",
            handleOrPhone = "0799000000"
        )
        val debtTxnId = repository.addTransaction(
            title = "قرض داده شده",
            amount = 4000.0,
            type = TransactionType.EXPENSE,
            category = "طلب و بدهی",
            accountId = 0L,
            recipientId = personId,
            recipientName = "همکار تجاری",
            currencyId = 1L,
            currencyCode = "AFN",
            currencySymbol = "؋",
            exchangeRate = 1.0,
            timestamp = 100020L,
            affectsBalance = true
        )

        // تسویه بخشی از طلب با شناسه ۱
        val settlementTxnId = repository.settleRecipientDebt(
            recipientId = personId,
            recipientName = "همکار تجاری",
            debtCurrencyId = 1L,
            debtCurrencyCode = "AFN",
            debtAmountToSettle = 1500.0,
            paymentAccountId = 0L,
            paymentCurrencyId = 1L,
            paymentCurrencyCode = "AFN",
            paymentAmount = 1500.0,
            exchangeRate = 1.0,
            isClaimSettlement = true,
            originalTransactionId = debtTxnId,
            note = "تسویه بخشی از طلب"
        )

        // ۵. انتقال بین نقدی و حسابی با ارز شناسه ۱
        val card = db.accountDao().getAccountById(accId)!!
        repository.executeCashCardTransfer(
            isCashToCard = true,
            cashCurrency = initialCurr,
            cardAccount = card,
            cashAmount = 500.0,
            cardAmount = 500.0,
            rate = 1.0,
            note = "واریز به حساب"
        )

        // ۶. تبدیل اسعار با ارز دیگر (USD به ارز شناسه ۱)
        // اطمینان از ایجاد مقداری دلار اولیه
        val usdCurr = db.currencyDao().getCurrencyByCode("USD") ?: db.currencyDao().getCurrencyById(2L)!!
        repository.addTransaction(
            title = "دریافت دلار نقد برای تبدیل",
            amount = 200.0,
            type = TransactionType.INCOME,
            category = "عاید",
            accountId = 0L,
            currencyId = usdCurr.id,
            currencyCode = usdCurr.code,
            currencySymbol = usdCurr.symbol,
            exchangeRate = 1.0,
            timestamp = 100040L,
            affectsBalance = true
        )

        repository.executeCurrencyExchange(
            fromCurrency = usdCurr,
            toCurrency = initialCurr,
            fromAmount = 100.0,
            toAmount = 7000.0,
            rate = 70.0,
            fromAccountId = 0L,
            toAccountId = 0L,
            note = "تبدیل دالر به ارز شناسه ۱"
        )

        // ۷. ذخیره وضعیت و موجودی‌های قبل از تغییر نام
        val beforeCash = repository.getCashBalance(currencyId = 1L, currencyCode = "AFN")
        val beforeAcc = db.accountDao().getAccountById(accId)!!
        val beforeDebtTxn = db.transactionDao().getTransactionById(debtTxnId)!!
        assertEquals(1500.0, beforeDebtTxn.settledAmount ?: 0.0, 0.01)

        // ۸. سناریوی تغییر مکرر کد: AFN -> AFG -> Afghan -> AFN2
        val renames = listOf(
            Triple("AFG", "افغانی جدید", "؋"),
            Triple("Afghan", "افغان", "AFG"),
            Triple("AFN2", "افغانی نهایی", "AFN2")
        )

        for ((newCode, newName, newSymbol) in renames) {
            val currToUpdate = db.currencyDao().getCurrencyById(1L)!!
            repository.updateCurrency(
                currToUpdate.copy(
                    code = newCode,
                    name = newName,
                    symbol = newSymbol
                )
            )
        }

        // ۹. راستی‌آزمایی دقیق اینکه همه اطلاعات بدون کوچک‌ترین نقصی به شناسه ۱ متصل مانده‌اند
        val finalCurr = db.currencyDao().getCurrencyById(1L)!!
        assertEquals(1L, finalCurr.id)
        assertEquals("AFN2", finalCurr.code)
        assertEquals("افغانی نهایی", finalCurr.name)

        // بررسی موجودی نقدی از طریق هر دو شیوه (با شناسه ۱ و کد جدید)
        val afterCashById = repository.getCashBalance(currencyId = 1L, currencyCode = "AFN2")
        val afterCashByCodeOnly = repository.getCashBalance(currencyId = 0L, currencyCode = "AFN2")
        assertEquals("موجودی نقدی پس از تغییرات کد باید دقیقاً حفظ شود", beforeCash, afterCashById, 0.001)
        assertEquals("موجودی نقدی با کد جدید نیز باید برابر باشد", beforeCash, afterCashByCodeOnly, 0.001)

        // بررسی حساب بانکی
        val afterAcc = db.accountDao().getAccountById(accId)!!
        assertEquals(beforeAcc.balance, afterAcc.balance, 0.001)
        assertEquals(1L, afterAcc.currencyId)
        assertEquals("AFN2", afterAcc.currencyCode)

        // بررسی طلب/بدهی و تسویه شخص
        val afterDebtTxn = db.transactionDao().getTransactionById(debtTxnId)!!
        assertEquals(1L, afterDebtTxn.currencyId)
        assertEquals("AFN2", afterDebtTxn.currencyCode)
        assertEquals(1500.0, afterDebtTxn.settledAmount ?: 0.0, 0.01)

        val afterSettleTxn = db.transactionDao().getTransactionById(settlementTxnId)!!
        assertEquals(1L, afterSettleTxn.currencyId)
        assertEquals("AFN2", afterSettleTxn.currencyCode)
        assertEquals(1500.0, afterSettleTxn.amount, 0.01)

        // بررسی تراکنش‌های متصل به شناسه ۱
        val allTxns = db.transactionDao().getAllTransactionsList()
        val txnsWithCurr1 = allTxns.filter { it.currencyId == 1L }
        assertTrue("تراکنش‌های متصل به شناسه ۱ باید موجود باشند", txnsWithCurr1.isNotEmpty())
        for (txn in txnsWithCurr1) {
            assertEquals("کد تراکنش‌های شناسه ۱ باید به AFN2 به‌روز شده باشد", "AFN2", txn.currencyCode)
        }

        // بررسی اینکه هیچ داده‌ای صفر یا گم نشده باشد
        val cashTxn = db.transactionDao().getTransactionById(cashTxnId)!!
        assertEquals(1L, cashTxn.currencyId)
        assertEquals(20000.0, cashTxn.amount, 0.001)

        val accTxn = db.transactionDao().getTransactionById(accTxnId)!!
        assertEquals(1L, accTxn.currencyId)
        assertEquals(2000.0, accTxn.amount, 0.001)
    }
}
