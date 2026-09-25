package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.util.PersianDateHelper
import java.util.Calendar

data class GoalPreset(
    val category: String,
    val defaultTitle: String,
    val iconName: String,
    val colorHex: Long
)

val GOAL_PRESETS = listOf(
    GoalPreset("صندوق اضطراری", "صندوق اضطراری", "Shield", 0xFF059669),
    GoalPreset("خرید موتر یا موتورسایکل", "خرید موتر کرولا", "Car", 0xFF2563EB),
    GoalPreset("خرید موبایل", "خرید موبایل هوشمند", "Phone", 0xFF7C3AED),
    GoalPreset("خرید خانه", "پیش‌پرداخت خرید خانه", "Home", 0xFF0D9488),
    GoalPreset("هزینه سفر", "سفر زیارتی یا تفریحی", "Flight", 0xFF0284C7),
    GoalPreset("پس‌انداز برای تولد طفل", "هزینه‌های تولد طفل", "Child", 0xFFEC4899),
    GoalPreset("پرداخت قرض", "تسویه بدهی و قرض", "CreditCard", 0xFFE11D48),
    GoalPreset("پس‌انداز عمومی", "پس‌انداز آینده", "Savings", 0xFF4F46E5),
    GoalPreset("هدف سفارشی", "هدف من", "Star", 0xFFF59E0B)
)

val GOAL_ICONS = listOf(
    "Savings" to Icons.Default.Savings,
    "Shield" to Icons.Default.Shield,
    "Car" to Icons.Default.DirectionsCar,
    "Phone" to Icons.Default.PhoneIphone,
    "Home" to Icons.Default.Home,
    "Flight" to Icons.Default.FlightTakeoff,
    "Child" to Icons.Default.ChildCare,
    "CreditCard" to Icons.Default.CreditCard,
    "Star" to Icons.Default.Star,
    "Laptop" to Icons.Default.LaptopMac,
    "School" to Icons.Default.School,
    "Shopping" to Icons.Default.ShoppingBag
)

fun getGoalImageVector(iconName: String): ImageVector {
    return GOAL_ICONS.find { it.first.equals(iconName, ignoreCase = true) }?.second ?: Icons.Default.Savings
}

val GOAL_COLORS = listOf(
    0xFF059669L, // Emerald
    0xFF2563EBL, // Royal Blue
    0xFF7C3AEDL, // Purple
    0xFF0D9488L, // Teal
    0xFF0284C7L, // Sky Blue
    0xFFEC4899L, // Pink
    0xFFE11D48L, // Rose / Red
    0xFFF59E0BL, // Amber Gold
    0xFF001552L  // Bento Navy
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditGoalDialog(
    goalToEdit: FinancialGoalEntity? = null,
    activeCurrencies: List<CurrencyEntity>,
    accounts: List<AccountCardEntity>,
    onSave: (
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        currencyCode: String,
        currencySymbol: String,
        iconName: String,
        colorHex: Long,
        category: String,
        targetDate: Long?,
        linkedAccountId: Long?,
        description: String,
        reminderFrequency: GoalReminderFrequency
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = goalToEdit != null

    var title by remember { mutableStateOf(goalToEdit?.title ?: "") }
    var description by remember { mutableStateOf(goalToEdit?.description ?: "") }
    var targetAmountText by remember { mutableStateOf(goalToEdit?.targetAmount?.let { if (it > 0) it.toLong().toString() else "" } ?: "") }
    var currentAmountText by remember { mutableStateOf(goalToEdit?.currentAmount?.let { if (it > 0) it.toLong().toString() else "0" } ?: "0") }

    val defaultCurrency = activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull()
    var selectedCurrencyCode by remember { mutableStateOf(goalToEdit?.currencyCode ?: defaultCurrency?.code ?: "AFN") }
    var selectedCurrencySymbol by remember { mutableStateOf(goalToEdit?.currencySymbol ?: defaultCurrency?.symbol ?: "؋") }

    var selectedCategory by remember { mutableStateOf(goalToEdit?.category ?: GOAL_PRESETS[0].category) }
    var selectedIconName by remember { mutableStateOf(goalToEdit?.iconName ?: GOAL_PRESETS[0].iconName) }
    var selectedColorHex by remember { mutableStateOf(goalToEdit?.colorHex ?: GOAL_PRESETS[0].colorHex) }

    val eligibleAccounts = remember(accounts) { accounts.filter { !it.isFrozen } }
    var targetDate by remember { mutableStateOf<Long?>(goalToEdit?.targetDate) }
    var selectedAccountId by remember { mutableStateOf<Long?>(goalToEdit?.linkedAccountId) }
    var reminderFrequency by remember { mutableStateOf(goalToEdit?.reminderFrequency ?: GoalReminderFrequency.MONTHLY) }

    var isCurrencyMenuOpen by remember { mutableStateOf(false) }
    var isAccountMenuOpen by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var targetAmountError by remember { mutableStateOf<String?>(null) }
    var currentAmountError by remember { mutableStateOf<String?>(null) }
    var showTargetAmountCalculator by remember { mutableStateOf(false) }
    var showCurrentAmountCalculator by remember { mutableStateOf(false) }

    var showTargetDatePicker by remember { mutableStateOf(false) }
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
                    .heightIn(max = 700.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, BentoBorder, RoundedCornerShape(28.dp))
                    .testTag("create_or_edit_goal_dialog"),
                color = SurfaceWhite,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    .background(Color(selectedColorHex).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getGoalImageVector(selectedIconName),
                                    contentDescription = null,
                                    tint = Color(selectedColorHex),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isEditing) "ویرایش هدف مالی" else "ایجاد هدف مالی جدید",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = "پس‌انداز هوشمند و هدفمند",
                                    fontSize = 11.sp,
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
                                .testTag("close_create_goal_dialog")
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

                    // Scrollable form content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp)
                    ) {
                        // 1. Preset Goal Templates (Quick Picks)
                        if (!isEditing) {
                            Text(
                                text = "انتخاب سریع از نمونه‌های آماده:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GOAL_PRESETS.forEach { preset ->
                                    val isSelected = selectedCategory == preset.category
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(preset.colorHex) else BentoBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                selectedCategory = preset.category
                                                title = preset.defaultTitle
                                                selectedIconName = preset.iconName
                                                selectedColorHex = preset.colorHex
                                            },
                                        color = if (isSelected) Color(preset.colorHex).copy(alpha = 0.12f) else SurfaceWhite
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = getGoalImageVector(preset.iconName),
                                                contentDescription = null,
                                                tint = Color(preset.colorHex),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = preset.category,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color(preset.colorHex) else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 2. Goal Title Input
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = null
                            },
                            label = { Text("نام هدف مالی *") },
                            placeholder = { Text("مثال: خرید موتر، پس‌انداز عروسی") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_title_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = titleError != null,
                            supportingText = {
                                if (titleError != null) {
                                    Text(
                                        text = titleError ?: "",
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
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
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Target Amount & Currency Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Target Amount
                            OutlinedTextField(
                                value = targetAmountText,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == '.' }) {
                                        targetAmountText = input
                                        targetAmountError = null
                                    }
                                },
                                label = { Text("مبلغ هدف *") },
                                placeholder = { Text("50000") },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("goal_target_amount_input"),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                isError = targetAmountError != null,
                                supportingText = {
                                    if (targetAmountError != null) {
                                        Text(
                                            text = targetAmountError ?: "",
                                            color = ExpenseRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    CalculatorMiniButton(
                                        onClick = { showTargetAmountCalculator = true },
                                        contentDescription = "ماشین‌حساب مبلغ هدف"
                                    )
                                },
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
                                )
                            )

                            // Currency Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = "$selectedCurrencyCode ($selectedCurrencySymbol)",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("واحد پول") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isCurrencyMenuOpen = true }
                                        .testTag("goal_currency_selector"),
                                    shape = RoundedCornerShape(14.dp),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { isCurrencyMenuOpen = true }
                                        )
                                    },
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

                                DropdownMenu(
                                    expanded = isCurrencyMenuOpen,
                                    onDismissRequest = { isCurrencyMenuOpen = false }
                                ) {
                                    activeCurrencies.forEach { curr ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(curr.flagEmoji)
                                                    Text(curr.name, fontWeight = FontWeight.Bold)
                                                    Text("(${curr.code})", color = TextSecondary)
                                                }
                                            },
                                            onClick = {
                                                selectedCurrencyCode = curr.code
                                                selectedCurrencySymbol = curr.symbol
                                                isCurrencyMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Starting / Current Amount (Only when creating, or for adjustment)
                        OutlinedTextField(
                            value = currentAmountText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() || it == '.' }) {
                                    currentAmountText = input
                                    currentAmountError = null
                                }
                            },
                            label = { Text("مبلغ پس‌انداز شروع / فعلی") },
                            placeholder = { Text("0") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_starting_amount_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            isError = currentAmountError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                CalculatorMiniButton(
                                    onClick = { showCurrentAmountCalculator = true },
                                    contentDescription = "ماشین‌حساب مبلغ فعلی"
                                )
                            },
                            supportingText = {
                                if (currentAmountError != null) {
                                    Text(
                                        currentAmountError ?: "",
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else if (!isEditing && (currentAmountText.toDoubleOrNull() ?: 0.0) > 0) {
                                    Text(
                                        "این مبلغ از کیف پول انتخابی کسر و به این هدف تخصیص می‌یابد.",
                                        fontSize = 11.sp,
                                        color = BentoIndigoAccent
                                    )
                                }
                            },
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
                            )
                        )

                        // 5. Source Account for Starting Balance
                        val startAmount = currentAmountText.toDoubleOrNull() ?: 0.0
                        if (!isEditing && startAmount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "حساب منبع برای کسر مبلغ شروع:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                val currentAcc = eligibleAccounts.find { it.id == selectedAccountId }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                        .clickable { isAccountMenuOpen = true }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    color = SurfaceWhite
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = currentAcc?.name ?: "بیلانس کل (حساب عمومی)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                DropdownMenu(
                                    expanded = isAccountMenuOpen,
                                    onDismissRequest = { isAccountMenuOpen = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("بیلانس کل (حساب عمومی)") },
                                        onClick = {
                                            selectedAccountId = null
                                            isAccountMenuOpen = false
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
                                                    Text("${acc.currencySymbol} ${acc.balance}", color = TextSecondary)
                                                }
                                            },
                                            onClick = {
                                                selectedAccountId = acc.id
                                                isAccountMenuOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 6. Target Date / Deadline Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سررسید / تاریخ پایان هدف:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            TextButton(
                                onClick = { showTargetDatePicker = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = BentoIndigoAccent
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "انتخاب تاریخ دستی",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoAccent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        val now = System.currentTimeMillis()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val chips = listOf(
                                "بدون سررسید" to null,
                                "۱ ماه دیگر" to (now + 30L * 24 * 3600 * 1000),
                                "۳ ماه دیگر" to (now + 90L * 24 * 3600 * 1000),
                                "۶ ماه دیگر" to (now + 180L * 24 * 3600 * 1000),
                                "۱ سال دیگر" to (now + 365L * 24 * 3600 * 1000)
                            )

                            chips.forEach { (label, timestamp) ->
                                val isSelected = if (timestamp == null) targetDate == null else (targetDate != null && Math.abs(targetDate!! - timestamp) < 5L * 24 * 3600 * 1000)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { targetDate = timestamp },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }

                            // Manual date button as a chip option too
                            FilterChip(
                                selected = targetDate != null && chips.none { it.second != null && Math.abs(targetDate!! - it.second!!) < 5L * 24 * 3600 * 1000 },
                                onClick = { showTargetDatePicker = true },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (targetDate != null && chips.none { it.second != null && Math.abs(targetDate!! - it.second!!) < 5L * 24 * 3600 * 1000 })
                                            PersianDateHelper.formatSolarDate(targetDate!!)
                                        else "انتخاب از تقویم...",
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }

                        if (targetDate != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "سررسید انتخابی: ${PersianDateHelper.formatSolarDate(targetDate!!)}",
                                    fontSize = 12.sp,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { targetDate = null },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "حذف سررسید",
                                        fontSize = 11.sp,
                                        color = ExpenseRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7. Savings Reminder Frequency
                        Text(
                            text = "یادآوری پس‌انداز:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                GoalReminderFrequency.NONE to "بدون یادآوری",
                                GoalReminderFrequency.WEEKLY to "هفتگی",
                                GoalReminderFrequency.MONTHLY to "ماهانه"
                            ).forEach { (freq, label) ->
                                FilterChip(
                                    selected = reminderFrequency == freq,
                                    onClick = { reminderFrequency = freq },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 8. Icon & Color Picker
                        Text(
                            text = "آیکون و رنگ هدف:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Icon Selector Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GOAL_ICONS.forEach { (name, vector) ->
                                val isSelected = selectedIconName.equals(name, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(selectedColorHex) else BentoLavenderSubtle)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) Color(selectedColorHex) else BentoBorder,
                                            CircleShape
                                        )
                                        .clickable { selectedIconName = name },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = vector,
                                        contentDescription = name,
                                        tint = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Color Selector Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GOAL_COLORS.forEach { colorVal ->
                                val isSelected = selectedColorHex == colorVal
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorVal))
                                        .border(
                                            if (isSelected) 3.dp else 1.dp,
                                            if (isSelected) BentoNavyDark else Color.White,
                                            CircleShape
                                        )
                                        .clickable { selectedColorHex = colorVal }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 9. Optional Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("توضیحات اختیاری") },
                            placeholder = { Text("مثال: خرید موتر کرولا سفید رنگ بدون تصادف") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_description_input"),
                            shape = RoundedCornerShape(14.dp),
                            minLines = 2,
                            maxLines = 3,
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

                    // Sticky Action Buttons (Save & Cancel)
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
                                    .testTag("cancel_create_goal"),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Text("انصراف", color = TextSecondary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    var hasError = false
                                    if (title.isBlank()) {
                                        titleError = "لطفاً نام هدف را وارد کنید"
                                        hasError = true
                                    }
                                    val targetAmount = targetAmountText.toDoubleOrNull()
                                    if (targetAmount == null || targetAmount <= 0) {
                                        targetAmountError = "مبلغ هدف باید بیشتر از صفر باشد"
                                        hasError = true
                                    }
                                    val currentAmount = currentAmountText.toDoubleOrNull() ?: 0.0
                                    if (currentAmount < 0) {
                                        currentAmountError = "مبلغ فعلی نمی‌تواند منفی باشد"
                                        hasError = true
                                    }
                                    if (hasError || targetAmount == null) return@Button

                                    onSave(
                                        title.trim(),
                                        targetAmount,
                                        currentAmount,
                                        selectedCurrencyCode,
                                        selectedCurrencySymbol,
                                        selectedIconName,
                                        selectedColorHex,
                                        selectedCategory,
                                        targetDate,
                                        selectedAccountId,
                                        description.trim(),
                                        reminderFrequency
                                    )
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("save_goal_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                            ) {
                                Text(
                                    text = if (isEditing) "ذخیره تغییرات" else "ایجاد هدف مالی",
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

    if (showTargetAmountCalculator) {
        MinimalCalculatorDialog(
            initialValue = targetAmountText,
            title = "ماشین‌حساب مبلغ هدف",
            onConfirm = { evaluated ->
                targetAmountText = evaluated
                targetAmountError = null
                showTargetAmountCalculator = false
            },
            onDismiss = { showTargetAmountCalculator = false }
        )
    }

    if (showCurrentAmountCalculator) {
        MinimalCalculatorDialog(
            initialValue = currentAmountText,
            title = "ماشین‌حساب مبلغ فعلی",
            onConfirm = { evaluated ->
                currentAmountText = evaluated
                currentAmountError = null
                showCurrentAmountCalculator = false
            },
            onDismiss = { showCurrentAmountCalculator = false }
        )
    }

    if (showTargetDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = targetDate ?: (System.currentTimeMillis() + 30L * 24 * 3600 * 1000),
            title = "انتخاب تاریخ سررسید هدف",
            onDismiss = { showTargetDatePicker = false },
            onDateSelected = { selectedTs ->
                targetDate = selectedTs
                showTargetDatePicker = false
            }
        )
    }
}
