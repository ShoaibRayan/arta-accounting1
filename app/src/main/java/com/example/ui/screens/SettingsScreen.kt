package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Widgets
import com.example.ui.components.QuickActionsManagementBottomSheet
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import com.example.ui.components.SwipeToRevealActionsLayout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Storage
import com.example.util.BackupFileInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.Intent
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.platform.LocalContext
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import com.example.data.local.CategoryEntity
import com.example.data.local.TransactionType
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CurrencyEntity
import com.example.ui.viewmodel.CurrencyBalanceInfo
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel

enum class SettingsSection {
    CURRENCIES,
    EXCHANGE,
    CATEGORIES,
    SECURITY,
    GENERAL
}

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBudget: () -> Unit = {},
    initialSection: SettingsSection = SettingsSection.CURRENCIES,
    modifier: Modifier = Modifier
) {
    val allCurrencies by viewModel.currencies.collectAsState()
    val activeCurrencies by viewModel.activeCurrencies.collectAsState()
    val currencyBalances by viewModel.currencyBalances.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val recipients by viewModel.recipients.collectAsState()
    val activeGoals by viewModel.activeGoals.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userAvatarEmoji by viewModel.userAvatarEmoji.collectAsState()
    val userAvatarColor by viewModel.userAvatarColor.collectAsState()
    val operationError by viewModel.operationErrorMessage.collectAsState()

    val currentSavedSection by viewModel.currentSettingsSection.collectAsState()
    var selectedSection by rememberSaveable {
        mutableStateOf(
            if (initialSection != SettingsSection.CURRENCIES) initialSection
            else currentSavedSection
        )
    }

    LaunchedEffect(selectedSection) {
        viewModel.setSettingsSection(selectedSection)
    }

    // Dialog state for Add/Edit currency
    var isCurrencyDialogOpen by remember { mutableStateOf(false) }
    var currencyToEdit by remember { mutableStateOf<CurrencyEntity?>(null) }

    // Category dialog state
    var isCategoryDialogOpen by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryFilterIndex by remember { mutableIntStateOf(0) }

    // Profile dialog state
    var isProfileDialogOpen by remember { mutableStateOf(false) }

    // Exchange success banner
    var exchangeSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Data Management (Backup, Restore, Wipe, Auto-Backup) State
    val context = LocalContext.current
    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    val pendingFileRestore by viewModel.pendingFileRestore.collectAsState()

    val localBackups by viewModel.localBackups.collectAsState()
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsState()
    val autoBackupDestination by viewModel.autoBackupDestination.collectAsState()
    val lastAutoBackupTime by viewModel.lastAutoBackupTime.collectAsState()
    val isOperatingBackup by viewModel.isOperatingBackup.collectAsState()

    var pendingRestoreLocalFile by remember { mutableStateOf<BackupFileInfo?>(null) }
    var showLocalFileRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showBackupSuccessDialog by remember { mutableStateOf(false) }
    var savedBackupInfo by remember { mutableStateOf<BackupFileInfo?>(null) }

    // Deletion confirmation states (Requirement 18)
    var currencyToDelete by remember { mutableStateOf<CurrencyEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var localBackupToDelete by remember { mutableStateOf<File?>(null) }

    val exportBakFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        viewModel.setInternalPickerActive(false)
        if (uri != null) {
            viewModel.exportBackup(
                onResult = { bakContent ->
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(bakContent.toByteArray(Charsets.UTF_8))
                        }
                        Toast.makeText(context, "فایل پشتیبان .bak با موفقیت در محل انتخابی ذخیره شد.", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در ذخیره فایل: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    val importBakFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        viewModel.setInternalPickerActive(false)
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    var fileName = "نسخه پشتیبان (.bak)"
                    try {
                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (cursor.moveToFirst() && nameIndex >= 0) {
                                fileName = cursor.getString(nameIndex) ?: fileName
                            }
                        }
                    } catch (_: Exception) {}

                    viewModel.stageBackupForRestore(content, fileName)
                } else {
                    Toast.makeText(context, "فایل انتخاب‌شده خالی یا نامعتبر است.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در خواندن فایل: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val shareLocalBackupFile: (File) -> Unit = { file ->
        try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "پشتیبان سیستم مالی آرتا - ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "فایل پشتیبان استاندارد پایگاه داده آرتا (.bak)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری و ارسال فایل پشتیبان"))
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Base Currency Management State
    val hasCurrencyDependentData by viewModel.hasCurrencyDependentData.collectAsState()
    var currencyToSetBase by remember { mutableStateOf<CurrencyEntity?>(null) }
    var showSetBaseConfirmDialog by remember { mutableStateOf(false) }
    var showSelectBaseCurrencyDialog by remember { mutableStateOf(false) }
    var showBaseCurrencyLockedInfoDialog by remember { mutableStateOf(false) }
    var showClearTransactionsConfirmDialog by remember { mutableStateOf(false) }
    val currentBaseCurrency = allCurrencies.find { it.isBaseCurrency } ?: allCurrencies.firstOrNull()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
                .statusBarsPadding()
                .testTag("settings_screen")
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = BentoNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "تنظیمات برنامه",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Text(
                            text = "مدیریت ارزها، تبادل اسعار و حساب کاربری",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Tab navigation
            ScrollableTabRow(
                selectedTabIndex = selectedSection.ordinal,
                containerColor = SurfaceWhite,
                contentColor = BentoNavyDark,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedSection.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection.ordinal]),
                            color = BentoNavyDark,
                            height = 3.dp
                        )
                    }
                }
            ) {
                Tab(
                    selected = selectedSection == SettingsSection.CURRENCIES,
                    onClick = { selectedSection = SettingsSection.CURRENCIES },
                    text = {
                        Text(
                            text = "ارزها",
                            fontWeight = if (selectedSection == SettingsSection.CURRENCIES) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_currencies")
                )
                Tab(
                    selected = selectedSection == SettingsSection.EXCHANGE,
                    onClick = { selectedSection = SettingsSection.EXCHANGE },
                    text = {
                        Text(
                            text = "صرافی",
                            fontWeight = if (selectedSection == SettingsSection.EXCHANGE) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_exchange")
                )
                Tab(
                    selected = selectedSection == SettingsSection.CATEGORIES,
                    onClick = { selectedSection = SettingsSection.CATEGORIES },
                    text = {
                        Text(
                            text = "دسته‌بندی‌ها",
                            fontWeight = if (selectedSection == SettingsSection.CATEGORIES) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_categories")
                )
                Tab(
                    selected = selectedSection == SettingsSection.SECURITY,
                    onClick = { selectedSection = SettingsSection.SECURITY },
                    text = {
                        Text(
                            text = "امنیت و رمز",
                            fontWeight = if (selectedSection == SettingsSection.SECURITY) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_security")
                )
                Tab(
                    selected = selectedSection == SettingsSection.GENERAL,
                    onClick = { selectedSection = SettingsSection.GENERAL },
                    text = {
                        Text(
                            text = "پروفایل",
                            fontWeight = if (selectedSection == SettingsSection.GENERAL) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_general")
                )
            }

            if (selectedSection == SettingsSection.SECURITY) {
                SecuritySettingsContent(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Body content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedSection) {
                        SettingsSection.SECURITY -> {}
                    SettingsSection.CURRENCIES -> {
                        item {
                            // Banner about independent currency balance
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "سیستم چند ارزی مستقل",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF312E81)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "هر ارز بیلانس و گردش مالی کاملاً تفکیک‌شده دارد و مبالغ ارزهای گوناگون با هم جمع نمی‌شوند مگر اینکه عمل تبادل صورت گیرد.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4338CA),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            // Base Currency Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("base_currency_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (hasCurrencyDependentData) Color(0xFFFBFBFE) else Color(0xFFF0FDF4)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (hasCurrencyDependentData) BentoBorder else Color(0xFF86EFAC)
                                )
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (hasCurrencyDependentData) BentoNavyDark.copy(alpha = 0.08f) else Color(0xFF10B981).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = currentBaseCurrency?.flagEmoji ?: "🪙",
                                                    fontSize = 24.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "ارز پایه حسابداری آرتا",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoNavyDark
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${currentBaseCurrency?.name ?: "نامشخص"} (${currentBaseCurrency?.code ?: "---"}) - نماد: ${currentBaseCurrency?.symbol ?: ""}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (hasCurrencyDependentData) BentoIndigoAccent else Color(0xFF047857)
                                                )
                                            }
                                        }

                                        // Status Chip
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (hasCurrencyDependentData) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (hasCurrencyDependentData) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = null,
                                                    tint = if (hasCurrencyDependentData) ExpenseRed else Color(0xFF16A34A),
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (hasCurrencyDependentData) "قفل (دارای تراکنش)" else "آماده تنظیم",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hasCurrencyDependentData) ExpenseRed else Color(0xFF16A34A)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = if (hasCurrencyDependentData) {
                                            "ارز پایه مبنای محاسبات، گزارش‌ها و نمودارهای مالی است. به دلیل ثبت تراکنش‌ها یا مانده‌های مالی در برنامه، ارز پایه جهت حفظ یکپارچگی حساب‌ها قفل است."
                                        } else {
                                            "هیچ گردش مالی وابسته در سیستم ثبت نشده است. می‌توانید با زدن دکمه زیر، واحد پولی دلخواه را به عنوان ارز پایه برنامه تعیین کنید."
                                        },
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (hasCurrencyDependentData) {
                                                    showBaseCurrencyLockedInfoDialog = true
                                                } else {
                                                    showSelectBaseCurrencyDialog = true
                                                }
                                            },
                                            modifier = Modifier.weight(1f).testTag("btn_change_base_currency"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                                        ) {
                                            Icon(
                                                imageVector = if (hasCurrencyDependentData) Icons.Default.Lock else Icons.Default.Tune,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (hasCurrencyDependentData) "تغییر ارز پایه" else "انتخاب و تنظیم ارز پایه",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (hasCurrencyDependentData) {
                                            OutlinedButton(
                                                onClick = { showBaseCurrencyLockedInfoDialog = true },
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                                                modifier = Modifier.testTag("btn_locked_currency_info")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = ExpenseRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "راهنما",
                                                    fontSize = 12.sp,
                                                    color = ExpenseRed,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Add Currency Button Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "واحدهای پولی فعال و تعریف‌شده",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark
                                )

                                Button(
                                    onClick = {
                                        currencyToEdit = null
                                        isCurrencyDialogOpen = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("add_currency_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "افزودن ارز",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "افزودن ارز",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        items(allCurrencies) { currency ->
                            val balanceInfo = currencyBalances.find { (currency.id > 0 && it.currency.id == currency.id) || it.currency.code.equals(currency.code, ignoreCase = true) }
                            val bal = balanceInfo?.balance ?: 0.0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("currency_card_${currency.code}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
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
                                                    .background(Color(currency.colorHex).copy(alpha = 0.15f))
                                                    .border(1.dp, Color(currency.colorHex).copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = currency.flagEmoji,
                                                    fontSize = 24.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = currency.name,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(currency.colorHex).copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = currency.code,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(currency.colorHex),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    if (currency.isBaseCurrency) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = PrimaryTeal.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                text = "ارز پایه",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = PrimaryTeal,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "علامت: ${currency.symbol}  |  نرخ به پایه: ${currency.exchangeRateToBase}",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        // Active Switch
                                        Switch(
                                            checked = currency.isActive,
                                            onCheckedChange = {
                                                if (!currency.isBaseCurrency) {
                                                    viewModel.toggleCurrencyActive(currency)
                                                }
                                            },
                                            enabled = !currency.isBaseCurrency,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = BentoNavyDark,
                                                uncheckedThumbColor = Color.LightGray,
                                                uncheckedTrackColor = Color(0xFFE5E7EB)
                                            ),
                                            modifier = Modifier.testTag("switch_active_${currency.code}")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Balance & Action Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF9FAFC))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "بیلانس خالص این ارز: ",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "${currency.symbol} ${String.format(java.util.Locale.US, "%,.0f", bal)}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (bal >= 0) IncomeGreen else ExpenseRed
                                            )
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    currencyToEdit = currency
                                                    isCurrencyDialogOpen = true
                                                },
                                                modifier = Modifier.size(32.dp).testTag("edit_currency_${currency.code}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "ویرایش",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            if (!currency.isBaseCurrency) {
                                                IconButton(
                                                    onClick = {
                                                        currencyToDelete = currency
                                                    },
                                                    modifier = Modifier.size(32.dp).testTag("delete_currency_${currency.code}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "حذف",
                                                        tint = ExpenseRed.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (currency.isBaseCurrency) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.12f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF047857),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(5.dp))
                                                    Text(
                                                        text = "ارز پایه اصلی سیستم",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF047857)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSection.EXCHANGE -> {
                        item {
                            // Currency Exchange Module
                            CurrencyExchangeCard(
                                currencies = activeCurrencies,
                                currencyBalances = currencyBalances,
                                onExecuteExchange = { from, to, amount, rate, targetAmount ->
                                    viewModel.executeCurrencyExchange(
                                        fromCurrency = from,
                                        toCurrency = to,
                                        fromAmount = amount,
                                        rate = rate,
                                        explicitToAmount = targetAmount,
                                        onSuccess = {
                                            exchangeSuccessMessage = "تبدیل مبلغ ${String.format(java.util.Locale.US, "%,.2f", amount)} ${from.symbol} به ${String.format(java.util.Locale.US, "%,.2f", targetAmount)} ${to.symbol} با موفقیت انجام شد."
                                        }
                                    )
                                },
                                successMessage = exchangeSuccessMessage,
                                onDismissSuccessMessage = { exchangeSuccessMessage = null }
                            )
                        }
                    }

                    SettingsSection.CATEGORIES -> {
                        item {
                            // Category Management Header Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(IncomeGreen.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Category,
                                                contentDescription = null,
                                                tint = IncomeGreen,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "دسته‌بندی‌های عواید و مصارف",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = BentoNavyDark
                                            )
                                            Text(
                                                text = "مدیریت آیکون، نام، وضعیت و نوع دسته‌ها",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            categoryToEdit = null
                                            isCategoryDialogOpen = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.testTag("add_category_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("دسته جدید", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Category filter tabs: All | Expenses | Incomes
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val filterTabs = listOf("همه دسته‌ها (${categories.size})", "مصارف (Expense)", "عواید (Income)")
                                filterTabs.forEachIndexed { index, label ->
                                    val isSelected = categoryFilterIndex == index
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, if (isSelected) BentoNavyDark else BentoBorder, RoundedCornerShape(12.dp))
                                            .clickable { categoryFilterIndex = index },
                                        color = if (isSelected) BentoNavyDark else SurfaceWhite,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else BentoNavyDark,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Filtered list of categories
                        val filteredCategories = categories.filter { cat ->
                            when (categoryFilterIndex) {
                                1 -> cat.type == TransactionType.EXPENSE
                                2 -> cat.type == TransactionType.INCOME
                                else -> true
                            }
                        }

                        items(filteredCategories) { cat ->
                            CategoryManageRowItem(
                                category = cat,
                                onToggleStatus = { viewModel.toggleCategoryStatus(cat) },
                                onEdit = {
                                    categoryToEdit = cat
                                    isCategoryDialogOpen = true
                                },
                                onDelete = { categoryToDelete = cat }
                            )
                        }
                    }

                    SettingsSection.GENERAL -> {
                        item {
                            // User Profile Card with Live Data and Edit Button
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(Color(userAvatarColor)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (userAvatarEmoji.isNotBlank()) {
                                                Text(
                                                    text = userAvatarEmoji,
                                                    fontSize = 24.sp
                                                )
                                            } else {
                                                Text(
                                                    text = userName.take(1),
                                                    color = Color.White,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column {
                                            Text(
                                                text = userName,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoNavyDark
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "مدیر سیستم مالی و حسابداری شخصی",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { isProfileDialogOpen = true },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF1F5F9))
                                            .testTag("edit_profile_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ویرایش مشخصات",
                                            tint = BentoNavyDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            val selectedCalendarType by viewModel.selectedCalendarType.collectAsState()
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("calendar_settings_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BentoNavyDark.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                tint = BentoNavyDark,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "تقویم و فرمت تاریخ و ساعت",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = BentoNavyDark
                                            )
                                            Text(
                                                text = "تاریخ‌ها در پایگاه داده استاندارد ثبت شده و بر اساس این تقویم نمایش داده می‌شوند",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    com.example.util.AppCalendarType.values().forEach { calType ->
                                        val isSelected = selectedCalendarType == calType
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) BentoIndigoAccent else BentoBorder,
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable { viewModel.setCalendarType(calType) },
                                            color = if (isSelected) Color(0xFFF5F7FF) else SurfaceWhite,
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = calType.displayName,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            fontSize = 13.sp,
                                                            color = if (isSelected) BentoIndigoAccent else BentoNavyDark
                                                        )
                                                        if (isSelected) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Text(
                                                                text = "✓ فعال",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = BentoIndigoAccent
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = calType.description,
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "امروز: ${com.example.util.PersianDateHelper.formatDate(System.currentTimeMillis(), calType)}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelected) BentoIndigoAccent else TextSecondary
                                                    )
                                                }

                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { viewModel.setCalendarType(calType) },
                                                    colors = RadioButtonDefaults.colors(selectedColor = BentoIndigoAccent)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Data Management, Backup & Reset Card
                            Card(
                                modifier = Modifier.fillMaxWidth().testTag("data_management_card"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryTeal.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cloud,
                                                contentDescription = null,
                                                tint = PrimaryTeal,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "پشتیبان‌گیری، گوگل درایو و بازیابی داده‌ها",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = BentoNavyDark
                                            )
                                            Text(
                                                text = "فرمت معیاری (.bak)، ذخیره محلی و پشتیبان‌گیری خودکار در دستگاه",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Auto-Backup Configuration Section
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = BentoIndigoAccent.copy(alpha = 0.05f),
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoIndigoAccent.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        viewModel.updateAutoBackupSettings(
                                                            enabled = !isAutoBackupEnabled,
                                                            frequency = autoBackupFrequency,
                                                            destination = autoBackupDestination
                                                        )
                                                    }
                                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Autorenew, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = "پشتیبان‌گیری خودکار (Auto-Backup)",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = BentoNavyDark
                                                        )
                                                        Text(
                                                            text = if (isAutoBackupEnabled) "فعال • نگهداری خودکار ۵ نسخه اخیر" else "غیرفعال",
                                                            fontSize = 11.sp,
                                                            color = if (isAutoBackupEnabled) BentoIndigoAccent else TextSecondary
                                                        )
                                                    }
                                                }

                                                Switch(
                                                    checked = isAutoBackupEnabled,
                                                    onCheckedChange = { isChecked ->
                                                        viewModel.updateAutoBackupSettings(
                                                            enabled = isChecked,
                                                            frequency = autoBackupFrequency,
                                                            destination = autoBackupDestination
                                                        )
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = SurfaceWhite,
                                                        checkedTrackColor = BentoIndigoAccent
                                                    )
                                                )
                                            }

                                            if (isAutoBackupEnabled) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Text("دوره زمانی پشتیبان‌گیری:", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val frequencies = listOf(
                                                        "daily" to "روزانه",
                                                        "weekly" to "هفتگی",
                                                        "per_5_tx" to "هر ۵ معامله"
                                                    )
                                                    frequencies.forEach { (freqKey, label) ->
                                                        val isSelected = autoBackupFrequency == freqKey
                                                        Surface(
                                                            shape = RoundedCornerShape(10.dp),
                                                            color = if (isSelected) BentoIndigoAccent else SurfaceWhite,
                                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) BentoIndigoAccent else BentoBorder),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clickable {
                                                                    viewModel.updateAutoBackupSettings(
                                                                        enabled = true,
                                                                        frequency = freqKey,
                                                                        destination = autoBackupDestination
                                                                    )
                                                                }
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.padding(vertical = 7.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = label,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                    color = if (isSelected) SurfaceWhite else BentoNavyDark
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = SurfaceWhite,
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.Storage, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(16.dp))
                                                        Text("مقصد ذخیره: حافظه محلی دستگاه (پوشه پشتیبان)", fontSize = 11.sp, color = BentoNavyDark, fontWeight = FontWeight.Medium)
                                                    }
                                                }

                                                if (lastAutoBackupTime > 0) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "آخرین بک‌آپ خودکار: ${com.example.util.PersianDateHelper.formatSolarDateTime(lastAutoBackupTime)}",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 3. Local Standard .bak File Section
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = BentoNavyDark.copy(alpha = 0.04f),
                                        border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoNavyDark.copy(alpha = 0.2f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Storage, contentDescription = null, tint = BentoNavyDark, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "فایل پشتیبان در حافظه دستگاه (.bak)",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = BentoNavyDark
                                                    )
                                                    Text(
                                                        text = "فرمت استاندارد فشرده، امن و سازگار بدون خطر کرش",
                                                        fontSize = 10.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.createLocalBackup(
                                                            onSuccess = { info ->
                                                                savedBackupInfo = info
                                                                showBackupSuccessDialog = true
                                                                Toast.makeText(context, "فایل پشتیبان .bak در حافظه ذخیره شد.", Toast.LENGTH_LONG).show()
                                                            },
                                                            onError = { err ->
                                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                            }
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("direct_local_backup_button"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                                                    enabled = !isOperatingBackup
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Text("ذخیره مستقیم (.bak)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        try {
                                                            viewModel.setInternalPickerActive(true)
                                                            importBakFileLauncher.launch(arrayOf("*/*"))
                                                        } catch (e: Exception) {
                                                            viewModel.setInternalPickerActive(false)
                                                            Toast.makeText(context, "خطا در باز کردن انتخابگر فایل: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f).testTag("restore_local_file_button"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BentoNavyDark),
                                                    enabled = !isOperatingBackup
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = BentoNavyDark, modifier = Modifier.size(16.dp))
                                                        Text("بازیابی از فایل", color = BentoNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                }
                                            }

                                            // Display local backups list if any exists
                                            if (localBackups.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text(
                                                    text = "نسخه‌های موجود در حافظه (${localBackups.size}):",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoNavyDark
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    localBackups.take(5).forEach { backupInfo ->
                                                        Surface(
                                                            shape = RoundedCornerShape(12.dp),
                                                            color = SurfaceWhite,
                                                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(10.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Folder,
                                                                        contentDescription = null,
                                                                        tint = BentoNavyDark,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                    Column {
                                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                                            Text(
                                                                                text = backupInfo.name,
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                color = BentoNavyDark,
                                                                                maxLines = 1
                                                                            )
                                                                            if (backupInfo.isAutoBackup) {
                                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(4.dp),
                                                                                    color = BentoIndigoAccent.copy(alpha = 0.15f)
                                                                                ) {
                                                                                    Text("خودکار", fontSize = 9.sp, color = BentoIndigoAccent, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                                                }
                                                                            }
                                                                        }
                                                                        Text(
                                                                            text = "${backupInfo.modifiedJalali} • ${backupInfo.sizeFormatted}",
                                                                            fontSize = 10.sp,
                                                                            color = TextSecondary
                                                                        )
                                                                    }
                                                                }

                                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    IconButton(
                                                                        onClick = {
                                                                            pendingRestoreLocalFile = backupInfo
                                                                            showLocalFileRestoreConfirmDialog = true
                                                                        },
                                                                        modifier = Modifier.size(30.dp)
                                                                    ) {
                                                                        Icon(Icons.Default.Restore, contentDescription = "بازیابی", tint = BentoIndigoAccent, modifier = Modifier.size(18.dp))
                                                                    }
                                                                    IconButton(
                                                                        onClick = { shareLocalBackupFile(backupInfo.file) },
                                                                        modifier = Modifier.size(30.dp)
                                                                    ) {
                                                                        Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری", tint = BentoNavyDark, modifier = Modifier.size(16.dp))
                                                                    }
                                                                    IconButton(
                                                                        onClick = { localBackupToDelete = backupInfo.file },
                                                                        modifier = Modifier.size(30.dp)
                                                                    ) {
                                                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Reset / Wipe All Data Button
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.dp, ExpenseRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .clickable { showWipeConfirmDialog = true }
                                            .testTag("wipe_data_button"),
                                        color = ExpenseRed.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                                                Column {
                                                    Text("حذف تمام داده‌ها (بازنشانی سیستم)", color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("پاکسازی تمام تراکنش‌ها، اشخاص و یادداشت‌ها", color = ExpenseRed.copy(alpha = 0.8f), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("app_version_card"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.splash_logo),
                                    contentDescription = "لوگوی حسابداری شخصی آرتا",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "حسابداری شخصی آرتا • نسخه ۳.۲.۰",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // Add / Edit Currency Dialog
        if (isCurrencyDialogOpen) {
            AddEditCurrencyDialog(
                currencyToEdit = currencyToEdit,
                baseCurrencyName = currentBaseCurrency?.name ?: "ارز پایه",
                onDismiss = {
                    isCurrencyDialogOpen = false
                    currencyToEdit = null
                },
                onSave = { name, code, symbol, emoji, colorHex, rate, decimalPlaces ->
                    if (currencyToEdit != null) {
                        viewModel.updateCurrency(
                            currencyToEdit!!.copy(
                                name = name,
                                code = code.uppercase(),
                                symbol = symbol,
                                flagEmoji = emoji,
                                colorHex = colorHex,
                                exchangeRateToBase = rate,
                                decimalPlaces = decimalPlaces
                            )
                        )
                    } else {
                        viewModel.addCurrency(
                            name = name,
                            code = code.uppercase(),
                            symbol = symbol,
                            flagEmoji = emoji,
                            colorHex = colorHex,
                            rate = rate,
                            decimalPlaces = decimalPlaces
                        )
                    }
                    isCurrencyDialogOpen = false
                    currencyToEdit = null
                }
            )
        }

        // Add / Edit Category Dialog
        if (isCategoryDialogOpen) {
            AddEditCategoryDialog(
                categoryToEdit = categoryToEdit,
                onDismiss = {
                    isCategoryDialogOpen = false
                    categoryToEdit = null
                },
                onSave = { name, type, iconName, colorHex, isActive ->
                    if (categoryToEdit != null) {
                        viewModel.updateCategory(
                            categoryToEdit!!.copy(
                                name = name,
                                type = type,
                                iconName = iconName,
                                colorHex = colorHex,
                                isActive = isActive
                            )
                        )
                    } else {
                        viewModel.addCategory(
                            name = name,
                            type = type,
                            iconName = iconName,
                            colorHex = colorHex,
                            isActive = isActive
                        )
                    }
                    isCategoryDialogOpen = false
                    categoryToEdit = null
                }
            )
        }

        // Edit User Profile Dialog
        if (isProfileDialogOpen) {
            EditUserProfileDialog(
                currentName = userName,
                currentEmoji = userAvatarEmoji,
                currentColorHex = userAvatarColor,
                onDismiss = { isProfileDialogOpen = false },
                onSave = { name, emoji, colorHex ->
                    viewModel.updateProfile(name, emoji, colorHex)
                    isProfileDialogOpen = false
                }
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

        // File Restore Confirm Dialog (staged in ViewModel, survives lifecycle & process changes)
        pendingFileRestore?.let { stagedRestore ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.clearPendingRestore()
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, tint = BentoNavyDark, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("بازیابی از فایل پشتیبان", fontWeight = FontWeight.Bold, color = BentoNavyDark, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundCanvas,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = stagedRestore.fileName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = "آماده بازگردانی و ذخیره در پایگاه داده",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "هشدار: با بازیابی این فایل، تمامی اطلاعات فعلی برنامه جایگزین اطلاعات موجود در این نسخه پشتیبان خواهند شد. آیا ادامه می‌دهید؟",
                            fontSize = 12.sp,
                            color = BentoNavyDark,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.restoreBackup(
                                jsonString = stagedRestore.content.trim(),
                                onSuccess = { msg ->
                                    viewModel.clearPendingRestore()
                                    Toast.makeText(context, "اطلاعات با موفقیت از فایل بازیابی شدند.", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    viewModel.clearPendingRestore()
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأیید و بازیابی اطلاعات")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearPendingRestore()
                        }
                    ) {
                        Text("انصراف")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Backup Creation Success Dialog
        if (showBackupSuccessDialog && savedBackupInfo != null) {
            val info = savedBackupInfo!!
            AlertDialog(
                onDismissRequest = {
                    showBackupSuccessDialog = false
                    savedBackupInfo = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("فایل پشتیبان ایجاد شد", fontWeight = FontWeight.Bold, color = BentoNavyDark, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundCanvas,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("نام فایل: ${info.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoNavyDark)
                                Text("فرمت: آرشیو استاندارد آرتا (.bak)", fontSize = 11.sp, color = BentoIndigoAccent)
                                Text("حجم: ${info.sizeFormatted}", fontSize = 11.sp, color = TextSecondary)
                                Text("تاریخ و ساعت: ${info.modifiedJalali}", fontSize = 11.sp, color = TextSecondary)
                                Text("محل ذخیره: پوشه آرتا (Arta / Downloads)", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                        Text(
                            "این فایل به طور کاملاً امن و فشرده ذخیره شد و می‌توانید در هر زمان آن را در همین دستگاه یا دستگاهی دیگر بازیابی نمایید.",
                            fontSize = 11.sp,
                            color = BentoNavyDark,
                            lineHeight = 16.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val f = info.file
                            showBackupSuccessDialog = false
                            savedBackupInfo = null
                            shareLocalBackupFile(f)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("اشتراک‌گذاری فایل")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showBackupSuccessDialog = false
                            savedBackupInfo = null
                        }
                    ) {
                        Text("متوجه شدم")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Local File Restore Confirm Dialog
        if (showLocalFileRestoreConfirmDialog && pendingRestoreLocalFile != null) {
            val localFile = pendingRestoreLocalFile!!
            AlertDialog(
                onDismissRequest = {
                    showLocalFileRestoreConfirmDialog = false
                    pendingRestoreLocalFile = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("بازیابی نسخه پشتیبان محلی", fontWeight = FontWeight.Bold, color = BentoNavyDark, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundCanvas,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("نام فایل: ${localFile.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoNavyDark)
                                Text("تاریخ ایجاد: ${localFile.modifiedJalali}", fontSize = 11.sp, color = TextSecondary)
                                Text("حجم: ${localFile.sizeFormatted}", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                        Text(
                            "هشدار: تمامی داده‌های فعلی برنامه جایگزین اطلاعات این فایل پشتیبان (.bak) خواهند شد. آیا ادامه می‌دهید؟",
                            fontSize = 12.sp,
                            color = BentoNavyDark,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.restoreFromBackupFile(
                                file = localFile.file,
                                onSuccess = { msg ->
                                    showLocalFileRestoreConfirmDialog = false
                                    pendingRestoreLocalFile = null
                                    Toast.makeText(context, "اطلاعات با موفقیت از فایل بازیابی شدند.", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأیید و بازیابی اطلاعات")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLocalFileRestoreConfirmDialog = false
                            pendingRestoreLocalFile = null
                        }
                    ) {
                        Text("انصراف")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Select Base Currency Dialog
        if (showSelectBaseCurrencyDialog) {
            AlertDialog(
                onDismissRequest = { showSelectBaseCurrencyDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = BentoNavyDark, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("انتخاب ارز پایه سیستم", fontWeight = FontWeight.Bold, color = BentoNavyDark, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "واحد پولی که می‌خواهید به عنوان مبنای اصلی گزارش‌ها و محاسبات مالی قرار گیرد را انتخاب نمایید:",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeCurrencies) { curr ->
                                val isSelected = curr.isBaseCurrency
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) PrimaryTeal else BentoBorder,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            showSelectBaseCurrencyDialog = false
                                            if (!curr.isBaseCurrency) {
                                                currencyToSetBase = curr
                                                showSetBaseConfirmDialog = true
                                            }
                                        },
                                    color = if (isSelected) Color(0xFFF0FDF4) else SurfaceWhite,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = curr.flagEmoji, fontSize = 22.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "${curr.name} (${curr.code})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = BentoNavyDark
                                                )
                                                Text(
                                                    text = "نماد: ${curr.symbol}",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PrimaryTeal.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "ارز فعلی",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryTeal,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showSelectBaseCurrencyDialog = false }) {
                        Text("بستن")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Wipe Confirm Dialog
        if (showWipeConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showWipeConfirmDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف تمام داده‌ها؟", fontWeight = FontWeight.Bold, color = ExpenseRed, fontSize = 16.sp)
                    }
                },
                text = {
                    Text(
                        text = "آیا از حذف تمام داده‌ها، تراکنش‌ها، یادداشت‌ها و اطلاعات برنامه اطمینان دارید؟\nتمام اطلاعات ثبت‌شده بدون بازگشت اطلاعات پیش‌فرض پاکسازی خواهند شد.",
                        fontSize = 13.sp,
                        color = BentoNavyDark,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.wipeAllData(
                                onSuccess = {
                                    showWipeConfirmDialog = false
                                    Toast.makeText(context, "تمامی اطلاعات با موفقیت پاکسازی شدند", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    showWipeConfirmDialog = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("بله، همه داده‌ها حذف شوند")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWipeConfirmDialog = false }) {
                        Text("انصراف")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Base Currency Selection Confirmation Dialog
        if (showSetBaseConfirmDialog && currencyToSetBase != null) {
            AlertDialog(
                onDismissRequest = {
                    showSetBaseConfirmDialog = false
                    currencyToSetBase = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currencyToSetBase!!.flagEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنظیم ارز پایه جدید",
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "آیا می‌خواهید «${currencyToSetBase!!.name} (${currencyToSetBase!!.code})» به عنوان ارز پایه جدید برنامه تعیین شود؟",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoNavyDark,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "از این پس تمام گزارش‌های مالی، بیلانس‌ها و نرخ برابری ارزهای دیگر نسبت به این ارز محاسبه خواهند شد.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetCode = currencyToSetBase!!.code
                            val targetName = currencyToSetBase!!.name
                            viewModel.setBaseCurrency(
                                currencyCode = targetCode,
                                onSuccess = {
                                    showSetBaseConfirmDialog = false
                                    currencyToSetBase = null
                                    Toast.makeText(context, "ارز پایه با موفقیت به «$targetName» تغییر یافت.", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    showSetBaseConfirmDialog = false
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأیید و تنظیم ارز پایه")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSetBaseConfirmDialog = false
                        currencyToSetBase = null
                    }) {
                        Text("انصراف")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Base Currency Locked Info Dialog
        if (showBaseCurrencyLockedInfoDialog) {
            AlertDialog(
                onDismissRequest = { showBaseCurrencyLockedInfoDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ارز پایه غیرقابل تغییر است",
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "طبق اصول حسابداری، ارز پایه فقط زمانی قابل تنظیم یا تغییر است که اطلاعات وابسته به آن (مانند تراکنش، گردش مالی، مانده حساب یا حساب اشخاص) در برنامه ثبت نشده باشد.",
                            fontSize = 13.sp,
                            color = BentoNavyDark,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "در صورتی که می‌خواهید ارز پایه دیگری را انتخاب کنید، می‌توانید ابتدا گردش مالی و تراکنش‌ها را پاکسازی کنید.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showBaseCurrencyLockedInfoDialog = false
                            showClearTransactionsConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("پاکسازی گردش مالی برای تغییر ارز")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBaseCurrencyLockedInfoDialog = false }) {
                        Text("متوجه شدم")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Clear Transactions for Currency Change Dialog
        if (showClearTransactionsConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearTransactionsConfirmDialog = false },
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
                            text = "تأیید پاکسازی گردش مالی",
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed,
                            fontSize = 16.sp
                        )
                    }
                },
                text = {
                    Text(
                        text = "با انجام این عملیات تمام تراکنش‌ها پاک شده و مانده تمام کارت‌ها و اشخاص صفر می‌گردد تا بتوانید ارز پایه جدید را آزادانه تنظیم نمایید.\n(تعریف حساب‌ها، دسته‌بندی‌ها و لیست ارزها حفظ می‌شوند).",
                        fontSize = 13.sp,
                        color = BentoNavyDark,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearTransactionsAndBalancesForBaseCurrencyChange(
                                onSuccess = {
                                    showClearTransactionsConfirmDialog = false
                                    Toast.makeText(context, "گردش مالی پاکسازی شد. اکنون می‌توانید ارز پایه را تغییر دهید.", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("بله، پاکسازی شود")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearTransactionsConfirmDialog = false }) {
                        Text("انصراف")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Currency Deletion Confirmation Dialog (Requirement 18)
        currencyToDelete?.let { curr ->
            AlertDialog(
                onDismissRequest = { currencyToDelete = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف واحد پولی", fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    }
                },
                text = {
                    Text(
                        "آیا از حذف واحد پولی «${curr.name} (${curr.code})» اطمینان دارید؟",
                        color = BentoNavyDark,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCurrency(curr)
                            currencyToDelete = null
                            Toast.makeText(context, "واحد پولی حذف شد.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حذف قطعی", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { currencyToDelete = null }) {
                        Text("انصراف", color = BentoNavyDark)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Category Deletion Confirmation Dialog (Requirement 18)
        categoryToDelete?.let { cat ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف دسته‌بندی", fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    }
                },
                text = {
                    Text(
                        "آیا از حذف دسته‌بندی «${cat.name}» اطمینان دارید؟ در صورت داشتن تراکنش با این دسته‌بندی، تراکنش‌ها حفظ می‌شوند ولی این دسته‌بندی حذف خواهد شد.",
                        color = BentoNavyDark,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(cat)
                            categoryToDelete = null
                            Toast.makeText(context, "دسته‌بندی حذف شد.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حذف قطعی", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text("انصراف", color = BentoNavyDark)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }

        // Local Backup Deletion Confirmation Dialog (Requirement 18)
        localBackupToDelete?.let { file ->
            AlertDialog(
                onDismissRequest = { localBackupToDelete = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ExpenseRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف فایل پشتیبان محلی", fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    }
                },
                text = {
                    Text(
                        "آیا از حذف این فایل پشتیبان (${file.name}) اطمینان دارید؟ این عملیات غیرقابل بازگشت است.",
                        color = BentoNavyDark,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteLocalBackup(file)
                            localBackupToDelete = null
                            Toast.makeText(context, "فایل پشتیبان حذف شد.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حذف قطعی", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { localBackupToDelete = null }) {
                        Text("انصراف", color = BentoNavyDark)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = SurfaceWhite
            )
        }
    }
}

@Composable
fun CurrencyExchangeCard(
    currencies: List<CurrencyEntity>,
    currencyBalances: List<CurrencyBalanceInfo> = emptyList(),
    onExecuteExchange: (from: CurrencyEntity, to: CurrencyEntity, amount: Double, rate: Double, targetAmount: Double) -> Unit,
    successMessage: String?,
    onDismissSuccessMessage: () -> Unit
) {
    if (currencies.size < 2) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Text(
                text = "حداقل دو واحد پولی فعال برای تبادل نیاز است.",
                modifier = Modifier.padding(20.dp),
                color = TextSecondary
            )
        }
        return
    }

    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(1.coerceAtMost(currencies.lastIndex)) }
    var amountText by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }

    val fromCurrency = currencies.getOrElse(fromIndex) { currencies.first() }
    val toCurrency = currencies.getOrElse(toIndex) { currencies.last() }

    fun getAutoExchangeRate(fCurr: CurrencyEntity, tCurr: CurrencyEntity): String {
        if (fCurr.code.equals(tCurr.code, ignoreCase = true)) return "1.0"
        val r = if (tCurr.exchangeRateToBase > 0) fCurr.exchangeRateToBase / tCurr.exchangeRateToBase else 1.0
        return if (r == 1.0) "1.0" else String.format(java.util.Locale.US, "%.4f", r).trimEnd('0').trimEnd('.')
    }

    val defaultRate = remember(fromCurrency, toCurrency) {
        if (toCurrency.exchangeRateToBase > 0) {
            fromCurrency.exchangeRateToBase / toCurrency.exchangeRateToBase
        } else 1.0
    }

    var customRateText by remember { mutableStateOf(getAutoExchangeRate(fromCurrency, toCurrency)) }
    var activeExchangeCalculatorField by remember { mutableStateOf<String?>(null) }

    val activeRate = customRateText.toDoubleOrNull() ?: defaultRate
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val targetAmount = targetAmountText.toDoubleOrNull() ?: (parsedAmount * activeRate)

    val fromBalance = currencyBalances.find { (fromCurrency.id > 0 && it.currency.id == fromCurrency.id) || it.currency.code.equals(fromCurrency.code, ignoreCase = true) }?.balance ?: 0.0
    val isExchangeZeroBalance = fromBalance <= 0.0
    val isExchangeExceeding = parsedAmount > fromBalance
    val isExchangeValid = parsedAmount > 0.0 && ((fromCurrency.id > 0 && toCurrency.id > 0 && fromCurrency.id != toCurrency.id) || fromCurrency.code != toCurrency.code) && !isExchangeZeroBalance && !isExchangeExceeding

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("currency_exchange_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "صرافی و تبادل اسعار",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BentoNavyDark
            )
            Text(
                text = "تبدیل موجودی میان حساب‌های ارزی مختلف بدون تغییر در سایر ارزها",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Success Message Banner
            AnimatedVisibility(
                visible = successMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (successMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = IncomeGreen.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = successMessage,
                                fontSize = 12.sp,
                                color = IncomeGreen,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // From Currency Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF9FAFD))
                    .border(1.dp, BentoBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "از ارز (برداشت)",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "موجودی: ${fromCurrency.symbol} ${String.format(java.util.Locale.US, "%,.2f", fromBalance)}",
                        fontSize = 11.sp,
                        color = if (fromBalance > 0.0) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Currency selector pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(currencies.indices.toList()) { idx ->
                            val c = currencies[idx]
                            val isSel = idx == fromIndex
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) BentoNavyDark else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder),
                                modifier = Modifier
                                    .clickable {
                                        fromIndex = idx
                                        val newFrom = currencies.getOrElse(idx) { currencies.first() }
                                        val autoRateStr = getAutoExchangeRate(newFrom, toCurrency)
                                        customRateText = autoRateStr
                                        val pAmt = amountText.toDoubleOrNull() ?: 0.0
                                        val r = autoRateStr.toDoubleOrNull() ?: 1.0
                                        if (pAmt > 0) {
                                            targetAmountText = String.format(java.util.Locale.US, "%.2f", pAmt * r).trimEnd('0').trimEnd('.')
                                        }
                                    }
                                    .testTag("exchange_from_${c.code}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = c.flagEmoji, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = c.code,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else BentoNavyDark
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newVal ->
                        amountText = newVal
                        val pAmt = newVal.toDoubleOrNull() ?: 0.0
                        val r = customRateText.toDoubleOrNull() ?: defaultRate
                        if (pAmt > 0) {
                            targetAmountText = String.format(java.util.Locale.US, "%.2f", pAmt * r).trimEnd('0').trimEnd('.')
                        } else if (newVal.isBlank()) {
                            targetAmountText = ""
                        }
                    },
                    label = { Text("مبلغ پرداختی (${fromCurrency.symbol})", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exchange_from_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        cursorColor = BentoNavyDark,
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedLabelColor = BentoNavyDark,
                        unfocusedLabelColor = TextSecondary
                    ),
                    trailingIcon = {
                        CalculatorMiniButton(
                            onClick = { activeExchangeCalculatorField = "from_amount" },
                            contentDescription = "ماشین‌حساب مبلغ پرداختی"
                        )
                    }
                )
            }

            // Swap Button in Center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromIndex
                        fromIndex = toIndex
                        toIndex = temp
                        val newFrom = currencies.getOrElse(fromIndex) { currencies.first() }
                        val newTo = currencies.getOrElse(toIndex) { currencies.last() }
                        val autoRateStr = getAutoExchangeRate(newFrom, newTo)
                        customRateText = autoRateStr
                        val pAmt = amountText.toDoubleOrNull() ?: 0.0
                        val r = autoRateStr.toDoubleOrNull() ?: 1.0
                        if (pAmt > 0) {
                            targetAmountText = String.format(java.util.Locale.US, "%.2f", pAmt * r).trimEnd('0').trimEnd('.')
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BentoNavyDark)
                        .testTag("swap_exchange_currencies_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "جابجایی ارزها",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // To Currency Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF9FAFD))
                    .border(1.dp, BentoBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "به ارز (واریز)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currencies.indices.toList()) { idx ->
                        val c = currencies[idx]
                        val isSel = idx == toIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) Color(c.colorHex) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color(c.colorHex) else BentoBorder),
                            modifier = Modifier
                                .clickable {
                                    toIndex = idx
                                    val newTo = currencies.getOrElse(idx) { currencies.last() }
                                    val autoRateStr = getAutoExchangeRate(fromCurrency, newTo)
                                    customRateText = autoRateStr
                                    val pAmt = amountText.toDoubleOrNull() ?: 0.0
                                    val r = autoRateStr.toDoubleOrNull() ?: 1.0
                                    if (pAmt > 0) {
                                        targetAmountText = String.format(java.util.Locale.US, "%.2f", pAmt * r).trimEnd('0').trimEnd('.')
                                    }
                                }
                                .testTag("exchange_to_${c.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = c.flagEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = c.code,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Editable Target (Received) Amount Field
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { newVal ->
                        targetAmountText = newVal
                        val pTarget = newVal.toDoubleOrNull()
                        if (pTarget != null && parsedAmount > 0.0) {
                            val computedRate = pTarget / parsedAmount
                            customRateText = String.format(java.util.Locale.US, "%.4f", computedRate)
                        }
                    },
                    placeholder = {
                        Text(
                            text = if (parsedAmount > 0) String.format(java.util.Locale.US, "%,.2f", parsedAmount * activeRate) else "مبلغ دریافتی",
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    },
                    label = { Text("مبلغ دریافتی (${toCurrency.symbol})", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exchange_target_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IncomeGreen,
                        unfocusedTextColor = IncomeGreen,
                        cursorColor = IncomeGreen,
                        focusedBorderColor = IncomeGreen,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite,
                        focusedLabelColor = IncomeGreen,
                        unfocusedLabelColor = TextSecondary
                    ),
                    trailingIcon = {
                        CalculatorMiniButton(
                            onClick = { activeExchangeCalculatorField = "target_amount" },
                            contentDescription = "ماشین‌حساب مبلغ دریافتی"
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rate calculation & customization
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "نرخ تبدیل: ۱ ${fromCurrency.code} = ${String.format(java.util.Locale.US, "%.4f", activeRate)} ${toCurrency.code}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = BentoNavyDark
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customRateText,
                    onValueChange = { newVal ->
                        customRateText = newVal
                        val newRate = newVal.toDoubleOrNull() ?: defaultRate
                        if (parsedAmount > 0.0) {
                            targetAmountText = String.format(java.util.Locale.US, "%.2f", parsedAmount * newRate)
                        }
                    },
                    placeholder = { Text(String.format(java.util.Locale.US, "%.4f", defaultRate), fontSize = 12.sp) },
                    label = { Text("تنظیم دلخواه نرخ تبدیل (اختیاری)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_exchange_rate_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    trailingIcon = {
                        CalculatorMiniButton(
                            onClick = { activeExchangeCalculatorField = "custom_rate" },
                            contentDescription = "ماشین‌حساب نرخ تبدیل"
                        )
                    }
                )
            }

            if (isExchangeZeroBalance) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExpenseRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text(
                        text = "موجودی ارز مبدا صفر است و امکان تبادل وجود ندارد.",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else if (isExchangeExceeding) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExpenseRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text(
                        text = "مبلغ تبادل نمی‌تواند بیشتر از موجودی (${fromCurrency.symbol} ${String.format(java.util.Locale.US, "%,.2f", fromBalance)}) باشد.",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Exchange Button
            Button(
                onClick = {
                    if (isExchangeValid) {
                        onExecuteExchange(fromCurrency, toCurrency, parsedAmount, activeRate, targetAmount)
                        amountText = ""
                        targetAmountText = ""
                        customRateText = ""
                    }
                },
                enabled = isExchangeValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_exchange_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoNavyDark,
                    disabledContainerColor = Color(0xFFE5E7EB)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ثبت تبادل و به‌روزرسانی بیلانس‌ها",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    activeExchangeCalculatorField?.let { field ->
        val initVal = when (field) {
            "from_amount" -> amountText
            "target_amount" -> targetAmountText
            "custom_rate" -> customRateText
            else -> ""
        }
        val title = when (field) {
            "from_amount" -> "محاسبه مبلغ پرداختی"
            "target_amount" -> "محاسبه مبلغ دریافتی"
            "custom_rate" -> "محاسبه نرخ تبدیل"
            else -> "ماشین‌حساب"
        }

        MinimalCalculatorDialog(
            initialValue = initVal,
            title = title,
            onConfirm = { calcVal ->
                when (field) {
                    "from_amount" -> {
                        amountText = calcVal
                        val pAmt = calcVal.toDoubleOrNull() ?: 0.0
                        if (pAmt > 0.0) {
                            targetAmountText = String.format(java.util.Locale.US, "%.2f", pAmt * activeRate)
                        }
                    }
                    "target_amount" -> {
                        targetAmountText = calcVal
                        val pTarget = calcVal.toDoubleOrNull()
                        if (pTarget != null && parsedAmount > 0.0) {
                            val computedRate = pTarget / parsedAmount
                            customRateText = String.format(java.util.Locale.US, "%.4f", computedRate)
                        }
                    }
                    "custom_rate" -> {
                        customRateText = calcVal
                        val newRate = calcVal.toDoubleOrNull() ?: defaultRate
                        if (parsedAmount > 0.0) {
                            targetAmountText = String.format(java.util.Locale.US, "%.2f", parsedAmount * newRate)
                        }
                    }
                }
            },
            onDismiss = { activeExchangeCalculatorField = null }
        )
    }
}

@Composable
fun AddEditCurrencyDialog(
    currencyToEdit: CurrencyEntity?,
    baseCurrencyName: String = "ارز پایه",
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, symbol: String, emoji: String, colorHex: Long, rate: Double, decimalPlaces: Int) -> Unit
) {
    var name by remember { mutableStateOf(currencyToEdit?.name ?: "") }
    var code by remember { mutableStateOf(currencyToEdit?.code ?: "") }
    var symbol by remember { mutableStateOf(currencyToEdit?.symbol ?: "") }
    var emoji by remember { mutableStateOf(currencyToEdit?.flagEmoji ?: "💵") }
    var rateText by remember { mutableStateOf(currencyToEdit?.exchangeRateToBase?.toString() ?: "1.0") }
    var selectedColorHex by remember { mutableStateOf(currencyToEdit?.colorHex ?: 0xFF3B82F6) }
    var decimalPlaces by remember {
        mutableIntStateOf(
            currencyToEdit?.decimalPlaces ?: if (currencyToEdit?.code == "AFN" || currencyToEdit?.code == "IRR" || currencyToEdit?.code == "PKR") 0 else 3
        )
    }
    var showCurrencyRateCalculator by remember { mutableStateOf(false) }

    val emojis = listOf("🇦🇫", "🇺🇸", "🇪🇺", "🇮🇷", "🇵🇰", "🇬🇧", "🇦🇪", "🇹🇷", "🇨🇳", "🇸🇦", "💵", "🪙", "💳")
    val colors = listOf(0xFF10B981, 0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0xFFF59E0B, 0xFFEF4444, 0xFF06B6D4, 0xFF6366F1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currencyToEdit == null) "افزودن واحد پولی جدید" else "ویرایش واحد پولی",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = BentoNavyDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_edit_currency_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام ارز (مانند: دالر کانادا)") },
                    modifier = Modifier.fillMaxWidth().testTag("currency_input_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        cursorColor = BentoNavyDark,
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoNavyDark,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("کد (CAD)") },
                        modifier = Modifier.weight(1f).testTag("currency_input_code"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoNavyDark,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("نماد ($)") },
                        modifier = Modifier.weight(1f).testTag("currency_input_symbol"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoNavyDark,
                            unfocusedTextColor = BentoNavyDark,
                            cursorColor = BentoNavyDark,
                            focusedBorderColor = BentoNavyDark,
                            unfocusedBorderColor = BentoBorder,
                            focusedLabelColor = BentoNavyDark,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                }

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = {
                        Text(
                            if (currencyToEdit?.isBaseCurrency == true)
                                "نرخ برابری (ارز پایه = ۱٫۰)"
                            else
                                "نرخ برابری نسبت به $baseCurrencyName"
                        )
                    },
                    readOnly = currencyToEdit?.isBaseCurrency == true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("currency_input_rate"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        cursorColor = BentoNavyDark,
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoNavyDark,
                        unfocusedLabelColor = TextSecondary
                    ),
                    trailingIcon = {
                        if (currencyToEdit?.isBaseCurrency != true) {
                            CalculatorMiniButton(
                                onClick = { showCurrencyRateCalculator = true },
                                contentDescription = "ماشین‌حساب نرخ برابری"
                            )
                        }
                    }
                )

                Text(
                    text = "ارقام اعشار در نمایش و محاسبات:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0 to "بدون اعشار (۰)",
                        1 to "۱ رقم (.۰)",
                        2 to "۲ رقم (.۰۰)",
                        3 to "۳ رقم (.۰۰۰)"
                    ).forEach { (dec, label) ->
                        val isSel = decimalPlaces == dec
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { decimalPlaces = dec },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BentoNavyDark else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSel) BentoNavyDark else BentoBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "انتخاب آیکون / پرچم:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(emojis) { em ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (emoji == em) BentoNavyDark.copy(alpha = 0.15f) else Color(0xFFF3F4F6))
                                .border(1.dp, if (emoji == em) BentoNavyDark else BentoBorder, RoundedCornerShape(8.dp))
                                .clickable { emoji = em },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = em, fontSize = 18.sp)
                        }
                    }
                }

                Text(
                    text = "رنگ نشانگر:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { col ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(col))
                                .border(2.dp, if (selectedColorHex == col) BentoNavyDark else Color.Transparent, CircleShape)
                                .clickable { selectedColorHex = col },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorHex == col) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateText.toDoubleOrNull() ?: 1.0
                    if (name.isNotBlank() && code.isNotBlank()) {
                        onSave(name, code, symbol.ifBlank { code }, emoji, selectedColorHex, rate, decimalPlaces)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                modifier = Modifier.testTag("save_currency_button")
            ) {
                Text("ذخیره", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )

    if (showCurrencyRateCalculator) {
        MinimalCalculatorDialog(
            initialValue = rateText,
            title = "محاسبه نرخ برابری ارز",
            onConfirm = { calcVal ->
                rateText = calcVal
            },
            onDismiss = { showCurrencyRateCalculator = false }
        )
    }
}

@Composable
fun CategoryManageRowItem(
    category: CategoryEntity,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    SwipeToRevealActionsLayout(
        onEdit = onEdit,
        onDelete = onDelete
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (category.isActive) SurfaceWhite else Color(0xFFF9FAFB)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (category.isActive) BentoBorder else Color(0xFFE5E7EB)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Category Icon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (category.isActive) Color(category.colorHex).copy(alpha = 0.15f)
                                else Color.LightGray.copy(alpha = 0.3f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when (category.iconName) {
                            "Fastfood" -> Icons.Default.Fastfood
                            "ShoppingBag" -> Icons.Default.ShoppingBag
                            "ReceiptLong" -> Icons.Default.ReceiptLong
                            "DirectionsCar" -> Icons.Default.DirectionsCar
                            "PhoneAndroid" -> Icons.Default.PhoneAndroid
                            "Subscriptions" -> Icons.Default.Subscriptions
                            else -> Icons.Default.Category
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = if (category.isActive) Color(category.colorHex) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = category.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (category.isActive) BentoNavyDark else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Type Badge
                            val isExpense = category.type == TransactionType.EXPENSE
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isExpense) Color(0xFFFFE4E6) else Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isExpense) "مصرف" else "عاید",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpense) ExpenseRed else IncomeGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (category.isActive) "وضعیت: فعال (بکشید برای ویرایش/حذف)" else "وضعیت: غیرفعال",
                            fontSize = 11.sp,
                            color = if (category.isActive) IncomeGreen else TextSecondary
                        )
                    }
                }

                // Active status switch
                Switch(
                    checked = category.isActive,
                    onCheckedChange = { onToggleStatus() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = BentoNavyDark,
                        checkedThumbColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun AddEditCategoryDialog(
    categoryToEdit: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TransactionType, iconName: String, colorHex: Long, isActive: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(categoryToEdit?.type ?: TransactionType.EXPENSE) }
    var selectedIcon by remember { mutableStateOf(categoryToEdit?.iconName ?: "ShoppingBag") }
    var selectedColorHex by remember { mutableStateOf(categoryToEdit?.colorHex ?: 0xFF3B82F6) }
    var isActive by remember { mutableStateOf(categoryToEdit?.isActive ?: true) }

    val iconOptions = listOf(
        "ShoppingBag" to "خرید",
        "Fastfood" to "غذا",
        "ReceiptLong" to "قبوض/خانه",
        "DirectionsCar" to "ترانسپورت",
        "PhoneAndroid" to "ارتباطات",
        "Subscriptions" to "اشتراک",
        "Category" to "عمومی"
    )

    val colorOptions = listOf(
        0xFFEA580C, // Orange
        0xFF9333EA, // Purple
        0xFFD97706, // Amber
        0xFF4F46E5, // Indigo
        0xFF0284C7, // Blue
        0xFFDB2777, // Pink
        0xFF10B981, // Green
        0xFF64748B  // Slate
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (categoryToEdit != null) "ویرایش دسته‌بندی" else "افزودن دسته‌بندی جدید",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = BentoNavyDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام دسته‌بندی (مثال: ناهار، معاش)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    singleLine = true
                )

                // Type selection: Expense vs Income
                Column {
                    Text("نوع دسته‌بندی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    if (selectedType == TransactionType.EXPENSE) ExpenseRed else BentoBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedType = TransactionType.EXPENSE },
                            color = if (selectedType == TransactionType.EXPENSE) Color(0xFFFFE4E6) else SurfaceWhite
                        ) {
                            Text(
                                text = "مصرف",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else BentoNavyDark,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    if (selectedType == TransactionType.INCOME) IncomeGreen else BentoBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedType = TransactionType.INCOME },
                            color = if (selectedType == TransactionType.INCOME) Color(0xFFDCFCE7) else SurfaceWhite
                        ) {
                            Text(
                                text = "عاید",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == TransactionType.INCOME) IncomeGreen else BentoNavyDark,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Icon Selection
                Column {
                    Text("انتخاب آیکون:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(iconOptions) { (iconKey, label) ->
                            val isSel = selectedIcon == iconKey
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.5.dp,
                                        if (isSel) BentoNavyDark else BentoBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIcon = iconKey },
                                color = if (isSel) BentoNavyDark else SurfaceWhite
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val iconVec = when (iconKey) {
                                        "Fastfood" -> Icons.Default.Fastfood
                                        "ShoppingBag" -> Icons.Default.ShoppingBag
                                        "ReceiptLong" -> Icons.Default.ReceiptLong
                                        "DirectionsCar" -> Icons.Default.DirectionsCar
                                        "PhoneAndroid" -> Icons.Default.PhoneAndroid
                                        "Subscriptions" -> Icons.Default.Subscriptions
                                        else -> Icons.Default.Category
                                    }
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = null,
                                        tint = if (isSel) Color.White else BentoNavyDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.White else BentoNavyDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Color Selection
                Column {
                    Text("انتخاب رنگ تم:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorOptions.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(2.dp, if (selectedColorHex == col) BentoNavyDark else Color.Transparent, CircleShape)
                                    .clickable { selectedColorHex = col },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorHex == col) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isActive = !isActive }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("وضعیت فعال بودن دسته:", fontSize = 12.sp, color = BentoNavyDark)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = BentoNavyDark)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedType, selectedIcon, selectedColorHex, isActive)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
            ) {
                Text("ذخیره دسته‌بندی", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EditUserProfileDialog(
    currentName: String,
    currentEmoji: String,
    currentColorHex: Long,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var emoji by remember { mutableStateOf(currentEmoji) }
    var selectedColorHex by remember { mutableStateOf(currentColorHex) }

    val colorOptions = listOf(
        0xFF1E293B, // Navy Dark
        0xFF4F46E5, // Indigo
        0xFF0284C7, // Sky Blue
        0xFF10B981, // Emerald Green
        0xFF9333EA, // Violet
        0xFFD97706, // Amber
        0xFFDC2626  // Red
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ویرایش مشخصات حساب کاربری",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = BentoNavyDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("ایموجی آواتار (اختیاری، مثلاً 👤، 💼، 👑)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoNavyDark,
                        unfocusedTextColor = BentoNavyDark,
                        focusedBorderColor = BentoNavyDark,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    singleLine = true
                )

                Column {
                    Text("انتخاب رنگ پس‌زمینه آواتار:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorOptions.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(2.dp, if (selectedColorHex == col) BentoNavyDark else Color.Transparent, CircleShape)
                                    .clickable { selectedColorHex = col },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorHex == col) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), emoji.trim(), selectedColorHex)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
            ) {
                Text("ذخیره تغییرات", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = TextSecondary)
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp)
    )
}
