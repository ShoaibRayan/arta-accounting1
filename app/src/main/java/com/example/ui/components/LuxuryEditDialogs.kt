package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import com.example.data.local.ShoppingListWithItems
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionKind
import com.example.data.local.TransactionType
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.CardDarkGradientEnd
import com.example.ui.theme.CardDarkGradientStart
import com.example.ui.theme.CardGoldGradientEnd
import com.example.ui.theme.CardGoldGradientStart
import com.example.ui.theme.CardTealGradientEnd
import com.example.ui.theme.CardTealGradientStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.mutableLongStateOf
import com.example.data.local.CategoryEntity
import com.example.util.PersianDateHelper
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatExchangeRateText(rate: Double, fromCode: String, toCode: String): String {
    if (rate <= 0.0) return ""
    return if (rate >= 1.0) {
        val rateStr = if (rate % 1.0 == 0.0) String.format(Locale.US, "%,.0f", rate)
        else if (rate >= 100) String.format(Locale.US, "%,.2f", rate).trimEnd('0').trimEnd('.')
        else String.format(Locale.US, "%.4f", rate).trimEnd('0').trimEnd('.')
        "۱ $fromCode = $rateStr $toCode"
    } else {
        val inv = 1.0 / rate
        val invStr = if (inv % 1.0 == 0.0) String.format(Locale.US, "%,.0f", inv)
        else if (inv >= 100) String.format(Locale.US, "%,.2f", inv).trimEnd('0').trimEnd('.')
        else String.format(Locale.US, "%.4f", inv).trimEnd('0').trimEnd('.')
        "۱ $toCode = $invStr $fromCode"
    }
}

/**
 * پاکسازی یادداشت معامله از هرگونه متن تولیدشده خودکار (مانند نرخ تبادله، معادل، پرداخت/دریافت)
 * تا فقط متن واقعی و شخصی کاربر باقی بماند.
 */
fun cleanTransactionNote(rawNote: String?): String? {
    if (rawNote.isNullOrBlank()) return null
    var text: String = rawNote
    val patterns = listOf(
        Regex("""(?:\s*[-–]?\s*(?:دریافت|پرداخت)\s+.*?(?:معادل)\s+[\d,.]*\s*[^\s()]*(?:\s*\(نرخ تبادله:[^)]*\))?)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*معادل\s+[\d,.]*\s*[^\s()]*(?:\s*\(نرخ تبادله:[^)]*\))?)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–(]?\s*نرخ تبادله:[^)]*\)?)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*تبدیل با نرخ\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*دریافت معادل با نرخ\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*معادل تبدیل شده از\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*انتقال با نرخ\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*دریافت انتقال با نرخ\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*واریز به کارت از نقد.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*واریز از موجودی نقد.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*برداشت از کارت به نقد.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*دریافت از کارت.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*انتقال بین حسابات.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*حواله به\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*حواله از\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*دریافت سریع از\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*پرداخت سریع به\s+.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*انتقال سریع.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*واریز سریع.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*برداشت سریع.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*تخصیص اولیه بودجه.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*مبلغ اولیه ثبت شده.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*واریز به پس‌انداز هدف.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*برداشت از پس‌انداز هدف.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*برداشت و انتقال به حساب.*)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*[-–]?\s*لیست خرید:\s*[^|]+(?:\s*\|\s*اقلام:[^–\n-]*)?)""", RegexOption.DOT_MATCHES_ALL),
        Regex("""(?:\s*\[لیست خرید #\d+\])""")
    )
    for (p in patterns) {
        text = text.replace(p, "")
    }
    val cleaned = text.trim().trimEnd('-', '–').trim()
    return cleaned.ifBlank { null }
}

/**
 * ساخت خودکار و پویای شرح معامله در صفحه جزئیات از روی اطلاعات ساختاریافته دیتابیس
 * (شامل نرخ ارز، ارز محاسبه‌شده، طرف حساب و نوع تراکنش)
 */
fun buildTransactionDynamicDescription(
    transaction: TransactionEntity,
    baseCurrency: CurrencyEntity? = null
): String? {
    val isIncome = transaction.type == TransactionType.INCOME
    val hasRecipient = !transaction.recipientName.isNullOrBlank()
    val hasLedger = !transaction.ledgerCurrencyCode.isNullOrBlank() &&
            transaction.ledgerAmount != null && transaction.ledgerAmount > 0.0
    val hasRate = transaction.exchangeRate > 0.0 && transaction.exchangeRate != 1.0

    val amtStr = "${transaction.currencySymbol} ${formatAmountDisplay(transaction.amount)}"

    if (hasRecipient && hasLedger) {
        val ledgerAmtStr = "${formatAmountDisplay(transaction.ledgerAmount!!)} ${transaction.ledgerCurrencySymbol ?: transaction.ledgerCurrencyCode}"
        val rateStr = formatExchangeRateText(transaction.exchangeRate, transaction.currencyCode, transaction.ledgerCurrencyCode!!)
        val actionWord = if (isIncome) "دریافت" else "پرداخت"
        val prepWord = if (isIncome) "از" else "به"
        return if (rateStr.isNotBlank()) {
            "$actionWord $amtStr $prepWord ${transaction.recipientName} معادل $ledgerAmtStr (نرخ تبادله: $rateStr)"
        } else {
            "$actionWord $amtStr $prepWord ${transaction.recipientName} معادل $ledgerAmtStr"
        }
    } else if (hasRecipient) {
        val actionWord = if (isIncome) "دریافت" else "پرداخت"
        val prepWord = if (isIncome) "از" else "به"
        val basePart = if (hasRate && baseCurrency != null && !transaction.currencyCode.equals(baseCurrency.code, ignoreCase = true)) {
            val equiv = transaction.amount * transaction.exchangeRate
            val equivStr = "${formatAmountDisplay(equiv)} ${baseCurrency.symbol}"
            val rateStr = formatExchangeRateText(transaction.exchangeRate, transaction.currencyCode, baseCurrency.code)
            if (rateStr.isNotBlank()) " معادل $equivStr (نرخ تبادله: $rateStr)" else " معادل $equivStr"
        } else ""
        return "$actionWord $amtStr $prepWord ${transaction.recipientName}$basePart"
    } else if (hasRate) {
        val targetCode = baseCurrency?.code ?: "ارز پایه"
        val targetSymbol = baseCurrency?.symbol ?: ""
        val equiv = if (baseCurrency != null) {
            val v = transaction.amount * transaction.exchangeRate
            "معادل ${formatAmountDisplay(v)} $targetSymbol "
        } else ""
        val rateStr = formatExchangeRateText(transaction.exchangeRate, transaction.currencyCode, targetCode)
        return if (rateStr.isNotBlank()) "$equiv(نرخ تبادله: $rateStr)" else equiv.ifBlank { null }
    }
    return null
}

/**
 * Luxury BottomSheet for Viewing & Editing any Transaction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxuryTransactionDetailAndEditBottomSheet(
    transaction: TransactionEntity,
    activeCurrencies: List<CurrencyEntity>,
    accounts: List<AccountCardEntity>,
    categories: List<CategoryEntity> = emptyList(),
    shoppingLists: List<ShoppingListWithItems> = emptyList(),
    isAmountMasked: Boolean = false,
    onDismiss: () -> Unit,
    onUpdate: (oldTxn: TransactionEntity, newTxn: TransactionEntity, onResult: (Boolean, String?) -> Unit) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onEditTransfer: ((TransactionEntity) -> Unit)? = null,
    onEditExchange: ((TransactionEntity) -> Unit)? = null,
    onEditGoal: ((TransactionEntity) -> Unit)? = null,
    onCreateQuickAction: ((TransactionEntity) -> Unit)? = null,
    onOpenShoppingList: ((Long) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var generalEditError by remember { mutableStateOf<String?>(null) }

    val linkedShoppingList = remember(transaction, shoppingLists) {
        shoppingLists.find { it.list.linkedTransactionId == transaction.id }
            ?: shoppingLists.find {
                transaction.title.equals("خرید: ${it.list.title}", ignoreCase = true) && it.list.isLoggedAsExpense
            }
    }
    val shoppingListId = linkedShoppingList?.list?.id ?: remember(transaction) {
        val regex = Regex("""\[لیست خرید #(\d+)\]""")
        val match = regex.find(transaction.note ?: "")
        match?.groupValues?.get(1)?.toLongOrNull()
    }
    val isShoppingListTxn = linkedShoppingList != null || shoppingListId != null

    // Edit fields
    var editTitle by remember(transaction) { mutableStateOf(transaction.title) }
    var editAmountText by remember(transaction) {
        mutableStateOf(
            if (transaction.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", transaction.amount)
            else String.format(Locale.US, "%.3f", transaction.amount).trimEnd('0').trimEnd('.')
        )
    }
    var editType by remember(transaction) { mutableStateOf(transaction.type) }
    var editCategoryId by remember(transaction) { mutableStateOf(transaction.categoryId) }
    var editCategory by remember(transaction) { mutableStateOf(transaction.category) }
    var editCurrencyId by remember(transaction) { mutableStateOf(transaction.currencyId) }
    var editCurrencyCode by remember(transaction) { mutableStateOf(transaction.currencyCode) }
    var editCurrencySymbol by remember(transaction) { mutableStateOf(transaction.currencySymbol) }
    var editNote by remember(transaction) { mutableStateOf(cleanTransactionNote(transaction.note) ?: "") }
    var editAccountId by remember(transaction) { mutableStateOf(transaction.accountId) }
    var editTimestamp by remember(transaction) { mutableLongStateOf(transaction.timestamp) }
    var editDueDate by remember(transaction) { mutableStateOf<Long?>(transaction.dueDate) }
    var editAffectsBalance by remember(transaction) { mutableStateOf(transaction.affectsBalance) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showDueDatePickerDialog by remember { mutableStateOf(false) }
    var showCalculatorForAmount by remember { mutableStateOf(false) }

    val isExchangeTxn = transaction.category == "Exchange" ||
        transaction.title.startsWith("تبدیل به") ||
        transaction.title.startsWith("دریافت از تبدیل") ||
        transaction.title.contains("تبدیل اسعار")

    val isGoalTxn = !isExchangeTxn && (
        transaction.kind == TransactionKind.GOAL_DEPOSIT ||
        transaction.kind == TransactionKind.GOAL_WITHDRAW ||
        transaction.category == "واریز به هدف" ||
        transaction.category == "برداشت از هدف" ||
        transaction.title.startsWith("انتقال به هدف") ||
        transaction.title.startsWith("انتقال از هدف") ||
        (transaction.category.contains("هدف") && (transaction.title.contains("هدف") || transaction.note?.contains("هدف") == true))
    )

    val isGoalDeposit = isGoalTxn && (
        transaction.kind == TransactionKind.GOAL_DEPOSIT ||
        transaction.category == "واریز به هدف" ||
        transaction.title.startsWith("واریز به هدف") ||
        transaction.title.startsWith("انتقال به هدف") ||
        transaction.type == TransactionType.EXPENSE
    )
    val isGoalWithdraw = isGoalTxn && !isGoalDeposit

    val goalName = remember(transaction) {
        val title = transaction.title
        val raw = when {
            title.startsWith("واریز به هدف:") -> title.substringAfter("واریز به هدف:")
            title.startsWith("برداشت از هدف:") -> title.substringAfter("برداشت از هدف:")
            title.startsWith("انتقال به هدف:") -> title.substringAfter("انتقال به هدف:")
            title.startsWith("انتقال از هدف:") -> title.substringAfter("انتقال از هدف:")
            title.contains("هدف:") -> title.substringAfter("هدف:")
            else -> ""
        }.trim()
        if (raw.isNotBlank()) raw else (transaction.note?.takeIf { it.isNotBlank() } ?: "هدف مالی")
    }

    val isTransferTxn = !isExchangeTxn && !isGoalTxn && (
        transaction.category == "انتقالات" ||
        transaction.kind == TransactionKind.TRANSFER ||
        transaction.type == TransactionType.TRANSFER ||
        transaction.relatedTransactionId != null
    )

    val initialHasCalculated = !transaction.ledgerCurrencyCode.isNullOrBlank() &&
            !transaction.ledgerCurrencyCode.equals(transaction.currencyCode, ignoreCase = true) &&
            (transaction.ledgerAmount != null && transaction.ledgerAmount > 0.0)

    var editHasCalculatedCurrency by remember(transaction) { mutableStateOf(initialHasCalculated) }

    var editLedgerCurrencyId by remember(transaction) { mutableStateOf(transaction.ledgerCurrencyId) }

    var editLedgerCurrencyCode by remember(transaction) {
        mutableStateOf(
            if (initialHasCalculated) transaction.ledgerCurrencyCode!!
            else (activeCurrencies.firstOrNull { !it.code.equals(transaction.currencyCode, ignoreCase = true) }?.code ?: "USD")
        )
    }
    var editLedgerCurrencySymbol by remember(transaction) {
        mutableStateOf(
            if (initialHasCalculated && !transaction.ledgerCurrencySymbol.isNullOrBlank()) transaction.ledgerCurrencySymbol!!
            else (activeCurrencies.firstOrNull { it.code.equals(editLedgerCurrencyCode, ignoreCase = true) }?.symbol ?: "$")
        )
    }
    var editExchangeRateText by remember(transaction) {
        if (initialHasCalculated) {
            val r = transaction.exchangeRate
            mutableStateOf(
                if (r > 0.0 && r != 1.0) {
                    if (r % 1.0 == 0.0) String.format(Locale.US, "%.0f", r)
                    else String.format(Locale.US, "%.6f", r).trimEnd('0').trimEnd('.')
                } else if (transaction.ledgerAmount != null && transaction.ledgerAmount > 0.0 && transaction.amount > 0.0) {
                    val derived = transaction.ledgerAmount / transaction.amount
                    if (derived % 1.0 == 0.0) String.format(Locale.US, "%.0f", derived)
                    else String.format(Locale.US, "%.6f", derived).trimEnd('0').trimEnd('.')
                } else "1"
            )
        } else {
            mutableStateOf("1")
        }
    }
    var editLedgerAmountText by remember(transaction) {
        if (initialHasCalculated) {
            val la = transaction.ledgerAmount
            mutableStateOf(
                if (la != null && la > 0.0) {
                    if (la % 1.0 == 0.0) String.format(Locale.US, "%.0f", la)
                    else String.format(Locale.US, "%.3f", la).trimEnd('0').trimEnd('.')
                } else ""
            )
        } else {
            mutableStateOf("")
        }
    }

    var showCalculatorForRate by remember { mutableStateOf(false) }
    var showCalculatorForLedgerAmount by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var exchangeRateError by remember { mutableStateOf<String?>(null) }
    var ledgerAmountError by remember { mutableStateOf<String?>(null) }
    var lockedNoticeMessage by remember { mutableStateOf<String?>(null) }

    val defaultExpenseCategoryNames = listOf(
        "خوراک و غذا", "خرید و پوشاک", "کرایه و خانه", "ترانسپورت و سفر",
        "تلفن و اینترنت", "اشتراک و نرم‌افزار", "صحت و درمان", "مصارف عمومی"
    )
    val defaultIncomeCategoryNames = listOf(
        "معاش و حقوق", "فروش و درآمد", "سرمایه‌گذاری", "هدیه و پاداش", "عاید عمومی"
    )
    val availableCategories = remember(categories, editType) {
        when (editType) {
            TransactionType.EXPENSE -> categories.filter { it.type == TransactionType.EXPENSE && it.isActive }
            TransactionType.INCOME -> categories.filter { it.type == TransactionType.INCOME && it.isActive }
            else -> categories.filter { it.isActive }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = BentoNavyDark,
        unfocusedTextColor = BentoNavyDark,
        cursorColor = BentoNavyDark,
        focusedBorderColor = BentoNavyDark,
        unfocusedBorderColor = BentoBorder,
        focusedContainerColor = SurfaceWhite,
        unfocusedContainerColor = SurfaceWhite,
        focusedLabelColor = BentoNavyDark,
        unfocusedLabelColor = TextSecondary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
        modifier = Modifier.testTag("luxury_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Drag handle pill at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .background(BentoBorder.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
            // Drag handle pill
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE2E8F0))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Row: Title, Mode toggle, and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isEditMode) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Edit else Icons.Default.Payments,
                            contentDescription = null,
                            tint = BentoNavyDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEditMode) "ویرایش معامله" else "جزئیات معامله",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Text(
                            text = if (isEditMode) "مشخصات معامله را تغییر دهید" else "اطلاعات ثبت شده معامله",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Edit / View toggle button (or Open Shopping List if linked)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isShoppingListTxn) BentoLavenderSubtle else if (isEditMode) BentoNavyDark else Color(0xFFF1F5F9),
                        border = if (isShoppingListTxn) androidx.compose.foundation.BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (isExchangeTxn && onEditExchange != null) {
                                    onEditExchange(transaction)
                                    onDismiss()
                                } else if (isTransferTxn && onEditTransfer != null) {
                                    onEditTransfer(transaction)
                                    onDismiss()
                                } else if (isGoalTxn && onEditGoal != null) {
                                    onEditGoal(transaction)
                                    onDismiss()
                                } else if (isShoppingListTxn && shoppingListId != null && onOpenShoppingList != null) {
                                    onOpenShoppingList(shoppingListId)
                                    onDismiss()
                                } else if (isShoppingListTxn) {
                                    // Direct edit is not permitted for shopping list transactions
                                } else if (isAmountMasked) {
                                    lockedNoticeMessage = "این معامله به دلیل فعال بودن حریم خصوصی شخص قفل است. لطفاً ابتدا از بخش اشخاص، قفل حساب را باز کنید."
                                } else {
                                    isEditMode = !isEditMode
                                }
                            }
                            .testTag("toggle_edit_transaction_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val btnIcon = if (isShoppingListTxn) Icons.Default.ShoppingCart else if (isEditMode) Icons.Default.Check else Icons.Default.Edit
                            val btnTint = if (isShoppingListTxn) BentoIndigoAccent else if (isEditMode) Color.White else BentoNavyDark
                            Icon(
                                imageVector = btnIcon,
                                contentDescription = null,
                                tint = btnTint,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val btnText = if (isShoppingListTxn) "ویرایش در لیست خرید" else if (isEditMode) "حالت نمایش" else "ویرایش"
                            Text(
                                text = btnText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = btnTint
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isEditMode) {
                // ==================== VIEW MODE ====================
                // Prominent Luxury Amount Banner
                val isIncome = transaction.type == TransactionType.INCOME
                val amountPrefix = when {
                    isGoalDeposit -> "-"
                    isGoalWithdraw -> "+"
                    isIncome -> "+"
                    else -> "-"
                }
                val bannerBg = when {
                    isGoalDeposit -> Color(0xFFF5F3FF)
                    isGoalWithdraw -> Color(0xFFFFFBEB)
                    isIncome -> IncomeGreenBg
                    else -> ExpenseRedBg
                }
                val bannerBorder = when {
                    isGoalDeposit -> Color(0xFFDDD6FE)
                    isGoalWithdraw -> Color(0xFFFDE68A)
                    isIncome -> Color(0xFFA7F3D0)
                    else -> Color(0xFFFECDD3)
                }
                val bannerColor = when {
                    isGoalDeposit -> Color(0xFF7C3AED)
                    isGoalWithdraw -> Color(0xFFD97706)
                    isIncome -> IncomeGreen
                    else -> ExpenseRed
                }
                val sym = transaction.currencySymbol.ifBlank { "؋" }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, bannerBorder)
                ) {
                    val hasRecipientHeader = !transaction.recipientName.isNullOrBlank()
                    val headerTypeLabel = when {
                        isGoalDeposit -> "واریز به هدف مالی (پس‌انداز)"
                        isGoalWithdraw -> "برداشت از پس‌انداز هدف"
                        hasRecipientHeader -> if (isIncome) "دریافت" else "پرداخت"
                        else -> if (isIncome) "عاید" else "مصرف"
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = headerTypeLabel,
                            color = bannerColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isAmountMasked) {
                                    Text(
                                        text = sym,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoIndigoAccent
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "••••••",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoIndigoAccent
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "محافظت شده",
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                } else {
                                    Text(
                                        text = sym,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bannerColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$amountPrefix${formatAmountDisplay(transaction.amount)}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = bannerColor
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isAmountMasked) "مبلغ این معامله به دلیل محافظت از طرف حساب مخفی است" else "واحد پولی: ${transaction.currencyCode}",
                            fontSize = 11.sp,
                            color = if (isAmountMasked) BentoIndigoAccent else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If this is a Goal transaction, show full schematic flow card (like transfers & cash/card)
                if (isGoalTxn) {
                    val matchedAccount = accounts.firstOrNull { it.id == transaction.accountId }
                    val acctName = if (transaction.accountId == 0L) "صندوق نقد اصلی" else (matchedAccount?.name ?: "کارت بانکی")

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isGoalDeposit) {
                                // Source: Cash / Account
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ExpenseRed))
                                        Column {
                                            Text(text = "مبدأ (برداشت وجه):", fontSize = 11.sp, color = TextSecondary)
                                            Text(text = acctName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                        }
                                    }
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isAmountMasked) {
                                                Text(text = "••••••", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoIndigoAccent)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = BentoIndigoAccent)
                                            } else {
                                                Text(text = "-${formatAmountDisplay(transaction.amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = ExpenseRed)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = BentoBorder)
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp).size(26.dp).clip(CircleShape).background(Color(0xFFF5F3FF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Savings, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(15.dp))
                                    }
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = BentoBorder)
                                }

                                // Target: Goal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                                        Column {
                                            Text(text = "مقصد (افزایش اندوخته):", fontSize = 11.sp, color = TextSecondary)
                                            Text(text = "هدف مالی: $goalName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                        }
                                    }
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isAmountMasked) {
                                                Text(text = "••••••", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoIndigoAccent)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = BentoIndigoAccent)
                                            } else {
                                                Text(text = "+${formatAmountDisplay(transaction.amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = Color(0xFF7C3AED))
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Goal Withdraw
                                // Source: Goal
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFD97706)))
                                        Column {
                                            Text(text = "مبدأ (برداشت از هدف):", fontSize = 11.sp, color = TextSecondary)
                                            Text(text = "هدف مالی: $goalName", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                        }
                                    }
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isAmountMasked) {
                                                Text(text = "••••••", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoIndigoAccent)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = BentoIndigoAccent)
                                            } else {
                                                Text(text = "-${formatAmountDisplay(transaction.amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = Color(0xFFD97706))
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = BentoBorder)
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp).size(26.dp).clip(CircleShape).background(Color(0xFFF0FDF4)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(15.dp))
                                    }
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = BentoBorder)
                                }

                                // Target: Cash / Account
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(IncomeGreen))
                                        Column {
                                            Text(text = "مقصد (واریز به حساب):", fontSize = 11.sp, color = TextSecondary)
                                            Text(text = acctName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                        }
                                    }
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isAmountMasked) {
                                                Text(text = "••••••", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoIndigoAccent)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = BentoIndigoAccent)
                                            } else {
                                                Text(text = "+${formatAmountDisplay(transaction.amount)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = transaction.currencySymbol, fontSize = 13.sp, color = IncomeGreen)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // If this transaction is linked to a shopping list, show prominent action card with link
                if (isShoppingListTxn && shoppingListId != null) {
                    val listTitle = linkedShoppingList?.list?.title ?: transaction.title.removePrefix("خرید: ")
                    val items = linkedShoppingList?.items ?: emptyList()
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BentoLavenderSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoIndigoAccent.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOpenShoppingList?.invoke(shoppingListId)
                                onDismiss()
                            }
                            .testTag("luxury_linked_shopping_list_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(BentoIndigoAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "مربوط به لیست خرید «$listTitle»",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = if (items.isNotEmpty()) "مشاهده ${items.size} قلم کالا و ویرایش اقلام در لیست خرید" else "مشاهده اقلام، بررسی قیمت‌ها و ویرایش در لیست خرید",
                                        fontSize = 11.5.sp,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BentoIndigoAccent,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "مشاهده لیست",
                                            color = Color.White,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }

                            // Items preview
                            if (items.isNotEmpty()) {
                                HorizontalDivider(color = BentoBorder, thickness = 0.8.dp)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items.take(3).forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(BentoIndigoAccent)
                                                )
                                                Text(
                                                    text = item.title + (if (item.quantity.isNotBlank()) " (${item.quantity})" else ""),
                                                    fontSize = 12.sp,
                                                    color = BentoNavyDark,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            if (item.price > 0.0) {
                                                val priceFormatted = if (item.price % 1.0 == 0.0) item.price.toLong().toString() else item.price.toString()
                                                Text(
                                                    text = "${transaction.currencySymbol} $priceFormatted",
                                                    fontSize = 11.5.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                    if (items.size > 3) {
                                        Text(
                                            text = "+ ${items.size - 3} قلم دیگر...",
                                            fontSize = 11.sp,
                                            color = BentoIndigoAccent,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Instruction notice that editing is done in the shopping list
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.8f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "امکان ویرایش مستقیم این تراکنش وجود ندارد و باید از قسمت خرید ویرایش شود.",
                                        fontSize = 10.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Bento Info Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (isGoalTxn) {
                            DetailInfoRow(
                                label = "تاریخ و ساعت ثبت",
                                value = PersianDateHelper.formatSolarDateTime(transaction.timestamp)
                            )
                            val userCleanNote = cleanTransactionNote(transaction.note)
                            if (!userCleanNote.isNullOrBlank()) {
                                DetailInfoRow(label = "یادداشت", value = userCleanNote)
                            }
                        } else {
                            if (transaction.category.isNotBlank()) {
                                DetailInfoRow(
                                    label = "دسته‌بندی",
                                    value = formatCategoryDari(transaction.category)
                                )
                            }
                            transaction.recipientName?.let {
                                DetailInfoRow(label = "طرف معامله / شخص", value = it)
                            }
                            if (transaction.affectsBalance) {
                                val matchedAccount = accounts.firstOrNull { it.id == transaction.accountId }
                                DetailInfoRow(
                                    label = "کیف پول / حساب",
                                    value = if (transaction.accountId == 0L) "کیف پول اصلی" else matchedAccount?.let { "${it.name} (${it.currencySymbol})" } ?: "کیف پول اصلی"
                                )
                            }
                            DetailInfoRow(
                                label = "تأثیر بر موجودی",
                                value = if (transaction.affectsBalance) "تأثیر مستقیم بر موجودی (فعال)" else "بدون تأثیر بر موجودی (صرفاً ثبت در سوابق)"
                            )
                            transaction.dueDate?.let {
                                val remaining = PersianDateHelper.getRemainingDaysText(it)
                                val valueStr = if (transaction.isSettled) {
                                    "${PersianDateHelper.formatSolarDate(it)} (تسویه‌شده)"
                                } else if (remaining.isNotBlank()) {
                                    "${PersianDateHelper.formatSolarDate(it)} ($remaining)"
                                } else {
                                    PersianDateHelper.formatSolarDate(it)
                                }
                                DetailInfoRow(label = "تاریخ سررسید", value = valueStr)
                            }
                            DetailInfoRow(
                                label = "تاریخ و ساعت ثبت",
                                value = PersianDateHelper.formatSolarDateTime(transaction.timestamp)
                            )

                            val hasLedger = !transaction.ledgerCurrencyCode.isNullOrBlank() &&
                                    transaction.ledgerAmount != null &&
                                    transaction.ledgerAmount > 0.0 &&
                                    !transaction.ledgerCurrencyCode.equals(transaction.currencyCode, ignoreCase = true)
                            if (hasLedger) {
                                val ledgerCode = transaction.ledgerCurrencyCode!!
                                val ledgerSym = transaction.ledgerCurrencySymbol ?: ledgerCode
                                DetailInfoRow(
                                    label = "ارز و مبلغ محاسبه شده (دفترچه طرف حساب)",
                                    value = if (isAmountMasked) "$ledgerSym •••••• $ledgerCode" else "$ledgerSym ${formatAmountDisplay(transaction.ledgerAmount!!)} $ledgerCode"
                                )
                                if (!isAmountMasked && transaction.exchangeRate > 0.0 && transaction.exchangeRate != 1.0) {
                                    DetailInfoRow(
                                        label = "نرخ تبدیل ارز",
                                        value = formatExchangeRateText(transaction.exchangeRate, transaction.currencyCode, ledgerCode)
                                    )
                                }
                            } else if (!isAmountMasked && transaction.exchangeRate > 0.0 && transaction.exchangeRate != 1.0) {
                                DetailInfoRow(
                                    label = "نرخ تبادله ارز",
                                    value = formatExchangeRateText(transaction.exchangeRate, transaction.currencyCode, "ارز پایه")
                                )
                            }

                            val userCleanNote = cleanTransactionNote(transaction.note)
                            if (!userCleanNote.isNullOrBlank()) {
                                DetailInfoRow(label = "یادداشت", value = userCleanNote)
                            }
                            if (!transaction.calculationExpression.isNullOrBlank()) {
                                DetailInfoRow(label = "فرمول محاسبه", value = if (isAmountMasked) "••••••" else transaction.calculationExpression)
                            }
                        }
                    }
                }

                if (lockedNoticeMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF2F2),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = lockedNoticeMessage!!,
                                fontSize = 11.5.sp,
                                color = ExpenseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isExchangeTxn && onEditExchange != null) {
                                onEditExchange(transaction)
                                onDismiss()
                            } else if (isTransferTxn && onEditTransfer != null) {
                                onEditTransfer(transaction)
                                onDismiss()
                            } else if (isGoalTxn && onEditGoal != null) {
                                onEditGoal(transaction)
                                onDismiss()
                            } else if (isShoppingListTxn && shoppingListId != null && onOpenShoppingList != null) {
                                onOpenShoppingList(shoppingListId)
                                onDismiss()
                            } else if (isShoppingListTxn) {
                                // Direct edit not permitted for shopping list transactions
                            } else if (isAmountMasked) {
                                lockedNoticeMessage = "این معامله به دلیل فعال بودن حریم خصوصی شخص قفل است. لطفاً ابتدا از بخش اشخاص، قفل حساب را باز کنید."
                            } else {
                                isEditMode = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("luxury_edit_txn_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isShoppingListTxn) BentoIndigoAccent else BentoNavyDark
                        )
                    ) {
                        val icon = if (isShoppingListTxn) Icons.Default.ShoppingCart else Icons.Default.Edit
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val btnLabel = when {
                            isExchangeTxn -> "ویرایش تبدیل اسعار"
                            isTransferTxn -> "ویرایش انتقال (هردو حساب)"
                            isGoalTxn -> "ویرایش عملیات پس‌انداز هدف"
                            isShoppingListTxn -> "ویرایش در لیست خرید"
                            else -> "ویرایش اطلاعات"
                        }
                        Text(btnLabel, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("luxury_delete_txn_btn"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف", fontWeight = FontWeight.Bold, color = ExpenseRed)
                    }
                }

                if (onCreateQuickAction != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onCreateQuickAction(transaction)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("luxury_create_quick_action_from_txn_btn"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoIndigoAccent.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoIndigoAccent)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp), tint = BentoIndigoAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ذخیره به عنوان میانبر عملیات سریع", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BentoIndigoAccent)
                    }
                }
            } else if (isShoppingListTxn) {
                // Editing must be done from the shopping list
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BentoLavenderSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = "ویرایش از طریق لیست خرید",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BentoNavyDark
                    )
                    Text(
                        text = "این تراکنش به لیست خرید متصل است. برای ویرایش قیمت‌ها، اقلام و مشخصات، لطفاً به لیست خرید مراجعه نمایید.",
                        fontSize = 12.5.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(
                        onClick = {
                            shoppingListId?.let { onOpenShoppingList?.invoke(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاهده و ویرایش لیست خرید", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // ==================== EDIT MODE ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Type selector
                    val hasRecipient = !transaction.recipientName.isNullOrBlank()
                    val expenseLabel = if (isGoalTxn) "واریز به هدف (پس‌انداز)" else if (hasRecipient) "پرداخت" else "مصرف"
                    val incomeLabel = if (isGoalTxn) "برداشت از هدف" else if (hasRecipient) "دریافت" else "عاید"
                    val typeHeader = if (isGoalTxn) "نوع عملیات هدف مالی:" else "نوع معامله:"

                    generalEditError?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = ExpenseRedBg,
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = err,
                                    color = ExpenseRed,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(text = typeHeader, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isExp = editType == TransactionType.EXPENSE
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    val prevType = editType
                                    editType = TransactionType.EXPENSE
                                    if (isGoalTxn) {
                                        editCategory = "واریز به هدف"
                                        if (editTitle.startsWith("انتقال از هدف")) {
                                            editTitle = editTitle.replace("انتقال از هدف", "انتقال به هدف")
                                        }
                                    } else if (prevType != TransactionType.EXPENSE) {
                                        if (categories.any { it.type == TransactionType.INCOME && it.name.equals(editCategory, ignoreCase = true) } || editCategory in defaultIncomeCategoryNames) {
                                            editCategory = ""
                                            editCategoryId = null
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isExp) (if (isGoalTxn) IncomeGreen else ExpenseRed) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = expenseLabel,
                                    color = if (isExp) Color.White else BentoNavyDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        val isInc = editType == TransactionType.INCOME
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    val prevType = editType
                                    editType = TransactionType.INCOME
                                    if (isGoalTxn) {
                                        editCategory = "برداشت از هدف"
                                        if (editTitle.startsWith("انتقال به هدف")) {
                                            editTitle = editTitle.replace("انتقال به هدف", "انتقال از هدف")
                                        }
                                    } else if (prevType != TransactionType.INCOME) {
                                        if (categories.any { it.type == TransactionType.EXPENSE && it.name.equals(editCategory, ignoreCase = true) } || editCategory in defaultExpenseCategoryNames) {
                                            editCategory = ""
                                            editCategoryId = null
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isInc) (if (isGoalTxn) ExpenseRed else IncomeGreen) else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = incomeLabel,
                                    color = if (isInc) Color.White else BentoNavyDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Recipient info badge (if transaction has a recipient)
                    if (hasRecipient && !transaction.recipientName.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFEEF2FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "طرف معامله: ",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = transaction.recipientName ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            if (titleError != null) titleError = null
                        },
                        label = { Text("عنوان معامله") },
                        isError = titleError != null,
                        supportingText = titleError?.let { err ->
                            {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = err,
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_txn_title_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    // Amount & Currency
                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { newAmt ->
                            editAmountText = newAmt
                            if (amountError != null) amountError = null
                            if (editHasCalculatedCurrency) {
                                if (editLedgerCurrencyCode.equals(editCurrencyCode, ignoreCase = true)) {
                                    editLedgerAmountText = newAmt
                                    if (ledgerAmountError != null) ledgerAmountError = null
                                } else {
                                    val a = newAmt.toDoubleOrNull()
                                    val r = editExchangeRateText.toDoubleOrNull()
                                    if (a != null && a > 0.0 && r != null && r > 0.0) {
                                        val calcLedger = a * r
                                        editLedgerAmountText = if (calcLedger % 1.0 == 0.0) String.format(Locale.US, "%.0f", calcLedger)
                                        else String.format(Locale.US, "%.3f", calcLedger).trimEnd('0').trimEnd('.')
                                        if (ledgerAmountError != null) ledgerAmountError = null
                                    }
                                }
                            }
                        },
                        label = { Text("مقدار ($editCurrencySymbol)") },
                        isError = amountError != null,
                        supportingText = amountError?.let { err ->
                            {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = err,
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_txn_amount_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        trailingIcon = {
                            CalculatorMiniButton(
                                onClick = { showCalculatorForAmount = true },
                                contentDescription = "ماشین‌حساب مقدار معامله"
                            )
                        }
                    )

                    // Currency Selector Pills
                    Text(text = "واحد پولی معامله:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(activeCurrencies) { c ->
                            val isSelected = (editCurrencyId > 0 && c.id == editCurrencyId) || c.code.equals(editCurrencyCode, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        editCurrencyId = c.id
                                        editCurrencyCode = c.code
                                        editCurrencySymbol = c.symbol
                                        if (c.code.equals(editLedgerCurrencyCode, ignoreCase = true)) {
                                            // تغییر به ارز خود تراکنش: خودکار نرخ تبدیل برابر ۱ و مبالغ یکسان
                                            editExchangeRateText = "1"
                                            editLedgerAmountText = editAmountText
                                        } else if (editHasCalculatedCurrency) {
                                            val targetCurr = activeCurrencies.find { it.code.equals(editLedgerCurrencyCode, ignoreCase = true) }
                                            val toRate = targetCurr?.exchangeRateToBase ?: 1.0
                                            val fromRate = c.exchangeRateToBase
                                            val autoRate = if (toRate > 0) fromRate / toRate else 1.0
                                            val autoRateStr = if (autoRate == 1.0) "1"
                                            else if (autoRate % 1.0 == 0.0) String.format(Locale.US, "%.0f", autoRate)
                                            else String.format(Locale.US, "%.4f", autoRate).trimEnd('0').trimEnd('.')
                                            editExchangeRateText = autoRateStr
                                            val a = editAmountText.toDoubleOrNull() ?: 0.0
                                            if (a > 0.0 && autoRate > 0.0) {
                                                val calc = a * autoRate
                                                editLedgerAmountText = if (calc % 1.0 == 0.0) String.format(Locale.US, "%.0f", calc)
                                                else String.format(Locale.US, "%.3f", calc).trimEnd('0').trimEnd('.')
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = c.flagEmoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${c.code} (${c.symbol})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BentoNavyDark
                                    )
                                }
                            }
                        }
                    }

                    // Calculated Currency & Exchange Rate Section (ارز محاسبه شده و نرخ تبدیل)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (editHasCalculatedCurrency) BentoIndigoAccent.copy(alpha = 0.4f) else BentoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ارز محاسبه شده و نرخ تبدیل",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }
                                Switch(
                                    checked = editHasCalculatedCurrency,
                                    onCheckedChange = { checked ->
                                        editHasCalculatedCurrency = checked
                                        if (checked) {
                                            if (editLedgerCurrencyCode.equals(editCurrencyCode, ignoreCase = true)) {
                                                val otherCurr = activeCurrencies.firstOrNull { !it.code.equals(editCurrencyCode, ignoreCase = true) }
                                                if (otherCurr != null) {
                                                    editLedgerCurrencyCode = otherCurr.code
                                                    editLedgerCurrencySymbol = otherCurr.symbol
                                                    val fromCurr = activeCurrencies.find { it.code.equals(editCurrencyCode, ignoreCase = true) }
                                                    val fromRate = fromCurr?.exchangeRateToBase ?: 1.0
                                                    val toRate = otherCurr.exchangeRateToBase
                                                    val autoRate = if (toRate > 0) fromRate / toRate else 1.0
                                                    val autoRateStr = if (autoRate == 1.0) "1"
                                                    else if (autoRate % 1.0 == 0.0) String.format(Locale.US, "%.0f", autoRate)
                                                    else String.format(Locale.US, "%.4f", autoRate).trimEnd('0').trimEnd('.')
                                                    editExchangeRateText = autoRateStr
                                                    val a = editAmountText.toDoubleOrNull() ?: 0.0
                                                    if (a > 0.0 && autoRate > 0.0) {
                                                        val calc = a * autoRate
                                                        editLedgerAmountText = if (calc % 1.0 == 0.0) String.format(Locale.US, "%.0f", calc)
                                                        else String.format(Locale.US, "%.3f", calc).trimEnd('0').trimEnd('.')
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BentoIndigoAccent
                                    )
                                )
                            }

                            if (editHasCalculatedCurrency) {
                                Text(
                                    text = "واحد پولی محاسبه شده (دفترچه طرف حساب):",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(activeCurrencies) { c ->
                                        val isSelected = (editLedgerCurrencyId != null && editLedgerCurrencyId!! > 0 && c.id == editLedgerCurrencyId) || c.code.equals(editLedgerCurrencyCode, ignoreCase = true)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) BentoIndigoAccent else Color.White,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoIndigoAccent else BentoBorder),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    editLedgerCurrencyId = c.id
                                                    editLedgerCurrencyCode = c.code
                                                    editLedgerCurrencySymbol = c.symbol
                                                    if (c.code.equals(editCurrencyCode, ignoreCase = true)) {
                                                        // تغییر به ارز خود تراکنش: خودکار نرخ تبدیل برابر با ۱ بشه و فیلدهای تبدیل و مبلغ محاسبه مخفی بشه
                                                        editExchangeRateText = "1"
                                                        editLedgerAmountText = editAmountText
                                                    } else {
                                                        val fromCurr = activeCurrencies.find { it.code.equals(editCurrencyCode, ignoreCase = true) }
                                                        val fromRate = fromCurr?.exchangeRateToBase ?: 1.0
                                                        val toRate = c.exchangeRateToBase
                                                        val autoRate = if (toRate > 0) fromRate / toRate else 1.0
                                                        val autoRateStr = if (autoRate == 1.0) "1"
                                                        else if (autoRate % 1.0 == 0.0) String.format(Locale.US, "%.0f", autoRate)
                                                        else String.format(Locale.US, "%.4f", autoRate).trimEnd('0').trimEnd('.')
                                                        editExchangeRateText = autoRateStr
                                                        val a = editAmountText.toDoubleOrNull() ?: 0.0
                                                        if (a > 0.0 && autoRate > 0.0) {
                                                            val calc = a * autoRate
                                                            editLedgerAmountText = if (calc % 1.0 == 0.0) String.format(Locale.US, "%.0f", calc)
                                                            else String.format(Locale.US, "%.3f", calc).trimEnd('0').trimEnd('.')
                                                        }
                                                    }
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = c.flagEmoji, fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${c.code} (${c.symbol})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else BentoNavyDark
                                                )
                                            }
                                        }
                                    }
                                }

                                val isSameCurrency = editLedgerCurrencyCode.equals(editCurrencyCode, ignoreCase = true)

                                if (isSameCurrency) {
                                    // وقتی ارز محاسبه برابر با ارز خود معامله شود، نرخ تبدیل ۱ شده و فیلدها مخفی می‌شوند
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = IncomeGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "ارز محاسبه با ارز اصلی معامله یکسان است (نرخ تبدیل = ۱، بدون نیاز به تبدیل)",
                                                fontSize = 11.sp,
                                                color = BentoNavyDark,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Ledger Amount Input
                                        OutlinedTextField(
                                            value = editLedgerAmountText,
                                            onValueChange = { newLedgerText ->
                                                editLedgerAmountText = newLedgerText
                                                if (ledgerAmountError != null) ledgerAmountError = null
                                                val la = newLedgerText.toDoubleOrNull()
                                                val a = editAmountText.toDoubleOrNull()
                                                if (la != null && la > 0.0 && a != null && a > 0.0) {
                                                    val r = la / a
                                                    editExchangeRateText = if (r % 1.0 == 0.0) String.format(Locale.US, "%.0f", r)
                                                    else String.format(Locale.US, "%.6f", r).trimEnd('0').trimEnd('.')
                                                    if (exchangeRateError != null) exchangeRateError = null
                                                }
                                            },
                                            label = { Text("مبلغ محاسبه ($editLedgerCurrencySymbol)", fontSize = 11.sp) },
                                            isError = ledgerAmountError != null,
                                            supportingText = ledgerAmountError?.let { err ->
                                                {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = null,
                                                            tint = ExpenseRed,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = err,
                                                            color = ExpenseRed,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1.1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = textFieldColors,
                                            trailingIcon = {
                                                CalculatorMiniButton(
                                                    onClick = { showCalculatorForLedgerAmount = true },
                                                    contentDescription = "ماشین‌حساب مبلغ محاسبه شده"
                                                )
                                            }
                                        )

                                        // Exchange Rate Input
                                        OutlinedTextField(
                                            value = editExchangeRateText,
                                            onValueChange = { newRateText ->
                                                editExchangeRateText = newRateText
                                                if (exchangeRateError != null) exchangeRateError = null
                                                val r = newRateText.toDoubleOrNull()
                                                val a = editAmountText.toDoubleOrNull()
                                                if (r != null && r > 0.0 && a != null && a > 0.0) {
                                                    val calcLedger = a * r
                                                    editLedgerAmountText = if (calcLedger % 1.0 == 0.0) String.format(Locale.US, "%.0f", calcLedger)
                                                    else String.format(Locale.US, "%.3f", calcLedger).trimEnd('0').trimEnd('.')
                                                    if (ledgerAmountError != null) ledgerAmountError = null
                                                }
                                            },
                                            label = { Text("نرخ تبدیل", fontSize = 11.sp) },
                                            isError = exchangeRateError != null,
                                            supportingText = exchangeRateError?.let { err ->
                                                {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = null,
                                                            tint = ExpenseRed,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = err,
                                                            color = ExpenseRed,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(0.9f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = textFieldColors,
                                            trailingIcon = {
                                                CalculatorMiniButton(
                                                    onClick = { showCalculatorForRate = true },
                                                    contentDescription = "ماشین‌حساب نرخ تبدیل"
                                                )
                                            }
                                        )
                                    }

                                    val previewRate = editExchangeRateText.toDoubleOrNull() ?: 1.0
                                    val rateSummary = formatExchangeRateText(previewRate, editCurrencyCode, editLedgerCurrencyCode)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = BentoIndigoAccent.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "معادل: $editLedgerCurrencySymbol ${editLedgerAmountText.ifBlank { "۰" }}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoIndigoAccent
                                            )
                                            if (rateSummary.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "($rateSummary)",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Category Selector Chips (فقط برای تراکنش‌های عمومی یا دارای دسته‌بندی)
                    val isExchangeOrTransfer = transaction.category.equals("Exchange", ignoreCase = true) ||
                            transaction.category.equals("انتقالات", ignoreCase = true) ||
                            transaction.category.equals("تبادله ارز", ignoreCase = true)
                    val shouldShowCategory = !isExchangeOrTransfer && !isGoalTxn && (editType == TransactionType.EXPENSE || editType == TransactionType.INCOME || !hasRecipient || editCategory.isNotBlank())

                    if (shouldShowCategory) {
                        val categoryHeader = when (editType) {
                            TransactionType.EXPENSE -> "دسته‌بندی مصرف:"
                            TransactionType.INCOME -> "دسته‌بندی عاید:"
                            else -> "دسته‌بندی معامله:"
                        }
                        Text(text = categoryHeader, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Option for No Category
                            item {
                                val isNoCatSelected = editCategory.isBlank()
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isNoCatSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isNoCatSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            editCategoryId = null
                                            editCategory = ""
                                        }
                                ) {
                                    Text(
                                        text = "بدون دسته‌بندی",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isNoCatSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            items(availableCategories, key = { it.id }) { catEntity ->
                                val isSelected = editCategoryId == catEntity.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            editCategoryId = catEntity.id
                                            editCategory = catEntity.name
                                        }
                                ) {
                                    Text(
                                        text = formatCategoryDari(catEntity.name),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Account Selector Chips (فقط در صورت تأثیر در موجودی)
                    if (editAffectsBalance) {
                        Text(
                            text = if (isGoalTxn) "کیف پول / کارت (پیش‌فرض: بیلانس کل):" else "کیف پول / حساب معامله (پیش‌فرض: بیلانس کل):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoNavyDark
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Option for Main Balance
                            item {
                                val isMainSelected = editAccountId == 0L
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isMainSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isMainSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { editAccountId = 0L }
                                ) {
                                    Text(
                                        text = "بیلانس کل (پیش‌فرض)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isMainSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            items(accounts.filter { !it.isFrozen }) { acct ->
                                val isSelected = acct.id == editAccountId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { editAccountId = acct.id }
                                ) {
                                    Text(
                                        text = "${acct.name} (${acct.currencySymbol})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Affects Balance Toggle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { editAffectsBalance = !editAffectsBalance },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تأثیر بر موجودی",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (editAffectsBalance) "مبلغ بر موجودی و گزارشات اثر می‌گذارد" else "بدون تغییر موجودی (صرفاً ثبت در سوابق)",
                                    fontSize = 11.sp,
                                    color = if (editAffectsBalance) IncomeGreen else TextSecondary
                                )
                            }
                            Switch(
                                checked = editAffectsBalance,
                                onCheckedChange = { editAffectsBalance = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BentoNavyDark
                                )
                            )
                        }
                    }

                    // Transaction Date Selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BentoNavyDark, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("تاریخ و ساعت معامله", fontSize = 11.sp, color = TextSecondary)
                                    Text(PersianDateHelper.formatSolarDateTime(editTimestamp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "انتخاب تاریخ",
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Due Date Selector (فقط برای معاملات مرتبط با اشخاص یا دارای سررسید)
                    if (!transaction.recipientName.isNullOrBlank() || transaction.dueDate != null || editDueDate != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = if (editDueDate != null) BentoIndigoAccent else TextSecondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("تاریخ سررسید", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            text = editDueDate?.let { PersianDateHelper.formatSolarDate(it) } ?: "تعیین نشده",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (editDueDate != null) BentoNavyDark else TextSecondary
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (editDueDate != null) {
                                        TextButton(
                                            onClick = { editDueDate = null },
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("حذف", fontSize = 11.sp, color = ExpenseRed)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { showDueDatePickerDialog = true },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(if (editDueDate != null) "ویرایش" else "تعیین", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Note
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("یادداشت معامله") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_txn_note_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Fixed Bottom Action Bar for Edit Mode (تأیید و ذخیره / انصراف)
        if (isEditMode) {
                Surface(
                    color = SurfaceWhite,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val trimmedTitle = editTitle.trim()
                                val parsedAmt = editAmountText.toDoubleOrNull()
                                val isSameCurrency = editLedgerCurrencyCode.equals(editCurrencyCode, ignoreCase = true)
                                val hasCalculated = editHasCalculatedCurrency && !isSameCurrency

                                var hasErrors = false

                                if (trimmedTitle.isEmpty()) {
                                    titleError = "لطفاً عنوان معامله را وارد کنید"
                                    hasErrors = true
                                } else {
                                    titleError = null
                                }

                                if (parsedAmt == null || parsedAmt <= 0.0) {
                                    amountError = "لطفاً مقدار معتبر و بزرگتر از صفر وارد کنید"
                                    hasErrors = true
                                } else {
                                    amountError = null
                                }

                                // Pre-validate account balance when editing
                                if (parsedAmt != null && parsedAmt > 0.0 && editAffectsBalance) {
                                    val cleanAmt = parsedAmt
                                    if (editAccountId > 0L) {
                                        val acc = accounts.find { it.id == editAccountId }
                                        if (acc != null) {
                                            val oldDelta = if (transaction.affectsBalance && transaction.accountId == editAccountId) {
                                                if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
                                            } else 0.0
                                            val newDelta = if (editType == TransactionType.INCOME) cleanAmt else -cleanAmt
                                            val projected = acc.balance + oldDelta + newDelta
                                            if (projected < 0.0) {
                                                amountError = "موجودی کارت پس از ویرایش منفی می‌شود (${acc.currencyCode} ${formatAmountDisplay(projected)})"
                                                hasErrors = true
                                            }
                                        }
                                        if (transaction.affectsBalance && transaction.accountId > 0L && transaction.accountId != editAccountId && transaction.type == TransactionType.INCOME) {
                                            val oldAcc = accounts.find { it.id == transaction.accountId }
                                            if (oldAcc != null && oldAcc.balance - transaction.amount < 0.0) {
                                                amountError = "موجودی کارت قبلی (${oldAcc.name}) با لغو تراکنش منفی می‌شود"
                                                hasErrors = true
                                            }
                                        }
                                        if (transaction.accountId != editAccountId && editType == TransactionType.EXPENSE) {
                                            val newAcc = accounts.find { it.id == editAccountId }
                                            if (newAcc != null && newAcc.balance - cleanAmt < 0.0) {
                                                amountError = "موجودی کارت انتخابی برای این هزینه کافی نیست"
                                                hasErrors = true
                                            }
                                        }
                                    }
                                }

                                var finalLedgerAmt: Double? = null
                                var finalRate = 1.0

                                if (hasCalculated) {
                                    val la = editLedgerAmountText.toDoubleOrNull()
                                    val rInput = editExchangeRateText.toDoubleOrNull()

                                    if (la == null || la <= 0.0) {
                                        ledgerAmountError = "مبلغ محاسبه باید بزرگتر از صفر باشد"
                                        hasErrors = true
                                    } else {
                                        ledgerAmountError = null
                                        finalLedgerAmt = la
                                    }

                                    if (rInput == null || rInput <= 0.0) {
                                        exchangeRateError = "نرخ تبدیل نامعتبر است"
                                        hasErrors = true
                                    } else {
                                        exchangeRateError = null
                                        finalRate = rInput
                                    }
                                } else {
                                    ledgerAmountError = null
                                    exchangeRateError = null
                                    finalRate = if (editHasCalculatedCurrency && isSameCurrency) 1.0
                                    else (if (transaction.exchangeRate > 0.0) transaction.exchangeRate else 1.0)
                                }

                                if (!hasErrors) {
                                    val amt = parsedAmt!!
                                    val finalLedgerCode = if (hasCalculated && (finalLedgerAmt ?: 0.0) > 0.0) editLedgerCurrencyCode else null
                                    val finalLedgerSymbol = if (hasCalculated && (finalLedgerAmt ?: 0.0) > 0.0) editLedgerCurrencySymbol else null
                                    val finalNote = cleanTransactionNote(editNote)

                                    val resolvedCatId = if (editCategory.isBlank()) null else editCategoryId

                                    val finalType = if (isGoalTxn) TransactionType.TRANSFER else editType
                                    val finalKind = if (isGoalTxn) {
                                        if (editType == TransactionType.EXPENSE || editCategory == "واریز به هدف") TransactionKind.GOAL_DEPOSIT
                                        else TransactionKind.GOAL_WITHDRAW
                                    } else transaction.kind

                                    val resolvedCurrId = if (editCurrencyId > 0L) editCurrencyId
                                        else (activeCurrencies.firstOrNull { it.code.equals(editCurrencyCode, ignoreCase = true) }?.id ?: transaction.currencyId)
                                    val resolvedLedgerCurrId = if (editHasCalculatedCurrency) {
                                        if (editLedgerCurrencyId != null && editLedgerCurrencyId!! > 0L) editLedgerCurrencyId
                                        else activeCurrencies.firstOrNull { it.code.equals(finalLedgerCode, ignoreCase = true) }?.id
                                    } else null

                                    val updated = transaction.copy(
                                        title = trimmedTitle,
                                        amount = amt,
                                        type = finalType,
                                        kind = finalKind,
                                        category = editCategory,
                                        categoryId = resolvedCatId,
                                        currencyId = resolvedCurrId,
                                        currencyCode = editCurrencyCode,
                                        currencySymbol = editCurrencySymbol,
                                        exchangeRate = finalRate,
                                        ledgerCurrencyId = resolvedLedgerCurrId,
                                        ledgerCurrencyCode = finalLedgerCode,
                                        ledgerCurrencySymbol = finalLedgerSymbol,
                                        ledgerAmount = finalLedgerAmt,
                                        note = finalNote,
                                        accountId = if (editAffectsBalance) editAccountId else 0L,
                                        timestamp = editTimestamp,
                                        dueDate = editDueDate,
                                        affectsBalance = editAffectsBalance
                                    )
                                    onUpdate(transaction, updated) { success, errorMsg ->
                                        if (success) {
                                            isEditMode = false
                                            generalEditError = null
                                        } else {
                                            generalEditError = errorMsg
                                            amountError = errorMsg
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("save_txn_edit_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                titleError = null
                                amountError = null
                                ledgerAmountError = null
                                exchangeRateError = null
                                isEditMode = false
                            },
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("cancel_txn_edit_btn"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Text("انصراف", color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialogs for Editing
    if (showDatePickerDialog) {
        SolarDatePickerDialog(
            initialTimestamp = editTimestamp,
            title = "انتخاب تاریخ معامله",
            onDismiss = { showDatePickerDialog = false },
            onDateSelected = {
                editTimestamp = it
                showDatePickerDialog = false
            }
        )
    }

    if (showDueDatePickerDialog) {
        SolarDatePickerDialog(
            initialTimestamp = editDueDate ?: System.currentTimeMillis(),
            title = "انتخاب تاریخ سررسید",
            onDismiss = { showDueDatePickerDialog = false },
            onDateSelected = {
                editDueDate = it
                showDueDatePickerDialog = false
            }
        )
    }

    if (showCalculatorForAmount) {
        MinimalCalculatorDialog(
            initialValue = editAmountText,
            title = "محاسبه مقدار معامله",
            onConfirm = { calculated ->
                editAmountText = calculated
                if (amountError != null) amountError = null
                val a = calculated.toDoubleOrNull()
                val r = editExchangeRateText.toDoubleOrNull()
                if (a != null && a > 0.0 && r != null && r > 0.0) {
                    val calcLedger = a * r
                    editLedgerAmountText = if (calcLedger % 1.0 == 0.0) String.format(Locale.US, "%.0f", calcLedger)
                    else String.format(Locale.US, "%.3f", calcLedger).trimEnd('0').trimEnd('.')
                    if (ledgerAmountError != null) ledgerAmountError = null
                }
            },
            onDismiss = { showCalculatorForAmount = false }
        )
    }

    if (showCalculatorForRate) {
        MinimalCalculatorDialog(
            initialValue = editExchangeRateText,
            title = "محاسبه نرخ تبدیل ارز",
            onConfirm = { calculated ->
                editExchangeRateText = calculated
                if (exchangeRateError != null) exchangeRateError = null
                val r = calculated.toDoubleOrNull()
                val a = editAmountText.toDoubleOrNull()
                if (r != null && r > 0.0 && a != null && a > 0.0) {
                    val calcLedger = a * r
                    editLedgerAmountText = if (calcLedger % 1.0 == 0.0) String.format(Locale.US, "%.0f", calcLedger)
                    else String.format(Locale.US, "%.3f", calcLedger).trimEnd('0').trimEnd('.')
                    if (ledgerAmountError != null) ledgerAmountError = null
                }
            },
            onDismiss = { showCalculatorForRate = false }
        )
    }

    if (showCalculatorForLedgerAmount) {
        MinimalCalculatorDialog(
            initialValue = editLedgerAmountText,
            title = "محاسبه مبلغ ارز محاسبه شده",
            onConfirm = { calculated ->
                editLedgerAmountText = calculated
                if (ledgerAmountError != null) ledgerAmountError = null
                val la = calculated.toDoubleOrNull()
                val a = editAmountText.toDoubleOrNull()
                if (la != null && la > 0.0 && a != null && a > 0.0) {
                    val r = la / a
                    editExchangeRateText = if (r % 1.0 == 0.0) String.format(Locale.US, "%.0f", r)
                    else String.format(Locale.US, "%.6f", r).trimEnd('0').trimEnd('.')
                    if (exchangeRateError != null) exchangeRateError = null
                }
            },
            onDismiss = { showCalculatorForLedgerAmount = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        val isGoalTxnForDelete = transaction.kind == TransactionKind.GOAL_DEPOSIT ||
                transaction.kind == TransactionKind.GOAL_WITHDRAW ||
                transaction.category == "واریز به هدف" ||
                transaction.category == "برداشت از هدف" ||
                transaction.title.startsWith("انتقال به هدف") ||
                transaction.title.startsWith("انتقال از هدف")
        val isTransferTxn = !isGoalTxnForDelete && (transaction.category == "انتقالات" || transaction.type == TransactionType.TRANSFER || transaction.relatedTransactionId != null)
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (isTransferTxn) "حذف معامله انتقال" else if (isGoalTxnForDelete) "حذف معامله هدف" else "حذف معامله", fontWeight = FontWeight.Bold, color = BentoNavyDark) },
            text = {
                Text(
                    if (isTransferTxn) "آیا مطمئن هستید که می‌خواهید این معامله انتقال را حذف کنید؟ با حذف آن، هر دو تراکنش مربوط به انتقال (مبدأ و مقصد) باهم حذف شده و مبالغ به موجودی حساب‌ها بازمی‌گردد."
                    else if (isGoalTxnForDelete) "آیا مطمئن هستید که می‌خواهید این تراکنش هدف مالی را حذف کنید؟ با حذف آن، موجودی هدف و حساب مربوطه بازگردانی می‌شود."
                    else "آیا مطمئن هستید که می‌خواهید این معامله را حذف کنید؟ این عمل غیرقابل بازگشت است."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(transaction)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(if (isTransferTxn) "بله، حذف انتقال و برگشت موجودی" else "بله، حذف شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

/**
 * Luxury BottomSheet for Viewing & Editing any Wallet / Account Card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxuryWalletDetailAndEditBottomSheet(
    account: AccountCardEntity,
    activeCurrencies: List<CurrencyEntity>,
    onDismiss: () -> Unit,
    onUpdate: (AccountCardEntity) -> Unit,
    onDelete: (AccountCardEntity) -> Unit,
    onToggleFreeze: (AccountCardEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Edit fields
    var editName by remember(account) { mutableStateOf(account.name) }
    var editCardNumber by remember(account) { mutableStateOf(account.cardNumberMasked) }
    var editBalanceText by remember(account) {
        mutableStateOf(
            if (account.balance % 1.0 == 0.0) String.format(Locale.US, "%.0f", account.balance)
            else String.format(Locale.US, "%.3f", account.balance).trimEnd('0').trimEnd('.')
        )
    }
    var editCurrencyId by remember(account) { mutableStateOf(account.currencyId) }
    var editCurrencyCode by remember(account) { mutableStateOf(account.currencyCode) }
    var editCurrencySymbol by remember(account) { mutableStateOf(account.currencySymbol) }
    var editTheme by remember(account) { mutableStateOf(account.cardColorTheme) }
    var showCalculatorForBalance by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = BentoNavyDark,
        unfocusedTextColor = BentoNavyDark,
        cursorColor = BentoNavyDark,
        focusedBorderColor = BentoNavyDark,
        unfocusedBorderColor = BentoBorder,
        focusedContainerColor = SurfaceWhite,
        unfocusedContainerColor = SurfaceWhite,
        focusedLabelColor = BentoNavyDark,
        unfocusedLabelColor = TextSecondary
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = null,
        modifier = Modifier.testTag("luxury_wallet_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE2E8F0))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = BentoNavyDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEditMode) "ویرایش کیف پول" else "جزئیات کیف پول و کارت",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Text(
                            text = if (isEditMode) "مشخصات حساب و ارز را تغییر دهید" else "اطلاعات حساب و تنظیمات امنیتی",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isEditMode) BentoNavyDark else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { isEditMode = !isEditMode }
                            .testTag("toggle_edit_wallet_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null,
                                tint = if (isEditMode) Color.White else BentoNavyDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isEditMode) "حالت نمایش" else "ویرایش",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEditMode) Color.White else BentoNavyDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Luxury Card Visual Graphic
            val gradient = when (if (isEditMode) editTheme else account.cardColorTheme) {
                "teal" -> Brush.horizontalGradient(listOf(CardTealGradientStart, CardTealGradientEnd))
                "gold" -> Brush.horizontalGradient(listOf(CardGoldGradientStart, CardGoldGradientEnd))
                else -> Brush.horizontalGradient(listOf(CardDarkGradientStart, CardDarkGradientEnd))
            }
            val cardCurrency = if (isEditMode) editCurrencySymbol else account.currencySymbol

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(gradient)
                        .padding(20.dp)
                ) {
                    // Top: Card Name & Currency Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) editName else account.name,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isEditMode) "$editCurrencyCode ($editCurrencySymbol)" else "${account.currencyCode} (${account.currencySymbol})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Center: Card Number & Freeze Indicator
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = if (isEditMode) editCardNumber else account.cardNumberMasked,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                        if (account.isFrozen && !isEditMode) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🔒 کارت مسدود است",
                                color = Color(0xFFFFB4AB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom: Balance and Holder
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "موجودی",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            val bal = if (isEditMode) editBalanceText.toDoubleOrNull() ?: account.balance else account.balance
                            Text(
                                text = "${formatAmountDisplay(bal)} $cardCurrency",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isEditMode) editCardNumber else account.cardNumberMasked,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isEditMode) {
                // ==================== VIEW MODE ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        DetailInfoRow(label = "نام حساب", value = account.name)
                        DetailInfoRow(label = "واحد پولی حساب", value = "${account.currencyCode} (${account.currencySymbol})")
                        DetailInfoRow(label = "موجودی فعلی", value = "\u200E${account.currencySymbol} ${String.format(Locale.US, "%,.2f", account.balance)}\u200E")
                        DetailInfoRow(label = "شماره حساب / کارت", value = account.cardNumberMasked)
                        DetailInfoRow(
                            label = "وضعیت کارت",
                            value = if (account.isFrozen) "مسدود شده" else "فعال و آماده استفاده"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Toggle Freeze, Edit, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Freeze / Unfreeze
                    OutlinedButton(
                        onClick = { onToggleFreeze(account) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoNavyDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoNavyDark)
                    ) {
                        Icon(
                            imageVector = if (account.isFrozen) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (account.isFrozen) "رفع مسدودی" else "مسدود ساختن",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Edit
                    Button(
                        onClick = { isEditMode = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ویرایش", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = ExpenseRed)
                    }
                }
            } else {
                // ==================== EDIT MODE ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card Name
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("نام حساب یا کارت") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_wallet_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    // Card Number
                    OutlinedTextField(
                        value = editCardNumber,
                        onValueChange = { editCardNumber = it },
                        label = { Text("شماره کارت یا شماره حساب") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_wallet_cardnum_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    // Balance (Read-only to enforce strict Financial Integrity - balance can only change via financial operations)
                    OutlinedTextField(
                        value = editBalanceText,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("موجودی حساب ($editCurrencySymbol)") },
                        supportingText = {
                            Text(
                                text = "موجودی حساب فقط از طریق ثبت تراکنش مالی تغییر می‌کند",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_wallet_balance_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    // Currency Selector Pills (Crucial requirement: every wallet has an associated currency)
                    Text(text = "واحد پولی این حساب / کارت:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(activeCurrencies) { c ->
                            val isSelected = (editCurrencyId > 0 && c.id == editCurrencyId) || c.code.equals(editCurrencyCode, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        editCurrencyId = c.id
                                        editCurrencyCode = c.code
                                        editCurrencySymbol = c.symbol
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = c.flagEmoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${c.code} (${c.symbol})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BentoNavyDark
                                    )
                                }
                            }
                        }
                    }

                    // Card Theme
                    Text(text = "رنگ و طرح بصری کارت:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("dark" to "سرمه‌ای لوکس", "teal" to "نیلی مدرن", "gold" to "طلایی شاهانه").forEach { (th, label) ->
                            val isSelected = editTheme == th
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { editTheme = th }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoNavyDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons (Save & Cancel)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val updated = account.copy(
                                    name = editName.ifBlank { account.name },
                                    cardNumberMasked = editCardNumber.ifBlank { account.cardNumberMasked },
                                    balance = account.balance,
                                    currencyId = if (editCurrencyId > 0L) editCurrencyId else (activeCurrencies.firstOrNull { it.code.equals(editCurrencyCode, ignoreCase = true) }?.id ?: account.currencyId),
                                    currencyCode = editCurrencyCode,
                                    currencySymbol = editCurrencySymbol,
                                    cardColorTheme = editTheme
                                )
                                onUpdate(updated)
                                isEditMode = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("save_wallet_edit_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                        }

                        TextButton(
                            onClick = { isEditMode = false },
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("انصراف", color = TextSecondary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCalculatorForBalance) {
        MinimalCalculatorDialog(
            initialValue = editBalanceText,
            title = "محاسبه موجودی حساب",
            onConfirm = { calculated ->
                editBalanceText = calculated
            },
            onDismiss = { showCalculatorForBalance = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("حذف کیف پول / کارت", fontWeight = FontWeight.Bold, color = BentoNavyDark) },
            text = { Text("آیا مطمئن هستید که می‌خواهید کارت «${account.name}» را حذف کنید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(account)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("بله، حذف شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = BentoNavyDark,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}
