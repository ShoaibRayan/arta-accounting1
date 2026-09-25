package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AccountCardEntity
import com.example.data.local.FinancialGoalEntity
import com.example.data.local.GoalTransactionEntity
import com.example.data.local.GoalTransactionType
import com.example.data.local.TransactionEntity
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianDateHelper
import java.text.DecimalFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTransactionEditDialog(
    goalTransaction: GoalTransactionEntity,
    linkedTransaction: TransactionEntity? = null,
    allGoals: List<FinancialGoalEntity>,
    accounts: List<AccountCardEntity>,
    onDismiss: () -> Unit,
    onSave: (
        goalTransactionId: Long,
        newGoalId: Long,
        newAmount: Double,
        newType: GoalTransactionType,
        newAccountId: Long?,
        newNote: String,
        newTimestamp: Long
    ) -> Unit
) {
    val formatter = remember { DecimalFormat("#,###.##") }

    // Selected Goal
    var selectedGoalId by remember { mutableStateOf(goalTransaction.goalId) }
    val currentGoal = allGoals.find { it.id == selectedGoalId } ?: allGoals.firstOrNull()

    // Operation Type: Deposit or Withdraw (واریز یا برداشت)
    var operationType by remember { mutableStateOf(goalTransaction.type) }

    // Amount Text
    var amountText by remember {
        val initialAmt = goalTransaction.amount
        val formatted = if (initialAmt % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", initialAmt)
        } else {
            String.format(Locale.US, "%.3f", initialAmt).trimEnd('0').trimEnd('.')
        }
        mutableStateOf(formatted)
    }

    // Account selection (default to null: بیلانس کل)
    var selectedAccountId by remember { mutableStateOf<Long?>(goalTransaction.accountId) }
    val selectedAccount = accounts.find { it.id == selectedAccountId }

    // Note / Reason
    var noteText by remember {
        val initialNote = goalTransaction.note.ifBlank {
            linkedTransaction?.note ?: ""
        }
        mutableStateOf(initialNote)
    }

    // Timestamp & Date
    var timestamp by remember {
        mutableLongStateOf(linkedTransaction?.timestamp ?: goalTransaction.timestamp)
    }

    // Dropdown states
    var isGoalDropdownExpanded by remember { mutableStateOf(false) }
    var isAccountDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isDeposit = operationType == GoalTransactionType.DEPOSIT
    val currencySymbol = currentGoal?.currencySymbol ?: goalTransaction.currencySymbol
    val currencyCode = currentGoal?.currencyCode ?: goalTransaction.currencyCode

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .heightIn(max = 680.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.2.dp, BentoBorder, RoundedCornerShape(26.dp))
                    .testTag("goal_transaction_edit_dialog"),
                color = SurfaceWhite,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Top Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundCanvas)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(BentoIndigoAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ویرایش عملیات پس‌انداز هدف",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "تنظیم واریز یا برداشت متصل به اهداف مالی",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_goal_edit_dialog_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                        }
                    }

                    // Content form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Goal Selection
                        Text(
                            text = "هدف مالی مربوطه",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ExposedDropdownMenuBox(
                            expanded = isGoalDropdownExpanded,
                            onExpandedChange = { isGoalDropdownExpanded = !isGoalDropdownExpanded }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, BentoBorder, RoundedCornerShape(14.dp)),
                                color = BackgroundCanvas
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Flag,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = currentGoal?.title ?: "انتخاب هدف مالی",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGoalDropdownExpanded)
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = isGoalDropdownExpanded,
                                onDismissRequest = { isGoalDropdownExpanded = false }
                            ) {
                                allGoals.forEach { goal ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(goal.title, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${formatter.format(goal.currentAmount)} / ${formatter.format(goal.targetAmount)} ${goal.currencySymbol}",
                                                    color = BentoIndigoAccent,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedGoalId = goal.id
                                            isGoalDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // 2. Operation Type Toggle: واریز به هدف vs برداشت از هدف
                        Text(
                            text = "نوع عملیات هدف مالی:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BackgroundCanvas)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Deposit button
                            Surface(
                                shape = RoundedCornerShape(11.dp),
                                color = if (isDeposit) IncomeGreen else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { operationType = GoalTransactionType.DEPOSIT }
                                    .testTag("goal_op_type_deposit")
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "واریز به هدف (پس‌انداز)",
                                        fontWeight = if (isDeposit) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isDeposit) Color.White else TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Withdraw button
                            Surface(
                                shape = RoundedCornerShape(11.dp),
                                color = if (!isDeposit) ExpenseRed else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable { operationType = GoalTransactionType.WITHDRAWAL }
                                    .testTag("goal_op_type_withdraw")
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
                                        modifier = Modifier.size(18.dp)
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

                        // 3. Amount Field with Mini Calculator Button
                        Text(
                            text = "مبلغ عملیات (${currencySymbol})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() || it == '.' }
                                if (clean.count { it == '.' } <= 1) {
                                    amountText = clean
                                    errorMessage = null
                                }
                            },
                            label = { Text("مبلغ") },
                            placeholder = { Text("0") },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = currencySymbol,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    CalculatorMiniButton(
                                        onClick = { showCalculator = true }
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = errorMessage != null,
                            supportingText = {
                                if (errorMessage != null) {
                                    Text(
                                        text = errorMessage ?: "",
                                        color = ExpenseRed,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_edit_amount_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                errorBorderColor = ExpenseRed,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite
                            )
                        )

                        // 4. Source / Destination Account (Default: Total Balance / بیلانس کل)
                        Text(
                            text = if (isDeposit) "حساب مبدأ (کسر از)" else "حساب مقصد (واریز به)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ExposedDropdownMenuBox(
                            expanded = isAccountDropdownExpanded,
                            onExpandedChange = { isAccountDropdownExpanded = !isAccountDropdownExpanded }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, BentoBorder, RoundedCornerShape(14.dp)),
                                color = BackgroundCanvas
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (selectedAccount == null) Icons.Default.AccountBalanceWallet else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = selectedAccount?.let { "${it.name} (موجودی: ${formatter.format(it.balance)} ${it.currencySymbol})" }
                                                ?: "بیلانس کل (موجودی عمومی)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
                                            Text("بیلانس کل (موجودی عمومی)", fontWeight = FontWeight.Bold)
                                            Text(
                                                "پیش‌فرض",
                                                color = BentoIndigoAccent,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAccountId = null
                                        isAccountDropdownExpanded = false
                                    }
                                )
                                accounts.filter { !it.isFrozen }.forEach { acc ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(acc.name, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${acc.currencySymbol} ${formatter.format(acc.balance)}",
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
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

                        // 5. Date selection (Solar Date)
                        Text(
                            text = "تاریخ عملیات",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                .clickable { showDatePicker = true },
                            color = BackgroundCanvas
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = PersianDateHelper.formatSolarDate(timestamp),
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "تغییر تاریخ",
                                    color = BentoIndigoAccent,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 6. Note Field
                        Text(
                            text = "یادداشت یا بابت",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            placeholder = { Text(if (isDeposit) "مثال: پس‌انداز برای هدف" else "مثال: برداشت از هدف مالی") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_edit_note_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite
                            )
                        )
                    }

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundCanvas)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("cancel_goal_txn_edit_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Text("انصراف", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                val amount = amountText.toDoubleOrNull()
                                if (amount == null || amount <= 0) {
                                    errorMessage = "لطفاً مبلغ معتبری وارد کنید"
                                    return@Button
                                }
                                val targetGoal = currentGoal ?: return@Button

                                // Validation
                                if (operationType == GoalTransactionType.DEPOSIT && selectedAccount != null) {
                                    if (selectedAccount.balance < amount) {
                                        errorMessage = "موجودی حساب ${selectedAccount.name} کافی نیست (${selectedAccount.currencySymbol} ${formatter.format(selectedAccount.balance)})"
                                        return@Button
                                    }
                                }

                                onSave(
                                    goalTransaction.id,
                                    targetGoal.id,
                                    amount,
                                    operationType,
                                    selectedAccountId,
                                    noteText.trim(),
                                    timestamp
                                )
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(50.dp)
                                .testTag("save_goal_txn_edit_btn"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDeposit) IncomeGreen else BentoNavyDark
                            )
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ذخیره تغییرات",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Solar Date Picker Dialog
    if (showDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = timestamp,
            title = "انتخاب تاریخ عملیات هدف",
            onDismiss = { showDatePicker = false },
            onDateSelected = { selectedTime ->
                timestamp = selectedTime
                showDatePicker = false
            }
        )
    }

    // Calculator Dialog
    if (showCalculator) {
        MinimalCalculatorDialog(
            initialValue = amountText,
            title = "محاسبه مبلغ",
            onConfirm = { calculated ->
                amountText = calculated
                showCalculator = false
            },
            onDismiss = { showCalculator = false }
        )
    }
}
