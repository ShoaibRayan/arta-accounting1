package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.util.PersianDateHelper
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailBottomSheet(
    goal: FinancialGoalEntity,
    transactions: List<GoalTransactionEntity>,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onEditClick: () -> Unit,
    onToggleStatus: (GoalStatus) -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteTransactionClick: ((GoalTransactionEntity) -> Unit)? = null,
    onEditTransactionClick: ((GoalTransactionEntity) -> Unit)? = null,
    onClearHistoryClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val formatter = remember { DecimalFormat("#,###") }
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentage = (progress * 100).toInt()
    val remainingAmount = maxOf(0.0, goal.targetAmount - goal.currentAmount)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var txnToDelete by remember { mutableStateOf<GoalTransactionEntity?>(null) }
    var showClearHistoryConfirmDialog by remember { mutableStateOf(false) }

    // Calculate smart savings suggestions based on targetDate
    val daysLeft: Long? = remember(goal.targetDate) {
        goal.targetDate?.let { target ->
            val diff = target - System.currentTimeMillis()
            if (diff > 0) diff / (24 * 3600 * 1000) else 0L
        }
    }

    val monthlySavingsSuggestion = remember(daysLeft, remainingAmount) {
        if (daysLeft != null && daysLeft > 0 && remainingAmount > 0) {
            val months = maxOf(1.0, daysLeft / 30.0)
            remainingAmount / months
        } else null
    }

    val weeklySavingsSuggestion = remember(daysLeft, remainingAmount) {
        if (daysLeft != null && daysLeft > 0 && remainingAmount > 0) {
            val weeks = maxOf(1.0, daysLeft / 7.0)
            remainingAmount / weeks
        } else null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BentoBorder)
            )
        }
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = 700.dp)
                    .padding(horizontal = 20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(goal.colorHex).copy(alpha = 0.15f))
                                .border(1.dp, Color(goal.colorHex).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getGoalImageVector(goal.iconName),
                                contentDescription = null,
                                tint = Color(goal.colorHex),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = goal.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                // Status Chip
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (goal.status) {
                                        GoalStatus.COMPLETED -> IncomeGreenBg
                                        GoalStatus.ARCHIVED -> BentoLavenderSubtle
                                        GoalStatus.ACTIVE -> Color(goal.colorHex).copy(alpha = 0.12f)
                                    }
                                ) {
                                    Text(
                                        text = when (goal.status) {
                                            GoalStatus.COMPLETED -> "تکمیل شده ✓"
                                            GoalStatus.ARCHIVED -> "بایگانی"
                                            GoalStatus.ACTIVE -> "در حال پیشرفت"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (goal.status) {
                                            GoalStatus.COMPLETED -> IncomeGreen
                                            GoalStatus.ARCHIVED -> TextSecondary
                                            GoalStatus.ACTIVE -> Color(goal.colorHex)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "دسته‌بندی: ${goal.category}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Action Icons: Edit & Delete (Generously spaced, no overlapping)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BentoLavenderSubtle)
                                .border(1.dp, BentoBorder, CircleShape)
                                .clickable(onClick = onEditClick)
                                .testTag("edit_goal_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "ویرایش",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ExpenseRedBg)
                                .border(1.dp, ExpenseRed.copy(alpha = 0.3f), CircleShape)
                                .clickable(onClick = { showDeleteConfirmDialog = true })
                                .testTag("delete_goal_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "حذف",
                                tint = ExpenseRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Bento Progress Card
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = BentoLavenderSubtle.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "موجودی جمع‌آوری شده",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${goal.currencySymbol} ${formatter.format(goal.currentAmount)}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoNavyDark
                                        )
                                    }
                                    // Circular Progress Badge
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color(goal.colorHex).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$percentage٪",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(goal.colorHex)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Modern Linear Progress Bar
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = Color(goal.colorHex),
                                    trackColor = BentoBorder
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "هدف: ${goal.currencySymbol} ${formatter.format(goal.targetAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "باقی‌مانده: ${goal.currencySymbol} ${formatter.format(remainingAmount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingAmount == 0.0) IncomeGreen else BentoNavyDark
                                    )
                                }
                            }
                        }
                    }

                    // 2. Goal Description (if present)
                    if (goal.description.isNotBlank()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "توضیحات هدف",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = goal.description,
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. Smart Savings Recommendation Box
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "پیشنهاد هوشمند پس‌انداز",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (remainingAmount <= 0) {
                                    Text(
                                        text = "تبریک! به این هدف مالی رسیدید. پول شما آماده استفاده است.",
                                        fontSize = 12.sp,
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (daysLeft != null && daysLeft > 0) {
                                    Text(
                                        text = "تا تاریخ ${goal.targetDate?.let { PersianDateHelper.formatSolarDate(it) } ?: ""} (${daysLeft} روز باقی‌مانده):",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        monthlySavingsSuggestion?.let { mSave ->
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = SurfaceWhite,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("ماهانه حدوداً", fontSize = 11.sp, color = TextSecondary)
                                                    Text(
                                                        "${goal.currencySymbol} ${formatter.format(mSave)}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoIndigoAccent
                                                    )
                                                }
                                            }
                                        }

                                        weeklySavingsSuggestion?.let { wSave ->
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = SurfaceWhite,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text("هفتگی حدوداً", fontSize = 11.sp, color = TextSecondary)
                                                    Text(
                                                        "${goal.currencySymbol} ${formatter.format(wSave)}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoIndigoAccent
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "برای این هدف سررسید مشخصی تعیین نشده است. هر زمان می‌توانید با واریز مبالغ خرد، پس‌انداز خود را تکمیل کنید.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 4. Prominent Luxury Goal Deposit / Withdrawal Action Panel
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            color = SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "عملیات مالی هدف",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = "موجودی فعلی: ${goal.currencySymbol} ${formatter.format(goal.currentAmount)}",
                                        fontSize = 11.sp,
                                        color = BentoIndigoAccent,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // دکمه واریز / افزودن مبلغ به هدف
                                    Button(
                                        onClick = onDepositClick,
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(50.dp)
                                            .testTag("detail_deposit_button"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.25f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text("افزودن مبلغ به هدف", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        }
                                    }

                                    // دکمه برداشت وجه
                                    OutlinedButton(
                                        onClick = onWithdrawClick,
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .height(50.dp)
                                            .testTag("detail_withdraw_button"),
                                        shape = RoundedCornerShape(16.dp),
                                        enabled = goal.currentAmount > 0,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.2.dp,
                                            if (goal.currentAmount > 0) ExpenseRed.copy(alpha = 0.6f) else BentoBorder
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (goal.currentAmount > 0) ExpenseRedBg.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                                            contentColor = ExpenseRed
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = if (goal.currentAmount > 0) ExpenseRed else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "برداشت وجه",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (goal.currentAmount > 0) ExpenseRed else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Goal Status Controls (Complete / Archive)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val nextStatus = if (goal.status == GoalStatus.COMPLETED) GoalStatus.ACTIVE else GoalStatus.COMPLETED
                                    onToggleStatus(nextStatus)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Text(
                                    text = if (goal.status == GoalStatus.COMPLETED) "فعال‌سازی مجدد" else "علامت‌گذاری به عنوان تکمیل شده",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (goal.status == GoalStatus.COMPLETED) BentoNavyDark else IncomeGreen
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val nextStatus = if (goal.status == GoalStatus.ARCHIVED) GoalStatus.ACTIVE else GoalStatus.ARCHIVED
                                    onToggleStatus(nextStatus)
                                },
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Text(
                                    text = if (goal.status == GoalStatus.ARCHIVED) "خروج از بایگانی" else "بایگانی هدف",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // 6. Goal Transactions History Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تاریخچه واریزها و برداشت‌ها (${transactions.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            if (transactions.isNotEmpty() && onClearHistoryClick != null) {
                                TextButton(
                                    onClick = { showClearHistoryConfirmDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حذف کل تاریخچه", fontSize = 11.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Transactions list items
                    if (transactions.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = BentoLavenderSubtle.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Text(
                                    text = "هنوز تراکنشی برای این هدف ثبت نشده است.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(transactions, key = { it.id }) { txn ->
                            val isDeposit = txn.type == GoalTransactionType.DEPOSIT
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = SurfaceWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
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
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isDeposit) IncomeGreenBg else ExpenseRedBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = if (isDeposit) IncomeGreen else ExpenseRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = if (isDeposit) "واریز به پس‌انداز" else "برداشت از پس‌انداز",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoNavyDark
                                            )
                                            Text(
                                                text = "${PersianDateHelper.formatSolarDate(txn.timestamp)} • ${txn.accountName ?: "بیلانس کل"}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            if (txn.note.isNotBlank()) {
                                                Text(
                                                    text = txn.note,
                                                    fontSize = 11.sp,
                                                    color = TextTertiary
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${if (isDeposit) "+" else "-"}${txn.currencySymbol} ${formatter.format(txn.amount)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDeposit) IncomeGreen else ExpenseRed
                                        )
                                        if (onEditTransactionClick != null) {
                                            IconButton(
                                                onClick = { onEditTransactionClick(txn) },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("edit_goal_txn_${txn.id}_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "ویرایش این تراکنش",
                                                    tint = BentoIndigoAccent.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        if (onDeleteTransactionClick != null) {
                                            IconButton(
                                                onClick = { txnToDelete = txn },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .testTag("delete_goal_txn_${txn.id}_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "حذف این تراکنش",
                                                    tint = ExpenseRed.copy(alpha = 0.75f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            // Confirm Delete Dialog
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = {
                        Text("حذف هدف مالی", fontWeight = FontWeight.Bold, color = ExpenseRed)
                    },
                    text = {
                        Text(
                            text = "آیا از حذف هدف «${goal.title}» اطمینان دارید؟ تمام تاریخچه واریزها و برداشت‌های این هدف حذف خواهد شد. (در صورت تمایل می‌توانید به جای حذف، آن را بایگانی کنید).",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                onDeleteClick()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Text("بله، حذف شود", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }

            // Confirm Delete Single Transaction Dialog
            txnToDelete?.let { txn ->
                val isDep = txn.type == GoalTransactionType.DEPOSIT
                AlertDialog(
                    onDismissRequest = { txnToDelete = null },
                    title = {
                        Text(
                            text = if (isDep) "حذف واریز به هدف" else "حذف برداشت از هدف",
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    },
                    text = {
                        Text(
                            text = "آیا از حذف این ${if (isDep) "واریز" else "برداشت"} به مبلغ ${txn.currencySymbol} ${formatter.format(txn.amount)} اطمینان دارید؟\nبا حذف این تراکنش، مبلغ مربوطه از موجودی هدف کسر/اضافه شده و حساب متصل نیز به وضعیت قبل بازگردانده می‌شود.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val target = txn
                                txnToDelete = null
                                onDeleteTransactionClick?.invoke(target)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Text("حذف و بازگردانی", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { txnToDelete = null }) {
                            Text("انصراف")
                        }
                    }
                )
            }

            // Confirm Clear All History Dialog
            if (showClearHistoryConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showClearHistoryConfirmDialog = false },
                    title = {
                        Text("پاک‌سازی تاریخچه هدف", fontWeight = FontWeight.Bold, color = ExpenseRed)
                    },
                    text = {
                        Text(
                            text = "آیا از حذف تمام تاریخچه واریزها و برداشت‌های این هدف اطمینان دارید؟",
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showClearHistoryConfirmDialog = false
                                onClearHistoryClick?.invoke()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Text("حذف تاریخچه", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showClearHistoryConfirmDialog = false }) {
                            Text("انصراف")
                        }
                    }
                )
            }
        }
    }
}
