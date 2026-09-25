package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AccountCardEntity
import com.example.data.local.FinancialGoalEntity
import com.example.ui.theme.*
import java.text.DecimalFormat

enum class GoalOperationType {
    DEPOSIT,
    WITHDRAW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDepositWithdrawDialog(
    goal: FinancialGoalEntity,
    initialOperation: GoalOperationType = GoalOperationType.DEPOSIT,
    accounts: List<AccountCardEntity>,
    generalBalance: Double = 0.0,
    onDeposit: (amount: Double, accountId: Long?, note: String) -> Unit,
    onWithdraw: (amount: Double, destinationAccountId: Long?, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var operationType by remember { mutableStateOf(initialOperation) }
    val isDeposit = operationType == GoalOperationType.DEPOSIT

    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAccountDropdownExpanded by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }

    val formatter = remember { DecimalFormat("#,###") }
    val remainingAmount = maxOf(0.0, goal.targetAmount - goal.currentAmount)
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val percentage = (progress * 100).toInt()

    // Filter accounts by goal currency and frozen status
    val eligibleAccounts = remember(accounts, goal.currencyCode) {
        val nonFrozen = accounts.filter { !it.isFrozen }
        val matching = nonFrozen.filter { it.currencyCode.equals(goal.currencyCode, ignoreCase = true) }
        if (matching.isNotEmpty()) matching else nonFrozen
    }

    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    val selectedAccount = eligibleAccounts.find { it.id == selectedAccountId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .heightIn(max = 640.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.2.dp, BentoBorder, RoundedCornerShape(26.dp))
                    .testTag("goal_deposit_withdraw_dialog"),
                color = SurfaceWhite,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 20.dp)
                    ) {
                        // Header with Goal info & Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isDeposit) IncomeGreen.copy(alpha = 0.12f)
                                            else ExpenseRed.copy(alpha = 0.12f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isDeposit) IncomeGreen.copy(alpha = 0.25f)
                                            else ExpenseRed.copy(alpha = 0.25f),
                                            RoundedCornerShape(14.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (isDeposit) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = if (isDeposit) "واریز به هدف مالی" else "برداشت از هدف مالی",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "هدف: ${goal.title} • ${goal.currencySymbol}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BentoLavenderSubtle)
                                    .testTag("close_goal_operation_dialog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Operation Type Toggle: واریز به هدف (پس‌انداز) vs برداشت از هدف
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(11.dp),
                                color = if (isDeposit) IncomeGreen else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clickable { operationType = GoalOperationType.DEPOSIT }
                                    .testTag("dialog_op_type_deposit")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (isDeposit) Color.White else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "واریز به هدف",
                                        fontWeight = if (isDeposit) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDeposit) Color.White else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(11.dp),
                                color = if (!isDeposit) ExpenseRed else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .clickable { operationType = GoalOperationType.WITHDRAW }
                                    .testTag("dialog_op_type_withdraw")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (!isDeposit) Color.White else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "برداشت از هدف",
                                        fontWeight = if (!isDeposit) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isDeposit) Color.White else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Goal Status Bento Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = BentoLavenderSubtle.copy(alpha = 0.55f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "موجودی پس‌انداز این هدف",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${goal.currencySymbol} ${formatter.format(goal.currentAmount)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (isDeposit) "مبلغ باقیمانده تا هدف" else "کل مبلغ هدف",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isDeposit) "${goal.currencySymbol} ${formatter.format(remainingAmount)}"
                                            else "${goal.currencySymbol} ${formatter.format(goal.targetAmount)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress Indicator & Percentage
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(3.5.dp)),
                                        color = Color(goal.colorHex),
                                        trackColor = BentoBorder
                                    )
                                    Text(
                                        text = "$percentage%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(goal.colorHex)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Amount Input Field with Calculator Button
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    amountText = input
                                    errorMessage = null
                                }
                            },
                            label = { Text("مبلغ معامله (${goal.currencySymbol}) *") },
                            placeholder = { Text("مثال: 5000") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_amount_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = errorMessage != null,
                            supportingText = {
                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage ?: "",
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoNavyDark,
                                unfocusedTextColor = BentoNavyDark,
                                cursorColor = BentoNavyDark,
                                focusedBorderColor = BentoNavyDark,
                                unfocusedBorderColor = BentoBorder,
                                errorBorderColor = ExpenseRed,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedLabelColor = BentoNavyDark,
                                unfocusedLabelColor = TextSecondary
                            ),
                            trailingIcon = {
                                CalculatorMiniButton(
                                    onClick = { showCalculator = true },
                                    contentDescription = "ماشین‌حساب مبلغ"
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Account / Wallet Selection Dropdown
                        Text(
                            text = if (isDeposit) "برداشت از کدام منبع (پیش‌فرض: بیلانس کل / حساب نقدی):" else "واریز به کدام منبع (پیش‌فرض: بیلانس کل / حساب نقدی):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ExposedDropdownMenuBox(
                            expanded = isAccountDropdownExpanded,
                            onExpandedChange = { isAccountDropdownExpanded = !isAccountDropdownExpanded }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                    .clickable { isAccountDropdownExpanded = true }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("goal_account_selector"),
                                color = SurfaceWhite
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = selectedAccount?.let { "${it.name} (موجودی: ${formatter.format(it.balance)} ${it.currencySymbol})" }
                                                ?: "بیلانس کل (حساب نقدی: ${formatter.format(generalBalance)} ${goal.currencySymbol})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAccountDropdownExpanded)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = isAccountDropdownExpanded,
                                onDismissRequest = { isAccountDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("بیلانس کل (حساب نقدی - پیش‌فرض)", fontWeight = FontWeight.Bold)
                                            Text(
                                                "${formatter.format(generalBalance)} ${goal.currencySymbol}",
                                                color = BentoIndigoAccent,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAccountId = null
                                        isAccountDropdownExpanded = false
                                    }
                                )
                                eligibleAccounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(acc.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${formatter.format(acc.balance)} ${acc.currencySymbol}",
                                                    color = TextSecondary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedAccountId = acc.id
                                            isAccountDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Note Input Field
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("یادداشت یا بابت (اختیاری)") },
                            placeholder = { Text(if (isDeposit) "مثال: پس‌انداز از معاش ماهانه" else "مثال: برداشت برای خرید ضروری") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_note_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
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
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Sticky Action Buttons at bottom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceWhite,
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("cancel_goal_operation"),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Text("انصراف", color = TextSecondary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val amount = amountText.toDoubleOrNull()
                                    if (amount == null || amount <= 0) {
                                        errorMessage = "لطفاً مبلغ معتبری وارد کنید"
                                        return@Button
                                    }
                                    if (isDeposit) {
                                        if (selectedAccount != null) {
                                            if (selectedAccount.balance < amount) {
                                                errorMessage = "موجودی حساب ${selectedAccount.name} کافی نیست (${formatter.format(selectedAccount.balance)} ${selectedAccount.currencySymbol})"
                                                return@Button
                                            }
                                        } else {
                                            if (generalBalance > 0.0 && generalBalance < amount) {
                                                errorMessage = "موجودی حساب نقدی (بیلانس کل) کافی نیست (${formatter.format(generalBalance)} ${goal.currencySymbol})"
                                                return@Button
                                            }
                                        }
                                        onDeposit(amount, selectedAccountId, noteText)
                                    } else {
                                        if (amount > goal.currentAmount) {
                                            errorMessage = "مبلغ برداشت نمی‌تواند بیشتر از موجودی هدف باشد (${formatter.format(goal.currentAmount)} ${goal.currencySymbol})"
                                            return@Button
                                        }
                                        onWithdraw(amount, selectedAccountId, noteText)
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("confirm_goal_operation"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDeposit) IncomeGreen else ExpenseRed
                                )
                            ) {
                                Text(
                                    text = if (isDeposit) "تأیید واریز" else "تأیید برداشت",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Popup Minimal Calculator for amount input
    if (showCalculator) {
        MinimalCalculatorDialog(
            initialValue = amountText,
            title = if (isDeposit) "ماشین‌حساب واریز به هدف" else "ماشین‌حساب برداشت از هدف",
            onConfirm = { evaluated ->
                amountText = evaluated
                errorMessage = null
                showCalculator = false
            },
            onDismiss = { showCalculator = false }
        )
    }
}
