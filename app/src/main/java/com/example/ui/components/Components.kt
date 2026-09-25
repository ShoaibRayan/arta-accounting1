package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import com.example.data.local.TransactionKind
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.RecipientEntity
import com.example.data.local.TransactionEntity
import com.example.util.PersianDateHelper
import com.example.data.local.TransactionType
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.CardDarkGradientEnd
import com.example.ui.theme.CardDarkGradientStart
import com.example.ui.theme.CardGoldGradientEnd
import com.example.ui.theme.CardGoldGradientStart
import com.example.ui.theme.CardTealGradientEnd
import com.example.ui.theme.CardTealGradientStart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

enum class NavTab {
    HOME,
    CARDS,
    CALCULATOR,
    GOALS,
    RECIPIENTS,
    ANALYTICS
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingBottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onHomeLongClick: () -> Unit = {},
    isHomePlusActive: Boolean = false,
    onHomePlusClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val homeBgColor by animateColorAsState(
        targetValue = if (isHomePlusActive) BentoIndigoAccent else PrimaryDark,
        animationSpec = tween(280),
        label = "home_bg_color"
    )

    val homeRotation by animateFloatAsState(
        targetValue = if (isHomePlusActive) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "home_rotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x1A001552)
            )
            .border(1.dp, BentoBorder, RoundedCornerShape(32.dp))
            .testTag("floating_bottom_bar"),
        shape = RoundedCornerShape(32.dp),
        color = SurfaceWhite,
        tonalElevation = 4.dp
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Goals (اهداف مالی)
                NavBarIcon(
                    icon = Icons.Default.Savings,
                    label = "اهداف",
                    isSelected = currentTab == NavTab.GOALS,
                    testTag = "nav_goals",
                    onClick = { onTabSelected(NavTab.GOALS) }
                )

                // Wallets / Cards (کیف پول)
                NavBarIcon(
                    icon = if (currentTab == NavTab.CARDS) Icons.Filled.CreditCard else Icons.Outlined.CreditCard,
                    label = "کیف پول",
                    isSelected = currentTab == NavTab.CARDS,
                    testTag = "nav_cards",
                    onClick = { onTabSelected(NavTab.CARDS) }
                )

                // Center: HOME (خانه) - Slightly larger prominent center key
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(homeBgColor)
                        .shadow(
                            elevation = if (isHomePlusActive) 12.dp else 8.dp,
                            shape = CircleShape,
                            spotColor = if (isHomePlusActive) BentoIndigoAccent else BentoNavyDark
                        )
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (isHomePlusActive) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onHomePlusClick()
                                } else {
                                    onTabSelected(NavTab.HOME)
                                }
                            },
                            onLongClick = {
                                if (!isHomePlusActive) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onHomeLongClick()
                                } else {
                                    onHomePlusClick()
                                }
                            }
                        )
                        .testTag("nav_home"),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = isHomePlusActive,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)))
                                .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 0.5f))
                        },
                        label = "home_plus_icon_transition"
                    ) { isPlus ->
                        if (isPlus) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "بستن عملیات سریع",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(30.dp)
                                    .graphicsLayer { rotationZ = homeRotation }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "خانه",
                                tint = if (currentTab == NavTab.HOME) BentoLavenderAccent else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Calculator: Payment (پرداخت)
                NavBarIcon(
                    icon = Icons.Default.Calculate,
                    label = "پرداخت",
                    isSelected = currentTab == NavTab.CALCULATOR,
                    testTag = "nav_calculator",
                    onClick = { onTabSelected(NavTab.CALCULATOR) }
                )

                // Recipients / People (مخاطبین)
                NavBarIcon(
                    icon = Icons.Default.Person,
                    label = "مخاطبین",
                    isSelected = currentTab == NavTab.RECIPIENTS,
                    testTag = "nav_recipients",
                    onClick = { onTabSelected(NavTab.RECIPIENTS) }
                )
            }
        }
    }
}

@Composable
fun NavBarIcon(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isSelected) BentoLavenderSubtle else Color.Transparent)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) BentoIndigoAccent else TextTertiary,
            modifier = Modifier.size(24.dp)
        )
    }
}

fun formatCategoryDari(category: String): String {
    return when (category.lowercase().trim()) {
        "shopping" -> "خریداری"
        "food & dining", "food", "dining" -> "غذا و خوراک"
        "electronics" -> "وسایل برقی"
        "subscription", "subscriptions" -> "اشتراک‌ها"
        "salary" -> "معاش و عواید"
        "transfer", "transfers" -> "انتقالات"
        "bills" -> "بل‌ها و فاکتورها"
        "travel" -> "سفر و ترانسپورت"
        "general" -> "عمومی"
        else -> category
    }
}

// Helper function to format amount with up to 3 decimal places without rounding off
fun formatAmountDisplay(amount: Double): String {
    val df = java.text.DecimalFormat("#,##0.###", java.text.DecimalFormatSymbols(java.util.Locale.US))
    df.maximumFractionDigits = 3
    return df.format(amount)
}

@Composable
fun DigitalCardItem(
    card: AccountCardEntity,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val gradientBrush = when (card.cardColorTheme.lowercase()) {
        "teal" -> Brush.linearGradient(listOf(CardTealGradientStart, CardTealGradientEnd))
        "gold" -> Brush.linearGradient(listOf(CardGoldGradientStart, CardGoldGradientEnd))
        else -> Brush.linearGradient(listOf(CardDarkGradientStart, CardDarkGradientEnd))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .testTag("digital_card_${card.id}"),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Card Title + Currency Badge + Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (card.name == "Digital card") "کارت دیجیتال" else card.name,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (card.currencyCode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = card.currencyCode,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Card Chip Graphic
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (card.isFrozen) {
                            Text(
                                text = "مسدود",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0x55000000), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 34.dp, height = 24.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFFE2C465))
                                .border(1.dp, Color(0xFFC7A843), RoundedCornerShape(5.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card Number Masked: e.g. •••• 7642
                Text(
                    text = card.cardNumberMasked,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Row: Balance & Expiry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val displaySym = card.currencySymbol.ifBlank { currencySymbol }
                    Column {
                        Text(
                            text = "موجودی",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "$displaySym ${formatAmountDisplay(card.balance)}",
                                color = Color.White,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (card.isFrozen) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = "مسدود شده",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = card.cardNumberMasked,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TxnTypeVisuals(
    val label: String,
    val bg: Color,
    val color: Color,
    val icon: ImageVector
)

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    currencySymbol: String,
    modifier: Modifier = Modifier,
    accounts: List<AccountCardEntity> = emptyList(),
    showTypeBadge: Boolean = true,
    showCategoryBadge: Boolean = true,
    showNote: Boolean = true,
    isAmountMasked: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val isTransfer = transaction.type == TransactionType.TRANSFER
    val hasRecipient = !transaction.recipientName.isNullOrBlank()

    val isGoalDeposit = transaction.kind == TransactionKind.GOAL_DEPOSIT ||
        transaction.category == "واریز به هدف" ||
        transaction.title.startsWith("انتقال به هدف") ||
        transaction.title.startsWith("واریز به هدف")

    val isGoalWithdraw = transaction.kind == TransactionKind.GOAL_WITHDRAW ||
        transaction.category == "برداشت از هدف" ||
        transaction.title.startsWith("انتقال از هدف") ||
        transaction.title.startsWith("برداشت از هدف")

    val isGoalTxn = isGoalDeposit || isGoalWithdraw

    val goalName = if (isGoalTxn) {
        transaction.title
            .removePrefix("انتقال به هدف: ")
            .removePrefix("واریز به هدف: ")
            .removePrefix("انتقال از هدف: ")
            .removePrefix("برداشت از هدف: ")
            .trim()
            .ifEmpty { "هدف مالی" }
    } else ""

    val visuals = when {
        isGoalDeposit -> TxnTypeVisuals(
            "واریز به هدف",
            Color(0xFFF5F3FF),
            Color(0xFF7C3AED),
            Icons.Default.Savings
        )
        isGoalWithdraw -> TxnTypeVisuals(
            "برداشت از هدف",
            Color(0xFFFFFBEB),
            Color(0xFFD97706),
            Icons.Default.Savings
        )
        isTransfer -> TxnTypeVisuals(
            "انتقال",
            Color(0xFFEEF2FF),
            BentoIndigoAccent,
            Icons.Default.SwapHoriz
        )
        hasRecipient && isExpense -> TxnTypeVisuals(
            "پرداخت",
            Color(0xFFFFFBEB),
            Color(0xFFD97706),
            Icons.AutoMirrored.Filled.TrendingDown
        )
        hasRecipient && !isExpense -> TxnTypeVisuals(
            "دریافت",
            Color(0xFFF0FDF4),
            Color(0xFF0D9488),
            Icons.AutoMirrored.Filled.TrendingUp
        )
        !hasRecipient && isExpense -> TxnTypeVisuals(
            "مصرف",
            Color(0xFFFFF1F2),
            ExpenseRed,
            Icons.AutoMirrored.Filled.TrendingDown
        )
        else -> TxnTypeVisuals(
            "عاید",
            Color(0xFFECFDF5),
            IncomeGreen,
            Icons.AutoMirrored.Filled.TrendingUp
        )
    }
    val typeLabel = visuals.label
    val typeBg = visuals.bg
    val typeColor = visuals.color
    val typeIcon = visuals.icon

    val displayTitle = when {
        isGoalDeposit -> {
            val matchedAccount = accounts.find { it.id == transaction.accountId }
            val acctName = if (transaction.accountId == 0L) "صندوق نقد" else (matchedAccount?.name ?: "کارت بانکی")
            "انتقال از $acctName به $goalName"
        }
        isGoalWithdraw -> {
            val matchedAccount = accounts.find { it.id == transaction.accountId }
            val acctName = if (transaction.accountId == 0L) "صندوق نقد" else (matchedAccount?.name ?: "کارت بانکی")
            "انتقال از $goalName به $acctName"
        }
        !showNote -> {
            if (hasRecipient) {
                transaction.recipientName ?: "معامله"
            } else if (transaction.title.isNotBlank() &&
                !transaction.title.equals(transaction.category, ignoreCase = true) &&
                !transaction.title.equals("مصرف عمومی", ignoreCase = true) &&
                !transaction.title.equals("عاید عمومی", ignoreCase = true) &&
                !transaction.title.equals("مصرف", ignoreCase = true) &&
                !transaction.title.equals("عاید", ignoreCase = true) &&
                !transaction.title.equals(transaction.note ?: "", ignoreCase = true)
            ) {
                transaction.title
            } else {
                "معامله"
            }
        }
        !showCategoryBadge && (transaction.title.equals(transaction.category, ignoreCase = true) ||
                transaction.title.equals("مصرف عمومی", ignoreCase = true) ||
                transaction.title.equals("مصرف", ignoreCase = true) ||
                transaction.title.equals("عاید عمومی", ignoreCase = true)) -> {
            if (!transaction.note.isNullOrBlank()) transaction.note
            else if (hasRecipient) transaction.recipientName ?: "معامله"
            else "معامله"
        }
        else -> transaction.title
    }

    Surface(
        modifier = modifier
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
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("transaction_row_${transaction.id}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(typeBg)
                        .border(1.dp, typeColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isGoalTxn -> Icons.Default.Savings
                            hasRecipient -> Icons.Default.Person
                            else -> typeIcon
                        },
                        contentDescription = typeLabel,
                        tint = typeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title & Type Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = displayTitle,
                            color = BentoNavyDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (showTypeBadge && !isGoalTxn) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = typeBg,
                                border = BorderStroke(0.8.dp, typeColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = typeLabel,
                                    color = typeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Essential Details Chips (Account / Card, Category, Recipient, Goal Details)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isGoalDeposit || isGoalWithdraw) {
                            val cleanNote = cleanTransactionNote(transaction.note)
                            if (showNote && !cleanNote.isNullOrBlank()) {
                                Text(
                                    text = cleanNote,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            // Account / Wallet Badge
                            if (transaction.affectsBalance) {
                                val matchedAccount = accounts.find { it.id == transaction.accountId }
                                val acctName = if (transaction.accountId == 0L) "کیف‌پول نقد" else (matchedAccount?.name ?: "کارت بانکی")
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(0.6.dp, BentoBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = acctName,
                                            color = BentoNavyDark,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Category Badge
                            if (showCategoryBadge && transaction.category.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(0.6.dp, BentoBorder)
                                ) {
                                    Text(
                                        text = formatCategoryDari(transaction.category),
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Recipient info if title is generic
                            if (hasRecipient && !transaction.title.contains(transaction.recipientName ?: "")) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFFEEF2FF)
                                ) {
                                    Text(
                                        text = transaction.recipientName ?: "",
                                        color = BentoIndigoAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Not affecting balance badge
                            if (!transaction.affectsBalance) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "بدون تغییر موجودی",
                                        color = Color(0xFFB45309),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Date & Time + Calculation Expression
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = PersianDateHelper.formatSolarDateTime(transaction.timestamp),
                            color = TextTertiary,
                            fontSize = 10.sp
                        )

                        if (!transaction.calculationExpression.isNullOrBlank() && transaction.calculationExpression != transaction.amount.toString()) {
                            Text(
                                text = "(${transaction.calculationExpression})",
                                color = BentoIndigoAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Due Date Badge with Remaining Days inside parenthesis
                    if (transaction.dueDate != null && transaction.dueDate > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        val remainingDays = PersianDateHelper.getRemainingDaysText(transaction.dueDate)
                        val isOverdue = remainingDays.contains("گذشته")
                        val isSettled = transaction.isSettled
                        val badgeBg = when {
                            isSettled -> IncomeGreenBg
                            isOverdue -> ExpenseRedBg
                            else -> Color(0xFFFEF3C7)
                        }
                        val badgeColor = when {
                            isSettled -> IncomeGreen
                            isOverdue -> ExpenseRed
                            else -> Color(0xFFB45309)
                        }
                        val dueText = when {
                            isSettled -> "سررسید: ${PersianDateHelper.formatSolarDate(transaction.dueDate)} (تسویه‌شده)"
                            remainingDays.isNotBlank() -> "سررسید: ${PersianDateHelper.formatSolarDate(transaction.dueDate)} ($remainingDays)"
                            else -> "سررسید: ${PersianDateHelper.formatSolarDate(transaction.dueDate)}"
                        }

                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = badgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSettled) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = dueText,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount Display with Currency strictly on the LEFT: e.g. "$ -60" or "؋ +2,000"
            val prefix = when {
                isGoalDeposit -> "-"
                isGoalWithdraw -> "+"
                isTransfer -> ""
                isExpense -> "-"
                else -> "+"
            }
            val amountColor = when {
                isGoalDeposit -> Color(0xFF7C3AED)
                isGoalWithdraw -> IncomeGreen
                isTransfer -> BentoIndigoAccent
                isExpense -> ExpenseRed
                else -> IncomeGreen
            }
            val displaySymbol = transaction.currencySymbol.ifBlank { currencySymbol }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isAmountMasked) {
                        Text(
                            text = displaySymbol,
                            color = BentoIndigoAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "••••••",
                            color = BentoIndigoAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
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
                            text = displaySymbol,
                            color = amountColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$prefix${formatAmountDisplay(transaction.amount)}",
                            color = amountColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecipientAvatarItem(
    recipient: RecipientEntity,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
            .testTag("recipient_${recipient.id}")
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(recipient.avatarColorHex))
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) PrimaryDark else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recipient.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = recipient.name,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
