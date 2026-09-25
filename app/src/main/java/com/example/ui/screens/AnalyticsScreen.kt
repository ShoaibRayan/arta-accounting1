package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AccountCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.ui.components.CategoryBudgetBentoCard
import com.example.ui.components.LuxuryTransactionDetailAndEditBottomSheet
import com.example.ui.components.OverallBudgetRingCard
import com.example.ui.components.TransactionRowItem
import com.example.ui.components.formatCategoryDari
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel

data class CategoryBudgetItem(
    val categoryId: Long,
    val category: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color,
    val limit: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToShoppingLists: ((Long?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val spending by viewModel.currentMonthSpending.collectAsStateWithLifecycle()
    val income by viewModel.currentMonthIncome.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val dbBudgets by viewModel.budgets.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val activeCurrencies by viewModel.activeCurrencies.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val shoppingLists by viewModel.shoppingLists.collectAsStateWithLifecycle()
    val selectedCalendarType by viewModel.selectedCalendarType.collectAsStateWithLifecycle()
    val baseCurr = activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull()

    val startOfMonth = remember(selectedCalendarType) {
        com.example.util.PersianDateHelper.getStartOfCurrentMonth(selectedCalendarType)
    }
    val endOfMonth = remember(selectedCalendarType) {
        com.example.util.PersianDateHelper.getEndOfCurrentMonth(selectedCalendarType)
    }
    val currentMonthName = remember(selectedCalendarType) {
        com.example.util.PersianDateHelper.getCurrentMonthName(selectedCalendarType)
    }

    var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }

    // Memoized map of spent amount per category in base currency for CURRENT CALENDAR MONTH
    val categorySpentMap = remember(transactions, activeCurrencies, baseCurr, startOfMonth, endOfMonth) {
        val rates = activeCurrencies.associate { it.code.uppercase() to it.exchangeRateToBase }
        val map = mutableMapOf<Long, Double>()
        for (txn in transactions) {
            if (txn.type != TransactionType.EXPENSE ||
                txn.timestamp !in startOfMonth..endOfMonth ||
                !txn.recipientName.isNullOrBlank() ||
                txn.category == "Exchange" ||
                txn.category == "PersonExchange" ||
                txn.category == "PersonDebt" ||
                txn.category == "انتقالات" ||
                txn.category == "انتقال حساب اشخاص" ||
                txn.category == "Transfer" ||
                txn.category == "واریز به هدف" ||
                txn.category == "برداشت از هدف"
            ) continue

            val catId = txn.categoryId ?: 0L
            val rate = when {
                baseCurr != null && txn.currencyCode.equals(baseCurr.code, ignoreCase = true) -> 1.0
                txn.exchangeRate > 0.0 && txn.exchangeRate != 1.0 -> txn.exchangeRate
                else -> rates[txn.currencyCode.uppercase()] ?: 1.0
            }
            val spentInBase = txn.amount * rate
            map[catId] = (map[catId] ?: 0.0) + spentInBase
        }
        map
    }

    var selectedBudgetCategory by remember { mutableStateOf<CategoryBudgetItem?>(null) }
    var isEditingBudget by remember { mutableStateOf(false) }
    var newBudgetLimitText by remember { mutableStateOf("") }
    var selectedReportCurrencyCode by remember { mutableStateOf<String?>(null) }
    var shoppingListIdToOpen by remember { mutableStateOf<Long?>(null) }
    var showShoppingListsSheet by remember { mutableStateOf(false) }

    val activeReportCurrency = remember(selectedReportCurrencyCode, activeCurrencies) {
        if (selectedReportCurrencyCode == null) null
        else activeCurrencies.find { it.code.equals(selectedReportCurrencyCode, ignoreCase = true) }
    }
    val effectiveCurrencySymbol = activeReportCurrency?.symbol ?: currencySymbol

    val effectiveSpending = remember(spending, selectedReportCurrencyCode, transactions, startOfMonth, endOfMonth) {
        if (selectedReportCurrencyCode == null) spending
        else transactions
            .filter {
                it.type == TransactionType.EXPENSE &&
                it.timestamp in startOfMonth..endOfMonth &&
                it.recipientName.isNullOrBlank() &&
                it.category != "Exchange" &&
                it.category != "PersonExchange" &&
                it.category != "PersonDebt" &&
                it.category != "انتقالات" &&
                it.category != "انتقال حساب اشخاص" &&
                it.category != "Transfer" &&
                it.category != "واریز به هدف" &&
                it.category != "برداشت از هدف" &&
                it.currencyCode.equals(selectedReportCurrencyCode, ignoreCase = true)
            }
            .sumOf { it.amount }
    }

    val effectiveIncome = remember(income, selectedReportCurrencyCode, transactions, startOfMonth, endOfMonth) {
        if (selectedReportCurrencyCode == null) income
        else transactions
            .filter {
                it.type == TransactionType.INCOME &&
                it.timestamp in startOfMonth..endOfMonth &&
                it.recipientName.isNullOrBlank() &&
                it.category != "Exchange" &&
                it.category != "PersonExchange" &&
                it.category != "PersonDebt" &&
                it.category != "انتقالات" &&
                it.category != "انتقال حساب اشخاص" &&
                it.category != "Transfer" &&
                it.category != "واریز به هدف" &&
                it.category != "برداشت از هدف" &&
                it.currencyCode.equals(selectedReportCurrencyCode, ignoreCase = true)
            }
            .sumOf { it.amount }
    }

    // Read categories directly from the settings, with limits from budgets (0.0 if not set)
    // Strictly restrict budget and budget reporting to EXPENSE categories only
    val budgetCategories = remember(categories, dbBudgets) {
        val activeCats = categories.filter { it.isActive && it.type == TransactionType.EXPENSE }
        activeCats.map { cat ->
            val iconVector = when (cat.iconName) {
                "Fastfood" -> Icons.Default.Fastfood
                "ShoppingBag" -> Icons.Default.ShoppingBag
                "ReceiptLong" -> Icons.Default.ReceiptLong
                "DirectionsCar" -> Icons.Default.DirectionsCar
                "PhoneAndroid" -> Icons.Default.PhoneAndroid
                "Subscriptions" -> Icons.Default.Subscriptions
                "Payments" -> Icons.Default.Payments
                "SwapHoriz" -> Icons.Default.SwapHoriz
                else -> Icons.Default.Category
            }
            val tintColor = Color(cat.colorHex)
            val bgColor = tintColor.copy(alpha = 0.15f)
            val saved = dbBudgets.find { it.categoryId == cat.id }
            val limit = saved?.monthlyLimit ?: 0.0
            CategoryBudgetItem(
                categoryId = cat.id,
                category = cat.name,
                icon = iconVector,
                iconBgColor = bgColor,
                iconTint = tintColor,
                limit = limit
            )
        }
    }

    val totalMonthlyBudget = remember(budgetCategories) {
        budgetCategories.sumOf { it.limit }
    }

    BackHandler {
        onNavigateBack()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "بودجه و تحلیل مالی",
                        color = BentoNavyDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "تحلیل مصارف و بودجه‌بندی هوشمند",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .border(1.dp, BentoBorder, CircleShape)
                        .testTag("analytics_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بازگشت به خانه",
                        tint = BentoNavyDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Overall Budget Ring Visualization (گراف بودجه کلی) ---
        item {
            OverallBudgetRingCard(
                totalBudget = totalMonthlyBudget,
                totalSpent = effectiveSpending,
                currencySymbol = effectiveCurrencySymbol
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Net Cashflow Cards (Income vs Expense)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("عواید ماهانه", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "$effectiveCurrencySymbol ${viewModel.formatAmount(effectiveIncome)}",
                                color = IncomeGreen,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Expense Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مصارف ماهانه", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "$effectiveCurrencySymbol ${viewModel.formatAmount(effectiveSpending)}",
                                color = BentoNavyDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Section Title: Category Budgets (Bento Grid Style)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بودجه‌بندی ماهوار ($currentMonthName)",
                    color = BentoNavyDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "برای جزئیات لمس کنید",
                    color = BentoIndigoAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Category Cards in Bento Grid
        if (budgetCategories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هیچ دسته‌بندی فعالی ثبت نشده است",
                            color = BentoNavyDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای بودجه‌بندی و تفکیک مصارف، دسته‌بندی‌ها را در تنظیمات اضافه فرمایید.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        } else {
            items(budgetCategories, key = { it.categoryId }) { catItem ->
                val catSpent = categorySpentMap[catItem.categoryId] ?: 0.0

                CategoryBudgetBentoCard(
                    category = catItem.category,
                    icon = catItem.icon,
                    iconBgColor = catItem.iconBgColor,
                    iconTint = catItem.iconTint,
                    spent = catSpent,
                    limit = catItem.limit,
                    currencySymbol = currencySymbol,
                    onClick = { selectedBudgetCategory = catItem },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
    }

    // --- Interactive Category Detail Bottom Sheet ---
    if (selectedBudgetCategory != null) {
        val cat = selectedBudgetCategory!!
        val catSpent = categorySpentMap[cat.categoryId] ?: 0.0
        val catRemaining = (cat.limit - catSpent).coerceAtLeast(0.0)
        val catTxns = transactions.filter {
            (if (cat.categoryId > 0) it.categoryId == cat.categoryId else it.categoryId == null && it.category.equals(cat.category, ignoreCase = true)) &&
            it.type == TransactionType.EXPENSE &&
            it.timestamp >= startOfMonth
        }

        ModalBottomSheet(
            onDismissRequest = { selectedBudgetCategory = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 36.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(cat.iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = cat.iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formatCategoryDari(cat.category),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(
                                text = "تحلیل و ریز جزئیات معاملات",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = { selectedBudgetCategory = null }) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats breakdown panel
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, BentoBorder, RoundedCornerShape(20.dp)),
                    color = Color(0xFFF8FAFD),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "بودجه تعیین‌شده", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(3.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "$currencySymbol ${viewModel.formatAmount(cat.limit)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                            }
                        }

                        Box(modifier = Modifier.height(30.dp).width(1.dp).background(BentoBorder))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "مصرف شده", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(3.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "$currencySymbol ${viewModel.formatAmount(catSpent)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (catSpent > cat.limit) ExpenseRed else BentoNavyDark
                                )
                            }
                        }

                        Box(modifier = Modifier.height(30.dp).width(1.dp).background(BentoBorder))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "باقیمانده", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(3.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "$currencySymbol ${viewModel.formatAmount(catRemaining)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action to edit budget for this category
                Button(
                    onClick = {
                        newBudgetLimitText = if (cat.limit > 0) cat.limit.toInt().toString() else ""
                        isEditingBudget = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("edit_category_budget_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ویرایش سقف بودجه ماهانه (${formatCategoryDari(cat.category)})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "معاملات این دسته‌بندی (${catTxns.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoNavyDark,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (catTxns.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "هنوز هیچ معامله‌ای برای این دسته‌بندی ثبت نشده است.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(catTxns) { txn ->
                            CategoryExpenseTransactionItem(
                                transaction = txn,
                                currencySymbol = currencySymbol,
                                accounts = accounts,
                                calendarType = selectedCalendarType,
                                isAmountMasked = viewModel.isTransactionAmountMasked(txn),
                                formatAmount = { viewModel.formatAmount(it, txn.currencyCode) },
                                onClick = {
                                    selectedTransactionForDetails = txn
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isEditingBudget && selectedBudgetCategory != null) {
        val cat = selectedBudgetCategory!!
        var showBudgetCalculator by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { isEditingBudget = false },
            title = {
                Text(
                    text = "تعیین بودجه ماهانه: ${formatCategoryDari(cat.category)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = BentoNavyDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "سقف بودجه برای این دسته‌بندی را به واحد ${currencySymbol} وارد نمایید:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newBudgetLimitText,
                        onValueChange = { newBudgetLimitText = it },
                        label = { Text("مبلغ سقف بودجه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("budget_limit_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoNavyDark,
                            unfocusedLabelColor = TextSecondary
                        ),
                        trailingIcon = {
                            CalculatorMiniButton(
                                onClick = { showBudgetCalculator = true },
                                contentDescription = "ماشین‌حساب سقف بودجه"
                            )
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = newBudgetLimitText.toDoubleOrNull() ?: 0.0
                        if (limit > 0) {
                            viewModel.setCategoryBudget(cat.category, limit, "AFN", categoryId = cat.categoryId)
                            selectedBudgetCategory = cat.copy(limit = limit)
                        }
                        isEditingBudget = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                    modifier = Modifier.testTag("save_budget_button")
                ) {
                    Text("ذخیره بودجه", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingBudget = false }) {
                    Text("انصراف", color = TextSecondary)
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(22.dp)
        )

        if (showBudgetCalculator) {
            MinimalCalculatorDialog(
                initialValue = newBudgetLimitText,
                title = "محاسبه سقف بودجه",
                onConfirm = { calcVal ->
                    newBudgetLimitText = calcVal
                },
                onDismiss = { showBudgetCalculator = false }
            )
        }
    }

    selectedTransactionForDetails?.let { txn ->
        LuxuryTransactionDetailAndEditBottomSheet(
            transaction = txn,
            activeCurrencies = activeCurrencies,
            accounts = accounts,
            categories = categories,
            shoppingLists = shoppingLists,
            isAmountMasked = viewModel.isTransactionAmountMasked(txn),
            onDismiss = { selectedTransactionForDetails = null },
            onUpdate = { oldTxn, newTxn, onResult ->
                viewModel.updateTransaction(
                    oldTxn = oldTxn,
                    newTxn = newTxn,
                    onSuccess = {
                        selectedTransactionForDetails = newTxn
                        onResult(true, null)
                    },
                    onError = { err ->
                        onResult(false, err)
                    }
                )
            },
            onDelete = { t ->
                viewModel.deleteTransaction(t)
                selectedTransactionForDetails = null
            },
            onOpenShoppingList = { listId ->
                selectedTransactionForDetails = null
                if (onNavigateToShoppingLists != null) {
                    onNavigateToShoppingLists(listId)
                } else {
                    shoppingListIdToOpen = listId
                    showShoppingListsSheet = true
                }
            }
        )
    }

    if (showShoppingListsSheet) {
        ShoppingListsSheet(
            viewModel = viewModel,
            onDismiss = {
                showShoppingListsSheet = false
                shoppingListIdToOpen = null
            },
            initialListIdToOpen = shoppingListIdToOpen
        )
    }
}

@Composable
private fun CategoryExpenseTransactionItem(
    transaction: TransactionEntity,
    currencySymbol: String,
    accounts: List<AccountCardEntity>,
    calendarType: com.example.util.AppCalendarType,
    isAmountMasked: Boolean,
    formatAmount: (Double) -> String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Description without category name and without transaction type ("مصرف")
    val titleText = when {
        !transaction.note.isNullOrBlank() -> transaction.note.trim()
        !transaction.recipientName.isNullOrBlank() -> transaction.recipientName.trim()
        transaction.title.isNotBlank() &&
            !transaction.title.equals(transaction.category, ignoreCase = true) &&
            !transaction.title.equals("مصرف", ignoreCase = true) &&
            !transaction.title.equals("مصرف عمومی", ignoreCase = true) &&
            !transaction.title.equals("عاید", ignoreCase = true) &&
            !transaction.title.equals("عاید عمومی", ignoreCase = true) -> transaction.title.trim()
        else -> "معامله بدون یادداشت"
    }

    // Account / Payment method
    val accountLabel = if (transaction.accountId > 0L) {
        val matched = accounts.find { it.id == transaction.accountId }
        matched?.name ?: "کارت بانکی"
    } else {
        "نقدی"
    }

    val formattedDate = com.example.util.PersianDateHelper.formatDateTime(transaction.timestamp, calendarType)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("category_txn_item_${transaction.id}"),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, BentoBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.accountId > 0L) Icons.Default.CreditCard else Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = BentoNavyDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleText,
                        color = BentoNavyDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(0.6.dp, BentoBorder)
                        ) {
                            Text(
                                text = accountLabel,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = if (isAmountMasked) {
                        "$currencySymbol ••••••"
                    } else {
                        "- ${transaction.currencySymbol ?: currencySymbol} ${formatAmount(transaction.amount)}"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }
    }
}
