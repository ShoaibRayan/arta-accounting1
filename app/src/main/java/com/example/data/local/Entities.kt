package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class TransactionKind {
    NORMAL,
    TRANSFER,
    CURRENCY_EXCHANGE,
    PERSON_PAYMENT,
    PERSON_RECEIVE,
    PERSON_TRANSFER,
    PERSON_CURRENCY_EXCHANGE,
    PERSON_SETTLEMENT,
    GOAL_DEPOSIT,
    GOAL_WITHDRAW,
    OPENING_BALANCE,
    BALANCE_ADJUSTMENT
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["recipientId"]),
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["currencyCode"]),
        Index(value = ["currencyId"]),
        Index(value = ["ledgerCurrencyId"]),
        Index(value = ["timestamp"]),
        Index(value = ["relatedTransactionId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long? = null,
    val category: String,
    val accountId: Long = 0,
    val recipientName: String? = null,
    val recipientId: Long? = null,
    val kind: TransactionKind = TransactionKind.NORMAL,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val calculationExpression: String? = null,
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN",
    val currencySymbol: String = "؋",
    val exchangeRate: Double = 1.0,
    val ledgerCurrencyId: Long? = null,
    val ledgerCurrencyCode: String? = null,
    val ledgerCurrencySymbol: String? = null,
    val ledgerAmount: Double? = null,
    val relatedTransactionId: Long? = null,
    val originalTransactionId: Long? = null,
    val settledAmount: Double = 0.0,
    val sourceCurrencyId: Long? = null,
    val sourceCurrencyCode: String? = null,
    val sourceCurrencySymbol: String? = null,
    val sourceAmount: Double? = null,
    val targetCurrencyId: Long? = null,
    val targetCurrencyCode: String? = null,
    val targetCurrencySymbol: String? = null,
    val targetAmount: Double? = null,
    val conversionRate: Double? = null,
    val historicalBaseCurrencyCode: String = "AFN",
    val historicalExchangeRateToBase: Double = 1.0,
    val dueDate: Long? = null,
    val isSettled: Boolean = false,
    val affectsBalance: Boolean = true
) {
    val remainingAmount: Double
        get() = maxOf(0.0, amount - settledAmount)
}

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. افغانی, دالر آمریکایی, یورو
    val code: String, // e.g. AFN, USD, EUR, IRR, PKR
    val symbol: String, // e.g. ؋, $, €, تومان, ₨
    val flagEmoji: String = "🇦🇫",
    val colorHex: Long = 0xFF10B981,
    val exchangeRateToBase: Double = 1.0, // relative to AFN (e.g. 1 USD = 70 AFN)
    val isActive: Boolean = true,
    val isBaseCurrency: Boolean = false,
    val decimalPlaces: Int = 3 // 0 for AFN/IRR/PKR, 3 for currencies with decimals like USD/EUR
)

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["currencyId"])
    ]
)
data class AccountCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val cardNumberMasked: String, // e.g. "•••• 7642"
    val cardHolder: String = "M. SHOAIB RAYAN",
    val expiry: String = "08/28",
    val balance: Double,
    val cardColorTheme: String = "dark", // "dark", "silver", "teal", "gold"
    val isFrozen: Boolean = false,
    val isDefault: Boolean = false,
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN",
    val currencySymbol: String = "؋"
)

@Entity(tableName = "recipients")
data class RecipientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val handleOrPhone: String,
    val avatarColorHex: Long = 0xFF21C6D8,
    val isFavorite: Boolean = false,
    val transactionCount: Int = 0,
    val iconName: String = "Person",
    val isActive: Boolean = true,
    val notes: String = "",
    val isAmountProtected: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long? = null,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String = "2026-09", // e.g. "2026-09"
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g. "خوراک و غذا", "خرید", "کرایه خانه", "معاش"
    val type: TransactionType = TransactionType.EXPENSE, // EXPENSE or INCOME
    val iconName: String = "ShoppingBag", // icon identifier
    val colorHex: Long = 0xFF3B82F6,
    val isActive: Boolean = true,
    val isDefault: Boolean = false
)

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

enum class GoalReminderFrequency {
    NONE,
    WEEKLY,
    MONTHLY,
    SPECIFIC_DATE
}

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN",
    val currencySymbol: String = "؋",
    val iconName: String = "Savings", // e.g. "Home", "Car", "Motorcycle", "Phone", "Flight", "Baby", "Debt", "Shield", "Savings", "Education", "Custom"
    val colorHex: Long = 0xFF10B981, // Theme color for goal card
    val category: String = "پس‌انداز عمومی", // "خرید", "خانه", "وسیله نقلیه", "سفر", "خانواده", "آموزش", "اضطراری", "پرداخت قرض", "پس‌انداز عمومی", "سفارشی"
    val startDate: Long = System.currentTimeMillis(),
    val targetDate: Long? = null, // Optional deadline
    val linkedAccountId: Long? = null, // Optional linked account / wallet
    val status: GoalStatus = GoalStatus.ACTIVE,
    val completedDate: Long? = null,
    val reminderFrequency: GoalReminderFrequency = GoalReminderFrequency.NONE,
    val reminderDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class GoalTransactionType {
    DEPOSIT,    // افزودن پول به هدف
    WITHDRAWAL  // برداشت پول از هدف
}

@Entity(tableName = "goal_transactions")
data class GoalTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN",
    val currencySymbol: String = "؋",
    val type: GoalTransactionType,
    val accountId: Long? = null,
    val accountName: String? = null,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val linkedTransactionId: Long? = null
)

enum class QuickActionType {
    EXPENSE,      // مصرف / هزینه
    INCOME,       // عواید / درآمد
    RECEIVE,      // دریافت از شخص
    PAY,          // پرداخت به شخص
    TRANSFER,     // انتقال بین کیف‌پول‌ها / کارت‌ها
    GOAL_DEPOSIT  // واریز به هدف مالی
}

enum class QuickActionAmountBehavior {
    FIXED,     // مبلغ ثابت
    VARIABLE,  // مبلغ قابل تغییر (دارای مبلغ پیش‌فرض)
    EMPTY      // مبلغ خالی
}

@Entity(tableName = "quick_actions")
data class QuickActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                               // نام میانبر (مثلاً: خرید نان، کرایه خانه، معاش...)
    val iconName: String = "ShoppingBag",             // شناسه آیکون یا ایموجی
    val actionType: QuickActionType = QuickActionType.EXPENSE,
    val amountBehavior: QuickActionAmountBehavior = QuickActionAmountBehavior.FIXED,
    val defaultAmount: Double = 0.0,                  // مبلغ پیش‌فرض
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN",
    val currencySymbol: String = "؋",
    val sourceAccountId: Long = 0,                   // 0 = پول نقد، >0 = کارت بانکی
    val destinationAccountId: Long? = null,          // حساب مقصد برای انتقال
    val categoryId: Long? = null,                    // شناسه دسته‌بندی
    val categoryName: String = "عمومی",              // دسته‌بندی
    val recipientId: Long? = null,                   // شناسه شخص
    val recipientName: String? = null,               // نام شخص
    val targetGoalId: Long? = null,                  // شناسه هدف مالی
    val targetGoalTitle: String? = null,             // عنوان هدف مالی
    val note: String? = null,                        // یادداشت
    val showOnHome: Boolean = true,                  // نمایش در صفحه اصلی
    val displayOrder: Int = 0,                       // ترتیب نمایش
    val colorHex: Long = 0xFF3B82F6,                 // رنگ کارت
    val usageCount: Int = 0,                         // تعداد دفعات استفاده
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                               // نام / عنوان لیست خرید
    val currencyId: Long = 0L,
    val currencyCode: String = "AFN",
    val currencySymbol: String = "؋",
    val isCompleted: Boolean = false,                // آیا تمام اقلام خریداری شده است
    val isLoggedAsExpense: Boolean = false,          // آیا به عنوان مصرف در سیستم مالی ثبت شده است
    val linkedTransactionId: Long? = null,           // شناسه تراکنش مصرف ثبت شده
    val accountId: Long? = null,                     // حساب/کارت پرداختی (null یا 0 = بیلانس کل)
    val accountName: String? = null,                 // نام کارت یا "بیلانس کل"
    val categoryId: Long? = null,                    // شناسه دسته‌بندی مصرف
    val category: String = "خرید روزمره",            // دسته‌بندی مصرف
    val totalAmount: Double = 0.0,                   // جمله نهایی لیست خرید
    val useItemizedSum: Boolean = true,              // محاسبه از جمع اقلام یا قیمت مقطوع کلی
    val manualTotalAmount: Double? = null,           // قیمت کلی دستی (در صورتی که جدا جدا قیمت زده نشده باشد)
    val createdAt: Long = System.currentTimeMillis(),
    val purchaseDate: Long = System.currentTimeMillis(), // تاریخ خرید / ثبت مصرف
    val note: String? = null
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long,                                // شناسه لیست خرید مربوطه
    val title: String,                               // نام جنس / کالا (مثلاً: روغن، برنج، شکر)
    val quantity: String = "",                       // مقدار یا تعداد اختیاری (مثلاً: ۵ کیلو، ۲ کارتن)
    val price: Double = 0.0,                         // قیمت مجموع این قلم به صورت جداگانه (مقدار ضرب در فی)
    val unitPrice: Double = 0.0,                     // قیمت فی هر واحد کالا (اختیاری)
    val isPurchased: Boolean = false,                // آیا این قلم خریده شده است
    val displayOrder: Int = 0
)

data class ShoppingListWithItems(
    val list: ShoppingListEntity,
    val items: List<ShoppingListItemEntity>
) {
    val itemsSum: Double get() = items.sumOf { it.price }
    val effectiveTotal: Double get() = if (!list.useItemizedSum && (list.manualTotalAmount ?: 0.0) > 0.0) {
        list.manualTotalAmount ?: 0.0
    } else {
        itemsSum
    }
    val purchasedCount: Int get() = items.count { it.isPurchased }
    val totalCount: Int get() = items.size
    val isFullyPurchased: Boolean get() = totalCount > 0 && purchasedCount == totalCount
}



