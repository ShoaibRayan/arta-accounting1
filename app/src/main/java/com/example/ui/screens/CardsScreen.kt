package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.DecimalFormat
import com.example.ui.theme.CardDarkGradientEnd
import com.example.ui.theme.CardDarkGradientStart
import com.example.ui.theme.CardGoldGradientEnd
import com.example.ui.theme.CardGoldGradientStart
import com.example.ui.theme.CardTealGradientEnd
import com.example.ui.theme.CardTealGradientStart
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AccountCardEntity
import com.example.ui.components.DigitalCardItem
import com.example.ui.components.LuxuryTransferDialog
import com.example.ui.components.LuxuryWalletDetailAndEditBottomSheet
import com.example.ui.components.TransferTab
import com.example.ui.theme.AccentLime
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun CardsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val activeCurrencies by viewModel.activeCurrencies.collectAsStateWithLifecycle()

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferInitialTab by remember { mutableStateOf(TransferTab.CASH_AND_CARD) }
    var selectedAccountForDetail by remember { mutableStateOf<AccountCardEntity?>(null) }
    var dragX by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundCanvas)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragX = 0f },
                    onDragEnd = {
                        // Swipe Right to Left returns to Home
                        if (dragX < -70f) {
                            onNavigateBack?.invoke()
                        }
                        dragX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount
                    }
                )
            }
            .testTag("cards_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 78.dp)
    ) {
        // --- Header (Wallets & Cards) ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceWhite)
                                .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                .testTag("cards_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Home",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column {
                        Text(
                            text = "کیف پول‌ها و کارت‌ها",
                            color = BentoNavyDark,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مدیریت حسابات بانکی و کارت‌های دیجیتال",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Header Action Buttons: Transfer Button (Icon-only) & New Card Button (Icon-only)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Transfer Button in Header (Icon-only)
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                transferInitialTab = TransferTab.CASH_AND_CARD
                                showTransferDialog = true
                            }
                            .testTag("cards_header_transfer_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoLavenderSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "انتقال وجه",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Add Card Button (Icon-only with plus sign, no "کارت جدید" text)
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showAddCardDialog = true }
                            .testTag("add_card_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = BentoNavyDark
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "افزودن کارت جدید",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Empty state if no accounts exist
        if (accounts.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceWhite,
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(BentoLavenderSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "هنوز کارتی اضافه نکرده‌اید",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "با افزودن کارت‌های بانکی یا کیف پول‌های خود، موجودی و تراکنش‌های آنها را به راحتی مدیریت و تفکیک کنید.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showAddCardDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("افزودن کارت اول", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Cards List ---
        items(accounts, key = { it.id }) { card ->
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                DigitalCardItem(
                    card = card,
                    currencySymbol = currencySymbol,
                    onClick = { selectedAccountForDetail = card }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Card Actions Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (card.isFrozen) "کارت مسدود است" else "کارت فعال است",
                            color = if (card.isFrozen) ExpenseRed else IncomeGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier.clickable { viewModel.toggleFreezeAccount(card) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (card.isFrozen) Icons.Default.LockOpen else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (card.isFrozen) "رفع انسداد" else "مسدود ساختن",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoNavyDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card security / info tip card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BentoLavenderSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "امنیت و رمزنگاری بانکی",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "تمام اطلاعات کارت‌ها و حساب‌های شما به شکل امن و رمزگذاری شده ذخیره می‌شوند.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Add Card Dialog
    if (showAddCardDialog) {
        var cardName by remember { mutableStateOf("") }
        var last4 by remember { mutableStateOf("") }
        var initialBal by remember { mutableStateOf("0") }
        var showCalculatorForBalance by remember { mutableStateOf(false) }
        var themeColor by remember { mutableStateOf("dark") }
        var cardCurrencyCode by remember { mutableStateOf("AFN") }
        var cardCurrencySymbol by remember { mutableStateOf("؋") }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = BentoNavyDark,
            unfocusedTextColor = BentoNavyDark,
            cursorColor = BentoNavyDark,
            focusedBorderColor = BentoNavyDark,
            unfocusedBorderColor = BentoBorder,
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWhite,
            focusedLabelColor = BentoNavyDark,
            unfocusedLabelColor = TextSecondary
        )

        Dialog(
            onDismissRequest = { showAddCardDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 480.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .border(1.2.dp, BentoBorder, RoundedCornerShape(26.dp)),
                    color = SurfaceWhite,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(BentoNavyDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "افزودن کارت یا حساب جدید",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "مشخصات و موجودی اولیه کارت را تعیین کنید",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showAddCardDialog = false },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Card Preview
                        val previewGradient = when (themeColor.lowercase()) {
                            "teal" -> Brush.linearGradient(listOf(CardTealGradientStart, CardTealGradientEnd))
                            "gold" -> Brush.linearGradient(listOf(CardGoldGradientStart, CardGoldGradientEnd))
                            else -> Brush.linearGradient(listOf(CardDarkGradientStart, CardDarkGradientEnd))
                        }
                        val previewFormattedBal = initialBal.toDoubleOrNull()?.let {
                            DecimalFormat("#,###.##").format(it)
                        } ?: "0"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(previewGradient)
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = cardName.ifBlank { "کارت جدید" },
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = cardCurrencyCode,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "•••• ${last4.ifBlank { "••••" }}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "موجودی اولیه",
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "$cardCurrencySymbol $previewFormattedBal",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Card Name Field
                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("نام کارت یا حساب") },
                            placeholder = { Text("مثلاً بانک ملت، صادرات، کارت دیجیتال...", color = TextSecondary.copy(alpha = 0.6f)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_card_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Last 4 Digits Field
                        OutlinedTextField(
                            value = last4,
                            onValueChange = { input ->
                                if (input.length <= 4 && input.all { it.isDigit() }) last4 = input
                            },
                            label = { Text("۴ رقم آخر کارت") },
                            placeholder = { Text("مثلاً ۱۲۳۴", color = TextSecondary.copy(alpha = 0.6f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_card_digits_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Initial Balance Field (default 0)
                        OutlinedTextField(
                            value = initialBal,
                            onValueChange = { initialBal = it },
                            label = { Text("موجودی اولیه ($cardCurrencySymbol)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_card_balance_input"),
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors,
                            trailingIcon = {
                                CalculatorMiniButton(
                                    onClick = { showCalculatorForBalance = true },
                                    contentDescription = "ماشین‌حساب موجودی اولیه"
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Currency Selection
                        Text(
                            text = "واحد پولی این کیف پول:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(activeCurrencies) { c ->
                                val isSel = c.code.equals(cardCurrencyCode, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            cardCurrencyCode = c.code
                                            cardCurrencySymbol = c.symbol
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = c.flagEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${c.code} (${c.symbol})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else BentoNavyDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Card Theme / Color Selection
                        Text(
                            text = "طرح و رنگ کارت:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                Triple("dark", "سرمه‌ای لوکس", PrimaryDark),
                                Triple("teal", "نیلی مدرن", PrimaryTeal),
                                Triple("gold", "طلایی شاهانه", Color(0xFFD4AF37))
                            ).forEach { (th, label, dotColor) ->
                                val isSel = themeColor == th
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { themeColor = th }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            color = if (isSel) Color.White else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAddCardDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Text("انصراف", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val bal = initialBal.toDoubleOrNull() ?: 0.0
                                    val finalLast4 = last4.ifBlank { "0000" }
                                    viewModel.addAccount(
                                        name = cardName.ifBlank { "کارت دیجیتال" },
                                        cardNumber = "•••• $finalLast4",
                                        balance = bal,
                                        theme = themeColor,
                                        currencyCode = cardCurrencyCode,
                                        currencySymbol = cardCurrencySymbol
                                    )
                                    showAddCardDialog = false
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("submit_add_card_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                            ) {
                                Text(
                                    text = "افزودن کارت",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCalculatorForBalance) {
            MinimalCalculatorDialog(
                initialValue = initialBal,
                title = "محاسبه موجودی اولیه",
                onConfirm = { calculated ->
                    initialBal = calculated
                },
                onDismiss = { showCalculatorForBalance = false }
            )
        }
    }

    // Luxury Detail & Edit Bottom Sheet for Wallets
    selectedAccountForDetail?.let { acct ->
        LuxuryWalletDetailAndEditBottomSheet(
            account = acct,
            activeCurrencies = activeCurrencies,
            onDismiss = { selectedAccountForDetail = null },
            onUpdate = { updated ->
                viewModel.updateAccount(updated)
                selectedAccountForDetail = null
            },
            onDelete = { toDelete ->
                viewModel.deleteAccount(toDelete)
                selectedAccountForDetail = null
            },
            onToggleFreeze = { toFreeze ->
                viewModel.toggleFreezeAccount(toFreeze)
                selectedAccountForDetail = null
            }
        )
    }

    if (showTransferDialog) {
        val recipients by viewModel.recipients.collectAsStateWithLifecycle()
        LuxuryTransferDialog(
            viewModel = viewModel,
            accounts = accounts.filter { !it.isFrozen },
            recipients = recipients,
            activeCurrencies = activeCurrencies,
            initialTab = transferInitialTab,
            lockedTab = transferInitialTab,
            onDismiss = { showTransferDialog = false }
        )
    }
}
