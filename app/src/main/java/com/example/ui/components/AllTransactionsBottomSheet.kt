package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.RecipientEntity
import com.example.data.local.ShoppingListEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionKind
import com.example.data.local.TransactionType
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.PersianDateHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * Filter Types for All Transactions
 */
enum class TransactionFilterType(val title: String) {
    ALL("همه"),
    EXPENSE("مصارف"),
    INCOME("عواید"),
    PAY("پرداخت به شخص"),
    RECEIVE("دریافت از شخص"),
    TRANSFER("انتقالات"),
    EXCHANGE("تبدیل اسعار")
}

/**
 * Date Range Presets using Persian / Solar Hijri Calendar
 */
enum class DateRangePreset(val title: String) {
    ALL("همه زمان‌ها"),
    TODAY("امروز"),
    YESTERDAY("دیروز"),
    THIS_WEEK("این هفته"),
    THIS_MONTH("این ماه"),
    LAST_MONTH("ماه قبل"),
    CUSTOM("بازه دلخواه")
}

/**
 * Data structure representing a grouped day of transactions
 */
data class TransactionDayGroup(
    val dateKey: String,
    val headerTitle: String,
    val weekday: String,
    val fullDateText: String,
    val timestamp: Long,
    val items: List<UnifiedTransactionItem>,
    val totalIncome: Double,
    val totalExpense: Double,
    val hasMaskedIncome: Boolean,
    val hasMaskedExpense: Boolean
)

/**
 * Completely redesigned, modern, lightweight and fast All Transactions History Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    consolidatedTransactions: List<UnifiedTransactionItem> = emptyList(),
    accounts: List<AccountCardEntity> = emptyList(),
    activeCurrencies: List<CurrencyEntity> = emptyList(),
    categories: List<CategoryEntity> = emptyList(),
    recipients: List<RecipientEntity> = emptyList(),
    currencySymbol: String = "؋",
    viewModel: FinanceViewModel? = null,
    onItemClick: ((UnifiedTransactionItem) -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onDismiss()
    }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Unmasked recipients set for reactive privacy check
    val unmaskedRecipientIds by (viewModel?.unmaskedRecipientIds?.collectAsState() ?: remember { mutableStateOf(emptySet()) })

    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf(TransactionFilterType.ALL) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var selectedRecipientId by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedCurrencyCode by remember { mutableStateOf<String?>(null) }
    var selectedDateRangePreset by remember { mutableStateOf(DateRangePreset.ALL) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }

    // Dialog & Sheet states
    var showFilterSheet by remember { mutableStateOf(false) }
    var showCustomStartDatePicker by remember { mutableStateOf(false) }
    var showCustomEndDatePicker by remember { mutableStateOf(false) }
    var recipientToAuth by remember { mutableStateOf<RecipientEntity?>(null) }

    // Internal detail sheet states (allows opening details directly in this screen)
    var internalSelectedTxn by remember { mutableStateOf<TransactionEntity?>(null) }
    var internalSelectedPaired by remember { mutableStateOf<UnifiedTransactionItem.Paired?>(null) }

    // Single Day Navigation states (وسط بالای صفحه با فلاش به دو طرف)
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDayDatePicker by remember { mutableStateOf(false) }

    val (startOfDayMillis, endOfDayMillis) = remember(selectedDateMillis) {
        val start = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        Pair(start, end)
    }

    val (dayTitle, isSelectedToday) = remember(selectedDateMillis) {
        val todayJ = PersianDateHelper.todayJalali()
        val jDate = PersianDateHelper.timestampToJalali(selectedDateMillis)
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val weekday = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> ""
        }
        val isToday = (jDate.year == todayJ.year && jDate.month == todayJ.month && jDate.day == todayJ.day)
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayJ = PersianDateHelper.timestampToJalali(yesterdayCal.timeInMillis)
        val isYesterday = (jDate.year == yesterdayJ.year && jDate.month == yesterdayJ.month && jDate.day == yesterdayJ.day)
        val title = when {
            isToday -> "امروز، ${jDate.day} ${jDate.monthName} ${jDate.year}"
            isYesterday -> "دیروز، ${jDate.day} ${jDate.monthName} ${jDate.year}"
            else -> "$weekday، ${jDate.day} ${jDate.monthName} ${jDate.year}"
        }
        Pair(title, isToday)
    }

    // Pagination / Phased loading: initial batch of 35 items
    var visibleLimit by remember { mutableIntStateOf(35) }

    // Reset pagination when date, search or filters change
    LaunchedEffect(
        selectedDateMillis,
        searchQuery,
        selectedTypeFilter,
        selectedAccountId,
        selectedRecipientId,
        selectedCategory,
        selectedCurrencyCode
    ) {
        visibleLimit = 35
    }

    // Number of active filters (excluding default ALL)
    val activeFilterCount = remember(
        selectedTypeFilter,
        selectedAccountId,
        selectedRecipientId,
        selectedCategory,
        selectedCurrencyCode
    ) {
        var count = 0
        if (selectedTypeFilter != TransactionFilterType.ALL) count++
        if (selectedAccountId != null) count++
        if (selectedRecipientId != null) count++
        if (selectedCategory != null) count++
        if (selectedCurrencyCode != null) count++
        count
    }

    // Filter transaction list strictly for the selected single day (low memory and lazy loaded)
    val filteredList = remember(
        consolidatedTransactions,
        selectedDateMillis,
        startOfDayMillis,
        endOfDayMillis,
        searchQuery,
        selectedTypeFilter,
        selectedAccountId,
        selectedRecipientId,
        selectedCategory,
        selectedCurrencyCode,
        accounts
    ) {
        consolidatedTransactions.filter { item ->
            // 1. Strictly match selected date range for this single day
            if (item.timestamp < startOfDayMillis || item.timestamp > endOfDayMillis) {
                return@filter false
            }

            // 2. Type Filter
            val matchesType = when (selectedTypeFilter) {
                TransactionFilterType.ALL -> true
                TransactionFilterType.EXPENSE -> when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.type == TransactionType.EXPENSE
                    is UnifiedTransactionItem.Paired -> item.fromTxn.type == TransactionType.EXPENSE
                }
                TransactionFilterType.INCOME -> when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.type == TransactionType.INCOME
                    is UnifiedTransactionItem.Paired -> item.toTxn.type == TransactionType.INCOME
                }
                TransactionFilterType.PAY -> when (item) {
                    is UnifiedTransactionItem.Single -> {
                        val t = item.transaction
                        t.type == TransactionType.EXPENSE &&
                                (t.recipientId != null || !t.recipientName.isNullOrBlank() || t.category.contains("پرداخت"))
                    }
                    is UnifiedTransactionItem.Paired -> item.pairType == TransferPairType.PERSON_TRANSFER && item.fromTxn.type == TransactionType.EXPENSE
                }
                TransactionFilterType.RECEIVE -> when (item) {
                    is UnifiedTransactionItem.Single -> {
                        val t = item.transaction
                        t.type == TransactionType.INCOME &&
                                (t.recipientId != null || !t.recipientName.isNullOrBlank() || t.category.contains("دریافت"))
                    }
                    is UnifiedTransactionItem.Paired -> item.pairType == TransferPairType.PERSON_TRANSFER && item.toTxn.type == TransactionType.INCOME
                }
                TransactionFilterType.TRANSFER -> when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.type == TransactionType.TRANSFER
                    is UnifiedTransactionItem.Paired -> item.pairType != TransferPairType.CURRENCY_EXCHANGE
                }
                TransactionFilterType.EXCHANGE -> when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.category == "Exchange" || item.transaction.title.contains("تبدیل")
                    is UnifiedTransactionItem.Paired -> item.pairType == TransferPairType.CURRENCY_EXCHANGE || item.pairType == TransferPairType.PERSON_EXCHANGE
                }
            }
            if (!matchesType) return@filter false

            // 3. Account Filter
            if (selectedAccountId != null) {
                val matchesAcc = when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.accountId == selectedAccountId
                    is UnifiedTransactionItem.Paired -> item.fromTxn.accountId == selectedAccountId || item.toTxn.accountId == selectedAccountId
                }
                if (!matchesAcc) return@filter false
            }

            // 4. Recipient Filter
            if (selectedRecipientId != null) {
                val matchesRec = when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.recipientId == selectedRecipientId
                    is UnifiedTransactionItem.Paired -> item.fromTxn.recipientId == selectedRecipientId || item.toTxn.recipientId == selectedRecipientId
                }
                if (!matchesRec) return@filter false
            }

            // 5. Category Filter
            if (selectedCategory != null) {
                val matchesCat = when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.category.equals(selectedCategory, ignoreCase = true)
                    is UnifiedTransactionItem.Paired -> item.fromTxn.category.equals(selectedCategory, ignoreCase = true) || item.toTxn.category.equals(selectedCategory, ignoreCase = true)
                }
                if (!matchesCat) return@filter false
            }

            // 6. Currency Filter
            if (selectedCurrencyCode != null) {
                val matchesCurr = when (item) {
                    is UnifiedTransactionItem.Single -> item.transaction.currencyCode.equals(selectedCurrencyCode, ignoreCase = true)
                    is UnifiedTransactionItem.Paired -> item.fromTxn.currencyCode.equals(selectedCurrencyCode, ignoreCase = true) || item.toTxn.currencyCode.equals(selectedCurrencyCode, ignoreCase = true)
                }
                if (!matchesCurr) return@filter false
            }

            // 7. Search Query Filter
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim()
                val numQ = q.filter { it.isDigit() || it == '.' }

                fun checkTxn(t: TransactionEntity): Boolean {
                    if (t.title.contains(q, ignoreCase = true)) return true
                    if (t.category.contains(q, ignoreCase = true)) return true
                    if (t.recipientName?.contains(q, ignoreCase = true) == true) return true
                    if (t.currencyCode.contains(q, ignoreCase = true)) return true
                    if (t.currencySymbol.contains(q)) return true
                    if (!t.note.isNullOrBlank() && t.note.contains(q, ignoreCase = true)) return true
                    if (numQ.isNotEmpty() && t.amount.toLong().toString().contains(numQ)) return true
                    if (t.amount.toString().contains(q)) return true

                    // Account name check
                    if (t.accountId == 0L && ("صندوق" in q || "نقدی" in q || "نقد" in q)) return true
                    val acc = accounts.find { it.id == t.accountId }
                    if (acc != null && acc.name.contains(q, ignoreCase = true)) return true
                    return false
                }

                val matchesSearch = when (item) {
                    is UnifiedTransactionItem.Single -> checkTxn(item.transaction)
                    is UnifiedTransactionItem.Paired -> checkTxn(item.fromTxn) || checkTxn(item.toTxn)
                }
                if (!matchesSearch) return@filter false
            }

            true
        }
    }

    // Paged items for lazy loading
    val pagedItems = remember(filteredList, visibleLimit) {
        filteredList.take(visibleLimit)
    }

    // Group paged transactions by Solar Hijri day
    val groupedDays = remember(pagedItems, unmaskedRecipientIds, viewModel, currencySymbol) {
        groupTransactionsByDay(pagedItems, viewModel, currencySymbol)
    }

    val listState = rememberLazyListState()

    // Smooth incremental loading as user reaches the end of the list
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 3 && visibleLimit < filteredList.size
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            visibleLimit += 35
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("all_transactions_full_screen")
        ) {
            // --- TOP APP BAR & SEARCH SECTION ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Header Bar (Back, Title, Count, Close)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BentoLavenderSubtle)
                                    .testTag("all_transactions_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "بازگشت",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BentoLavenderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "تاریخچه کامل تراکنش‌ها",
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (filteredList.size == consolidatedTransactions.size)
                                        "${filteredList.size} معامله ثبت‌شده"
                                    else
                                        "${filteredList.size} معامله از مجموع ${consolidatedTransactions.size}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BentoNavyDark.copy(alpha = 0.05f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Input with Filter Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "جستجو در عنوان، دسته‌بندی، شخص، حساب یا مبلغ...",
                                    fontSize = 11.5.sp,
                                    color = TextTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "پاک کردن جستجو",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("search_all_transactions_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedContainerColor = BackgroundCanvas,
                                unfocusedContainerColor = BackgroundCanvas
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        // Filter Button with Badge
                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showFilterSheet = true }
                                .testTag("filter_all_transactions_btn"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (activeFilterCount > 0) BentoIndigoAccent else BentoLavenderSubtle,
                            border = BorderStroke(1.dp, if (activeFilterCount > 0) BentoIndigoAccent else BentoBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (activeFilterCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = ExpenseRed,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = activeFilterCount.toString(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "فیلترها",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "فیلترها",
                                        tint = BentoNavyDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- DAY NAVIGATOR (وسط بالای صفحه با فلاش به دو طرف برای پیمایش بین یک روز به قبل و بعد) ---
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoLavenderSubtle,
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // فلاش روز قبل (پیمایش به روز قبل)
                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = selectedDateMillis
                                        add(Calendar.DAY_OF_YEAR, -1)
                                    }
                                    selectedDateMillis = cal.timeInMillis
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceWhite)
                                    .testTag("prev_day_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "روز قبل",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // تاریخ به وسط بالای صفحه و با لمس کردن خود تاریخ، انتخاب تاریخ
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDayDatePicker = true }
                                    .background(SurfaceWhite)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("select_day_button"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = dayTitle,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "انتخاب تقویم",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // فلاش روز بعد (پیمایش به روز بعد)
                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = selectedDateMillis
                                        add(Calendar.DAY_OF_YEAR, 1)
                                    }
                                    selectedDateMillis = cal.timeInMillis
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceWhite)
                                    .testTag("next_day_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "روز بعد",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    if (!isSelectedToday) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BentoIndigoAccent.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedDateMillis = System.currentTimeMillis() }
                            ) {
                                Text(
                                    text = "↺ بازگشت به تاریخ امروز",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Horizontal Type Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TransactionFilterType.values()) { type ->
                            QuickChipItem(
                                label = type.title,
                                isSelected = selectedTypeFilter == type,
                                onClick = { selectedTypeFilter = type }
                            )
                        }
                    }

                    // Active Filters Reminder & Quick Clear Chips
                    if (activeFilterCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (selectedDateRangePreset != DateRangePreset.ALL) {
                                item {
                                    ActiveFilterBadgeChip(
                                        label = "بازه: ${selectedDateRangePreset.title}",
                                        onRemove = {
                                            selectedDateRangePreset = DateRangePreset.ALL
                                            customStartDate = null
                                            customEndDate = null
                                        }
                                    )
                                }
                            }
                            if (selectedAccountId != null) {
                                val accName = if (selectedAccountId == 0L) "صندوق نقدی" else accounts.find { it.id == selectedAccountId }?.name ?: "حساب"
                                item {
                                    ActiveFilterBadgeChip(
                                        label = "حساب: $accName",
                                        onRemove = { selectedAccountId = null }
                                    )
                                }
                            }
                            if (selectedRecipientId != null) {
                                val recName = recipients.find { it.id == selectedRecipientId }?.name ?: "طرف حساب"
                                item {
                                    ActiveFilterBadgeChip(
                                        label = "شخص: $recName",
                                        onRemove = { selectedRecipientId = null }
                                    )
                                }
                            }
                            if (selectedCategory != null) {
                                item {
                                    ActiveFilterBadgeChip(
                                        label = "دسته: $selectedCategory",
                                        onRemove = { selectedCategory = null }
                                    )
                                }
                            }
                            if (selectedCurrencyCode != null) {
                                item {
                                    ActiveFilterBadgeChip(
                                        label = "ارز: $selectedCurrencyCode",
                                        onRemove = { selectedCurrencyCode = null }
                                    )
                                }
                            }
                            item {
                                Text(
                                    text = "پاکسازی همه",
                                    fontSize = 11.sp,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedTypeFilter = TransactionFilterType.ALL
                                            selectedAccountId = null
                                            selectedRecipientId = null
                                            selectedCategory = null
                                            selectedCurrencyCode = null
                                            selectedDateRangePreset = DateRangePreset.ALL
                                            customStartDate = null
                                            customEndDate = null
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- MAIN LIST CONTENT ---
            if (consolidatedTransactions.isEmpty()) {
                // Entire database is empty
                EmptyStateCard(
                    icon = Icons.Default.ReceiptLong,
                    title = "هنوز تراکنشی ثبت نشده است",
                    subtitle = "اولین معامله خود را از صفحه اصلی یا دکمه‌های سریع ثبت کنید."
                )
            } else if (filteredList.isEmpty()) {
                // Filters or search gave no results for this day
                EmptyStateCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "در این تاریخ تراکنشی ثبت نشده است",
                    subtitle = if (!isSelectedToday) "با دکمه‌های فلش بالا می‌توانید روزهای قبل یا بعد را مشاهده کنید یا به امروز بازگردید." else "هنوز در تاریخ امروز معامله‌ای ثبت نشده است.",
                    actionLabel = if (!isSelectedToday) "بازگشت به تاریخ امروز" else if (searchQuery.isNotEmpty() || activeFilterCount > 0) "پاکسازی فیلترها" else null,
                    onAction = {
                        if (!isSelectedToday) {
                            selectedDateMillis = System.currentTimeMillis()
                        } else {
                            searchQuery = ""
                            selectedTypeFilter = TransactionFilterType.ALL
                            selectedAccountId = null
                            selectedRecipientId = null
                            selectedCategory = null
                            selectedCurrencyCode = null
                        }
                    }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (group in groupedDays) {
                        // Day Header
                        item(key = "header_${group.dateKey}") {
                            CompactDayHeader(
                                group = group,
                                currencySymbol = currencySymbol
                            )
                        }

                        // Day Items (using stable key based on transaction id)
                        items(group.items, key = { "item_${it.id}" }) { item ->
                            val isSingle = item is UnifiedTransactionItem.Single
                            val isItemMasked = viewModel?.isUnifiedItemMasked(item) ?: false
                            val maskedRec = if (isItemMasked && viewModel != null) viewModel.getMaskedRecipientForUnifiedItem(item) else null

                            CompactTransactionHistoryRow(
                                item = item,
                                accounts = accounts,
                                currencySymbol = currencySymbol,
                                isAmountMasked = isItemMasked,
                                onLockClick = {
                                    if (maskedRec != null) {
                                        recipientToAuth = maskedRec
                                    }
                                },
                                onClick = {
                                    if (onItemClick != null) {
                                        onItemClick(item)
                                    } else {
                                        when (item) {
                                            is UnifiedTransactionItem.Single -> internalSelectedTxn = item.transaction
                                            is UnifiedTransactionItem.Paired -> internalSelectedPaired = item
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Skeleton / Loading indicator when loading more pages
                    if (visibleLimit < filteredList.size) {
                        item(key = "loading_skeleton") {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { visibleLimit += 35 },
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceWhite.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, BentoBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "نمایش معاملات بیشتر (${filteredList.size - visibleLimit} باقی‌مانده)...",
                                        fontSize = 11.5.sp,
                                        color = BentoIndigoAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FILTER BOTTOM SHEET ---
    if (showFilterSheet) {
        AllTransactionsFilterBottomSheet(
            selectedType = selectedTypeFilter,
            selectedAccountId = selectedAccountId,
            selectedRecipientId = selectedRecipientId,
            selectedCategory = selectedCategory,
            selectedCurrencyCode = selectedCurrencyCode,
            accounts = accounts,
            recipients = recipients,
            categories = categories,
            currencies = activeCurrencies,
            onApply = { type, accId, recId, cat, curr ->
                selectedTypeFilter = type
                selectedAccountId = accId
                selectedRecipientId = recId
                selectedCategory = cat
                selectedCurrencyCode = curr
                showFilterSheet = false
            },
            onReset = {
                selectedTypeFilter = TransactionFilterType.ALL
                selectedAccountId = null
                selectedRecipientId = null
                selectedCategory = null
                selectedCurrencyCode = null
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    // --- SOLAR DATE PICKERS FOR DAY NAVIGATION AND CUSTOM DATE RANGE ---
    if (showDayDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = selectedDateMillis,
            title = "انتخاب تاریخ تراکنش‌ها",
            onDismiss = { showDayDatePicker = false },
            onDateSelected = { ts ->
                selectedDateMillis = ts
                showDayDatePicker = false
            }
        )
    }

    if (showCustomStartDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = customStartDate ?: System.currentTimeMillis(),
            title = "انتخاب تاریخ شروع (از تاریخ)",
            onDismiss = { showCustomStartDatePicker = false },
            onDateSelected = { ts ->
                customStartDate = ts
                showCustomStartDatePicker = false
            }
        )
    }

    if (showCustomEndDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = customEndDate ?: System.currentTimeMillis(),
            title = "انتخاب تاریخ پایان (تا تاریخ)",
            onDismiss = { showCustomEndDatePicker = false },
            onDateSelected = { ts ->
                customEndDate = ts
                showCustomEndDatePicker = false
            }
        )
    }

    // --- RECIPIENT AUTHENTICATION DIALOG (When tapping a locked transaction) ---
    recipientToAuth?.let { rec ->
        if (viewModel != null) {
            RecipientAuthDialog(
                recipientName = rec.name,
                viewModel = viewModel,
                promptTitle = "احراز هویت برای نمایش مبالغ",
                promptSubtitle = "با وارد کردن رمز، مبالغ مربوط به ${rec.name} نمایان می‌شوند.",
                onAuthenticated = {
                    viewModel.unmaskRecipient(rec.id)
                    recipientToAuth = null
                },
                onNavigateToSettings = {
                    recipientToAuth = null
                },
                onDismiss = { recipientToAuth = null }
            )
        }
    }

    // --- INTERNAL DETAIL & EDIT SHEETS (When tapped in full-screen mode) ---
    internalSelectedTxn?.let { txn ->
        if (viewModel != null) {
            LuxuryTransactionDetailAndEditBottomSheet(
                transaction = txn,
                activeCurrencies = activeCurrencies,
                accounts = accounts,
                categories = categories,
                shoppingLists = emptyList(),
                isAmountMasked = viewModel.isTransactionAmountMasked(txn),
                onDismiss = { internalSelectedTxn = null },
                onUpdate = { oldTxn, newTxn, onResult ->
                    viewModel.updateTransaction(
                        oldTxn = oldTxn,
                        newTxn = newTxn,
                        onSuccess = {
                            internalSelectedTxn = newTxn
                            onResult(true, null)
                        },
                        onError = { err -> onResult(false, err) }
                    )
                },
                onDelete = { t ->
                    viewModel.deleteTransaction(t)
                    internalSelectedTxn = null
                }
            )
        }
    }

    internalSelectedPaired?.let { pairedItem ->
        if (viewModel != null) {
            LuxuryPairedTransactionDetailBottomSheet(
                item = pairedItem,
                accounts = accounts,
                isFromAmountMasked = viewModel.isTransactionAmountMasked(pairedItem.fromTxn),
                isToAmountMasked = viewModel.isTransactionAmountMasked(pairedItem.toTxn),
                onDismiss = { internalSelectedPaired = null },
                onEdit = {
                    internalSelectedPaired = null
                },
                onDelete = {
                    viewModel.deleteTransaction(pairedItem.fromTxn)
                    internalSelectedPaired = null
                }
            )
        }
    }
}

/**
 * Compact, lightweight Day Header showing date, count, and daily income / expense
 */
@Composable
private fun CompactDayHeader(
    group: TransactionDayGroup,
    currencySymbol: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        shape = RoundedCornerShape(10.dp),
        color = BackgroundCanvas,
        border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right: Date & Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = BentoNavyDark,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = group.headerTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoNavyDark
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BentoLavenderSubtle
                ) {
                    Text(
                        text = "${group.items.size} معامله",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoIndigoAccent,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Left: Daily Totals (Privacy-safe: masks if any transaction in day is protected)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (group.totalIncome > 0.0 || group.hasMaskedIncome) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IncomeGreenBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = if (group.hasMaskedIncome) "+ $currencySymbol ••••" else "+ $currencySymbol ${formatAmountCompact(group.totalIncome)}",
                                    color = IncomeGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (group.hasMaskedIncome) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }

                if (group.totalExpense > 0.0 || group.hasMaskedExpense) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ExpenseRedBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = if (group.hasMaskedExpense) "- $currencySymbol ••••" else "- $currencySymbol ${formatAmountCompact(group.totalExpense)}",
                                    color = ExpenseRed,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (group.hasMaskedExpense) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact, lightweight Row for individual transactions (Single or Paired)
 */
@Composable
private fun CompactTransactionHistoryRow(
    item: UnifiedTransactionItem,
    accounts: List<AccountCardEntity>,
    currencySymbol: String,
    isAmountMasked: Boolean,
    onLockClick: () -> Unit,
    onClick: () -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    val minute = cal.get(Calendar.MINUTE)
    val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)

    when (item) {
        is UnifiedTransactionItem.Single -> {
            val txn = item.transaction
            val isIncome = txn.type == TransactionType.INCOME
            val isExpense = txn.type == TransactionType.EXPENSE
            val isTransfer = txn.type == TransactionType.TRANSFER

            val isPayToPerson = isExpense && (!txn.recipientName.isNullOrBlank() || txn.recipientId != null)
            val isReceiveFromPerson = isIncome && (!txn.recipientName.isNullOrBlank() || txn.recipientId != null)

            val (icon, iconBg, iconTint) = when {
                isPayToPerson -> Triple(Icons.Default.TrendingDown, Color(0xFFFFFBEB), Color(0xFFD97706))
                isReceiveFromPerson -> Triple(Icons.Default.TrendingUp, Color(0xFFF0FDF4), Color(0xFF0D9488))
                isIncome -> Triple(Icons.Default.TrendingUp, IncomeGreenBg, IncomeGreen)
                isExpense -> Triple(Icons.Default.TrendingDown, ExpenseRedBg, ExpenseRed)
                isTransfer -> Triple(Icons.Default.SwapHoriz, BentoLavenderSubtle, BentoIndigoAccent)
                else -> Triple(Icons.Default.Payments, BentoLavenderSubtle, BentoIndigoAccent)
            }

            val matchedAccount = if (txn.accountId == 0L) "صندوق نقدی" else accounts.find { it.id == txn.accountId }?.name

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick() }
                    .testTag("transaction_row_${txn.id}"),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right: Icon + Title + Meta
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = txn.title,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )

                                if (txn.category.isNotBlank() && txn.category != "General") {
                                    Text(text = "•", fontSize = 9.sp, color = TextTertiary)
                                    Text(
                                        text = txn.category,
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (!txn.recipientName.isNullOrBlank()) {
                                    Text(text = "•", fontSize = 9.sp, color = TextTertiary)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = txn.recipientName!!,
                                            fontSize = 10.sp,
                                            color = BentoIndigoAccent,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (matchedAccount != null) {
                                    Text(text = "•", fontSize = 9.sp, color = TextTertiary)
                                    Text(
                                        text = matchedAccount,
                                        fontSize = 9.5.sp,
                                        color = TextTertiary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Left: Amount & Currency (in LTR)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isAmountMasked) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onLockClick() }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "••••••",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoAccent
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "قفل شده",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else {
                            val sign = if (isIncome) "+" else if (isExpense) "-" else ""
                            val amountColor = if (isIncome) IncomeGreen else if (isExpense) ExpenseRed else BentoNavyDark

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "$sign${txn.currencySymbol} ${formatAmountCompact(txn.amount)}",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor
                                )
                            }
                        }

                        // Ledger secondary amount if present
                        if (!isAmountMasked && !txn.ledgerCurrencyCode.isNullOrBlank() && txn.ledgerAmount != null && txn.ledgerAmount > 0.0) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "${txn.ledgerCurrencySymbol ?: txn.ledgerCurrencyCode} ${formatAmountCompact(txn.ledgerAmount!!)}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
        is UnifiedTransactionItem.Paired -> {
            val fromTxn = item.fromTxn
            val toTxn = item.toTxn
            val pairType = item.pairType

            val (icon, iconBg, iconTint, titleText) = when (pairType) {
                TransferPairType.CURRENCY_EXCHANGE -> Triple(
                    Icons.Default.CurrencyExchange,
                    IncomeGreenBg,
                    IncomeGreen
                ).let { Tuple4(it.first, it.second, it.third, "تبدیل ${fromTxn.currencyCode} به ${toTxn.currencyCode}") }

                TransferPairType.ACCOUNT_TRANSFER -> {
                    val fromAcc = if (fromTxn.accountId == 0L) "نقد" else accounts.find { it.id == fromTxn.accountId }?.name ?: "مبدأ"
                    val toAcc = if (toTxn.accountId == 0L) "نقد" else accounts.find { it.id == toTxn.accountId }?.name ?: "مقصد"
                    Tuple4(Icons.Default.SwapHoriz, BentoLavenderSubtle, BentoIndigoAccent, "انتقال: $fromAcc ➔ $toAcc")
                }
                TransferPairType.CASH_CARD_TRANSFER -> Tuple4(Icons.Default.Payments, BentoLavenderSubtle, BentoIndigoAccent, "انتقال نقد و کارت")
                TransferPairType.PERSON_TRANSFER -> Tuple4(Icons.Default.Person, Color(0xFFFFFBEB), Color(0xFFD97706), "انتقال حساب اشخاص")
                TransferPairType.PERSON_EXCHANGE -> Tuple4(Icons.Default.CurrencyExchange, Color(0xFFFDF4FF), Color(0xFFA855F7), "تبدیل اسعار حساب شخص")
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onClick() }
                    .testTag("paired_row_${item.id}"),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BentoBorder.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right: Icon & Title
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = titleText,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                                Text(text = "•", fontSize = 9.sp, color = TextTertiary)
                                Text(
                                    text = if (pairType == TransferPairType.CURRENCY_EXCHANGE) "صرافی" else "انتقال داخلی",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Left: Amount flow
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isAmountMasked) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onLockClick() }
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "••••••",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoAccent
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "قفل شده",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "-${fromTxn.currencySymbol} ${formatAmountCompact(fromTxn.amount)}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                    Text(
                                        text = "+${toTxn.currencySymbol} ${formatAmountCompact(toTxn.amount)}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
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

/**
 * Filter Bottom Sheet for deep and multi-criteria filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllTransactionsFilterBottomSheet(
    selectedType: TransactionFilterType,
    selectedAccountId: Long?,
    selectedRecipientId: Long?,
    selectedCategory: String?,
    selectedCurrencyCode: String?,
    accounts: List<AccountCardEntity>,
    recipients: List<RecipientEntity>,
    categories: List<CategoryEntity>,
    currencies: List<CurrencyEntity>,
    onApply: (
        type: TransactionFilterType,
        accountId: Long?,
        recipientId: Long?,
        category: String?,
        currencyCode: String?
    ) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var tempType by remember { mutableStateOf(selectedType) }
    var tempAccountId by remember { mutableStateOf(selectedAccountId) }
    var tempRecipientId by remember { mutableStateOf(selectedRecipientId) }
    var tempCategory by remember { mutableStateOf(selectedCategory) }
    var tempCurrencyCode by remember { mutableStateOf(selectedCurrencyCode) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "فیلتر پیشرفته معاملات",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    Text(
                        text = "پاکسازی",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                tempType = TransactionFilterType.ALL
                                tempAccountId = null
                                tempRecipientId = null
                                tempCategory = null
                                tempCurrencyCode = null
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 1. Transaction Type
                FilterSectionHeader("نوع تراکنش")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TransactionFilterType.values()) { type ->
                        FilterChoiceChip(
                            label = type.title,
                            isSelected = tempType == type,
                            onClick = { tempType = type }
                        )
                    }
                }

                // 3. Accounts / Cards
                if (accounts.isNotEmpty()) {
                    FilterSectionHeader("حساب یا کارت بانکی")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChoiceChip(
                                label = "همه حساب‌ها",
                                isSelected = tempAccountId == null,
                                onClick = { tempAccountId = null }
                            )
                        }
                        item {
                            FilterChoiceChip(
                                label = "صندوق نقدی",
                                isSelected = tempAccountId == 0L,
                                onClick = { tempAccountId = if (tempAccountId == 0L) null else 0L }
                            )
                        }
                        items(accounts) { acc ->
                            FilterChoiceChip(
                                label = "${acc.name} (${acc.currencySymbol})",
                                isSelected = tempAccountId == acc.id,
                                onClick = { tempAccountId = if (tempAccountId == acc.id) null else acc.id }
                            )
                        }
                    }
                }

                // 4. Person / Recipients
                if (recipients.isNotEmpty()) {
                    FilterSectionHeader("شخص / طرف حساب")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChoiceChip(
                                label = "همه اشخاص",
                                isSelected = tempRecipientId == null,
                                onClick = { tempRecipientId = null }
                            )
                        }
                        items(recipients) { rec ->
                            FilterChoiceChip(
                                label = rec.name,
                                isSelected = tempRecipientId == rec.id,
                                onClick = { tempRecipientId = if (tempRecipientId == rec.id) null else rec.id }
                            )
                        }
                    }
                }

                // 5. Categories
                if (categories.isNotEmpty()) {
                    FilterSectionHeader("دسته‌بندی")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChoiceChip(
                                label = "همه دسته‌ها",
                                isSelected = tempCategory == null,
                                onClick = { tempCategory = null }
                            )
                        }
                        items(categories) { cat ->
                            FilterChoiceChip(
                                label = cat.name,
                                isSelected = tempCategory.equals(cat.name, ignoreCase = true),
                                onClick = {
                                    tempCategory = if (tempCategory.equals(cat.name, ignoreCase = true)) null else cat.name
                                }
                            )
                        }
                    }
                }

                // 6. Currencies
                if (currencies.size > 1) {
                    FilterSectionHeader("واحد پولی")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChoiceChip(
                                label = "همه ارزها",
                                isSelected = tempCurrencyCode == null,
                                onClick = { tempCurrencyCode = null }
                            )
                        }
                        items(currencies) { curr ->
                            FilterChoiceChip(
                                label = "${curr.code} (${curr.symbol})",
                                isSelected = tempCurrencyCode.equals(curr.code, ignoreCase = true),
                                onClick = {
                                    tempCurrencyCode = if (tempCurrencyCode.equals(curr.code, ignoreCase = true)) null else curr.code
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onApply(
                                tempType,
                                tempAccountId,
                                tempRecipientId,
                                tempCategory,
                                tempCurrencyCode
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent)
                    ) {
                        Text("اعمال فیلترها", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text("حذف فیلترها", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Bold,
        color = BentoNavyDark,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun FilterChoiceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BentoNavyDark else BackgroundCanvas,
        border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else BentoNavyDark,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun QuickChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) BentoNavyDark else BentoLavenderSubtle,
        border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else BentoNavyDark,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun ActiveFilterBadgeChip(
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BentoLavenderSubtle,
        border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoIndigoAccent
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "حذف فیلتر",
                tint = BentoIndigoAccent,
                modifier = Modifier
                    .size(13.dp)
                    .clip(CircleShape)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BentoLavenderSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BentoIndigoAccent,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BentoNavyDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent)
                ) {
                    Text(text = actionLabel, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

/**
 * Checks whether an item timestamp falls into the requested DateRangePreset
 */
fun isItemInDateRange(
    timestamp: Long,
    preset: DateRangePreset,
    customStart: Long?,
    customEnd: Long?
): Boolean {
    val now = System.currentTimeMillis()
    return when (preset) {
        DateRangePreset.ALL -> true
        DateRangePreset.TODAY -> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            timestamp >= cal.timeInMillis
        }
        DateRangePreset.YESTERDAY -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            timestamp in startCal.timeInMillis..endCal.timeInMillis
        }
        DateRangePreset.THIS_WEEK -> {
            val cal = Calendar.getInstance().apply {
                timeInMillis = now
                val dow = get(Calendar.DAY_OF_WEEK)
                val daysSinceSat = if (dow == Calendar.SATURDAY) 0 else dow
                add(Calendar.DAY_OF_YEAR, -daysSinceSat)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            timestamp >= cal.timeInMillis
        }
        DateRangePreset.THIS_MONTH -> {
            val startOfMonth = PersianDateHelper.getStartOfCurrentMonth()
            val endOfMonth = PersianDateHelper.getEndOfCurrentMonth()
            timestamp in startOfMonth..endOfMonth
        }
        DateRangePreset.LAST_MONTH -> {
            val todayJ = PersianDateHelper.todayJalali()
            val (prevY, prevM) = if (todayJ.month == 1) Pair(todayJ.year - 1, 12) else Pair(todayJ.year, todayJ.month - 1)
            val daysInPrevM = PersianDateHelper.getDaysInJalaliMonth(prevY, prevM)
            val startOfPrev = PersianDateHelper.jalaliToTimestamp(prevY, prevM, 1, 0, 0)
            val endOfPrev = PersianDateHelper.jalaliToTimestamp(prevY, prevM, daysInPrevM, 23, 59) + 59999L
            timestamp in startOfPrev..endOfPrev
        }
        DateRangePreset.CUSTOM -> {
            val start = customStart ?: Long.MIN_VALUE
            val end = customEnd?.let {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = it
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                cal.timeInMillis
            } ?: Long.MAX_VALUE
            timestamp in start..end
        }
    }
}

/**
 * Groups items into daily buckets with Solar Hijri headers and cashflow totals
 */
fun groupTransactionsByDay(
    items: List<UnifiedTransactionItem>,
    viewModel: FinanceViewModel?,
    currencySymbol: String
): List<TransactionDayGroup> {
    val todayJ = PersianDateHelper.todayJalali()

    val yesterdayCal = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val yesterdayJ = PersianDateHelper.timestampToJalali(yesterdayCal.timeInMillis)

    val grouped = items.groupBy { item ->
        val j = PersianDateHelper.timestampToJalali(item.timestamp)
        "${j.year}/${j.month}/${j.day}"
    }

    return grouped.map { (dateKey, dayItems) ->
        val firstItem = dayItems.first()
        val jDate = PersianDateHelper.timestampToJalali(firstItem.timestamp)

        val cal = Calendar.getInstance().apply { timeInMillis = firstItem.timestamp }
        val weekday = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "شنبه"
            Calendar.SUNDAY -> "یکشنبه"
            Calendar.MONDAY -> "دوشنبه"
            Calendar.TUESDAY -> "سه‌شنبه"
            Calendar.WEDNESDAY -> "چهارشنبه"
            Calendar.THURSDAY -> "پنج‌شنبه"
            Calendar.FRIDAY -> "جمعه"
            else -> ""
        }

        val isToday = (jDate.year == todayJ.year && jDate.month == todayJ.month && jDate.day == todayJ.day)
        val isYesterday = (jDate.year == yesterdayJ.year && jDate.month == yesterdayJ.month && jDate.day == yesterdayJ.day)

        val fullDateText = "${jDate.day} ${jDate.monthName} ${jDate.year}"
        val headerTitle = when {
            isToday -> "امروز، ${jDate.day} ${jDate.monthName}"
            isYesterday -> "دیروز، ${jDate.day} ${jDate.monthName}"
            else -> "$weekday، ${jDate.day} ${jDate.monthName} ${jDate.year}"
        }

        var dayIncome = 0.0
        var dayExpense = 0.0
        var maskedIncome = false
        var maskedExpense = false

        for (item in dayItems) {
            when (item) {
                is UnifiedTransactionItem.Single -> {
                    val t = item.transaction
                    val isMasked = viewModel?.isTransactionAmountMasked(t) == true
                    when (t.type) {
                        TransactionType.INCOME -> {
                            if (isMasked) maskedIncome = true else dayIncome += t.amount
                        }
                        TransactionType.EXPENSE -> {
                            if (isMasked) maskedExpense = true else dayExpense += t.amount
                        }
                        TransactionType.TRANSFER -> {
                            // Balance-neutral
                        }
                    }
                }
                is UnifiedTransactionItem.Paired -> {
                    val fromMasked = viewModel?.isTransactionAmountMasked(item.fromTxn) == true
                    val toMasked = viewModel?.isTransactionAmountMasked(item.toTxn) == true
                    if (item.pairType == TransferPairType.PERSON_TRANSFER || item.pairType == TransferPairType.PERSON_EXCHANGE) {
                        if (item.fromTxn.type == TransactionType.EXPENSE) {
                            if (fromMasked) maskedExpense = true else dayExpense += item.fromTxn.amount
                        }
                        if (item.toTxn.type == TransactionType.INCOME) {
                            if (toMasked) maskedIncome = true else dayIncome += item.toTxn.amount
                        }
                    }
                }
            }
        }

        TransactionDayGroup(
            dateKey = dateKey,
            headerTitle = headerTitle,
            weekday = weekday,
            fullDateText = fullDateText,
            timestamp = firstItem.timestamp,
            items = dayItems.sortedByDescending { it.timestamp },
            totalIncome = dayIncome,
            totalExpense = dayExpense,
            hasMaskedIncome = maskedIncome,
            hasMaskedExpense = maskedExpense
        )
    }.sortedByDescending { it.timestamp }
}

/**
 * Compact number formatting helper
 */
private fun formatAmountCompact(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", amount)
    } else {
        String.format(Locale.US, "%,.2f", amount).trimEnd('0').trimEnd('.')
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Backward compatibility alias for AllTransactionsScreen
 */
@Composable
fun AllTransactionsBottomSheet(
    consolidatedTransactions: List<UnifiedTransactionItem>,
    accounts: List<AccountCardEntity> = emptyList(),
    activeCurrencies: List<CurrencyEntity> = emptyList(),
    currencySymbol: String = "؋",
    onItemClick: (UnifiedTransactionItem) -> Unit,
    onDismiss: () -> Unit
) {
    AllTransactionsScreen(
        consolidatedTransactions = consolidatedTransactions,
        accounts = accounts,
        activeCurrencies = activeCurrencies,
        currencySymbol = currencySymbol,
        onItemClick = onItemClick,
        onDismiss = onDismiss
    )
}
