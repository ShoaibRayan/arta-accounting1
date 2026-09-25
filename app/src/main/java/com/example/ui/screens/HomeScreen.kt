package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.QuickActionEntity
import com.example.data.local.ShoppingListWithItems
import com.example.ui.components.CreateOrEditQuickActionDialog
import com.example.data.local.FinancialGoalEntity
import com.example.ui.components.GoalDepositWithdrawDialog
import com.example.ui.components.GoalOperationType
import com.example.ui.components.AllTransactionsBottomSheet
import com.example.ui.components.AllTransactionsScreen
import com.example.ui.components.QuickActionCard
import com.example.ui.components.QuickActionExecutionDialog
import com.example.ui.components.QuickActionsManagementBottomSheet
import com.example.ui.components.QuickActionsSelectorBottomSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionType
import com.example.data.local.GoalTransactionEntity
import com.example.data.local.GoalTransactionType
import com.example.ui.components.GoalTransactionEditDialog
import com.example.ui.components.CompactCurrencyExchangeDialog
import com.example.ui.components.NavTab
import com.example.ui.components.TransactionRowItem
import com.example.ui.components.UnifiedTransactionItem
import com.example.ui.components.UnifiedTransactionRowItem
import com.example.ui.components.LuxuryPairedTransactionDetailBottomSheet
import com.example.ui.components.TransferPairType
import com.example.ui.components.consolidateTransactions
import com.example.ui.components.formatCategoryDari
import com.example.ui.components.getGoalImageVector
import com.example.ui.components.LuxuryTransactionDetailAndEditBottomSheet
import com.example.ui.components.LuxuryTransferDialog
import com.example.ui.components.TransferTab
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoNavyCard
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpenseRed
import com.example.ui.viewmodel.CurrencyBalanceInfo
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyCard
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ChipActive
import com.example.ui.theme.ChipInactive
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.components.CurrencyPickerBottomSheet
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.TransactionFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onNavigateTab: (NavTab) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSettingsSection: (SettingsSection) -> Unit = {},
    onNavigateToShoppingLists: ((Long?) -> Unit)? = null,
    onNavigateToQuickActionsManagement: (() -> Unit)? = null,
    onNavigateToAllTransactions: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val spending by viewModel.currentMonthSpending.collectAsStateWithLifecycle()
    val income by viewModel.currentMonthIncome.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val activeCurrencies by viewModel.activeCurrencies.collectAsStateWithLifecycle()
    val currencyBalances by viewModel.currencyBalances.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val activeAccounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val recipients by viewModel.recipients.collectAsStateWithLifecycle()
    val activeCategories by viewModel.activeCategories.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val activeGoals by viewModel.activeGoals.collectAsStateWithLifecycle()
    val selectedCurrencyGoalSummary by viewModel.selectedCurrencyGoalSummary.collectAsStateWithLifecycle()
    val homeQuickActions by viewModel.homeQuickActions.collectAsStateWithLifecycle()
    val maxHomeQuickActions by viewModel.maxHomeQuickActions.collectAsStateWithLifecycle()
    val allCategories by viewModel.categories.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedPairedItemForDetails by remember { mutableStateOf<UnifiedTransactionItem.Paired?>(null) }
    var editingTransferPair by remember { mutableStateOf<Pair<TransactionEntity, TransactionEntity>?>(null) }
    var editingExchangePair by remember { mutableStateOf<Pair<TransactionEntity, TransactionEntity>?>(null) }
    var editingGoalTxnPair by remember { mutableStateOf<Pair<GoalTransactionEntity, TransactionEntity>?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showTransferChoiceSheet by remember { mutableStateOf(false) }
    var transferLockedTab by remember { mutableStateOf<TransferTab?>(null) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showCompactExchangeDialog by remember { mutableStateOf(false) }
    var showFeaturesDropdown by remember { mutableStateOf(false) }
    var selectedCurrencyFilterCode by remember { mutableStateOf<String?>(null) }
    var showQuickActionsSelectorSheet by remember { mutableStateOf(false) }
    var executingQuickAction by remember { mutableStateOf<QuickActionEntity?>(null) }
    var showQuickActionsManagementSheet by remember { mutableStateOf(false) }
    var showCreateQuickActionDialog by remember { mutableStateOf(false) }
    var quickActionToCreateFromTxn by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAllTransactionsSheet by remember { mutableStateOf(false) }
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val pendingShoppingLists = remember(shoppingLists) { shoppingLists.filter { !it.list.isLoggedAsExpense } }
    var showShoppingListsSheet by remember { mutableStateOf(false) }
    var shoppingListIdToOpen by remember { mutableStateOf<Long?>(null) }
    var goalForDeposit by remember { mutableStateOf<FinancialGoalEntity?>(null) }

    val displayedTransactions = remember(transactions, selectedCurrencyFilterCode) {
        if (selectedCurrencyFilterCode == null) {
            transactions
        } else {
            val matchingIds = transactions.filter { it.currencyCode.equals(selectedCurrencyFilterCode, ignoreCase = true) }.map { it.id }.toSet()
            transactions.filter { txn ->
                txn.currencyCode.equals(selectedCurrencyFilterCode, ignoreCase = true) ||
                (txn.relatedTransactionId != null && txn.relatedTransactionId in matchingIds)
            }
        }
    }

    val consolidatedTransactions = remember(displayedTransactions, transactions) {
        consolidateTransactions(displayedTransactions, transactions)
    }

    // Recent Transactions limited to 10 on Home Screen (Requirement 21)
    val recentTransactions = remember(consolidatedTransactions) {
        consolidatedTransactions.take(10)
    }

    val formattedTotal = viewModel.formatAmount(totalBalance)
    val formattedSpending = viewModel.formatAmount(spending)
    val formatter = remember { java.text.DecimalFormat("#,###") }

    var dragX by remember { mutableFloatStateOf(0f) }

    // Dynamic greeting based on Persian/Afghan cultural etiquette
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val timeGreeting = when (currentHour) {
        in 5..11 -> "صبح بخیر"
        in 12..15 -> "چاشت بخیر"
        in 16..19 -> "عصر بخیر"
        else -> "شب بخیر"
    }
    val pleasantSubtitle = when (currentHour) {
        in 5..11 -> "امیدوارم روز پربرکت و موفقی داشته باشید."
        in 12..15 -> "وقت شما بخیر و برکت، حساب‌هایتان همیشه منظم باد."
        in 16..19 -> "عصر دل‌انگیزی داشته باشید، گزارش‌های مالی آماده است."
        else -> "شب آرام و آرامی داشته باشید، امور مالی در دسترس شماست."
    }

    val avatarInitials = remember(userName) {
        val parts = userName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (parts.size >= 2) "${parts.first().take(1)} ${parts.last().take(1)}"
        else userName.take(2).ifBlank { "MR" }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragX = 0f },
                        onDragEnd = {
                            // Swipe Left to Right: Go to Wallets / Cards
                            if (dragX > 70f) {
                                onNavigateTab(NavTab.CARDS)
                            } else if (dragX < -70f) {
                                // Swipe Right to Left: Go to Send/Receive Calculator
                                onNavigateTab(NavTab.CALCULATOR)
                            }
                            dragX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragX += dragAmount
                        }
                    )
                }
                .testTag("home_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 78.dp)
        ) {
        // --- Top Bar (Bento Header) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BentoLavenderAccent)
                            .border(1.5.dp, BentoBorder, CircleShape)
                            .clickable { onNavigateToSettings() }
                            .testTag("user_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInitials,
                            color = BentoNavyDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.clickable { onNavigateToSettings() }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$timeGreeting، ",
                                color = BentoIndigoAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userName,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pleasantSubtitle,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Top Header Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Features & Capabilities Menu Button (منوی امکانات و قابلیت‌های برنامه)
                    Box {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (showFeaturesDropdown) BentoLavenderSubtle else SurfaceWhite)
                                .border(
                                    width = 1.dp,
                                    color = if (showFeaturesDropdown) BentoIndigoAccent else BentoBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { showFeaturesDropdown = !showFeaturesDropdown }
                                .testTag("top_bar_features_menu_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "منوی امکانات و ابزارها",
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // منوی امکانات و ابزارها دقیقاً زیر کلید لمس شده باز می‌شود
                        DropdownMenu(
                            expanded = showFeaturesDropdown,
                            onDismissRequest = { showFeaturesDropdown = false },
                            modifier = Modifier
                                .widthIn(min = 250.dp, max = 285.dp)
                                .background(SurfaceWhite, RoundedCornerShape(20.dp))
                                .border(1.2.dp, BentoBorder, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            containerColor = SurfaceWhite,
                            shadowElevation = 16.dp
                        ) {
                            FeaturesDropdownContent(
                                onDismiss = { showFeaturesDropdown = false },
                                onOpenAllTransactions = {
                                    showFeaturesDropdown = false
                                    if (onNavigateToAllTransactions != null) {
                                        onNavigateToAllTransactions()
                                    } else {
                                        showAllTransactionsSheet = true
                                    }
                                },
                                onOpenQuickActionSettings = {
                                    showFeaturesDropdown = false
                                    if (onNavigateToQuickActionsManagement != null) {
                                        onNavigateToQuickActionsManagement()
                                    } else {
                                        showQuickActionsManagementSheet = true
                                    }
                                },
                                onOpenFinancialAnalytics = {
                                    showFeaturesDropdown = false
                                    onNavigateTab(NavTab.ANALYTICS)
                                },
                                onOpenExchange = {
                                    showFeaturesDropdown = false
                                    showCompactExchangeDialog = true
                                },
                                onOpenTransfer = {
                                    showFeaturesDropdown = false
                                    showTransferChoiceSheet = true
                                },
                                onOpenShoppingLists = {
                                    showFeaturesDropdown = false
                                    if (onNavigateToShoppingLists != null) {
                                        onNavigateToShoppingLists(null)
                                    } else {
                                        showShoppingListsSheet = true
                                    }
                                }
                            )
                        }
                    }

                    // Search Toggle Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSearchExpanded) BentoNavyDark else SurfaceWhite)
                            .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                            .clickable { isSearchExpanded = !isSearchExpanded }
                            .testTag("search_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "جستجو",
                            tint = if (isSearchExpanded) Color.White else BentoNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Search Field (Expandable) ---
        item {
            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("جستجوی معاملات، افراد، یادداشت...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("search_input_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        cursorColor = BentoNavyDark,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedBorderColor = BentoIndigoAccent,
                        unfocusedBorderColor = BentoBorder
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "پاک کردن جستجو")
                            }
                        }
                    }
                )
            }
        }

        // --- Bento Grid Hero: Minimalist Stacked Currency Cards (Self-contained, not linked) ---
        item {
            StackedCurrencyBalanceDeck(
                currencyBalances = currencyBalances,
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- Quick Actions: ثبت صرافی و انتقال وجه ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: ثبت صرافی (تبدیل اسعار)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(78.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0x14001552),
                            ambientColor = Color(0x0A001552)
                        )
                        .clickable { showCompactExchangeDialog = true }
                        .testTag("home_quick_action_exchange"),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF0FDF4))
                                .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = "ثبت صرافی",
                                tint = IncomeGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ثبت صرافی",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تبدیل و تبادله اسعار",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Card 2: انتقال وجه
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(78.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0x14001552),
                            ambientColor = Color(0x0A001552)
                        )
                        .clickable { showTransferChoiceSheet = true }
                        .testTag("home_quick_action_transfer"),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BentoLavenderSubtle)
                                .border(1.dp, BentoBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "انتقال وجه",
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "انتقال وجه",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "کارت، نقد و اشخاص",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // --- Bento Shopping Lists (خریدهای در انتظار) Section ---
        // کلید لیست خرید در صفحه اصلی تنها در صورتی نمایش داده می‌شود که خرید در انتظار وجود داشته باشد
        if (pendingShoppingLists.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0x14001552),
                            ambientColor = Color(0x0A001552)
                        )
                        .clickable {
                            if (onNavigateToShoppingLists != null) {
                                onNavigateToShoppingLists(null)
                            } else {
                                showShoppingListsSheet = true
                            }
                        }
                        .testTag("home_shopping_lists_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "خریدهای در انتظار",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "خریدهای در انتظار",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = "${pendingShoppingLists.size} در انتظار",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                val firstPending = pendingShoppingLists.first()
                                val pendingItemsCount = firstPending.items.count { !it.isPurchased }
                                Text(
                                    text = if (pendingShoppingLists.size == 1) {
                                        "«${firstPending.list.title}» ($pendingItemsCount قلم خرید باقی‌مانده)"
                                    } else {
                                        "${pendingShoppingLists.size} خرید در انتظار تسویه و تبدیل به مصرف"
                                    },
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoLavenderSubtle,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = "مشاهده",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoIndigoAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }


        // --- Bento Financial Goals (اهداف مالی و پس‌انداز) Section (تنها در صورت وجود اهداف نمایش داده می‌شود) ---
        if (activeGoals.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(22.dp),
                            spotColor = Color(0x14001552),
                            ambientColor = Color(0x0A001552)
                        )
                        .testTag("home_financial_goals_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Goals Card Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(BentoLavenderSubtle)
                                        .border(1.dp, BentoBorder, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "اهداف مالی و پس‌انداز",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = "${activeGoals.size} هدف فعال • ${selectedCurrencyGoalSummary.currencySymbol} ${viewModel.formatAmount(selectedCurrencyGoalSummary.totalAllocatedToGoals, selectedCurrencyGoalSummary.currencyCode)} پس‌انداز",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Active Goals List with Direct Deposit from HomeScreen
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            activeGoals.take(3).forEach { goal ->
                                val goalProgress = if (goal.targetAmount > 0) {
                                    (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                                } else 0f
                                val goalPercentage = (goalProgress * 100).toInt()

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateTab(NavTab.GOALS) }
                                        .testTag("home_goal_item_${goal.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    color = BentoLavenderSubtle.copy(alpha = 0.45f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = getGoalImageVector(goal.iconName),
                                                    contentDescription = null,
                                                    tint = Color(goal.colorHex),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = goal.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoNavyDark,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Progress percentage badge
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(goal.colorHex).copy(alpha = 0.12f),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(goal.colorHex).copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = "$goalPercentage%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(goal.colorHex),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }

                                                // Quick '+' deposit button right on this goal
                                                Surface(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .clickable { goalForDeposit = goal }
                                                        .testTag("home_goal_deposit_btn_${goal.id}"),
                                                    shape = CircleShape,
                                                    color = IncomeGreen,
                                                    shadowElevation = 1.dp
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "واریز به ${goal.title}",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        LinearProgressIndicator(
                                            progress = { goalProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(goal.colorHex),
                                            trackColor = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${goal.currencySymbol} ${viewModel.formatAmount(goal.currentAmount, goal.currencyCode)} ($goalPercentage٪)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoNavyDark
                                            )
                                            Text(
                                                text = "هدف: ${goal.currencySymbol} ${viewModel.formatAmount(goal.targetAmount, goal.currencyCode)}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
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

        // --- Alarm Banner for Upcoming Due Dates (Appears AFTER Total Balance Card) ---
        item {
            val upcomingDueTxns by viewModel.upcomingDueTransactions.collectAsStateWithLifecycle()
            if (upcomingDueTxns.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("upcoming_due_alarm_banner"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFDE68A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    val payCount = upcomingDueTxns.count { it.type == com.example.data.local.TransactionType.INCOME }
                                    val recvCount = upcomingDueTxns.size - payCount
                                    Text(
                                        text = "آلارم سررسید تسویه حساب",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "${if (payCount > 0) "$payCount پرداختنی " else ""}${if (recvCount > 0) "• $recvCount دریافتنی " else ""}(نزدیک به موعد یا معوقه - برای حذف بکشید)",
                                        fontSize = 10.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upcomingDueTxns.forEach { txn ->
                                SwipeableAlarmCard(
                                    txn = txn,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Bento Grid Module: Spending & Budget Card has been removed from HomeScreen and moved exclusively to top menu as requested ---

        // --- Transactions Section Header (Bento Module) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "معاملات اخیر",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (consolidatedTransactions.size > 10) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BentoIndigoAccent.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (onNavigateToAllTransactions != null) {
                                        onNavigateToAllTransactions()
                                    } else {
                                        showAllTransactionsSheet = true
                                    }
                                }
                        ) {
                            Text(
                                text = "۱۰ از ${consolidatedTransactions.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoIndigoAccent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Filter tabs: All, Expenses, Income
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterPill(
                        label = "همه",
                        isSelected = selectedFilter == TransactionFilter.ALL,
                        onClick = { viewModel.setFilter(TransactionFilter.ALL) }
                    )
                    FilterPill(
                        label = "مصارف",
                        isSelected = selectedFilter == TransactionFilter.EXPENSE,
                        onClick = { viewModel.setFilter(TransactionFilter.EXPENSE) }
                    )
                    FilterPill(
                        label = "عواید",
                        isSelected = selectedFilter == TransactionFilter.INCOME,
                        onClick = { viewModel.setFilter(TransactionFilter.INCOME) }
                    )
                }
            }
        }

        // --- Currency Filter Chips for Transactions History ---
        if (activeCurrencies.size > 1) {
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        FilterPill(
                            label = "همه ارزها",
                            isSelected = selectedCurrencyFilterCode == null,
                            onClick = { selectedCurrencyFilterCode = null }
                        )
                    }
                    items(activeCurrencies, key = { it.code }) { curr ->
                        FilterPill(
                            label = "${curr.code} ${curr.symbol}",
                            isSelected = selectedCurrencyFilterCode.equals(curr.code, ignoreCase = true),
                            onClick = {
                                selectedCurrencyFilterCode = if (selectedCurrencyFilterCode.equals(curr.code, ignoreCase = true)) null else curr.code
                            }
                        )
                    }
                }
            }
        }

        // --- Transaction List Items (Limited to 10 on Home Screen) ---
        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceWhite)
                        .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ معامله‌ای یافت نشد",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(recentTransactions, key = { it.id }) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    UnifiedTransactionRowItem(
                        item = item,
                        accounts = accounts,
                        currencySymbol = currencySymbol,
                        isAmountMasked = viewModel.isUnifiedItemMasked(item),
                        isFromAmountMasked = when (item) {
                            is UnifiedTransactionItem.Single -> viewModel.isTransactionAmountMasked(item.transaction)
                            is UnifiedTransactionItem.Paired -> viewModel.isTransactionAmountMasked(item.fromTxn)
                        },
                        isToAmountMasked = when (item) {
                            is UnifiedTransactionItem.Single -> viewModel.isTransactionAmountMasked(item.transaction)
                            is UnifiedTransactionItem.Paired -> viewModel.isTransactionAmountMasked(item.toTxn)
                        },
                        onClick = {
                            when (item) {
                                is UnifiedTransactionItem.Single -> {
                                    selectedTransactionForDetails = item.transaction
                                }
                                is UnifiedTransactionItem.Paired -> {
                                    selectedPairedItemForDetails = item
                                }
                            }
                        }
                    )
                }
            }

            // View All Transactions Button (Requirement 21)
            if (consolidatedTransactions.size > 10) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (onNavigateToAllTransactions != null) {
                                    onNavigateToAllTransactions()
                                } else {
                                    showAllTransactionsSheet = true
                                }
                            }
                            .testTag("btn_view_all_transactions"),
                        color = SurfaceWhite,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "مشاهده تمام معاملات",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = "مشاهده تمام ${consolidatedTransactions.size} معامله با قابلیت جستجو و فیلتر",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Paired Transaction Comprehensive Detail & Edit Sheet
    selectedPairedItemForDetails?.let { pairedItem ->
        LuxuryPairedTransactionDetailBottomSheet(
            item = pairedItem,
            accounts = accounts,
            isFromAmountMasked = viewModel.isTransactionAmountMasked(pairedItem.fromTxn),
            isToAmountMasked = viewModel.isTransactionAmountMasked(pairedItem.toTxn),
            onDismiss = { selectedPairedItemForDetails = null },
            onEdit = {
                selectedPairedItemForDetails = null
                when (pairedItem.pairType) {
                    TransferPairType.CURRENCY_EXCHANGE -> {
                        editingExchangePair = Pair(pairedItem.fromTxn, pairedItem.toTxn)
                    }
                    else -> {
                        editingTransferPair = Pair(pairedItem.fromTxn, pairedItem.toTxn)
                    }
                }
            },
            onDelete = {
                viewModel.deleteTransaction(pairedItem.fromTxn)
                selectedPairedItemForDetails = null
            }
        )
    }

    // Luxury Details & Edit Bottom Sheet
    selectedTransactionForDetails?.let { txn ->
        LuxuryTransactionDetailAndEditBottomSheet(
            transaction = txn,
            activeCurrencies = activeCurrencies,
            accounts = accounts,
            categories = activeCategories,
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
            onEditTransfer = { transferTxn ->
                coroutineScope.launch {
                    val linked = viewModel.getLinkedTransferDetails(transferTxn)
                    if (linked != null) {
                        editingTransferPair = linked
                    }
                }
            },
            onEditExchange = { exchangeTxn ->
                coroutineScope.launch {
                    val linked = viewModel.getLinkedTransferDetails(exchangeTxn)
                    if (linked != null) {
                        val (from, to) = if (linked.first.type == TransactionType.EXPENSE) linked else Pair(linked.second, linked.first)
                        editingExchangePair = Pair(from, to)
                    }
                }
            },
            onOpenShoppingList = { listId ->
                if (onNavigateToShoppingLists != null) {
                    onNavigateToShoppingLists(listId)
                } else {
                    shoppingListIdToOpen = listId
                    showShoppingListsSheet = true
                }
            },
            onEditGoal = { goalTxnEntity ->
                coroutineScope.launch {
                    val found = viewModel.findGoalTransactionForTransaction(goalTxnEntity)
                    if (found != null) {
                        editingGoalTxnPair = Pair(found, goalTxnEntity)
                    } else {
                        val matchedGoal = allGoals.find { goalTxnEntity.title.contains(it.title) } ?: allGoals.firstOrNull()
                        if (matchedGoal != null) {
                            val opType = if (goalTxnEntity.type == TransactionType.EXPENSE) GoalTransactionType.DEPOSIT else GoalTransactionType.WITHDRAWAL
                            val synthetic = GoalTransactionEntity(
                                id = 0L,
                                goalId = matchedGoal.id,
                                amount = goalTxnEntity.amount,
                                currencyCode = goalTxnEntity.currencyCode,
                                currencySymbol = goalTxnEntity.currencySymbol,
                                type = opType,
                                accountId = if (goalTxnEntity.accountId > 0) goalTxnEntity.accountId else null,
                                accountName = if (goalTxnEntity.accountId > 0) accounts.find { it.id == goalTxnEntity.accountId }?.name ?: "کارت" else "بیلانس کل",
                                note = goalTxnEntity.note ?: "",
                                timestamp = goalTxnEntity.timestamp,
                                linkedTransactionId = goalTxnEntity.id
                            )
                            editingGoalTxnPair = Pair(synthetic, goalTxnEntity)
                        }
                    }
                }
            },
            onCreateQuickAction = { txn ->
                quickActionToCreateFromTxn = txn
            }
        )
    }

    // All Transactions Comprehensive Screen (Requirement: Full screen, back button on top / device, not dismissed by drag)
    if (showAllTransactionsSheet) {
        AllTransactionsScreen(
            consolidatedTransactions = consolidatedTransactions,
            accounts = accounts,
            activeCurrencies = activeCurrencies,
            categories = activeCategories,
            recipients = recipients,
            currencySymbol = currencySymbol,
            viewModel = viewModel,
            onItemClick = { item ->
                when (item) {
                    is UnifiedTransactionItem.Single -> {
                        selectedTransactionForDetails = item.transaction
                    }
                    is UnifiedTransactionItem.Paired -> {
                        selectedPairedItemForDetails = item
                    }
                }
            },
            onDismiss = { showAllTransactionsSheet = false },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(15f)
        )
    }

    // Edit Currency Exchange Dialog (Atomic edit of both linked exchange transactions)
    editingExchangePair?.let { pair ->
        CompactCurrencyExchangeDialog(
            viewModel = viewModel,
            currencies = activeCurrencies,
            currencyBalances = currencyBalances,
            editingExchangeFromTxn = pair.first,
            editingExchangeToTxn = pair.second,
            onDismiss = { editingExchangePair = null }
        )
    }

    // Edit Transfer Dialog (Atomic edit of both linked transfer transactions)
    editingTransferPair?.let { pair ->
        LuxuryTransferDialog(
            viewModel = viewModel,
            accounts = activeAccounts,
            recipients = recipients,
            activeCurrencies = activeCurrencies,
            editingTransferFromTxn = pair.first,
            editingTransferToTxn = pair.second,
            onDismiss = { editingTransferPair = null }
        )
    }

    // Edit Goal Transaction Dialog (Atomic edit of savings goal operation)
    editingGoalTxnPair?.let { (goalTxn, linkedTxn) ->
        GoalTransactionEditDialog(
            goalTransaction = goalTxn,
            linkedTransaction = linkedTxn,
            allGoals = allGoals,
            accounts = accounts,
            onDismiss = { editingGoalTxnPair = null },
            onSave = { id, newGoalId, newAmount, newType, newAccountId, newNote, newTimestamp ->
                viewModel.updateGoalTransaction(
                    goalTransactionId = id,
                    newGoalId = newGoalId,
                    newAmount = newAmount,
                    newType = newType,
                    newAccountId = newAccountId,
                    newNote = newNote,
                    newTimestamp = newTimestamp
                )
                editingGoalTxnPair = null
            }
        )
    }

    if (showTransferDialog) {
        LuxuryTransferDialog(
            viewModel = viewModel,
            accounts = activeAccounts,
            recipients = recipients,
            activeCurrencies = activeCurrencies,
            initialTab = transferLockedTab ?: TransferTab.CASH_AND_CARD,
            lockedTab = transferLockedTab,
            onDismiss = {
                showTransferDialog = false
                transferLockedTab = null
            }
        )
    }

    // Transfer Choice Bottom Sheet (انتخاب نوع انتقال: کارت به کارت، نقد و کارت، یا حواله اشخاص)
    if (showTransferChoiceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTransferChoiceSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "انتخاب نوع انتقال",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "عملیات مورد نظر خود را انتخاب فرمایید",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { showTransferChoiceSheet = false },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = BentoNavyDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Option 1: انتقال نقد و کارت (کارت به کارت، واریز و برداشت صندوق)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            transferLockedTab = TransferTab.CASH_AND_CARD
                            showTransferChoiceSheet = false
                            showTransferDialog = true
                        }
                        .testTag("transfer_choice_cash_and_card"),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "انتقال نقد و کارت",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "کارت به کارت، واریز نقد به کارت یا برداشت از کارت به صندوق",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Option 2: انتقال حساب و حواله اشخاص
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            transferLockedTab = TransferTab.PERSON_TO_PERSON
                            showTransferChoiceSheet = false
                            showTransferDialog = true
                        }
                        .testTag("transfer_choice_person_to_person"),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BentoLavenderSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "انتقال حساب و حواله اشخاص",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "انتقال طلب، بدهی یا تسویه حساب بین طرف‌حساب‌ها",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Modern Minimal Currency Picker Bottom Sheet
    if (showCurrencyDialog) {
        CurrencyPickerBottomSheet(
            currencies = activeCurrencies,
            currencyBalances = currencyBalances,
            selectedCurrency = selectedCurrency,
            onCurrencySelected = { curr ->
                viewModel.selectCurrency(curr)
            },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToExchange = onNavigateToSettings,
            onDismiss = { showCurrencyDialog = false }
        )
    }

    // Compact Currency Exchange Dialog (صفحه کوچک برای ثبت صرافی و تبدیل ارز یا انتقال به تنظیمات)
    if (showCompactExchangeDialog) {
        CompactCurrencyExchangeDialog(
            viewModel = viewModel,
            currencies = activeCurrencies,
            currencyBalances = currencyBalances,
            onDismiss = { showCompactExchangeDialog = false },
            onNavigateToFullSettingsExchange = {
                showCompactExchangeDialog = false
                onNavigateToSettingsSection(SettingsSection.EXCHANGE)
            }
        )
    }

    // Quick Actions Selector Bottom Sheet (از منوی امکانات برنامه یا لمس طولانی)
    if (showQuickActionsSelectorSheet) {
        QuickActionsSelectorBottomSheet(
            viewModel = viewModel,
            accounts = activeAccounts,
            onSelectAction = { action ->
                executingQuickAction = action
            },
            onOpenManagement = {
                showQuickActionsSelectorSheet = false
                if (onNavigateToQuickActionsManagement != null) {
                    onNavigateToQuickActionsManagement()
                } else {
                    showQuickActionsManagementSheet = true
                }
            },
            onCreateNewAction = {
                showCreateQuickActionDialog = true
            },
            onDismiss = { showQuickActionsSelectorSheet = false }
        )
    }

    // Quick Action Execution Dialog
    executingQuickAction?.let { action ->
        QuickActionExecutionDialog(
            quickAction = action,
            viewModel = viewModel,
            accounts = activeAccounts,
            onDismiss = { executingQuickAction = null },
            onSuccess = { executingQuickAction = null }
        )
    }

    // Quick Actions Management Sheet
    if (showQuickActionsManagementSheet) {
        QuickActionsManagementBottomSheet(
            viewModel = viewModel,
            accounts = activeAccounts,
            activeCurrencies = activeCurrencies,
            categories = allCategories,
            recipients = recipients,
            activeGoals = activeGoals,
            onDismiss = { showQuickActionsManagementSheet = false }
        )
    }

    // Create / Edit Quick Action Dialog (from "+" or from existing transaction)
    if (showCreateQuickActionDialog || quickActionToCreateFromTxn != null) {
        CreateOrEditQuickActionDialog(
            initialTransaction = quickActionToCreateFromTxn,
            viewModel = viewModel,
            accounts = activeAccounts,
            activeCurrencies = activeCurrencies,
            categories = allCategories,
            recipients = recipients,
            activeGoals = activeGoals,
            onDismiss = {
                showCreateQuickActionDialog = false
                quickActionToCreateFromTxn = null
            },
            onSaved = {
                showCreateQuickActionDialog = false
                quickActionToCreateFromTxn = null
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

    goalForDeposit?.let { goal ->
        val currentCurrencyBalance = currencyBalances.find {
            (goal.currencyId > 0 && it.currency.id == goal.currencyId) || it.currency.code.equals(goal.currencyCode, ignoreCase = true)
        }
        GoalDepositWithdrawDialog(
            goal = goal,
            initialOperation = GoalOperationType.DEPOSIT,
            accounts = accounts,
            generalBalance = currentCurrencyBalance?.balance ?: 0.0,
            onDeposit = { amount, accountId, note ->
                viewModel.depositToGoal(goal.id, amount, accountId, note)
                goalForDeposit = null
            },
            onWithdraw = { amount, accountId, note ->
                viewModel.withdrawFromGoal(goal.id, amount, accountId, note)
                goalForDeposit = null
            },
            onDismiss = { goalForDeposit = null }
        )
    }
    }
}

/**
 * منوی امکانات و ابزارهای برنامه که مستقیماً زیر کلید منو باز می‌شود
 * شامل تنظیمات عملیات سریع (بدون متن اصلی)، تحلیل مالی، صرافی و انتقال
 */
@Composable
fun FeaturesDropdownContent(
    onDismiss: () -> Unit,
    onOpenAllTransactions: () -> Unit,
    onOpenQuickActionSettings: () -> Unit,
    onOpenFinancialAnalytics: () -> Unit,
    onOpenExchange: () -> Unit,
    onOpenTransfer: () -> Unit,
    onOpenShoppingLists: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Dropdown Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BentoLavenderSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = BentoIndigoAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "امکانات و ابزارها",
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = BentoNavyDark
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            thickness = 0.8.dp,
            color = BentoBorder
        )

        // ۱. تراکنش‌های کامل (دسترسی مستقیم از منو طبق خواسته کاربر)
        FeaturesDropdownMenuItem(
            title = "تراکنش‌های کامل",
            subtitle = "مشاهده، جستجو و فیلتر کلیه معاملات",
            icon = Icons.Default.ReceiptLong,
            iconBg = BentoLavenderSubtle,
            iconTint = BentoIndigoAccent,
            onClick = onOpenAllTransactions,
            testTag = "menu_item_all_transactions"
        )

        // ۲. تنظیمات عملیات سریع
        FeaturesDropdownMenuItem(
            title = "تنظیمات عملیات سریع",
            subtitle = "مدیریت، ساخت و ویرایش میانبرها",
            icon = Icons.Default.Tune,
            iconBg = BentoLavenderSubtle,
            iconTint = BentoIndigoAccent,
            onClick = onOpenQuickActionSettings,
            testTag = "menu_item_quick_action_settings"
        )

        // ۳. بودجه و تحلیل مالی (تنها در منوی بالا طبق خواسته کاربر)
        FeaturesDropdownMenuItem(
            title = "بودجه و تحلیل مالی",
            subtitle = "مدیریت سقف بودجه، مصارف و نمودارهای مالی",
            icon = Icons.Default.PieChart,
            iconBg = Color(0xFFF3E8FF),
            iconTint = Color(0xFF8B5CF6),
            onClick = onOpenFinancialAnalytics,
            testTag = "menu_item_financial_analytics"
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            thickness = 0.6.dp,
            color = BentoBorder.copy(alpha = 0.6f)
        )

        // ۴. ثبت صرافی و تبدیل اسعار
        FeaturesDropdownMenuItem(
            title = "ثبت صرافی و تبدیل اسعار",
            subtitle = "تبدیل فوری بین ارزها و برابری",
            icon = Icons.Default.CurrencyExchange,
            iconBg = Color(0xFFF0FDF4),
            iconTint = IncomeGreen,
            onClick = onOpenExchange,
            testTag = "menu_item_exchange"
        )

        // ۵. انتقال وجه بین‌حسابی
        FeaturesDropdownMenuItem(
            title = "انتقال وجه بین‌حسابی",
            subtitle = "جابجایی وجه میان حساب‌ها و اشخاص",
            icon = Icons.Default.SwapHoriz,
            iconBg = Color(0xFFEFF6FF),
            iconTint = Color(0xFF2563EB),
            onClick = onOpenTransfer,
            testTag = "menu_item_transfer"
        )

        // ۶. لیست‌های خرید
        FeaturesDropdownMenuItem(
            title = "لیست‌های خرید",
            subtitle = "ثبت اقلام، قیمت‌گذاری و تبدیل به مصرف",
            icon = Icons.Default.ShoppingCart,
            iconBg = Color(0xFFFEF3C7),
            iconTint = Color(0xFFD97706),
            onClick = onOpenShoppingLists,
            testTag = "menu_item_shopping_lists"
        )
    }
}

@Composable
fun FeaturesDropdownMenuItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoNavyDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (backgroundColor == BentoNavyDark) BentoNavyDark else BentoBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) BentoNavyDark else SurfaceWhite)
            .border(
                1.dp,
                if (isSelected) BentoNavyDark else BentoBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableAlarmCard(
    txn: com.example.data.local.TransactionEntity,
    viewModel: com.example.ui.viewmodel.FinanceViewModel
) {
    val now = System.currentTimeMillis()
    val diffMillis = (txn.dueDate ?: now) - now
    val diffDays = (diffMillis / (24 * 60 * 60 * 1000L)).toInt()
    val isOverdue = diffDays < 0
    val isToday = diffDays == 0

    val remainingText = when {
        isOverdue -> "${-diffDays} روز گذشته (معوقه)"
        isToday -> "امروز موعد سررسید است"
        else -> "$diffDays روز تا سررسید مانده"
    }

    // اگر دریافتی داشتیم یعنی ما باید پرداخت کنیم (پرداختنی)، اگر پرداختی داشتیم یعنی طرف بدهکار است و ما دریافت می‌کنیم (دریافتنی)
    val isDebtToPay = txn.type == com.example.data.local.TransactionType.INCOME
    val badgeBg = if (isOverdue) com.example.ui.theme.ExpenseRedBg else Color(0xFFFEF3C7)
    val badgeColor = if (isOverdue) com.example.ui.theme.ExpenseRed else Color(0xFFD97706)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                viewModel.dismissAlarm(txn)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isSwipingToStart = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(com.example.ui.theme.ExpenseRed)
                    .padding(horizontal = 16.dp),
                contentAlignment = if (isSwipingToStart) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف هشدار",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "حذف هشدار",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        content = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = com.example.ui.theme.SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.BentoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = txn.recipientName ?: txn.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = com.example.ui.theme.BentoNavyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDebtToPay) com.example.ui.theme.ExpenseRed.copy(alpha = 0.12f) else com.example.ui.theme.IncomeGreen.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (isDebtToPay) "پرداختنی" else "دریافتنی",
                                    color = if (isDebtToPay) com.example.ui.theme.ExpenseRed else com.example.ui.theme.IncomeGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = remainingText,
                                    color = badgeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val hasLedgerConversion = !txn.ledgerCurrencyCode.isNullOrBlank() && txn.ledgerAmount != null && txn.ledgerAmount > 0.0
                        val displayAmount = if (hasLedgerConversion) txn.ledgerAmount!! else txn.amount
                        val displaySymbol = if (hasLedgerConversion) (txn.ledgerCurrencySymbol ?: txn.ledgerCurrencyCode!!) else txn.currencySymbol
                        val displayCode = if (hasLedgerConversion) txn.ledgerCurrencyCode!! else txn.currencyCode

                        val isTxnMasked = viewModel.isTransactionAmountMasked(txn)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isDebtToPay) "مبلغ پرداختنی:" else "مبلغ دریافتنی:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDebtToPay) com.example.ui.theme.ExpenseRed else com.example.ui.theme.IncomeGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                if (isTxnMasked) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$displaySymbol ••••••",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = com.example.ui.theme.BentoIndigoAccent
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "محافظت شده",
                                            tint = com.example.ui.theme.BentoIndigoAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "$displaySymbol ${viewModel.formatAmount(displayAmount, displayCode)} $displayCode",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDebtToPay) com.example.ui.theme.ExpenseRed else com.example.ui.theme.IncomeGreen
                                    )
                                }
                            }
                        }
                        if (hasLedgerConversion) {
                            Spacer(modifier = Modifier.height(2.dp))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                if (isTxnMasked) {
                                    Text(
                                        text = "${txn.currencySymbol} •••••• (معادل)",
                                        fontSize = 10.sp,
                                        color = com.example.ui.theme.BentoIndigoAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "${txn.currencySymbol} ${viewModel.formatAmount(txn.amount, txn.currencyCode)} (معادل)",
                                        fontSize = 10.sp,
                                        color = com.example.ui.theme.BentoIndigoAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Swipe hint indicator (No button, purely gestural deletion)
                    Text(
                        text = "کشیدن برای حذف ⟷",
                        fontSize = 10.sp,
                        color = com.example.ui.theme.TextSecondary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

/**
 * 3D Vertical Circular Carousel for Currency Balances
 * - Features cylindrical/circular 3D orbit trajectory (3D Vertical Circular Carousel)
 * - User perspective is angled from the left of the vertical wheel
 * - Primary/active card stays in the center, enlarged, crisp elevation
 * - Secondary cards above and below curve away into depth with 3D rotationX, rotationY, scale, and depth alpha
 * - Seamless infinite wrapping / circular modulo navigation
 * - Smooth touch dragging with spring snapping
 */
@Composable
fun StackedCurrencyBalanceDeck(
    currencyBalances: List<CurrencyBalanceInfo>,
    viewModel: com.example.ui.viewmodel.FinanceViewModel,
    modifier: Modifier = Modifier
) {
    if (currencyBalances.isEmpty()) return

    // Base currency at the top of stack
    val orderedBalances = remember(currencyBalances) {
        val base = currencyBalances.filter { it.currency.isBaseCurrency }
        val nonBase = currencyBalances.filter { !it.currency.isBaseCurrency }
        (base + nonBase).ifEmpty { currencyBalances }
    }

    var activeIndex by remember { mutableIntStateOf(0) }
    val n = orderedBalances.size
    val safeActiveIndex = activeIndex.coerceIn(0, orderedBalances.lastIndex)
    val coroutineScope = rememberCoroutineScope()
    val dragAnim = remember { Animatable(0f) }

    val localDensity = LocalDensity.current
    val stepPx = with(localDensity) { 60.dp.toPx() }
    val radiusPx = with(localDensity) { 42.dp.toPx() }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clipToBounds()
                .testTag("stacked_currency_deck")
                .pointerInput(n, safeActiveIndex) {
                    if (n <= 1) return@pointerInput
                    detectVerticalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            coroutineScope.launch {
                                val currentOffset = dragAnim.value
                                val stepThreshold = stepPx * 0.20f
                                val dragSteps = (currentOffset / stepPx).roundToInt()
                                val snapStep = if (dragSteps != 0) {
                                    dragSteps
                                } else {
                                    if (currentOffset > stepThreshold) 1
                                    else if (currentOffset < -stepThreshold) -1
                                    else 0
                                }
                                val targetOffset = snapStep * stepPx
                                dragAnim.animateTo(
                                    targetValue = targetOffset,
                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
                                )
                                activeIndex = ((safeActiveIndex - snapStep) % n + n) % n
                                dragAnim.snapTo(0f)
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                dragAnim.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = 450f))
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                dragAnim.snapTo(dragAnim.value + dragAmount)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val virtualOffset = (-dragAnim.value / stepPx).toDouble()

            for (i in 0 until n) {
                val rawDiff = ((i - safeActiveIndex).toDouble() - virtualOffset)
                var normalizedDiff = ((rawDiff % n) + n) % n
                if (normalizedDiff > n / 2.0) {
                    normalizedDiff -= n
                }

                // Render visible window cards (center, top, bottom, and transitioning ones)
                if (abs(normalizedDiff) > 1.48) continue

                val absDiff = abs(normalizedDiff).toFloat()
                val thetaDeg = (normalizedDiff * 22.0).toFloat().coerceIn(-30f, 30f)
                val thetaRad = Math.toRadians(thetaDeg.toDouble())

                // 3D Circular cylindrical path with tight, compact offset:
                val targetTranslationY = (radiusPx * Math.sin(thetaRad)).toFloat()
                // Soft curved transition without harsh perspective distortions:
                val rotX = -thetaDeg * 0.18f
                val rotY = 0f
                val targetTranslationX = 0f

                val cardScale = (1.0f - (absDiff * 0.06f)).coerceIn(0.88f, 1.0f)
                val cardAlpha = (1.0f - (absDiff * 0.25f)).coerceIn(0f, 1.0f)
                val isCenter = absDiff < 0.35f
                val zIndex = (10f - absDiff * 4f)

                val balInfo = orderedBalances[i]
                val isTopPeeking = normalizedDiff in -1.38..-0.35
                val isBottomPeeking = normalizedDiff in 0.35..1.38

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                        .graphicsLayer {
                            translationY = targetTranslationY
                            translationX = targetTranslationX
                            rotationX = rotX
                            rotationY = rotY
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = cardAlpha
                            shape = RoundedCornerShape(26.dp)
                            clip = false
                            shadowElevation = 0f
                            cameraDistance = 16f * localDensity.density
                        }
                        .zIndex(zIndex)
                        .clip(RoundedCornerShape(26.dp))
                        .border(
                            width = if (isCenter) 1.2.dp else 1.dp,
                            color = if (isCenter) Color.White.copy(alpha = 0.24f) else BentoBorder.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable(enabled = !isCenter && n > 1) {
                            coroutineScope.launch {
                                val targetShift = if (isTopPeeking) stepPx else -stepPx
                                val nextIndex = if (isTopPeeking) {
                                    ((safeActiveIndex - 1) % n + n) % n
                                } else {
                                    ((safeActiveIndex + 1) % n + n) % n
                                }
                                dragAnim.animateTo(
                                    targetValue = targetShift,
                                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
                                )
                                activeIndex = nextIndex
                                dragAnim.snapTo(0f)
                            }
                        },
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCenter) BentoNavyDark else Color(0xFF0F172A)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    if (isCenter) listOf(BentoNavyDark, BentoNavyCard)
                                    else listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 15.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Currency Info + Base Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.12f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = balInfo.currency.flagEmoji, fontSize = 16.sp)
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = balInfo.currency.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (balInfo.currency.isBaseCurrency) "حساب نقدی (ارز پایه)" else "حساب نقدی",
                                            color = BentoLavenderAccent.copy(alpha = 0.75f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
                                ) {
                                    Text(
                                        text = balInfo.currency.code,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Middle: Formatted Balance with currency strictly on the left
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = balInfo.currency.symbol,
                                        color = BentoLavenderAccent,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = viewModel.formatAmount(balInfo.balance, balInfo.currency.code),
                                        color = Color.White,
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    )
                                }
                            }

                            // Bottom Row: Inflow / Outflow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SouthWest,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "${balInfo.currency.symbol} ${viewModel.formatAmount(balInfo.totalIncome, balInfo.currency.code)}",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.NorthEast,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "${balInfo.currency.symbol} ${viewModel.formatAmount(balInfo.totalExpense, balInfo.currency.code)}",
                                                color = Color.White.copy(alpha = 0.85f),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
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

        // Indicator dots below the carousel
        if (n > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(n) { i ->
                    val isSelected = i == safeActiveIndex
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) BentoNavyDark else BentoBorder)
                            .clickable {
                                if (i != safeActiveIndex) {
                                    coroutineScope.launch {
                                        var stepDiff = ((i - safeActiveIndex) % n + n) % n
                                        if (stepDiff > n / 2) stepDiff -= n
                                        dragAnim.animateTo(
                                            targetValue = -stepDiff * stepPx,
                                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f)
                                        )
                                        activeIndex = i
                                        dragAnim.snapTo(0f)
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}

