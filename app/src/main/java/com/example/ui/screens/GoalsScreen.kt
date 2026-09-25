package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.PersianDateHelper
import java.text.DecimalFormat

enum class GoalFilterTab(val label: String) {
    ACTIVE("در حال پیشرفت"),
    ALL("همه"),
    COMPLETED("تکمیل شده"),
    ARCHIVED("بایگانی")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val activeCurrencies by viewModel.activeCurrencies.collectAsStateWithLifecycle()
    val accounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val selectedCurrencyGoalSummary by viewModel.selectedCurrencyGoalSummary.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val currencyBalances by viewModel.currencyBalances.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(GoalFilterTab.ACTIVE) }
    var selectedGoalForDetail by remember { mutableStateOf<FinancialGoalEntity?>(null) }
    var goalForDepositWithdraw by remember { mutableStateOf<Pair<FinancialGoalEntity, GoalOperationType>?>(null) }
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<FinancialGoalEntity?>(null) }
    var txnToEdit by remember { mutableStateOf<GoalTransactionEntity?>(null) }
    var showCurrencyPickerSheet by remember { mutableStateOf(false) }

    val formatter = remember { DecimalFormat("#,###") }

    // Filter goals according to selected tab and selected currency (if any)
    val filteredGoals = remember(allGoals, selectedTab, selectedCurrency) {
        val currCode = selectedCurrency?.code
        val currFiltered = if (currCode != null) {
            allGoals.filter { it.currencyCode.equals(currCode, ignoreCase = true) }
        } else {
            allGoals
        }

        when (selectedTab) {
            GoalFilterTab.ACTIVE -> currFiltered.filter { it.status == GoalStatus.ACTIVE }
            GoalFilterTab.COMPLETED -> currFiltered.filter { it.status == GoalStatus.COMPLETED }
            GoalFilterTab.ARCHIVED -> currFiltered.filter { it.status == GoalStatus.ARCHIVED }
            GoalFilterTab.ALL -> currFiltered
        }
    }

    // Collect transactions for currently opened detail goal
    val currentGoalTransactions by remember(selectedGoalForDetail) {
        if (selectedGoalForDetail != null) {
            viewModel.getTransactionsForGoal(selectedGoalForDetail!!.id)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 78.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Top Bar / Bento Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BentoLavenderSubtle)
                                    .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                    .clickable { onNavigateBack() }
                                    .testTag("goals_back_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "بازگشت",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "اهداف مالی و پس‌انداز",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = "مدیریت و برنامه‌ریزی هوشمند اندوخته‌ها",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Currency Chip & Add Goal Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Currency selector chip
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                    .clickable { showCurrencyPickerSheet = true }
                                    .testTag("goals_currency_picker"),
                                color = SurfaceWhite
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = selectedCurrency?.let { "${it.flagEmoji} ${it.code}" } ?: "همه اسعار",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            // Add Goal FAB-styled button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BentoNavyDark)
                                    .clickable { showCreateGoalDialog = true }
                                    .testTag("add_new_goal_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "هدف جدید",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Bento Summary Card: Real Total, Allocated, and Free Available Balance (strictly per prompt!)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .border(1.dp, BentoBorder, RoundedCornerShape(26.dp)),
                        color = BentoNavyDark,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(BentoNavyDark, BentoNavyCard)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            // Header of Summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BentoLavenderAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = BentoLavenderAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "تفکیک شفاف دارایی (${selectedCurrencyGoalSummary.currencyCode})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoLavenderAccent
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BentoLavenderAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${selectedCurrencyGoalSummary.activeGoalsCount} هدف فعال",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Top Primary Stat: Real Total Balance
                            Text(
                                text = "مجموع دارایی واقعی شما:",
                                fontSize = 12.sp,
                                color = BentoLavenderAccent.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = formatter.format(selectedCurrencyGoalSummary.totalRealBalance),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = selectedCurrencyGoalSummary.currencySymbol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoLavenderAccent,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Two Secondary Split Stat Cards: Allocated to Goals vs Free Cash
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Allocated to Goals
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF38BDF8))
                                            )
                                            Text(
                                                text = "اختصاص‌یافته به اهداف",
                                                fontSize = 11.sp,
                                                color = BentoLavenderAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${selectedCurrencyGoalSummary.currencySymbol} ${formatter.format(selectedCurrencyGoalSummary.totalAllocatedToGoals)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                // 2. Free Available Cash
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(IncomeGreen)
                                            )
                                            Text(
                                                text = "موجودی آزاد و قابل مصرف",
                                                fontSize = 11.sp,
                                                color = BentoLavenderAccent
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${selectedCurrencyGoalSummary.currencySymbol} ${formatter.format(selectedCurrencyGoalSummary.availableFreeBalance)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Filter Tabs (Active, All, Completed, Archived)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoalFilterTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            val count = when (tab) {
                                GoalFilterTab.ACTIVE -> allGoals.count { it.status == GoalStatus.ACTIVE }
                                GoalFilterTab.COMPLETED -> allGoals.count { it.status == GoalStatus.COMPLETED }
                                GoalFilterTab.ARCHIVED -> allGoals.count { it.status == GoalStatus.ARCHIVED }
                                GoalFilterTab.ALL -> allGoals.size
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) BentoNavyDark else BentoBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedTab = tab }
                                    .testTag("filter_tab_${tab.name.lowercase()}"),
                                color = if (isSelected) BentoNavyDark else SurfaceWhite
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tab.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color.White.copy(alpha = 0.2f) else BentoLavenderSubtle
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else BentoNavyDark,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Goals List
                if (filteredGoals.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(BentoLavenderSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "هیچ هدف مالی در این بخش یافت نشد",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "با ایجاد هدف جدید، مبالغ مورد نیاز برای خرید موتر، خانه، سفر یا موارد دلخواه را هدفمند ذخیره کنید.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { showCreateGoalDialog = true },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                                    modifier = Modifier.testTag("empty_add_goal_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("ایجاد اولین هدف مالی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredGoals, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            onClick = { selectedGoalForDetail = goal }
                        )
                    }
                }
            }
        }

        // --- Dialogs & Bottom Sheets ---

        // 1. Goal Detail Bottom Sheet
        selectedGoalForDetail?.let { activeGoal ->
            // Re-fetch current state of activeGoal in case it was modified
            val latestGoal = allGoals.find { it.id == activeGoal.id } ?: activeGoal
            GoalDetailBottomSheet(
                goal = latestGoal,
                transactions = currentGoalTransactions,
                onDepositClick = {
                    goalForDepositWithdraw = Pair(latestGoal, GoalOperationType.DEPOSIT)
                },
                onWithdrawClick = {
                    goalForDepositWithdraw = Pair(latestGoal, GoalOperationType.WITHDRAW)
                },
                onEditClick = {
                    goalToEdit = latestGoal
                },
                onToggleStatus = { nextStatus ->
                    viewModel.updateGoalStatus(latestGoal.id, nextStatus)
                },
                onDeleteClick = {
                    viewModel.deleteGoal(latestGoal.id)
                    selectedGoalForDetail = null
                },
                onDeleteTransactionClick = { txn ->
                    viewModel.deleteGoalTransaction(txn.id, revertBalance = true)
                },
                onEditTransactionClick = { txn ->
                    txnToEdit = txn
                },
                onClearHistoryClick = {
                    viewModel.clearGoalTransactions(latestGoal.id, revertBalance = false)
                },
                onDismiss = { selectedGoalForDetail = null }
            )
        }

        // 1.5 Edit Goal Transaction Dialog
        txnToEdit?.let { txn ->
            GoalTransactionEditDialog(
                goalTransaction = txn,
                allGoals = allGoals,
                accounts = accounts,
                onDismiss = { txnToEdit = null },
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
                    txnToEdit = null
                }
            )
        }

        // 2. Deposit / Withdraw Dialog
        goalForDepositWithdraw?.let { (goal, initialOp) ->
            val currentCurrencyBalance = currencyBalances.find {
                (goal.currencyId > 0 && it.currency.id == goal.currencyId) || it.currency.code.equals(goal.currencyCode, ignoreCase = true)
            }?.balance ?: 0.0

            GoalDepositWithdrawDialog(
                goal = goal,
                initialOperation = initialOp,
                accounts = accounts,
                generalBalance = currentCurrencyBalance,
                onDeposit = { amount, accountId, note ->
                    viewModel.depositToGoal(goal.id, amount, accountId, note)
                },
                onWithdraw = { amount, destinationAccountId, note ->
                    viewModel.withdrawFromGoal(goal.id, amount, destinationAccountId, note)
                },
                onDismiss = { goalForDepositWithdraw = null }
            )
        }

        // 3. Create Goal Dialog
        if (showCreateGoalDialog) {
            CreateOrEditGoalDialog(
                goalToEdit = null,
                activeCurrencies = activeCurrencies,
                accounts = accounts,
                onSave = { title, target, current, currCode, currSymbol, icon, color, cat, date, accId, desc, reminder ->
                    viewModel.createGoal(
                        title = title,
                        targetAmount = target,
                        currentAmount = current,
                        currencyCode = currCode,
                        currencySymbol = currSymbol,
                        iconName = icon,
                        colorHex = color,
                        category = cat,
                        targetDate = date,
                        linkedAccountId = accId,
                        description = desc,
                        reminderFrequency = reminder
                    )
                },
                onDismiss = { showCreateGoalDialog = false }
            )
        }

        // 4. Edit Goal Dialog
        goalToEdit?.let { g ->
            CreateOrEditGoalDialog(
                goalToEdit = g,
                activeCurrencies = activeCurrencies,
                accounts = accounts,
                onSave = { title, target, current, currCode, currSymbol, icon, color, cat, date, accId, desc, reminder ->
                    viewModel.updateGoal(
                        g.copy(
                            title = title,
                            targetAmount = target,
                            currentAmount = current,
                            currencyCode = currCode,
                            currencySymbol = currSymbol,
                            iconName = icon,
                            colorHex = color,
                            category = cat,
                            targetDate = date,
                            linkedAccountId = accId,
                            description = desc,
                            reminderFrequency = reminder
                        )
                    )
                    goalToEdit = null
                },
                onDismiss = { goalToEdit = null }
            )
        }

        // 5. Currency Picker Sheet
        if (showCurrencyPickerSheet) {
            CurrencyPickerBottomSheet(
                currencies = activeCurrencies,
                currencyBalances = currencyBalances,
                selectedCurrency = selectedCurrency,
                onCurrencySelected = { curr ->
                    viewModel.selectCurrency(curr)
                    showCurrencyPickerSheet = false
                },
                onDismiss = { showCurrencyPickerSheet = false }
            )
        }
    }
}

@Composable
fun GoalItemCard(
    goal: FinancialGoalEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DecimalFormat("#,###") }
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentage = (progress * 100).toInt()
    val isCompleted = goal.status == GoalStatus.COMPLETED || progress >= 1f

    val daysLeft: Long? = remember(goal.targetDate) {
        goal.targetDate?.let { target ->
            val diff = target - System.currentTimeMillis()
            if (diff > 0) diff / (24 * 3600 * 1000) else 0L
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, BentoBorder, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag("goal_card_${goal.id}"),
        color = SurfaceWhite,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon + Title & Category + Percentage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(goal.colorHex).copy(alpha = 0.14f))
                            .border(1.dp, Color(goal.colorHex).copy(alpha = 0.25f), RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getGoalImageVector(goal.iconName),
                            contentDescription = null,
                            tint = Color(goal.colorHex),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = goal.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = goal.category,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Percentage Badge / Completed Badge
                if (isCompleted) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = IncomeGreenBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(14.dp))
                            Text("تکمیل شد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(goal.colorHex).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$percentage٪",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(goal.colorHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(goal.colorHex),
                trackColor = BentoBorder
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amounts Row & Deadline Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "${goal.currencySymbol} ${formatter.format(goal.currentAmount)} ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(
                                text = "/ ${formatter.format(goal.targetAmount)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (daysLeft != null && daysLeft > 0 && !isCompleted) {
                        Text(
                            text = "$daysLeft روز تا سررسید (${goal.targetDate?.let { PersianDateHelper.formatSolarDate(it) }})",
                            fontSize = 10.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
