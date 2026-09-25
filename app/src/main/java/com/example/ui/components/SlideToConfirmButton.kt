package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToConfirmButton(
    label: String = "برای ثبت معامله به چپ بکشید",
    sublabel: String = "تأیید و ارسال",
    isConfirmed: Boolean = false,
    isEnabled: Boolean = true,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val thumbAnim = remember { Animatable(0f) }
    var maxDragPx by remember { mutableFloatStateOf(0f) }
    var hasTriggeredConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(isConfirmed) {
        if (!isConfirmed) {
            hasTriggeredConfirmation = false
            thumbAnim.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(if (isEnabled) Color(0xFFF1F4F9) else Color(0xFFE5E9F0))
            .border(1.dp, BentoBorder, RoundedCornerShape(26.dp))
            .testTag("slide_to_confirm_container"),
        contentAlignment = Alignment.CenterEnd
    ) {
        val totalWidthPx = with(density) { maxWidth.toPx() }
        val thumbSizePx = with(density) { 44.dp.toPx() }
        val paddingPx = with(density) { 4.dp.toPx() }
        maxDragPx = (totalWidthPx - thumbSizePx - (paddingPx * 2)).coerceAtLeast(0f)

        val progress = if (maxDragPx > 0f) (thumbAnim.value / maxDragPx).coerceIn(0f, 1f) else 0f

        // Gradual background fill that expands from Right to Left following the slider thumb
        val fillWidthDp = with(density) { (paddingPx * 2 + thumbSizePx + thumbAnim.value).toDp() }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(52.dp)
                .width(fillWidthDp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    if (isConfirmed || progress >= 0.95f) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF10B981), IncomeGreen)
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(BentoIndigoAccent, BentoNavyDark)
                        )
                    }
                )
        )

        // Center Hint Text (fades out as dragged)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = !isConfirmed && !hasTriggeredConfirmation,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 52.dp)
                ) {
                    Text(
                        text = if (isEnabled) "$label  •  $sublabel" else "مبلغ را وارد کنید",
                        color = if (progress > 0.45f) Color.White.copy(alpha = (1f - progress).coerceAtLeast(0f))
                               else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Confirmed Text
            AnimatedVisibility(
                visible = isConfirmed || hasTriggeredConfirmation,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "✓ معامله با موفقیت انجام شد",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Draggable Thumb Button (Starts at Right, dragged towards Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset { IntOffset((-paddingPx - thumbAnim.value).roundToInt(), 0) }
                .padding(vertical = 4.dp)
                .size(44.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .border(1.5.dp, if (progress > 0.8f) IncomeGreen else BentoBorder, CircleShape)
                .pointerInput(isEnabled, isConfirmed, maxDragPx) {
                    if (!isEnabled || isConfirmed || hasTriggeredConfirmation) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            if (thumbAnim.value >= maxDragPx * 0.72f) {
                                // Trigger confirmation
                                coroutineScope.launch {
                                    thumbAnim.animateTo(
                                        targetValue = maxDragPx,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    hasTriggeredConfirmation = true
                                    onConfirmed()
                                }
                            } else {
                                // Snap back to right
                                coroutineScope.launch {
                                    thumbAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            // Moving to the left means negative dragAmount, so distance moved left is -dragAmount
                            val next = (thumbAnim.value - dragAmount).coerceIn(0f, maxDragPx)
                            coroutineScope.launch {
                                thumbAnim.snapTo(next)
                            }
                            if (next >= maxDragPx * 0.80f && !hasTriggeredConfirmation) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                }
                .testTag("slide_to_confirm_thumb"),
            contentAlignment = Alignment.Center
        ) {
            if (isConfirmed || hasTriggeredConfirmation) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirmed",
                    tint = IncomeGreen,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Slide left to confirm",
                    tint = if (isEnabled) BentoNavyDark else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
