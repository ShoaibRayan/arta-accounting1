package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun OverallBudgetRingCard(
    totalBudget: Double,
    totalSpent: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val remaining = (totalBudget - totalSpent).coerceAtLeast(0.0)
    val ratio = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val clampedRatio = ratio.coerceIn(0f, 1f)

    var animationTrigger by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        animationTrigger = clampedRatio
    }

    val animatedProgress by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "overall_ring_progress"
    )

    // Status state
    val statusText = when {
        ratio > 1.0f -> "اضافه مصرف از بودجه"
        ratio > 0.80f -> "نزدیک به سقف بودجه"
        else -> "✓ وضعیت متوازن و مجاز"
    }
    val statusBg = when {
        ratio > 1.0f -> ExpenseRedBg
        ratio > 0.80f -> Color(0xFFFEF3C7) // Amber subtle
        else -> IncomeGreenBg
    }
    val statusColor = when {
        ratio > 1.0f -> ExpenseRed
        ratio > 0.80f -> Color(0xFFD97706) // Amber text
        else -> IncomeGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .border(1.dp, BentoBorder, RoundedCornerShape(32.dp))
            .testTag("overall_budget_ring_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with title and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بودجه کل ماهانه",
                    color = BentoNavyDark,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Ring Canvas Gauge
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    val strokeW = 16.dp.toPx()
                    val arcSize = Size(size.width - strokeW, size.height - strokeW)
                    val topLeft = Offset(strokeW / 2f, strokeW / 2f)

                    // Background full ring track
                    drawArc(
                        color = Color(0xFFEFF2F8),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Foreground animated ring arc
                    val sweep = animatedProgress * 360f
                    if (sweep > 0f) {
                        val arcBrush = Brush.sweepGradient(
                            listOf(
                                BentoIndigoAccent,
                                if (ratio > 0.85f) ExpenseRed else Color(0xFF38BDF8),
                                BentoIndigoAccent
                            )
                        )
                        drawArc(
                            brush = arcBrush,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Content: Remaining amount & text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "باقیمانده",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format(java.util.Locale.US, "%,.0f", remaining)}",
                        color = BentoNavyDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${(clampedRatio * 100).toInt()}% مصرف شده",
                        color = BentoIndigoAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer info: Spent vs Total Limit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF8FAFD))
                    .border(1.dp, BentoBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "مصرف شده", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currencySymbol${String.format(java.util.Locale.US, "%,.0f", totalSpent)}",
                        color = if (ratio > 1f) ExpenseRed else BentoNavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(BentoBorder)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "سقف کل بودجه", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currencySymbol${String.format(java.util.Locale.US, "%,.0f", totalBudget)}",
                        color = BentoNavyDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetBentoCard(
    category: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    spent: Double,
    limit: Double,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBudget = limit > 0
    val remaining = if (hasBudget) (limit - spent).coerceAtLeast(0.0) else 0.0
    val overSpent = if (hasBudget) (spent - limit).coerceAtLeast(0.0) else 0.0
    val isOver = hasBudget && spent > limit
    val ratio = if (hasBudget) (spent / limit).toFloat() else 0f
    val clampedRatio = ratio.coerceIn(0f, 1f)

    var animationTrigger by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        animationTrigger = clampedRatio
    }
    val animatedProgress by animateFloatAsState(
        targetValue = animationTrigger,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "cat_ring_progress_$category"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isOver) 1.5.dp else 1.dp,
                color = if (isOver) ExpenseRed.copy(alpha = 0.7f) else BentoBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .testTag("budget_card_$category"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isOver) Color(0xFFFFF1F2) else SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOver) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress Ring for Category
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    val strokeW = 6.dp.toPx()
                    val arcSize = Size(size.width - strokeW, size.height - strokeW)
                    val topLeft = Offset(strokeW / 2f, strokeW / 2f)

                    // Track
                    drawArc(
                        color = if (isOver) Color(0xFFFFE4E6) else Color(0xFFEFF2F8),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Progress Arc
                    val progressColor = when {
                        isOver -> ExpenseRed
                        ratio > 0.85f -> Color(0xFFF59E0B)
                        else -> BentoIndigoAccent
                    }
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = if (isOver) 360f else animatedProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                // Category Icon or % inside ring
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isOver) Color(0xFFFFE4E6) else iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category,
                        tint = if (isOver) ExpenseRed else iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatCategoryDari(category),
                            color = BentoNavyDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isOver) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ExpenseRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "کسری بودجه!",
                                    color = ExpenseRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = if (hasBudget) "${(ratio * 100).toInt()}%" else "بدون سقف",
                        color = if (isOver) ExpenseRed else if (hasBudget) BentoIndigoAccent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currencySymbol${String.format(java.util.Locale.US, "%,.0f", spent)} مصرف شده",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    if (isOver) {
                        Text(
                            text = "+$currencySymbol${String.format(java.util.Locale.US, "%,.0f", overSpent)} بیش از سقف",
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (hasBudget) {
                        Text(
                            text = "$currencySymbol${String.format(java.util.Locale.US, "%,.0f", remaining)} باقیمانده",
                            color = IncomeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "تعیین سقف +",
                            color = BentoIndigoAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
