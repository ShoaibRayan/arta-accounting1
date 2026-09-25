package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.QuickActionAmountBehavior
import com.example.data.local.QuickActionEntity
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.BentoNavyDarker
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * نمایش حباب‌های شناور عملیات سریع با طرح مدرن، دل‌انگیز و ترانزیشن نرم
 * با فشردن چند ثانیه‌ای کلید هوم این لایه ظاهر شده و کلید هوم به علامت (+) تغییر می‌کند.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionBubblesOverlay(
    actions: List<QuickActionEntity>,
    accounts: List<AccountCardEntity>,
    onSelectAction: (QuickActionEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(40)
        isContentVisible = true
    }

    // Gentle floating breathing animation for floating bubbles
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")
    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset_1"
    )
    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset_2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .testTag("quick_action_bubbles_overlay")
    ) {
        // Soft translucent frosted backdrop (محو و ملایم، نه سیاه)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x180F172A),
                            Color(0x330F172A),
                            Color(0x400F172A)
                        )
                    )
                )
        )

        // Floating Bubbles Content Box positioned above the bottom dock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp) // Leave space for the floating bottom bar
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Header Pill Badge
            AnimatedVisibility(
                visible = isContentVisible,
                enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.8f)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceWhite.copy(alpha = 0.95f),
                    border = BorderStroke(1.2.dp, BentoIndigoAccent.copy(alpha = 0.4f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "عملیات سریع (حباب‌های فعال)",
                            color = BentoNavyDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (actions.isEmpty()) {
                // Empty state bubble
                AnimatedVisibility(
                    visible = isContentVisible,
                    enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.7f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = fadeOut(tween(200))
                ) {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = SurfaceWhite.copy(alpha = 0.95f),
                        border = BorderStroke(1.2.dp, BentoBorder),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(BentoLavenderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "هنوز میانبری ایجاد نشده است",
                                color = BentoNavyDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "برای ساخت و مدیریت میانبرها، از دکمه منو در بالای صفحه اصلی استفاده کنید.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                // Organic Scattered Floating Bubbles Layout (پراکنده و نامنظم در پایین صفحه)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    val usableWidth = (maxWidth - 88.dp).coerceAtLeast(100.dp)
                    val usableHeight = (maxHeight - 100.dp).coerceAtLeast(100.dp)

                    actions.forEachIndexed { index, action ->
                        val anchor = scatterAnchors[index % scatterAnchors.size]
                        val delayMillis = (index * 50).coerceAtMost(300)

                        val xPos = usableWidth * anchor.xRatio
                        val yPos = usableHeight * anchor.yRatio

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isContentVisible,
                            enter = fadeIn(tween(250, delayMillis = delayMillis)) +
                                    scaleIn(
                                        initialScale = 0.2f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    ),
                            exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.2f),
                            modifier = Modifier.offset(x = xPos, y = yPos)
                        ) {
                            QuickActionBubbleItem(
                                action = action,
                                floatDuration = anchor.floatDuration,
                                floatAmp = anchor.floatAmp,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectAction(action)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class BubbleScatterAnchor(
    val xRatio: Float,
    val yRatio: Float,
    val floatDuration: Int,
    val floatAmp: Float
)

// مختصات نامنظم، پراکنده و متوازن برای حباب‌ها در بخش پایینی صفحه
private val scatterAnchors = listOf(
    BubbleScatterAnchor(xRatio = 0.10f, yRatio = 0.03f, floatDuration = 2300, floatAmp = 6f),
    BubbleScatterAnchor(xRatio = 0.74f, yRatio = 0.02f, floatDuration = 2700, floatAmp = 5f),
    BubbleScatterAnchor(xRatio = 0.42f, yRatio = 0.18f, floatDuration = 2400, floatAmp = 7f),
    BubbleScatterAnchor(xRatio = 0.04f, yRatio = 0.40f, floatDuration = 3100, floatAmp = 5f),
    BubbleScatterAnchor(xRatio = 0.82f, yRatio = 0.36f, floatDuration = 2800, floatAmp = 6f),
    BubbleScatterAnchor(xRatio = 0.38f, yRatio = 0.58f, floatDuration = 2500, floatAmp = 6f),
    BubbleScatterAnchor(xRatio = 0.80f, yRatio = 0.72f, floatDuration = 2200, floatAmp = 7f),
    BubbleScatterAnchor(xRatio = 0.08f, yRatio = 0.76f, floatDuration = 2900, floatAmp = 5f),
    BubbleScatterAnchor(xRatio = 0.48f, yRatio = 0.80f, floatDuration = 2600, floatAmp = 6f),
    BubbleScatterAnchor(xRatio = 0.62f, yRatio = 0.12f, floatDuration = 3000, floatAmp = 5f),
    BubbleScatterAnchor(xRatio = 0.22f, yRatio = 0.28f, floatDuration = 2400, floatAmp = 6f),
    BubbleScatterAnchor(xRatio = 0.60f, yRatio = 0.46f, floatDuration = 2750, floatAmp = 6f)
)

/**
 * کارت حباب تک عملیات سریع با جلوه شیشه‌ای و شناور دلپذیر
 */
@Composable
fun QuickActionBubbleItem(
    action: QuickActionEntity,
    floatDuration: Int = 2400,
    floatAmp: Float = 5f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionColor = Color(action.colorHex)
    val iconVector = getQuickActionIconVector(action.iconName)
    val typeColor = getActionTypeColor(action.actionType)

    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float_${action.id}")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -floatAmp,
        targetValue = floatAmp,
        animationSpec = infiniteRepeatable(
            animation = tween(floatDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "item_float_${action.id}"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .offset(y = floatOffset.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("bubble_item_${action.id}")
    ) {
        // Glowing Circular Bubble Orb
        Box(
            modifier = Modifier
                .size(62.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = actionColor.copy(alpha = 0.6f),
                    ambientColor = actionColor.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            actionColor.copy(alpha = 0.92f),
                            Color(0xFF0F172A).copy(alpha = 0.95f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.9f),
                                actionColor,
                                Color.White.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = action.title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            // Small glowing dot on top corner
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(typeColor)
                    .border(1.dp, Color.White, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Capsule Label - High contrast and readable
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xF20F172A),
            border = BorderStroke(1.dp, Color(0x33CBD5E1)),
            shadowElevation = 4.dp,
            modifier = Modifier.width(84.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = action.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                val badgeText = if (action.amountBehavior == QuickActionAmountBehavior.FIXED && action.defaultAmount > 0) {
                    "${action.currencySymbol} ${action.defaultAmount.toInt()}"
                } else {
                    getActionTypeLabel(action.actionType)
                }

                Text(
                    text = badgeText,
                    color = Color(0xFFCBD5E1),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
