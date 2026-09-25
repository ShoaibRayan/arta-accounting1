package com.example.ui.components

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.RecipientEntity
import com.example.data.local.TransactionEntity
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
import com.example.util.PersianDateHelper
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notes
import com.example.data.local.AccountCardEntity
import java.text.DecimalFormat

@Composable
fun PersonStatementDialog(
    recipient: RecipientEntity,
    transactions: List<TransactionEntity>,
    targetCurrency: String? = null,
    accounts: List<AccountCardEntity> = emptyList(),
    isMasked: Boolean = false,
    onRequestUnmask: () -> Unit = {},
    onTransactionClick: (TransactionEntity) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf(false) }

    // Filter transactions specifically for this recipient strictly by recipientId
    val personTxns = remember(transactions, recipient.id) {
        transactions.filter { txn ->
            txn.recipientId == recipient.id
        }.sortedByDescending { it.timestamp }
    }

    // Extract all unique currencies present for this recipient (respecting ledgerCurrencyCode when present)
    val availableCurrencies = remember(personTxns) {
        val list = personTxns.map { txn ->
            if (!txn.ledgerCurrencyCode.isNullOrBlank() && txn.ledgerAmount != null && txn.ledgerAmount > 0.0) {
                txn.ledgerCurrencyCode.uppercase()
            } else {
                txn.currencyCode.uppercase()
            }
        }.distinct()
        if (list.isEmpty()) listOf("AFN") else list
    }

    var selectedCurrency by remember(availableCurrencies, targetCurrency) {
        mutableStateOf(
            if (!targetCurrency.isNullOrBlank()) {
                availableCurrencies.firstOrNull { it.equals(targetCurrency, ignoreCase = true) } ?: targetCurrency
            } else {
                availableCurrencies.first()
            }
        )
    }

    var filterType by remember { mutableStateOf<TransactionType?>(null) }

    // Helper to get effective currency and amount for person ledger
    fun TransactionEntity.effectiveCurrency(): String =
        if (!ledgerCurrencyCode.isNullOrBlank() && ledgerAmount != null && ledgerAmount > 0.0) {
            ledgerCurrencyCode.uppercase()
        } else {
            currencyCode.uppercase()
        }

    fun TransactionEntity.effectiveAmount(): Double =
        if (!ledgerCurrencyCode.isNullOrBlank() && ledgerAmount != null && ledgerAmount > 0.0) {
            ledgerAmount
        } else {
            amount
        }

    // Filter transactions by selected currency AND transaction type
    val currencyFilteredTxns = remember(personTxns, selectedCurrency) {
        personTxns.filter { it.effectiveCurrency().equals(selectedCurrency, ignoreCase = true) }
    }

    val filteredTxns = remember(currencyFilteredTxns, filterType) {
        if (filterType == null) currencyFilteredTxns else currencyFilteredTxns.filter { it.type == filterType }
    }

    // Calculations for the selected currency
    val totalPaidForCurrency = remember(currencyFilteredTxns) {
        currencyFilteredTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.effectiveAmount() }
    }
    val totalReceivedForCurrency = remember(currencyFilteredTxns) {
        currencyFilteredTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.effectiveAmount() }
    }
    val netBalanceForCurrency = remember(totalPaidForCurrency, totalReceivedForCurrency) {
        // net > 0: we paid more, person owes us (طلب ما / دریافتنی)
        // net < 0: we received more, we owe person (بدهی ما / پرداختنی)
        totalPaidForCurrency - totalReceivedForCurrency
    }

    val isAccountSettled = remember(currencyFilteredTxns, netBalanceForCurrency) {
        currencyFilteredTxns.isNotEmpty() && kotlin.math.abs(netBalanceForCurrency) < 0.01
    }
    var showSettledTransactions by remember { mutableStateOf(false) }

    val displayDialogTxns = remember(filteredTxns, isAccountSettled, showSettledTransactions) {
        if (isAccountSettled && !showSettledTransactions) {
            emptyList()
        } else {
            filteredTxns
        }
    }

    // Currency summaries for all currencies (for export)
    val currencySummaries = remember(personTxns) {
        val groups = personTxns.groupBy { it.effectiveCurrency() }
        groups.map { (curr, txns) ->
            val totalReceived = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.effectiveAmount() }
            val totalPaid = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.effectiveAmount() }
            val net = totalPaid - totalReceived
            Triple(curr, Pair(totalPaid, totalReceived), net)
        }
    }

    val decFormat = remember { DecimalFormat("#,##0.##") }

    // Shareable report text builder
    fun generateStatementText(): String {
        val sb = StringBuilder()
        sb.append("📋 گزارش حساب و صورتحساب مالی\n")
        sb.append("👤 طرف حساب: ${recipient.name}\n")
        if (recipient.handleOrPhone.isNotBlank()) {
            sb.append("📞 شماره/شناسه: ${recipient.handleOrPhone}\n")
        }
        sb.append("📅 تاریخ گزارش: ${PersianDateHelper.formatSolarDateTime(System.currentTimeMillis())}\n")
        sb.append("------------------------------------\n")
        sb.append("💰 گزارش ارز انتخابی ($selectedCurrency):\n")
        if (isMasked) {
            sb.append("   🔒 (مبالغ این شخص محافظت‌شده هستند)\n")
            sb.append("   - مجموع پرداختی شما: **** $selectedCurrency\n")
            sb.append("   - مجموع دریافتی شما: **** $selectedCurrency\n")
            if (netBalanceForCurrency > 0) {
                sb.append("   ✓ وضعیت: طلب شما (دریافتنی): **** $selectedCurrency 🔒\n")
            } else if (netBalanceForCurrency < 0) {
                sb.append("   ⚠️ وضعیت: بدهی شما (پرداختنی): **** $selectedCurrency 🔒\n")
            } else {
                sb.append("   ✓ وضعیت: تسویه کامل (۰)\n")
            }
        } else {
            sb.append("   - مجموع پرداختی شما: ${decFormat.format(totalPaidForCurrency)} $selectedCurrency\n")
            sb.append("   - مجموع دریافتی شما: ${decFormat.format(totalReceivedForCurrency)} $selectedCurrency\n")
            if (netBalanceForCurrency > 0) {
                sb.append("   ✓ وضعیت: طلب شما (دریافتنی): ${decFormat.format(netBalanceForCurrency)} $selectedCurrency\n")
            } else if (netBalanceForCurrency < 0) {
                sb.append("   ⚠️ وضعیت: بدهی شما (پرداختنی): ${decFormat.format(-netBalanceForCurrency)} $selectedCurrency\n")
            } else {
                sb.append("   ✓ وضعیت: تسویه کامل (۰)\n")
            }
        }
        sb.append("------------------------------------\n")
        sb.append("📝 ریز تراکنش‌های این ارز (${currencyFilteredTxns.size} مورد):\n")
        currencyFilteredTxns.forEachIndexed { index, txn ->
            val hasLedger = !txn.ledgerCurrencyCode.isNullOrBlank() && txn.ledgerAmount != null && txn.ledgerAmount > 0.0
            val effAmtStr = if (isMasked) "****" else decFormat.format(txn.effectiveAmount())
            val effCurr = txn.effectiveCurrency()
            val typeStr = if (txn.type == TransactionType.EXPENSE) "پرداخت به ایشان" else "دریافت از ایشان"
            val dateStr = PersianDateHelper.formatSolarDateTime(txn.timestamp)
            val convertedNote = if (hasLedger) {
                if (isMasked) " (ارز محاسبه‌شده: معادل **** ${txn.currencyCode})" else " (ارز محاسبه‌شده: معادل ${decFormat.format(txn.amount)} ${txn.currencyCode})"
            } else ""
            sb.append("${index + 1}. $typeStr | $effCurr $effAmtStr$convertedNote | $dateStr\n")
            val cleanUserNote = cleanTransactionNote(txn.note)
            if (!cleanUserNote.isNullOrBlank()) {
                sb.append("   یادداشت: $cleanUserNote\n")
            }
            if (txn.dueDate != null && txn.dueDate > 0) {
                val remaining = PersianDateHelper.getRemainingDaysText(txn.dueDate)
                val statusText = if (txn.isSettled) " (تسویه‌شده)" else if (remaining.isNotBlank()) " ($remaining)" else ""
                sb.append("   سررسید: ${PersianDateHelper.formatSolarDate(txn.dueDate)}$statusText\n")
            }
        }
        sb.append("------------------------------------\n")
        sb.append("ارسال‌شده از نرم‌افزار حسابداری شخصی هوشمند")
        return sb.toString()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .wrapContentHeight()
                    .heightIn(max = 720.dp)
                    .clip(RoundedCornerShape(26.dp)),
                color = BackgroundCanvas,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(recipient.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recipient.name.take(1),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "صورتحساب: ${recipient.name}",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BentoNavyDark
                                    ) {
                                        Text(
                                            text = selectedCurrency,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${currencyFilteredTxns.size} تراکنش ثبت‌شده به $selectedCurrency",
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
                                .background(Color(0xFFE2E8F0))
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

                    // Currency Selection Chips (نمایش و جابجایی بین ارزهای حساب شخص)
                    if (availableCurrencies.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "انتخاب ارز گزارش صورتحساب:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(availableCurrencies) { currCode ->
                                    val isSel = currCode.equals(selectedCurrency, ignoreCase = true)
                                    val currTxnCount = personTxns.count { it.effectiveCurrency().equals(currCode, ignoreCase = true) }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSel) BentoNavyDark else Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedCurrency = currCode }
                                            .testTag("statement_currency_tab_$currCode")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = currCode,
                                                color = if (isSel) Color.White else BentoNavyDark,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSel) Color.White.copy(alpha = 0.2f) else BentoBorder.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = "$currTxnCount",
                                                    color = if (isSel) Color.White else TextSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // --- موقعیت وضعیت حساب: کارت برجسته و زیبای بنتو برای ارز انتخابی ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Status Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "وضعیت حساب ($selectedCurrency)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        netBalanceForCurrency > 0 -> IncomeGreenBg
                                        netBalanceForCurrency < 0 -> ExpenseRedBg
                                        else -> Color(0xFFEEF2FF)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                netBalanceForCurrency > 0 -> "طلب شما (دریافتنی)"
                                                netBalanceForCurrency < 0 -> "بدهی شما (پرداختنی)"
                                                else -> "تسویه کامل"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                netBalanceForCurrency > 0 -> IncomeGreen
                                                netBalanceForCurrency < 0 -> ExpenseRed
                                                else -> BentoIndigoAccent
                                            }
                                        )
                                        if (isMasked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "محافظت شده",
                                                tint = BentoIndigoAccent,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Large Net Balance with Currency on Left
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = if (isMasked) {
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onRequestUnmask() }
                                    } else Modifier
                                ) {
                                    Text(
                                        text = selectedCurrency,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoIndigoAccent
                                    )
                                    Text(
                                        text = if (isMasked) "****" else decFormat.format(kotlin.math.abs(netBalanceForCurrency)),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isMasked) BentoNavyDark else when {
                                            netBalanceForCurrency > 0 -> IncomeGreen
                                            netBalanceForCurrency < 0 -> ExpenseRed
                                            else -> BentoNavyDark
                                        }
                                    )
                                    if (isMasked) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "برای نمایش مبلغ لمس کنید",
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (isMasked) {
                                Text(
                                    text = "برای نمایش مبلغ، لمس کنید 🔒",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoIndigoAccent,
                                    modifier = Modifier
                                        .clickable { onRequestUnmask() }
                                        .padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Sub-stats: Total Paid and Total Received
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = BackgroundCanvas,
                                    border = BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "مجموع پرداختی شما", fontSize = 10.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = if (isMasked) "$selectedCurrency ****" else "$selectedCurrency ${decFormat.format(totalPaidForCurrency)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseRed
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = BackgroundCanvas,
                                    border = BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = "مجموع دریافتی شما", fontSize = 10.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = if (isMasked) "$selectedCurrency ****" else "$selectedCurrency ${decFormat.format(totalReceivedForCurrency)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filter Type Buttons (همه | دریافتی | پرداختی) برای ارز انتخابی
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            null to "همه (${currencyFilteredTxns.size})",
                            TransactionType.INCOME to "دریافتی‌ها (${currencyFilteredTxns.count { it.type == TransactionType.INCOME }})",
                            TransactionType.EXPENSE to "پرداختی‌ها (${currencyFilteredTxns.count { it.type == TransactionType.EXPENSE }})"
                        ).forEach { (t, label) ->
                            val isSel = filterType == t
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) BentoNavyDark else SurfaceWhite)
                                    .border(1.dp, if (isSel) BentoNavyDark else BentoBorder, RoundedCornerShape(10.dp))
                                    .clickable { filterType = t }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }

                    if (isAccountSettled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showSettledTransactions = !showSettledTransactions }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نمایش حساب‌ها و تراکنش‌های تسویه‌شده",
                                fontSize = 11.5.sp,
                                color = BentoNavyDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Checkbox(
                                checked = showSettledTransactions,
                                onCheckedChange = { showSettledTransactions = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = BentoIndigoAccent,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transaction List
                    if (isAccountSettled && !showSettledTransactions) {
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "حساب این شخص کاملاً تسویه است (۰)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BentoNavyDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "برای مشاهده سوابق، تیک نمایش حساب‌های تسویه‌شده را فعال کنید.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else if (displayDialogTxns.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "تراکنشی برای این شخص ثبت نشده است",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(displayDialogTxns, key = { it.id }) { txn ->
                                val isIncome = txn.type == TransactionType.INCOME
                                val hasLedger = !txn.ledgerCurrencyCode.isNullOrBlank() && (txn.ledgerAmount ?: 0.0) > 0.0
                                val effAmt = txn.effectiveAmount()
                                val effCurr = txn.effectiveCurrency()

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onTransactionClick(txn) }
                                        .testTag("person_statement_txn_${txn.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = SurfaceWhite,
                                    border = BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        // Header Row: Type, Icon, Date, Amount
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
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isIncome) IncomeGreenBg else ExpenseRedBg),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                        contentDescription = null,
                                                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = if (isIncome) "دریافت از شخص" else "پرداخت به شخص",
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoNavyDark
                                                    )
                                                    Text(
                                                        text = PersianDateHelper.formatSolarDateTime(txn.timestamp),
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        text = if (isMasked) "${if (isIncome) "+" else "-"}$effCurr ****" else "${if (isIncome) "+" else "-"}$effCurr ${decFormat.format(effAmt)}",
                                                        fontSize = 14.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isIncome) IncomeGreen else ExpenseRed
                                                    )
                                                }
                                                if (hasLedger) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = BentoIndigoAccent.copy(alpha = 0.08f),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isMasked) "محاسبه از: ${txn.currencyCode} ****" else "محاسبه از: ${txn.currencyCode} ${decFormat.format(txn.amount)}",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = BentoIndigoAccent,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // --- Important General Details (اطلاعات مهم کلی) ---
                                        val accountName = remember(txn.accountId, accounts) {
                                            accounts.find { it.id == txn.accountId }?.name
                                                ?: if (txn.accountId == 0L) "حساب اصلی / پیش‌فرض" else "حساب شماره ${txn.accountId}"
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF8F9FD),
                                            border = BorderStroke(0.8.dp, BentoBorder)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                // Badges row: Account & Category & Due/Settlement
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Account Badge
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = BentoLavenderSubtle,
                                                        border = BorderStroke(0.6.dp, BentoBorder)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                        ) {
                                                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(11.dp))
                                                            Text(text = accountName, fontSize = 9.5.sp, color = BentoNavyDark, fontWeight = FontWeight.Medium)
                                                        }
                                                    }

                                                    // Category Badge
                                                    if (txn.category.isNotBlank()) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color.White,
                                                            border = BorderStroke(0.6.dp, BentoBorder)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                            ) {
                                                                Icon(Icons.Default.Category, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                                                                Text(text = txn.category, fontSize = 9.5.sp, color = TextSecondary)
                                                            }
                                                        }
                                                    }

                                                    // Settlement status if exists
                                                    if (txn.dueDate != null && txn.dueDate > 0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = if (txn.isSettled) IncomeGreenBg else ExpenseRedBg
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                            ) {
                                                                Icon(
                                                                    if (txn.isSettled) Icons.Default.CheckCircle else Icons.Default.Pending,
                                                                    contentDescription = null,
                                                                    tint = if (txn.isSettled) IncomeGreen else ExpenseRed,
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                                val remainingDays = PersianDateHelper.getRemainingDaysText(txn.dueDate)
                                                                val dueText = if (txn.isSettled) {
                                                                    "تسویه‌شده (${PersianDateHelper.formatSolarDate(txn.dueDate)})"
                                                                } else if (remainingDays.isNotBlank()) {
                                                                    "سررسید: ${PersianDateHelper.formatSolarDate(txn.dueDate)} ($remainingDays)"
                                                                } else {
                                                                    "سررسید: ${PersianDateHelper.formatSolarDate(txn.dueDate)}"
                                                                }
                                                                Text(
                                                                    text = dueText,
                                                                    fontSize = 9.sp,
                                                                    color = if (txn.isSettled) IncomeGreen else ExpenseRed,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                // Note if present
                                                val cleanItemNote = cleanTransactionNote(txn.note)
                                                if (!cleanItemNote.isNullOrBlank()) {
                                                    Row(
                                                        verticalAlignment = Alignment.Top,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(Icons.Default.Notes, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(12.dp).padding(top = 1.dp))
                                                        Text(
                                                            text = "یادداشت: $cleanItemNote",
                                                            fontSize = 10.sp,
                                                            color = TextSecondary,
                                                            lineHeight = 14.sp
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions Bar (اشتراک‌گذاری و کپی)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val text = generateStatementText()
                                clipboardManager.setText(AnnotatedString(text))
                                copiedToast = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoNavyDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (copiedToast) "کپی شد ✓" else "کپی صورتحساب",
                                fontSize = 12.sp,
                                color = if (copiedToast) IncomeGreen else BentoNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val text = generateStatementText()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "ارسال صورتحساب ${recipient.name}")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اشتراک‌گذاری صورتحساب", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
