package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.PersianDateHelper
import java.util.Locale

val QUICK_ACTION_ICONS = listOf(
    "ShoppingBag" to Icons.Default.ShoppingBag,
    "Fastfood" to Icons.Default.Fastfood,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Home" to Icons.Default.Home,
    "Bolt" to Icons.Default.Bolt,
    "Wifi" to Icons.Default.Wifi,
    "Payments" to Icons.Default.Payments,
    "Person" to Icons.Default.Person,
    "SwapHoriz" to Icons.Default.SwapHoriz,
    "Savings" to Icons.Default.Savings,
    "LocalPharmacy" to Icons.Default.LocalPharmacy,
    "LocalCafe" to Icons.Default.LocalCafe,
    "School" to Icons.Default.School,
    "Storefront" to Icons.Default.Storefront,
    "LocalGasStation" to Icons.Default.LocalGasStation,
    "CardGiftcard" to Icons.Default.CardGiftcard,
    "Star" to Icons.Default.Star,
    "FlashOn" to Icons.Default.FlashOn
)

fun getQuickActionIconVector(iconName: String): ImageVector {
    return QUICK_ACTION_ICONS.find { it.first.equals(iconName, ignoreCase = true) }?.second
        ?: Icons.Default.FlashOn
}

val QUICK_ACTION_COLORS = listOf(
    0xFF001552L, // Bento Navy Dark
    0xFF4355B9L, // Indigo Accent
    0xFF059669L, // Emerald Green
    0xFF2563EBL, // Royal Blue
    0xFFD97706L, // Amber
    0xFFE11D48L, // Rose Red
    0xFF7C3AEDL, // Violet
    0xFF0D9488L, // Teal
    0xFFEA580CL  // Orange
)

fun getActionTypeLabel(type: QuickActionType): String = when (type) {
    QuickActionType.EXPENSE -> "مصرف"
    QuickActionType.INCOME -> "عاید"
    QuickActionType.RECEIVE -> "دریافت"
    QuickActionType.PAY -> "پرداخت"
    QuickActionType.TRANSFER -> "انتقال"
    QuickActionType.GOAL_DEPOSIT -> "هدف مالی"
}

fun getActionTypeColor(type: QuickActionType): Color = when (type) {
    QuickActionType.EXPENSE -> ExpenseRed
    QuickActionType.INCOME -> IncomeGreen
    QuickActionType.RECEIVE -> Color(0xFF0D9488)
    QuickActionType.PAY -> Color(0xFFEA580C)
    QuickActionType.TRANSFER -> Color(0xFF2563EB)
    QuickActionType.GOAL_DEPOSIT -> Color(0xFF7C3AED)
}

/**
 * Quick Action Card for Grid or Horizontal Row
 */
@Composable
fun QuickActionCard(
    quickAction: QuickActionEntity,
    viewModel: FinanceViewModel,
    accounts: List<AccountCardEntity>,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeColor = Color(quickAction.colorHex)
    val typeColor = getActionTypeColor(quickAction.actionType)
    val sourceAccountName = remember(quickAction.sourceAccountId, accounts) {
        if (quickAction.sourceAccountId > 0) {
            accounts.find { it.id == quickAction.sourceAccountId }?.name ?: "کارت بانکی"
        } else {
            "پول نقد"
        }
    }

    Card(
        modifier = modifier
            .testTag("quick_action_card_${quickAction.id}")
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(themeColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getQuickActionIconVector(quickAction.iconName),
                        contentDescription = quickAction.title,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Type Badge
                Box(
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = getActionTypeLabel(quickAction.actionType),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = quickAction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BentoNavyDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Amount Representation
            when (quickAction.amountBehavior) {
                QuickActionAmountBehavior.FIXED -> {
                    Text(
                        text = "${viewModel.formatAmount(quickAction.defaultAmount, quickAction.currencyCode)} ${quickAction.currencySymbol}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColor
                    )
                }
                QuickActionAmountBehavior.VARIABLE -> {
                    Text(
                        text = "~ ${viewModel.formatAmount(quickAction.defaultAmount, quickAction.currencyCode)} ${quickAction.currencySymbol}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
                QuickActionAmountBehavior.EMPTY -> {
                    Text(
                        text = "مبلغ دلخواه",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Wallet/Target detail
            Text(
                text = sourceAccountName,
                fontSize = 10.sp,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Execution Dialog for Quick Action
 * Handles FIXED, VARIABLE, and EMPTY amount behaviors smoothly
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionExecutionDialog(
    quickAction: QuickActionEntity,
    viewModel: FinanceViewModel,
    accounts: List<AccountCardEntity>,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var amountText by remember {
        mutableStateOf(
            if (quickAction.amountBehavior != QuickActionAmountBehavior.EMPTY && quickAction.defaultAmount > 0) {
                if (quickAction.defaultAmount % 1.0 == 0.0) String.format(Locale.US, "%.0f", quickAction.defaultAmount)
                else String.format(Locale.US, "%.3f", quickAction.defaultAmount).trimEnd('0').trimEnd('.')
            } else ""
        )
    }
    var noteText by remember { mutableStateOf(quickAction.note ?: "") }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val themeColor = Color(quickAction.colorHex)
    val typeColor = getActionTypeColor(quickAction.actionType)
    val sourceAccount = remember(quickAction.sourceAccountId, accounts) {
        if (quickAction.sourceAccountId > 0) accounts.find { it.id == quickAction.sourceAccountId } else null
    }
    val sourceAccountName = sourceAccount?.name ?: "پول نقد"
    val destinationAccount = remember(quickAction.destinationAccountId, accounts) {
        if (quickAction.destinationAccountId != null && quickAction.destinationAccountId > 0) {
            accounts.find { it.id == quickAction.destinationAccountId }
        } else null
    }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ثبت سریع: ${quickAction.title}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(themeColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getQuickActionIconVector(quickAction.iconName),
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Prominent Activity Explanation & Details Card (توضیحات و مشخصات فعالیت)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BackgroundCanvas),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top row: Type Badge + Currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = typeColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(7.dp).background(typeColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = getActionTypeLabel(quickAction.actionType),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor
                                )
                            }
                        }

                        Text(
                            text = "${quickAction.currencyCode} (${quickAction.currencySymbol})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BentoBorder.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Detail: Category
                    if (!quickAction.categoryName.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quickAction.categoryName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(text = "دسته‌بندی معامله:", fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Detail: Source Account
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sourceAccountName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Text(text = "حساب / منبع وجه:", fontSize = 12.sp, color = TextSecondary)
                    }

                    // Detail: Counterparty / Recipient (طرف حساب)
                    if (!quickAction.recipientName.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quickAction.recipientName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(text = "طرف حساب:", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    // Detail: Destination Account (for transfer)
                    if (destinationAccount != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = destinationAccount.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(text = "حساب مقصد:", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    // Detail: Target Goal (for goal deposit)
                    if (!quickAction.targetGoalTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quickAction.targetGoalTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(text = "هدف پس‌انداز:", fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    // Detail: Default Note
                    if (!quickAction.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quickAction.note,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = BentoNavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(text = "شرح پیش‌فرض:", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Amount Section: Input with embedded Calculator Button
            if (quickAction.amountBehavior == QuickActionAmountBehavior.FIXED && amountText.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = themeColor.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showCalculatorDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(SurfaceWhite, CircleShape)
                                .border(1.dp, BentoBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "ماشین‌حساب",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "مبلغ مشخص‌شده (ثابت)", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${viewModel.formatAmount(amountText.toDoubleOrNull() ?: quickAction.defaultAmount, quickAction.currencyCode)} ${quickAction.currencySymbol}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = themeColor
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ معامله (${quickAction.currencySymbol})") },
                    placeholder = { Text("مبلغ را وارد کنید") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_action_amount_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        cursorColor = BentoNavyDark,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedLabelColor = BentoNavyDark,
                        unfocusedLabelColor = TextSecondary
                    ),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            IconButton(
                                onClick = { showCalculatorDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "ماشین‌حساب",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = quickAction.currencySymbol,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                )
            }

            if (showCalculatorDialog) {
                MinimalCalculatorDialog(
                    initialValue = amountText.ifBlank { if (quickAction.defaultAmount > 0) quickAction.defaultAmount.toString() else "" },
                    title = "ماشین‌حساب مبلغ",
                    onConfirm = { result ->
                        amountText = result
                        showCalculatorDialog = false
                    },
                    onDismiss = { showCalculatorDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Note Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundCanvas)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = BentoNavyDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = PersianDateHelper.formatSolarDateTime(selectedTimestamp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                }
                Text(text = "تاریخ معامله", fontSize = 12.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("توضیحات (اختیاری)") },
                placeholder = { Text("توضیح یا یادداشت این معامله...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoNavyDark,
                    unfocusedBorderColor = BentoBorder,
                    focusedTextColor = BentoNavyDark,
                    unfocusedTextColor = BentoNavyDark,
                    cursorColor = BentoNavyDark,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedLabelColor = BentoNavyDark,
                    unfocusedLabelColor = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = {
                    val finalAmount = if (quickAction.amountBehavior == QuickActionAmountBehavior.FIXED) {
                        quickAction.defaultAmount
                    } else {
                        amountText.toDoubleOrNull() ?: 0.0
                    }
                    if (finalAmount > 0) {
                        viewModel.executeQuickAction(
                            quickAction = quickAction,
                            amount = finalAmount,
                            customTimestamp = selectedTimestamp,
                            customNote = noteText.ifBlank { null },
                            onSuccess = {
                                onSuccess()
                                onDismiss()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_quick_action_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (quickAction.amountBehavior == QuickActionAmountBehavior.FIXED) "تأیید و ثبت سریع" else "ثبت معامله",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = selectedTimestamp,
            title = "انتخاب تاریخ معامله",
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                selectedTimestamp = it
                showDatePicker = false
            }
        )
    }
}

/**
 * Dialog to Create or Edit a Quick Action Shortcut
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditQuickActionDialog(
    quickActionToEdit: QuickActionEntity? = null,
    initialTransaction: TransactionEntity? = null,
    viewModel: FinanceViewModel,
    accounts: List<AccountCardEntity>,
    activeCurrencies: List<CurrencyEntity>,
    categories: List<CategoryEntity>,
    recipients: List<RecipientEntity>,
    activeGoals: List<FinancialGoalEntity>,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var title by remember {
        mutableStateOf(
            quickActionToEdit?.title
                ?: initialTransaction?.title
                ?: ""
        )
    }
    var actionType by remember {
        mutableStateOf(
            quickActionToEdit?.actionType
                ?: when (initialTransaction?.type) {
                    TransactionType.INCOME -> QuickActionType.INCOME
                    TransactionType.EXPENSE -> QuickActionType.EXPENSE
                    TransactionType.TRANSFER -> QuickActionType.TRANSFER
                    null -> QuickActionType.EXPENSE
                }
        )
    }
    var amountBehavior by remember {
        mutableStateOf(
            quickActionToEdit?.amountBehavior
                ?: if (initialTransaction != null) QuickActionAmountBehavior.FIXED else QuickActionAmountBehavior.FIXED
        )
    }
    var defaultAmountText by remember {
        mutableStateOf(
            if (quickActionToEdit != null && quickActionToEdit.defaultAmount > 0) {
                if (quickActionToEdit.defaultAmount % 1.0 == 0.0) String.format(Locale.US, "%.0f", quickActionToEdit.defaultAmount)
                else String.format(Locale.US, "%.3f", quickActionToEdit.defaultAmount).trimEnd('0').trimEnd('.')
            } else if (initialTransaction != null && initialTransaction.amount > 0) {
                if (initialTransaction.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", initialTransaction.amount)
                else String.format(Locale.US, "%.3f", initialTransaction.amount).trimEnd('0').trimEnd('.')
            } else ""
        )
    }
    var selectedCurrencyId by remember {
        mutableStateOf(
            quickActionToEdit?.currencyId
                ?: initialTransaction?.currencyId
                ?: activeCurrencies.firstOrNull { it.isBaseCurrency }?.id
                ?: 1L
        )
    }
    var selectedCurrencyCode by remember {
        mutableStateOf(
            quickActionToEdit?.currencyCode
                ?: initialTransaction?.currencyCode
                ?: activeCurrencies.firstOrNull { it.isBaseCurrency }?.code
                ?: "AFN"
        )
    }
    var selectedSourceAccountId by remember {
        mutableStateOf(
            quickActionToEdit?.sourceAccountId
                ?: initialTransaction?.accountId
                ?: 0L
        )
    }
    var selectedDestinationAccountId by remember {
        mutableStateOf<Long?>(
            quickActionToEdit?.destinationAccountId
        )
    }
    var selectedCategoryName by remember {
        mutableStateOf(
            quickActionToEdit?.categoryName
                ?: initialTransaction?.category
                ?: categories.firstOrNull { it.type == TransactionType.EXPENSE }?.name
                ?: "عمومی"
        )
    }
    var selectedCategoryId by remember {
        mutableStateOf<Long?>(
            quickActionToEdit?.categoryId
                ?: initialTransaction?.categoryId
                ?: categories.firstOrNull { it.type == TransactionType.EXPENSE }?.id
        )
    }
    var selectedRecipientId by remember {
        mutableStateOf(
            quickActionToEdit?.recipientId
                ?: initialTransaction?.recipientId
                ?: recipients.firstOrNull()?.id
        )
    }
    var selectedTargetGoalId by remember {
        mutableStateOf(
            quickActionToEdit?.targetGoalId
                ?: activeGoals.firstOrNull()?.id
        )
    }
    var selectedIconName by remember {
        mutableStateOf(
            quickActionToEdit?.iconName
                ?: when (actionType) {
                    QuickActionType.EXPENSE -> "ShoppingBag"
                    QuickActionType.INCOME -> "Payments"
                    QuickActionType.RECEIVE, QuickActionType.PAY -> "Person"
                    QuickActionType.TRANSFER -> "SwapHoriz"
                    QuickActionType.GOAL_DEPOSIT -> "Savings"
                }
        )
    }
    var selectedColorHex by remember {
        mutableStateOf(
            quickActionToEdit?.colorHex ?: QUICK_ACTION_COLORS[0]
        )
    }
    var note by remember {
        mutableStateOf(
            quickActionToEdit?.note ?: initialTransaction?.note ?: ""
        )
    }
    var showOnHome by remember {
        mutableStateOf(
            quickActionToEdit?.showOnHome ?: true
        )
    }

    val currSymbol = (if (selectedCurrencyId > 0) activeCurrencies.find { it.id == selectedCurrencyId } else null)?.symbol
        ?: activeCurrencies.find { it.code.equals(selectedCurrencyCode, ignoreCase = true) }?.symbol
        ?: "؋"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 680.dp)
                .testTag("create_quick_action_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                    }
                    Text(
                        text = if (quickActionToEdit != null) "ویرایش میانبر" else "ایجاد عملیات سریع",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("نام میانبر (مثلاً خرید نان، کرایه خانه، معاش...)") },
                        placeholder = { Text("عنوان میانبر را وارد کنید") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_action_title_input"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder,
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedLabelColor = BentoNavyDark,
                            unfocusedLabelColor = TextSecondary
                        )
                    )

                    // Action Type Selector
                    Column {
                        Text(text = "نوع عملیات:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(QuickActionType.values().filter { it != QuickActionType.GOAL_DEPOSIT }) { type ->
                                val isSelected = actionType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        actionType = type
                                        if (type == QuickActionType.EXPENSE) selectedIconName = "ShoppingBag"
                                        else if (type == QuickActionType.INCOME) selectedIconName = "Payments"
                                        else if (type == QuickActionType.RECEIVE || type == QuickActionType.PAY) selectedIconName = "Person"
                                        else if (type == QuickActionType.TRANSFER) selectedIconName = "SwapHoriz"
                                        else if (type == QuickActionType.GOAL_DEPOSIT) selectedIconName = "Savings"
                                    },
                                    label = { Text(getActionTypeLabel(type), fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoNavyDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = BackgroundCanvas,
                                        labelColor = BentoNavyDark
                                    )
                                )
                            }
                        }
                    }

                    // Amount Behavior Selector
                    Column {
                        Text(text = "حالت مبلغ:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val behaviors = listOf(
                                QuickActionAmountBehavior.FIXED to "مبلغ ثابت",
                                QuickActionAmountBehavior.VARIABLE to "مبلغ متغیر",
                                QuickActionAmountBehavior.EMPTY to "مبلغ دلخواه"
                            )
                            behaviors.forEach { (b, label) ->
                                val isSelected = amountBehavior == b
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { amountBehavior = b },
                                    label = { Text(label, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoNavyDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = BackgroundCanvas,
                                        labelColor = BentoNavyDark
                                    )
                                )
                            }
                        }
                    }

                    // Default Amount (if not EMPTY)
                    if (amountBehavior != QuickActionAmountBehavior.EMPTY) {
                        var showDefaultAmountCalculator by remember { mutableStateOf(false) }
                        OutlinedTextField(
                            value = defaultAmountText,
                            onValueChange = { defaultAmountText = it },
                            label = { Text("مبلغ پیش‌فرض (${currSymbol})") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoNavyDark,
                                unfocusedBorderColor = BentoBorder,
                                focusedTextColor = BentoNavyDark,
                                unfocusedTextColor = BentoNavyDark,
                                cursorColor = BentoNavyDark,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedLabelColor = BentoNavyDark,
                                unfocusedLabelColor = TextSecondary
                            ),
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    IconButton(
                                        onClick = { showDefaultAmountCalculator = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Calculate,
                                            contentDescription = "ماشین‌حساب",
                                            tint = BentoNavyDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(currSymbol, fontWeight = FontWeight.Bold, color = BentoNavyDark, modifier = Modifier.padding(end = 8.dp))
                                }
                            }
                        )

                        if (showDefaultAmountCalculator) {
                            MinimalCalculatorDialog(
                                initialValue = defaultAmountText,
                                title = "ماشین‌حساب مبلغ پیش‌فرض",
                                onConfirm = { result ->
                                    defaultAmountText = result
                                    showDefaultAmountCalculator = false
                                },
                                onDismiss = { showDefaultAmountCalculator = false }
                            )
                        }
                    }

                    // Currency Selector
                    Column {
                        Text(text = "ارز معامله:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(activeCurrencies) { curr ->
                                val isSelected = (selectedCurrencyId > 0 && curr.id == selectedCurrencyId) || selectedCurrencyCode.equals(curr.code, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCurrencyId = curr.id
                                        selectedCurrencyCode = curr.code
                                    },
                                    label = { Text("${curr.symbol} ${curr.name}", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoNavyDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = BackgroundCanvas,
                                        labelColor = BentoNavyDark
                                    )
                                )
                            }
                        }
                    }

                    // Source Account / Wallet
                    Column {
                        Text(text = "منبع وجه (کیف‌پول یا حساب):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                val isSelected = selectedSourceAccountId == 0L
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSourceAccountId = 0L },
                                    label = { Text("💵 پول نقد", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoNavyDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = BackgroundCanvas,
                                        labelColor = BentoNavyDark
                                    )
                                )
                            }
                            items(accounts.filter { !it.isFrozen }) { acct ->
                                val isSelected = selectedSourceAccountId == acct.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSourceAccountId = acct.id },
                                    label = { Text("💳 ${acct.name}", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BentoNavyDark,
                                        selectedLabelColor = Color.White,
                                        containerColor = BackgroundCanvas,
                                        labelColor = BentoNavyDark
                                    )
                                )
                            }
                        }
                    }

                    // Contextual Destination / Target
                    when (actionType) {
                        QuickActionType.TRANSFER -> {
                            Column {
                                Text(text = "حساب یا مقصد انتقال:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        val isSelected = selectedDestinationAccountId == null
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedDestinationAccountId = null },
                                            label = { Text("💵 پول نقد", fontSize = 12.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BentoNavyDark,
                                                selectedLabelColor = Color.White,
                                                containerColor = BackgroundCanvas,
                                                labelColor = BentoNavyDark
                                            )
                                        )
                                    }
                                    items(accounts.filter { !it.isFrozen && it.id != selectedSourceAccountId }) { acct ->
                                        val isSelected = selectedDestinationAccountId == acct.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedDestinationAccountId = acct.id },
                                            label = { Text("💳 ${acct.name}", fontSize = 12.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BentoNavyDark,
                                                selectedLabelColor = Color.White,
                                                containerColor = BackgroundCanvas,
                                                labelColor = BentoNavyDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        QuickActionType.RECEIVE, QuickActionType.PAY -> {
                            Column {
                                Text(text = "شخص طرف حساب:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (recipients.isEmpty()) {
                                    Text(text = "هیچ شخصی ثبت نشده است. نام شخص را در عنوان میانبر وارد کنید.", fontSize = 11.sp, color = TextSecondary)
                                } else {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(recipients, key = { it.id }) { r ->
                                            val isSelected = selectedRecipientId == r.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedRecipientId = r.id },
                                                label = { Text("👤 ${r.name}", fontSize = 12.sp) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BentoNavyDark,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = BackgroundCanvas,
                                                    labelColor = BentoNavyDark
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        QuickActionType.GOAL_DEPOSIT -> {
                            Column {
                                Text(text = "هدف مالی مقصد:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (activeGoals.isEmpty()) {
                                    Text(text = "هیچ هدف مالی فعالی وجود ندارد.", fontSize = 11.sp, color = TextSecondary)
                                } else {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(activeGoals) { g ->
                                            val isSelected = selectedTargetGoalId == g.id
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedTargetGoalId = g.id },
                                                label = { Text("🎯 ${g.title}", fontSize = 12.sp) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = BentoNavyDark,
                                                    selectedLabelColor = Color.White,
                                                    containerColor = BackgroundCanvas,
                                                    labelColor = BentoNavyDark
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        QuickActionType.EXPENSE, QuickActionType.INCOME -> {
                            val filterType = if (actionType == QuickActionType.EXPENSE) TransactionType.EXPENSE else TransactionType.INCOME
                            val availableCategories = categories.filter { it.type == filterType && it.isActive }
                            Column {
                                Text(text = "دسته‌بندی:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(availableCategories, key = { it.id }) { cat ->
                                        val isSelected = selectedCategoryId == cat.id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedCategoryId = cat.id
                                                selectedCategoryName = cat.name
                                            },
                                            label = { Text(cat.name, fontSize = 12.sp) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BentoNavyDark,
                                                selectedLabelColor = Color.White,
                                                containerColor = BackgroundCanvas,
                                                labelColor = BentoNavyDark
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Icon Picker
                    Column {
                        Text(text = "آیکون میانبر:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(QUICK_ACTION_ICONS) { (name, icon) ->
                                val isSelected = selectedIconName.equals(name, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) BentoNavyDark else BackgroundCanvas)
                                        .clickable { selectedIconName = name },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = name,
                                        tint = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Color Picker
                    Column {
                        Text(text = "رنگ تم:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(QUICK_ACTION_COLORS) { colorHex ->
                                val isSelected = selectedColorHex == colorHex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorHex))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) BentoNavyDark else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorHex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Note field
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("یادداشت پیش‌فرض (اختیاری)") },
                        placeholder = { Text("توضیحات پیش‌فرض...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder,
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite,
                            focusedLabelColor = BentoNavyDark,
                            unfocusedLabelColor = TextSecondary
                        )
                    )

                    // Show on Home Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BackgroundCanvas)
                            .clickable { showOnHome = !showOnHome }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = showOnHome,
                            onCheckedChange = { showOnHome = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BentoNavyDark)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "نمایش در صفحه اصلی", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                            Text(text = "دسترسی سریع از ویجت صفحه نخست", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        val selectedRec = recipients.firstOrNull { it.id == selectedRecipientId }
                        val trimmedTitle = title.trim().ifBlank {
                            when (actionType) {
                                QuickActionType.EXPENSE -> "مصرف سریع"
                                QuickActionType.INCOME -> "عاید سریع"
                                QuickActionType.RECEIVE -> "دریافت از ${selectedRec?.name ?: "شخص"}"
                                QuickActionType.PAY -> "پرداخت به ${selectedRec?.name ?: "شخص"}"
                                QuickActionType.TRANSFER -> "انتقال حساب"
                                QuickActionType.GOAL_DEPOSIT -> "واریز به هدف"
                            }
                        }
                        val parsedAmount = if (amountBehavior != QuickActionAmountBehavior.EMPTY) {
                            defaultAmountText.toDoubleOrNull() ?: 0.0
                        } else 0.0

                        val targetGoal = activeGoals.find { it.id == selectedTargetGoalId }
                        val resolvedCatId = if (actionType == QuickActionType.EXPENSE || actionType == QuickActionType.INCOME) {
                            selectedCategoryId
                        } else null

                        val entity = QuickActionEntity(
                            id = quickActionToEdit?.id ?: 0L,
                            title = trimmedTitle,
                            iconName = selectedIconName,
                            actionType = actionType,
                            amountBehavior = amountBehavior,
                            defaultAmount = parsedAmount,
                            currencyId = selectedCurrencyId,
                            currencyCode = selectedCurrencyCode,
                            currencySymbol = currSymbol,
                            sourceAccountId = selectedSourceAccountId,
                            destinationAccountId = if (actionType == QuickActionType.TRANSFER) selectedDestinationAccountId else null,
                            categoryId = resolvedCatId,
                            categoryName = selectedCategoryName,
                            recipientId = if (actionType == QuickActionType.RECEIVE || actionType == QuickActionType.PAY) selectedRecipientId else null,
                            recipientName = if (actionType == QuickActionType.RECEIVE || actionType == QuickActionType.PAY) selectedRec?.name else null,
                            targetGoalId = if (actionType == QuickActionType.GOAL_DEPOSIT) selectedTargetGoalId else null,
                            targetGoalTitle = if (actionType == QuickActionType.GOAL_DEPOSIT) targetGoal?.title else null,
                            note = note.ifBlank { null },
                            showOnHome = showOnHome,
                            colorHex = selectedColorHex,
                            displayOrder = quickActionToEdit?.displayOrder ?: 0,
                            usageCount = quickActionToEdit?.usageCount ?: 0
                        )

                        if (quickActionToEdit != null) {
                            viewModel.updateQuickAction(entity) {
                                onSaved()
                                onDismiss()
                            }
                        } else {
                            viewModel.insertQuickAction(entity) {
                                onSaved()
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_quick_action_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                ) {
                    Text(
                        text = if (quickActionToEdit != null) "ذخیره تغییرات" else "ایجاد میانبر",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Management Sheet for all Quick Actions (reorder, edit, delete, toggle home)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsManagementBottomSheet(
    viewModel: FinanceViewModel,
    accounts: List<AccountCardEntity>,
    activeCurrencies: List<CurrencyEntity>,
    categories: List<CategoryEntity>,
    recipients: List<RecipientEntity>,
    activeGoals: List<FinancialGoalEntity>,
    onDismiss: () -> Unit
) {
    val allActions by viewModel.allQuickActions.collectAsState()
    val maxHomeCount by viewModel.maxHomeQuickActions.collectAsState()

    var actionToEdit by remember { mutableStateOf<QuickActionEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var actionToDelete by remember { mutableStateOf<QuickActionEntity?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 650.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مدیریت عملیات سریع",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(BentoLavenderAccent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${allActions.size} میانبر",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Settings: Max visible items on Home screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundCanvas)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(4, 6, 8, 12).forEach { count ->
                        FilterChip(
                            selected = maxHomeCount == count,
                            onClick = { viewModel.setMaxHomeQuickActions(count) },
                            label = { Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoNavyDark,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceWhite,
                                labelColor = BentoNavyDark
                            )
                        )
                    }
                }
                Text(
                    text = "تعداد نمایش در خانه:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add New Shortcut Button
            OutlinedButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_new_quick_action_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoNavyDark),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(BentoNavyDark, BentoIndigoAccent)))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = BentoNavyDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ایجاد میانبر جدید", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of shortcuts
            if (allActions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هنوز میانبری ایجاد نشده است",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای تراکنش‌های پرتکرار مثل نان، کرایه یا معاش میانبر بسازید.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(allActions, key = { _, item -> item.id }) { index, action ->
                        QuickActionManagementRow(
                            action = action,
                            index = index,
                            totalCount = allActions.size,
                            viewModel = viewModel,
                            onMoveUp = {
                                if (index > 0) {
                                    val mutable = allActions.toMutableList()
                                    val item = mutable.removeAt(index)
                                    mutable.add(index - 1, item)
                                    viewModel.reorderQuickActions(mutable)
                                }
                            },
                            onMoveDown = {
                                if (index < allActions.size - 1) {
                                    val mutable = allActions.toMutableList()
                                    val item = mutable.removeAt(index)
                                    mutable.add(index + 1, item)
                                    viewModel.reorderQuickActions(mutable)
                                }
                            },
                            onToggleHome = {
                                viewModel.toggleQuickActionHomeVisibility(action)
                            },
                            onEdit = { actionToEdit = action },
                            onDelete = { actionToDelete = action }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateOrEditQuickActionDialog(
            viewModel = viewModel,
            accounts = accounts,
            activeCurrencies = activeCurrencies,
            categories = categories,
            recipients = recipients,
            activeGoals = activeGoals,
            onDismiss = { showCreateDialog = false },
            onSaved = { showCreateDialog = false }
        )
    }

    if (actionToEdit != null) {
        CreateOrEditQuickActionDialog(
            quickActionToEdit = actionToEdit,
            viewModel = viewModel,
            accounts = accounts,
            activeCurrencies = activeCurrencies,
            categories = categories,
            recipients = recipients,
            activeGoals = activeGoals,
            onDismiss = { actionToEdit = null },
            onSaved = { actionToEdit = null }
        )
    }

    if (actionToDelete != null) {
        AlertDialog(
            onDismissRequest = { actionToDelete = null },
            title = { Text("حذف میانبر", fontWeight = FontWeight.Bold, color = BentoNavyDark) },
            text = { Text("آیا از حذف میانبر «${actionToDelete?.title}» اطمینان دارید؟", color = TextPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        actionToDelete?.let { viewModel.deleteQuickAction(it) }
                        actionToDelete = null
                    }
                ) {
                    Text("حذف", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { actionToDelete = null }) {
                    Text("انصراف", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = SurfaceWhite
        )
    }
}

@Composable
fun QuickActionManagementRow(
    action: QuickActionEntity,
    index: Int,
    totalCount: Int,
    viewModel: FinanceViewModel,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleHome: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val themeColor = Color(action.colorHex)
    val typeColor = getActionTypeColor(action.actionType)

    SwipeToRevealActionsLayout(
        onEdit = onEdit,
        onDelete = onDelete
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_action_management_row_${action.id}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCanvas)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reorder Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "بالا",
                            tint = if (index > 0) BentoNavyDark else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "پایین",
                            tint = if (index < totalCount - 1) BentoNavyDark else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Right side: Info and icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(typeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = getActionTypeLabel(action.actionType), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = typeColor)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = action.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (action.amountBehavior) {
                            QuickActionAmountBehavior.FIXED -> "${viewModel.formatAmount(action.defaultAmount, action.currencyCode)} ${action.currencySymbol}"
                            QuickActionAmountBehavior.VARIABLE -> "~ ${viewModel.formatAmount(action.defaultAmount, action.currencyCode)} ${action.currencySymbol}"
                            QuickActionAmountBehavior.EMPTY -> "مبلغ دلخواه"
                        },
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(themeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getQuickActionIconVector(action.iconName),
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
}

/**
 * برگه شناور انتخاب عملیات سریع که با لمس طولانی کلید Home یا از منوی قابلیت‌ها باز می‌شود
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsSelectorBottomSheet(
    viewModel: FinanceViewModel,
    accounts: List<AccountCardEntity>,
    onSelectAction: (QuickActionEntity) -> Unit,
    onOpenManagement: () -> Unit = {},
    onCreateNewAction: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val quickActions by viewModel.allQuickActions.collectAsState(initial = emptyList())
    val activeActions = remember(quickActions) { quickActions.sortedBy { it.displayOrder } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            // Top Bar: Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "عملیات سریع و میانبرها",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(BentoLavenderSubtle, CircleShape)
                            .border(1.dp, BentoBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "برای ثبت سریع و آنی معامله، میانبر مورد نظر را انتخاب کنید:",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions List
            if (activeActions.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = BackgroundCanvas,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(BentoLavenderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "هنوز میانبری ایجاد نشده است",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "برای فعالیت‌های مکرر مانند خرید نان، کرایه، معاش و قبوض، میانبر دلخواه بسازید.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onCreateNewAction()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ایجاد اولین میانبر", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeActions, key = { it.id }) { action ->
                        val itemColor = Color(action.colorHex)
                        val typeColor = getActionTypeColor(action.actionType)
                        val srcAcc = accounts.find { it.id == action.sourceAccountId }
                        val accName = srcAcc?.name ?: "پول نقد"

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    onDismiss()
                                    onSelectAction(action)
                                }
                                .testTag("quick_action_item_${action.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = BackgroundCanvas,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Amount or Variable Badge
                                Column(horizontalAlignment = Alignment.Start) {
                                    if (action.amountBehavior == QuickActionAmountBehavior.FIXED && action.defaultAmount > 0) {
                                        Text(
                                            text = "${viewModel.formatAmount(action.defaultAmount, action.currencyCode)} ${action.currencySymbol}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = typeColor
                                        )
                                        Text(
                                            text = "مبلغ ثابت",
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = BentoIndigoAccent.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = "مبلغ متغیر",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoIndigoAccent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (action.usageCount > 0) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${action.usageCount} بار استفاده",
                                            fontSize = 9.5.sp,
                                            color = TextTertiary
                                        )
                                    }
                                }

                                // Right: Title, Icon, Account, Type
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = typeColor.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = getActionTypeLabel(action.actionType),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = typeColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = action.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoNavyDark
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))

                                        val subDesc = buildString {
                                            append(accName)
                                            if (!action.recipientName.isNullOrBlank()) {
                                                append(" • ")
                                                append(action.recipientName)
                                            } else if (!action.targetGoalTitle.isNullOrBlank()) {
                                                append(" • ")
                                                append(action.targetGoalTitle)
                                            } else if (!action.categoryName.isNullOrBlank()) {
                                                append(" • ")
                                                append(action.categoryName)
                                            }
                                        }
                                        Text(
                                            text = subDesc,
                                            fontSize = 11.5.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(itemColor.copy(alpha = 0.14f))
                                            .border(1.dp, itemColor.copy(alpha = 0.3f), RoundedCornerShape(13.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getQuickActionIconVector(action.iconName),
                                            contentDescription = null,
                                            tint = itemColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
