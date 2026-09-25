package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.RecipientEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.DateRangePreset
import com.example.ui.components.LuxuryTransactionDetailAndEditBottomSheet
import com.example.ui.components.SolarDatePickerDialog
import com.example.ui.components.cleanTransactionNote
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.AppCalendarType
import com.example.util.PersianDateHelper
import java.text.DecimalFormat
import java.util.Calendar

/**
 * Filter type for Person Statement Ledger
 */
enum class PersonStatementTypeFilter(val title: String) {
    ALL("همه"),
    RECEIVE("دریافتی (Receive)"),
    PAY("پرداختی (Pay)")
}

/**
 * Settlement status filter for Person Statement Ledger
 */
enum class PersonStatementSettlementFilter(val title: String) {
    ALL("همه وضعیت‌ها"),
    SETTLED("تسویه‌شده"),
    UNSETTLED("تسویه‌نشده")
}

/**
 * Completely redesigned, dedicated, modern and ultra-fast Person Statement Screen.
 * Fulfills all requirements:
 * - Dedicated full screen (not a dialog)
 * - Immediate summary: who owes whom, net balance, currencies, statement status
 * - Lightweight Timeline / Ledger list with Running Balance after each transaction
 * - Grouped by dates (امروز، دیروز، ...)
 * - Toggle for: "نمایش تراکنش‌های قبل از آخرین تسویه"
 * - Multi-currency support strictly tracked by currencyId / currencyCode
 * - Masked privacy protection (****)
 * - Quick Action Bar at bottom [دریافت] [پرداخت] with pre-selected recipientId
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonStatementScreen(
    recipient: RecipientEntity,
    transactions: List<TransactionEntity>,
    accounts: List<AccountCardEntity> = emptyList(),
    targetCurrency: String? = null,
    viewModel: FinanceViewModel? = null,
    isMasked: Boolean = false,
    onRequestUnmask: () -> Unit = {},
    onNavigateToPay: (RecipientEntity, String?) -> Unit = { _, _ -> },
    onNavigateToReceive: (RecipientEntity, String?) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onDismiss()
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf(false) }

    LaunchedEffect(copiedToast) {
        if (copiedToast) {
            kotlinx.coroutines.delay(2000)
            copiedToast = false
        }
    }

    val decFormat = remember { DecimalFormat("#,##0.##") }

    // Filter transactions specifically for this recipient strictly by recipientId
    val allPersonTxns = remember(transactions, recipient.id) {
        transactions.filter { txn ->
            txn.recipientId == recipient.id
        }.sortedBy { it.timestamp } // Chronological order ascending for running balance calculation
    }

    // Helper functions for effective currency and amount
    fun TransactionEntity.effectiveCurrency(): String =
        if (!ledgerCurrencyCode.isNullOrBlank() && ledgerAmount != null && ledgerAmount > 0.0) {
            ledgerCurrencyCode.uppercase()
        } else {
            currencyCode.uppercase()
        }

    fun TransactionEntity.effectiveAmount(): Double =
        if (!ledgerCurrencyCode.isNullOrBlank() && ledgerAmount != null && ledgerAmount > 0.0) {
            ledgerAmount
        } else {
            amount
        }

    val activeCurrencies by (viewModel?.activeCurrencies?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val categories by (viewModel?.categories?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    val calendarType by (viewModel?.selectedCalendarType?.collectAsState() ?: remember { mutableStateOf(AppCalendarType.SOLAR_DARI) })

    // Helper functions for currency symbol and Persian name
    fun getCurrencySymbol(code: String): String {
        val found = activeCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
        if (found != null && found.symbol.isNotBlank()) return found.symbol
        val txnWithCurr = allPersonTxns.firstOrNull { it.effectiveCurrency().equals(code, ignoreCase = true) }
        val fromTxn = txnWithCurr?.ledgerCurrencySymbol ?: txnWithCurr?.currencySymbol
        if (!fromTxn.isNullOrBlank()) return fromTxn
        return when (code.uppercase()) {
            "AFN" -> "؋"
            "USD" -> "$"
            "EUR" -> "€"
            "IRR" -> "تومان"
            "PKR" -> "₨"
            "GBP" -> "£"
            "AED" -> "د.إ"
            "TRY" -> "₺"
            else -> code
        }
    }

    fun getCurrencyName(code: String): String {
        val found = activeCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) }
        if (found != null && found.name.isNotBlank()) return found.name
        return when (code.uppercase()) {
            "AFN" -> "افغانی"
            "USD" -> "دالر آمریکایی"
            "EUR" -> "یورو"
            "IRR" -> "تومان ایران"
            "PKR" -> "کلدار پاکستان"
            "GBP" -> "پوند انگلیس"
            "AED" -> "درهم امارات"
            "TRY" -> "لیر ترکیه"
            else -> code
        }
    }

    // All available selectable currencies: from this person's transactions + all active currencies in the system
    val allSelectableCurrencies = remember(allPersonTxns, activeCurrencies, targetCurrency) {
        val personCodes = allPersonTxns.map { it.effectiveCurrency() }
        val activeCodes = activeCurrencies.map { it.code.uppercase() }
        val combined = (personCodes + activeCodes + listOfNotNull(targetCurrency?.uppercase(), "AFN")).distinct()
        combined
    }

    // Available currencies for this recipient
    val availableCurrencies = remember(allPersonTxns, targetCurrency) {
        val list = allPersonTxns.map { it.effectiveCurrency() }.distinct()
        if (list.isEmpty()) {
            listOf(targetCurrency?.uppercase() ?: "AFN")
        } else {
            list
        }
    }

    var selectedCurrency by remember(allSelectableCurrencies, targetCurrency) {
        mutableStateOf(
            if (!targetCurrency.isNullOrBlank()) {
                allSelectableCurrencies.firstOrNull { it.equals(targetCurrency, ignoreCase = true) } ?: targetCurrency.uppercase()
            } else {
                availableCurrencies.firstOrNull() ?: allSelectableCurrencies.firstOrNull() ?: "AFN"
            }
        )
    }

    val selectedCurrencySymbol = remember(selectedCurrency, activeCurrencies) {
        getCurrencySymbol(selectedCurrency)
    }

    var showCurrencyPickerSheet by remember { mutableStateOf(false) }

    // Filters and search states
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTypeFilter by remember { mutableStateOf(PersonStatementTypeFilter.ALL) }
    var selectedSettlementFilter by remember { mutableStateOf(PersonStatementSettlementFilter.ALL) }
    var selectedDateRangePreset by remember { mutableStateOf(DateRangePreset.ALL) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }

    // Key requirement: Checkbox "نمایش تراکنش‌های قبل از آخرین تسویه"
    // Default is false: show only transactions from the last full settlement onward
    var showBeforeLastSettlement by remember { mutableStateOf(false) }

    // Selected transaction for details/edit sheet
    var selectedTxnForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    // Chronological transactions for current currency
    val currencyTxnsChronological = remember(allPersonTxns, selectedCurrency) {
        allPersonTxns.filter { it.effectiveCurrency().equals(selectedCurrency, ignoreCase = true) }
    }

    // Calculate Running Balance and Identify Settlement Points (where balance becomes 0)
    // Running balance definition:
    // EXPENSE (We paid to person / lent money) -> +amount (Our receivable / طلب ما از او)
    // INCOME (We received from person / borrowed money) -> -amount (Our payable / بدهی ما به او)
    val (runningBalancesMap, lastSettlementPointIndex) = remember(currencyTxnsChronological) {
        val map = mutableMapOf<Long, Double>()
        var running = 0.0
        var lastZeroIdx = -1

        for (i in currencyTxnsChronological.indices) {
            val txn = currencyTxnsChronological[i]
            val effAmt = txn.effectiveAmount()
            if (txn.type == TransactionType.EXPENSE) {
                running += effAmt
            } else if (txn.type == TransactionType.INCOME) {
                running -= effAmt
            }
            map[txn.id] = running

            // Check if this transaction completely settled the account (balance reached ~0)
            if (kotlin.math.abs(running) < 0.01) {
                lastZeroIdx = i
            }
        }
        Pair(map, lastZeroIdx)
    }

    // Current net balance for selected currency
    val currentNetBalance = remember(currencyTxnsChronological, runningBalancesMap) {
        if (currencyTxnsChronological.isEmpty()) 0.0 else (runningBalancesMap[currencyTxnsChronological.last().id] ?: 0.0)
    }

    val isAccountCurrentlySettled = remember(currentNetBalance, currencyTxnsChronological) {
        currencyTxnsChronological.isNotEmpty() && kotlin.math.abs(currentNetBalance) < 0.01
    }

    // Apply "نمایش تراکنش‌های قبل از آخرین تسویه / حساب‌های تسویه" logic:
    // If showBeforeLastSettlement == false:
    // If account is settled, show NO transactions even if there are transactions!
    val txnsAfterSettlementFilter = remember(
        currencyTxnsChronological,
        showBeforeLastSettlement,
        lastSettlementPointIndex,
        isAccountCurrentlySettled
    ) {
        if (showBeforeLastSettlement) {
            currencyTxnsChronological
        } else if (isAccountCurrentlySettled) {
            // صورتحساب تسویه است و تیک نمایش حساب‌های تسویه وجود ندارد -> هیچ تراکنشی نشان داده نشود
            emptyList()
        } else if (lastSettlementPointIndex != -1 && lastSettlementPointIndex < currencyTxnsChronological.size - 1) {
            // There are transactions after the last zero settlement
            currencyTxnsChronological.subList(lastSettlementPointIndex + 1, currencyTxnsChronological.size)
        } else {
            currencyTxnsChronological
        }
    }

    // Active filter bounds calculated based on calendarType (Solar Dari / Solar Iranian / Gregorian / Lunar)
    val (activeFilterStartTimestamp, activeFilterEndTimestamp) = remember(
        selectedDateRangePreset,
        customStartDate,
        customEndDate,
        calendarType
    ) {
        val now = System.currentTimeMillis()
        when (selectedDateRangePreset) {
            DateRangePreset.ALL -> Pair(null, null)
            DateRangePreset.TODAY -> {
                val start = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.YESTERDAY -> {
                val start = Calendar.getInstance().apply {
                    timeInMillis = now; add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = Calendar.getInstance().apply {
                    timeInMillis = now; add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.THIS_WEEK -> {
                val start = Calendar.getInstance().apply {
                    timeInMillis = now; add(Calendar.DAY_OF_YEAR, -7)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val end = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.THIS_MONTH -> {
                // Strictly based on selected calendar: first day to last day of this month
                val start = PersianDateHelper.getStartOfCurrentMonth(calendarType, now)
                val end = PersianDateHelper.getEndOfCurrentMonth(calendarType, now)
                Pair(start, end)
            }
            DateRangePreset.LAST_MONTH -> {
                val startOfThis = PersianDateHelper.getStartOfCurrentMonth(calendarType, now)
                val endOfLast = startOfThis - 1
                val startOfLast = PersianDateHelper.getStartOfCurrentMonth(calendarType, endOfLast)
                Pair(startOfLast, endOfLast)
            }
            DateRangePreset.CUSTOM -> {
                val start = customStartDate?.let {
                    Calendar.getInstance().apply {
                        timeInMillis = it
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                } ?: 0L
                val end = customEndDate?.let {
                    Calendar.getInstance().apply {
                        timeInMillis = it
                        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                } ?: Long.MAX_VALUE
                Pair(start, end)
            }
        }
    }

    // Previous Balance before the filtered period (حساب سابقه / مانده انتقالی از قبل)
    val previousBalance = remember(
        txnsAfterSettlementFilter,
        activeFilterStartTimestamp,
        runningBalancesMap
    ) {
        if (activeFilterStartTimestamp == null) {
            0.0
        } else {
            val txnsBefore = txnsAfterSettlementFilter.filter { it.timestamp < activeFilterStartTimestamp }
            if (txnsBefore.isNotEmpty()) {
                runningBalancesMap[txnsBefore.last().id] ?: 0.0
            } else {
                0.0
            }
        }
    }

    // Apply User Filters (Type, Settlement, Date Range, Search)
    val fullyFilteredTxns = remember(
        txnsAfterSettlementFilter,
        selectedTypeFilter,
        selectedSettlementFilter,
        activeFilterStartTimestamp,
        activeFilterEndTimestamp,
        searchQuery
    ) {
        txnsAfterSettlementFilter.filter { txn ->
            // 1. Type Filter
            val matchesType = when (selectedTypeFilter) {
                PersonStatementTypeFilter.ALL -> true
                PersonStatementTypeFilter.RECEIVE -> txn.type == TransactionType.INCOME
                PersonStatementTypeFilter.PAY -> txn.type == TransactionType.EXPENSE
            }
            if (!matchesType) return@filter false

            // 2. Settlement Filter
            val matchesSettlement = when (selectedSettlementFilter) {
                PersonStatementSettlementFilter.ALL -> true
                PersonStatementSettlementFilter.SETTLED -> txn.isSettled
                PersonStatementSettlementFilter.UNSETTLED -> !txn.isSettled
            }
            if (!matchesSettlement) return@filter false

            // 3. Date Range Filter
            if (activeFilterStartTimestamp != null && activeFilterEndTimestamp != null) {
                if (txn.timestamp < activeFilterStartTimestamp || txn.timestamp > activeFilterEndTimestamp) {
                    return@filter false
                }
            }

            // 4. Search Filter
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim()
                val matchesQ = txn.title.contains(q, ignoreCase = true) ||
                        txn.category.contains(q, ignoreCase = true) ||
                        (!txn.note.isNullOrBlank() && txn.note.contains(q, ignoreCase = true)) ||
                        txn.amount.toString().contains(q) ||
                        txn.effectiveAmount().toString().contains(q)
                if (!matchesQ) return@filter false
            }

            true
        }
    }

    // Display descending (newest first) for user-friendly statement view
    val displayTxns = remember(fullyFilteredTxns) {
        fullyFilteredTxns.sortedByDescending { it.timestamp }
    }

    // Lazy load paging (initial 35 items)
    var visibleLimit by remember { mutableIntStateOf(35) }
    LaunchedEffect(selectedCurrency, showBeforeLastSettlement, selectedTypeFilter, selectedSettlementFilter, searchQuery, selectedDateRangePreset) {
        visibleLimit = 35
    }

    val pagedTxns = remember(displayTxns, visibleLimit) {
        displayTxns.take(visibleLimit)
    }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            val lastIdx = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastIdx >= total - 3 && visibleLimit < displayTxns.size
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            visibleLimit += 35
        }
    }

    // Group paged items by date
    data class StatementDateGroup(
        val dateLabel: String,
        val txns: List<TransactionEntity>
    )

    val groupedTxns = remember(pagedTxns) {
        val todayJ = PersianDateHelper.todayJalali()
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayJ = PersianDateHelper.timestampToJalali(yesterdayCal.timeInMillis)

        pagedTxns.groupBy { txn ->
            val j = PersianDateHelper.timestampToJalali(txn.timestamp)
            "${j.year}/${j.month}/${j.day}"
        }.map { (_, txnsInDay) ->
            val first = txnsInDay.first()
            val jDate = PersianDateHelper.timestampToJalali(first.timestamp)
            val cal = Calendar.getInstance().apply { timeInMillis = first.timestamp }
            val nowCal = Calendar.getInstance()
            val diffDays = ((nowCal.timeInMillis - first.timestamp) / (24 * 60 * 60 * 1000L)).toInt()

            val isToday = (jDate.year == todayJ.year && jDate.month == todayJ.month && jDate.day == todayJ.day)
            val isYesterday = (jDate.year == yesterdayJ.year && jDate.month == yesterdayJ.month && jDate.day == yesterdayJ.day)

            val label = when {
                isToday -> "امروز"
                isYesterday -> "دیروز"
                diffDays in 2..6 -> "$diffDays روز قبل"
                else -> "${jDate.day} ${jDate.monthName} ${jDate.year}"
            }
            StatementDateGroup(label, txnsInDay)
        }
    }

    // Share Statement Builder
    fun shareStatement() {
        val sb = StringBuilder()
        sb.append("📋 صورت‌حساب مالی طرف‌حساب\n")
        sb.append("👤 نام: ${recipient.name}\n")
        if (recipient.handleOrPhone.isNotBlank()) sb.append("📞 شناسه/شماره: ${recipient.handleOrPhone}\n")
        sb.append("📅 تاریخ گزارش: ${PersianDateHelper.formatSolarDateTime(System.currentTimeMillis())}\n")
        sb.append("------------------------------------\n")
        sb.append("💰 ارز انتخابی: $selectedCurrencySymbol ($selectedCurrency)\n")
        if (isMasked) {
            sb.append("🔒 مبالغ این صورت‌حساب محافظت‌شده هستند.\n")
        } else {
            val totalIn = currencyTxnsChronological.filter { it.type == TransactionType.INCOME }.sumOf { it.effectiveAmount() }
            val totalOut = currencyTxnsChronological.filter { it.type == TransactionType.EXPENSE }.sumOf { it.effectiveAmount() }
            sb.append("مجموع دریافتی: ${decFormat.format(totalIn)} $selectedCurrencySymbol\n")
            sb.append("مجموع پرداختی: ${decFormat.format(totalOut)} $selectedCurrencySymbol\n")
            val absBal = kotlin.math.abs(currentNetBalance)
            val balText = when {
                currentNetBalance > 0.01 -> "طلب من از او: ${decFormat.format(absBal)} $selectedCurrencySymbol (دریافتنی)"
                currentNetBalance < -0.01 -> "بدهی من به او: ${decFormat.format(absBal)} $selectedCurrencySymbol (پرداختنی)"
                else -> "حساب کاملاً تسویه است (۰ $selectedCurrencySymbol)"
            }
            sb.append("مانده نهایی: $balText\n")
        }
        sb.append("------------------------------------\n")
        sb.append("ریز تراکنش‌ها (${displayTxns.size} مورد):\n")
        displayTxns.forEachIndexed { idx, t ->
            val typeStr = if (t.type == TransactionType.EXPENSE) "پرداخت" else "دریافت"
            val amtStr = if (isMasked) "****" else decFormat.format(t.effectiveAmount())
            val dateStr = PersianDateHelper.formatSolarDateTime(t.timestamp)
            sb.append("${idx + 1}. $typeStr: $amtStr $selectedCurrencySymbol ($dateStr)\n")
            if (!t.note.isNullOrBlank()) sb.append("   یادداشت: ${cleanTransactionNote(t.note)}\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "صورتحساب ${recipient.name}")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری صورت‌حساب"))
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("person_statement_screen")
        ) {
            // --- 1. HEADER BAR [←] نام شخص + آیکون‌های جستجو، فیلتر، اشتراک ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoLavenderSubtle)
                                    .testTag("statement_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "بازگشت",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = recipient.name,
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (recipient.handleOrPhone.isNotBlank()) recipient.handleOrPhone else "صورت‌حساب و گردش حساب",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Top Action Icons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Search Toggle
                            IconButton(
                                onClick = {
                                    isSearchActive = !isSearchActive
                                    if (!isSearchActive) searchQuery = ""
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSearchActive) BentoIndigoAccent.copy(alpha = 0.12f) else Color.Transparent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "جستجو در تراکنش‌ها",
                                    tint = if (isSearchActive) BentoIndigoAccent else BentoNavyDark,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Filter Button with Badge
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedTypeFilter != PersonStatementTypeFilter.ALL || selectedSettlementFilter != PersonStatementSettlementFilter.ALL || selectedDateRangePreset != DateRangePreset.ALL) BentoIndigoAccent.copy(alpha = 0.12f) else Color.Transparent)
                            ) {
                                val hasActiveFilters = selectedTypeFilter != PersonStatementTypeFilter.ALL ||
                                        selectedSettlementFilter != PersonStatementSettlementFilter.ALL ||
                                        selectedDateRangePreset != DateRangePreset.ALL
                                if (hasActiveFilters) {
                                    BadgedBox(badge = { Badge(containerColor = BentoIndigoAccent) }) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "فیلترها",
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "فیلترها",
                                        tint = BentoNavyDark,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // Share / Export Statement
                            IconButton(
                                onClick = { shareStatement() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "اشتراک‌گذاری صورت‌حساب",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Mask / Unmask Privacy Lock
                            if (recipient.isAmountProtected) {
                                IconButton(
                                    onClick = onRequestUnmask,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isMasked) ExpenseRed.copy(alpha = 0.1f) else IncomeGreen.copy(alpha = 0.1f))
                                ) {
                                    Icon(
                                        imageVector = if (isMasked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = if (isMasked) "مبالغ محافظت شده‌اند" else "مبالغ نمایان هستند",
                                        tint = if (isMasked) ExpenseRed else IncomeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Optional Search Bar (Toggled on Search Click)
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در عنوان، یادداشت، دسته یا مبلغ...", fontSize = 12.sp, color = TextTertiary) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "پاک کردن", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BackgroundCanvas,
                                unfocusedContainerColor = BackgroundCanvas,
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    }
                }
            }

            // --- MAIN SCROLLABLE CONTENT ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- 2. SUMMARY CARD (مانده حساب بزرگ با نماد ارز، برچسب وضعیت، و انتخاب ارز) ---
                item(key = "statement_summary_card") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BentoBorder),
                        shadowElevation = 0.5.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // --- Balance Display Box ---
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            currentNetBalance > 0.01 -> Color(0xFFF0FDF4) // Green
                                            currentNetBalance < -0.01 -> Color(0xFFFEF2F2) // Red
                                            else -> Color(0xFFF8FAFC) // Neutral Slate
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Balance Amount with Currency Symbol on the LEFT
                                val absBalance = kotlin.math.abs(currentNetBalance)
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Interactive Currency Symbol on the Left (لمس برای تغییر و تبدیل ارز)
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = BentoIndigoAccent.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { showCurrencyPickerSheet = true }
                                                .testTag("statement_currency_symbol_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = selectedCurrencySymbol,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = BentoIndigoAccent
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = "تغییر و تبدیل ارز",
                                                    tint = BentoIndigoAccent,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        if (isMasked) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onRequestUnmask() }
                                            ) {
                                                Text(
                                                    text = "****",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoNavyDark
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "محافظت شده",
                                                    tint = BentoIndigoAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        } else {
                                            val signPrefix = when {
                                                currentNetBalance > 0.01 -> "+"
                                                currentNetBalance < -0.01 -> "−"
                                                else -> ""
                                            }
                                            val balanceColor = when {
                                                currentNetBalance > 0.01 -> Color(0xFF166534)
                                                currentNetBalance < -0.01 -> Color(0xFF991B1B)
                                                else -> BentoNavyDark
                                            }

                                            if (signPrefix.isNotEmpty()) {
                                                Text(
                                                    text = "$signPrefix ",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = balanceColor
                                                )
                                            }
                                            Text(
                                                text = decFormat.format(absBalance),
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = balanceColor
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Status Badge: طلب من از او / بدهی من به او / تسویه شده
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when {
                                        currentNetBalance > 0.01 -> IncomeGreen.copy(alpha = 0.15f)
                                        currentNetBalance < -0.01 -> ExpenseRed.copy(alpha = 0.15f)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        val icon = when {
                                            currentNetBalance > 0.01 -> Icons.Default.TrendingUp
                                            currentNetBalance < -0.01 -> Icons.Default.TrendingDown
                                            else -> Icons.Default.CheckCircle
                                        }
                                        val tint = when {
                                            currentNetBalance > 0.01 -> IncomeGreen
                                            currentNetBalance < -0.01 -> ExpenseRed
                                            else -> Color(0xFF475569)
                                        }
                                        val statusText = when {
                                            currentNetBalance > 0.01 -> "طلب من از او (دریافتنی)"
                                            currentNetBalance < -0.01 -> "بدهی من به او (پرداختنی)"
                                            else -> "حساب کاملاً تسویه شده (۰)"
                                        }

                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isMasked && currentNetBalance != 0.0) "$statusText 🔒" else statusText,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 3. OPTION TOGGLE: ☐ «نمایش تراکنش‌های قبل از آخرین تسویه» ---
                item(key = "toggle_settlement_history") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBeforeLastSettlement = !showBeforeLastSettlement }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "نمایش حساب‌ها و تراکنش‌های تسویه‌شده",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (showBeforeLastSettlement)
                                        "تمام تاریخچه از جمله تراکنش‌های حساب‌های تسویه‌شده نمایش داده می‌شوند"
                                    else
                                        "در صورت تسویه بودن حساب، هیچ تراکنشی نمایش داده نمی‌شود",
                                    fontSize = 10.5.sp,
                                    color = TextSecondary
                                )
                            }

                            Checkbox(
                                checked = showBeforeLastSettlement,
                                onCheckedChange = { showBeforeLastSettlement = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BentoIndigoAccent,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }
                }

                // --- 4. TIMELINE / LEDGER SECTION HEADER ---
                item(key = "ledger_section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "گردش حساب و تراکنش‌ها",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                        }

                        Text(
                            text = "${displayTxns.size} ردیف معامله",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // --- 5. EMPTY STATES ---
                if (allPersonTxns.isEmpty()) {
                    item(key = "empty_no_txns") {
                        StatementEmptyCard(
                            icon = Icons.Default.ReceiptLong,
                            title = "هنوز تراکنشی با این شخص ثبت نشده",
                            subtitle = "می‌توانید با دکمه‌های پایین، اولین دریافت یا پرداخت را ثبت کنید."
                        )
                    }
                } else if (isAccountCurrentlySettled && !showBeforeLastSettlement) {
                    item(key = "empty_settled_hidden") {
                        StatementEmptyCard(
                            icon = Icons.Default.CheckCircle,
                            title = "حساب این شخص کاملاً تسویه است (۰)",
                            subtitle = "به دلیل تسویه بودن حساب، هیچ تراکنشی نمایش داده نمی‌شود. برای مشاهده سوابق، تیک نمایش حساب‌های تسویه‌شده را بزنید.",
                            actionText = "نمایش حساب‌ها و تراکنش‌های تسویه‌شده",
                            onAction = {
                                showBeforeLastSettlement = true
                            }
                        )
                    }
                } else if (displayTxns.isEmpty()) {
                    item(key = "empty_filtered") {
                        StatementEmptyCard(
                            icon = Icons.Default.FilterAltOff,
                            title = "تراکنشی با فیلتر انتخابی یافت نشد",
                            subtitle = "فیلترها را ریست کنید یا تیک نمایش حساب‌های تسویه‌شده را بزنید.",
                            actionText = "مشاهده تمام تاریخچه",
                            onAction = {
                                showBeforeLastSettlement = true
                                selectedTypeFilter = PersonStatementTypeFilter.ALL
                                selectedSettlementFilter = PersonStatementSettlementFilter.ALL
                                selectedDateRangePreset = DateRangePreset.ALL
                                searchQuery = ""
                            }
                        )
                    }
                } else {
                    // --- 6. TIMELINE / LEDGER ROWS (GROUPED BY DATE) ---
                    for (group in groupedTxns) {
                        // Date Group Header: امروز، دیروز، ۲ روز قبل...
                        item(key = "date_hdr_${group.dateLabel}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BentoNavyDark.copy(alpha = 0.06f)
                                ) {
                                    Text(
                                        text = group.dateLabel,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(BentoBorder)
                                )
                            }
                        }

                        // Ledger items for this date group
                        items(group.txns, key = { "statement_txn_${it.id}" }) { txn ->
                            val isReceive = txn.type == TransactionType.INCOME
                            val effAmount = txn.effectiveAmount()
                            val runningBalAfter = runningBalancesMap[txn.id] ?: 0.0

                            StatementLedgerRow(
                                txn = txn,
                                isReceive = isReceive,
                                effectiveAmount = effAmount,
                                currencySymbol = selectedCurrencySymbol,
                                runningBalance = runningBalAfter,
                                isMasked = isMasked,
                                decFormat = decFormat,
                                onClick = { selectedTxnForDetail = txn }
                            )
                        }
                    }

                    // Loading indicator for more pages
                    if (visibleLimit < displayTxns.size) {
                        item(key = "load_more_statement") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { visibleLimit += 35 }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceWhite,
                                border = BorderStroke(1.dp, BentoBorder)
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "نمایش معاملات بیشتر (${displayTxns.size - visibleLimit} مورد باقی‌مانده)...",
                                        fontSize = 11.5.sp,
                                        color = BentoIndigoAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 6.5 PREVIOUS BALANCE / CARRY-OVER CARD (حساب سابقه / مانده از قبل) ---
                if (activeFilterStartTimestamp != null && txnsAfterSettlementFilter.isNotEmpty()) {
                    item(key = "previous_balance_summary_card") {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = "حساب سابقه (مانده قبل از بازه)",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BentoLavenderSubtle
                                    ) {
                                        Text(
                                            text = "قبل از ${selectedDateRangePreset.title}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoIndigoAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = BentoBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مانده انتقالی از قبل:",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    if (isMasked) {
                                        Text(
                                            text = "****",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                    } else {
                                        val absPrev = kotlin.math.abs(previousBalance)
                                        val prevStatus = when {
                                            previousBalance > 0.01 -> "(طلب)"
                                            previousBalance < -0.01 -> "(بدهی)"
                                            else -> "(تسویه - صفر)"
                                        }
                                        val prevColor = when {
                                            previousBalance > 0.01 -> IncomeGreen
                                            previousBalance < -0.01 -> ExpenseRed
                                            else -> Color(0xFF64748B)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = "$selectedCurrencySymbol ${decFormat.format(absPrev)}",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = prevColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = prevStatus,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = prevColor
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "گردش در این بازه:",
                                        fontSize = 11.sp,
                                        color = TextTertiary
                                    )
                                    if (isMasked) {
                                        Text("****", fontSize = 11.sp, color = TextTertiary)
                                    } else {
                                        val periodChange = fullyFilteredTxns.sumOf {
                                            if (it.type == TransactionType.EXPENSE) it.effectiveAmount() else -it.effectiveAmount()
                                        }
                                        val absChange = kotlin.math.abs(periodChange)
                                        val changeSign = if (periodChange > 0.01) "+ " else if (periodChange < -0.01) "− " else ""
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "$changeSign$selectedCurrencySymbol ${decFormat.format(absChange)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (periodChange > 0.01) IncomeGreen else if (periodChange < -0.01) ExpenseRed else TextTertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Space before sticky bottom action bar
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- 7. QUICK ACTION BAR AT BOTTOM: [دریافت] [پرداخت] ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Receive Button (دریافت طلب)
                    Button(
                        onClick = {
                            viewModel?.selectRecipient(recipient)
                            viewModel?.prepareReceiveScreen()
                            if (selectedCurrency.isNotBlank()) viewModel?.setCurrency(selectedCurrency)
                            onNavigateToReceive(recipient, selectedCurrency)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("statement_receive_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دریافت ($selectedCurrencySymbol)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Pay Button (پرداخت بدهی)
                    Button(
                        onClick = {
                            viewModel?.selectRecipient(recipient)
                            viewModel?.preparePaymentScreen()
                            if (selectedCurrency.isNotBlank()) viewModel?.setCurrency(selectedCurrency)
                            onNavigateToPay(recipient, selectedCurrency)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("statement_pay_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پرداخت ($selectedCurrencySymbol)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // --- FILTER BOTTOM SHEET ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCBD5E1))
                )
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فیلترهای صورت‌حساب",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        TextButton(
                            onClick = {
                                selectedTypeFilter = PersonStatementTypeFilter.ALL
                                selectedSettlementFilter = PersonStatementSettlementFilter.ALL
                                selectedDateRangePreset = DateRangePreset.ALL
                                customStartDate = null
                                customEndDate = null
                            }
                        ) {
                            Text("پاکسازی فیلترها", fontSize = 12.sp, color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // 1. Transaction Type (همه، دریافت، پرداخت)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("نوع تراکنش:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PersonStatementTypeFilter.values().forEach { type ->
                                val isSelected = selectedTypeFilter == type
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedTypeFilter = type },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BentoNavyDark else BentoLavenderSubtle,
                                    border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = type.title,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else BentoNavyDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Settlement Status (همه، تسویه‌شده، تسویه‌نشده)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("وضعیت تسویه تراکنش:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PersonStatementSettlementFilter.values().forEach { st ->
                                val isSelected = selectedSettlementFilter == st
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedSettlementFilter = st },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BentoIndigoAccent else BentoLavenderSubtle,
                                    border = BorderStroke(1.dp, if (isSelected) BentoIndigoAccent else BentoBorder)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = st.title,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else BentoNavyDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Date Range Preset
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("بازه زمانی:", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(listOf(
                                DateRangePreset.ALL,
                                DateRangePreset.TODAY,
                                DateRangePreset.YESTERDAY,
                                DateRangePreset.THIS_WEEK,
                                DateRangePreset.THIS_MONTH,
                                DateRangePreset.LAST_MONTH,
                                DateRangePreset.CUSTOM
                            )) { preset ->
                                val isSelected = selectedDateRangePreset == preset
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BentoNavyDark else BentoLavenderSubtle,
                                    border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedDateRangePreset = preset
                                            if (preset == DateRangePreset.CUSTOM) {
                                                showCustomStartDatePicker = true
                                            }
                                        }
                                ) {
                                    Text(
                                        text = preset.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // If Custom Date Range is selected, show interactive Start & End date pickers
                        if (selectedDateRangePreset == DateRangePreset.CUSTOM) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showCustomStartDatePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = BackgroundCanvas,
                                    border = BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "از تاریخ:", fontSize = 10.5.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (customStartDate != null) PersianDateHelper.formatDate(customStartDate!!, calendarType) else "انتخاب تاریخ شروع",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showCustomEndDatePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = BackgroundCanvas,
                                    border = BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "تا تاریخ:", fontSize = 10.5.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (customEndDate != null) PersianDateHelper.formatDate(customEndDate!!, calendarType) else "انتخاب تاریخ پایان",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent)
                    ) {
                        Text("اعمال فیلترها", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // --- TRANSACTION DETAIL & EDIT SHEET ---
    if (selectedTxnForDetail != null) {
        val txn = selectedTxnForDetail!!
        LuxuryTransactionDetailAndEditBottomSheet(
            transaction = txn,
            activeCurrencies = activeCurrencies,
            accounts = accounts,
            categories = categories,
            shoppingLists = emptyList(),
            isAmountMasked = viewModel?.isTransactionAmountMasked(txn) ?: false,
            onDismiss = { selectedTxnForDetail = null },
            onUpdate = { oldTxn, newTxn, onResult ->
                if (viewModel != null) {
                    viewModel.updateTransaction(
                        oldTxn = oldTxn,
                        newTxn = newTxn,
                        onSuccess = {
                            selectedTxnForDetail = newTxn
                            onResult(true, null)
                        },
                        onError = { err -> onResult(false, err) }
                    )
                } else {
                    onResult(true, null)
                }
            },
            onDelete = { t ->
                viewModel?.deleteTransaction(t)
                selectedTxnForDetail = null
            }
        )
    }

    // --- CUSTOM DATE PICKER DIALOGS ---
    if (showCustomStartDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = customStartDate ?: System.currentTimeMillis(),
            title = "انتخاب تاریخ شروع",
            onDismiss = { showCustomStartDatePicker = false },
            onDateSelected = {
                customStartDate = it
                showCustomStartDatePicker = false
                if (customEndDate == null) {
                    showCustomEndDatePicker = true
                }
            }
        )
    }
    if (showCustomEndDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = customEndDate ?: System.currentTimeMillis(),
            title = "انتخاب تاریخ پایان",
            onDismiss = { showCustomEndDatePicker = false },
            onDateSelected = {
                customEndDate = it
                showCustomEndDatePicker = false
            }
        )
    }

    // --- CURRENCY PICKER BOTTOM SHEET ---
    if (showCurrencyPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCurrencyPickerSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCBD5E1))
                )
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "انتخاب ارز صورت‌حساب",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                        }

                        IconButton(onClick = { showCurrencyPickerSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                        }
                    }

                    Text(
                        text = "با لمس هر ارز، مانده حساب و ریز معاملات نظر به همان ارز محاسبه و نمایش داده می‌شود:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allSelectableCurrencies) { code ->
                            val isSelected = code.equals(selectedCurrency, ignoreCase = true)
                            val symbol = getCurrencySymbol(code)
                            val name = getCurrencyName(code)
                            val txnCount = allPersonTxns.count { it.effectiveCurrency().equals(code, ignoreCase = true) }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedCurrency = code
                                        showCurrencyPickerSheet = false
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) BentoLavenderSubtle else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BentoIndigoAccent else BentoBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Currency Symbol Badge
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) BentoIndigoAccent else BentoLavenderSubtle)
                                                .border(1.dp, if (isSelected) BentoIndigoAccent else BentoBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = symbol,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else BentoNavyDark
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = name,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = BentoNavyDark
                                            )
                                            Text(
                                                text = "$code • $txnCount معامله ثبت‌شده",
                                                fontSize = 11.5.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "انتخاب شده",
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Lightweight, modern Timeline/Ledger Row for Person Statement
 */
@Composable
private fun StatementLedgerRow(
    txn: TransactionEntity,
    isReceive: Boolean,
    effectiveAmount: Double,
    currencySymbol: String,
    runningBalance: Double,
    isMasked: Boolean,
    decFormat: DecimalFormat,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Section (Icon + Title + Date & Time + Impact Status)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Small Action Icon
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isReceive) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReceive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isReceive) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = if (txn.title.isNotBlank()) txn.title else if (isReceive) "دریافت وجه" else "پرداخت وجه",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = PersianDateHelper.formatSolarDateTime(txn.timestamp),
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )

                        // Impact badge (e.g. کاهش بدهی / افزایش طلب)
                        val impactText = if (isReceive) "دریافت" else "پرداخت"
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isReceive) IncomeGreen.copy(alpha = 0.08f) else ExpenseRed.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = impactText,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isReceive) IncomeGreen else ExpenseRed,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        if (txn.isSettled) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "تسویه",
                                    fontSize = 9.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    val cleanNote = cleanTransactionNote(txn.note)
                    if (!cleanNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = cleanNote,
                            fontSize = 10.5.sp,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Left Section (Amount + Running Balance after transaction)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Transaction Amount (+ for Receive, − for Pay) with symbol on the LEFT
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    if (isMasked) {
                        Text(
                            text = if (isReceive) "+ $currencySymbol ****" else "− $currencySymbol ****",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReceive) IncomeGreen else ExpenseRed
                        )
                    } else {
                        Text(
                            text = if (isReceive) "+ $currencySymbol ${decFormat.format(effectiveAmount)}" else "− $currencySymbol ${decFormat.format(effectiveAmount)}",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReceive) IncomeGreen else ExpenseRed
                        )
                    }
                }

                // Running Balance after this transaction with symbol on the LEFT
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    if (isMasked) {
                        Text(
                            text = "مانده: ****",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    } else {
                        val absBal = kotlin.math.abs(runningBalance)
                        val balSuffix = when {
                            runningBalance > 0.01 -> "(طلب)"
                            runningBalance < -0.01 -> "(بدهی)"
                            else -> "(تسویه)"
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مانده: ",
                                fontSize = 10.sp,
                                color = TextSecondary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "$currencySymbol ${decFormat.format(absBal)}",
                                    fontSize = 10.sp,
                                    color = TextSecondary.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = " $balSuffix",
                                fontSize = 10.sp,
                                color = TextSecondary.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Empty State Card
 */
@Composable
private fun StatementEmptyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BentoLavenderSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BentoIndigoAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BentoNavyDark
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = actionText, fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
