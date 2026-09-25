package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AccountCardEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.RecipientEntity
import com.example.data.local.TransactionType
import com.example.ui.components.SlideToConfirmButton
import com.example.ui.components.RecipientAuthDialog
import com.example.ui.components.formatCategoryDari
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyCard
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ChipActive
import com.example.ui.theme.ChipInactive
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
import com.example.ui.components.CurrencyPickerBottomSheet
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import com.example.ui.components.SlideToConfirmButton
import com.example.ui.components.SolarDatePickerDialog
import com.example.util.PersianDateHelper
import androidx.compose.runtime.mutableLongStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTransactionScreen(
    viewModel: FinanceViewModel,
    onTransactionCompleted: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isRequestMode by viewModel.isRequestMode.collectAsStateWithLifecycle()
    val calcExpression by viewModel.calcExpression.collectAsStateWithLifecycle()
    val evaluatedAmount by viewModel.evaluatedAmount.collectAsStateWithLifecycle()
    val selectedRecipient by viewModel.selectedRecipient.collectAsStateWithLifecycle()
    val isGeneralSelected by viewModel.isGeneralSelected.collectAsStateWithLifecycle()
    val selectedAccount by viewModel.selectedAccount.collectAsStateWithLifecycle()
    val transactionNote by viewModel.transactionNote.collectAsStateWithLifecycle()
    val selectedCategoryEntity by viewModel.selectedCategoryEntity.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val dbCategories by viewModel.categories.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val recipients by viewModel.recipients.collectAsStateWithLifecycle()
    val accounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val successMessage by viewModel.transactionSuccessMessage.collectAsStateWithLifecycle()
    val activeCurrencies by viewModel.activeCurrencies.collectAsStateWithLifecycle()
    val currencyBalances by viewModel.currencyBalances.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val unmaskedRecipientIds by viewModel.unmaskedRecipientIds.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    val haptic = LocalHapticFeedback.current
    var showCurrencySheet by remember { mutableStateOf(false) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showRecipientAuthDialogForId by remember { mutableStateOf<Long?>(null) }
    var showMoreDetails by remember { mutableStateOf(false) }
    var newPersonName by remember { mutableStateOf("") }
    var newPersonPhone by remember { mutableStateOf("") }
    var showZeroAmountWarning by remember { mutableStateOf(false) }
    var showMandatoryError by remember { mutableStateOf(false) }

    // Isolated transaction currency: Selecting a currency here only affects this single transaction!
    var txnCurrency by remember { mutableStateOf<CurrencyEntity?>(null) }

    LaunchedEffect(activeCurrencies) {
        if (txnCurrency == null && activeCurrencies.isNotEmpty()) {
            txnCurrency = activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull()
        }
    }

    // When entering the payment/calculator screen, reset inputs to zero/default
    // while preserving the selected recipient, mode (payment/receive), and currency
    LaunchedEffect(Unit) {
        viewModel.resetCalculatorInputsOnly()
    }

    val categories = listOf("General", "Shopping", "Food & Dining", "Electronics", "Subscription", "Salary", "Transfer", "Travel", "Bills")
    val isReadyToSubmit = evaluatedAmount > 0.0

    // Fullscreen success view states
    var showSuccessScreen by remember { mutableStateOf(false) }
    var completedAmount by remember { mutableStateOf(0.0) }
    var completedRecipientName by remember { mutableStateOf<String?>(null) }
    var completedIsRequestMode by remember { mutableStateOf(false) }
    var completedCurrencyCode by remember { mutableStateOf("AFN") }
    var completedCurrencySymbol by remember { mutableStateOf("؋") }
    var completedCurrencyName by remember { mutableStateOf("افغانی") }

    val baseCurrency = remember(activeCurrencies) {
        activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull()
    }
    val isNonBaseCurrency = txnCurrency != null && baseCurrency != null &&
            !txnCurrency!!.code.equals(baseCurrency.code, ignoreCase = true)

    var customExchangeRateText by remember(txnCurrency?.code) {
        val r = txnCurrency?.exchangeRateToBase ?: 1.0
        mutableStateOf(if (r > 0.0) String.format(java.util.Locale.US, "%.4f", r).trimEnd('0').trimEnd('.') else "1.0")
    }
    val effectiveExchangeRate = customExchangeRateText.toDoubleOrNull() ?: (txnCurrency?.exchangeRateToBase ?: 1.0)
    val convertedAmountInBase = evaluatedAmount * effectiveExchangeRate

    var selectedRecipientCurrencyCode by remember { mutableStateOf<String?>(null) }
    var recipientExchangeRateText by remember { mutableStateOf("") }
    var recipientLedgerAmountText by remember { mutableStateOf("") }
    var showCalculatorForCustomRate by remember { mutableStateOf(false) }
    var showCalculatorForRecipientRate by remember { mutableStateOf(false) }
    var showCalculatorForRecipientLedger by remember { mutableStateOf(false) }
    var selectedDueDateDays by remember { mutableStateOf<Int?>(null) }
    var customDueDateTimestamp by remember { mutableStateOf<Long?>(null) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var transactionTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showTxnDatePicker by remember { mutableStateOf(false) }

    val budgetStatus = remember(selectedCategoryEntity, selectedCategory, evaluatedAmount, effectiveExchangeRate, isRequestMode, isGeneralSelected, selectedRecipient) {
        // کسری بودجه تنها و منحصراً در مصارف عمومی (نه اشخاص) بررسی شود
        if (isGeneralSelected && selectedRecipient == null && !isRequestMode && (selectedCategoryEntity != null || selectedCategory.isNotBlank())) {
            viewModel.getCategoryBudgetStatus(
                category = selectedCategory,
                pendingAmount = evaluatedAmount,
                pendingExchangeRate = if (isNonBaseCurrency) effectiveExchangeRate else 1.0,
                categoryId = selectedCategoryEntity?.id
            )
        } else null
    }

    var affectsBalance by remember { mutableStateOf(true) }
    val currentAccount = selectedAccount

    val currTxn = txnCurrency
    val currentCurrencyBal = currencyBalances.find { (currTxn != null && currTxn.id > 0 && it.currency.id == currTxn.id) || it.currency.code.equals(currTxn?.code, ignoreCase = true) }?.balance ?: 0.0
    val availableBalance = if (selectedAccount != null) selectedAccount!!.balance else currentCurrencyBal

    // کسری بودجه تنها در مصارف عمومی و بر اساس مجموع پرداخت‌های همان دسته‌بندی چک شود
    val isCategoryBudgetDeficit = isGeneralSelected && selectedRecipient == null && !isRequestMode &&
            budgetStatus != null && (budgetStatus.isOverBudget || budgetStatus.remainingBefore < 0.0)
    val hasBudgetDeficit = isCategoryBudgetDeficit

    fun submitAndFinish() {
        if (evaluatedAmount <= 0.0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showZeroAmountWarning = true
            return
        }

        // Must select either a recipient or general person
        if (selectedRecipient == null && !isGeneralSelected) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showMandatoryError = true
            return
        }

        // For general expenses and incomes (عمومی), Category is mandatory (Account defaults to Total Balance / بیلانس کل)
        if (isGeneralSelected) {
            val isCatMissing = selectedCategory.isBlank()
            if (isCatMissing) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showMandatoryError = true
                return
            }
        }

        showMandatoryError = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        completedAmount = evaluatedAmount
        completedRecipientName = if (isGeneralSelected) null else selectedRecipient?.name
        completedIsRequestMode = isRequestMode
        val tCurr = txnCurrency ?: activeCurrencies.firstOrNull()
        completedCurrencyCode = tCurr?.code ?: "AFN"
        completedCurrencySymbol = tCurr?.symbol ?: "؋"
        completedCurrencyName = when (completedCurrencySymbol) {
            "$" -> "دالر"
            "؋" -> "افغانی"
            "€" -> "یورو"
            "تومان" -> "تومان"
            "₨" -> "کلدار"
            else -> tCurr?.name ?: completedCurrencySymbol
        }
        showSuccessScreen = true

        val isDifferentLedger = selectedRecipient != null && selectedRecipientCurrencyCode != null &&
                !selectedRecipientCurrencyCode.equals(completedCurrencyCode, ignoreCase = true)
        val targetLedgerCurr = if (isDifferentLedger) activeCurrencies.find { it.code.equals(selectedRecipientCurrencyCode, ignoreCase = true) } else null
        val directLedgerAmt = recipientLedgerAmountText.toDoubleOrNull()
        val personRate = recipientExchangeRateText.toDoubleOrNull() ?: 1.0
        val personAmount = if (isDifferentLedger && targetLedgerCurr != null) {
            if (directLedgerAmt != null && directLedgerAmt > 0.0) directLedgerAmt
            else if (personRate > 0) evaluatedAmount * personRate
            else null
        } else null

        val dueDateTimestamp: Long? = if (selectedRecipient != null) {
            customDueDateTimestamp ?: selectedDueDateDays?.let { days ->
                System.currentTimeMillis() + days.toLong() * 24L * 60L * 60L * 1000L
            }
        } else null

        val targetLedgerRate = if (selectedRecipient != null && targetLedgerCurr != null && !targetLedgerCurr.code.equals(completedCurrencyCode, ignoreCase = true)) {
            val rVal = recipientExchangeRateText.toDoubleOrNull()
            if (rVal != null && rVal > 0.0) rVal
            else if (evaluatedAmount > 0.0 && personAmount != null && personAmount > 0.0) (personAmount / evaluatedAmount)
            else 1.0
        } else {
            if (isNonBaseCurrency) effectiveExchangeRate else 1.0
        }

        // Submit transaction with isolated currency, exchange rate, custom date, affectsBalance, and optional due date
        viewModel.submitTransaction(
            overrideCurrencyCode = completedCurrencyCode,
            overrideCurrencySymbol = completedCurrencySymbol,
            exchangeRate = targetLedgerRate,
            recipientCurrencyCode = targetLedgerCurr?.code,
            recipientCurrencySymbol = targetLedgerCurr?.symbol,
            recipientAmount = personAmount,
            dueDate = dueDateTimestamp,
            customTimestamp = transactionTimestamp,
            affectsBalance = affectsBalance
        )
        selectedDueDateDays = null
        customDueDateTimestamp = null
        transactionTimestamp = System.currentTimeMillis()
    }

    LaunchedEffect(showZeroAmountWarning) {
        if (showZeroAmountWarning) {
            delay(2500)
            showZeroAmountWarning = false
        }
    }

    LaunchedEffect(showMandatoryError) {
        if (showMandatoryError) {
            delay(3500)
            showMandatoryError = false
        }
    }

    // When success screen is displayed, auto-navigate to Home quickly (reduced display time as requested)
    LaunchedEffect(showSuccessScreen) {
        if (showSuccessScreen) {
            delay(1100)
            viewModel.clearSuccessMessage()
            showSuccessScreen = false
            onTransactionCompleted()
        }
    }

    // Full-screen view: either the Fullscreen Success Animation or the Transaction/Calculator screen
    AnimatedContent(
        targetState = showSuccessScreen,
        transitionSpec = {
            (fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.92f, animationSpec = tween(280)))
                .togetherWith(fadeOut(animationSpec = tween(200)))
        },
        label = "screen_transition"
    ) { isSuccess ->
        if (isSuccess) {
            // --- FULLSCREEN SUCCESS CONFIRMATION SCREEN ---
            val formattedFinalAmount = viewModel.formatAmount(completedAmount, completedCurrencyCode)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundCanvas)
                    .clickable {
                        viewModel.clearSuccessMessage()
                        showSuccessScreen = false
                        onTransactionCompleted()
                    }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Big Green Checkmark with smooth pop animation
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                            .shadow(12.dp, CircleShape, spotColor = IncomeGreen.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Clean amount display with transaction's actual currency
                    Text(
                        text = "$completedCurrencySymbol $formattedFinalAmount",
                        color = BentoNavyDark,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Persian literary confirmation message using actual transaction currency
                    val confirmationMessage = if (!completedRecipientName.isNullOrBlank()) {
                        if (completedIsRequestMode) {
                            "مقدار $formattedFinalAmount $completedCurrencyName با موفقیت از $completedRecipientName دریافت گردید."
                        } else {
                            "مقدار $formattedFinalAmount $completedCurrencyName با موفقیت به $completedRecipientName پرداخت شد."
                        }
                    } else {
                        if (completedIsRequestMode) {
                            "مقدار $formattedFinalAmount $completedCurrencyName عاید شد."
                        } else {
                            "مقدار $formattedFinalAmount $completedCurrencyName مصرف شد."
                        }
                    }

                    Text(
                        text = confirmationMessage,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "در حال انتقال به صفحه اصلی...",
                        color = TextTertiary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- MAIN CALCULATOR & TRANSACTION SCREEN WITH SLIDE-TO-CONFIRM GESTURE ---
            // Wrap in LTR coordinate space so positive offset is physically to the RIGHT and negative is physically to the LEFT
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                BoxWithConstraints(
                    modifier = modifier
                        .fillMaxSize()
                        .background(BackgroundCanvas)
                        .testTag("calculator_screen")
                ) {
                    val screenWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                    val halfWidthPx = screenWidthPx * 0.45f
                    val backThresholdPx = screenWidthPx * 0.30f
                    val dragOffset = remember { Animatable(0f) }
                    val coroutineScope = rememberCoroutineScope()
                    var isSubmitting by remember { mutableStateOf(false) }
                    var accumRightDrag by remember { mutableFloatStateOf(0f) }
                    var isDetailsExpanded by remember { mutableStateOf(false) }
                    var showSwipeHint by remember { mutableStateOf(false) }

                    // شرط نمایش متن راهنما: فقط وقتی که اطلاعات تراکنش (مخاطب) پر شده باشد و مبلغ معتبر وارد شده باشد، پس از چند ثانیه ظاهر شود
                    val isFormFilled = (isGeneralSelected || selectedRecipient != null) && evaluatedAmount > 0.0
                    LaunchedEffect(isFormFilled) {
                        if (isFormFilled) {
                            showSwipeHint = false
                            kotlinx.coroutines.delay(1800)
                            showSwipeHint = true
                        } else {
                            showSwipeHint = false
                        }
                    }

                    val currentDrag = dragOffset.value
                    val isSwipingToSave = currentDrag < 0f // Dragging to physical left
                    val progress = (-currentDrag / halfWidthPx).coerceIn(0f, 1f)

                    val swipeValidationError: String? = when {
                        evaluatedAmount <= 0.0 -> "مبلغ را وارد نکرده‌اید!"
                        selectedRecipient == null && !isGeneralSelected -> "طرف حساب انتخاب نشده!"
                        isGeneralSelected && selectedCategory.isBlank() -> "دسته‌بندی انتخاب نشده!"
                        else -> null
                    }
                    val swipeValidationSublabel: String? = when {
                        evaluatedAmount <= 0.0 -> "ابتدا مبلغ تراکنش را وارد نمایید"
                        selectedRecipient == null && !isGeneralSelected -> "شخص طرف حساب یا مصرف عمومی را مشخص کنید"
                        isGeneralSelected && selectedCategory.isBlank() -> "انتخاب دسته‌بندی برای عاید/مصرف الزامی است"
                        else -> null
                    }
                    val hasSwipeError = swipeValidationError != null

                    // Reveal: Save Confirmation or Error on the physical RIGHT (when dragged to the left) with Background AND Text
                    if (isSwipingToSave && progress > 0.01f) {
                        val revealGradient = when {
                            evaluatedAmount <= 0.0 -> Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF9A3412),
                                    Color(0xFFC2410C),
                                    Color(0xFFEA580C)
                                )
                            )
                            selectedRecipient == null && !isGeneralSelected -> Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF7F1D1D),
                                    Color(0xFF991B1B),
                                    Color(0xFFDC2626)
                                )
                            )
                            isGeneralSelected && selectedCategory.isBlank() -> Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF831843),
                                    Color(0xFF9F1239),
                                    Color(0xFFE11D48)
                                )
                            )
                            isRequestMode -> Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF047857),
                                    Color(0xFF059669),
                                    IncomeGreen
                                )
                            )
                            else -> Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0B1329),
                                    BentoNavyDark,
                                    BentoIndigoAccent
                                )
                            )
                        }

                        val revealTitle = when {
                            hasSwipeError -> if (progress >= 1f) "امکان ثبت نیست ✕" else swipeValidationError!!
                            progress >= 1f -> "رها کنید برای ثبت"
                            else -> "ثبت تراکنش"
                        }
                        val revealSublabel = when {
                            hasSwipeError -> swipeValidationSublabel ?: ""
                            progress >= 1f -> "تأیید و ذخیره شد ✓"
                            else -> "برای ثبت نهایی به چپ بکشید"
                        }
                        val revealIcon = when {
                            evaluatedAmount <= 0.0 -> Icons.Default.Warning
                            hasSwipeError -> Icons.Default.Close
                            else -> Icons.Default.Check
                        }
                        val revealIconTint = when {
                            evaluatedAmount <= 0.0 -> Color(0xFFEA580C)
                            hasSwipeError -> ExpenseRed
                            progress >= 1f -> if (isRequestMode) IncomeGreen else BentoIndigoAccent
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(brush = revealGradient)
                                .padding(24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = revealTitle,
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = revealSublabel,
                                        color = Color.White.copy(alpha = 0.88f),
                                        fontSize = 11.5.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size((52 + 16 * progress).dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f + 0.75f * progress))
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = revealIcon,
                                        contentDescription = if (hasSwipeError) "خطای ثبت" else "ثبت تراکنش",
                                        tint = revealIconTint,
                                        modifier = Modifier.size((26 + 10 * progress).dp)
                                    )
                                }
                            }
                        }
                    }

                    // Foreground Sliding Screen Container - Strictly fixed to the right, can ONLY slide left
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                            .graphicsLayer {
                                shape = RoundedCornerShape((progress * 20).dp)
                                clip = true
                                shadowElevation = (progress * 16).dp.toPx()
                            }
                            .background(BackgroundCanvas)
                            .pointerInput(evaluatedAmount, selectedRecipient, isGeneralSelected, selectedCategory, currentAccount, isSubmitting) {
                                detectHorizontalDragGestures(
                                    onDragStart = { accumRightDrag = 0f },
                                    onDragEnd = {
                                        if (isSubmitting) return@detectHorizontalDragGestures
                                        if (accumRightDrag > 70f) {
                                            onTransactionCompleted()
                                            return@detectHorizontalDragGestures
                                        }
                                        accumRightDrag = 0f
                                        coroutineScope.launch {
                                            val offsetVal = dragOffset.value
                                            if (-offsetVal >= halfWidthPx) {
                                                // Swipe Right to Left: Reached half screen! Save transaction
                                                if (evaluatedAmount <= 0.0) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    showZeroAmountWarning = true
                                                    dragOffset.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                                } else if (selectedRecipient == null && !isGeneralSelected) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    showMandatoryError = true
                                                    dragOffset.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                                } else if (isGeneralSelected && selectedCategory.isBlank()) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    showMandatoryError = true
                                                    dragOffset.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 400f))
                                                } else {
                                                    isSubmitting = true
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    dragOffset.animateTo(-screenWidthPx, tween(220))
                                                    submitAndFinish()
                                                }
                                            } else {
                                                // Released before threshold: spring back to 0
                                                dragOffset.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 450f))
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        accumRightDrag = 0f
                                        coroutineScope.launch {
                                            dragOffset.animateTo(0f, spring())
                                        }
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        if (isSubmitting) return@detectHorizontalDragGestures
                                        // If at resting position (0f) and dragging to the physical right: navigate back to Home
                                        if (dragOffset.value >= 0f && dragAmount > 0f) {
                                            accumRightDrag += dragAmount
                                            if (accumRightDrag > 70f) {
                                                onTransactionCompleted()
                                            }
                                        } else {
                                            accumRightDrag = 0f
                                            coroutineScope.launch {
                                                // Bidirectional finger tracking: dragging left pulls open the save action,
                                                // dragging back right smoothly follows finger back to 0 without saving!
                                                val newOffset = (dragOffset.value + dragAmount).coerceIn(-screenWidthPx, 0f)
                                                val wasPastHalf = -dragOffset.value >= halfWidthPx
                                                val isNowPastHalf = -newOffset >= halfWidthPx
                                                dragOffset.snapTo(newOffset)
                                                if (isNowPastHalf && !wasPastHalf) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .navigationBarsPadding()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // --- Top Bar: Back Arrow & Centered Mode Toggle ---
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(
                                            onClick = onTransactionCompleted,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(SurfaceWhite)
                                                .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                                .testTag("calc_back_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "بازگشت به صفحه اصلی",
                                                tint = BentoNavyDark,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Segmented Mode Control: پرداخت | دریافت
                                        Surface(
                                            modifier = Modifier.testTag("mode_toggle_pill"),
                                            shape = RoundedCornerShape(22.dp),
                                            color = SurfaceWhite,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                ModePill(
                                                    label = if (isGeneralSelected) "مصرف" else "پرداخت",
                                                    isSelected = !isRequestMode,
                                                    onClick = { viewModel.setMode(false) }
                                                )
                                                ModePill(
                                                    label = if (isGeneralSelected) "عاید" else "دریافت",
                                                    isSelected = isRequestMode,
                                                    onClick = { viewModel.setMode(true) }
                                                )
                                            }
                                        }

                                        // Empty Box to keep ModePill centered without top details button
                                        Box(modifier = Modifier.size(40.dp))
                                    }

                                    var personSectionHeightPx by remember { mutableIntStateOf(0) }
                                    val isAmountSticky by remember {
                                        derivedStateOf {
                                            personSectionHeightPx > 0 && scrollState.value >= (personSectionHeightPx - 8)
                                        }
                                    }

                                    // Container holding scrollable content and sticky amount header
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    ) {
                                        // Scrollable Content (Person Selector, Sticky Amount Card, Inline Details)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(scrollState)
                                                .padding(horizontal = 20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {

        // --- Person Selector (گرد و وسط‌چین، ترانزیشن روان، خروج از انتخاب با کلیک مجدد) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .onSizeChanged { personSectionHeightPx = it.height }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when {
                            isGeneralSelected -> if (isRequestMode) "عاید" else "مصرف"
                            selectedRecipient != null -> if (isRequestMode) "دریافت از طرف حساب" else "پرداخت به طرف حساب"
                            else -> if (isRequestMode) "دریافت" else "پرداخت"
                        },
                        color = BentoNavyDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (selectedRecipient == null && !isGeneralSelected) {
                    TextButton(
                        onClick = { showAddPersonDialog = true },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = BentoIndigoAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "شخص جدید",
                            fontSize = 12.sp,
                            color = BentoIndigoAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            val hasSelection = selectedRecipient != null || isGeneralSelected
            val carouselListState = rememberLazyListState()

            // Smooth scroll to selected item so it is prominently visible
            LaunchedEffect(selectedRecipient) {
                val currentSelected = selectedRecipient
                if (currentSelected != null) {
                    val activeList = recipients.filter { it.isActive }
                    val idx = activeList.indexOfFirst { it.id == currentSelected.id }
                    if (idx >= 0) {
                        carouselListState.animateScrollToItem(idx + 1)
                    }
                }
            }
            LaunchedEffect(isGeneralSelected) {
                if (isGeneralSelected) {
                    carouselListState.animateScrollToItem(0)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    state = carouselListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipients_horizontal_carousel"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    // Item 1: عاید / مصرف (ثبت عمومی)
                    item(key = "general_item") {
                        val isThisSelected = isGeneralSelected
                        val isOtherSelected = hasSelection && !isThisSelected

                        val cardScale by animateFloatAsState(
                            targetValue = when {
                                isThisSelected -> 1.03f
                                isOtherSelected -> 0.86f
                                else -> 1.0f
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "general_card_scale"
                        )
                        val cardAlpha by animateFloatAsState(
                            targetValue = when {
                                isThisSelected -> 1.0f
                                isOtherSelected -> 0.35f
                                else -> 1.0f
                            },
                            animationSpec = tween(durationMillis = 220),
                            label = "general_card_alpha"
                        )

                        Surface(
                            modifier = Modifier
                                .width(98.dp)
                                .height(106.dp)
                                .graphicsLayer {
                                    scaleX = cardScale
                                    scaleY = cardScale
                                    alpha = cardAlpha
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(
                                        if (isThisSelected) 1.8.dp else 1.dp,
                                        if (isThisSelected) (if (isRequestMode) IncomeGreen else ExpenseRed) else BentoBorder
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !isOtherSelected) {
                                    if (isThisSelected) {
                                        viewModel.selectGeneral(false)
                                    } else {
                                        viewModel.selectGeneral(true)
                                    }
                                }
                                .testTag("general_expense_item"),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            shadowElevation = if (isThisSelected) 3.dp else 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isRequestMode) IncomeGreenBg else ExpenseRedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRequestMode) Icons.Default.Payments else Icons.Default.ShoppingBag,
                                        contentDescription = if (isRequestMode) "عاید" else "مصرف",
                                        tint = if (isRequestMode) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = if (isRequestMode) "عاید" else "مصرف",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isThisSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = BentoNavyDark,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    if (isThisSelected) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(if (isRequestMode) IncomeGreen else ExpenseRed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "انتخاب شده",
                                                tint = Color.White,
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isThisSelected) (if (isRequestMode) IncomeGreenBg else ExpenseRedBg) else Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isThisSelected) "لغو با لمس" else if (isRequestMode) "عاید" else "مصرف",
                                        fontSize = 8.5.sp,
                                        fontWeight = if (isThisSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isThisSelected) (if (isRequestMode) IncomeGreen else ExpenseRed) else TextSecondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Recipients items (ONLY active recipients shown)
                    val activeRecipients = recipients.filter { it.isActive }
                    items(activeRecipients, key = { it.id }) { recipient ->
                        val isThisSelected = selectedRecipient?.id == recipient.id
                        val isOtherSelected = hasSelection && !isThisSelected

                        val cardScale by animateFloatAsState(
                            targetValue = when {
                                isThisSelected -> 1.03f
                                isOtherSelected -> 0.86f
                                else -> 1.0f
                            },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "recipient_card_scale_${recipient.id}"
                        )
                        val cardAlpha by animateFloatAsState(
                            targetValue = when {
                                isThisSelected -> 1.0f
                                isOtherSelected -> 0.35f
                                else -> 1.0f
                            },
                            animationSpec = tween(durationMillis = 220),
                            label = "recipient_card_alpha_${recipient.id}"
                        )

                        val currCode = txnCurrency?.code ?: "AFN"
                        val debtInfo = viewModel.getRecipientDebtForCurrency(recipient.id, currCode)

                        val statusBadgeText = debtInfo.statusText
                        val statusColor = when {
                            debtInfo.isSettled -> TextSecondary
                            debtInfo.isDebtor -> IncomeGreen
                            else -> ExpenseRed
                        }

                        Surface(
                            modifier = Modifier
                                .width(98.dp)
                                .height(106.dp)
                                .graphicsLayer {
                                    scaleX = cardScale
                                    scaleY = cardScale
                                    alpha = cardAlpha
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(
                                        if (isThisSelected) 1.8.dp else 1.dp,
                                        if (isThisSelected) BentoIndigoAccent else BentoBorder
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !isOtherSelected) {
                                    if (isThisSelected) {
                                        viewModel.selectRecipient(null)
                                        selectedRecipientCurrencyCode = null
                                        recipientExchangeRateText = ""
                                        selectedDueDateDays = null
                                        customDueDateTimestamp = null
                                    } else {
                                        viewModel.selectRecipient(recipient)
                                        selectedRecipientCurrencyCode = null
                                        recipientExchangeRateText = ""
                                    }
                                }
                                .testTag("recipient_pill_${recipient.id}"),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            shadowElevation = if (isThisSelected) 3.dp else 0.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(recipient.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = recipient.name.take(1),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = recipient.name,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isThisSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isThisSelected) BentoIndigoAccent else BentoNavyDark,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    if (isThisSelected) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(BentoIndigoAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "انتخاب شده",
                                                tint = Color.White,
                                                modifier = Modifier.size(9.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        isThisSelected -> BentoIndigoAccent.copy(alpha = 0.12f)
                                        debtInfo.isSettled -> Color(0xFFF1F5F9)
                                        debtInfo.isDebtor -> IncomeGreen.copy(alpha = 0.12f)
                                        else -> ExpenseRed.copy(alpha = 0.12f)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (isThisSelected) "لغو با لمس" else statusBadgeText,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isThisSelected) BentoIndigoAccent else statusColor,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected recipient details bar with quick cancel
                AnimatedVisibility(
                    visible = selectedRecipient != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    selectedRecipient?.let { recipient ->
                        val currCode = txnCurrency?.code ?: "AFN"
                        val debtInfo = viewModel.getRecipientDebtForCurrency(recipient.id, currCode)
                        val currSymbol = txnCurrency?.symbol ?: currCode
                        val statusColor = when {
                            debtInfo.isSettled -> TextSecondary
                            debtInfo.isDebtor -> IncomeGreen
                            else -> ExpenseRed
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = BentoIndigoAccent.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "طرف حساب: ${recipient.name}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = if (debtInfo.isMasked) {
                                                Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { showRecipientAuthDialogForId = recipient.id }
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            } else Modifier
                                        ) {
                                            Text(
                                                text = when {
                                                    debtInfo.isMasked -> if (debtInfo.isDebtor) "(دریافتنی: ••••••)" else "(پرداختنی: ••••••)"
                                                    debtInfo.isSettled -> "(تسویه)"
                                                    debtInfo.isDebtor -> "(دریافتنی: $currSymbol ${viewModel.formatAmount(kotlin.math.abs(debtInfo.netAmount), currCode)})"
                                                    else -> "(پرداختنی: $currSymbol ${viewModel.formatAmount(kotlin.math.abs(debtInfo.netAmount), currCode)})"
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            )
                                            if (debtInfo.isMasked) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "مبلغ مخفی شده است - برای نمایش لمس کنید",
                                                    tint = BentoIndigoAccent,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.selectRecipient(null)
                                        selectedRecipientCurrencyCode = null
                                        recipientExchangeRateText = ""
                                        selectedDueDateDays = null
                                        customDueDateTimestamp = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(
                                        text = "✕ لغو انتخاب",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected general details bar with quick cancel
                AnimatedVisibility(
                    visible = isGeneralSelected,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRequestMode) IncomeGreen.copy(alpha = 0.08f) else ExpenseRed.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isRequestMode) IncomeGreen.copy(alpha = 0.25f) else ExpenseRed.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isRequestMode) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isRequestMode) "ثبت عاید" else "ثبت مصرف",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                            }

                            TextButton(
                                onClick = { viewModel.selectGeneral(false) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "✕ لغو انتخاب",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- Main Amount Display with Arithmetic Formula Preview (Sticky & Currency on Left) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isAmountSticky) 0f else 1f)
                .clip(RoundedCornerShape(28.dp))
                .border(
                    width = if (hasBudgetDeficit) 2.dp else 1.dp,
                    color = if (hasBudgetDeficit) ExpenseRed else BentoBorder,
                    shape = RoundedCornerShape(28.dp)
                )
                .testTag("amount_display_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (hasBudgetDeficit) Color(0xFFFFF1F2) else SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Deficit Alert Pill directly on top of amount
                if (hasBudgetDeficit) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ExpenseRed,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCategoryBudgetDeficit) "⚠️ کسری بودجه در دسته‌بندی!" else "⚠️ کسری موجودی حساب!",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Live arithmetic formula line
                if (calcExpression.contains("+") || calcExpression.contains("-") ||
                    calcExpression.contains("×") || calcExpression.contains("*") ||
                    calcExpression.contains("÷") || calcExpression.contains("/")
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "$calcExpression =",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Main Amount Number with Currency on the Left (Ltr layout guarantees currency on left)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Clickable Currency Badge (سمت چپ مقدار)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                            .clickable { showCurrencySheet = true }
                            .testTag("currency_selector_trigger"),
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = txnCurrency?.symbol ?: currencySymbol,
                                fontSize = 22.sp,
                                color = BentoNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "انتخاب واحد پولی",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Amount Value (سمت راست ارز)
                    Text(
                        text = viewModel.formatCalculatorDisplay(calcExpression, evaluatedAmount, txnCurrency?.code ?: selectedCurrency?.code),
                        fontSize = 42.sp,
                        color = if (hasBudgetDeficit) ExpenseRed else BentoNavyDark,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

                // Available Wallet Balance Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFD))
                        .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = BentoIndigoAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val currForBal = txnCurrency
                    val currentCurrencyBal = currencyBalances.find { (currForBal != null && currForBal.id > 0 && it.currency.id == currForBal.id) || it.currency.code.equals(currForBal?.code, ignoreCase = true) }?.balance ?: 0.0
                    val balAccountName = if (selectedAccount != null) "${selectedAccount!!.name}: " else "حساب نقدی (${txnCurrency?.name ?: ""}): "
                    val balSym = if (selectedAccount != null) selectedAccount!!.currencySymbol else (txnCurrency?.symbol ?: currencySymbol)
                    val balAmount = if (selectedAccount != null) viewModel.formatAmount(selectedAccount!!.balance, selectedAccount!!.currencyCode) else viewModel.formatAmount(currentCurrencyBal, txnCurrency?.code)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = balAccountName,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "$balSym $balAmount",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // --- نرخ ارز لحظه‌ای (در صورت انتخاب ارزی غیر از ارز پایه) ---
                if (isNonBaseCurrency && txnCurrency != null && baseCurrency != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "نرخ تبادله به ${baseCurrency.name}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "۱ ${txnCurrency!!.symbol} =",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SurfaceWhite,
                                        border = BorderStroke(1.dp, BentoBorder)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                androidx.compose.foundation.text.BasicTextField(
                                                    value = customExchangeRateText,
                                                    onValueChange = { customExchangeRateText = it },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                    singleLine = true,
                                                    textStyle = TextStyle(
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoNavyDark,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    modifier = Modifier
                                                        .width(56.dp)
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        .testTag("exchange_rate_input_field")
                                                )
                                            }
                                            CalculatorMiniButton(
                                                onClick = { showCalculatorForCustomRate = true },
                                                contentDescription = "ماشین‌حساب نرخ تبدیل"
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = baseCurrency.symbol,
                                        fontSize = 11.sp,
                                        color = BentoNavyDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (evaluatedAmount > 0.0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "معادل در ارز پایه:",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(
                                            text = "${baseCurrency.symbol} ${viewModel.formatAmount(convertedAmountInBase, baseCurrency.code)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- هشدار و جزئیات کسری بودجه (منحصراً برای مصارف عمومی با توجه به سقف بودجه دسته‌بندی) ---
                if (isCategoryBudgetDeficit && budgetStatus != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFFE4E6),
                        border = BorderStroke(1.2.dp, ExpenseRed)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "کسری بودجه",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "کسری بودجه: ${formatCategoryDari(selectedCategory)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                                val deficitAmount = kotlin.math.abs(budgetStatus.remainingAfter)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ExpenseRed
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "کسری: ",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "$currencySymbol ${viewModel.formatAmount(deficitAmount)}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "سقف بودجه ماهانه: ",
                                        fontSize = 11.sp,
                                        color = BentoNavyDark
                                    )
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(
                                            text = "$currencySymbol ${viewModel.formatAmount(budgetStatus.monthlyLimit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "مجموع با این معامله: ",
                                        fontSize = 11.sp,
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(
                                            text = "$currencySymbol ${viewModel.formatAmount(budgetStatus.currentSpent + budgetStatus.pendingAmount)}",
                                            fontSize = 11.sp,
                                            color = ExpenseRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (budgetStatus.remainingBefore <= 0.0)
                                    "این دسته‌بندی از پیش سقف بودجه را رد کرده بود و وارد کردن این مبلغ کسری بودجه را افزایش می‌دهد."
                                else
                                    "با ثبت این مبلغ، مجموع مصارف این ماه از سقف بودجه تعیین‌شده بیشتر شده و کسری بودجه ایجاد می‌شود.",
                                fontSize = 10.5.sp,
                                color = ExpenseRed.copy(alpha = 0.95f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mandatory Error Banner if user tries to submit without picking Account or Category in General Mode
        AnimatedVisibility(
            visible = showMandatoryError,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = ExpenseRed.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, ExpenseRed)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedRecipient == null && !isGeneralSelected) {
                            "لطفاً ابتدا شخص طرف حساب یا «عاید / مصرف» را انتخاب نمایید!"
                        } else {
                            "برای ثبت مصارف و عواید عمومی، انتخاب حساب و دسته‌بندی الزامی است!"
                        },
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Spacer(modifier = Modifier.height(2.dp))

        // --- بخش جزئیات پرداخت و حساب ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, if (isDetailsExpanded) BentoIndigoAccent else BentoBorder, RoundedCornerShape(18.dp))
                .clickable { isDetailsExpanded = !isDetailsExpanded }
                .testTag("inline_details_toggle"),
            shape = RoundedCornerShape(18.dp),
            color = if (isDetailsExpanded) Color(0xFFF8FAFD) else SurfaceWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BentoIndigoAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "جزئیات پرداخت و دریافت",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BentoNavyDark
                        )
                        Text(
                            text = if (affectsBalance) {
                                "${selectedAccount?.name ?: "موجودی کل"} • تاثیر در موجودی: فعال"
                            } else {
                                "بدون تأثیر در موجودی (صرفاً ثبت در سوابق)"
                            },
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDetailsExpanded) "بستن جزئیات" else "نمایش تنظیمات",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoIndigoAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = BentoIndigoAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Inline Expandable Details Body
        AnimatedVisibility(
            visible = isDetailsExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ۱. تنظیم تاثیر در موجودی کل (بیلانس)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BentoBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceWhite
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { affectsBalance = !affectsBalance }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (affectsBalance) IncomeGreen.copy(alpha = 0.12f) else TextSecondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (affectsBalance) Icons.Default.AccountBalanceWallet else Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (affectsBalance) IncomeGreen else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تاثیر در موجودی",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                Text(
                                    text = if (affectsBalance) "این معامله به موجودی اضافه یا از آن کسر می‌شود" else "بدون تغییر در موجودی نقد (صرفاً ثبت در سوابق / گذشته)",
                                    fontSize = 10.sp,
                                    color = if (affectsBalance) IncomeGreen else TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = affectsBalance,
                            onCheckedChange = { affectsBalance = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IncomeGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }

                // ۲. انتخاب حساب و کارت‌ها (فقط در صورت تاثیر در موجودی)
                if (affectsBalance) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BentoBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRequestMode) "واریز به حساب / کیف پول:" else "برداشت از حساب / کیف پول:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                if (selectedAccount != null) {
                                    Text(
                                        text = selectedAccount!!.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoIndigoAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val currencyCode = txnCurrency?.code ?: "AFN"
                            val filteredAccounts = accounts.filter { it.currencyCode.equals(currencyCode, ignoreCase = true) }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // گزینه ۱: حساب نقدی (کیف پول) - بدون نیاز به تکرار موجودی
                                item {
                                    val isTotalSelected = (selectedAccount == null)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isTotalSelected) BentoNavyDark else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isTotalSelected) BentoNavyDark else BentoBorder, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.selectAccount(null) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = if (isTotalSelected) Color.White else BentoIndigoAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "حساب نقدی",
                                                color = if (isTotalSelected) Color.White else BentoNavyDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isTotalSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }

                                // بقیه کارت‌ها و حساب‌های مربوط به همین ارز
                                items(filteredAccounts) { acc ->
                                    val isSel = (selectedAccount?.id == acc.id)
                                    CardItemChip(
                                        account = acc,
                                        isSelected = isSel,
                                        onClick = { viewModel.selectAccount(acc) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ۳. تاریخ و ساعت معامله (بدون متن اضافی طبق تقویم)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, BentoBorder, RoundedCornerShape(16.dp))
                        .clickable { showTxnDatePicker = true }
                        .testTag("transaction_date_picker_trigger"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFD)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(BentoIndigoAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "تاریخ معامله",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تاریخ و زمان معامله",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = PersianDateHelper.formatSolarDateTime(transactionTimestamp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "انتخاب تاریخ",
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // ۴. جزئیات بسته به حالت عمومی یا شخص
                if (isGeneralSelected) {
                    // انتخاب دسته‌بندی هزینه یا عاید
                    val currentType = if (isRequestMode) TransactionType.INCOME else TransactionType.EXPENSE
                    val filteredCategories = dbCategories.filter { it.isActive && it.type == currentType }
                        .ifEmpty { dbCategories.filter { it.isActive } }
                    val isCatSelected = selectedCategory.isNotBlank() && selectedCategory != "General"

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (showMandatoryError && !isCatSelected) 1.5.dp else 1.dp,
                                color = if (showMandatoryError && !isCatSelected) ExpenseRed else BentoBorder,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRequestMode) "دسته‌بندی منبع درآمد (الزامی):" else "دسته‌بندی هزینه (الزامی):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showMandatoryError && !isCatSelected) ExpenseRed else BentoNavyDark
                                )
                                if (isCatSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = (if (isRequestMode) IncomeGreen else ExpenseRed).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = formatCategoryDari(selectedCategory),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRequestMode) IncomeGreen else ExpenseRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredCategories) { cat ->
                                    val isSel = selectedCategoryEntity?.id == cat.id || (selectedCategoryEntity == null && selectedCategory == cat.name)
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                1.dp,
                                                if (isSel) (if (isRequestMode) IncomeGreen else ExpenseRed) else BentoBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.selectCategory(cat)
                                                showMandatoryError = false
                                            },
                                        color = if (isSel) (if (isRequestMode) IncomeGreen else ExpenseRed).copy(alpha = 0.12f) else SurfaceWhite
                                    ) {
                                        Text(
                                            text = formatCategoryDari(cat.name),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                            color = if (isSel) (if (isRequestMode) IncomeGreen else ExpenseRed) else BentoNavyDark,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // یادداشت اختیاری
                    OutlinedTextField(
                        value = transactionNote,
                        onValueChange = { viewModel.setNote(it) },
                        placeholder = { Text("یادداشت اختیاری برای این معامله...", fontSize = 11.sp, color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedContainerColor = Color(0xFFF8FAFD),
                            unfocusedContainerColor = Color(0xFFF8FAFD),
                            focusedBorderColor = BentoIndigoAccent,
                            unfocusedBorderColor = BentoBorder
                        )
                    )
                } else if (selectedRecipient != null) {
                    // جزئیات سررسید و ارز برای شخص طرف حساب
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, BentoBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isRequestMode) "جزئیات دریافت از ${selectedRecipient!!.name}" else "جزئیات پرداخت برای ${selectedRecipient!!.name}",
                                color = BentoNavyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // موعد سررسید
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "موعد سررسید تسویه حساب:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )
                                if (customDueDateTimestamp != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BentoIndigoAccent.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = PersianDateHelper.formatSolarDate(customDueDateTimestamp!!),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else if (selectedDueDateDays != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = BentoIndigoAccent.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "$selectedDueDateDays روزه",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoIndigoAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "بدون سررسید",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            val dueDateOptions = listOf(
                                null to "بدون سررسید",
                                3 to "۳ روزه",
                                5 to "۵ روزه",
                                7 to "۷ روزه",
                                15 to "۱۵ روزه",
                                30 to "۳۰ روزه"
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(dueDateOptions) { (days, label) ->
                                    val isSel = customDueDateTimestamp == null && selectedDueDateDays == days
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) BentoIndigoAccent else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isSel) BentoIndigoAccent else BentoBorder, RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedDueDateDays = days
                                                customDueDateTimestamp = null
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSel) Color.White else BentoNavyDark,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }

                                item {
                                    val isCustom = customDueDateTimestamp != null
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCustom) BentoIndigoAccent else Color(0xFFEEF2FF))
                                            .border(1.dp, BentoIndigoAccent, RoundedCornerShape(10.dp))
                                            .clickable { showDueDatePicker = true }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = if (isCustom) Color.White else BentoIndigoAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = if (isCustom) PersianDateHelper.formatSolarDate(customDueDateTimestamp!!) else "تقویم",
                                                color = if (isCustom) Color.White else BentoIndigoAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // تنظیم ارز ثبت حساب شخص
                            Text(
                                text = "ارز ثبت در دفترچه شخص:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    val isDefault = selectedRecipientCurrencyCode == null
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isDefault) BentoNavyDark else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isDefault) BentoNavyDark else BentoBorder, RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedRecipientCurrencyCode = null
                                                recipientExchangeRateText = ""
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        val currentTxnCurrencyCode = txnCurrency?.code ?: "AFN"
                                        Text(
                                            text = "ارز معامله ($currentTxnCurrencyCode)",
                                            color = if (isDefault) Color.White else BentoNavyDark,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                items(activeCurrencies.filter { !it.code.equals(txnCurrency?.code ?: "AFN", ignoreCase = true) }) { curr ->
                                    val isSel = curr.code.equals(selectedRecipientCurrencyCode, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) BentoIndigoAccent else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isSel) BentoIndigoAccent else BentoBorder, RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedRecipientCurrencyCode = curr.code
                                                val targetRate = curr.exchangeRateToBase
                                                val currentTxnRate = txnCurrency?.exchangeRateToBase ?: 1.0
                                                val autoRate = if (targetRate > 0) currentTxnRate / targetRate else 1.0
                                                val autoRateStr = if (autoRate == 1.0) "1.0" else String.format(java.util.Locale.US, "%.4f", autoRate).trimEnd('0').trimEnd('.')
                                                recipientExchangeRateText = autoRateStr
                                                if (evaluatedAmount > 0.0 && autoRate > 0.0) {
                                                    val convertedLedger = evaluatedAmount * autoRate
                                                    recipientLedgerAmountText = if (convertedLedger % 1.0 == 0.0) String.format(java.util.Locale.US, "%.0f", convertedLedger)
                                                    else String.format(java.util.Locale.US, "%.3f", convertedLedger).trimEnd('0').trimEnd('.')
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "${curr.name} (${curr.code})",
                                            color = if (isSel) Color.White else BentoNavyDark,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // اگر ارز حساب شخص متفاوت از ارز معامله باشد
                            if (selectedRecipientCurrencyCode != null) {
                                val targetCurr = activeCurrencies.find { it.code.equals(selectedRecipientCurrencyCode, ignoreCase = true) }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp)),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, BentoBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "محاسبه و ثبت با ارز شخص (${targetCurr?.name ?: selectedRecipientCurrencyCode}):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "مبلغ پرداختی: ",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = "${txnCurrency?.symbol ?: "؋"} ${viewModel.formatAmount(evaluatedAmount)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                            }
                                            Text(
                                                text = " • ثبت در دفترچه شخص:",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // فیلد مبلغ نهایی در دفترچه
                                            OutlinedTextField(
                                                value = recipientLedgerAmountText,
                                                onValueChange = { newLedgerAmt ->
                                                    recipientLedgerAmountText = newLedgerAmt
                                                    val amt = newLedgerAmt.toDoubleOrNull()
                                                    if (amt != null && amt > 0.0 && evaluatedAmount > 0.0) {
                                                        recipientExchangeRateText = String.format(java.util.Locale.US, "%.6f", amt / evaluatedAmount).trimEnd('0').trimEnd('.')
                                                    }
                                                },
                                                label = { Text("مبلغ در دفترچه (${targetCurr?.symbol ?: selectedRecipientCurrencyCode})", fontSize = 10.sp) },
                                                placeholder = { Text("مثلاً 120", fontSize = 11.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                modifier = Modifier.weight(1.2f),
                                                textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                ),
                                                trailingIcon = {
                                                    CalculatorMiniButton(
                                                        onClick = { showCalculatorForRecipientLedger = true },
                                                        contentDescription = "ماشین‌حساب مبلغ در دفترچه"
                                                    )
                                                }
                                            )

                                            // فیلد نرخ تبدیل
                                            OutlinedTextField(
                                                value = recipientExchangeRateText,
                                                onValueChange = { newRate ->
                                                    recipientExchangeRateText = newRate
                                                    val r = newRate.toDoubleOrNull()
                                                    if (r != null && r > 0.0 && evaluatedAmount > 0.0) {
                                                        val convertedLedger = evaluatedAmount * r
                                                        recipientLedgerAmountText = if (convertedLedger % 1.0 == 0.0) String.format(java.util.Locale.US, "%.0f", convertedLedger)
                                                        else String.format(java.util.Locale.US, "%.3f", convertedLedger).trimEnd('0').trimEnd('.')
                                                    }
                                                },
                                                label = { Text("نرخ تبدیل", fontSize = 10.sp) },
                                                placeholder = { Text("نرخ", fontSize = 11.sp) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f),
                                                textStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                ),
                                                trailingIcon = {
                                                    CalculatorMiniButton(
                                                        onClick = { showCalculatorForRecipientRate = true },
                                                        contentDescription = "ماشین‌حساب نرخ تبدیل"
                                                    )
                                                }
                                            )
                                        }

                                        val finalLedgerAmt = recipientLedgerAmountText.toDoubleOrNull() ?: ((recipientExchangeRateText.toDoubleOrNull() ?: 1.0) * evaluatedAmount)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BentoIndigoAccent.copy(alpha = 0.08f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "طرف حساب به مقدار ",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = BentoIndigoAccent
                                                )
                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        text = "${targetCurr?.symbol ?: selectedRecipientCurrencyCode} ${viewModel.formatAmount(finalLedgerAmt)}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoIndigoAccent
                                                    )
                                                }
                                                Text(
                                                    text = " ${if (isRequestMode) "طلبکار (بستانکار)" else "قرضدار (بدهکار)"} خواهد شد.",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = BentoIndigoAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // یادداشت شخص
                            OutlinedTextField(
                                value = transactionNote,
                                onValueChange = { viewModel.setNote(it) },
                                placeholder = { Text("بابت قرض / حواله / توضیحات...", fontSize = 11.sp, color = TextSecondary) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("transaction_note_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoNavyDark,
                                    unfocusedTextColor = BentoNavyDark,
                                    cursorColor = BentoNavyDark,
                                    focusedContainerColor = Color(0xFFF8FAFD),
                                    unfocusedContainerColor = Color(0xFFF8FAFD),
                                    focusedBorderColor = BentoIndigoAccent,
                                    unfocusedBorderColor = BentoBorder
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color(0xFFF8FAFD),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text(
                            text = "لطفاً ابتدا شخص طرف حساب یا «عاید / مصرف عمومی» را از بالای صفحه انتخاب کنید.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        // فضای خالی مختصر در انتهای اسکرول بدون خارج شدن اطلاعات از صفحه
        Spacer(modifier = Modifier.height(16.dp))
    } // End of top scrollable Column

    // --- هدر چسبان مبلغ هنگام اسکرول به بالا (ثابت در بالای صفحه) ---
    androidx.compose.animation.AnimatedVisibility(
        visible = isAmountSticky,
        enter = fadeIn(tween(150)) + slideInVertically { -it / 2 },
        exit = fadeOut(tween(150)) + slideOutVertically { -it / 2 },
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = if (hasBudgetDeficit) 2.dp else 1.dp,
                    color = if (hasBudgetDeficit) ExpenseRed else BentoBorder,
                    shape = RoundedCornerShape(22.dp)
                )
                .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color(0x22001552))
                .testTag("sticky_amount_header"),
            shape = RoundedCornerShape(22.dp),
            color = if (hasBudgetDeficit) Color(0xFFFFF1F2) else SurfaceWhite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // سمت راست در چیدمان راست‌به‌چپ: مشخصات دسته‌بندی / طرف حساب و نشانگر کسری
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedRecipient != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEEF2FF),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = selectedRecipient!!.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoNavyDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (selectedCategory.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = formatCategoryDari(selectedCategory),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoNavyDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (hasBudgetDeficit) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ExpenseRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ExpenseRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "کسری",
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "کسری بودجه",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }

                // سمت چپ در چیدمان: نماد ارز در سمت چپ و مبلغ در سمت راست آن (کاملاً چپ‌به‌راست)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                .clickable { showCurrencySheet = true }
                                .testTag("sticky_currency_selector_trigger"),
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = txnCurrency?.symbol ?: currencySymbol,
                                    fontSize = 18.sp,
                                    color = BentoNavyDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "انتخاب واحد پولی",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            if (calcExpression.contains("+") || calcExpression.contains("-") ||
                                calcExpression.contains("×") || calcExpression.contains("*") ||
                                calcExpression.contains("÷") || calcExpression.contains("/")
                            ) {
                                Text(
                                    text = "$calcExpression =",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = viewModel.formatCalculatorDisplay(calcExpression, evaluatedAmount, txnCurrency?.code ?: selectedCurrency?.code),
                                fontSize = 26.sp,
                                color = if (hasBudgetDeficit) ExpenseRed else BentoNavyDark,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }
                }
            }
        }
    }
} // End of Box container holding scrollable content and sticky header

    // --- ماشین حساب فوق‌العاده مدرن، تمیز و مینیمال الهام گرفته از Samsung Galaxy One UI ---
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .padding(bottom = 96.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(30.dp),
                spotColor = Color(0x1F001552)
            )
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(30.dp))
            .testTag("fixed_bottom_calculator"),
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_keypad"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ردیف ۱: [ AC ] [ +/- ] [ % ] [ ÷ ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SamsungCalcButton(
                            text = "AC",
                            onClick = { viewModel.onClear() },
                            style = SamsungCalcStyle.FUNCTION_CLEAR,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_ac"
                        )
                        SamsungCalcButton(
                            text = "+/-",
                            onClick = { viewModel.onToggleSign() },
                            style = SamsungCalcStyle.FUNCTION_UTIL,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_toggle_sign"
                        )
                        SamsungCalcButton(
                            text = "%",
                            onClick = { viewModel.onPercent() },
                            style = SamsungCalcStyle.FUNCTION_UTIL,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_percent"
                        )
                        SamsungCalcButton(
                            text = "÷",
                            onClick = { viewModel.onOperator("÷") },
                            style = SamsungCalcStyle.OPERATOR_WARM,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_divide"
                        )
                    }

                    // ردیف ۲: [ 7 ] [ 8 ] [ 9 ] [ × ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SamsungCalcButton(
                            text = "7",
                            onClick = { viewModel.onDigit("7") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_7"
                        )
                        SamsungCalcButton(
                            text = "8",
                            onClick = { viewModel.onDigit("8") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_8"
                        )
                        SamsungCalcButton(
                            text = "9",
                            onClick = { viewModel.onDigit("9") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_9"
                        )
                        SamsungCalcButton(
                            text = "×",
                            onClick = { viewModel.onOperator("×") },
                            style = SamsungCalcStyle.OPERATOR_WARM,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_multiply"
                        )
                    }

                    // ردیف ۳: [ 4 ] [ 5 ] [ 6 ] [ − ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SamsungCalcButton(
                            text = "4",
                            onClick = { viewModel.onDigit("4") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_4"
                        )
                        SamsungCalcButton(
                            text = "5",
                            onClick = { viewModel.onDigit("5") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_5"
                        )
                        SamsungCalcButton(
                            text = "6",
                            onClick = { viewModel.onDigit("6") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_6"
                        )
                        SamsungCalcButton(
                            text = "−",
                            onClick = { viewModel.onOperator("-") },
                            style = SamsungCalcStyle.OPERATOR_WARM,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_minus"
                        )
                    }

                    // ردیف ۴: [ 1 ] [ 2 ] [ 3 ] [ + ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SamsungCalcButton(
                            text = "1",
                            onClick = { viewModel.onDigit("1") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_1"
                        )
                        SamsungCalcButton(
                            text = "2",
                            onClick = { viewModel.onDigit("2") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_2"
                        )
                        SamsungCalcButton(
                            text = "3",
                            onClick = { viewModel.onDigit("3") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_3"
                        )
                        SamsungCalcButton(
                            text = "+",
                            onClick = { viewModel.onOperator("+") },
                            style = SamsungCalcStyle.OPERATOR_WARM,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_plus"
                        )
                    }

                    // ردیف ۵: [ 0 ] [ . ] [ ⌫ ] [ = ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SamsungCalcButton(
                            text = "0",
                            onClick = { viewModel.onDigit("0") },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_num_0"
                        )
                        SamsungCalcButton(
                            text = ".",
                            onClick = { viewModel.onDot() },
                            style = SamsungCalcStyle.NUMBER,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_dot"
                        )
                        CalcIconButton(
                            icon = Icons.AutoMirrored.Filled.Backspace,
                            onClick = { viewModel.onBackspace() },
                            modifier = Modifier.weight(1f).height(52.dp)
                        )
                        SamsungCalcButton(
                            text = "=",
                            onClick = { viewModel.onEquals() },
                            style = SamsungCalcStyle.OPERATOR_WARM,
                            modifier = Modifier.weight(1f).height(52.dp),
                            testTag = "calc_btn_equals"
                        )
                    }
                }
            }
        }
    }
    } // End of Column (Top Bar + scrollable content + fixed bottom calculator)

    } // End of inner Box
    } // End of CompositionLocalProvider
    } // End of sliding foreground Box

    // Add New Person Dialog
    if (showAddPersonDialog) {
        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("افزودن شخص جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPersonName,
                        onValueChange = { newPersonName = it },
                        label = { Text("نام شخص") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder
                        )
                    )
                    OutlinedTextField(
                        value = newPersonPhone,
                        onValueChange = { newPersonPhone = it },
                        label = { Text("شماره تماس یا آیدی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPersonName.isNotBlank()) {
                            viewModel.addRecipient(
                                name = newPersonName.trim(),
                                handleOrPhone = newPersonPhone.ifBlank { "@${newPersonName.trim().lowercase().replace(" ", "")}" }
                            )
                            newPersonName = ""
                            newPersonPhone = ""
                            showAddPersonDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Recipient Authentication Dialog for unmasking protected debt
    if (showRecipientAuthDialogForId != null) {
        val targetId = showRecipientAuthDialogForId!!
        val recipientToAuth = recipients.firstOrNull { it.id == targetId } ?: selectedRecipient
        if (recipientToAuth != null) {
            RecipientAuthDialog(
                recipientName = recipientToAuth.name,
                viewModel = viewModel,
                onAuthenticated = {
                    viewModel.unmaskRecipient(targetId)
                    showRecipientAuthDialogForId = null
                },
                onNavigateToSettings = {
                    showRecipientAuthDialogForId = null
                },
                onDismiss = {
                    showRecipientAuthDialogForId = null
                }
            )
        }
    }

    // Modern & Minimal Currency Selection Bottom Sheet
    if (showCurrencySheet) {
        CurrencyPickerBottomSheet(
            currencies = activeCurrencies,
            currencyBalances = currencyBalances,
            selectedCurrency = txnCurrency ?: selectedCurrency,
            onCurrencySelected = { curr ->
                txnCurrency = curr
                if (selectedAccount?.currencyCode?.equals(curr.code, ignoreCase = true) != true) {
                    viewModel.selectAccount(null)
                }
                showCurrencySheet = false
            },
            onNavigateToSettings = onNavigateToSettings,
            onDismiss = { showCurrencySheet = false }
        )
    }

    if (showTxnDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = transactionTimestamp,
            title = "",
            onDismiss = { showTxnDatePicker = false },
            onDateSelected = { ts ->
                transactionTimestamp = ts
                showTxnDatePicker = false
            }
        )
    }

    if (showDueDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = customDueDateTimestamp ?: (System.currentTimeMillis() + 7L * 24L * 60L * 60L * 1000L),
            title = "",
            onDismiss = { showDueDatePicker = false },
            onDateSelected = { ts ->
                customDueDateTimestamp = ts
                selectedDueDateDays = null
                showDueDatePicker = false
            }
        )
    }

    if (showCalculatorForCustomRate) {
        MinimalCalculatorDialog(
            initialValue = customExchangeRateText,
            title = "محاسبه نرخ تبدیل ارز",
            onConfirm = { calculated ->
                customExchangeRateText = calculated
            },
            onDismiss = { showCalculatorForCustomRate = false }
        )
    }

    if (showCalculatorForRecipientRate) {
        MinimalCalculatorDialog(
            initialValue = recipientExchangeRateText,
            title = "محاسبه نرخ تبدیل ارز طرف حساب",
            onConfirm = { calculated ->
                recipientExchangeRateText = calculated
                val r = calculated.toDoubleOrNull()
                if (r != null && r > 0.0 && evaluatedAmount > 0.0) {
                    val convertedLedger = evaluatedAmount * r
                    recipientLedgerAmountText = if (convertedLedger % 1.0 == 0.0) String.format(java.util.Locale.US, "%.0f", convertedLedger)
                    else String.format(java.util.Locale.US, "%.3f", convertedLedger).trimEnd('0').trimEnd('.')
                }
            },
            onDismiss = { showCalculatorForRecipientRate = false }
        )
    }

    if (showCalculatorForRecipientLedger) {
        MinimalCalculatorDialog(
            initialValue = recipientLedgerAmountText,
            title = "محاسبه مبلغ در دفترچه طرف حساب",
            onConfirm = { calculated ->
                recipientLedgerAmountText = calculated
                val amt = calculated.toDoubleOrNull()
                if (amt != null && amt > 0.0 && evaluatedAmount > 0.0) {
                    recipientExchangeRateText = String.format(java.util.Locale.US, "%.6f", amt / evaluatedAmount).trimEnd('0').trimEnd('.')
                }
            },
            onDismiss = { showCalculatorForRecipientLedger = false }
        )
    }
    } // End of BoxWithConstraints
    } // End of CompositionLocalProvider (LTR)
    } // End of else (showSuccessAnim)
    } // End of outer surface or box
} // End of CalculatorTransactionScreen

@Composable
fun ModePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) BentoNavyDark else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

// --- استایل و دکمه‌های ماشین حساب مدرن Samsung Galaxy One UI ---
enum class SamsungCalcStyle {
    NUMBER,          // دکمه‌های ارقام: زمینه بسیار روشن/سفید تمیز، ارقام واضح و مشکی
    OPERATOR_WARM,   // دکمه‌های عملیات: رنگ تأکیدی گرم مدرن سامسونگ (نارنجی زنده #FF851B / #F97316) با متن سفید واضح
    FUNCTION_CLEAR,  // دکمه پاک‌کردن AC: زمینه روشن با رنگ مرجانی نرم
    FUNCTION_UTIL    // دکمه‌های عملیاتی +/- و %: زمینه خاکستری روشن و مینیمال
}

@Composable
fun SamsungCalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SamsungCalcStyle = SamsungCalcStyle.NUMBER,
    testTag: String = ""
) {
    val containerColor = when (style) {
        SamsungCalcStyle.NUMBER -> Color(0xFFFFFFFF)
        SamsungCalcStyle.OPERATOR_WARM -> Color(0xFFFF851B) // رنگ نارنجی گرم شاخص ماشین‌حساب گلکسی سامسونگ
        SamsungCalcStyle.FUNCTION_CLEAR -> Color(0xFFFEE2E2) // رنگ مرجانی نرم کاملاً مات و بدون سایه اضافه
        SamsungCalcStyle.FUNCTION_UTIL -> Color(0xFFF1F5F9)
    }

    val contentColor = when (style) {
        SamsungCalcStyle.NUMBER -> Color(0xFF0F172A) // ارقام بزرگ، واضح، مشکی و خوانا
        SamsungCalcStyle.OPERATOR_WARM -> Color.White
        SamsungCalcStyle.FUNCTION_CLEAR -> ExpenseRed
        SamsungCalcStyle.FUNCTION_UTIL -> BentoNavyDark.copy(alpha = 0.85f)
    }

    val borderColor = when (style) {
        SamsungCalcStyle.NUMBER -> Color(0xFFE2E8F0).copy(alpha = 0.7f)
        SamsungCalcStyle.OPERATOR_WARM -> Color(0xFFFF9E43).copy(alpha = 0.4f)
        SamsungCalcStyle.FUNCTION_CLEAR -> Color(0xFFFECACA)
        SamsungCalcStyle.FUNCTION_UTIL -> Color(0xFFE2E8F0).copy(alpha = 0.7f)
    }

    val elevation = when (style) {
        SamsungCalcStyle.NUMBER -> 1.dp
        SamsungCalcStyle.OPERATOR_WARM -> 1.5.dp
        SamsungCalcStyle.FUNCTION_CLEAR -> 1.dp
        SamsungCalcStyle.FUNCTION_UTIL -> 1.dp
    }

    val fontSize = when (style) {
        SamsungCalcStyle.NUMBER -> if (text == ".") 28.sp else 24.sp
        SamsungCalcStyle.OPERATOR_WARM -> if (text == "=") 26.sp else 24.sp
        SamsungCalcStyle.FUNCTION_CLEAR -> 18.sp
        SamsungCalcStyle.FUNCTION_UTIL -> 18.sp
    }

    val fontWeight = when (style) {
        SamsungCalcStyle.NUMBER -> FontWeight.SemiBold
        SamsungCalcStyle.OPERATOR_WARM -> FontWeight.Bold
        SamsungCalcStyle.FUNCTION_CLEAR -> FontWeight.Bold
        SamsungCalcStyle.FUNCTION_UTIL -> FontWeight.Bold
    }

    val shape = RoundedCornerShape(22.dp)

    Surface(
        onClick = onClick,
        modifier = modifier.then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = shape,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = elevation,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            )
        }
    }
}

// دکمه‌های سازگاری قبلی (Backward Compatibility)
@Composable
fun CalcOperatorButton(
    op: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SamsungCalcButton(
        text = op,
        onClick = onClick,
        modifier = modifier,
        style = if (op == "C" || op == "AC") SamsungCalcStyle.FUNCTION_CLEAR else SamsungCalcStyle.OPERATOR_WARM,
        testTag = "calc_op_$op"
    )
}

@Composable
fun CalcNumberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SamsungCalcButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = SamsungCalcStyle.NUMBER,
        testTag = "calc_num_$text"
    )
}

@Composable
fun CalcIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .border(0.75.dp, Color(0xFFE2E8F0).copy(alpha = 0.7f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("calc_backspace"),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF1F5F9),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = "حذف رقم",
                tint = BentoNavyDark,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun CardItemChip(
    account: com.example.data.local.AccountCardEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BentoNavyDark else Color(0xFFF1F5F9))
            .border(1.dp, if (isSelected) BentoNavyDark else BentoBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${account.name} (${account.cardNumberMasked})",
                color = if (isSelected) Color.White else BentoNavyDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

