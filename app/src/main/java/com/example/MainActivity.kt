package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.NavTab
import com.example.data.local.QuickActionEntity
import com.example.ui.components.QuickActionBubblesOverlay
import com.example.ui.components.QuickActionExecutionDialog
import com.example.ui.components.AllTransactionsScreen
import com.example.ui.components.consolidateTransactions
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalculatorTransactionScreen
import com.example.ui.screens.AppLockScreen
import com.example.ui.screens.CardsScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuickActionsManagementScreen
import com.example.ui.screens.RecipientsScreen
import com.example.ui.screens.SettingsSection
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShoppingListsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedBg
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextSecondary

class MainActivity : FragmentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun attachBaseContext(newBase: android.content.Context?) {
        if (newBase == null) {
            super.attachBaseContext(null)
            return
        }
        try {
            val locale = java.util.Locale("fa")
            java.util.Locale.setDefault(locale)
            val config = android.content.res.Configuration(newBase.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            val context = newBase.createConfigurationContext(config)
            super.attachBaseContext(context)
        } catch (e: Exception) {
            super.attachBaseContext(newBase)
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.onAppResumed()
            viewModel.refreshBiometricStatus()
        } catch (_: Exception) {}
    }

    override fun onPause() {
        super.onPause()
        try {
            viewModel.onAppBackgrounded()
        } catch (_: Exception) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MyApplicationTheme {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var previousTabBeforeAnalytics by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var initialSettingsSection by rememberSaveable { mutableStateOf(SettingsSection.CURRENCIES) }
    var isShoppingListsOpen by remember { mutableStateOf(false) }
    var initialShoppingListId by remember { mutableStateOf<Long?>(null) }
    var isQuickActionsManagementOpen by remember { mutableStateOf(false) }
    var isAllTransactionsOpen by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var isQuickActionBubblesOpen by remember { mutableStateOf(false) }
    var isPersonStatementOpen by rememberSaveable { mutableStateOf(false) }

    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val isScreenSecurityEnabled by viewModel.isScreenSecurityEnabled.collectAsState()

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val fragmentActivity = context as? FragmentActivity
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    // Screen security (FLAG_SECURE) handling - Disabled in emulator/DEBUG so streaming emulator displays properly
    LaunchedEffect(isScreenSecurityEnabled, fragmentActivity) {
        if (fragmentActivity != null) {
            val isEmulator = android.os.Build.FINGERPRINT.startsWith("generic")
                || android.os.Build.FINGERPRINT.startsWith("unknown")
                || android.os.Build.MODEL.contains("google_sdk")
                || android.os.Build.MODEL.contains("Emulator")
                || android.os.Build.MODEL.contains("Android SDK built for x86")
                || (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                || "google_sdk" == android.os.Build.PRODUCT

            if (isScreenSecurityEnabled && !isEmulator && !BuildConfig.DEBUG) {
                fragmentActivity.window.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                fragmentActivity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    // کلید برگشت گوشی: اگر روی صفحه اصلی بود برای خروج دوباره بپرسد، در غیر این صورت به صفحه اصلی بازگردد
    BackHandler {
        if (showSplash) {
            activity?.finish()
        } else if (isAppLocked) {
            activity?.finish()
        } else if (isQuickActionBubblesOpen) {
            isQuickActionBubblesOpen = false
        } else if (isQuickActionsManagementOpen) {
            isQuickActionsManagementOpen = false
        } else if (isAllTransactionsOpen) {
            isAllTransactionsOpen = false
        } else if (isSettingsOpen) {
            isSettingsOpen = false
        } else if (isShoppingListsOpen) {
            isShoppingListsOpen = false
            initialShoppingListId = null
        } else if (currentTab == NavTab.ANALYTICS) {
            currentTab = previousTabBeforeAnalytics
        } else if (currentTab != NavTab.HOME) {
            currentTab = NavTab.HOME
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000L) {
                activity?.finish()
            } else {
                lastBackPressTime = currentTime
                Toast.makeText(context, "برای خروج دوباره بزنید", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            fadeIn(animationSpec = tween(350)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "splash_screen_transition"
    ) { isSplashVisible ->
        if (isSplashVisible) {
            SplashScreen(
                onSplashFinished = { showSplash = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(BackgroundCanvas)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                if (isSettingsOpen) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { isSettingsOpen = false },
                        onNavigateToBudget = {
                            isSettingsOpen = false
                            currentTab = NavTab.ANALYTICS
                        },
                        initialSection = initialSettingsSection
                    )
                } else if (isShoppingListsOpen) {
                    ShoppingListsScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            isShoppingListsOpen = false
                            initialShoppingListId = null
                        },
                        initialListIdToOpen = initialShoppingListId,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isQuickActionsManagementOpen) {
                    QuickActionsManagementScreen(
                        viewModel = viewModel,
                        onNavigateBack = { isQuickActionsManagementOpen = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (isAllTransactionsOpen) {
                    val navAccountsAll by viewModel.accounts.collectAsState()
                    val navCurrenciesAll by viewModel.activeCurrencies.collectAsState()
                    val navCurrenciesList by viewModel.currencies.collectAsState()
                    val navCategoriesAll by viewModel.categories.collectAsState()
                    val navRecipientsAll by viewModel.recipients.collectAsState()
                    val baseCurrSymbol = navCurrenciesList.firstOrNull { it.isBaseCurrency }?.symbol ?: "؋"
                    val allTxns by viewModel.allTransactions.collectAsState()
                    val consolidatedTxns = remember(allTxns) {
                        consolidateTransactions(allTxns, allTxns)
                    }
                    AllTransactionsScreen(
                        consolidatedTransactions = consolidatedTxns,
                        accounts = navAccountsAll,
                        activeCurrencies = navCurrenciesAll,
                        categories = navCategoriesAll,
                        recipients = navRecipientsAll,
                        currencySymbol = baseCurrSymbol,
                        viewModel = viewModel,
                        onDismiss = { isAllTransactionsOpen = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Screen Content with Smooth Horizontal Connected Transition
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            val tabOrder = mapOf(
                                NavTab.ANALYTICS to 0,
                                NavTab.GOALS to 1,
                                NavTab.CARDS to 2,
                                NavTab.HOME to 3,
                                NavTab.CALCULATOR to 4,
                                NavTab.RECIPIENTS to 5
                            )
                            val initialOrder = tabOrder[initialState] ?: 1
                            val targetOrder = tabOrder[targetState] ?: 1

                            if (targetOrder > initialOrder) {
                                // Slide leftwards (moving to the right screen)
                                (slideInHorizontally(animationSpec = tween(320)) { width -> width } + fadeIn(animationSpec = tween(320)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(320)) { width -> -width } + fadeOut(animationSpec = tween(320)))
                            } else {
                                // Slide rightwards (moving to the left screen)
                                (slideInHorizontally(animationSpec = tween(320)) { width -> -width } + fadeIn(animationSpec = tween(320)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(320)) { width -> width } + fadeOut(animationSpec = tween(320)))
                            }
                        },
                        label = "tab_transition",
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        when (tab) {
                            NavTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateTab = {
                                    if (it == NavTab.ANALYTICS) {
                                        previousTabBeforeAnalytics = currentTab
                                    }
                                    currentTab = it
                                },
                                onNavigateToSettings = { isSettingsOpen = true },
                                onNavigateToSettingsSection = { section ->
                                    initialSettingsSection = section
                                    isSettingsOpen = true
                                },
                                onNavigateToShoppingLists = { listId ->
                                    initialShoppingListId = listId
                                    isShoppingListsOpen = true
                                },
                                onNavigateToQuickActionsManagement = {
                                    isQuickActionsManagementOpen = true
                                },
                                onNavigateToAllTransactions = {
                                    isAllTransactionsOpen = true
                                }
                            )
                            NavTab.CARDS -> CardsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = NavTab.HOME }
                            )
                            NavTab.CALCULATOR -> CalculatorTransactionScreen(
                                viewModel = viewModel,
                                onTransactionCompleted = { currentTab = NavTab.HOME },
                                onNavigateToSettings = { isSettingsOpen = true }
                            )
                            NavTab.RECIPIENTS -> RecipientsScreen(
                                viewModel = viewModel,
                                onNavigateTab = {
                                    if (it == NavTab.ANALYTICS) {
                                        previousTabBeforeAnalytics = currentTab
                                    }
                                    currentTab = it
                                },
                                onNavigateToShoppingLists = { listId ->
                                    initialShoppingListId = listId
                                    isShoppingListsOpen = true
                                },
                                onNavigateToSettings = { isSettingsOpen = true },
                                onStatementVisibilityChanged = { isPersonStatementOpen = it }
                            )
                            NavTab.ANALYTICS -> AnalyticsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = previousTabBeforeAnalytics },
                                onNavigateToShoppingLists = { listId ->
                                    initialShoppingListId = listId
                                    isShoppingListsOpen = true
                                }
                            )
                            NavTab.GOALS -> GoalsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentTab = NavTab.HOME }
                            )
                        }
                    }

                    val navAccounts by viewModel.accounts.collectAsState()
                    val allQuickActions by viewModel.allQuickActions.collectAsState()

                    var executingNavQuickAction by remember { mutableStateOf<QuickActionEntity?>(null) }

                    // Quick Action Bubbles Overlay (نمایش حباب‌های عملیات سریع با فشردن چند ثانیه‌ای هوم)
                    if (isQuickActionBubblesOpen) {
                        QuickActionBubblesOverlay(
                            actions = allQuickActions,
                            accounts = navAccounts,
                            onSelectAction = { action ->
                                isQuickActionBubblesOpen = false
                                executingNavQuickAction = action
                            },
                            onDismiss = { isQuickActionBubblesOpen = false }
                        )
                    }

                    // Floating Bottom Navigation Bar (در صفحات جانبی مثل بودجه، تحلیل مالی و صورتحساب اشخاص مخفی می‌گردد)
                    if (currentTab != NavTab.ANALYTICS && !isPersonStatementOpen) {
                        FloatingBottomNavBar(
                            currentTab = currentTab,
                            onTabSelected = {
                                isPersonStatementOpen = false
                                if (it == NavTab.ANALYTICS) {
                                    previousTabBeforeAnalytics = currentTab
                                }
                                currentTab = it
                            },
                            onHomeLongClick = { isQuickActionBubblesOpen = true },
                            isHomePlusActive = isQuickActionBubblesOpen,
                            onHomePlusClick = { isQuickActionBubblesOpen = false },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    executingNavQuickAction?.let { action ->
                        QuickActionExecutionDialog(
                            quickAction = action,
                            viewModel = viewModel,
                            accounts = navAccounts,
                            onDismiss = { executingNavQuickAction = null },
                            onSuccess = { executingNavQuickAction = null }
                        )
                    }

                    val opError by viewModel.operationErrorMessage.collectAsState()
                    val currentOpError = opError
                    if (currentOpError != null) {
                        AlertDialog(
                            onDismissRequest = { viewModel.clearOperationError() },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(ExpenseRedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            title = {
                                val errorTitle = if (currentOpError.contains("حذف")) "عدم امکان حذف" else "خطا در عملیات مالی"
                                Text(
                                    text = errorTitle,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark,
                                    fontSize = 17.sp,
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    text = currentOpError,
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Center
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { viewModel.clearOperationError() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("متوجه شدم", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            containerColor = SurfaceWhite
                        )
                    }
                }

                if (isAppLocked) {
                    AppLockScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
