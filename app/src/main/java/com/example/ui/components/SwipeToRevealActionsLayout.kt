package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Reusable Swipe to Reveal Layout for list items.
 *
 * Supports bidirectional dragging:
 * - Dragging to the left reveals actions on the right.
 * - Dragging to the right reveals actions on the left.
 *
 * When released, if dragged past the threshold, it snaps open and stays visible so the user
 * can easily click Edit or Delete.
 * Clicking on the open foreground item closes it.
 */
@Composable
fun SwipeToRevealActionsLayout(
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    editLabel: String = "ویرایش",
    deleteLabel: String = "حذف",
    contentCornerRadius: Int = 16,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Determine how wide the revealed action buttons need to be
    val actionCount = (if (onEdit != null) 1 else 0) + (if (onDelete != null) 1 else 0)
    if (actionCount == 0) {
        // No actions, just render content
        content()
        return
    }

    val revealWidthDp = (actionCount * 58).dp
    val revealWidthPx = with(density) { revealWidthDp.toPx() }
    val dragThresholdPx = revealWidthPx * 0.35f

    val offsetXAnim = remember { Animatable(0f) }
    var revealedDirection by remember { mutableFloatStateOf(0f) } // -1 for left drag (revealed on right), 1 for right drag (revealed on left)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(contentCornerRadius.dp))
    ) {
        // --- BACKGROUND ACTION BUTTONS ---
        val isRevealedOnRight = offsetXAnim.value < 0f || (offsetXAnim.value == 0f && revealedDirection < 0f)
        val isRevealedOnLeft = offsetXAnim.value > 0f || (offsetXAnim.value == 0f && revealedDirection > 0f)

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0xFFF1F5F9)),
            contentAlignment = if (isRevealedOnRight) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onEdit != null) {
                    SwipeActionButton(
                        icon = Icons.Default.Edit,
                        label = editLabel,
                        backgroundColor = BentoNavyDark,
                        contentColor = Color.White,
                        onClick = {
                            scope.launch { offsetXAnim.animateTo(0f) }
                            onEdit()
                        }
                    )
                }

                if (onDelete != null) {
                    SwipeActionButton(
                        icon = Icons.Default.Delete,
                        label = deleteLabel,
                        backgroundColor = ExpenseRed,
                        contentColor = Color.White,
                        onClick = {
                            scope.launch { offsetXAnim.animateTo(0f) }
                            onDelete()
                        }
                    )
                }
            }
        }

        // --- FOREGROUND ITEM WITH GESTURE DETECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetXAnim.value.roundToInt(), 0) }
                .pointerInput(revealWidthPx) {
                    detectHorizontalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            scope.launch {
                                val currentOffset = offsetXAnim.value
                                val target = when {
                                    currentOffset < -dragThresholdPx -> {
                                        revealedDirection = -1f
                                        -revealWidthPx
                                    }
                                    currentOffset > dragThresholdPx -> {
                                        revealedDirection = 1f
                                        revealWidthPx
                                    }
                                    else -> {
                                        revealedDirection = 0f
                                        0f
                                    }
                                }
                                offsetXAnim.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetXAnim.animateTo(0f)
                                revealedDirection = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset = (offsetXAnim.value + dragAmount)
                                    .coerceIn(-revealWidthPx * 1.15f, revealWidthPx * 1.15f)
                                offsetXAnim.snapTo(newOffset)
                            }
                        }
                    )
                }
                .clickable(enabled = abs(offsetXAnim.value) > 5f) {
                    // Tap on card while revealed will snap it back closed
                    scope.launch {
                        offsetXAnim.animateTo(0f)
                        revealedDirection = 0f
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
private fun SwipeActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
