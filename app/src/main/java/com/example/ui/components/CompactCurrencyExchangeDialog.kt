package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CurrencyEntity
import com.example.data.local.TransactionEntity
import com.example.ui.screens.parseLocalizedNumber
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
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.CurrencyBalanceInfo
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactCurrencyExchangeDialog(
    viewModel: FinanceViewModel,
    currencies: List<CurrencyEntity>,
    currencyBalances: List<CurrencyBalanceInfo>,
    onDismiss: () -> Unit,
    onNavigateToFullSettingsExchange: () -> Unit = {},
    editingExchangeFromTxn: TransactionEntity? = null,
    editingExchangeToTxn: TransactionEntity? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing = editingExchangeFromTxn != null && editingExchangeToTxn != null
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (currencies.size < 2) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("حداقل دو ارز فعال برای انجام صرافی و تبدیل نیاز است.", fontWeight = FontWeight.Bold, color = BentoNavyDark)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onDismiss()
                        onNavigateToFullSettingsExchange()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent)
                ) {
                    Text("مدیریت و افزودن ارزها", color = Color.White)
                }
            }
        }
        return
    }

    val initialFromIdx = remember(editingExchangeFromTxn) {
        if (editingExchangeFromTxn != null) {
            currencies.indexOfFirst { it.code.equals(editingExchangeFromTxn.currencyCode, ignoreCase = true) }.takeIf { it >= 0 } ?: 0
        } else 0
    }
    val initialToIdx = remember(editingExchangeToTxn) {
        if (editingExchangeToTxn != null) {
            currencies.indexOfFirst { it.code.equals(editingExchangeToTxn.currencyCode, ignoreCase = true) }.takeIf { it >= 0 } ?: 1.coerceAtMost(currencies.lastIndex)
        } else 1.coerceAtMost(currencies.lastIndex)
    }

    var fromIndex by remember { mutableIntStateOf(initialFromIdx) }
    var toIndex by remember { mutableIntStateOf(initialToIdx) }

    val fromCurrency = currencies.getOrElse(fromIndex) { currencies.first() }
    val toCurrency = currencies.getOrElse(toIndex) { currencies.last() }

    val rawFromBalance = currencyBalances.find { (fromCurrency.id > 0 && it.currency.id == fromCurrency.id) || it.currency.code.equals(fromCurrency.code, ignoreCase = true) }?.balance ?: 0.0
    val fromBalance = rawFromBalance + (if (isEditing && editingExchangeFromTxn != null && ((fromCurrency.id > 0 && editingExchangeFromTxn.currencyId > 0 && editingExchangeFromTxn.currencyId == fromCurrency.id) || editingExchangeFromTxn.currencyCode.equals(fromCurrency.code, ignoreCase = true))) editingExchangeFromTxn.amount else 0.0)
    val toBalance = currencyBalances.find { (toCurrency.id > 0 && it.currency.id == toCurrency.id) || it.currency.code.equals(toCurrency.code, ignoreCase = true) }?.balance ?: 0.0

    fun getFormattedRate(fCurr: CurrencyEntity, tCurr: CurrencyEntity): String {
        if ((fCurr.id > 0 && tCurr.id > 0 && fCurr.id == tCurr.id) || fCurr.code.equals(tCurr.code, ignoreCase = true)) return "1.0"
        val r = if (tCurr.exchangeRateToBase > 0) fCurr.exchangeRateToBase / tCurr.exchangeRateToBase else 1.0
        return if (r == 1.0) "1.0" else String.format(Locale.US, "%.4f", r).trimEnd('0').trimEnd('.')
    }

    val defaultRate = remember(fromCurrency, toCurrency) {
        val fRate = fromCurrency.exchangeRateToBase
        val tRate = toCurrency.exchangeRateToBase
        if (tRate > 0) fRate / tRate else 1.0
    }

    val initialCustomRate = remember(editingExchangeFromTxn, editingExchangeToTxn, fromCurrency, toCurrency) {
        if (editingExchangeFromTxn != null && editingExchangeToTxn != null && editingExchangeFromTxn.amount > 0) {
            String.format(Locale.US, "%.4f", editingExchangeToTxn.amount / editingExchangeFromTxn.amount).trimEnd('0').trimEnd('.')
        } else {
            getFormattedRate(fromCurrency, toCurrency)
        }
    }

    val initialAmount = remember(editingExchangeFromTxn) {
        if (editingExchangeFromTxn != null) {
            if (editingExchangeFromTxn.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", editingExchangeFromTxn.amount)
            else String.format(Locale.US, "%.3f", editingExchangeFromTxn.amount).trimEnd('0').trimEnd('.')
        } else ""
    }

    val initialTargetAmount = remember(editingExchangeToTxn) {
        if (editingExchangeToTxn != null) {
            if (editingExchangeToTxn.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", editingExchangeToTxn.amount)
            else String.format(Locale.US, "%.3f", editingExchangeToTxn.amount).trimEnd('0').trimEnd('.')
        } else ""
    }

    var customRateText by remember { mutableStateOf(initialCustomRate) }
    var amountText by remember { mutableStateOf(initialAmount) }
    var targetAmountText by remember { mutableStateOf(initialTargetAmount) }

    val parsedAmount = parseLocalizedNumber(amountText) ?: 0.0
    val activeRate = parseLocalizedNumber(customRateText) ?: defaultRate

    fun onFromAmountChanged(newVal: String) {
        amountText = newVal
        val amt = parseLocalizedNumber(newVal) ?: 0.0
        if (amt > 0) {
            val converted = amt * activeRate
            targetAmountText = if (converted % 1.0 == 0.0) String.format(Locale.US, "%.0f", converted) else String.format(Locale.US, "%.3f", converted).trimEnd('0').trimEnd('.')
        } else if (newVal.isBlank()) {
            targetAmountText = ""
        }
    }

    fun onTargetAmountChanged(newVal: String) {
        targetAmountText = newVal
        val tAmt = parseLocalizedNumber(newVal) ?: 0.0
        val fAmt = parseLocalizedNumber(amountText) ?: 0.0
        if (tAmt > 0 && fAmt > 0) {
            val compRate = tAmt / fAmt
            customRateText = String.format(Locale.US, "%.4f", compRate).trimEnd('0').trimEnd('.')
        }
    }

    fun onRateChanged(newVal: String) {
        customRateText = newVal
        val r = parseLocalizedNumber(newVal) ?: defaultRate
        val fAmt = parseLocalizedNumber(amountText) ?: 0.0
        if (fAmt > 0) {
            val converted = fAmt * r
            targetAmountText = if (converted % 1.0 == 0.0) String.format(Locale.US, "%.0f", converted) else String.format(Locale.US, "%.3f", converted).trimEnd('0').trimEnd('.')
        }
    }

    var activeCalculatorField by remember { mutableStateOf<String?>(null) }

    val isZeroBalance = fromBalance <= 0.0
    val isInsufficient = parsedAmount > fromBalance
    val isSameCurrency = fromIndex == toIndex
    val isAmountValid = parsedAmount > 0.0
    val canSubmit = isAmountValid && !isInsufficient && !isZeroBalance && !isSameCurrency

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.testTag("compact_currency_exchange_sheet")
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = 720.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BentoBorder)
                    )
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BentoLavenderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isEditing) "ویرایش صرافی و تبدیل اسعار" else "ثبت صرافی و تبدیل اسعار",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (isEditing) "تغییر و ذخیره معامله تبدیل ارز" else "تبدیل فوری میان موجودی نقد ارزها",
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
                                .background(BentoLavenderSubtle)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // From Currency Section (پرداخت و کسر)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.2.dp, BentoBorder)
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
                                    text = "از ارز (پرداخت و کسر):",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (fromBalance > 0) BentoLavenderSubtle else ExpenseRedBg,
                                    border = BorderStroke(0.8.dp, if (fromBalance > 0) BentoBorder else ExpenseRed.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "موجودی: ",
                                            color = if (fromBalance > 0) TextSecondary else ExpenseRed,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "${fromCurrency.symbol} ${viewModel.formatAmount(fromBalance)}",
                                                color = if (fromBalance > 0) BentoNavyDark else ExpenseRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Currencies Row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(currencies) { idx, c ->
                                    val isSel = idx == fromIndex
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) BentoNavyDark else BackgroundCanvas,
                                        border = BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                fromIndex = idx
                                                val newFrom = currencies.getOrElse(idx) { currencies.first() }
                                                val autoRateStr = getFormattedRate(newFrom, toCurrency)
                                                customRateText = autoRateStr
                                                val amt = parseLocalizedNumber(amountText) ?: 0.0
                                                val r = parseLocalizedNumber(autoRateStr) ?: 1.0
                                                if (amt > 0) {
                                                    val converted = amt * r
                                                    targetAmountText = if (converted % 1.0 == 0.0) String.format(Locale.US, "%.0f", converted) else String.format(Locale.US, "%.2f", converted)
                                                }
                                            }
                                            .testTag("compact_exchange_from_${c.code}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(text = c.flagEmoji, fontSize = 13.sp)
                                            Text(
                                                text = c.code,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) Color.White else BentoNavyDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Amount Input
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { onFromAmountChanged(it) },
                                label = { Text("مبلغ پرداختی (${fromCurrency.symbol})", fontSize = 11.5.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("compact_exchange_from_amount_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoNavyDark,
                                    unfocusedTextColor = BentoNavyDark,
                                    cursorColor = BentoIndigoAccent,
                                    focusedBorderColor = BentoIndigoAccent,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedContainerColor = BackgroundCanvas,
                                    unfocusedContainerColor = BackgroundCanvas,
                                    focusedLabelColor = BentoIndigoAccent,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                trailingIcon = {
                                    CalculatorMiniButton(
                                        onClick = { activeCalculatorField = "from_amount" },
                                        contentDescription = "ماشین‌حساب مبلغ پرداختی"
                                    )
                                }
                            )
                        }
                    }

                    // Swap Button in Center
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    val temp = fromIndex
                                    fromIndex = toIndex
                                    toIndex = temp
                                    val newFrom = currencies.getOrElse(fromIndex) { currencies.first() }
                                    val newTo = currencies.getOrElse(toIndex) { currencies.last() }
                                    val autoRateStr = getFormattedRate(newFrom, newTo)
                                    customRateText = autoRateStr
                                    val amt = parseLocalizedNumber(amountText) ?: 0.0
                                    val r = parseLocalizedNumber(autoRateStr) ?: 1.0
                                    if (amt > 0) {
                                        val converted = amt * r
                                        targetAmountText = if (converted % 1.0 == 0.0) String.format(Locale.US, "%.0f", converted) else String.format(Locale.US, "%.2f", converted)
                                    }
                                }
                                .testTag("compact_swap_exchange_currencies_button"),
                            shape = CircleShape,
                            color = BentoLavenderSubtle,
                            border = BorderStroke(1.2.dp, BentoIndigoAccent.copy(alpha = 0.35f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "جابجایی ارزها",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // To Currency Section (دریافت و واریز)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.2.dp, BentoBorder)
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
                                    text = "به ارز (دریافت و واریز):",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BentoLavenderSubtle,
                                    border = BorderStroke(0.8.dp, BentoBorder)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "موجودی: ",
                                            color = TextSecondary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "${toCurrency.symbol} ${viewModel.formatAmount(toBalance)}",
                                                color = BentoNavyDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Currencies Row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(currencies) { idx, c ->
                                    val isSel = idx == toIndex
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) BentoIndigoAccent else BackgroundCanvas,
                                        border = BorderStroke(1.dp, if (isSel) BentoIndigoAccent else BentoBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                toIndex = idx
                                                val newTo = currencies.getOrElse(idx) { currencies.last() }
                                                val autoRateStr = getFormattedRate(fromCurrency, newTo)
                                                customRateText = autoRateStr
                                                val amt = parseLocalizedNumber(amountText) ?: 0.0
                                                val r = parseLocalizedNumber(autoRateStr) ?: 1.0
                                                if (amt > 0) {
                                                    val converted = amt * r
                                                    targetAmountText = if (converted % 1.0 == 0.0) String.format(Locale.US, "%.0f", converted) else String.format(Locale.US, "%.2f", converted)
                                                }
                                            }
                                            .testTag("compact_exchange_to_${c.code}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text(text = c.flagEmoji, fontSize = 13.sp)
                                            Text(
                                                text = c.code,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSel) Color.White else BentoNavyDark
                                            )
                                        }
                                    }
                                }
                            }

                            // Target Amount Input
                            OutlinedTextField(
                                value = targetAmountText,
                                onValueChange = { onTargetAmountChanged(it) },
                                label = { Text("مبلغ دریافتی (${toCurrency.symbol})", fontSize = 11.5.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("compact_exchange_target_amount_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = IncomeGreen,
                                    unfocusedTextColor = IncomeGreen,
                                    cursorColor = IncomeGreen,
                                    focusedBorderColor = IncomeGreen,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedContainerColor = BackgroundCanvas,
                                    unfocusedContainerColor = BackgroundCanvas,
                                    focusedLabelColor = IncomeGreen,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                trailingIcon = {
                                    CalculatorMiniButton(
                                        onClick = { activeCalculatorField = "target_amount" },
                                        contentDescription = "ماشین‌حساب مبلغ دریافتی"
                                    )
                                }
                            )
                        }
                    }

                    // Rate & Conversion Summary Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = BentoLavenderSubtle.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "نرخ تبدیل: ۱ ${fromCurrency.code} = ${String.format(Locale.US, "%.4f", activeRate)} ${toCurrency.code}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }
                            }

                            // Live Conversion Preview
                            if (parsedAmount > 0) {
                                val finalTargetAmt = parseLocalizedNumber(targetAmountText) ?: (parsedAmount * activeRate)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SurfaceWhite,
                                    border = BorderStroke(0.8.dp, BentoBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "خلاصه تبادله:",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "${viewModel.formatAmount(parsedAmount)} ${fromCurrency.code} ➔ ${viewModel.formatAmount(finalTargetAmt)} ${toCurrency.code}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoNavyDark
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = customRateText,
                                onValueChange = { onRateChanged(it) },
                                placeholder = { Text(String.format(Locale.US, "%.4f", defaultRate), fontSize = 11.sp) },
                                label = { Text("نرخ تبدیل دستی (اختیاری)", fontSize = 10.5.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("compact_custom_exchange_rate_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoNavyDark,
                                    unfocusedTextColor = BentoNavyDark,
                                    cursorColor = BentoIndigoAccent,
                                    focusedBorderColor = BentoIndigoAccent,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedContainerColor = BackgroundCanvas,
                                    unfocusedContainerColor = BackgroundCanvas,
                                    focusedLabelColor = BentoIndigoAccent,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                trailingIcon = {
                                    CalculatorMiniButton(
                                        onClick = { activeCalculatorField = "custom_rate" },
                                        contentDescription = "ماشین‌حساب نرخ"
                                    )
                                }
                            )
                        }
                    }

                    // Warnings
                    if (isSameCurrency) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ارز مبدأ و مقصد یکسان هستند. لطفاً ارز متفاوتی را انتخاب نمایید.",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    } else if (isZeroBalance) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExpenseRedBg,
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "موجودی ارز مبدأ صفر است. امکان تبدیل وجود ندارد.",
                                    color = ExpenseRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (isInsufficient) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExpenseRedBg,
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "مبلغ وارد شده بیشتر از موجودی در دسترس (\u200E${fromCurrency.symbol} ${viewModel.formatAmount(fromBalance)}\u200E) است.",
                                    color = ExpenseRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (showDeleteConfirmDialog && editingExchangeFromTxn != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmDialog = false },
                            title = {
                                Text(
                                    "حذف معامله تبدیل اسعار",
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                            },
                            text = {
                                Text(
                                    "آیا از حذف این معامله تبدیل ارز اطمینان دارید؟ با حذف این معامله، هر دو فعالیت تبدیل لغو شده و مبالغ به موجودی نقد حساب‌های مربوطه بازگردانده می‌شوند.",
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteTransaction(editingExchangeFromTxn)
                                        showDeleteConfirmDialog = false
                                        onDismiss()
                                        Toast.makeText(context, "معامله تبدیل با موفقیت حذف و بیلانس اصلاح شد.", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                                ) {
                                    Text("حذف قطعی", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                                    Text("انصراف", color = BentoNavyDark)
                                }
                            }
                        )
                    }
                }

                // Sticky Action Buttons at the bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceWhite,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isEditing) {
                            OutlinedButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                            ) {
                                Text("حذف معامله", color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Text("انصراف", color = BentoNavyDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (canSubmit) {
                                    val finalTargetAmt = parseLocalizedNumber(targetAmountText) ?: (parsedAmount * activeRate)
                                    if (isEditing && editingExchangeFromTxn != null && editingExchangeToTxn != null) {
                                        viewModel.updateCurrencyExchange(
                                            txn1Id = editingExchangeFromTxn.id,
                                            txn2Id = editingExchangeToTxn.id,
                                            oldFromAccountId = editingExchangeFromTxn.accountId,
                                            oldToAccountId = editingExchangeToTxn.accountId,
                                            oldFromAmount = editingExchangeFromTxn.amount,
                                            oldToAmount = editingExchangeToTxn.amount,
                                            newFromAccountId = editingExchangeFromTxn.accountId,
                                            newToAccountId = editingExchangeToTxn.accountId,
                                            fromCurrency = fromCurrency,
                                            toCurrency = toCurrency,
                                            newFromAmount = parsedAmount,
                                            newToAmount = finalTargetAmt,
                                            newRate = activeRate,
                                            newNote = null,
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "تغییرات معامله تبدیل اسعار با موفقیت ذخیره شد.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onDismiss()
                                            }
                                        )
                                    } else {
                                        viewModel.executeCurrencyExchange(
                                            fromCurrency = fromCurrency,
                                            toCurrency = toCurrency,
                                            fromAmount = parsedAmount,
                                            rate = activeRate,
                                            explicitToAmount = finalTargetAmt,
                                            onSuccess = {
                                                Toast.makeText(
                                                    context,
                                                    "تبدیل مبلغ \u200E${fromCurrency.symbol} ${viewModel.formatAmount(parsedAmount)}\u200E به \u200E${toCurrency.symbol} ${viewModel.formatAmount(finalTargetAmt)}\u200E با موفقیت انجام شد.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            },
                            enabled = canSubmit,
                            modifier = Modifier
                                .weight(1.6f)
                                .height(48.dp)
                                .testTag("compact_submit_exchange_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoIndigoAccent,
                                disabledContainerColor = BentoIndigoAccent.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = if (isEditing) "ذخیره تغییرات تبدیل" else "ثبت تبدیل ارز",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Mini Calculator Dialog
    if (activeCalculatorField != null) {
        val field = activeCalculatorField!!
        val initVal = when (field) {
            "from_amount" -> amountText
            "target_amount" -> targetAmountText
            "custom_rate" -> customRateText
            else -> ""
        }
        val title = when (field) {
            "from_amount" -> "محاسبه مبلغ پرداختی"
            "target_amount" -> "محاسبه مبلغ دریافتی"
            "custom_rate" -> "محاسبه نرخ تبدیل"
            else -> "ماشین‌حساب"
        }

        MinimalCalculatorDialog(
            initialValue = initVal,
            title = title,
            onConfirm = { calcVal ->
                when (field) {
                    "from_amount" -> onFromAmountChanged(calcVal)
                    "target_amount" -> onTargetAmountChanged(calcVal)
                    "custom_rate" -> onRateChanged(calcVal)
                }
            },
            onDismiss = { activeCalculatorField = null }
        )
    }
}
