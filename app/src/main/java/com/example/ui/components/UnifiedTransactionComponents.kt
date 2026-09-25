package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionKind
import com.example.data.local.TransactionType
import com.example.ui.theme.*
import com.example.util.PersianDateHelper
import java.util.Locale

/**
 * انواع تراکنش‌های دوتایی پیوسته (Paired Transactions)
 */
enum class TransferPairType {
    CURRENCY_EXCHANGE,    // صرافی و تبدیل اسعار
    ACCOUNT_TRANSFER,     // انتقال بین حسابات و کارت‌ها
    CASH_CARD_TRANSFER,   // انتقال بین صندوق نقد و کارت بانکی
    PERSON_TRANSFER,      // حواله و انتقال حساب اشخاص
    PERSON_EXCHANGE       // تبدیل اسعار حساب شخص
}

/**
 * مدل یکپارچه برای نمایش تراکنش‌ها (تک‌تراکنش یا تراکنش دوتایی ادغام‌شده)
 */
sealed class UnifiedTransactionItem {
    abstract val id: Long
    abstract val timestamp: Long

    data class Single(val transaction: TransactionEntity) : UnifiedTransactionItem() {
        override val id: Long = transaction.id
        override val timestamp: Long = transaction.timestamp
    }

    data class Paired(
        val fromTxn: TransactionEntity,
        val toTxn: TransactionEntity,
        val pairType: TransferPairType
    ) : UnifiedTransactionItem() {
        override val id: Long = fromTxn.id
        override val timestamp: Long = fromTxn.timestamp
    }
}

/**
 * تابع تجمیع تراکنش‌های دوتایی به یک فعالیت واحد
 */
fun consolidateTransactions(
    rawList: List<TransactionEntity>,
    allTransactionsLookup: List<TransactionEntity>? = null
): List<UnifiedTransactionItem> {
    val candidateList = allTransactionsLookup ?: rawList
    val fullLookup = candidateList.associateBy { it.id }
    val reverseRelatedLookup = mutableMapOf<Long, TransactionEntity>()
    for (item in candidateList) {
        val relId = item.relatedTransactionId
        if (relId != null && relId != item.id) {
            reverseRelatedLookup[relId] = item
        }
    }
    val pairedSeen = mutableSetOf<Long>()
    val result = mutableListOf<UnifiedTransactionItem>()

    for (txn in rawList) {
        if (txn.id in pairedSeen) continue

        val relatedId = txn.relatedTransactionId
        val relatedTxn = if (relatedId != null) {
            fullLookup[relatedId]
        } else {
            reverseRelatedLookup[txn.id]
        }

        if (relatedTxn != null && relatedTxn.id != txn.id) {
            pairedSeen.add(txn.id)
            pairedSeen.add(relatedTxn.id)

            // تشخیص طرف برداشت (مبدأ) و واریز (مقصد)
            val (fromTxn, toTxn) = if (txn.type == TransactionType.EXPENSE ||
                (txn.type != TransactionType.INCOME && txn.id < relatedTxn.id)
            ) {
                Pair(txn, relatedTxn)
            } else {
                Pair(relatedTxn, txn)
            }

            val pairType = when {
                fromTxn.category == "Exchange" || toTxn.category == "Exchange" ||
                fromTxn.title.startsWith("تبدیل به") || toTxn.title.startsWith("تبدیل به") ||
                fromTxn.title.startsWith("دریافت از تبدیل") || toTxn.title.startsWith("دریافت از تبدیل") ||
                fromTxn.title.contains("تبدیل اسعار") || toTxn.title.contains("تبدیل اسعار") ->
                    TransferPairType.CURRENCY_EXCHANGE

                fromTxn.kind == TransactionKind.PERSON_TRANSFER || toTxn.kind == TransactionKind.PERSON_TRANSFER ||
                fromTxn.category == "انتقال حساب اشخاص" || toTxn.category == "انتقال حساب اشخاص" ||
                (fromTxn.recipientId != null && toTxn.recipientId != null && fromTxn.recipientId != toTxn.recipientId) ||
                (!fromTxn.recipientName.isNullOrBlank() && !toTxn.recipientName.isNullOrBlank() && fromTxn.recipientName != toTxn.recipientName) ->
                    TransferPairType.PERSON_TRANSFER

                fromTxn.kind == TransactionKind.PERSON_CURRENCY_EXCHANGE || toTxn.kind == TransactionKind.PERSON_CURRENCY_EXCHANGE ||
                fromTxn.category == "تبدیل اسعار شخص" || toTxn.category == "تبدیل اسعار شخص" ||
                (fromTxn.recipientId != null && toTxn.recipientId != null && fromTxn.recipientId == toTxn.recipientId && (fromTxn.title.contains("تبدیل") || toTxn.title.contains("تبدیل"))) ||
                (!fromTxn.recipientName.isNullOrBlank() && fromTxn.recipientName == toTxn.recipientName && (fromTxn.title.contains("تبدیل") || toTxn.title.contains("تبدیل"))) ->
                    TransferPairType.PERSON_EXCHANGE

                fromTxn.title.contains("نقد") || toTxn.title.contains("نقد") ||
                fromTxn.title.contains("کارت به نقد") || toTxn.title.contains("کارت به نقد") ||
                fromTxn.title.contains("نقد به کارت") || toTxn.title.contains("نقد به کارت") ->
                    TransferPairType.CASH_CARD_TRANSFER

                else -> TransferPairType.ACCOUNT_TRANSFER
            }

            result.add(UnifiedTransactionItem.Paired(fromTxn = fromTxn, toTxn = toTxn, pairType = pairType))
        } else {
            result.add(UnifiedTransactionItem.Single(txn))
        }
    }

    return result
}

/**
 * رندر ردیف تراکنش یکپارچه (تک‌تراکنش یا تراکنش دوتایی ادغام‌شده در یک فعالیت)
 */
@Composable
fun UnifiedTransactionRowItem(
    item: UnifiedTransactionItem,
    accounts: List<AccountCardEntity> = emptyList(),
    currencySymbol: String = "؋",
    isAmountMasked: Boolean = false,
    isFromAmountMasked: Boolean = isAmountMasked,
    isToAmountMasked: Boolean = isAmountMasked,
    onClick: () -> Unit = {}
) {
    when (item) {
        is UnifiedTransactionItem.Single -> {
            TransactionRowItem(
                transaction = item.transaction,
                currencySymbol = currencySymbol,
                accounts = accounts,
                isAmountMasked = isAmountMasked,
                onClick = onClick
            )
        }
        is UnifiedTransactionItem.Paired -> {
            PairedTransactionRowItem(
                item = item,
                accounts = accounts,
                isFromAmountMasked = isFromAmountMasked,
                isToAmountMasked = isToAmountMasked,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun PairedTransactionRowItem(
    item: UnifiedTransactionItem.Paired,
    accounts: List<AccountCardEntity>,
    isFromAmountMasked: Boolean = false,
    isToAmountMasked: Boolean = false,
    onClick: () -> Unit
) {
    val fromTxn = item.fromTxn
    val toTxn = item.toTxn
    val pairType = item.pairType

    val (icon, bgTint, iconTint, typeTitle, typeTag) = when (pairType) {
        TransferPairType.CURRENCY_EXCHANGE -> {
            Tuple5(
                Icons.Default.CurrencyExchange,
                Color(0xFFECFDF5),
                IncomeGreen,
                "تبدیل ${fromTxn.currencyCode} ➔ ${toTxn.currencyCode}",
                "صرافی و تبدیل اسعار"
            )
        }
        TransferPairType.ACCOUNT_TRANSFER -> {
            val fromAcc = accounts.find { it.id == fromTxn.accountId }?.name ?: "حساب مبدأ"
            val toAcc = accounts.find { it.id == toTxn.accountId }?.name ?: "حساب مقصد"
            Tuple5(
                Icons.Default.SwapHoriz,
                Color(0xFFEEF2FF),
                BentoIndigoAccent,
                "انتقال از $fromAcc به $toAcc",
                "انتقال بین حسابات"
            )
        }
        TransferPairType.CASH_CARD_TRANSFER -> {
            val isFromCash = fromTxn.title.contains("نقد به کارت") || fromTxn.accountId == 0L
            val fromName = if (isFromCash) "صندوق نقد" else "کارت بانکی"
            val toName = if (isFromCash) "کارت بانکی" else "صندوق نقد"
            Tuple5(
                Icons.Default.Payments,
                Color(0xFFF0FDF4),
                Color(0xFF0D9488),
                "انتقال از $fromName به $toName",
                "انتقال نقد و کارت"
            )
        }
        TransferPairType.PERSON_TRANSFER -> {
            val fromP = fromTxn.recipientName ?: "شخص مبدأ"
            val toP = toTxn.recipientName ?: "شخص مقصد"
            Tuple5(
                Icons.Default.SyncAlt,
                Color(0xFFFFFBEB),
                Color(0xFFD97706),
                "انتقال از $fromP به $toP",
                "انتقال حساب اشخاص"
            )
        }
        TransferPairType.PERSON_EXCHANGE -> {
            val person = fromTxn.recipientName ?: "مشتری"
            Tuple5(
                Icons.Default.CurrencyExchange,
                Color(0xFFFDF4FF),
                Color(0xFFA855F7),
                "تبدیل اسعار حساب: $person",
                "تبدیل اسعار شخص"
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color(0x14001552),
                ambientColor = Color(0x0A001552)
            ),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.2.dp, BentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp)
                .testTag("paired_transaction_${item.id}"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون ترکیبی و متمایز فعالیت دوتایی
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // عنوان فعالیت و تگ دسته‌بندی
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = typeTitle,
                        color = BentoNavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // تگ فعالیت
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = bgTint
                    ) {
                        Text(
                            text = typeTag,
                            color = iconTint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // زمان ثبت
                    Text(
                        text = PersianDateHelper.formatSolarDateTime(fromTxn.timestamp),
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // نمایش دو سویه مبالغ: مبدأ (منفی) و مقصد (مثبت)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // مبلغ خروجی
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFromAmountMasked) {
                            Text(
                                text = fromTxn.currencySymbol,
                                color = BentoIndigoAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "••••••",
                                color = BentoIndigoAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "محافظت شده",
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(11.dp)
                            )
                        } else {
                            Text(
                                text = fromTxn.currencySymbol,
                                color = ExpenseRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "-${formatAmountDisplay(fromTxn.amount)}",
                                color = ExpenseRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // مبلغ ورودی
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isToAmountMasked) {
                            Text(
                                text = toTxn.currencySymbol,
                                color = BentoIndigoAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "••••••",
                                color = BentoIndigoAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "محافظت شده",
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(11.dp)
                            )
                        } else {
                            Text(
                                text = toTxn.currencySymbol,
                                color = IncomeGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${formatAmountDisplay(toTxn.amount)}",
                                color = IncomeGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ جزئیات کامل معامله دوتایی (تبدیل ارز یا انتقال) همراه با قابلیت ویرایش دقیق و حذف
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxuryPairedTransactionDetailBottomSheet(
    item: UnifiedTransactionItem.Paired,
    accounts: List<AccountCardEntity> = emptyList(),
    isFromAmountMasked: Boolean = false,
    isToAmountMasked: Boolean = false,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val fromTxn = item.fromTxn
    val toTxn = item.toTxn
    val pairType = item.pairType

    val fromAccountName = accounts.find { it.id == fromTxn.accountId }?.name
        ?: if (fromTxn.accountId == 0L) "صندوق نقدی" else "حساب شماره ${fromTxn.accountId}"
    val toAccountName = accounts.find { it.id == toTxn.accountId }?.name
        ?: if (toTxn.accountId == 0L) "صندوق نقدی" else "حساب شماره ${toTxn.accountId}"

    val (titleText, descText, typeIcon, iconBg, iconColor) = when (pairType) {
        TransferPairType.CURRENCY_EXCHANGE -> Tuple5(
            "جزئیات صرافی و تبدیل اسعار",
            "تبدیل یکپارچه بین دو واحد پولی",
            Icons.Default.CurrencyExchange,
            Color(0xFFECFDF5),
            IncomeGreen
        )
        TransferPairType.ACCOUNT_TRANSFER -> Tuple5(
            "جزئیات انتقال بین حسابات",
            "انتقال داخلی بین کارت‌ها و حساب‌های بانکی",
            Icons.Default.SwapHoriz,
            Color(0xFFEEF2FF),
            BentoIndigoAccent
        )
        TransferPairType.CASH_CARD_TRANSFER -> Tuple5(
            "جزئیات انتقال نقد و کارت",
            "انتقال بین موجودی فیزیکی نقد و حساب بانکی",
            Icons.Default.Payments,
            Color(0xFFF0FDF4),
            Color(0xFF0D9488)
        )
        TransferPairType.PERSON_TRANSFER -> Tuple5(
            "جزئیات حواله و انتقال حساب اشخاص",
            "انتقال یا تسویه متقابل بین حساب اشخاص",
            Icons.Default.SyncAlt,
            Color(0xFFFFFBEB),
            Color(0xFFD97706)
        )
        TransferPairType.PERSON_EXCHANGE -> Tuple5(
            "جزئیات تبدیل اسعار شخص",
            "تبدیل ارزی انجام‌شده در حساب شخص",
            Icons.Default.CurrencyExchange,
            Color(0xFFFDF4FF),
            Color(0xFFA855F7)
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.testTag("paired_transaction_detail_sheet")
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // سربرگ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = titleText,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(
                                text = descText,
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
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = BentoNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // کارت نمایش شماتیک انتقال و جریان وجه (Source ➔ Target)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ردیف بخش مبدأ (Source Outflow)
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRed)
                                )
                                Column {
                                    Text(
                                        text = "مبدأ (برداشت / خروجی):",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    val sourceName = when (pairType) {
                                        TransferPairType.PERSON_TRANSFER -> "شخص: ${fromTxn.recipientName ?: "نامشخص"}"
                                        TransferPairType.PERSON_EXCHANGE -> "حساب: ${fromTxn.recipientName ?: "نامشخص"}"
                                        else -> fromAccountName
                                    }
                                    Text(
                                        text = sourceName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isFromAmountMasked) {
                                        Text(
                                            text = fromTxn.currencySymbol,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "••••••",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "محافظت شده",
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Text(
                                            text = fromTxn.currencySymbol,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "-${formatAmountDisplay(fromTxn.amount)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }

                        // فلش جریان در میانه
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = BentoBorder
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = BentoBorder
                            )
                        }

                        // ردیف بخش مقصد (Target Inflow)
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen)
                                )
                                Column {
                                    Text(
                                        text = "مقصد (واریز / دریافتی):",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    val targetName = when (pairType) {
                                        TransferPairType.PERSON_TRANSFER -> "شخص: ${toTxn.recipientName ?: "نامشخص"}"
                                        TransferPairType.PERSON_EXCHANGE -> "حساب: ${toTxn.recipientName ?: "نامشخص"}"
                                        else -> toAccountName
                                    }
                                    Text(
                                        text = targetName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }
                            }

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isToAmountMasked) {
                                        Text(
                                            text = toTxn.currencySymbol,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "••••••",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "محافظت شده",
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Text(
                                            text = toTxn.currencySymbol,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+${formatAmountDisplay(toTxn.amount)}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // کارت نرخ برابری ارزی (اگر دو ارز متفاوت هستند)
                if (fromTxn.currencyCode != toTxn.currencyCode && fromTxn.amount > 0) {
                    val computedRate = toTxn.amount / fromTxn.amount
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نرخ برابری معامله:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "1 ${fromTxn.currencyCode} = ${String.format(Locale.US, "%.4f", computedRate).trimEnd('0').trimEnd('.')} ${toTxn.currencyCode}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                            }
                        }
                    }
                }

                // اطلاعات جزئی تکمیلی
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow(
                            label = "زمان دقیق معامله",
                            value = PersianDateHelper.formatSolarDateTime(fromTxn.timestamp)
                        )

                        if (!fromTxn.note.isNullOrBlank() || !toTxn.note.isNullOrBlank()) {
                            HorizontalDivider(color = BentoBorder.copy(alpha = 0.5f))
                            val finalNote = (fromTxn.note ?: toTxn.note).orEmpty()
                            DetailRow(
                                label = "یادداشت معامله",
                                value = finalNote
                            )
                        }
                    }
                }

                // دکمه‌های اقدام: ویرایش و حذف
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // دکمه حذف
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("delete_paired_transaction_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف معامله", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // دکمه ویرایش متناسب با نوع تراکنش
                    Button(
                        onClick = {
                            onDismiss()
                            onEdit()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("edit_paired_transaction_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ویرایش اطلاعات", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // دیالوگ تأیید حذف دو تراکنش با هم
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "حذف این معامله یکپارچه",
                    fontWeight = FontWeight.Bold,
                    color = BentoNavyDark
                )
            },
            text = {
                Text(
                    text = "آیا از حذف این معامله اطمینان دارید؟ با حذف، هر دو سمت تراکنش (برداشت و واریز) حذف شده و مبالغ به حساب‌ها و موجودی مربوطه بازگردانده می‌شوند.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDismiss()
                        onDelete()
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

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
