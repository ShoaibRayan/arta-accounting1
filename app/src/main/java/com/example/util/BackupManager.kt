package com.example.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.example.data.local.AccountCardEntity
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
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionKind
import com.example.data.local.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

data class BackupData(
    val transactions: List<TransactionEntity>,
    val accounts: List<AccountCardEntity>,
    val recipients: List<RecipientEntity>,
    val currencies: List<CurrencyEntity>,
    val categories: List<CategoryEntity>,
    val budgets: List<BudgetEntity>,
    val financialGoals: List<FinancialGoalEntity> = emptyList(),
    val goalTransactions: List<GoalTransactionEntity> = emptyList(),
    val quickActions: List<QuickActionEntity> = emptyList(),
    val shoppingLists: List<ShoppingListEntity> = emptyList(),
    val shoppingListItems: List<ShoppingListItemEntity> = emptyList(),
    val exportedAt: Long
)

data class BackupFileInfo(
    val name: String,
    val path: String,
    val sizeFormatted: String,
    val sizeBytes: Long,
    val modifiedJalali: String,
    val modifiedMillis: Long,
    val isAutoBackup: Boolean,
    val file: File
)

object BackupManager {

    const val BAK_HEADER_PREFIX = "ARTA_BAK_V1:"
    const val LEGACY_BAK_HEADER_PREFIX = "NAQD_BAK_V1:"
    private const val PREFS_AUTO_BACKUP_SETTINGS = "auto_backup_settings"

    private fun compressGzip(input: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { it.write(input.toByteArray(Charsets.UTF_8)) }
        return bos.toByteArray()
    }

    private fun decompressGzip(compressed: ByteArray): String {
        val bis = ByteArrayInputStream(compressed)
        return GZIPInputStream(bis).bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun exportToBakString(data: BackupData): String {
        val json = exportToJson(data)
        val compressed = compressGzip(json)
        val b64 = Base64.encodeToString(compressed, Base64.NO_WRAP)
        return "$BAK_HEADER_PREFIX$b64"
    }

    fun exportToBakBytes(data: BackupData): ByteArray {
        val bakStr = exportToBakString(data)
        return bakStr.toByteArray(Charsets.UTF_8)
    }

    fun parseFromAny(raw: String): Result<BackupData> {
        return runCatching {
            val trimmed = raw.trim()
            val jsonStr = when {
                trimmed.startsWith(BAK_HEADER_PREFIX) -> {
                    val b64 = trimmed.substring(BAK_HEADER_PREFIX.length).trim()
                    val compressed = Base64.decode(b64, Base64.DEFAULT)
                    decompressGzip(compressed)
                }
                trimmed.startsWith(LEGACY_BAK_HEADER_PREFIX) -> {
                    val b64 = trimmed.substring(LEGACY_BAK_HEADER_PREFIX.length).trim()
                    val compressed = Base64.decode(b64, Base64.DEFAULT)
                    decompressGzip(compressed)
                }
                trimmed.startsWith("{") -> trimmed
                else -> {
                    try {
                        val compressed = Base64.decode(trimmed, Base64.DEFAULT)
                        decompressGzip(compressed)
                    } catch (_: Exception) {
                        trimmed
                    }
                }
            }
            parseFromJson(jsonStr).getOrThrow()
        }
    }

    fun getBackupDirectory(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), "Arta")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun saveBackupFileToDevice(context: Context, data: BackupData, isAuto: Boolean = false): File {
        val dir = getBackupDirectory(context)
        val prefix = if (isAuto) "arta_autobackup" else "arta_backup"
        val timestampStr = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
        val file = File(dir, "${prefix}_$timestampStr.bak")
        val content = exportToBakString(data)
        file.writeText(content, Charsets.UTF_8)

        // Attempt saving a copy to Downloads/Arta for easy file manager visibility
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Arta")
                }
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            } else {
                val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Arta")
                if (publicDir.exists() || publicDir.mkdirs()) {
                    File(publicDir, file.name).writeText(content, Charsets.UTF_8)
                }
            }
        } catch (_: Exception) {}

        // Manage max local auto-backups (keep max 5)
        if (isAuto) {
            pruneOldAutoBackups(context, maxKeep = 5)
        }

        return file
    }

    private fun pruneOldAutoBackups(context: Context, maxKeep: Int) {
        try {
            val dir = getBackupDirectory(context)
            val autoFiles = dir.listFiles { f -> f.name.contains("autobackup", ignoreCase = true) }
            if (autoFiles != null && autoFiles.size > maxKeep) {
                autoFiles.sortedBy { it.lastModified() }
                    .take(autoFiles.size - maxKeep)
                    .forEach { it.delete() }
            }
        } catch (_: Exception) {}
    }

    fun listBackupFiles(context: Context): List<BackupFileInfo> {
        val dir = getBackupDirectory(context)
        val currentFiles = dir.listFiles { f -> f.isFile && (f.extension.equals("bak", ignoreCase = true) || f.extension.equals("json", ignoreCase = true)) }?.toList() ?: emptyList()
        val legacyDir = File(context.getExternalFilesDir(null), "Backups")
        val legacyFiles = if (legacyDir.exists()) {
            legacyDir.listFiles { f -> f.isFile && (f.extension.equals("bak", ignoreCase = true) || f.extension.equals("json", ignoreCase = true)) }?.toList() ?: emptyList()
        } else emptyList()

        val allUniqueFiles = (currentFiles + legacyFiles).distinctBy { it.name }

        return allUniqueFiles.sortedByDescending { it.lastModified() }.map { file ->
            val sizeKb = file.length() / 1024.0
            val sizeStr = if (sizeKb < 1024) String.format(java.util.Locale.US, "%.1f KB", sizeKb) else String.format(java.util.Locale.US, "%.2f MB", sizeKb / 1024.0)
            BackupFileInfo(
                name = file.name,
                path = file.absolutePath,
                sizeFormatted = sizeStr,
                sizeBytes = file.length(),
                modifiedJalali = PersianDateHelper.formatSolarDateTime(file.lastModified()),
                modifiedMillis = file.lastModified(),
                isAutoBackup = file.name.contains("autobackup", ignoreCase = true),
                file = file
            )
        }
    }

    fun deleteBackupFile(file: File): Boolean {
        return try {
            file.delete()
        } catch (_: Exception) {
            false
        }
    }

    fun exportToJson(data: BackupData): String {
        val root = JSONObject().apply {
            put("version", 2)
            put("appName", "Arta")
            put("exportedAt", data.exportedAt)
            put("exportedAtJalali", PersianDateHelper.formatSolarDateTime(data.exportedAt))

            // Transactions
            val txnsArray = JSONArray()
            data.transactions.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("amount", t.amount)
                    put("type", t.type.name)
                    t.categoryId?.let { put("categoryId", it) }
                    put("category", t.category)
                    put("timestamp", t.timestamp)
                    put("accountId", t.accountId)
                    t.recipientName?.let { put("recipientName", it) }
                    t.recipientId?.let { put("recipientId", it) }
                    put("kind", t.kind.name)
                    t.note?.let { put("note", it) }
                    t.calculationExpression?.let { put("calculationExpression", it) }
                    put("currencyId", t.currencyId)
                    put("currencyCode", t.currencyCode)
                    put("currencySymbol", t.currencySymbol)
                    put("exchangeRate", t.exchangeRate)
                    t.ledgerCurrencyId?.let { put("ledgerCurrencyId", it) }
                    t.ledgerCurrencyCode?.let { put("ledgerCurrencyCode", it) }
                    t.ledgerCurrencySymbol?.let { put("ledgerCurrencySymbol", it) }
                    t.ledgerAmount?.let { put("ledgerAmount", it) }
                    t.relatedTransactionId?.let { put("relatedTransactionId", it) }
                    t.sourceCurrencyId?.let { put("sourceCurrencyId", it) }
                    t.sourceCurrencyCode?.let { put("sourceCurrencyCode", it) }
                    t.sourceCurrencySymbol?.let { put("sourceCurrencySymbol", it) }
                    t.sourceAmount?.let { put("sourceAmount", it) }
                    t.targetCurrencyId?.let { put("targetCurrencyId", it) }
                    t.targetCurrencyCode?.let { put("targetCurrencyCode", it) }
                    t.targetCurrencySymbol?.let { put("targetCurrencySymbol", it) }
                    t.targetAmount?.let { put("targetAmount", it) }
                    t.conversionRate?.let { put("conversionRate", it) }
                    t.historicalBaseCurrencyCode?.let { put("historicalBaseCurrencyCode", it) }
                    t.historicalExchangeRateToBase?.let { put("historicalExchangeRateToBase", it) }
                    t.dueDate?.let { put("dueDate", it) }
                    put("isSettled", t.isSettled)
                    t.settledAmount?.let { put("settledAmount", it) }
                    put("affectsBalance", t.affectsBalance)
                }
                txnsArray.put(obj)
            }
            put("transactions", txnsArray)

            // Accounts
            val acctsArray = JSONArray()
            data.accounts.forEach { a ->
                val obj = JSONObject().apply {
                    put("id", a.id)
                    put("name", a.name)
                    put("cardNumberMasked", a.cardNumberMasked)
                    put("cardHolder", a.cardHolder)
                    put("expiry", a.expiry)
                    put("balance", a.balance)
                    put("cardColorTheme", a.cardColorTheme)
                    put("isFrozen", a.isFrozen)
                    put("isDefault", a.isDefault)
                    put("currencyId", a.currencyId)
                    put("currencyCode", a.currencyCode)
                    put("currencySymbol", a.currencySymbol)
                }
                acctsArray.put(obj)
            }
            put("accounts", acctsArray)

            // Recipients
            val recArray = JSONArray()
            data.recipients.forEach { r ->
                val obj = JSONObject().apply {
                    put("id", r.id)
                    put("name", r.name)
                    put("handleOrPhone", r.handleOrPhone)
                    put("avatarColorHex", r.avatarColorHex)
                    put("iconName", r.iconName)
                    put("transactionCount", r.transactionCount)
                    put("isFavorite", r.isFavorite)
                    put("notes", r.notes)
                    put("isActive", r.isActive)
                    put("isAmountProtected", r.isAmountProtected)
                }
                recArray.put(obj)
            }
            put("recipients", recArray)

            // Currencies
            val currArray = JSONArray()
            data.currencies.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("code", c.code)
                    put("name", c.name)
                    put("symbol", c.symbol)
                    put("flagEmoji", c.flagEmoji)
                    put("exchangeRateToBase", c.exchangeRateToBase)
                    put("isBaseCurrency", c.isBaseCurrency)
                    put("isActive", c.isActive)
                    put("colorHex", c.colorHex)
                    put("decimalPlaces", c.decimalPlaces)
                }
                currArray.put(obj)
            }
            put("currencies", currArray)

            // Categories
            val catArray = JSONArray()
            data.categories.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("iconName", c.iconName)
                    put("colorHex", c.colorHex)
                    put("type", c.type.name)
                    put("isDefault", c.isDefault)
                    put("isActive", c.isActive)
                }
                catArray.put(obj)
            }
            put("categories", catArray)

            // Budgets
            val budArray = JSONArray()
            data.budgets.forEach { b ->
                val obj = JSONObject().apply {
                    put("id", b.id)
                    b.categoryId?.let { put("categoryId", it) }
                    put("category", b.category)
                    put("monthlyLimit", b.monthlyLimit)
                    put("currencyId", b.currencyId)
                    put("currencyCode", b.currencyCode)
                    put("monthYear", b.monthYear)
                }
                budArray.put(obj)
            }
            put("budgets", budArray)

            // Financial Goals
            val goalArray = JSONArray()
            data.financialGoals.forEach { g ->
                val obj = JSONObject().apply {
                    put("id", g.id)
                    put("title", g.title)
                    put("description", g.description)
                    put("targetAmount", g.targetAmount)
                    put("currentAmount", g.currentAmount)
                    put("currencyId", g.currencyId)
                    put("currencyCode", g.currencyCode)
                    put("currencySymbol", g.currencySymbol)
                    put("iconName", g.iconName)
                    put("colorHex", g.colorHex)
                    put("category", g.category)
                    put("startDate", g.startDate)
                    g.targetDate?.let { put("targetDate", it) }
                    g.linkedAccountId?.let { put("linkedAccountId", it) }
                    put("status", g.status.name)
                    g.completedDate?.let { put("completedDate", it) }
                    put("reminderFrequency", g.reminderFrequency.name)
                    g.reminderDate?.let { put("reminderDate", it) }
                    put("createdAt", g.createdAt)
                }
                goalArray.put(obj)
            }
            put("financialGoals", goalArray)

            // Goal Transactions
            val goalTxnArray = JSONArray()
            data.goalTransactions.forEach { gt ->
                val obj = JSONObject().apply {
                    put("id", gt.id)
                    put("goalId", gt.goalId)
                    put("amount", gt.amount)
                    put("currencyId", gt.currencyId)
                    put("currencyCode", gt.currencyCode)
                    put("currencySymbol", gt.currencySymbol)
                    put("type", gt.type.name)
                    gt.accountId?.let { put("accountId", it) }
                    gt.accountName?.let { put("accountName", it) }
                    put("note", gt.note)
                    put("timestamp", gt.timestamp)
                    gt.linkedTransactionId?.let { put("linkedTransactionId", it) }
                }
                goalTxnArray.put(obj)
            }
            put("goalTransactions", goalTxnArray)

            // Quick Actions
            val qaArray = JSONArray()
            data.quickActions.forEach { q ->
                val obj = JSONObject().apply {
                    put("id", q.id)
                    put("title", q.title)
                    put("iconName", q.iconName)
                    put("actionType", q.actionType.name)
                    put("amountBehavior", q.amountBehavior.name)
                    put("defaultAmount", q.defaultAmount)
                    put("currencyId", q.currencyId)
                    put("currencyCode", q.currencyCode)
                    put("currencySymbol", q.currencySymbol)
                    put("sourceAccountId", q.sourceAccountId)
                    q.destinationAccountId?.let { put("destinationAccountId", it) }
                    q.categoryId?.let { put("categoryId", it) }
                    put("categoryName", q.categoryName)
                    q.recipientId?.let { put("recipientId", it) }
                    q.recipientName?.let { put("recipientName", it) }
                    q.targetGoalId?.let { put("targetGoalId", it) }
                    q.targetGoalTitle?.let { put("targetGoalTitle", it) }
                    q.note?.let { put("note", it) }
                    put("showOnHome", q.showOnHome)
                    put("displayOrder", q.displayOrder)
                    put("colorHex", q.colorHex)
                    put("usageCount", q.usageCount)
                    put("createdAt", q.createdAt)
                }
                qaArray.put(obj)
            }
            put("quickActions", qaArray)

            // Shopping Lists
            val slArray = JSONArray()
            data.shoppingLists.forEach { s ->
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("title", s.title)
                    put("currencyId", s.currencyId)
                    put("currencyCode", s.currencyCode)
                    put("currencySymbol", s.currencySymbol)
                    put("isCompleted", s.isCompleted)
                    put("isLoggedAsExpense", s.isLoggedAsExpense)
                    s.linkedTransactionId?.let { put("linkedTransactionId", it) }
                    s.accountId?.let { put("accountId", it) }
                    s.accountName?.let { put("accountName", it) }
                    s.categoryId?.let { put("categoryId", it) }
                    put("category", s.category)
                    put("totalAmount", s.totalAmount)
                    put("useItemizedSum", s.useItemizedSum)
                    s.manualTotalAmount?.let { put("manualTotalAmount", it) }
                    put("createdAt", s.createdAt)
                    put("purchaseDate", s.purchaseDate)
                    s.note?.let { put("note", it) }
                }
                slArray.put(obj)
            }
            put("shoppingLists", slArray)

            // Shopping List Items
            val sliArray = JSONArray()
            data.shoppingListItems.forEach { si ->
                val obj = JSONObject().apply {
                    put("id", si.id)
                    put("listId", si.listId)
                    put("title", si.title)
                    put("quantity", si.quantity)
                    put("price", si.price)
                    put("unitPrice", si.unitPrice)
                    put("isPurchased", si.isPurchased)
                    put("displayOrder", si.displayOrder)
                }
                sliArray.put(obj)
            }
            put("shoppingListItems", sliArray)
        }
        return root.toString(2)
    }

    fun parseFromJson(jsonString: String): Result<BackupData> {
        return runCatching {
            val root = JSONObject(jsonString)

            // Transactions
            val txns = mutableListOf<TransactionEntity>()
            if (root.has("transactions")) {
                val array = root.getJSONArray("transactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    txns.add(
                        TransactionEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.optString("title", "معامله"),
                            amount = obj.optDouble("amount", 0.0),
                            type = try {
                                TransactionType.valueOf(obj.optString("type", TransactionType.EXPENSE.name))
                            } catch (_: Exception) {
                                TransactionType.EXPENSE
                            },
                            categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.optLong("categoryId") else null,
                            category = obj.optString("category", "عمومی"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            accountId = obj.optLong("accountId", 0L),
                            recipientName = if (obj.has("recipientName") && !obj.isNull("recipientName")) obj.optString("recipientName") else null,
                            recipientId = if (obj.has("recipientId") && !obj.isNull("recipientId")) obj.optLong("recipientId") else null,
                            kind = try {
                                if (obj.has("kind") && !obj.isNull("kind")) TransactionKind.valueOf(obj.optString("kind")) else TransactionKind.NORMAL
                            } catch (_: Exception) {
                                TransactionKind.NORMAL
                            },
                            note = if (obj.has("note") && !obj.isNull("note")) obj.optString("note") else null,
                            calculationExpression = if (obj.has("calculationExpression") && !obj.isNull("calculationExpression")) obj.optString("calculationExpression") else null,
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN"),
                            currencySymbol = obj.optString("currencySymbol", "؋"),
                            exchangeRate = obj.optDouble("exchangeRate", 1.0),
                            ledgerCurrencyId = if (obj.has("ledgerCurrencyId") && !obj.isNull("ledgerCurrencyId")) obj.optLong("ledgerCurrencyId") else null,
                            ledgerCurrencyCode = if (obj.has("ledgerCurrencyCode") && !obj.isNull("ledgerCurrencyCode")) obj.optString("ledgerCurrencyCode") else null,
                            ledgerCurrencySymbol = if (obj.has("ledgerCurrencySymbol") && !obj.isNull("ledgerCurrencySymbol")) obj.optString("ledgerCurrencySymbol") else null,
                            ledgerAmount = if (obj.has("ledgerAmount") && !obj.isNull("ledgerAmount")) obj.optDouble("ledgerAmount") else null,
                            relatedTransactionId = if (obj.has("relatedTransactionId") && !obj.isNull("relatedTransactionId")) obj.optLong("relatedTransactionId") else null,
                            sourceCurrencyId = if (obj.has("sourceCurrencyId") && !obj.isNull("sourceCurrencyId")) obj.optLong("sourceCurrencyId") else null,
                            sourceCurrencyCode = if (obj.has("sourceCurrencyCode") && !obj.isNull("sourceCurrencyCode")) obj.optString("sourceCurrencyCode") else null,
                            sourceCurrencySymbol = if (obj.has("sourceCurrencySymbol") && !obj.isNull("sourceCurrencySymbol")) obj.optString("sourceCurrencySymbol") else null,
                            sourceAmount = if (obj.has("sourceAmount") && !obj.isNull("sourceAmount")) obj.optDouble("sourceAmount") else null,
                            targetCurrencyId = if (obj.has("targetCurrencyId") && !obj.isNull("targetCurrencyId")) obj.optLong("targetCurrencyId") else null,
                            targetCurrencyCode = if (obj.has("targetCurrencyCode") && !obj.isNull("targetCurrencyCode")) obj.optString("targetCurrencyCode") else null,
                            targetCurrencySymbol = if (obj.has("targetCurrencySymbol") && !obj.isNull("targetCurrencySymbol")) obj.optString("targetCurrencySymbol") else null,
                            targetAmount = if (obj.has("targetAmount") && !obj.isNull("targetAmount")) obj.optDouble("targetAmount") else null,
                            conversionRate = if (obj.has("conversionRate") && !obj.isNull("conversionRate")) obj.optDouble("conversionRate") else null,
                            historicalBaseCurrencyCode = obj.optString("historicalBaseCurrencyCode", "AFN"),
                            historicalExchangeRateToBase = obj.optDouble("historicalExchangeRateToBase", 1.0),
                            dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.optLong("dueDate") else null,
                            isSettled = obj.optBoolean("isSettled", false),
                            settledAmount = obj.optDouble("settledAmount", 0.0),
                            affectsBalance = obj.optBoolean("affectsBalance", true)
                        )
                    )
                }
            }

            // Accounts
            val accts = mutableListOf<AccountCardEntity>()
            if (root.has("accounts")) {
                val array = root.getJSONArray("accounts")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    accts.add(
                        AccountCardEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", "حساب اصلی"),
                            cardNumberMasked = obj.optString("cardNumberMasked", "•••• 0000"),
                            cardHolder = obj.optString("cardHolder", "کاربر"),
                            expiry = obj.optString("expiry", "12/28"),
                            balance = obj.optDouble("balance", 0.0),
                            cardColorTheme = obj.optString("cardColorTheme", "dark"),
                            isFrozen = obj.optBoolean("isFrozen", false),
                            isDefault = obj.optBoolean("isDefault", false),
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN"),
                            currencySymbol = obj.optString("currencySymbol", "؋")
                        )
                    )
                }
            }

            // Recipients
            val recs = mutableListOf<RecipientEntity>()
            if (root.has("recipients")) {
                val array = root.getJSONArray("recipients")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    recs.add(
                        RecipientEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", "مخاطب"),
                            handleOrPhone = obj.optString("handleOrPhone", ""),
                            avatarColorHex = obj.optLong("avatarColorHex", 0xFF0F766E),
                            iconName = obj.optString("iconName", "person"),
                            transactionCount = obj.optInt("transactionCount", 0),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            notes = obj.optString("notes", ""),
                            isActive = obj.optBoolean("isActive", true),
                            isAmountProtected = obj.optBoolean("isAmountProtected", false)
                        )
                    )
                }
            }

            // Currencies
            val currs = mutableListOf<CurrencyEntity>()
            if (root.has("currencies")) {
                val array = root.getJSONArray("currencies")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    currs.add(
                        CurrencyEntity(
                            id = obj.optLong("id", 0L),
                            code = obj.optString("code", "AFN"),
                            name = obj.optString("name", "افغانی"),
                            symbol = obj.optString("symbol", "؋"),
                            flagEmoji = obj.optString("flagEmoji", "🇦🇫"),
                            exchangeRateToBase = obj.optDouble("exchangeRateToBase", 1.0),
                            isBaseCurrency = obj.optBoolean("isBaseCurrency", false),
                            isActive = obj.optBoolean("isActive", true),
                            colorHex = obj.optLong("colorHex", 0xFF0F766E),
                            decimalPlaces = obj.optInt("decimalPlaces", 0)
                        )
                    )
                }
            }

            // Categories
            val cats = mutableListOf<CategoryEntity>()
            if (root.has("categories")) {
                val array = root.getJSONArray("categories")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    cats.add(
                        CategoryEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.optString("name", "دسته بندی"),
                            iconName = obj.optString("iconName", "category"),
                            colorHex = obj.optLong("colorHex", 0xFF0F766E),
                            type = try {
                                TransactionType.valueOf(obj.optString("type", TransactionType.EXPENSE.name))
                            } catch (_: Exception) {
                                TransactionType.EXPENSE
                            },
                            isDefault = obj.optBoolean("isDefault", false),
                            isActive = obj.optBoolean("isActive", true)
                        )
                    )
                }
            }

            // Budgets
            val buds = mutableListOf<BudgetEntity>()
            if (root.has("budgets")) {
                val array = root.getJSONArray("budgets")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    buds.add(
                        BudgetEntity(
                            id = obj.optLong("id", 0L),
                            categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.optLong("categoryId") else null,
                            category = obj.optString("category", ""),
                            monthlyLimit = obj.optDouble("monthlyLimit", 0.0),
                            monthYear = obj.optString("monthYear", "2026-09"),
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN")
                        )
                    )
                }
            }

            // Financial Goals
            val goals = mutableListOf<FinancialGoalEntity>()
            if (root.has("financialGoals")) {
                val array = root.getJSONArray("financialGoals")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    goals.add(
                        FinancialGoalEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.optString("title", "هدف مالی"),
                            description = obj.optString("description", ""),
                            targetAmount = obj.optDouble("targetAmount", 0.0),
                            currentAmount = obj.optDouble("currentAmount", 0.0),
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN"),
                            currencySymbol = obj.optString("currencySymbol", "؋"),
                            iconName = obj.optString("iconName", "Savings"),
                            colorHex = obj.optLong("colorHex", 0xFF10B981),
                            category = obj.optString("category", "پس‌انداز عمومی"),
                            startDate = obj.optLong("startDate", System.currentTimeMillis()),
                            targetDate = if (obj.has("targetDate") && !obj.isNull("targetDate")) obj.optLong("targetDate") else null,
                            linkedAccountId = if (obj.has("linkedAccountId") && !obj.isNull("linkedAccountId")) obj.optLong("linkedAccountId") else null,
                            status = try {
                                GoalStatus.valueOf(obj.optString("status", GoalStatus.ACTIVE.name))
                            } catch (_: Exception) {
                                GoalStatus.ACTIVE
                            },
                            completedDate = if (obj.has("completedDate") && !obj.isNull("completedDate")) obj.optLong("completedDate") else null,
                            reminderFrequency = try {
                                GoalReminderFrequency.valueOf(obj.optString("reminderFrequency", GoalReminderFrequency.NONE.name))
                            } catch (_: Exception) {
                                GoalReminderFrequency.NONE
                            },
                            reminderDate = if (obj.has("reminderDate") && !obj.isNull("reminderDate")) obj.optLong("reminderDate") else null,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Goal Transactions
            val goalTxns = mutableListOf<GoalTransactionEntity>()
            if (root.has("goalTransactions")) {
                val array = root.getJSONArray("goalTransactions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    goalTxns.add(
                        GoalTransactionEntity(
                            id = obj.optLong("id", 0L),
                            goalId = obj.optLong("goalId", 0L),
                            amount = obj.optDouble("amount", 0.0),
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN"),
                            currencySymbol = obj.optString("currencySymbol", "؋"),
                            type = try {
                                GoalTransactionType.valueOf(obj.optString("type", GoalTransactionType.DEPOSIT.name))
                            } catch (_: Exception) {
                                GoalTransactionType.DEPOSIT
                            },
                            accountId = if (obj.has("accountId") && !obj.isNull("accountId")) obj.optLong("accountId") else null,
                            accountName = if (obj.has("accountName") && !obj.isNull("accountName")) obj.optString("accountName") else null,
                            note = obj.optString("note", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            linkedTransactionId = if (obj.has("linkedTransactionId") && !obj.isNull("linkedTransactionId")) obj.optLong("linkedTransactionId") else null
                        )
                    )
                }
            }

            // Quick Actions
            val quickActions = mutableListOf<QuickActionEntity>()
            if (root.has("quickActions")) {
                val array = root.getJSONArray("quickActions")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    quickActions.add(
                        QuickActionEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.optString("title", "عملیات سریع"),
                            iconName = obj.optString("iconName", "ShoppingBag"),
                            actionType = try {
                                QuickActionType.valueOf(obj.optString("actionType", QuickActionType.EXPENSE.name))
                            } catch (_: Exception) {
                                QuickActionType.EXPENSE
                            },
                            amountBehavior = try {
                                QuickActionAmountBehavior.valueOf(obj.optString("amountBehavior", QuickActionAmountBehavior.FIXED.name))
                            } catch (_: Exception) {
                                QuickActionAmountBehavior.FIXED
                            },
                            defaultAmount = obj.optDouble("defaultAmount", 0.0),
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN"),
                            currencySymbol = obj.optString("currencySymbol", "؋"),
                            sourceAccountId = obj.optLong("sourceAccountId", 0L),
                            destinationAccountId = if (obj.has("destinationAccountId") && !obj.isNull("destinationAccountId")) obj.optLong("destinationAccountId") else null,
                            categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.optLong("categoryId") else null,
                            categoryName = obj.optString("categoryName", "عمومی"),
                            recipientId = if (obj.has("recipientId") && !obj.isNull("recipientId")) obj.optLong("recipientId") else null,
                            recipientName = if (obj.has("recipientName") && !obj.isNull("recipientName")) obj.optString("recipientName") else null,
                            targetGoalId = if (obj.has("targetGoalId") && !obj.isNull("targetGoalId")) obj.optLong("targetGoalId") else null,
                            targetGoalTitle = if (obj.has("targetGoalTitle") && !obj.isNull("targetGoalTitle")) obj.optString("targetGoalTitle") else null,
                            note = if (obj.has("note") && !obj.isNull("note")) obj.optString("note") else null,
                            showOnHome = obj.optBoolean("showOnHome", true),
                            displayOrder = obj.optInt("displayOrder", 0),
                            colorHex = obj.optLong("colorHex", 0xFF3B82F6),
                            usageCount = obj.optInt("usageCount", 0),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Shopping Lists
            val shoppingLists = mutableListOf<ShoppingListEntity>()
            if (root.has("shoppingLists")) {
                val array = root.getJSONArray("shoppingLists")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    shoppingLists.add(
                        ShoppingListEntity(
                            id = obj.optLong("id", 0L),
                            title = obj.optString("title", "لیست خرید"),
                            currencyId = obj.optLong("currencyId", 0L),
                            currencyCode = obj.optString("currencyCode", "AFN"),
                            currencySymbol = obj.optString("currencySymbol", "؋"),
                            isCompleted = obj.optBoolean("isCompleted", false),
                            isLoggedAsExpense = obj.optBoolean("isLoggedAsExpense", false),
                            linkedTransactionId = if (obj.has("linkedTransactionId") && !obj.isNull("linkedTransactionId")) obj.optLong("linkedTransactionId") else null,
                            accountId = if (obj.has("accountId") && !obj.isNull("accountId")) obj.optLong("accountId") else null,
                            accountName = if (obj.has("accountName") && !obj.isNull("accountName")) obj.optString("accountName") else null,
                            categoryId = if (obj.has("categoryId") && !obj.isNull("categoryId")) obj.optLong("categoryId") else null,
                            category = obj.optString("category", "خرید روزمره"),
                            totalAmount = obj.optDouble("totalAmount", 0.0),
                            useItemizedSum = obj.optBoolean("useItemizedSum", true),
                            manualTotalAmount = if (obj.has("manualTotalAmount") && !obj.isNull("manualTotalAmount")) obj.optDouble("manualTotalAmount") else null,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            purchaseDate = obj.optLong("purchaseDate", System.currentTimeMillis()),
                            note = if (obj.has("note") && !obj.isNull("note")) obj.optString("note") else null
                        )
                    )
                }
            }

            // Shopping List Items
            val shoppingListItems = mutableListOf<ShoppingListItemEntity>()
            if (root.has("shoppingListItems")) {
                val array = root.getJSONArray("shoppingListItems")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    shoppingListItems.add(
                        ShoppingListItemEntity(
                            id = obj.optLong("id", 0L),
                            listId = obj.optLong("listId", 0L),
                            title = obj.optString("title", "کالا"),
                            quantity = obj.optString("quantity", ""),
                            price = obj.optDouble("price", 0.0),
                            unitPrice = obj.optDouble("unitPrice", 0.0),
                            isPurchased = obj.optBoolean("isPurchased", false),
                            displayOrder = obj.optInt("displayOrder", 0)
                        )
                    )
                }
            }

            BackupData(
                transactions = txns,
                accounts = accts,
                recipients = recs,
                currencies = currs,
                categories = cats,
                budgets = buds,
                financialGoals = goals,
                goalTransactions = goalTxns,
                quickActions = quickActions,
                shoppingLists = shoppingLists,
                shoppingListItems = shoppingListItems,
                exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
            )
        }
    }
}
