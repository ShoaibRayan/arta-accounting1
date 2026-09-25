package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import com.example.ui.components.RecipientAuthDialog
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CurrencyEntity
import com.example.data.local.RecipientEntity
import com.example.data.local.TransactionEntity
import com.example.ui.components.NavTab
import com.example.ui.screens.PersonStatementScreen
import com.example.ui.components.LuxuryTransferDialog
import com.example.ui.components.LuxuryTransactionDetailAndEditBottomSheet
import com.example.ui.components.TransferTab
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.DividerColor
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenBg
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.RecipientCurrencyDebt
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

// Helper to normalize Persian, Arabic, and Western numbers
fun parseLocalizedNumber(input: String): Double {
    if (input.isBlank()) return 0.0
    val cleaned = input
        .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
        .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
        .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
        .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
        .replace('،', ',').replace('٫', '.').replace(",", "")
        .trim()
    return cleaned.toDoubleOrNull() ?: 0.0
}

fun getRecipientIconVector(iconName: String): ImageVector = when (iconName) {
    "Store" -> Icons.Default.Storefront
    "Work" -> Icons.Default.Work
    "Business" -> Icons.Default.CorporateFare
    "Group" -> Icons.Default.Group
    "Family" -> Icons.Default.Home
    "Bank" -> Icons.Default.AccountBalance
    "Star" -> Icons.Default.Star
    else -> Icons.Default.Person
}

val RECIPIENT_PALETTE = listOf(
    0xFF21C6D8, // Turquoise
    0xFF6366F1, // Indigo
    0xFF10B981, // Emerald
    0xFFFF6584, // Coral Rose
    0xFFF59E0B, // Amber
    0xFF8B5CF6, // Purple
    0xFF0EA5E9, // Sky Blue
    0xFF1E293B  // Slate Navy
)

val RECIPIENT_ICON_OPTIONS = listOf(
    "Person" to "شخصی",
    "Store" to "فروشگاه",
    "Work" to "دفتر و کار",
    "Business" to "تجارت",
    "Group" to "دوستان",
    "Family" to "خانواده",
    "Bank" to "صرافی / بانک",
    "Star" to "ویژه"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientsScreen(
    viewModel: FinanceViewModel,
    onNavigateTab: (NavTab) -> Unit,
    onNavigateToShoppingLists: ((Long?) -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    onStatementVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val recipients by viewModel.recipients.collectAsStateWithLifecycle()
    // Observe transactions to ensure reactiveness when debts or conversions update
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val activeCurrencies by viewModel.activeCurrencies.collectAsStateWithLifecycle()
    val accounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val shoppingLists by viewModel.shoppingLists.collectAsStateWithLifecycle()
    val operationError by viewModel.operationErrorMessage.collectAsStateWithLifecycle()
    val recipientDebtsMap by viewModel.recipientDebtsMap.collectAsStateWithLifecycle()

    var searchRecipientQuery by remember { mutableStateOf("") }
    var showAddOrEditDialog by remember { mutableStateOf(false) }
    var recipientToEdit by remember { mutableStateOf<RecipientEntity?>(null) }
    var recipientToDelete by remember { mutableStateOf<RecipientEntity?>(null) }
    var selectedRecipientDetails by remember { mutableStateOf<RecipientEntity?>(null) }
    var recipientToConvert by remember { mutableStateOf<RecipientEntity?>(null) }
    var showConvertPersonDialog by remember { mutableStateOf(false) }
    var statementRecipient by remember { mutableStateOf<RecipientEntity?>(null) }
    var statementTargetCurrency by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(statementRecipient) {
        onStatementVisibilityChanged(statementRecipient != null)
    }
    DisposableEffect(Unit) {
        onDispose {
            onStatementVisibilityChanged(false)
        }
    }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionEntity?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferPreselectedPersonName by remember { mutableStateOf<String?>(null) }
    var transferPreselectedPersonId by remember { mutableStateOf<Long?>(null) }
    var shoppingListIdToOpen by remember { mutableStateOf<Long?>(null) }
    var showShoppingListsSheet by remember { mutableStateOf(false) }

    val unmaskedRecipientIds by viewModel.unmaskedRecipientIds.collectAsStateWithLifecycle()
    var recipientToAuthenticate by remember { mutableStateOf<RecipientEntity?>(null) }
    var showAllRecipientsAuthDialog by remember { mutableStateOf(false) }
    var authPromptTitle by remember { mutableStateOf("احراز هویت برای نمایش مبلغ") }
    var authPromptSubtitle by remember { mutableStateOf<String?>(null) }
    var onAuthSuccessAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val baseCurrencySummary = remember(recipients, allTransactions, unmaskedRecipientIds, activeCurrencies) {
        viewModel.getRecipientsBaseCurrencySummary(recipients)
    }

    val recentRecipients = remember(recipients) {
        recipients.filter { it.isActive && (it.isFavorite || it.transactionCount > 5) }
    }
    val allRecipientsFiltered = remember(recipients, searchRecipientQuery) {
        recipients.filter {
            searchRecipientQuery.isBlank() ||
                it.name.contains(searchRecipientQuery, ignoreCase = true) ||
                it.handleOrPhone.contains(searchRecipientQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
            .testTag("recipients_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 78.dp)
    ) {
        // --- Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "اشخاص و حساب‌ها",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Transfer Debt / Credit Button (انتقال حساب اشخاص / حواله اشخاص - آیکن تنها)
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                transferPreselectedPersonName = null
                                showTransferDialog = true
                            }
                            .testTag("transfer_recipients_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "حواله اشخاص (انتقال حساب)",
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Add Recipient Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryDark)
                            .clickable {
                                recipientToEdit = null
                                showAddOrEditDialog = true
                            }
                            .testTag("add_recipient_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "افزودن شخص جدید",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- Search Field ---
        item {
            OutlinedTextField(
                value = searchRecipientQuery,
                onValueChange = { searchRecipientQuery = it },
                placeholder = { Text("جستجو بر اساس نام، شماره تماس یا آیدی...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = TextTertiary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("recipient_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BentoNavyDark,
                    unfocusedTextColor = BentoNavyDark,
                    cursorColor = BentoNavyDark,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite,
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = DividerColor
                )
            )
        }

        // --- Recent Horizontal Carousel ---
        if (recentRecipients.isNotEmpty() && searchRecipientQuery.isBlank()) {
            item {
                Text(
                    text = "اشخاص پرکاربرد",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentRecipients, key = { it.id }) { r ->
                        val debts = recipientDebtsMap[r.id] ?: emptyList()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedRecipientDetails = r }
                                .padding(4.dp)
                                .testTag("recent_recipient_${r.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(Color(r.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getRecipientIconVector(r.iconName),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                if (r.isAmountProtected) {
                                    val isMasked = viewModel.isRecipientMasked(r.id)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(BentoNavyDark)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isMasked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = "محافظت از مبلغ",
                                            tint = if (isMasked) Color.White else AccentLime,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = r.name,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // --- Section Title with Total Count & Hint ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "همه اشخاص (${allRecipientsFiltered.size})",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- Swipeable List of All Recipients ---
        items(allRecipientsFiltered, key = { it.id }) { r ->
            val personDebts = recipientDebtsMap[r.id] ?: emptyList()
            SwipeableRecipientCard(
                recipient = r,
                debts = personDebts,
                onClick = { selectedRecipientDetails = r },
                onToggleActive = {
                    viewModel.updateRecipient(r.copy(isActive = !r.isActive))
                },
                onSwipeToPay = {
                    viewModel.selectRecipient(r)
                    viewModel.preparePaymentScreen()
                    onNavigateTab(NavTab.CALCULATOR)
                },
                onSwipeToReceive = {
                    viewModel.selectRecipient(r)
                    viewModel.prepareReceiveScreen()
                    onNavigateTab(NavTab.CALCULATOR)
                },
                onAuthenticateRecipient = { person ->
                    authPromptTitle = "احراز هویت برای نمایش مبلغ"
                    authPromptSubtitle = "برای مشاهده مبالغ طلب و بدهی «${person.name}»، رمز عبور را وارد کنید"
                    onAuthSuccessAction = null
                    recipientToAuthenticate = person
                },
                onMaskRecipient = { personId ->
                    viewModel.maskRecipient(personId)
                }
            )
        }

        // --- Base Currency Summary Footer (جمله حسابات به ارز پایه: طلب و بدهی) ---
        item {
            Spacer(modifier = Modifier.height(16.dp))
            RecipientsBaseCurrencySummaryCard(
                summary = baseCurrencySummary,
                onRequestUnlock = {
                    showAllRecipientsAuthDialog = true
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- Luxury Person Details Modal Bottom Sheet ---
    if (selectedRecipientDetails != null) {
        val person = recipients.firstOrNull { it.id == selectedRecipientDetails!!.id } ?: selectedRecipientDetails!!
        val debts = remember(allTransactions, person.id, unmaskedRecipientIds, recipients) { viewModel.getRecipientMultiCurrencyDebts(person.id) }
        val clipboardManager = LocalClipboardManager.current
        var copiedToast by remember { mutableStateOf(false) }

        LaunchedEffect(copiedToast) {
            if (copiedToast) {
                kotlinx.coroutines.delay(2000)
                copiedToast = false
            }
        }

        ModalBottomSheet(
            onDismissRequest = { selectedRecipientDetails = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCBD5E1))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header: Avatar, Name, Handle/Phone, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(person.avatarColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getRecipientIconVector(person.iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = person.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            if (person.handleOrPhone.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(person.handleOrPhone))
                                            copiedToast = true
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = person.handleOrPhone,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "کپی",
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    if (copiedToast) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "کپی شد ✓",
                                            fontSize = 10.sp,
                                            color = IncomeGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "بدون شماره یا شناسه",
                                    fontSize = 12.sp,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { selectedRecipientDetails = null },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 1-Tap Active / Inactive Switch Card (سریع، بدون نیاز به ویرایش)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = person.copy(isActive = !person.isActive)
                                viewModel.updateRecipient(updated)
                                selectedRecipientDetails = updated
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (person.isActive) IncomeGreen else TextTertiary)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (person.isActive) "وضعیت حساب: فعال" else "وضعیت حساب: غیرفعال",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (person.isActive) "در لیست پرداخت‌ها قابل انتخاب است" else "مخاطب غیرفعال است و پیشنهاد نمی‌شود",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = person.isActive,
                            onCheckedChange = { isChecked ->
                                val updated = person.copy(isActive = isChecked)
                                viewModel.updateRecipient(updated)
                                selectedRecipientDetails = updated
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IncomeGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                // --- Debts & Financial Balances Section (قسمت قرض‌ها - شیک و مجزا با تم اصلی) ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "وضعیت قرض‌ها و مانده حساب مخاطب",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    if (debts.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    statementTargetCurrency = null
                                    statementRecipient = person
                                    selectedRecipientDetails = null
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "حساب کاملاً تسویه است (۰)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                    Text(
                                        text = "هیچ طلب یا بدهی ثبت‌شده‌ای با این مخاطب وجود ندارد.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    } else {
                        debts.forEach { debt ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        statementTargetCurrency = debt.currencyCode
                                        statementRecipient = person
                                        selectedRecipientDetails = null
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = if (debt.isDebtor) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                                border = BorderStroke(
                                    1.dp,
                                    if (debt.isDebtor) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = BentoNavyDark
                                            ) {
                                                Text(
                                                    text = debt.currencyCode,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = if (debt.isDebtor) "طلب شما از شخص" else "بدهی شما به شخص",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (debt.isDebtor) IncomeGreen else ExpenseRed
                                            )
                                            if (debt.isMasked) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "محافظت شده",
                                                    tint = BentoIndigoAccent,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = if (debt.isMasked) {
                                                    Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {
                                                            authPromptTitle = "احراز هویت برای نمایش مبلغ"
                                                            authPromptSubtitle = "برای مشاهده مبالغ طلب و بدهی «${person.name}»، رمز عبور را وارد کنید"
                                                            onAuthSuccessAction = null
                                                            recipientToAuthenticate = person
                                                        }
                                                } else Modifier
                                            ) {
                                                Text(
                                                    text = if (debt.isMasked) "${debt.currencySymbol} ****" else "${debt.currencySymbol} ${debt.formattedAmount}",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (debt.isMasked) BentoNavyDark else if (debt.isDebtor) Color(0xFF166534) else Color(0xFF991B1B)
                                                )
                                                if (debt.isMasked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "لمس کنید برای نمایش مبلغ",
                                                        tint = BentoIndigoAccent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = if (debt.isMasked) "مبلغ محافظت‌شده است • برای نمایش یا صورتحساب لمس کنید 🔒" else "برای مشاهده صورتحساب این ارز لمس کنید",
                                            fontSize = 10.sp,
                                            color = if (debt.isMasked) BentoIndigoAccent else TextSecondary.copy(alpha = 0.8f)
                                        )
                                    }

                                    // Quick Pay/Receive for this specific debt currency
                                    Button(
                                        onClick = {
                                            viewModel.selectRecipient(person)
                                            if (debt.isDebtor) {
                                                viewModel.prepareReceiveScreen()
                                            } else {
                                                viewModel.preparePaymentScreen()
                                            }
                                            viewModel.setCurrency(debt.currencyCode)
                                            selectedRecipientDetails = null
                                            onNavigateTab(NavTab.CALCULATOR)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (debt.isDebtor) IncomeGreen else ExpenseRed
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (debt.isDebtor) "دریافت طلب" else "پرداخت بدهی",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Notes Card
                if (person.notes.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = "یادداشت‌ها:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = person.notes,
                                    fontSize = 12.sp,
                                    color = BentoNavyDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.selectRecipient(person)
                            viewModel.preparePaymentScreen()
                            selectedRecipientDetails = null
                            onNavigateTab(NavTab.CALCULATOR)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پرداخت بدهی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.selectRecipient(person)
                            viewModel.prepareReceiveScreen()
                            selectedRecipientDetails = null
                            onNavigateTab(NavTab.CALCULATOR)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دریافت طلب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            recipientToConvert = person
                            showConvertPersonDialog = true
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent)
                    ) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تبدیل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Transfer Debt/Credit to another Person
                OutlinedButton(
                    onClick = {
                        transferPreselectedPersonName = person.name
                        transferPreselectedPersonId = person.id
                        showTransferDialog = true
                        selectedRecipientDetails = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("transfer_person_account_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = BentoNavyDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "انتقال طلب یا بدهی به شخص دیگر",
                        color = BentoNavyDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Amount Protection Toggle Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, BentoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                    .background(if (person.isAmountProtected) BentoIndigoAccent.copy(alpha = 0.12f) else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (person.isAmountProtected) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (person.isAmountProtected) BentoIndigoAccent else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "محافظت از مبلغ طلب و بدهی",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (person.isAmountProtected) "مبالغ مخفی هستند و فقط با احراز هویت نمایش داده می‌شوند"
                                           else "مبالغ بدون نیاز به رمز عبور نمایش داده می‌شوند",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = person.isAmountProtected,
                            onCheckedChange = { willProtect ->
                                if (willProtect) {
                                    if (!viewModel.securityManager.hasPasscode()) {
                                        authPromptTitle = "قفل برنامه فعال نیست"
                                        authPromptSubtitle = "برای محافظت از مبالغ، ابتدا قفل برنامه را در بخش تنظیمات فعال کنید."
                                        onAuthSuccessAction = null
                                        recipientToAuthenticate = person
                                    } else {
                                        viewModel.setRecipientAmountProtected(person.id, true)
                                        selectedRecipientDetails = person.copy(isAmountProtected = true)
                                    }
                                } else {
                                    // Disable protection requires authentication
                                    authPromptTitle = "غیرفعال‌سازی محافظت از مبلغ"
                                    authPromptSubtitle = "برای خاموش‌کردن محافظت از مبالغ «${person.name}»، رمز عبور را وارد کنید"
                                    onAuthSuccessAction = {
                                        viewModel.setRecipientAmountProtected(person.id, false)
                                        selectedRecipientDetails = person.copy(isAmountProtected = false)
                                    }
                                    recipientToAuthenticate = person
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BentoIndigoAccent
                            )
                        )
                    }
                }

                // Edit & Delete row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (person.isAmountProtected && viewModel.isRecipientMasked(person.id) && viewModel.securityManager.hasPasscode()) {
                                authPromptTitle = "احراز هویت برای ویرایش شخص"
                                authPromptSubtitle = "برای ویرایش مشخصات «${person.name}»، رمز عبور را وارد کنید"
                                onAuthSuccessAction = {
                                    recipientToEdit = person
                                    showAddOrEditDialog = true
                                    selectedRecipientDetails = null
                                }
                                recipientToAuthenticate = person
                            } else {
                                recipientToEdit = person
                                showAddOrEditDialog = true
                                selectedRecipientDetails = null
                            }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoIndigoAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ویرایش مشخصات کامل", fontSize = 12.sp, color = BentoIndigoAccent, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = {
                            recipientToDelete = person
                            selectedRecipientDetails = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حذف مخاطب", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // --- Person Financial Statement Screen (گردش حساب و صورت‌حساب اشخاص) ---
    if (statementRecipient != null) {
        val currentRecipient = statementRecipient!!
        val isMasked = viewModel.isRecipientMasked(currentRecipient.id)
        PersonStatementScreen(
            recipient = currentRecipient,
            transactions = allTransactions,
            accounts = accounts,
            targetCurrency = statementTargetCurrency,
            viewModel = viewModel,
            isMasked = isMasked,
            onRequestUnmask = {
                authPromptTitle = "احراز هویت برای صورتحساب"
                authPromptSubtitle = "برای مشاهده مبالغ صورتحساب «${currentRecipient.name}»، رمز عبور را وارد کنید"
                onAuthSuccessAction = null
                recipientToAuthenticate = currentRecipient
            },
            onNavigateToPay = { p, curr ->
                statementRecipient = null
                statementTargetCurrency = null
                viewModel.selectRecipient(p)
                viewModel.preparePaymentScreen()
                if (curr != null) viewModel.setCurrency(curr)
                onNavigateTab(NavTab.CALCULATOR)
            },
            onNavigateToReceive = { p, curr ->
                statementRecipient = null
                statementTargetCurrency = null
                viewModel.selectRecipient(p)
                viewModel.prepareReceiveScreen()
                if (curr != null) viewModel.setCurrency(curr)
                onNavigateTab(NavTab.CALCULATOR)
            },
            onDismiss = {
                statementRecipient = null
                statementTargetCurrency = null
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // --- Recipient Authentication Dialog (For Unmasking or Protection Changes) ---
    if (recipientToAuthenticate != null) {
        val personToAuth = recipientToAuthenticate!!
        RecipientAuthDialog(
            recipientName = personToAuth.name,
            viewModel = viewModel,
            promptTitle = authPromptTitle,
            promptSubtitle = authPromptSubtitle,
            onAuthenticated = {
                viewModel.unmaskRecipient(personToAuth.id)
                onAuthSuccessAction?.invoke()
                recipientToAuthenticate = null
                onAuthSuccessAction = null
            },
            onNavigateToSettings = {
                recipientToAuthenticate = null
                onAuthSuccessAction = null
                onNavigateToSettings?.invoke()
            },
            onDismiss = {
                recipientToAuthenticate = null
                authPromptTitle = "احراز هویت برای نمایش مبلغ"
                authPromptSubtitle = null
                onAuthSuccessAction = null
            }
        )
    }

    if (showAllRecipientsAuthDialog) {
        RecipientAuthDialog(
            recipientName = "همه اشخاص",
            viewModel = viewModel,
            promptTitle = "احراز هویت برای نمایش کل حسابات",
            promptSubtitle = "برای مشاهده جمله طلب و بدهی اشخاص محافظت‌شده، رمز عبور را وارد کنید",
            onAuthenticated = {
                viewModel.unmaskAllRecipients()
                showAllRecipientsAuthDialog = false
            },
            onNavigateToSettings = {
                showAllRecipientsAuthDialog = false
                onNavigateToSettings?.invoke()
            },
            onDismiss = {
                showAllRecipientsAuthDialog = false
            }
        )
    }

    // Transaction Details and Edit Sheet from Person Statement (Requirement 22)
    selectedTransactionForDetails?.let { txn ->
        LuxuryTransactionDetailAndEditBottomSheet(
            transaction = txn,
            activeCurrencies = activeCurrencies,
            accounts = accounts,
            categories = categories,
            shoppingLists = shoppingLists,
            isAmountMasked = viewModel.isTransactionAmountMasked(txn),
            onDismiss = { selectedTransactionForDetails = null },
            onUpdate = { oldTxn, newTxn, onResult ->
                viewModel.updateTransaction(
                    oldTxn = oldTxn,
                    newTxn = newTxn,
                    onSuccess = {
                        selectedTransactionForDetails = newTxn
                        onResult(true, null)
                    },
                    onError = { err ->
                        onResult(false, err)
                    }
                )
            },
            onDelete = { t ->
                viewModel.deleteTransaction(t)
                selectedTransactionForDetails = null
            },
            onOpenShoppingList = { listId ->
                selectedTransactionForDetails = null
                if (onNavigateToShoppingLists != null) {
                    onNavigateToShoppingLists(listId)
                } else {
                    shoppingListIdToOpen = listId
                    showShoppingListsSheet = true
                }
            }
        )
    }

    if (showShoppingListsSheet) {
        ShoppingListsSheet(
            viewModel = viewModel,
            onDismiss = {
                showShoppingListsSheet = false
                shoppingListIdToOpen = null
            },
            initialListIdToOpen = shoppingListIdToOpen
        )
    }

    // --- Currency Conversion Dialog (Robust & Fixed) ---
    if (showConvertPersonDialog && (recipientToConvert != null || selectedRecipientDetails != null)) {
        val person = recipientToConvert ?: selectedRecipientDetails!!
        val debts = viewModel.getRecipientMultiCurrencyDebts(person.id)

        // Find which currency this person has debt in to set intelligent default
        val initialFromIdx = activeCurrencies.indexOfFirst { c ->
            debts.any { d -> d.currencyCode.equals(c.code, ignoreCase = true) }
        }.let { if (it >= 0) it else 0 }

        val initialToIdx = if (activeCurrencies.size > 1) {
            if (initialFromIdx == 0) 1 else 0
        } else 0

        var fromCurrIdx by remember { mutableIntStateOf(initialFromIdx) }
        var toCurrIdx by remember { mutableIntStateOf(initialToIdx) }
        var amountText by remember { mutableStateOf("") }
        var rateText by remember { mutableStateOf("1.0") }
        var targetAmountText by remember { mutableStateOf("") }
        var activeConvertCalculatorField by remember { mutableStateOf<String?>(null) }

        val fromCurr = activeCurrencies.getOrNull(fromCurrIdx) ?: activeCurrencies.firstOrNull()
        val toCurr = activeCurrencies.getOrNull(toCurrIdx) ?: activeCurrencies.firstOrNull()

        // Pre-fill amount from existing debt when fromCurr changes
        LaunchedEffect(fromCurrIdx) {
            if (fromCurr != null) {
                val matchingDebt = debts.firstOrNull { (fromCurr.id > 0 && it.currencyId == fromCurr.id) || it.currencyCode.equals(fromCurr.code, ignoreCase = true) }
                if (matchingDebt != null) {
                    val debtVal = kotlin.math.abs(matchingDebt.netAmount)
                    amountText = String.format(Locale.US, "%.0f", debtVal)
                    val r = parseLocalizedNumber(rateText).let { if (it > 0.0) it else 1.0 }
                    val targetVal = debtVal * r
                    targetAmountText = if (targetVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", targetVal) else String.format(Locale.US, "%.2f", targetVal).trimEnd('0').trimEnd('.')
                }
            }
        }

        // Auto calculate default exchange rate
        LaunchedEffect(fromCurr, toCurr) {
            if (fromCurr != null && toCurr != null && toCurr.exchangeRateToBase > 0) {
                val autoRate = fromCurr.exchangeRateToBase / toCurr.exchangeRateToBase
                rateText = if (autoRate == 1.0) "1.0" else String.format(Locale.US, "%.4f", autoRate).trimEnd('0').trimEnd('.')
                val amt = parseLocalizedNumber(amountText)
                if (amt > 0.0) {
                    val targetVal = amt * autoRate
                    targetAmountText = if (targetVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", targetVal) else String.format(Locale.US, "%.2f", targetVal).trimEnd('0').trimEnd('.')
                }
            }
        }

        val parsedAmount = parseLocalizedNumber(amountText)
        val parsedRate = parseLocalizedNumber(rateText).let { if (it > 0.0) it else 1.0 }
        val parsedTarget = parseLocalizedNumber(targetAmountText)
        val convertedTargetAmount = if (parsedTarget > 0.0) parsedTarget else (parsedAmount * parsedRate)

        // Check if person is debtor in fromCurrency
        val matchingDebt = debts.firstOrNull { (fromCurr != null && fromCurr.id > 0 && it.currencyId == fromCurr.id) || it.currencyCode.equals(fromCurr?.code, ignoreCase = true) }
        val isDebtor = matchingDebt?.isDebtor ?: true

        val targetAccountId = accounts.firstOrNull { it.isDefault }?.id
            ?: accounts.firstOrNull()?.id
            ?: 1L

        AlertDialog(
            onDismissRequest = { showConvertPersonDialog = false },
            title = {
                Text(
                    text = "تبدیل ارز حساب ${person.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoNavyDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "طلب یا بدهی این شخص را از یک ارز به ارز دیگر تبدیل کنید:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    // From currency selector
                    Text(text = "از ارز منبع:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeCurrencies.indices.toList()) { idx ->
                            val c = activeCurrencies[idx]
                            val isSel = idx == fromCurrIdx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) BentoNavyDark else Color(0xFFF1F5F9))
                                    .clickable { fromCurrIdx = idx }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${c.flagEmoji} ${c.code}",
                                    color = if (isSel) Color.White else BentoNavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // To currency selector
                    Text(text = "به ارز مقصد:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeCurrencies.indices.toList()) { idx ->
                            val c = activeCurrencies[idx]
                            val isSel = idx == toCurrIdx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF6366F1) else Color(0xFFF1F5F9))
                                    .clickable { toCurrIdx = idx }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${c.flagEmoji} ${c.code}",
                                    color = if (isSel) Color.White else BentoNavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Source amount field
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { newAmt ->
                            amountText = newAmt
                            val amt = parseLocalizedNumber(newAmt)
                            val r = parseLocalizedNumber(rateText)
                            if (amt > 0.0 && r > 0.0) {
                                val targetVal = amt * r
                                targetAmountText = if (targetVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", targetVal) else String.format(Locale.US, "%.2f", targetVal).trimEnd('0').trimEnd('.')
                            }
                        },
                        label = { Text("مبلغ مبدأ (${fromCurr?.symbol ?: ""})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            CalculatorMiniButton(
                                onClick = { activeConvertCalculatorField = "source_amount" },
                                contentDescription = "ماشین‌حساب مبلغ مبدأ"
                            )
                        }
                    )

                    // Target converted amount field (two-way sync)
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { newTarget ->
                            targetAmountText = newTarget
                            val targetVal = parseLocalizedNumber(newTarget)
                            val amt = parseLocalizedNumber(amountText)
                            if (amt > 0.0 && targetVal > 0.0) {
                                val calculatedRate = targetVal / amt
                                rateText = String.format(Locale.US, "%.4f", calculatedRate).trimEnd('0').trimEnd('.')
                            }
                        },
                        label = { Text("مبلغ تبدیل شده مقصد (${toCurr?.symbol ?: ""})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            CalculatorMiniButton(
                                onClick = { activeConvertCalculatorField = "target_amount" },
                                contentDescription = "ماشین‌حساب مبلغ مقصد"
                            )
                        }
                    )

                    // Exchange rate field (two-way sync)
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { newRate ->
                            rateText = newRate
                            val amt = parseLocalizedNumber(amountText)
                            val r = parseLocalizedNumber(newRate)
                            if (amt > 0.0 && r > 0.0) {
                                val targetVal = amt * r
                                targetAmountText = if (targetVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", targetVal) else String.format(Locale.US, "%.2f", targetVal).trimEnd('0').trimEnd('.')
                            }
                        },
                        label = { Text("نرخ تبادله (۱ ${fromCurr?.code ?: ""} = چند ${toCurr?.code ?: ""}؟)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            CalculatorMiniButton(
                                onClick = { activeConvertCalculatorField = "rate" },
                                contentDescription = "ماشین‌حساب نرخ تبادله"
                            )
                        }
                    )

                    if (parsedAmount > 0.0 && fromCurr != null && toCurr != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEEF2FF)
                        ) {
                            Text(
                                text = "حساب ${person.name} به مقدار \u200E${toCurr.symbol} ${viewModel.formatAmount(convertedTargetAmount)}\u200E ثبت خواهد شد (نرخ: $rateText).",
                                fontSize = 11.sp,
                                color = BentoNavyDark,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (parsedAmount > 0.0 && fromCurr != null && toCurr != null && fromCurr.code != toCurr.code) {
                            viewModel.convertPersonDebtCurrency(
                                recipientId = person.id,
                                personName = person.name,
                                fromCurrency = fromCurr,
                                toCurrency = toCurr,
                                fromAmount = parsedAmount,
                                toAmount = convertedTargetAmount,
                                rate = parsedRate,
                                accountId = targetAccountId,
                                isDebtor = isDebtor,
                                onSuccess = {
                                    showConvertPersonDialog = false
                                    recipientToConvert = null
                                    // Instantly refresh modal sheet state if open
                                    selectedRecipientDetails = recipients.firstOrNull { it.id == person.id } ?: person
                                }
                            )
                        }
                    },
                    enabled = parsedAmount > 0.0 && fromCurr != null && toCurr != null && fromCurr.code != toCurr.code,
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                ) {
                    Text("انجام تبدیل ارز")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConvertPersonDialog = false
                    recipientToConvert = null
                }) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )

        activeConvertCalculatorField?.let { field ->
            val initVal = when (field) {
                "source_amount" -> amountText
                "target_amount" -> targetAmountText
                "rate" -> rateText
                else -> ""
            }
            val title = when (field) {
                "source_amount" -> "محاسبه مبلغ مبدأ"
                "target_amount" -> "محاسبه مبلغ مقصد"
                "rate" -> "محاسبه نرخ تبادله"
                else -> "ماشین‌حساب"
            }

            MinimalCalculatorDialog(
                initialValue = initVal,
                title = title,
                onConfirm = { calcVal ->
                    when (field) {
                        "source_amount" -> {
                            amountText = calcVal
                            val amt = parseLocalizedNumber(calcVal)
                            val r = parseLocalizedNumber(rateText)
                            if (amt > 0.0 && r > 0.0) {
                                val targetVal = amt * r
                                targetAmountText = if (targetVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", targetVal) else String.format(Locale.US, "%.2f", targetVal).trimEnd('0').trimEnd('.')
                            }
                        }
                        "target_amount" -> {
                            targetAmountText = calcVal
                            val targetVal = parseLocalizedNumber(calcVal)
                            val amt = parseLocalizedNumber(amountText)
                            if (amt > 0.0 && targetVal > 0.0) {
                                val calculatedRate = targetVal / amt
                                rateText = String.format(Locale.US, "%.4f", calculatedRate).trimEnd('0').trimEnd('.')
                            }
                        }
                        "rate" -> {
                            rateText = calcVal
                            val amt = parseLocalizedNumber(amountText)
                            val r = parseLocalizedNumber(calcVal)
                            if (amt > 0.0 && r > 0.0) {
                                val targetVal = amt * r
                                targetAmountText = if (targetVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", targetVal) else String.format(Locale.US, "%.2f", targetVal).trimEnd('0').trimEnd('.')
                            }
                        }
                    }
                },
                onDismiss = { activeConvertCalculatorField = null }
            )
        }
    }

    // --- Add or Edit Recipient Dialog (Complete fields: Icon, Color, Notes, Active switch) ---
    if (showAddOrEditDialog) {
        val isEditing = recipientToEdit != null
        var name by remember { mutableStateOf(recipientToEdit?.name ?: "") }
        var handleOrPhone by remember { mutableStateOf(recipientToEdit?.handleOrPhone ?: "") }
        var selectedColorHex by remember { mutableStateOf(recipientToEdit?.avatarColorHex ?: RECIPIENT_PALETTE.first()) }
        var selectedIconName by remember { mutableStateOf(recipientToEdit?.iconName ?: "Person") }
        var isActive by remember { mutableStateOf(recipientToEdit?.isActive ?: true) }
        var isAmountProtected by remember { mutableStateOf(recipientToEdit?.isAmountProtected ?: false) }
        var notes by remember { mutableStateOf(recipientToEdit?.notes ?: "") }

        AlertDialog(
            onDismissRequest = { showAddOrEditDialog = false },
            title = {
                Text(
                    text = if (isEditing) "ویرایش مشخصات شخص" else "افزودن شخص جدید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = BentoNavyDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام و نام خانوادگی (الزامی)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = handleOrPhone,
                        onValueChange = { handleOrPhone = it },
                        label = { Text("شماره تماس / آیدی / شماره کارت") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Icon Selection
                    Text(
                        text = "انتخاب آیکون شخص:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoNavyDark
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(RECIPIENT_ICON_OPTIONS) { (iconKey, iconLabel) ->
                            val isSel = selectedIconName == iconKey
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedIconName = iconKey },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) BentoNavyDark else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getRecipientIconVector(iconKey),
                                        contentDescription = iconLabel,
                                        tint = if (isSel) Color.White else BentoNavyDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = iconLabel,
                                        color = if (isSel) Color.White else BentoNavyDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Color Palette
                    Text(
                        text = "رنگ نشان شخص:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoNavyDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RECIPIENT_PALETTE.forEach { colorVal ->
                            val isSel = selectedColorHex == colorVal
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .clickable { selectedColorHex = colorVal }
                                    .then(
                                        if (isSel) Modifier.border(2.5.dp, BentoNavyDark, CircleShape)
                                        else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Active Status Toggle
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isActive = !isActive }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "شخص فعال است",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (isActive) "در لیست پرداخت و دریافت نشان داده می‌شود"
                                           else "غیرفعال (در لیست پرداخت نشان داده نمی‌شود)",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryTeal
                                )
                            )
                        }
                    }

                    // Amount Protection Toggle
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val willProtect = !isAmountProtected
                                    if (!willProtect) {
                                        if (isEditing && recipientToEdit?.isAmountProtected == true && viewModel.securityManager.hasPasscode()) {
                                            authPromptTitle = "غیرفعال‌سازی محافظت از مبلغ"
                                            authPromptSubtitle = "برای غیرفعال‌سازی محافظت از مبالغ «${recipientToEdit?.name ?: name}»، رمز عبور را وارد کنید"
                                            onAuthSuccessAction = {
                                                isAmountProtected = false
                                            }
                                            recipientToAuthenticate = recipientToEdit
                                        } else {
                                            isAmountProtected = false
                                        }
                                    } else {
                                        if (!viewModel.securityManager.hasPasscode()) {
                                            authPromptTitle = "قفل برنامه فعال نیست"
                                            authPromptSubtitle = "برای محافظت از مبالغ، ابتدا قفل برنامه را در بخش تنظیمات فعال کنید."
                                            onAuthSuccessAction = null
                                            recipientToAuthenticate = recipientToEdit ?: RecipientEntity(name = name, handleOrPhone = handleOrPhone)
                                        } else {
                                            isAmountProtected = true
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "محافظت از مبلغ طلب و بدهی",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }
                                Text(
                                    text = if (isAmountProtected) "مبالغ مخفی (****) و فقط با رمز یا اثر انگشت نمایش داده می‌شوند"
                                           else "مبالغ بدون نیاز به رمز عبور نمایش داده می‌شوند",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isAmountProtected,
                                onCheckedChange = { willProtect ->
                                    if (!willProtect) {
                                        if (isEditing && recipientToEdit?.isAmountProtected == true && viewModel.securityManager.hasPasscode()) {
                                            authPromptTitle = "غیرفعال‌سازی محافظت از مبلغ"
                                            authPromptSubtitle = "برای غیرفعال‌سازی محافظت از مبالغ «${recipientToEdit?.name ?: name}»، رمز عبور را وارد کنید"
                                            onAuthSuccessAction = {
                                                isAmountProtected = false
                                            }
                                            recipientToAuthenticate = recipientToEdit
                                        } else {
                                            isAmountProtected = false
                                        }
                                    } else {
                                        if (!viewModel.securityManager.hasPasscode()) {
                                            authPromptTitle = "قفل برنامه فعال نیست"
                                            authPromptSubtitle = "برای محافظت از مبالغ، ابتدا قفل برنامه را در بخش تنظیمات فعال کنید."
                                            onAuthSuccessAction = null
                                            recipientToAuthenticate = recipientToEdit ?: RecipientEntity(name = name, handleOrPhone = handleOrPhone)
                                        } else {
                                            isAmountProtected = true
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = BentoIndigoAccent
                                )
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("یادداشت یا توضیحات اضافی (اختیاری)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (isEditing && recipientToEdit != null) {
                                val effectiveProtection = if (recipientToEdit!!.isAmountProtected && !isAmountProtected && viewModel.securityManager.hasPasscode() && viewModel.isRecipientMasked(recipientToEdit!!.id)) {
                                    true
                                } else {
                                    isAmountProtected
                                }
                                viewModel.updateRecipient(
                                    recipientToEdit!!.copy(
                                        name = name.trim(),
                                        handleOrPhone = handleOrPhone.trim(),
                                        avatarColorHex = selectedColorHex,
                                        iconName = selectedIconName,
                                        isActive = isActive,
                                        notes = notes.trim(),
                                        isAmountProtected = effectiveProtection
                                    )
                                )
                            } else {
                                viewModel.addRecipient(
                                    name = name.trim(),
                                    handleOrPhone = handleOrPhone.trim(),
                                    avatarColorHex = selectedColorHex,
                                    iconName = selectedIconName,
                                    isActive = isActive,
                                    notes = notes.trim(),
                                    isAmountProtected = isAmountProtected
                                )
                            }
                            showAddOrEditDialog = false
                        }
                    },
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                ) {
                    Text(if (isEditing) "ذخیره تغییرات" else "افزودن شخص")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOrEditDialog = false }) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // --- Delete Confirmation Dialog ---
    if (recipientToDelete != null) {
        val person = recipientToDelete!!
        AlertDialog(
            onDismissRequest = { recipientToDelete = null },
            title = {
                Text("حذف مخاطب", fontWeight = FontWeight.Bold, color = ExpenseRed)
            },
            text = {
                Text(
                    text = "آیا مطمئن هستید که می‌خواهید «${person.name}» را حذف کنید؟ سوابق تراکنش‌های قبلی محفوظ خواهند ماند.",
                    fontSize = 13.sp,
                    color = BentoNavyDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecipient(person)
                        recipientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { recipientToDelete = null }) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    // Operation Error Dialog (Database Integrity & Reference Constraints)
    if (operationError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearOperationError() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "امکان حذف وجود ندارد",
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = operationError!!,
                    fontSize = 13.sp,
                    color = BentoNavyDark,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearOperationError() },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("متوجه شدم")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }

    if (showTransferDialog) {
        LuxuryTransferDialog(
            viewModel = viewModel,
            accounts = accounts,
            recipients = recipients,
            activeCurrencies = activeCurrencies,
            initialTab = TransferTab.PERSON_TO_PERSON,
            lockedTab = TransferTab.PERSON_TO_PERSON,
            preselectedPersonName = transferPreselectedPersonName,
            preselectedPersonId = transferPreselectedPersonId,
            onDismiss = {
                showTransferDialog = false
                transferPreselectedPersonName = null
                transferPreselectedPersonId = null
            }
        )
    }
}

/**
 * Swipeable Card for Recipient:
 * Allows smooth horizontal dragging following the user's finger.
 * Dragging reveals a "پرداخت / دریافت" action. When released past the threshold,
 * it animates off-screen and prepares the payment/calculator screen with this recipient.
 * Also contains a quick 1-tap active/inactive toggle chip directly on the card.
 */
@Composable
private fun SwipeableRecipientCard(
    recipient: RecipientEntity,
    debts: List<RecipientCurrencyDebt> = emptyList(),
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onSwipeToPay: () -> Unit,
    onSwipeToReceive: () -> Unit,
    onAuthenticateRecipient: (RecipientEntity) -> Unit = {},
    onMaskRecipient: (Long) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var cardWidth by remember { mutableFloatStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .onSizeChanged { cardWidth = it.width.toFloat().coerceAtLeast(1f) }
    ) {
        val currentOffset = offsetX.value
        val threshold = cardWidth * 0.28f
        val progress = (kotlin.math.abs(currentOffset) / threshold).coerceIn(0f, 1f)
        val isPastThreshold = kotlin.math.abs(currentOffset) >= threshold
        val isSwipingRight = currentOffset > 0f

        // Action background container revealed behind card: Left swipe = Pay (ExpenseRed/Navy), Right swipe = Receive (IncomeGreen)
        val bgColor = if (isSwipingRight) {
            if (isPastThreshold) IncomeGreen else Color(0xFF15803D).copy(alpha = 0.85f)
        } else {
            if (isPastThreshold) Color(0xFFDC2626) else BentoNavyDark
        }

        Surface(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(18.dp)),
            color = bgColor,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = if (isSwipingRight) Arrangement.Absolute.Left else Arrangement.Absolute.Right,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = progress
                        scaleX = 0.85f + 0.15f * progress
                        scaleY = 0.85f + 0.15f * progress
                    }
                ) {
                    if (isSwipingRight) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "دریافت از شخص",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "دریافت وجه",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "پرداخت وجه",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "پرداخت به شخص",
                            tint = AccentLime,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Foreground Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .absoluteOffset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(recipient.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                // Smoothly follow the user's drag in both directions
                                val newX = (offsetX.value + dragAmount).coerceIn(-cardWidth, cardWidth)
                                offsetX.snapTo(newX)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                if (kotlin.math.abs(offsetX.value) >= threshold) {
                                    val isRight = offsetX.value > 0
                                    val targetX = if (isRight) cardWidth else -cardWidth
                                    offsetX.animateTo(targetX, tween(180))
                                    if (isRight) {
                                        onSwipeToReceive()
                                    } else {
                                        onSwipeToPay()
                                    }
                                    offsetX.snapTo(0f)
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring())
                            }
                        }
                    )
                }
                .clip(RoundedCornerShape(18.dp))
                .clickable { onClick() }
                .testTag("all_recipient_${recipient.id}"),
            shape = RoundedCornerShape(18.dp),
            color = if (recipient.isActive) SurfaceWhite else Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, if (recipient.isActive) BentoBorder else Color(0xFFE2E8F0)),
            shadowElevation = if (recipient.isActive) 0.5.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (recipient.isActive) Color(recipient.avatarColorHex)
                                else Color(recipient.avatarColorHex).copy(alpha = 0.5f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getRecipientIconVector(recipient.iconName),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = recipient.name,
                                color = if (recipient.isActive) TextPrimary else TextTertiary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (recipient.isAmountProtected) {
                                val anyMasked = debts.any { it.isMasked }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (anyMasked) BentoNavyDark.copy(alpha = 0.08f) else AccentLime.copy(alpha = 0.25f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (anyMasked) {
                                                onAuthenticateRecipient(recipient)
                                            } else {
                                                onMaskRecipient(recipient.id)
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (anyMasked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = if (anyMasked) "مبلغ مخفی است" else "مبلغ نمایان است",
                                            tint = if (anyMasked) BentoIndigoAccent else Color(0xFF15803D),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = recipient.handleOrPhone.ifBlank { "بدون شماره" },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (debts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                debts.take(2).forEach { d ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (d.isMasked) Color(0xFFF1F5F9) else if (d.isDebtor) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f),
                                        border = if (d.isMasked) BorderStroke(1.dp, BentoBorder) else null,
                                        modifier = if (d.isMasked) {
                                            Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onAuthenticateRecipient(recipient) }
                                                .testTag("masked_debt_badge_${recipient.id}")
                                        } else Modifier
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = d.statusBadgeText,
                                                color = if (d.isMasked) BentoNavyDark else if (d.isDebtor) IncomeGreen else ExpenseRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (d.isMasked) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "قفل شده - برای نمایش لمس کنید",
                                                    tint = BentoIndigoAccent,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick 1-tap Active/Inactive Toggle Chip (نیازی به ویرایش کردن نباشه)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (recipient.isActive) IncomeGreen.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (recipient.isActive) IncomeGreen.copy(alpha = 0.35f) else BentoBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleActive() }
                            .testTag("toggle_active_${recipient.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (recipient.isActive) IncomeGreen else TextTertiary)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (recipient.isActive) "فعال" else "غیرفعال",
                                color = if (recipient.isActive) IncomeGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "مشاهده وضعیت حساب",
                        tint = TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecipientsBaseCurrencySummaryCard(
    summary: FinanceViewModel.RecipientsBaseCurrencySummary,
    onRequestUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, BentoBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recipients_base_currency_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title & Base Currency Tag
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BentoIndigoAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "جمله کل حسابات اشخاص",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoNavyDark
                        )
                        Text(
                            text = "محاسبه شده به ارز پایه",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BentoIndigoAccent.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.25f))
                ) {
                    Text(
                        text = "ارز پایه: ${summary.baseCurrencyCode} (${summary.baseCurrencySymbol})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoIndigoAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Bento Tiles: جمله طلب (Receivable) and جمله بدهی (Payable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // جمله طلب (Receivable)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = IncomeGreenBg,
                    border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "جمله طلب (طلب ما)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (summary.hasMaskedProtectedRecipients) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "••••••",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "محافظت شده",
                                    tint = IncomeGreen.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "${summary.formattedTotalReceivable} ${summary.baseCurrencySymbol}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = IncomeGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (summary.totalDebtorsCount > 0) "${summary.totalDebtorsCount} نفر بدهکار به ما" else "هیچ طلبی ندارید",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                // جمله بدهی (Payable)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ExpenseRedBg,
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "جمله بدهی (بدهی ما)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (summary.hasMaskedProtectedRecipients) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "••••••",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "محافظت شده",
                                    tint = ExpenseRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "${summary.formattedTotalPayable} ${summary.baseCurrencySymbol}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ExpenseRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (summary.totalCreditorsCount > 0) "${summary.totalCreditorsCount} نفر طلبکار از ما" else "هیچ بدهی ندارید",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Net balance bar (خالص تراز حسابات)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BackgroundCanvas,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNetReceivable = summary.netBalanceInBase > 0.01
                    val isNetPayable = summary.netBalanceInBase < -0.01
                    val netColor = if (isNetReceivable) IncomeGreen else if (isNetPayable) ExpenseRed else BentoNavyDark
                    val netLabel = if (isNetReceivable) {
                        "تراز کل: خالص طلبکار"
                    } else if (isNetPayable) {
                        "تراز کل: خالص بدهکار"
                    } else {
                        "تراز کل: بی‌حساب و تسویه"
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(netColor)
                        )
                        Text(
                            text = netLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    if (summary.hasMaskedProtectedRecipients) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "••••••",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        }
                    } else {
                        Text(
                            text = "${summary.formattedNetBalance} ${summary.baseCurrencySymbol}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = netColor
                        )
                    }
                }
            }

            // If some recipients are masked due to amount protection, show an unmask button
            if (summary.hasMaskedProtectedRecipients) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoIndigoAccent.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.20f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRequestUnlock() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "برخی حساب‌ها محافظت‌شده هستند",
                                fontSize = 12.sp,
                                color = BentoIndigoAccent,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "نمایش با رمز",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoIndigoAccent
                        )
                    }
                }
            }
        }
    }
}
