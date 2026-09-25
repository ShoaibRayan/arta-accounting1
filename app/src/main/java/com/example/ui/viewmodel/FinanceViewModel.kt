package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AccountCardEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.FinancialGoalEntity
import com.example.data.local.GoalReminderFrequency
import com.example.data.local.GoalStatus
import com.example.data.local.GoalTransactionEntity
import com.example.data.local.GoalTransactionType
import com.example.data.local.QuickActionAmountBehavior
import com.example.data.local.QuickActionEntity
import com.example.data.local.QuickActionType
import com.example.data.local.RecipientEntity
import com.example.data.local.ShoppingListEntity
import com.example.data.local.ShoppingListItemEntity
import com.example.data.local.ShoppingListWithItems
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionKind
import com.example.data.local.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale
import java.io.File
import com.example.util.AutoLockDuration
import com.example.util.BiometricStatus
import com.example.util.LockType
import com.example.util.SecurityManager
import com.example.util.BackupFileInfo
import com.example.util.BackupManager
import com.example.util.PersianDateHelper

data class CurrencyBalanceInfo(
    val currency: CurrencyEntity,
    val balance: Double,
    val totalIncome: Double,
    val totalExpense: Double
)

data class CurrencyGoalSummary(
    val currencyCode: String,
    val currencySymbol: String,
    val totalRealBalance: Double,
    val totalAllocatedToGoals: Double,
    val availableFreeBalance: Double,
    val activeGoalsCount: Int,
    val completedGoalsCount: Int
)

class FinanceViewModel @JvmOverloads constructor(
    application: Application,
    injectedRepository: FinanceRepository? = null
) : AndroidViewModel(application) {

    private val repository: FinanceRepository = injectedRepository ?: run {
        val db = AppDatabase.getDatabase(application)
        FinanceRepository(
            db,
            db.transactionDao(),
            db.accountDao(),
            db.recipientDao(),
            db.currencyDao(),
            db.categoryDao(),
            db.budgetDao(),
            db.goalDao(),
            db.goalTransactionDao(),
            db.quickActionDao(),
            db.shoppingListDao(),
            db.shoppingListItemDao()
        )
    }

    private val prefs = application.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
    private val _userName = MutableStateFlow(
        prefs.getString("user_name", null)?.let { saved ->
            if (saved.isBlank() || saved == "محمد شعیب رایان") "مهمان" else saved
        } ?: "مهمان"
    )
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userAvatarEmoji = MutableStateFlow(prefs.getString("user_avatar_emoji", "👤") ?: "👤")
    val userAvatarEmoji: StateFlow<String> = _userAvatarEmoji.asStateFlow()

    private val _userAvatarColor = MutableStateFlow(prefs.getLong("user_avatar_color", 0xFF6366F1))
    val userAvatarColor: StateFlow<Long> = _userAvatarColor.asStateFlow()

    // --- Security & Biometrics ---
    val securityManager = SecurityManager(application)

    private val _isAppLockEnabled = MutableStateFlow(securityManager.isAppLockEnabled() && securityManager.hasPasscode())
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _lockType = MutableStateFlow(securityManager.getLockType())
    val lockType: StateFlow<LockType> = _lockType.asStateFlow()

    private val _hasPasscode = MutableStateFlow(securityManager.hasPasscode())
    val hasPasscode: StateFlow<Boolean> = _hasPasscode.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _autoLockDuration = MutableStateFlow(securityManager.getAutoLockDuration())
    val autoLockDuration: StateFlow<AutoLockDuration> = _autoLockDuration.asStateFlow()

    private val _isScreenSecurityEnabled = MutableStateFlow(securityManager.isScreenSecurityEnabled())
    val isScreenSecurityEnabled: StateFlow<Boolean> = _isScreenSecurityEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(securityManager.isAppLockEnabled() && securityManager.hasPasscode())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _biometricStatus = MutableStateFlow(securityManager.checkBiometricStatus())
    val biometricStatus: StateFlow<BiometricStatus> = _biometricStatus.asStateFlow()

    // Protected recipient amounts temporarily unmasked in the current active session
    private val _unmaskedRecipientIds = MutableStateFlow<Set<Long>>(emptySet())
    val unmaskedRecipientIds: StateFlow<Set<Long>> = _unmaskedRecipientIds.asStateFlow()

    fun unmaskRecipient(recipientId: Long) {
        _unmaskedRecipientIds.update { it + recipientId }
    }

    fun maskRecipient(recipientId: Long) {
        _unmaskedRecipientIds.update { it - recipientId }
    }

    fun maskAllRecipients() {
        _unmaskedRecipientIds.value = emptySet()
    }

    fun isRecipientMasked(recipientId: Long): Boolean {
        val rec = recipients.value.firstOrNull { it.id == recipientId } ?: return false
        return rec.isAmountProtected && !_unmaskedRecipientIds.value.contains(recipientId)
    }

    fun isRecipientMaskedByName(name: String?): Boolean {
        if (name.isNullOrBlank()) return false
        val rec = recipients.value.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: return false
        return isRecipientMasked(rec.id)
    }

    fun isTransactionAmountMasked(transaction: TransactionEntity): Boolean {
        // 1. Direct recipientId
        if (transaction.recipientId != null && transaction.recipientId > 0L) {
            if (isRecipientMasked(transaction.recipientId)) return true
        }
        // 2. Direct recipientName
        if (!transaction.recipientName.isNullOrBlank()) {
            if (isRecipientMaskedByName(transaction.recipientName)) return true
        }
        // 3. Related transaction check (e.g. transfers, paired transactions, exchange legs)
        val relId = transaction.relatedTransactionId
        if (relId != null && relId > 0L) {
            val relTxn = transactions.value.firstOrNull { it.id == relId }
            if (relTxn != null) {
                if (relTxn.recipientId != null && relTxn.recipientId > 0L && isRecipientMasked(relTxn.recipientId)) return true
                if (!relTxn.recipientName.isNullOrBlank() && isRecipientMaskedByName(relTxn.recipientName)) return true
            }
        }
        // 4. Any protected & masked recipient name in title, category, or note
        val maskedRecs = recipients.value.filter { it.isAmountProtected && !_unmaskedRecipientIds.value.contains(it.id) }
        for (r in maskedRecs) {
            val trimmedName = r.name.trim()
            if (trimmedName.length >= 2) {
                if (transaction.title.contains(trimmedName, ignoreCase = true) ||
                    transaction.category.contains(trimmedName, ignoreCase = true) ||
                    (!transaction.note.isNullOrBlank() && transaction.note.contains(trimmedName, ignoreCase = true))
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun isUnifiedItemMasked(item: com.example.ui.components.UnifiedTransactionItem): Boolean {
        return when (item) {
            is com.example.ui.components.UnifiedTransactionItem.Single -> isTransactionAmountMasked(item.transaction)
            is com.example.ui.components.UnifiedTransactionItem.Paired -> isTransactionAmountMasked(item.fromTxn) || isTransactionAmountMasked(item.toTxn)
        }
    }

    fun getMaskedRecipientForTransaction(transaction: TransactionEntity): RecipientEntity? {
        if (transaction.recipientId != null && transaction.recipientId > 0L) {
            val rec = recipients.value.firstOrNull { it.id == transaction.recipientId }
            if (rec != null && isRecipientMasked(rec.id)) return rec
        }
        if (!transaction.recipientName.isNullOrBlank()) {
            val rec = recipients.value.firstOrNull { it.name.equals(transaction.recipientName, ignoreCase = true) }
            if (rec != null && isRecipientMasked(rec.id)) return rec
        }
        val relId = transaction.relatedTransactionId
        if (relId != null && relId > 0L) {
            val relTxn = transactions.value.firstOrNull { it.id == relId }
            if (relTxn != null) {
                if (relTxn.recipientId != null && relTxn.recipientId > 0L) {
                    val rec = recipients.value.firstOrNull { it.id == relTxn.recipientId }
                    if (rec != null && isRecipientMasked(rec.id)) return rec
                }
                if (!relTxn.recipientName.isNullOrBlank()) {
                    val rec = recipients.value.firstOrNull { it.name.equals(relTxn.recipientName, ignoreCase = true) }
                    if (rec != null && isRecipientMasked(rec.id)) return rec
                }
            }
        }
        val maskedRecs = recipients.value.filter { it.isAmountProtected && !_unmaskedRecipientIds.value.contains(it.id) }
        for (r in maskedRecs) {
            val trimmedName = r.name.trim()
            if (trimmedName.length >= 2) {
                if (transaction.title.contains(trimmedName, ignoreCase = true) ||
                    transaction.category.contains(trimmedName, ignoreCase = true) ||
                    (!transaction.note.isNullOrBlank() && transaction.note.contains(trimmedName, ignoreCase = true))
                ) {
                    return r
                }
            }
        }
        return null
    }

    fun getMaskedRecipientForUnifiedItem(item: com.example.ui.components.UnifiedTransactionItem): RecipientEntity? {
        return when (item) {
            is com.example.ui.components.UnifiedTransactionItem.Single -> getMaskedRecipientForTransaction(item.transaction)
            is com.example.ui.components.UnifiedTransactionItem.Paired -> getMaskedRecipientForTransaction(item.fromTxn) ?: getMaskedRecipientForTransaction(item.toTxn)
        }
    }

    fun setRecipientAmountProtected(recipientId: Long, isProtected: Boolean) {
        viewModelScope.launch {
            repository.updateRecipientAmountProtection(recipientId, isProtected)
            if (!isProtected) {
                _unmaskedRecipientIds.update { it - recipientId }
            }
        }
    }

    fun refreshBiometricStatus() {
        _biometricStatus.value = securityManager.checkBiometricStatus()
    }

    fun setAppLockEnabled(enabled: Boolean) {
        securityManager.setAppLockEnabled(enabled)
        _isAppLockEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun setPasscode(passcode: String, type: LockType) {
        securityManager.setPasscode(passcode)
        securityManager.setLockType(type)
        _hasPasscode.value = true
        _lockType.value = type
        setAppLockEnabled(true)
    }

    fun verifyPasscode(input: String): Boolean {
        return securityManager.verifyPasscode(input)
    }

    fun removePasscode() {
        securityManager.removePasscode()
        _hasPasscode.value = false
        _isAppLockEnabled.value = false
        _isBiometricEnabled.value = false
        _isAppLocked.value = false
        maskAllRecipients()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    fun setAutoLockDuration(duration: AutoLockDuration) {
        securityManager.setAutoLockDuration(duration)
        _autoLockDuration.value = duration
    }

    fun setScreenSecurityEnabled(enabled: Boolean) {
        securityManager.setScreenSecurityEnabled(enabled)
        _isScreenSecurityEnabled.value = enabled
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun lockApp() {
        if (_isAppLockEnabled.value && _hasPasscode.value) {
            _isAppLocked.value = true
        }
        maskAllRecipients()
    }

    private var internalPickerActiveUntil = 0L

    fun setInternalPickerActive(active: Boolean) {
        if (active) {
            internalPickerActiveUntil = System.currentTimeMillis() + 180_000L
        } else {
            // Grant a 20-second grace period after picker closes so onResume does not lock
            internalPickerActiveUntil = System.currentTimeMillis() + 20_000L
            securityManager.recordBackgroundTimestamp()
        }
    }

    fun grantInternalPickerGracePeriod(millis: Long = 60_000L) {
        val target = System.currentTimeMillis() + millis
        if (target > internalPickerActiveUntil) {
            internalPickerActiveUntil = target
        }
    }

    fun onAppBackgrounded() {
        if (System.currentTimeMillis() >= internalPickerActiveUntil) {
            securityManager.recordBackgroundTimestamp()
        }
        maskAllRecipients()
    }

    fun onAppResumed() {
        if (System.currentTimeMillis() < internalPickerActiveUntil) {
            // Returning from system file/document picker or within active grace period - do not lock app
            securityManager.recordBackgroundTimestamp()
            return
        }
        if (securityManager.shouldLockOnResume()) {
            _isAppLocked.value = true
            maskAllRecipients()
        }
    }

    // Pending file restore state (survives Activity configuration changes)
    data class PendingFileRestore(
        val content: String,
        val fileName: String
    )

    private val _pendingFileRestore = MutableStateFlow<PendingFileRestore?>(null)
    val pendingFileRestore: StateFlow<PendingFileRestore?> = _pendingFileRestore.asStateFlow()

    fun stageBackupForRestore(content: String, fileName: String) {
        grantInternalPickerGracePeriod(120_000L)
        _pendingFileRestore.value = PendingFileRestore(content, fileName)
    }

    fun clearPendingRestore() {
        _pendingFileRestore.value = null
        securityManager.recordBackgroundTimestamp()
    }

    private val _currentSettingsSection = MutableStateFlow(com.example.ui.screens.SettingsSection.CURRENCIES)
    val currentSettingsSection: StateFlow<com.example.ui.screens.SettingsSection> = _currentSettingsSection.asStateFlow()

    fun setSettingsSection(section: com.example.ui.screens.SettingsSection) {
        _currentSettingsSection.value = section
    }

    private val _selectedCalendarType = MutableStateFlow(
        try {
            val saved = prefs.getString("selected_calendar_type", com.example.util.AppCalendarType.SOLAR_DARI.code)
            com.example.util.AppCalendarType.values().firstOrNull { it.code == saved } ?: com.example.util.AppCalendarType.SOLAR_DARI
        } catch (e: Exception) {
            com.example.util.AppCalendarType.SOLAR_DARI
        }
    )
    val selectedCalendarType: StateFlow<com.example.util.AppCalendarType> = _selectedCalendarType.asStateFlow()

    fun setCalendarType(type: com.example.util.AppCalendarType) {
        _selectedCalendarType.value = type
        com.example.util.PersianDateHelper.activeCalendarType = type
        prefs.edit().putString("selected_calendar_type", type.code).apply()
    }

    fun formatDate(timestamp: Long): String =
        com.example.util.PersianDateHelper.formatDate(timestamp, _selectedCalendarType.value)

    fun formatDateTime(timestamp: Long): String =
        com.example.util.PersianDateHelper.formatDateTime(timestamp, _selectedCalendarType.value)

    fun formatDateNumeric(timestamp: Long): String =
        com.example.util.PersianDateHelper.formatDateNumeric(timestamp, _selectedCalendarType.value)

    // --- Data Backup & Auto-Backup State ---
    private val autoBackupPrefs = application.getSharedPreferences("auto_backup_prefs", Context.MODE_PRIVATE)

    private val _localBackups = MutableStateFlow<List<BackupFileInfo>>(emptyList())
    val localBackups: StateFlow<List<BackupFileInfo>> = _localBackups.asStateFlow()

    private val _isAutoBackupEnabled = MutableStateFlow(autoBackupPrefs.getBoolean("enabled", false))
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    private val _autoBackupFrequency = MutableStateFlow(autoBackupPrefs.getString("frequency", "daily") ?: "daily")
    val autoBackupFrequency: StateFlow<String> = _autoBackupFrequency.asStateFlow()

    private val _autoBackupDestination = MutableStateFlow(autoBackupPrefs.getString("destination", "local") ?: "local")
    val autoBackupDestination: StateFlow<String> = _autoBackupDestination.asStateFlow()

    private val _lastAutoBackupTime = MutableStateFlow(autoBackupPrefs.getLong("last_time", 0L))
    val lastAutoBackupTime: StateFlow<Long> = _lastAutoBackupTime.asStateFlow()

    private val _isOperatingBackup = MutableStateFlow(false)
    val isOperatingBackup: StateFlow<Boolean> = _isOperatingBackup.asStateFlow()

    init {
        com.example.util.PersianDateHelper.activeCalendarType = _selectedCalendarType.value
        if (injectedRepository == null) {
            val isCleanSlateDone = prefs.getBoolean("app_clean_slate_arta_v1", false)
            if (!isCleanSlateDone) {
                viewModelScope.launch {
                    repository.wipeAllData()
                    repository.seedInitialDataIfEmpty(force = true)
                    prefs.edit()
                        .putString("user_name", "مهمان")
                        .putBoolean("app_clean_slate_arta_v1", true)
                        .putBoolean("db_initialized_once", true)
                        .apply()
                    _userName.value = "مهمان"
                    autoBackupPrefs.edit().putBoolean("enabled", false).apply()
                    _isAutoBackupEnabled.value = false
                }
            } else {
                val isInitializedOnce = prefs.getBoolean("db_initialized_once", false)
                if (!isInitializedOnce) {
                    viewModelScope.launch {
                        repository.seedInitialDataIfEmpty()
                        prefs.edit().putBoolean("db_initialized_once", true).apply()
                    }
                }
            }
        }
        loadLocalBackups()
        checkAndPerformAutoBackup()
    }

    val accounts: StateFlow<List<AccountCardEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccounts: StateFlow<List<AccountCardEntity>> = repository.allAccounts
        .map { list -> list.filter { !it.isFrozen } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipients: StateFlow<List<RecipientEntity>> = repository.allRecipients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protectedMaskedRecipientIds: StateFlow<Set<Long>> = combine(
        recipients,
        _unmaskedRecipientIds
    ) { recList, unmasked ->
        recList.filter { it.isAmountProtected && !unmasked.contains(it.id) }.map { it.id }.toSet()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val currencies: StateFlow<List<CurrencyEntity>> = repository.allCurrencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCurrencies: StateFlow<List<CurrencyEntity>> = repository.activeCurrencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCategories: StateFlow<List<CategoryEntity>> = repository.activeCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<FinancialGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeGoals: StateFlow<List<FinancialGoalEntity>> = repository.activeGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedGoals: StateFlow<List<FinancialGoalEntity>> = repository.completedGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedGoals: StateFlow<List<FinancialGoalEntity>> = repository.archivedGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoalTransactions: StateFlow<List<GoalTransactionEntity>> = repository.allGoalTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allQuickActions: StateFlow<List<QuickActionEntity>> = repository.allQuickActions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeQuickActions: StateFlow<List<QuickActionEntity>> = repository.homeQuickActions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingLists: StateFlow<List<ShoppingListWithItems>> = repository.allShoppingListsWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _maxHomeQuickActions = MutableStateFlow(
        prefs.getInt("max_home_quick_actions", 6)
    )
    val maxHomeQuickActions: StateFlow<Int> = _maxHomeQuickActions.asStateFlow()

    fun setMaxHomeQuickActions(count: Int) {
        _maxHomeQuickActions.value = count
        prefs.edit().putInt("max_home_quick_actions", count).apply()
    }

    fun insertQuickAction(quickAction: QuickActionEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertQuickAction(quickAction)
            onComplete?.invoke(id)
        }
    }

    fun updateQuickAction(quickAction: QuickActionEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.updateQuickAction(quickAction)
            onComplete?.invoke()
        }
    }

    fun deleteQuickAction(quickAction: QuickActionEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteQuickAction(quickAction)
            onComplete?.invoke()
        }
    }

    fun deleteQuickActionById(id: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteQuickActionById(id)
            onComplete?.invoke()
        }
    }

    fun reorderQuickActions(list: List<QuickActionEntity>) {
        viewModelScope.launch {
            repository.reorderQuickActions(list)
        }
    }

    fun toggleQuickActionHomeVisibility(quickAction: QuickActionEntity) {
        viewModelScope.launch {
            repository.updateQuickAction(quickAction.copy(showOnHome = !quickAction.showOnHome))
        }
    }

    fun executeQuickAction(
        quickAction: QuickActionEntity,
        amount: Double,
        customTimestamp: Long? = null,
        customNote: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (amount <= 0.0) return
        val ts = customTimestamp ?: System.currentTimeMillis()
        val note = customNote ?: quickAction.note

        viewModelScope.launch {
            runCatching {
                when (quickAction.actionType) {
                    QuickActionType.EXPENSE -> {
                        repository.addTransaction(
                            title = quickAction.title,
                            amount = amount,
                            type = TransactionType.EXPENSE,
                            category = quickAction.categoryName.ifBlank { "مصرف عمومی" },
                            accountId = quickAction.sourceAccountId,
                            categoryId = quickAction.categoryId,
                            note = note,
                            currencyId = quickAction.currencyId,
                            currencyCode = quickAction.currencyCode,
                            currencySymbol = quickAction.currencySymbol,
                            timestamp = ts,
                            affectsBalance = true
                        )
                    }
                    QuickActionType.INCOME -> {
                        repository.addTransaction(
                            title = quickAction.title,
                            amount = amount,
                            type = TransactionType.INCOME,
                            category = quickAction.categoryName.ifBlank { "عاید عمومی" },
                            accountId = quickAction.sourceAccountId,
                            categoryId = quickAction.categoryId,
                            note = note,
                            currencyId = quickAction.currencyId,
                            currencyCode = quickAction.currencyCode,
                            currencySymbol = quickAction.currencySymbol,
                            timestamp = ts,
                            affectsBalance = true
                        )
                    }
                    QuickActionType.RECEIVE -> {
                        val personName = quickAction.recipientName ?: quickAction.title
                        val userNote = note?.trim()?.ifBlank { null }
                        repository.addTransaction(
                            title = personName,
                            amount = amount,
                            type = TransactionType.INCOME,
                            category = "",
                            accountId = quickAction.sourceAccountId,
                            recipientName = personName,
                            recipientId = quickAction.recipientId,
                            note = userNote,
                            currencyId = quickAction.currencyId,
                            currencyCode = quickAction.currencyCode,
                            currencySymbol = quickAction.currencySymbol,
                            timestamp = ts,
                            affectsBalance = true
                        )
                    }
                    QuickActionType.PAY -> {
                        val personName = quickAction.recipientName ?: quickAction.title
                        val userNote = note?.trim()?.ifBlank { null }
                        repository.addTransaction(
                            title = personName,
                            amount = amount,
                            type = TransactionType.EXPENSE,
                            category = "",
                            accountId = quickAction.sourceAccountId,
                            recipientName = personName,
                            recipientId = quickAction.recipientId,
                            note = userNote,
                            currencyId = quickAction.currencyId,
                            currencyCode = quickAction.currencyCode,
                            currencySymbol = quickAction.currencySymbol,
                            timestamp = ts,
                            affectsBalance = true
                        )
                    }
                    QuickActionType.TRANSFER -> {
                        val destId = quickAction.destinationAccountId
                        val srcId = quickAction.sourceAccountId
                        val accountsList = accounts.value
                        val srcAcc = accountsList.find { it.id == srcId }
                        val destAcc = if (destId != null) accountsList.find { it.id == destId } else null
                        val userNote = note?.trim()?.ifBlank { null }

                        if (srcAcc != null && destAcc != null) {
                            repository.executeWalletTransfer(
                                fromAccount = srcAcc,
                                toAccount = destAcc,
                                fromAmount = amount,
                                toAmount = amount,
                                rate = 1.0,
                                note = userNote
                            )
                        } else if (destAcc != null && srcId == 0L) {
                            val curr = (if (quickAction.currencyId > 0L) currencies.value.find { it.id == quickAction.currencyId } else null)
                                ?: currencies.value.find { it.code.equals(quickAction.currencyCode, ignoreCase = true) }
                                ?: activeCurrencies.value.firstOrNull() ?: CurrencyEntity(name = "افغانی", code = "AFN", symbol = "؋")
                            repository.executeCashCardTransfer(
                                isCashToCard = true,
                                cashCurrency = curr,
                                cardAccount = destAcc,
                                cashAmount = amount,
                                cardAmount = amount,
                                rate = 1.0,
                                note = userNote
                            )
                        } else if (srcAcc != null && destId == null) {
                            val curr = (if (quickAction.currencyId > 0L) currencies.value.find { it.id == quickAction.currencyId } else null)
                                ?: currencies.value.find { it.code.equals(quickAction.currencyCode, ignoreCase = true) }
                                ?: activeCurrencies.value.firstOrNull() ?: CurrencyEntity(name = "افغانی", code = "AFN", symbol = "؋")
                            repository.executeCashCardTransfer(
                                isCashToCard = false,
                                cashCurrency = curr,
                                cardAccount = srcAcc,
                                cashAmount = amount,
                                cardAmount = amount,
                                rate = 1.0,
                                note = userNote
                            )
                        }
                    }
                    QuickActionType.GOAL_DEPOSIT -> {
                        val goalId = quickAction.targetGoalId
                        val userNote = note?.trim()?.ifBlank { null } ?: ""
                        if (goalId != null) {
                            val depResult = repository.depositToGoal(
                                goalId = goalId,
                                amount = amount,
                                accountId = if (quickAction.sourceAccountId > 0) quickAction.sourceAccountId else null,
                                note = userNote
                            )
                            if (depResult.isFailure) {
                                throw (depResult.exceptionOrNull() ?: IllegalStateException("خطا در واریز به هدف"))
                            }
                        }
                    }
                }
                repository.incrementQuickActionUsage(quickAction.id)
            }.onSuccess {
                onSuccess()
            }.onFailure { ex ->
                _operationErrorMessage.value = ex.message ?: "خطا در انجام عملیات سریع"
            }
        }
    }

    private val _selectedCurrency = MutableStateFlow<CurrencyEntity?>(null)
    val selectedCurrency: StateFlow<CurrencyEntity?> = _selectedCurrency.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter: StateFlow<TransactionFilter> = _selectedFilter.asStateFlow()

    val currencySymbol: StateFlow<String> = combine(
        _selectedCurrency,
        activeCurrencies
    ) { selected, active ->
        selected?.symbol ?: active.firstOrNull()?.symbol ?: "؋"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "؋")

    /**
     * Balances strictly separated per currency - no mixing or summing different currencies!
     * Strictly calculated purely from transactions using currencyId (with code fallback)!
     */
    val currencyBalances: StateFlow<List<CurrencyBalanceInfo>> = combine(
        repository.allTransactions,
        currencies
    ) { txns, allList ->
        allList.map { curr ->
            val currTxns = txns.filter {
                ((it.currencyId > 0 && it.currencyId == curr.id) || (it.currencyId <= 0 && it.currencyCode.equals(curr.code, ignoreCase = true))) &&
                it.category != "PersonExchange" &&
                it.category != "PersonDebt" &&
                it.category != "انتقال حساب اشخاص" &&
                (it.accountId == 0L || it.category == "Exchange")
            }
            val effectiveTxns = currTxns.filter { it.affectsBalance }
            // Operational Incomes and Expenses exclude person transactions, exchange/transfers, and goal allocations
            val income = effectiveTxns.filter {
                it.type == TransactionType.INCOME &&
                it.recipientName.isNullOrBlank() &&
                it.category != "Exchange" &&
                it.category != "انتقالات" &&
                it.category != "واریز به هدف" &&
                it.category != "برداشت از هدف"
            }.sumOf { it.amount }
            val expense = effectiveTxns.filter {
                it.type == TransactionType.EXPENSE &&
                it.recipientName.isNullOrBlank() &&
                it.category != "Exchange" &&
                it.category != "انتقالات" &&
                it.category != "واریز به هدف" &&
                it.category != "برداشت از هدف"
            }.sumOf { it.amount }
            
            // All inflows and outflows affect the cash/account balance - strictly computed from transactions!
            val allInflow = effectiveTxns.filter {
                it.type == TransactionType.INCOME ||
                it.kind == TransactionKind.GOAL_WITHDRAW ||
                (it.type == TransactionType.TRANSFER && (it.category == "برداشت از هدف" || it.title.startsWith("انتقال از هدف")))
            }.sumOf { it.amount }
            val allOutflow = effectiveTxns.filter {
                it.type == TransactionType.EXPENSE ||
                it.kind == TransactionKind.GOAL_DEPOSIT ||
                (it.type == TransactionType.TRANSFER && (it.category == "واریز به هدف" || it.title.startsWith("انتقال به هدف")))
            }.sumOf { it.amount }
            val bal = allInflow - allOutflow
            CurrencyBalanceInfo(
                currency = curr,
                balance = bal,
                totalIncome = income,
                totalExpense = expense
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Financial Goals Overview & Balance Breakdown per Currency
     * Separates Real Total Balance, Allocated to Goals, and Free Available Balance
     * Without any double counting.
     */
    val selectedCurrencyGoalSummary: StateFlow<CurrencyGoalSummary> = combine(
        currencyBalances,
        _selectedCurrency,
        allGoals
    ) { balances, selCurr, goals ->
        val target = if (selCurr != null) {
            balances.find { (selCurr.id > 0 && it.currency.id == selCurr.id) || it.currency.code.equals(selCurr.code, ignoreCase = true) }
        } else {
            balances.firstOrNull()
        }
        val targetCurr = target?.currency ?: selCurr
        val code = targetCurr?.code ?: "AFN"
        val symbol = targetCurr?.symbol ?: "؋"
        val currId = targetCurr?.id ?: 0L
        val freeBalance = target?.balance ?: 0.0

        val matchingActiveGoals = goals.filter {
            it.status == GoalStatus.ACTIVE &&
            ((currId > 0 && it.currencyId == currId) || it.currencyCode.equals(code, ignoreCase = true))
        }
        val matchingCompletedGoals = goals.filter {
            it.status == GoalStatus.COMPLETED &&
            ((currId > 0 && it.currencyId == currId) || it.currencyCode.equals(code, ignoreCase = true))
        }
        val totalAllocated = matchingActiveGoals.sumOf { it.currentAmount }
        val totalReal = freeBalance + totalAllocated

        CurrencyGoalSummary(
            currencyCode = code,
            currencySymbol = symbol,
            totalRealBalance = totalReal,
            totalAllocatedToGoals = totalAllocated,
            availableFreeBalance = freeBalance,
            activeGoalsCount = matchingActiveGoals.size,
            completedGoalsCount = matchingCompletedGoals.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CurrencyGoalSummary("AFN", "؋", 0.0, 0.0, 0.0, 0, 0)
    )

    val allCurrenciesGoalSummaries: StateFlow<List<CurrencyGoalSummary>> = combine(
        currencyBalances,
        allGoals
    ) { balances, goals ->
        balances.map { bInfo ->
            val code = bInfo.currency.code
            val symbol = bInfo.currency.symbol
            val freeBalance = bInfo.balance
            val matchingActiveGoals = goals.filter {
                it.status == GoalStatus.ACTIVE &&
                ((bInfo.currency.id > 0 && it.currencyId == bInfo.currency.id) || it.currencyCode.equals(code, ignoreCase = true))
            }
            val matchingCompletedGoals = goals.filter {
                it.status == GoalStatus.COMPLETED &&
                ((bInfo.currency.id > 0 && it.currencyId == bInfo.currency.id) || it.currencyCode.equals(code, ignoreCase = true))
            }
            val totalAllocated = matchingActiveGoals.sumOf { it.currentAmount }
            val totalReal = freeBalance + totalAllocated

            CurrencyGoalSummary(
                currencyCode = code,
                currencySymbol = symbol,
                totalRealBalance = totalReal,
                totalAllocatedToGoals = totalAllocated,
                availableFreeBalance = freeBalance,
                activeGoalsCount = matchingActiveGoals.size,
                completedGoalsCount = matchingCompletedGoals.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = combine(
        repository.allTransactions,
        _searchQuery,
        _selectedFilter
    ) { txns, query, filter ->
        txns.filter { txn ->
            val matchesQuery = query.isBlank() ||
                txn.title.contains(query, ignoreCase = true) ||
                txn.category.contains(query, ignoreCase = true) ||
                (txn.recipientName?.contains(query, ignoreCase = true) == true) ||
                (txn.note?.contains(query, ignoreCase = true) == true)

            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> txn.type == TransactionType.EXPENSE
                TransactionFilter.INCOME -> txn.type == TransactionType.INCOME
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBalance: StateFlow<Double> = combine(
        currencyBalances,
        _selectedCurrency
    ) { balances, selCurr ->
        val target = if (selCurr != null) {
            balances.find { (selCurr.id > 0 && it.currency.id == selCurr.id) || it.currency.code.equals(selCurr.code, ignoreCase = true) }
        } else {
            balances.firstOrNull()
        }
        target?.balance ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthSpending: StateFlow<Double> = combine(
        transactions,
        activeCurrencies,
        _selectedCurrency,
        _selectedCalendarType
    ) { txns, currencies, selectedCurr, calendarType ->
        val startOfMonth = PersianDateHelper.getStartOfCurrentMonth(calendarType)
        val endOfMonth = PersianDateHelper.getEndOfCurrentMonth(calendarType)

        val baseCurr = currencies.firstOrNull { it.isBaseCurrency } ?: currencies.firstOrNull()
        val totalInBase = txns.filter {
            it.affectsBalance &&
            it.type == TransactionType.EXPENSE &&
            it.recipientName.isNullOrBlank() && // Exclude person payments from expenses
            it.category != "Exchange" &&
            it.category != "PersonExchange" &&
            it.category != "PersonDebt" &&
            it.category != "انتقالات" &&
            it.category != "انتقال حساب اشخاص" &&
            it.category != "Transfer" &&
            it.category != "واریز به هدف" &&
            it.category != "برداشت از هدف" &&
            it.timestamp in startOfMonth..endOfMonth
        }.sumOf { txn ->
            val txnCurr = currencies.firstOrNull { (txn.currencyId > 0 && it.id == txn.currencyId) || it.code.equals(txn.currencyCode, ignoreCase = true) }
            val rateToBase = when {
                baseCurr != null && ((txn.currencyId > 0 && txn.currencyId == baseCurr.id) || txn.currencyCode.equals(baseCurr.code, ignoreCase = true)) -> 1.0
                txn.exchangeRate > 0.0 && txn.exchangeRate != 1.0 -> txn.exchangeRate
                else -> txnCurr?.exchangeRateToBase ?: 1.0
            }
            txn.amount * rateToBase
        }

        if (selectedCurr != null && !selectedCurr.isBaseCurrency && selectedCurr.exchangeRateToBase > 0) {
            totalInBase / selectedCurr.exchangeRateToBase
        } else {
            totalInBase
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentMonthIncome: StateFlow<Double> = combine(
        transactions,
        activeCurrencies,
        _selectedCurrency,
        _selectedCalendarType
    ) { txns, currencies, selectedCurr, calendarType ->
        val startOfMonth = PersianDateHelper.getStartOfCurrentMonth(calendarType)
        val endOfMonth = PersianDateHelper.getEndOfCurrentMonth(calendarType)

        val baseCurr = currencies.firstOrNull { it.isBaseCurrency } ?: currencies.firstOrNull()
        val totalInBase = txns.filter {
            it.affectsBalance &&
            it.type == TransactionType.INCOME &&
            it.recipientName.isNullOrBlank() && // Exclude person receipts from income
            it.category != "Exchange" &&
            it.category != "PersonExchange" &&
            it.category != "PersonDebt" &&
            it.category != "انتقالات" &&
            it.category != "انتقال حساب اشخاص" &&
            it.category != "Transfer" &&
            it.category != "واریز به هدف" &&
            it.category != "برداشت از هدف" &&
            it.timestamp in startOfMonth..endOfMonth
        }.sumOf { txn ->
            val txnCurr = currencies.firstOrNull { (txn.currencyId > 0 && it.id == txn.currencyId) || it.code.equals(txn.currencyCode, ignoreCase = true) }
            val rateToBase = when {
                baseCurr != null && ((txn.currencyId > 0 && txn.currencyId == baseCurr.id) || txn.currencyCode.equals(baseCurr.code, ignoreCase = true)) -> 1.0
                txn.exchangeRate > 0.0 && txn.exchangeRate != 1.0 -> txn.exchangeRate
                else -> txnCurr?.exchangeRateToBase ?: 1.0
            }
            txn.amount * rateToBase
        }

        if (selectedCurr != null && !selectedCurr.isBaseCurrency && selectedCurr.exchangeRateToBase > 0) {
            totalInBase / selectedCurr.exchangeRateToBase
        } else {
            totalInBase
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Alarm banner for upcoming due dates (within 5 days or overdue)
    val upcomingDueTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions.map { txns ->
        val now = System.currentTimeMillis()
        val fiveDaysInMillis = 5L * 24 * 60 * 60 * 1000L
        txns.filter { txn ->
            val due = txn.dueDate
            !txn.recipientName.isNullOrBlank() &&
            due != null &&
            !txn.isSettled &&
            (due - now <= fiveDaysInMillis)
        }.sortedBy { it.dueDate ?: Long.MAX_VALUE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Centralized feedback error message for database integrity protection
    private val _operationErrorMessage = MutableStateFlow<String?>(null)
    val operationErrorMessage: StateFlow<String?> = _operationErrorMessage.asStateFlow()

    fun clearOperationError() {
        _operationErrorMessage.value = null
    }

    fun settleTransaction(txn: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(txn, txn.copy(isSettled = true))
        }
    }

    fun dismissAlarm(txn: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(txn, txn.copy(dueDate = null))
        }
    }

    // --- Calculator Screen State (Video Frame 08 - 14) ---
    private val _isRequestMode = MutableStateFlow(false) // false = Send (Expense), true = Request (Income)
    val isRequestMode: StateFlow<Boolean> = _isRequestMode.asStateFlow()

    private val _calcExpression = MutableStateFlow("")
    val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

    private val _evaluatedAmount = MutableStateFlow(0.0)
    val evaluatedAmount: StateFlow<Double> = _evaluatedAmount.asStateFlow()

    private val _selectedRecipient = MutableStateFlow<RecipientEntity?>(null)
    val selectedRecipient: StateFlow<RecipientEntity?> = _selectedRecipient.asStateFlow()

    private val _isGeneralSelected = MutableStateFlow(false)
    val isGeneralSelected: StateFlow<Boolean> = _isGeneralSelected.asStateFlow()

    private val _selectedAccount = MutableStateFlow<AccountCardEntity?>(null)
    val selectedAccount: StateFlow<AccountCardEntity?> = _selectedAccount.asStateFlow()

    private val _transactionNote = MutableStateFlow("")
    val transactionNote: StateFlow<String> = _transactionNote.asStateFlow()

    private val _selectedCategoryEntity = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategoryEntity: StateFlow<CategoryEntity?> = _selectedCategoryEntity.asStateFlow()

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _transactionSuccessMessage = MutableStateFlow<String?>(null)
    val transactionSuccessMessage: StateFlow<String?> = _transactionSuccessMessage.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun setCurrency(symbol: String) {
        val match = activeCurrencies.value.find { it.symbol == symbol }
        if (match != null) {
            _selectedCurrency.value = match
        }
    }

    fun setMode(isRequest: Boolean) {
        _isRequestMode.value = isRequest
    }

    fun selectRecipient(recipient: RecipientEntity?) {
        _selectedRecipient.value = recipient
        if (recipient != null) {
            _isGeneralSelected.value = false
        }
    }

    fun selectGeneral(selected: Boolean) {
        _isGeneralSelected.value = selected
        if (selected) {
            _selectedRecipient.value = null
        }
    }

    fun clearRecipientAndGeneral() {
        _selectedRecipient.value = null
        _isGeneralSelected.value = false
    }

    fun selectAccount(account: AccountCardEntity?) {
        _selectedAccount.value = account
    }

    fun setNote(note: String) {
        _transactionNote.value = note
    }

    fun selectCategory(category: CategoryEntity?) {
        _selectedCategoryEntity.value = category
        _selectedCategory.value = category?.name ?: ""
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        _selectedCategoryEntity.value = null
    }

    fun clearSuccessMessage() {
        _transactionSuccessMessage.value = null
    }

    /**
     * Resets the calculator and payment screen inputs to zero/default
     * while preserving ONLY the previously selected recipient and currency.
     */
    fun preparePaymentScreen() {
        _calcExpression.value = ""
        _evaluatedAmount.value = 0.0
        _transactionNote.value = ""
        _selectedCategory.value = ""
        _selectedCategoryEntity.value = null
        _isRequestMode.value = false
        _isGeneralSelected.value = false
    }

    fun prepareReceiveScreen() {
        _calcExpression.value = ""
        _evaluatedAmount.value = 0.0
        _transactionNote.value = ""
        _selectedCategory.value = ""
        _selectedCategoryEntity.value = null
        _isRequestMode.value = true
        _isGeneralSelected.value = false
    }

    fun resetCalculatorInputsOnly() {
        _calcExpression.value = ""
        _evaluatedAmount.value = 0.0
        _transactionNote.value = ""
        _selectedCategory.value = ""
        _selectedCategoryEntity.value = null
        _selectedAccount.value = null // null indicates "موجودی کل (بیلانس کل)" without needing any card!
    }

    // --- Calculator Button Handlers ---
    fun onDigit(d: String) {
        val current = _calcExpression.value
        // Prevent multiple consecutive leading zeros
        if (current == "0" && d != ".") {
            _calcExpression.value = d
        } else {
            _calcExpression.value = current + d
        }
        evaluateCurrent()
    }

    fun onOperator(op: String) {
        val current = _calcExpression.value.trim()
        if (current.isEmpty()) {
            if (_evaluatedAmount.value > 0) {
                val base = if (_evaluatedAmount.value % 1.0 == 0.0) _evaluatedAmount.value.toLong().toString() else _evaluatedAmount.value.toString()
                _calcExpression.value = "$base $op "
                evaluateCurrent()
            }
            return
        }

        val lastChar = current.last()
        if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '*' || lastChar == '÷' || lastChar == '/') {
            // Replace previous operator
            _calcExpression.value = current.dropLast(1) + op
        } else {
            _calcExpression.value = "$current $op "
        }
        evaluateCurrent()
    }

    fun onDot() {
        val current = _calcExpression.value.trim()
        if (current.isEmpty()) {
            _calcExpression.value = "0."
            evaluateCurrent()
            return
        }
        val lastChar = current.last()
        if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '*' || lastChar == '÷' || lastChar == '/') {
            _calcExpression.value = "$current 0."
            evaluateCurrent()
            return
        }
        val tokens = current.split(" ", "+", "-", "×", "*", "÷", "/")
        val lastToken = tokens.lastOrNull() ?: ""
        if (!lastToken.contains(".")) {
            _calcExpression.value = if (lastToken.isEmpty()) "$current 0." else "$current."
        }
        evaluateCurrent()
    }

    fun onBackspace() {
        val current = _calcExpression.value
        if (current.isNotEmpty()) {
            _calcExpression.value = current.trimEnd().dropLast(1).trimEnd()
        }
        evaluateCurrent()
    }

    fun onClear() {
        _calcExpression.value = ""
        _evaluatedAmount.value = 0.0
    }

    fun onToggleSign() {
        val current = _calcExpression.value.trim()
        if (current.isEmpty()) {
            if (_evaluatedAmount.value > 0) {
                val toggled = -_evaluatedAmount.value
                val toggledStr = if (toggled % 1.0 == 0.0) toggled.toLong().toString() else toggled.toString()
                _calcExpression.value = toggledStr
                evaluateCurrent()
            }
            return
        }
        val lastSpace = current.lastIndexOf(' ')
        if (lastSpace != -1) {
            val prefix = current.substring(0, lastSpace + 1)
            val lastToken = current.substring(lastSpace + 1)
            if (lastToken.isNotEmpty()) {
                val toggled = if (lastToken.startsWith("-")) lastToken.removePrefix("-") else "-$lastToken"
                _calcExpression.value = prefix + toggled
            }
        } else {
            val toggled = if (current.startsWith("-")) current.removePrefix("-") else "-$current"
            _calcExpression.value = toggled
        }
        evaluateCurrent()
    }

    fun onPercent() {
        val current = _calcExpression.value.trim()
        if (current.isEmpty()) {
            if (_evaluatedAmount.value > 0) {
                val pct = _evaluatedAmount.value / 100.0
                _calcExpression.value = if (pct % 1.0 == 0.0) pct.toLong().toString() else String.format(java.util.Locale.US, "%.3f", pct).trimEnd('0').trimEnd('.')
                evaluateCurrent()
            }
            return
        }
        val lastSpace = current.lastIndexOf(' ')
        if (lastSpace != -1) {
            val prefix = current.substring(0, lastSpace + 1)
            val lastToken = current.substring(lastSpace + 1)
            val num = lastToken.toDoubleOrNull()
            if (num != null) {
                val pct = num / 100.0
                val pctStr = if (pct % 1.0 == 0.0) pct.toLong().toString() else String.format(java.util.Locale.US, "%.3f", pct).trimEnd('0').trimEnd('.')
                _calcExpression.value = prefix + pctStr
            }
        } else {
            val num = current.toDoubleOrNull()
            if (num != null) {
                val pct = num / 100.0
                val pctStr = if (pct % 1.0 == 0.0) pct.toLong().toString() else String.format(java.util.Locale.US, "%.3f", pct).trimEnd('0').trimEnd('.')
                _calcExpression.value = pctStr
            }
        }
        evaluateCurrent()
    }

    fun onEquals() {
        evaluateCurrent()
        val res = _evaluatedAmount.value
        if (res >= 0) {
            _calcExpression.value = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(java.util.Locale.US, "%.3f", res).trimEnd('0').trimEnd('.')
        }
    }

    private fun evaluateCurrent() {
        val expr = _calcExpression.value
        if (expr.isBlank()) {
            _evaluatedAmount.value = 0.0
            return
        }
        try {
            val result = evaluateSimpleMath(expr)
            if (result >= 0) {
                _evaluatedAmount.value = result
            }
        } catch (_: Exception) {
            // Keep previous valid result or 0
        }
    }

    private fun evaluateSimpleMath(expression: String): Double {
        val cleaned = expression.replace("×", "*").replace("÷", "/")
        val rawTokens = cleaned.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (rawTokens.isEmpty()) return 0.0

        val tokens = rawTokens.map { token ->
            when {
                token == "." -> "0.0"
                token.endsWith(".") && token.length > 1 -> token.dropLast(1)
                token.startsWith(".") && token.length > 1 -> "0$token"
                else -> token
            }
        }

        // Process multiplications and divisions first
        val step1Tokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "*" || token == "/") {
                if (step1Tokens.isNotEmpty() && i + 1 < tokens.size) {
                    val prev = step1Tokens.removeAt(step1Tokens.size - 1).toDoubleOrNull() ?: 0.0
                    val next = tokens[i + 1].toDoubleOrNull() ?: 1.0
                    val res = if (token == "*") prev * next else if (next != 0.0) prev / next else prev
                    step1Tokens.add(res.toString())
                    i += 2
                    continue
                }
            }
            step1Tokens.add(token)
            i++
        }

        // Process additions and subtractions
        if (step1Tokens.isEmpty()) return 0.0
        var total = step1Tokens[0].toDoubleOrNull() ?: 0.0
        var j = 1
        while (j < step1Tokens.size) {
            val op = step1Tokens[j]
            val next = if (j + 1 < step1Tokens.size) step1Tokens[j + 1].toDoubleOrNull() ?: 0.0 else 0.0
            if (op == "+") {
                total += next
            } else if (op == "-") {
                total -= next
            }
            j += 2
        }
        return total
    }

    /**
     * Formats calculator amount display without wiping out the decimal point or trailing zeros
     * when the user is actively typing.
     */
    fun formatCalculatorDisplay(
        expression: String,
        evaluatedAmount: Double,
        currencyCode: String? = null
    ): String {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return "0"

        val hasOperator = trimmed.contains("+") || trimmed.contains("-") ||
                trimmed.contains("×") || trimmed.contains("*") ||
                trimmed.contains("÷") || trimmed.contains("/")

        if (!hasOperator) {
            // User is typing a single number directly: preserve what they typed, including trailing dots and zeros!
            return if (trimmed.contains(".")) {
                val parts = trimmed.split(".", limit = 2)
                val intPartStr = parts[0]
                val decPartStr = parts.getOrNull(1) ?: ""
                val intVal = intPartStr.toLongOrNull()
                val formattedInt = if (intVal != null) {
                    java.lang.String.format(java.util.Locale.US, "%,d", intVal)
                } else {
                    if (intPartStr.isEmpty()) "0" else intPartStr
                }
                "$formattedInt.$decPartStr"
            } else {
                val num = trimmed.toLongOrNull()
                if (num != null) java.lang.String.format(java.util.Locale.US, "%,d", num) else trimmed
            }
        } else {
            // Expression contains an operator (e.g., 5 + 3):
            val lastToken = trimmed.split("\\s+".toRegex()).lastOrNull() ?: ""
            if (lastToken.contains(".")) {
                val parts = lastToken.split(".", limit = 2)
                val intVal = parts[0].toLongOrNull() ?: 0L
                val decPart = parts.getOrNull(1) ?: ""
                return java.lang.String.format(java.util.Locale.US, "%,d.%s", intVal, decPart)
            }
            return formatAmount(evaluatedAmount, currencyCode)
        }
    }

    fun formatExchangeRateDescription(rate: Double, fromCode: String, toCode: String): String {
        if (rate <= 0.0) return ""
        return if (rate >= 1.0) {
            val rateStr = if (rate % 1.0 == 0.0) java.lang.String.format(java.util.Locale.US, "%,.0f", rate)
            else if (rate >= 100) java.lang.String.format(java.util.Locale.US, "%,.2f", rate).trimEnd('0').trimEnd('.')
            else java.lang.String.format(java.util.Locale.US, "%.4f", rate).trimEnd('0').trimEnd('.')
            "۱ $fromCode = $rateStr $toCode"
        } else {
            val inv = 1.0 / rate
            val invStr = if (inv % 1.0 == 0.0) java.lang.String.format(java.util.Locale.US, "%,.0f", inv)
            else if (inv >= 100) java.lang.String.format(java.util.Locale.US, "%,.2f", inv).trimEnd('0').trimEnd('.')
            else java.lang.String.format(java.util.Locale.US, "%.4f", inv).trimEnd('0').trimEnd('.')
            "۱ $toCode = $invStr $fromCode"
        }
    }

    fun submitTransaction(
        overrideCurrencyCode: String? = null,
        overrideCurrencySymbol: String? = null,
        exchangeRate: Double = 1.0,
        recipientCurrencyCode: String? = null,
        recipientCurrencySymbol: String? = null,
        recipientAmount: Double? = null,
        dueDate: Long? = null,
        customTimestamp: Long? = null,
        affectsBalance: Boolean = true,
        overrideCurrencyId: Long? = null,
        recipientCurrencyId: Long? = null,
        onSuccess: () -> Unit = {}
    ) {
        val amount = _evaluatedAmount.value
        if (amount <= 0.0) return

        // If no card is explicitly selected or affectsBalance is false, accountId is 0L
        val targetAccountId = if (affectsBalance) (_selectedAccount.value?.id ?: 0L) else 0L
        val isReq = _isRequestMode.value
        val type = if (isReq) TransactionType.INCOME else TransactionType.EXPENSE
        val recipient = _selectedRecipient.value
        val activeCurr = _selectedCurrency.value ?: activeCurrencies.value.firstOrNull()
        val currEntity = if (overrideCurrencyId != null && overrideCurrencyId > 0L) {
            activeCurrencies.value.firstOrNull { it.id == overrideCurrencyId }
        } else if (overrideCurrencyCode != null) {
            activeCurrencies.value.firstOrNull { it.code.equals(overrideCurrencyCode, ignoreCase = true) }
        } else {
            activeCurr
        }
        val finalCurrencyId = currEntity?.id ?: 0L
        val cCode = overrideCurrencyCode ?: currEntity?.code ?: "AFN"
        val cSymbol = overrideCurrencySymbol ?: currEntity?.symbol ?: "؋"

        val categoryName = when {
            recipient != null -> "" // No category for person transactions
            _selectedCategory.value.isNotBlank() -> _selectedCategory.value
            isReq -> "عاید عمومی"
            else -> "مصرف عمومی"
        }

        val title = when {
            recipient != null -> recipient.name
            _selectedCategory.value.isNotBlank() -> _selectedCategory.value
            else -> if (isReq) "عاید عمومی" else "مصرف عمومی"
        }

        val baseCurr = activeCurrencies.value.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.value.firstOrNull()
        val isBase = baseCurr != null && ((currEntity != null && currEntity.id == baseCurr.id) || cCode.equals(baseCurr.code, ignoreCase = true))
        val hasRecipientLedger = recipient != null && recipientCurrencyCode != null &&
                !recipientCurrencyCode.equals(cCode, ignoreCase = true) &&
                recipientAmount != null && recipientAmount > 0.0

        val finalExchangeRate = when {
            hasRecipientLedger -> {
                if (exchangeRate > 0.0) exchangeRate
                else if (amount > 0.0 && recipientAmount != null) (recipientAmount / amount)
                else 1.0
            }
            isBase -> 1.0
            exchangeRate > 0.0 && exchangeRate != 1.0 -> exchangeRate
            else -> activeCurrencies.value.firstOrNull { it.code.equals(cCode, ignoreCase = true) }?.exchangeRateToBase ?: 1.0
        }

        val userNote = _transactionNote.value.trim().ifBlank { null }

        // Case: Transaction with a Person where recipient ledger is recorded in a different currency
        if (hasRecipientLedger) {
            val rSymbol = recipientCurrencySymbol ?: recipientCurrencyCode!!
            val rCurr = if (recipientCurrencyId != null && recipientCurrencyId > 0L) {
                activeCurrencies.value.firstOrNull { it.id == recipientCurrencyId }
            } else {
                activeCurrencies.value.firstOrNull { it.code.equals(recipientCurrencyCode, ignoreCase = true) }
            }
            val rCurrId = rCurr?.id

            viewModelScope.launch {
                runCatching {
                    // Single transaction: amount and account in payment currency, ledger info in recipient's currency
                    repository.addTransaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = "",
                        accountId = targetAccountId,
                        recipientName = recipient!!.name,
                        recipientId = recipient.id,
                        note = userNote,
                        calculationExpression = if (_calcExpression.value.isNotBlank() && _calcExpression.value != amount.toString()) _calcExpression.value else null,
                        currencyId = finalCurrencyId,
                        currencyCode = cCode,
                        currencySymbol = cSymbol,
                        exchangeRate = finalExchangeRate,
                        ledgerCurrencyId = rCurrId,
                        ledgerCurrencyCode = recipientCurrencyCode,
                        ledgerCurrencySymbol = rSymbol,
                        ledgerAmount = recipientAmount,
                        dueDate = dueDate,
                        timestamp = customTimestamp ?: System.currentTimeMillis(),
                        affectsBalance = affectsBalance
                    )
                }.onSuccess {
                    _transactionSuccessMessage.value = if (isReq) {
                        "مقدار ${formatAmount(amount, cCode)} $cSymbol از ${recipient!!.name} دریافت و معادل ${formatAmount(recipientAmount!!, recipientCurrencyCode)} $rSymbol در حساب وی ثبت شد"
                    } else {
                        "مقدار ${formatAmount(amount, cCode)} $cSymbol به ${recipient!!.name} پرداخت و معادل ${formatAmount(recipientAmount!!, recipientCurrencyCode)} $rSymbol در حساب وی ثبت شد"
                    }
                    onClear()
                    _transactionNote.value = ""
                    _selectedCategory.value = ""
                    _selectedCategoryEntity.value = null
                    _isGeneralSelected.value = false
                    _selectedRecipient.value = null
                    checkAndPerformAutoBackup(triggerIsTransaction = true)
                    onSuccess()
                }.onFailure { ex ->
                    _operationErrorMessage.value = ex.message ?: "خطا در ثبت تراکنش"
                }
            }
            return
        }

        val resolvedCatId = if (recipient != null) null else _selectedCategoryEntity.value?.id

        viewModelScope.launch {
            runCatching {
                repository.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = categoryName,
                    categoryId = resolvedCatId,
                    accountId = targetAccountId,
                    recipientName = recipient?.name,
                    recipientId = recipient?.id,
                    note = userNote,
                    calculationExpression = if (_calcExpression.value.isNotBlank() && _calcExpression.value != amount.toString()) _calcExpression.value else null,
                    currencyId = finalCurrencyId,
                    currencyCode = cCode,
                    currencySymbol = cSymbol,
                    exchangeRate = finalExchangeRate,
                    dueDate = dueDate,
                    timestamp = customTimestamp ?: System.currentTimeMillis(),
                    affectsBalance = affectsBalance
                )
            }.onSuccess {
                _transactionSuccessMessage.value = if (isReq) {
                    if (recipient != null) "مقدار ${formatAmount(amount, cCode)} $cSymbol با موفقیت از ${recipient.name} دریافت شد"
                    else "مقدار ${formatAmount(amount, cCode)} $cSymbol عاید شد"
                } else {
                    if (recipient != null) "مقدار ${formatAmount(amount, cCode)} $cSymbol با موفقیت به ${recipient.name} پرداخت شد"
                    else "مقدار ${formatAmount(amount, cCode)} $cSymbol مصرف شد"
                }
                onClear()
                _transactionNote.value = ""
                _selectedCategory.value = ""
                _selectedCategoryEntity.value = null
                _isGeneralSelected.value = false
                _selectedRecipient.value = null
                checkAndPerformAutoBackup(triggerIsTransaction = true)
                onSuccess()
            }.onFailure { ex ->
                _operationErrorMessage.value = ex.message ?: "خطا در ثبت تراکنش"
            }
        }
    }

    fun updateTransaction(
        oldTxn: TransactionEntity,
        newTxn: TransactionEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                repository.updateTransaction(oldTxn, newTxn)
            }.onSuccess {
                _operationErrorMessage.value = null
                onSuccess()
            }.onFailure { ex ->
                val msg = ex.message ?: "خطا در ویرایش معامله"
                _operationErrorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun deleteTransaction(
        transaction: TransactionEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching {
                repository.deleteTransaction(transaction)
            }.onSuccess {
                _operationErrorMessage.value = null
                onSuccess()
            }.onFailure { ex ->
                val msg = ex.message ?: "خطا در حذف معامله"
                _operationErrorMessage.value = msg
                onError(msg)
            }
        }
    }

    fun addAccount(
        name: String,
        cardNumber: String,
        balance: Double,
        theme: String,
        holder: String = "SHOHRAB N.",
        expiry: String = "12/28",
        currencyCode: String = "AFN",
        currencySymbol: String = "؋",
        currencyId: Long = 0L
    ) {
        val cId = if (currencyId > 0L) currencyId else (activeCurrencies.value.firstOrNull { it.code.equals(currencyCode, ignoreCase = true) }?.id ?: 0L)
        viewModelScope.launch {
            repository.addAccount(
                name = name,
                cardNumberMasked = cardNumber,
                balance = balance,
                theme = theme,
                holder = holder,
                expiry = expiry,
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                currencyId = cId
            )
        }
    }

    fun updateAccount(account: AccountCardEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: AccountCardEntity) {
        viewModelScope.launch {
            val count = repository.countTransactionsForAccount(account.id)
            if (count > 0) {
                _operationErrorMessage.value = "امکان حذف حساب «${account.name}» وجود ندارد زیرا $count تراکنش به این حساب متصل است."
                return@launch
            }
            repository.deleteAccount(account)
        }
    }

    fun executeCurrencyExchange(
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        fromAmount: Double,
        rate: Double,
        explicitToAmount: Double? = null,
        fromAccountId: Long? = null,
        toAccountId: Long? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (fromAmount <= 0.0 || (rate <= 0.0 && (explicitToAmount == null || explicitToAmount <= 0.0))) return
        val fromBal = currencyBalances.value.find { (fromCurrency.id > 0 && it.currency.id == fromCurrency.id) || it.currency.code.equals(fromCurrency.code, ignoreCase = true) }?.balance ?: 0.0
        if (fromBal <= 0.0 || fromAmount > fromBal) return

        val toAmount = if (explicitToAmount != null && explicitToAmount > 0.0) explicitToAmount else fromAmount * rate
        val fAcctId = fromAccountId ?: 0L
        val tAcctId = toAccountId ?: fAcctId
        viewModelScope.launch {
            repository.executeCurrencyExchange(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = fromAmount,
                toAmount = toAmount,
                rate = if (rate > 0.0) rate else (toAmount / fromAmount),
                fromAccountId = fAcctId,
                toAccountId = tAcctId
            )
            onSuccess()
        }
    }

    fun executeCashCardTransfer(
        isCashToCard: Boolean,
        cashCurrency: CurrencyEntity,
        cardAccount: AccountCardEntity,
        cashAmount: Double,
        cardAmount: Double,
        rate: Double,
        targetCurrency: CurrencyEntity? = null,
        note: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (cashAmount <= 0.0 || cardAmount <= 0.0) return
        if (isCashToCard) {
            val cashBal = currencyBalances.value.find { (cashCurrency.id > 0 && it.currency.id == cashCurrency.id) || it.currency.code.equals(cashCurrency.code, ignoreCase = true) }?.balance ?: 0.0
            if (cashBal <= 0.0 || cashAmount > cashBal) return
        } else {
            if (cardAccount.balance <= 0.0 || cardAmount > cardAccount.balance) return
        }
        viewModelScope.launch {
            repository.executeCashCardTransfer(
                isCashToCard = isCashToCard,
                cashCurrency = cashCurrency,
                cardAccount = cardAccount,
                cashAmount = cashAmount,
                cardAmount = cardAmount,
                rate = rate,
                targetCurrency = targetCurrency,
                note = note
            )
            onSuccess()
        }
    }

    fun updateCashCardTransfer(
        txn1Id: Long,
        txn2Id: Long,
        oldIsCashToCard: Boolean,
        oldCardAccountId: Long,
        oldCardAmount: Double,
        newIsCashToCard: Boolean,
        newCashCurrency: CurrencyEntity,
        newCardAccount: AccountCardEntity,
        newCashAmount: Double,
        newCardAmount: Double,
        newRate: Double,
        newTargetCurrency: CurrencyEntity? = null,
        newNote: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (newCashAmount <= 0.0 || newCardAmount <= 0.0) return
        viewModelScope.launch {
            repository.updateCashCardTransfer(
                txn1Id = txn1Id,
                txn2Id = txn2Id,
                oldIsCashToCard = oldIsCashToCard,
                oldCardAccountId = oldCardAccountId,
                oldCardAmount = oldCardAmount,
                newIsCashToCard = newIsCashToCard,
                newCashCurrency = newCashCurrency,
                newCardAccount = newCardAccount,
                newCashAmount = newCashAmount,
                newCardAmount = newCardAmount,
                newRate = newRate,
                newTargetCurrency = newTargetCurrency,
                newNote = newNote
            )
            onSuccess()
        }
    }

    fun executeWalletTransfer(
        fromAccount: AccountCardEntity,
        toAccount: AccountCardEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        note: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (fromAmount <= 0.0 || toAmount <= 0.0 || fromAccount.id == toAccount.id) return
        if (fromAccount.balance <= 0.0 || fromAmount > fromAccount.balance) return
        viewModelScope.launch {
            repository.executeWalletTransfer(
                fromAccount = fromAccount,
                toAccount = toAccount,
                fromAmount = fromAmount,
                toAmount = toAmount,
                rate = rate,
                note = note
            )
            onSuccess()
        }
    }

    suspend fun getLinkedTransferDetails(txn: TransactionEntity): Pair<TransactionEntity, TransactionEntity>? {
        return repository.getLinkedTransferTransactions(txn)
    }

    fun updateWalletTransfer(
        txn1Id: Long,
        txn2Id: Long,
        oldFromAccountId: Long,
        oldToAccountId: Long,
        oldFromAmount: Double,
        oldToAmount: Double,
        newFromAccount: AccountCardEntity,
        newToAccount: AccountCardEntity,
        newFromAmount: Double,
        newToAmount: Double,
        newRate: Double,
        newNote: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (newFromAmount <= 0.0 || newToAmount <= 0.0 || newFromAccount.id == newToAccount.id) return
        viewModelScope.launch {
            repository.updateWalletTransfer(
                txn1Id = txn1Id,
                txn2Id = txn2Id,
                oldFromAccountId = oldFromAccountId,
                oldToAccountId = oldToAccountId,
                oldFromAmount = oldFromAmount,
                oldToAmount = oldToAmount,
                newFromAccount = newFromAccount,
                newToAccount = newToAccount,
                newFromAmount = newFromAmount,
                newToAmount = newToAmount,
                newRate = newRate,
                newNote = newNote
            )
            onSuccess()
        }
    }

    fun updateCurrencyExchange(
        txn1Id: Long,
        txn2Id: Long,
        oldFromAccountId: Long,
        oldToAccountId: Long,
        oldFromAmount: Double,
        oldToAmount: Double,
        newFromAccountId: Long = 0L,
        newToAccountId: Long = 0L,
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        newFromAmount: Double,
        newToAmount: Double,
        newRate: Double,
        newNote: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (newFromAmount <= 0.0 || newToAmount <= 0.0) return
        viewModelScope.launch {
            repository.updateCurrencyExchange(
                txn1Id = txn1Id,
                txn2Id = txn2Id,
                oldFromAccountId = oldFromAccountId,
                oldToAccountId = oldToAccountId,
                oldFromAmount = oldFromAmount,
                oldToAmount = oldToAmount,
                newFromAccountId = newFromAccountId,
                newToAccountId = newToAccountId,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                newFromAmount = newFromAmount,
                newToAmount = newToAmount,
                newRate = newRate,
                newNote = newNote
            )
            onSuccess()
        }
    }

    fun transferPersonDebt(
        fromRecipientId: Long,
        fromPersonName: String,
        toRecipientId: Long,
        toPersonName: String,
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        note: String? = null,
        isDebtor: Boolean? = null,
        onSuccess: () -> Unit = {}
    ) {
        if (fromAmount <= 0.0 || toAmount <= 0.0) return
        if (fromRecipientId == toRecipientId) return
        val currentDebt = getRecipientDebtForCurrency(fromRecipientId, fromCurrency.code)
        if (currentDebt.isSettled || currentDebt.netAmount <= 0.0 || fromAmount > (currentDebt.netAmount + 0.01)) return
        val effectiveIsDebtor = isDebtor ?: currentDebt.isDebtor
        viewModelScope.launch {
            runCatching {
                repository.transferPersonDebt(
                    fromRecipientId = fromRecipientId,
                    fromPersonName = fromPersonName,
                    toRecipientId = toRecipientId,
                    toPersonName = toPersonName,
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    fromAmount = fromAmount,
                    toAmount = toAmount,
                    rate = rate,
                    note = note,
                    isDebtor = effectiveIsDebtor
                )
            }.onSuccess {
                onSuccess()
            }.onFailure { ex ->
                _operationErrorMessage.value = ex.message ?: "خطا در انتقال بدهی"
            }
        }
    }

    fun addCurrency(
        name: String,
        code: String,
        symbol: String,
        flagEmoji: String,
        colorHex: Long,
        rate: Double,
        decimalPlaces: Int = 0
    ) {
        viewModelScope.launch {
            repository.addCurrency(name, code, symbol, flagEmoji, colorHex, rate, isActive = true, decimalPlaces = decimalPlaces)
        }
    }

    fun updateCurrency(currency: CurrencyEntity) {
        viewModelScope.launch {
            repository.updateCurrency(currency)
        }
    }

    fun toggleCurrencyActive(currency: CurrencyEntity) {
        viewModelScope.launch {
            repository.toggleCurrencyActive(currency)
        }
    }

    fun deleteCurrency(currency: CurrencyEntity) {
        viewModelScope.launch {
            if (currency.isBaseCurrency) {
                _operationErrorMessage.value = "امکان حذف ارز پایه اصلی وجود ندارد."
                return@launch
            }
            val count = repository.countUsageForCurrency(currency.id, currency.code)
            if (count > 0) {
                _operationErrorMessage.value = "امکان حذف ارز «${currency.name}» وجود ندارد زیرا در $count تراکنش یا حساب بانکی استفاده شده است."
                return@launch
            }
            repository.deleteCurrency(currency)
        }
    }

    val hasCurrencyDependentData: StateFlow<Boolean> = combine(
        transactions,
        accounts
    ) { txns, accs ->
        txns.isNotEmpty() ||
        accs.any { kotlin.math.abs(it.balance) > 0.0001 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setBaseCurrency(currencyCode: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.setBaseCurrency(currencyCode)
            result.onSuccess {
                _operationErrorMessage.value = null
                onSuccess()
            }.onFailure { err ->
                _operationErrorMessage.value = err.message ?: "خطا در تنظیم ارز پایه"
                onError(err.message ?: "خطا در تنظیم ارز پایه")
            }
        }
    }

    fun clearTransactionsAndBalancesForBaseCurrencyChange(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.clearTransactionsAndBalancesOnly()
            onSuccess()
        }
    }

    fun selectCurrency(currency: CurrencyEntity) {
        _selectedCurrency.value = currency
    }

    fun toggleFreezeAccount(account: AccountCardEntity) {
        viewModelScope.launch {
            repository.toggleFreezeCard(account)
        }
    }

    fun addRecipient(
        name: String,
        handleOrPhone: String,
        avatarColorHex: Long = 0xFF21C6D8,
        iconName: String = "Person",
        isActive: Boolean = true,
        notes: String = "",
        isAmountProtected: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addRecipient(
                name = name,
                handleOrPhone = handleOrPhone,
                avatarColorHex = avatarColorHex,
                iconName = iconName,
                isActive = isActive,
                notes = notes,
                isAmountProtected = isAmountProtected
            )
        }
    }

    fun toggleRecipientActive(recipient: RecipientEntity) {
        viewModelScope.launch {
            repository.updateRecipient(recipient.copy(isActive = !recipient.isActive))
        }
    }

    fun updateRecipient(recipient: RecipientEntity) {
        viewModelScope.launch {
            repository.updateRecipient(recipient)
        }
    }

    fun deleteRecipient(recipient: RecipientEntity) {
        viewModelScope.launch {
            val count = repository.countTransactionsForRecipient(recipient.id)
            if (count > 0) {
                _operationErrorMessage.value = "امکان حذف «${recipient.name}» وجود ندارد زیرا $count تراکنش به نام این شخص در دیتابیس ثبت شده است."
                return@launch
            }
            repository.deleteRecipient(recipient)
        }
    }

    fun updateProfile(name: String, avatarEmoji: String, colorHex: Long) {
        _userName.value = name
        _userAvatarEmoji.value = avatarEmoji
        _userAvatarColor.value = colorHex
        prefs.edit()
            .putString("user_name", name)
            .putString("user_avatar_emoji", avatarEmoji)
            .putLong("user_avatar_color", colorHex)
            .apply()
    }

    fun addCategory(
        name: String,
        type: TransactionType,
        iconName: String,
        colorHex: Long,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            repository.addCategory(name, type, iconName, colorHex, isActive)
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val count = repository.countTransactionsForCategory(category.id, category.name)
            if (count > 0) {
                _operationErrorMessage.value = "امکان حذف دسته‌بندی «${category.name}» وجود ندارد زیرا $count تراکنش در این دسته‌بندی ثبت شده است."
                return@launch
            }
            repository.deleteCategory(category)
        }
    }

    fun toggleCategoryStatus(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isActive = !category.isActive))
        }
    }

    fun setCategoryBudget(category: String, limit: Double, currencyCode: String = "AFN", categoryId: Long? = null, currencyId: Long = 0L) {
        val cId = if (currencyId > 0L) currencyId else (activeCurrencies.value.firstOrNull { it.code.equals(currencyCode, ignoreCase = true) }?.id ?: 0L)
        viewModelScope.launch {
            repository.setBudget(category, limit, currencyCode = currencyCode, categoryId = categoryId, currencyId = cId)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    /**
     * Calculates the budget status for a given category.
     * Evaluates whether a pending expense will exceed the monthly budget.
     * Converts all cross-currency transactions into base currency for accurate budgeting.
     */
    fun getCategoryBudgetStatus(
        category: String,
        pendingAmount: Double = 0.0,
        pendingExchangeRate: Double = 1.0,
        categoryId: Long? = null
    ): BudgetStatus? {
        val budget = if (categoryId != null && categoryId > 0) {
            budgets.value.firstOrNull { it.categoryId == categoryId }
        } else {
            budgets.value.firstOrNull { it.categoryId == null && it.category.equals(category, ignoreCase = true) }
        } ?: return null

        val startOfMonth = PersianDateHelper.getStartOfCurrentMonth(_selectedCalendarType.value)
        val endOfMonth = PersianDateHelper.getEndOfCurrentMonth(_selectedCalendarType.value)

        val baseCurr = activeCurrencies.value.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.value.firstOrNull()
        val targetCatId = categoryId ?: budget.categoryId

        // Calculate all spending for this category in base currency using categoryId strictly when available
        val currentSpent = transactions.value
            .filter { txn ->
                txn.type == TransactionType.EXPENSE && 
                txn.timestamp in startOfMonth..endOfMonth &&
                (
                    if (targetCatId != null && targetCatId > 0) txn.categoryId == targetCatId
                    else txn.categoryId == null && txn.category.equals(category, ignoreCase = true)
                )
            }
            .sumOf { txn ->
                val rate = when {
                    baseCurr != null && ((txn.currencyId > 0 && txn.currencyId == baseCurr.id) || txn.currencyCode.equals(baseCurr.code, ignoreCase = true)) -> 1.0
                    txn.exchangeRate > 0.0 -> txn.exchangeRate
                    else -> {
                        val curr = if (txn.currencyId > 0L) activeCurrencies.value.firstOrNull { it.id == txn.currencyId } else activeCurrencies.value.firstOrNull { it.code.equals(txn.currencyCode, ignoreCase = true) }
                        curr?.exchangeRateToBase ?: 1.0
                    }
                }
                txn.amount * rate
            }

        val pendingInBase = pendingAmount * pendingExchangeRate
        val remainingBefore = budget.monthlyLimit - currentSpent
        val remainingAfter = remainingBefore - pendingInBase
        val isOverBudget = remainingAfter < 0.0
        val totalSpentProjected = currentSpent + pendingInBase
        val pct = if (budget.monthlyLimit > 0) (totalSpentProjected / budget.monthlyLimit).toFloat() else 0f

        return BudgetStatus(
            categoryId = targetCatId,
            category = category.ifBlank { budget.category },
            monthlyLimit = budget.monthlyLimit,
            currentSpent = currentSpent,
            pendingAmount = pendingInBase,
            remainingBefore = remainingBefore,
            remainingAfter = remainingAfter,
            isOverBudget = isOverBudget,
            percentageUsed = pct.coerceIn(0f, 2f),
            currencyCode = baseCurr?.code ?: budget.currencyCode
        )
    }

    /**
     * Converts a person's debt/credit from one currency to another without cross-currency conflict.
     */
    fun convertPersonDebtCurrency(
        recipientId: Long,
        personName: String,
        fromCurrency: CurrencyEntity,
        toCurrency: CurrencyEntity,
        fromAmount: Double,
        toAmount: Double,
        rate: Double,
        accountId: Long,
        isDebtor: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.convertPersonDebtCurrency(
                recipientId = recipientId,
                personName = personName,
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = fromAmount,
                toAmount = toAmount,
                rate = rate,
                accountId = accountId,
                isDebtor = isDebtor
            )
            onSuccess()
        }
    }

    /**
     * Settles debt/credit for a specific recipient identified strictly by recipientId.
     */
    fun settleRecipientDebt(
        recipientId: Long,
        recipientName: String,
        debtCurrencyCode: String,
        debtAmountToSettle: Double,
        paymentAccountId: Long = 0L,
        paymentCurrencyCode: String,
        paymentAmount: Double,
        exchangeRate: Double = 1.0,
        isClaimSettlement: Boolean,
        originalTransactionId: Long? = null,
        note: String? = null,
        debtCurrencyId: Long = 0L,
        paymentCurrencyId: Long = 0L,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val dId = if (debtCurrencyId > 0L) debtCurrencyId else (activeCurrencies.value.firstOrNull { it.code.equals(debtCurrencyCode, ignoreCase = true) }?.id ?: 0L)
        val pId = if (paymentCurrencyId > 0L) paymentCurrencyId else (activeCurrencies.value.firstOrNull { it.code.equals(paymentCurrencyCode, ignoreCase = true) }?.id ?: 0L)
        viewModelScope.launch {
            runCatching {
                repository.settleRecipientDebt(
                    recipientId = recipientId,
                    recipientName = recipientName,
                    debtCurrencyId = dId,
                    debtCurrencyCode = debtCurrencyCode,
                    debtAmountToSettle = debtAmountToSettle,
                    paymentAccountId = paymentAccountId,
                    paymentCurrencyId = pId,
                    paymentCurrencyCode = paymentCurrencyCode,
                    paymentAmount = paymentAmount,
                    exchangeRate = exchangeRate,
                    isClaimSettlement = isClaimSettlement,
                    originalTransactionId = originalTransactionId,
                    note = note
                )
            }.onSuccess {
                onSuccess()
            }.onFailure { ex ->
                val msg = ex.message ?: "خطا در تسویه حساب"
                _operationErrorMessage.value = msg
                onError(msg)
            }
        }
    }

    /**
     * Multi-currency debts for a specific recipient identified strictly by recipientId.
     * Respects ledgerCurrencyCode and ledgerAmount if the transaction was recorded in another ledger currency.
     * Returns ONLY currencies where debt/credit != 0 (if 0, returns empty so nothing is shown).
     */
    fun getRecipientMultiCurrencyDebts(recipientId: Long): List<RecipientCurrencyDebt> {
        val txns = allTransactions.value.filter { txn ->
            txn.recipientId == recipientId
        }
        val isProtected = recipients.value.firstOrNull { it.id == recipientId }?.isAmountProtected ?: false
        val isMasked = isProtected && !_unmaskedRecipientIds.value.contains(recipientId)
        return calculateMultiCurrencyDebts(txns, isProtected, isMasked)
    }

    /**
     * Multi-currency debts by recipientName (resolves recipientId if unique, never combines duplicate names).
     */
    fun getRecipientMultiCurrencyDebts(recipientName: String): List<RecipientCurrencyDebt> {
        val matched = recipients.value.filter { it.name.equals(recipientName, ignoreCase = true) }
        return if (matched.size == 1) {
            getRecipientMultiCurrencyDebts(matched.first().id)
        } else {
            emptyList()
        }
    }

    private fun calculateMultiCurrencyDebts(
        txns: List<TransactionEntity>,
        isProtected: Boolean = false,
        isMasked: Boolean = false
    ): List<RecipientCurrencyDebt> {
        data class TxnLedgerItem(
            val currencyId: Long,
            val currencyCode: String,
            val currencySymbol: String,
            val amount: Double,
            val type: TransactionType
        )
        val allCurrs = currencies.value
        val ledgerItems = txns.map { txn ->
            if (!txn.ledgerCurrencyCode.isNullOrBlank() && txn.ledgerAmount != null && txn.ledgerAmount > 0.0) {
                val ledgId = txn.ledgerCurrencyId ?: (allCurrs.firstOrNull { it.code.equals(txn.ledgerCurrencyCode, ignoreCase = true) }?.id ?: 0L)
                val curr = if (ledgId > 0L) allCurrs.firstOrNull { it.id == ledgId } else allCurrs.firstOrNull { it.code.equals(txn.ledgerCurrencyCode, ignoreCase = true) }
                TxnLedgerItem(
                    currencyId = curr?.id ?: ledgId,
                    currencyCode = curr?.code?.uppercase() ?: txn.ledgerCurrencyCode.uppercase(),
                    currencySymbol = curr?.symbol ?: txn.ledgerCurrencySymbol ?: txn.ledgerCurrencyCode,
                    amount = txn.ledgerAmount,
                    type = txn.type
                )
            } else {
                val cId = if (txn.currencyId > 0L) txn.currencyId else (allCurrs.firstOrNull { it.code.equals(txn.currencyCode, ignoreCase = true) }?.id ?: 0L)
                val curr = if (cId > 0L) allCurrs.firstOrNull { it.id == cId } else allCurrs.firstOrNull { it.code.equals(txn.currencyCode, ignoreCase = true) }
                TxnLedgerItem(
                    currencyId = curr?.id ?: cId,
                    currencyCode = curr?.code?.uppercase() ?: txn.currencyCode.uppercase(),
                    currencySymbol = curr?.symbol ?: txn.currencySymbol,
                    amount = txn.amount,
                    type = txn.type
                )
            }
        }
        val grouped = ledgerItems.groupBy { if (it.currencyId > 0L) it.currencyId.toString() else it.currencyCode }
        val result = mutableListOf<RecipientCurrencyDebt>()

        for ((_, currTxns) in grouped) {
            val lent = currTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val borrowed = currTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val net = lent - borrowed
            if (kotlin.math.abs(net) >= 0.01) {
                val firstItem = currTxns.first()
                val currEntity = if (firstItem.currencyId > 0L) allCurrs.firstOrNull { it.id == firstItem.currencyId } else allCurrs.firstOrNull { it.code.equals(firstItem.currencyCode, ignoreCase = true) }
                val code = currEntity?.code ?: firstItem.currencyCode
                val sym = currEntity?.symbol ?: firstItem.currencySymbol
                val cId = currEntity?.id ?: firstItem.currencyId
                val isDebtor = net > 0.0 // We paid money: Receivable (دریافتنی ما / طلب ما)
                val absAmount = kotlin.math.abs(net)
                val formattedAmt = formatAmount(absAmount, code)
                val realBadgeText = if (isDebtor) "طلب: \u200E$sym $formattedAmt\u200E" else "بدهی: \u200E$sym $formattedAmt\u200E"
                val maskedBadgeText = if (isDebtor) "طلب: **** $sym" else "بدهی: **** $sym"
                val badgeText = if (isMasked) maskedBadgeText else realBadgeText
                result.add(
                    RecipientCurrencyDebt(
                        currencyId = cId,
                        currencyCode = code,
                        currencySymbol = sym,
                        netAmount = net,
                        isDebtor = isDebtor,
                        formattedAmount = formattedAmt,
                        statusBadgeText = badgeText,
                        isProtected = isProtected,
                        isMasked = isMasked,
                        maskedFormattedAmount = "**** $sym",
                        maskedStatusBadgeText = maskedBadgeText
                    )
                )
            }
        }
        return result
    }

    val recipientDebtsMap: StateFlow<Map<Long, List<RecipientCurrencyDebt>>> = combine(
        allTransactions,
        recipients,
        _unmaskedRecipientIds
    ) { txns, recList, unmaskedIds ->
        val txnsByRecipient = txns.filter { it.recipientId != null }.groupBy { it.recipientId!! }
        recList.associate { rec ->
            val recTxns = txnsByRecipient[rec.id] ?: emptyList()
            val isProtected = rec.isAmountProtected
            val isMasked = isProtected && !unmaskedIds.contains(rec.id)
            rec.id to calculateMultiCurrencyDebts(recTxns, isProtected, isMasked)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /**
     * Data class holding the aggregated totals of all recipient accounts converted to Base Currency.
     */
    data class RecipientsBaseCurrencySummary(
        val baseCurrencyCode: String,
        val baseCurrencySymbol: String,
        val totalReceivableInBase: Double, // جمله طلب به ارز پایه (طلبکار از دیگران)
        val totalPayableInBase: Double,    // جمله بدهی به ارز پایه (بدهکار به دیگران)
        val netBalanceInBase: Double,      // جمله خالص حسابات (طلب منهای بدهی)
        val formattedTotalReceivable: String,
        val formattedTotalPayable: String,
        val formattedNetBalance: String,
        val hasMaskedProtectedRecipients: Boolean,
        val totalDebtorsCount: Int,  // تعداد اشخاص بدهکار به ما (طلب ما)
        val totalCreditorsCount: Int // تعداد اشخاص طلبکار از ما (بدهی ما)
    )

    /**
     * Calculates the total receivables, total payables, and net balance of accounts
     * strictly in Base Currency for the given list of recipients (using recipient.id).
     * Strictly respects multi-currency rules, converting each currency debt to Base Currency
     * using the exact exchange rate to base.
     */
    fun getRecipientsBaseCurrencySummary(recipientsList: List<RecipientEntity>): RecipientsBaseCurrencySummary {
        val baseCurr = activeCurrencies.value.firstOrNull { it.isBaseCurrency }
            ?: activeCurrencies.value.firstOrNull()
            ?: CurrencyEntity(name = "افغانی", code = "AFN", symbol = "؋")

        var totalReceivableInBase = 0.0
        var totalPayableInBase = 0.0
        var debtorsCount = 0
        var creditorsCount = 0
        var hasMaskedProtected = false

        val currentUnmasked = _unmaskedRecipientIds.value
        val allTxns = allTransactions.value
        val txnsByRecipient = allTxns.filter { it.recipientId != null }.groupBy { it.recipientId!! }
        val currencyRates = activeCurrencies.value.associate { it.code.uppercase() to it.exchangeRateToBase }
        val allCurrs = activeCurrencies.value

        for (r in recipientsList) {
            val isProtected = r.isAmountProtected
            val isMasked = isProtected && !currentUnmasked.contains(r.id)

            val rTxns = txnsByRecipient[r.id] ?: emptyList()
            if (rTxns.isEmpty()) continue

            // Ledger items grouped by currencyId (with code fallback)
            val ledgerItems = rTxns.map { txn ->
                if (!txn.ledgerCurrencyCode.isNullOrBlank() && txn.ledgerAmount != null && txn.ledgerAmount > 0.0) {
                    val ledgId = txn.ledgerCurrencyId ?: (allCurrs.firstOrNull { it.code.equals(txn.ledgerCurrencyCode, ignoreCase = true) }?.id ?: 0L)
                    val curr = if (ledgId > 0L) allCurrs.firstOrNull { it.id == ledgId } else allCurrs.firstOrNull { it.code.equals(txn.ledgerCurrencyCode, ignoreCase = true) }
                    Triple(
                        curr?.id ?: ledgId,
                        txn.ledgerAmount,
                        txn.type
                    )
                } else {
                    val cId = if (txn.currencyId > 0L) txn.currencyId else (allCurrs.firstOrNull { it.code.equals(txn.currencyCode, ignoreCase = true) }?.id ?: 0L)
                    val curr = if (cId > 0L) allCurrs.firstOrNull { it.id == cId } else allCurrs.firstOrNull { it.code.equals(txn.currencyCode, ignoreCase = true) }
                    Triple(
                        curr?.id ?: cId,
                        txn.amount,
                        txn.type
                    )
                }
            }

            val grouped = ledgerItems.groupBy { it.first }
            var personHasDebt = false
            var personNetInBase = 0.0

            for ((currId, txList) in grouped) {
                val lent = txList.filter { it.third == TransactionType.EXPENSE }.sumOf { it.second }
                val borrowed = txList.filter { it.third == TransactionType.INCOME }.sumOf { it.second }
                val net = lent - borrowed
                if (kotlin.math.abs(net) >= 0.01) {
                    personHasDebt = true
                    val curr = allCurrs.firstOrNull { it.id == currId }
                    val isBase = (baseCurr.id > 0 && currId == baseCurr.id) || (curr != null && curr.code.equals(baseCurr.code, ignoreCase = true))
                    val rate = if (isBase) {
                        1.0
                    } else {
                        curr?.exchangeRateToBase ?: (if (curr != null) currencyRates[curr.code.uppercase()] else 1.0) ?: 1.0
                    }
                    val amountInBase = kotlin.math.abs(net) * rate
                    personNetInBase += (net * rate)

                    if (net > 0.0) {
                        // Receivable (طلب ما از این شخص)
                        totalReceivableInBase += amountInBase
                    } else {
                        // Payable (بدهی ما به این شخص)
                        totalPayableInBase += amountInBase
                    }
                }
            }

            if (personHasDebt) {
                if (isMasked) {
                    hasMaskedProtected = true
                }
                if (personNetInBase > 0.01) {
                    debtorsCount++
                } else if (personNetInBase < -0.01) {
                    creditorsCount++
                }
            }
        }

        val netBalanceInBase = totalReceivableInBase - totalPayableInBase

        return RecipientsBaseCurrencySummary(
            baseCurrencyCode = baseCurr.code,
            baseCurrencySymbol = baseCurr.symbol,
            totalReceivableInBase = totalReceivableInBase,
            totalPayableInBase = totalPayableInBase,
            netBalanceInBase = netBalanceInBase,
            formattedTotalReceivable = formatAmount(totalReceivableInBase, baseCurr.code),
            formattedTotalPayable = formatAmount(totalPayableInBase, baseCurr.code),
            formattedNetBalance = formatAmount(kotlin.math.abs(netBalanceInBase), baseCurr.code),
            hasMaskedProtectedRecipients = hasMaskedProtected,
            totalDebtorsCount = debtorsCount,
            totalCreditorsCount = creditorsCount
        )
    }

    /**
     * Unmasks all protected recipients in current session (e.g. after authentication on summary card)
     */
    fun unmaskAllRecipients() {
        val allProtectedIds = recipients.value.filter { it.isAmountProtected }.map { it.id }
        _unmaskedRecipientIds.update { it + allProtectedIds }
    }

    /**
     * Returns recipient debt status strictly for the target currency (no fallback to other currencies).
     */
    fun getRecipientDebtForCurrency(recipientId: Long, targetCurrencyCode: String): RecipientDebtInfo {
        val multiDebts = getRecipientMultiCurrencyDebts(recipientId)
        val debt = multiDebts.firstOrNull { it.currencyCode.equals(targetCurrencyCode, ignoreCase = true) }
        val isProtected = recipients.value.firstOrNull { it.id == recipientId }?.isAmountProtected ?: false
        val isMasked = isProtected && !_unmaskedRecipientIds.value.contains(recipientId)
        if (debt == null) {
            return RecipientDebtInfo(
                netAmount = 0.0,
                statusText = "تسویه",
                isDebtor = false,
                isSettled = true,
                isProtected = isProtected,
                isMasked = isMasked
            )
        }
        return RecipientDebtInfo(
            netAmount = kotlin.math.abs(debt.netAmount),
            statusText = debt.statusBadgeText,
            isDebtor = debt.isDebtor,
            isSettled = false,
            isProtected = debt.isProtected,
            isMasked = debt.isMasked
        )
    }

    fun getRecipientDebtForCurrency(recipientName: String, targetCurrencyCode: String): RecipientDebtInfo {
        val matched = recipients.value.filter { it.name.equals(recipientName, ignoreCase = true) }
        return if (matched.size == 1) {
            getRecipientDebtForCurrency(matched.first().id, targetCurrencyCode)
        } else {
            RecipientDebtInfo(
                netAmount = 0.0,
                statusText = "تسویه",
                isDebtor = false,
                isSettled = true,
                isProtected = false,
                isMasked = false
            )
        }
    }

    fun getRecipientDebtInfo(recipientId: Long): RecipientDebtInfo {
        val isProtected = recipients.value.firstOrNull { it.id == recipientId }?.isAmountProtected ?: false
        val isMasked = isProtected && !_unmaskedRecipientIds.value.contains(recipientId)
        val multiDebts = getRecipientMultiCurrencyDebts(recipientId)
        if (multiDebts.isEmpty()) {
            return RecipientDebtInfo(
                netAmount = 0.0,
                statusText = "تسویه",
                isDebtor = false,
                isSettled = true,
                isProtected = isProtected,
                isMasked = isMasked
            )
        }
        val first = multiDebts.first()
        return RecipientDebtInfo(
            netAmount = kotlin.math.abs(first.netAmount),
            statusText = first.statusBadgeText,
            isDebtor = first.isDebtor,
            isSettled = false,
            isProtected = first.isProtected,
            isMasked = first.isMasked
        )
    }

    fun getRecipientDebtInfo(recipientName: String): RecipientDebtInfo {
        val matched = recipients.value.filter { it.name.equals(recipientName, ignoreCase = true) }
        return if (matched.size == 1) {
            getRecipientDebtInfo(matched.first().id)
        } else {
            RecipientDebtInfo(
                netAmount = 0.0,
                statusText = "تسویه",
                isDebtor = false,
                isSettled = true,
                isProtected = false,
                isMasked = false
            )
        }
    }

    fun getCurrencyDecimals(currencyCode: String? = null): Int {
        val code = currencyCode ?: selectedCurrency.value?.code ?: "AFN"
        val curr = activeCurrencies.value.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: currencies.value.firstOrNull { it.code.equals(code, ignoreCase = true) }
        return curr?.decimalPlaces ?: 3
    }

    fun roundToCurrencyDecimals(amount: Double, currencyCode: String? = null): Double {
        val decimals = maxOf(getCurrencyDecimals(currencyCode), 3)
        val factor = Math.pow(10.0, decimals.coerceIn(3, 6).toDouble())
        return kotlin.math.round(amount * factor) / factor
    }

    fun formatAmount(amount: Double, currencyCode: String? = null): String {
        val decimals = getCurrencyDecimals(currencyCode)
        val hasFraction = kotlin.math.abs(amount - kotlin.math.round(amount)) > 0.00001
        val pattern = when {
            hasFraction -> "#,##0.###"
            decimals >= 3 -> "#,##0.###"
            decimals == 2 -> "#,##0.##"
            decimals == 1 -> "#,##0.#"
            else -> "#,##0"
        }
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        formatter.maximumFractionDigits = maxOf(decimals, 3)
        return formatter.format(amount)
    }

    fun wipeAllData(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                prefs.edit().putBoolean("db_initialized_once", true).apply()
                repository.wipeAllData()
                _selectedRecipient.value = null
                _isGeneralSelected.value = false
                _transactionNote.value = ""
                _selectedCategory.value = ""
                onClear()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در حذف اطلاعات")
            }
        }
    }

    fun resetToDefaultData(onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                prefs.edit().putBoolean("db_initialized_once", true).apply()
                repository.wipeAllData()
                repository.seedInitialDataIfEmpty(force = true)
                _selectedRecipient.value = null
                _isGeneralSelected.value = false
                _transactionNote.value = ""
                _selectedCategory.value = ""
                onClear()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در بازنشانی اطلاعات پیش‌فرض")
            }
        }
    }

    fun loadLocalBackups() {
        viewModelScope.launch {
            val list = BackupManager.listBackupFiles(getApplication())
            _localBackups.value = list
        }
    }

    fun createLocalBackup(onSuccess: (BackupFileInfo) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isOperatingBackup.value = true
            try {
                val data = repository.getBackupData()
                val file = BackupManager.saveBackupFileToDevice(getApplication(), data, isAuto = false)
                loadLocalBackups()
                _isOperatingBackup.value = false
                val info = BackupManager.listBackupFiles(getApplication()).find { it.name == file.name }
                    ?: BackupFileInfo(
                        name = file.name,
                        path = file.absolutePath,
                        sizeFormatted = "${(file.length() / 1024.0).toInt()} KB",
                        sizeBytes = file.length(),
                        modifiedJalali = com.example.util.PersianDateHelper.formatSolarDateTime(file.lastModified()),
                        modifiedMillis = file.lastModified(),
                        isAutoBackup = false,
                        file = file
                    )
                onSuccess(info)
            } catch (e: Exception) {
                _isOperatingBackup.value = false
                onError(e.message ?: "خطا در ایجاد فایل پشتیبان")
            }
        }
    }

    fun deleteLocalBackup(file: File) {
        viewModelScope.launch {
            BackupManager.deleteBackupFile(file)
            loadLocalBackups()
        }
    }

    fun restoreFromBackupFile(file: File, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isOperatingBackup.value = true
            try {
                val content = file.readText(Charsets.UTF_8)
                val res = repository.restoreBackup(content)
                _isOperatingBackup.value = false
                res.fold(
                    onSuccess = { msg ->
                        _selectedRecipient.value = null
                        _isGeneralSelected.value = false
                        onClear()
                        onSuccess(msg)
                    },
                    onFailure = { err ->
                        onError(err.message ?: "فرمت فایل پشتیبان نامعتبر است.")
                    }
                )
            } catch (e: Exception) {
                _isOperatingBackup.value = false
                onError(e.message ?: "خطا در خواندن و بازیابی فایل")
            }
        }
    }

    fun updateAutoBackupSettings(enabled: Boolean, frequency: String, destination: String = "local") {
        _isAutoBackupEnabled.value = enabled
        _autoBackupFrequency.value = frequency
        _autoBackupDestination.value = destination
        autoBackupPrefs.edit()
            .putBoolean("enabled", enabled)
            .putString("frequency", frequency)
            .putString("destination", destination)
            .apply()
    }

    fun checkAndPerformAutoBackup(triggerIsTransaction: Boolean = false) {
        if (!_isAutoBackupEnabled.value) return
        val now = System.currentTimeMillis()
        val lastTime = _lastAutoBackupTime.value
        val freq = _autoBackupFrequency.value

        val shouldRun = when (freq) {
            "daily" -> (now - lastTime) >= 24 * 60 * 60 * 1000L
            "weekly" -> (now - lastTime) >= 7 * 24 * 60 * 60 * 1000L
            "per_5_tx" -> triggerIsTransaction
            else -> (now - lastTime) >= 24 * 60 * 60 * 1000L
        }

        if (shouldRun) {
            viewModelScope.launch {
                try {
                    val data = repository.getBackupData()
                    BackupManager.saveBackupFileToDevice(getApplication(), data, isAuto = true)
                    loadLocalBackups()
                    _lastAutoBackupTime.value = now
                    autoBackupPrefs.edit().putLong("last_time", now).apply()
                } catch (_: Exception) {}
            }
        }
    }

    fun exportBackup(onResult: (String) -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val bak = repository.exportBackup()
                onResult(bak)
            } catch (e: Exception) {
                onError(e.message ?: "خطا در تهیه نسخه پشتیبان")
            }
        }
    }

    fun restoreBackup(jsonString: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.restoreBackup(jsonString)
                res.fold(
                    onSuccess = { msg ->
                        _selectedRecipient.value = null
                        _isGeneralSelected.value = false
                        onClear()
                        onSuccess(msg)
                    },
                    onFailure = { err ->
                        onError(err.message ?: "فرمت فایل پشتیبان نامعتبر است.")
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "خطا در بازیابی نسخه پشتیبان")
            }
        }
    }

    // ==========================================
    // FINANCIAL GOALS OPERATIONS (اهداف مالی)
    // ==========================================

    fun getGoalByIdFlow(id: Long): Flow<FinancialGoalEntity?> = repository.getGoalByIdFlow(id)

    fun getTransactionsForGoal(goalId: Long): Flow<List<GoalTransactionEntity>> =
        repository.getTransactionsForGoal(goalId)

    fun createGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double = 0.0,
        currencyCode: String = "AFN",
        currencySymbol: String = "؋",
        iconName: String = "Savings",
        colorHex: Long = 0xFF10B981,
        category: String = "پس‌انداز عمومی",
        startDate: Long = System.currentTimeMillis(),
        targetDate: Long? = null,
        linkedAccountId: Long? = null,
        description: String = "",
        reminderFrequency: GoalReminderFrequency = GoalReminderFrequency.NONE,
        reminderDate: Long? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (title.isBlank()) {
            onError("لطفاً نام هدف را وارد کنید")
            return
        }
        if (targetAmount <= 0) {
            onError("مبلغ هدف باید بیشتر از صفر باشد")
            return
        }
        if (currentAmount < 0) {
            onError("مبلغ فعلی نمی‌تواند منفی باشد")
            return
        }
        viewModelScope.launch {
            try {
                val isCompleted = currentAmount >= targetAmount
                val status = if (isCompleted) GoalStatus.COMPLETED else GoalStatus.ACTIVE
                val completedDate = if (isCompleted) System.currentTimeMillis() else null

                val goal = FinancialGoalEntity(
                    title = title.trim(),
                    description = description.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    currencyCode = currencyCode,
                    currencySymbol = currencySymbol,
                    iconName = iconName,
                    colorHex = colorHex,
                    category = category,
                    startDate = startDate,
                    targetDate = targetDate,
                    linkedAccountId = linkedAccountId,
                    status = status,
                    completedDate = completedDate,
                    reminderFrequency = reminderFrequency,
                    reminderDate = reminderDate
                )
                repository.addGoal(goal, linkedAccountId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در ایجاد هدف مالی")
            }
        }
    }

    fun updateGoal(
        goal: FinancialGoalEntity,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (goal.title.isBlank()) {
            onError("نام هدف نمی‌تواند خالی باشد")
            return
        }
        if (goal.targetAmount <= 0) {
            onError("مبلغ هدف باید بیشتر از صفر باشد")
            return
        }
        viewModelScope.launch {
            try {
                val isCompleted = goal.currentAmount >= goal.targetAmount
                val updatedStatus = if (isCompleted && goal.status == GoalStatus.ACTIVE) GoalStatus.COMPLETED else goal.status
                val completedDate = if (isCompleted && goal.status == GoalStatus.ACTIVE) System.currentTimeMillis() else goal.completedDate
                repository.updateGoal(goal.copy(status = updatedStatus, completedDate = completedDate))
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در ویرایش هدف مالی")
            }
        }
    }

    fun deleteGoal(
        goalId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goalId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در حذف هدف مالی")
            }
        }
    }

    fun depositToGoal(
        goalId: Long,
        amount: Double,
        accountId: Long?,
        note: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (amount <= 0) {
            val err = "مبلغ واریزی باید بزرگتر از صفر باشد"
            _operationErrorMessage.value = err
            onError(err)
            return
        }
        viewModelScope.launch {
            val res = repository.depositToGoal(goalId, amount, accountId, note)
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = {
                    val msg = it.message ?: "خطا در واریز به هدف"
                    _operationErrorMessage.value = msg
                    onError(msg)
                }
            )
        }
    }

    fun withdrawFromGoal(
        goalId: Long,
        amount: Double,
        destinationAccountId: Long?,
        note: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (amount <= 0) {
            val err = "مبلغ برداشت باید بزرگتر از صفر باشد"
            _operationErrorMessage.value = err
            onError(err)
            return
        }
        viewModelScope.launch {
            val res = repository.withdrawFromGoal(goalId, amount, destinationAccountId, note)
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = {
                    val msg = it.message ?: "خطا در برداشت از هدف"
                    _operationErrorMessage.value = msg
                    onError(msg)
                }
            )
        }
    }

    fun updateGoalStatus(
        goalId: Long,
        newStatus: GoalStatus,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.updateGoalStatus(goalId, newStatus)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "خطا در تغییر وضعیت هدف")
            }
        }
    }

    fun deleteGoalTransaction(
        transactionId: Long,
        revertBalance: Boolean = true,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.deleteGoalTransaction(transactionId, revertBalance)
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "خطا در حذف تراکنش هدف") }
            )
        }
    }

    fun clearGoalTransactions(
        goalId: Long,
        revertBalance: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.clearGoalTransactions(goalId, revertBalance)
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "خطا در پاک‌سازی تاریخچه هدف") }
            )
        }
    }

    suspend fun findGoalTransactionForTransaction(txn: TransactionEntity): GoalTransactionEntity? {
        return repository.findGoalTransactionForTransaction(txn)
    }

    fun updateGoalTransaction(
        goalTransactionId: Long,
        newGoalId: Long,
        newAmount: Double,
        newType: GoalTransactionType,
        newAccountId: Long?,
        newNote: String,
        newTimestamp: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.updateGoalTransaction(
                goalTransactionId = goalTransactionId,
                newGoalId = newGoalId,
                newAmount = newAmount,
                newType = newType,
                newAccountId = newAccountId,
                newNote = newNote,
                newTimestamp = newTimestamp
            )
            res.fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "خطا در ویرایش عملیات هدف") }
            )
        }
    }

    // ==========================================
    // SHOPPING LISTS OPERATIONS
    // ==========================================

    fun saveShoppingList(
        list: ShoppingListEntity,
        items: List<ShoppingListItemEntity>,
        onComplete: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val id = repository.saveShoppingList(list, items)
            onComplete?.invoke(id)
        }
    }

    fun logShoppingListAsExpense(
        listId: Long,
        accountId: Long?,
        category: String,
        timestamp: Long,
        customNote: String? = null,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.logShoppingListAsExpense(listId, accountId, category, timestamp, customNote)
            result.fold(
                onSuccess = { onResult?.invoke(true, null) },
                onFailure = { onResult?.invoke(false, it.message) }
            )
        }
    }

    fun deleteShoppingList(
        listId: Long,
        deleteLinkedExpense: Boolean = false,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            repository.deleteShoppingList(listId, deleteLinkedExpense)
            onComplete?.invoke()
        }
    }

    fun toggleShoppingListItemPurchased(itemId: Long, isPurchased: Boolean) {
        viewModelScope.launch {
            repository.toggleShoppingListItemPurchased(itemId, isPurchased)
        }
    }

    fun getShoppingListForTransaction(transactionId: Long, onResult: (ShoppingListWithItems?) -> Unit) {
        viewModelScope.launch {
            val res = repository.findShoppingListForTransaction(transactionId)
            onResult(res)
        }
    }

    fun getShoppingListWithItemsById(listId: Long, onResult: (ShoppingListWithItems?) -> Unit) {
        viewModelScope.launch {
            val res = repository.getShoppingListWithItemsById(listId)
            onResult(res)
        }
    }
}


data class RecipientCurrencyDebt(
    val currencyId: Long = 0L,
    val currencyCode: String,
    val currencySymbol: String,
    val netAmount: Double,
    val isDebtor: Boolean, // true = person owes us (طلب ما), false = we owe them (بدهی ما)
    val formattedAmount: String,
    val statusBadgeText: String,
    val isProtected: Boolean = false,
    val isMasked: Boolean = false,
    val maskedFormattedAmount: String = "$currencyCode ****",
    val maskedStatusBadgeText: String = if (isDebtor) "طلب: $currencyCode ****" else "بدهی: $currencyCode ****"
) {
    val displayAmount: String
        get() = if (isMasked) maskedFormattedAmount else "$currencyCode $formattedAmount"

    val displayStatusText: String
        get() = if (isMasked) maskedStatusBadgeText else (if (isDebtor) "طلب: $currencyCode $formattedAmount" else "بدهی: $currencyCode $formattedAmount")
}

data class BudgetStatus(
    val categoryId: Long? = null,
    val category: String,
    val monthlyLimit: Double,
    val currentSpent: Double,
    val pendingAmount: Double,
    val remainingBefore: Double,
    val remainingAfter: Double,
    val isOverBudget: Boolean,
    val percentageUsed: Float,
    val currencyCode: String = "AFN"
)

data class RecipientDebtInfo(
    val netAmount: Double,
    val statusText: String,
    val isDebtor: Boolean,
    val isSettled: Boolean,
    val isProtected: Boolean = false,
    val isMasked: Boolean = false
)

enum class TransactionFilter {
    ALL,
    EXPENSE,
    INCOME
}
