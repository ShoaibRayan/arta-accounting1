package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuickActionEntity
import com.example.ui.components.CreateOrEditQuickActionDialog
import com.example.ui.components.QuickActionManagementRow
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.FinanceViewModel

/**
 * صفحه مجزای مدیریت عملیات سریع و میانبرها (بدون نویگیشن بار).
 * با فشردن کلید بازگشت (در نوار بالا یا کلید سخت‌افزاری/سیستمی) به صفحه قبلی بازمی‌گردد.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsManagementScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val allActions by viewModel.allQuickActions.collectAsState()
    val maxHomeCount by viewModel.maxHomeQuickActions.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeCurrencies by viewModel.activeCurrencies.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val recipients by viewModel.recipients.collectAsState()
    val activeGoals by viewModel.activeGoals.collectAsState()

    var actionToEdit by remember { mutableStateOf<QuickActionEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var actionToDelete by remember { mutableStateOf<QuickActionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("quick_actions_management_screen")
    ) {
        // --- Top Bar (Header with back button, title, and count badge) ---
        Surface(
            color = SurfaceWhite,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(0.8.dp, BentoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BackgroundCanvas)
                            .border(1.dp, BentoBorder, CircleShape)
                            .testTag("quick_actions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = BentoNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "مدیریت عملیات سریع",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Text(
                            text = "مدیریت، ساخت و چینش میانبرهای صفحه اصلی",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(BentoLavenderAccent, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${allActions.size} میانبر",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                }
            }
        }

        // --- Body Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Settings Card: Max visible items on Home screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BentoBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                containerColor = BackgroundCanvas,
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
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(54.dp)
                        )
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
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 20.dp),
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
            shape = RoundedCornerShape(16.dp),
            containerColor = SurfaceWhite
        )
    }
}
