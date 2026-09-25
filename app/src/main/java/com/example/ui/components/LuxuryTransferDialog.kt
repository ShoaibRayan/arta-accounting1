package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.RecipientEntity
import com.example.data.local.TransactionEntity
import com.example.ui.screens.parseLocalizedNumber
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale
import com.example.ui.components.CalculatorMiniButton
import com.example.ui.components.MinimalCalculatorDialog
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class TransferTab {
    CASH_AND_CARD,
    WALLET_TO_WALLET,
    PERSON_TO_PERSON
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LuxuryTransferDialog(
    viewModel: FinanceViewModel,
    accounts: List<AccountCardEntity>,
    recipients: List<RecipientEntity>,
    activeCurrencies: List<CurrencyEntity>,
    initialTab: TransferTab = TransferTab.CASH_AND_CARD,
    lockedTab: TransferTab? = null,
    preselectedPersonName: String? = null,
    preselectedPersonId: Long? = null,
    editingTransferFromTxn: TransactionEntity? = null,
    editingTransferToTxn: TransactionEntity? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val currencyBalances by viewModel.currencyBalances.collectAsStateWithLifecycle()
    val isEditingTransfer = editingTransferFromTxn != null && editingTransferToTxn != null
    val detectedTab = remember(editingTransferFromTxn, editingTransferToTxn, lockedTab) {
        if (lockedTab != null) lockedTab
        else when {
            editingTransferFromTxn == null -> initialTab
            editingTransferFromTxn.category == "انتقال حساب اشخاص" || !editingTransferFromTxn.recipientName.isNullOrBlank() ||
                    (editingTransferToTxn != null && (!editingTransferToTxn.recipientName.isNullOrBlank() || editingTransferToTxn.category == "انتقال حساب اشخاص")) ->
                TransferTab.PERSON_TO_PERSON

            (editingTransferFromTxn.accountId == 0L && (editingTransferToTxn?.accountId ?: 0L) > 0L) ||
                    (editingTransferFromTxn.accountId > 0L && (editingTransferToTxn?.accountId ?: 0L) == 0L) ||
                    editingTransferFromTxn.title.contains("نقد") || (editingTransferToTxn?.title?.contains("نقد") == true) ||
                    editingTransferFromTxn.title.contains("بیلانس") || (editingTransferToTxn?.title?.contains("بیلانس") == true) ||
                    editingTransferFromTxn.title.contains("کارت به نقد") || editingTransferFromTxn.title.contains("نقد به کارت") ->
                TransferTab.CASH_AND_CARD

            else -> TransferTab.WALLET_TO_WALLET
        }
    }
    var selectedTab by remember { mutableStateOf(detectedTab) }

    // Eligible (unfrozen) accounts for transfers
    val eligibleAccounts = remember(accounts) { accounts.filter { !it.isFrozen } }

    // --- Unified Cash and Card Transfer state ---
    // Source can be Cash or Card. Destination can be Cash or Card.
    // Rule: Both can be Card (Card to Card). Both CANNOT be Cash simultaneously.
    val initialIsSourceCash = remember(editingTransferFromTxn, editingTransferToTxn, initialTab) {
        if (editingTransferFromTxn != null) {
            editingTransferFromTxn.accountId == 0L
        } else {
            initialTab != TransferTab.WALLET_TO_WALLET
        }
    }
    val initialIsDestCash = remember(editingTransferFromTxn, editingTransferToTxn, initialTab) {
        if (editingTransferToTxn != null) {
            editingTransferToTxn.accountId == 0L
        } else {
            false
        }
    }
    var isSourceCash by remember(initialIsSourceCash) {
        mutableStateOf(if (initialIsSourceCash && initialIsDestCash) true else initialIsSourceCash)
    }
    var isDestCash by remember(initialIsDestCash) {
        mutableStateOf(if (initialIsSourceCash && initialIsDestCash) false else initialIsDestCash)
    }

    var sourceCard by remember(editingTransferFromTxn, eligibleAccounts) {
        val cId = editingTransferFromTxn?.accountId ?: 0L
        mutableStateOf(eligibleAccounts.find { it.id == cId } ?: eligibleAccounts.firstOrNull { it.isDefault } ?: eligibleAccounts.firstOrNull())
    }
    var destCard by remember(editingTransferToTxn, eligibleAccounts, sourceCard) {
        val cId = editingTransferToTxn?.accountId ?: 0L
        val matched = eligibleAccounts.find { it.id == cId }
        val fallback = eligibleAccounts.firstOrNull { it.id != sourceCard?.id } ?: eligibleAccounts.getOrNull(1) ?: eligibleAccounts.firstOrNull()
        mutableStateOf(matched ?: fallback)
    }

    var sourceCashCurrency by remember(editingTransferFromTxn, activeCurrencies) {
        val currId = editingTransferFromTxn?.currencyId ?: 0L
        val cCode = editingTransferFromTxn?.currencyCode
        val matched = if (currId > 0L) activeCurrencies.find { it.id == currId } else null
        mutableStateOf(matched ?: activeCurrencies.find { it.code.equals(cCode, ignoreCase = true) } ?: activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull())
    }
    var destCashCurrency by remember(editingTransferToTxn, activeCurrencies, sourceCashCurrency) {
        val currId = editingTransferToTxn?.currencyId ?: 0L
        val cCode = editingTransferToTxn?.currencyCode
        val matched = if (currId > 0L) activeCurrencies.find { it.id == currId } else null
        mutableStateOf(matched ?: activeCurrencies.find { it.code.equals(cCode, ignoreCase = true) } ?: sourceCashCurrency)
    }

    var cashCardFromAmountText by remember(editingTransferFromTxn) {
        mutableStateOf(
            if (editingTransferFromTxn != null) {
                if (editingTransferFromTxn.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", editingTransferFromTxn.amount)
                else String.format(Locale.US, "%.3f", editingTransferFromTxn.amount).trimEnd('0').trimEnd('.')
            } else ""
        )
    }

    var cashCardToAmountText by remember(editingTransferToTxn) {
        mutableStateOf(
            if (editingTransferToTxn != null) {
                if (editingTransferToTxn.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", editingTransferToTxn.amount)
                else String.format(Locale.US, "%.3f", editingTransferToTxn.amount).trimEnd('0').trimEnd('.')
            } else ""
        )
    }

    // Dropdown expanded states
    var isSourceCashDropdownOpen by remember { mutableStateOf(false) }
    var isSourceCardDropdownOpen by remember { mutableStateOf(false) }
    var isDestCashDropdownOpen by remember { mutableStateOf(false) }
    var isDestCardDropdownOpen by remember { mutableStateOf(false) }

    // Dynamic balance calculations
    val sourceCashBal = remember(currencyBalances, sourceCashCurrency) {
        val sCurr = sourceCashCurrency
        currencyBalances.find { (sCurr?.id != null && sCurr.id > 0 && it.currency.id == sCurr.id) || it.currency.code.equals(sCurr?.code, ignoreCase = true) }?.balance ?: 0.0
    }
    val sourceCardBal = remember(sourceCard) {
        sourceCard?.balance ?: 0.0
    }
    val rawSourceAvailableBal = if (isSourceCash) sourceCashBal else sourceCardBal
    val effSourceAvailableBal = remember(isEditingTransfer, editingTransferFromTxn, isSourceCash, initialIsSourceCash, sourceCashCurrency, sourceCard, rawSourceAvailableBal) {
        if (isEditingTransfer && editingTransferFromTxn != null && isSourceCash == initialIsSourceCash) {
            if (isSourceCash) {
                val sCurr = sourceCashCurrency
                val isMatch = (sCurr?.id != null && sCurr.id > 0 && editingTransferFromTxn.currencyId > 0 && sCurr.id == editingTransferFromTxn.currencyId) ||
                    sCurr?.code.equals(editingTransferFromTxn.currencyCode, ignoreCase = true)
                if (isMatch) {
                    rawSourceAvailableBal + editingTransferFromTxn.amount
                } else rawSourceAvailableBal
            } else {
                if (sourceCard?.id == editingTransferFromTxn.accountId) {
                    rawSourceAvailableBal + editingTransferFromTxn.amount
                } else rawSourceAvailableBal
            }
        } else {
            rawSourceAvailableBal
        }
    }

    val destCashBal = remember(currencyBalances, destCashCurrency) {
        val dCurr = destCashCurrency
        currencyBalances.find { (dCurr?.id != null && dCurr.id > 0 && it.currency.id == dCurr.id) || it.currency.code.equals(dCurr?.code, ignoreCase = true) }?.balance ?: 0.0
    }
    val destCardBal = remember(destCard) {
        destCard?.balance ?: 0.0
    }
    val destCurrentBal = if (isDestCash) destCashBal else destCardBal

    val sourceCurrCode = if (isSourceCash) (sourceCashCurrency?.code ?: "") else (sourceCard?.currencyCode ?: "")
    val sourceCurrSymbol = if (isSourceCash) (sourceCashCurrency?.symbol ?: "") else (sourceCard?.currencySymbol ?: "")
    val destCurrCode = if (isDestCash) (destCashCurrency?.code ?: "") else (destCard?.currencyCode ?: "")
    val destCurrSymbol = if (isDestCash) (destCashCurrency?.symbol ?: "") else (destCard?.currencySymbol ?: "")

    fun computeCashCardAutoRate(srcCode: String, dstCode: String): String {
        if (srcCode.isBlank() || dstCode.isBlank() || srcCode.equals(dstCode, ignoreCase = true)) return "1.0"
        val sRate = activeCurrencies.find { it.code.equals(srcCode, ignoreCase = true) }?.exchangeRateToBase ?: 1.0
        val dRate = activeCurrencies.find { it.code.equals(dstCode, ignoreCase = true) }?.exchangeRateToBase ?: 1.0
        val propRate = if (dRate > 0) sRate / dRate else 1.0
        return if (propRate == 1.0) "1.0" else String.format(Locale.US, "%.4f", propRate).trimEnd('0').trimEnd('.')
    }

    val initialCashCardRate = remember(editingTransferFromTxn, editingTransferToTxn, sourceCurrCode, destCurrCode) {
        if (editingTransferFromTxn != null && editingTransferToTxn != null && editingTransferFromTxn.amount > 0) {
            String.format(Locale.US, "%.4f", editingTransferToTxn.amount / editingTransferFromTxn.amount).trimEnd('0').trimEnd('.')
        } else {
            computeCashCardAutoRate(sourceCurrCode, destCurrCode)
        }
    }
    var cashCardRateText by remember { mutableStateOf(initialCashCardRate) }
    var cashCardNote by remember(editingTransferFromTxn) { mutableStateOf(editingTransferFromTxn?.note ?: "") }

    fun updateCashCardRate(srcCode: String, dstCode: String) {
        val newRateStr = computeCashCardAutoRate(srcCode, dstCode)
        cashCardRateText = newRateStr
        val r = parseLocalizedNumber(newRateStr) ?: 1.0
        val fromVal = parseLocalizedNumber(cashCardFromAmountText) ?: 0.0
        if (fromVal > 0) {
            val toVal = fromVal * r
            cashCardToAmountText = if (toVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", toVal) else String.format(Locale.US, "%.3f", toVal).trimEnd('0').trimEnd('.')
        }
    }

    fun onCashCardFromChanged(newVal: String) {
        cashCardFromAmountText = newVal
        val fromVal = parseLocalizedNumber(newVal) ?: 0.0
        val r = parseLocalizedNumber(cashCardRateText) ?: 1.0
        if (fromVal > 0 && r > 0) {
            val toVal = fromVal * r
            cashCardToAmountText = if (toVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", toVal) else String.format(Locale.US, "%.3f", toVal).trimEnd('0').trimEnd('.')
        } else if (newVal.isBlank()) {
            cashCardToAmountText = ""
        }
    }

    fun onCashCardRateChanged(newRateStr: String) {
        cashCardRateText = newRateStr
        val r = parseLocalizedNumber(newRateStr) ?: 0.0
        val fromVal = parseLocalizedNumber(cashCardFromAmountText) ?: 0.0
        if (fromVal > 0 && r > 0) {
            val toVal = fromVal * r
            cashCardToAmountText = if (toVal % 1.0 == 0.0) String.format(Locale.US, "%.0f", toVal) else String.format(Locale.US, "%.3f", toVal).trimEnd('0').trimEnd('.')
        }
    }

    fun onCashCardToChanged(newVal: String) {
        cashCardToAmountText = newVal
        val toVal = parseLocalizedNumber(newVal) ?: 0.0
        val fromVal = parseLocalizedNumber(cashCardFromAmountText) ?: 0.0
        if (fromVal > 0 && toVal > 0) {
            val newRate = toVal / fromVal
            cashCardRateText = String.format(Locale.US, "%.4f", newRate).trimEnd('0').trimEnd('.')
        }
    }

    val parsedCashCardFromAmt = parseLocalizedNumber(cashCardFromAmountText) ?: 0.0
    val parsedCashCardToAmt = parseLocalizedNumber(cashCardToAmountText) ?: 0.0
    val parsedCashCardRate = parseLocalizedNumber(cashCardRateText) ?: 1.0
    val isCashCardDiff = !sourceCurrCode.equals(destCurrCode, ignoreCase = true)

    val isCashCardAmountValid = parsedCashCardFromAmt > 0.0 && parsedCashCardToAmt > 0.0
    val isCashCardBalSufficient = effSourceAvailableBal >= parsedCashCardFromAmt && effSourceAvailableBal > 0.0
    val isCashCardDistinct = if (!isSourceCash && !isDestCash) (sourceCard != null && destCard != null && sourceCard!!.id != destCard!!.id) else true
    val isCashCardAccountValid = !(isSourceCash && isDestCash) && isCashCardDistinct &&
            ((isSourceCash && sourceCashCurrency != null) || (!isSourceCash && sourceCard != null)) &&
            ((isDestCash && destCashCurrency != null) || (!isDestCash && destCard != null))
    val isCashCardTransferValid = isCashCardAmountValid && isCashCardBalSufficient && isCashCardAccountValid

    // Aliases for compatibility
    val fromAccount = sourceCard
    val toAccount = destCard
    val walletFromAmountText = cashCardFromAmountText
    val walletRateText = cashCardRateText
    val walletToAmountText = cashCardToAmountText
    val walletNote = cashCardNote
    val isWalletTransferValid = isCashCardTransferValid

    // --- Person to Person state ---
    fun computePersonAutoRate(fromCurr: CurrencyEntity?, toCurr: CurrencyEntity?): String {
        if (fromCurr == null || toCurr == null) return "1.0"
        if (fromCurr.code.equals(toCurr.code, ignoreCase = true)) return "1.0"
        val sRate = fromCurr.exchangeRateToBase
        val dRate = toCurr.exchangeRateToBase
        val autoRate = if (dRate > 0) sRate / dRate else 1.0
        return if (autoRate == 1.0) "1.0" else String.format(Locale.US, "%.4f", autoRate).trimEnd('0').trimEnd('.')
    }

    var fromPerson by remember(editingTransferFromTxn) {
        mutableStateOf(
            if (preselectedPersonId != null) recipients.find { it.id == preselectedPersonId }
            else if (editingTransferFromTxn?.recipientId != null) recipients.find { it.id == editingTransferFromTxn.recipientId }
            else recipients.firstOrNull()
        )
    }
    var toPerson by remember(editingTransferToTxn) {
        mutableStateOf(
            if (editingTransferToTxn?.recipientId != null) recipients.find { it.id == editingTransferToTxn.recipientId } ?: recipients.filter { it.id != fromPerson?.id }.firstOrNull()
            else recipients.filter { it.id != fromPerson?.id }.firstOrNull() ?: recipients.getOrNull(1)
        )
    }
    var personFromCurrency by remember(editingTransferFromTxn) {
        mutableStateOf(
            if (editingTransferFromTxn != null) {
                (if (editingTransferFromTxn.currencyId > 0L) activeCurrencies.firstOrNull { it.id == editingTransferFromTxn.currencyId } else null)
                    ?: activeCurrencies.firstOrNull { it.code.equals(editingTransferFromTxn.currencyCode, ignoreCase = true) }
                    ?: activeCurrencies.firstOrNull()
            } else activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull()
        )
    }
    var personToCurrency by remember(editingTransferToTxn) {
        mutableStateOf(
            if (editingTransferToTxn != null) {
                (if (editingTransferToTxn.currencyId > 0L) activeCurrencies.firstOrNull { it.id == editingTransferToTxn.currencyId } else null)
                    ?: activeCurrencies.firstOrNull { it.code.equals(editingTransferToTxn.currencyCode, ignoreCase = true) }
                    ?: activeCurrencies.firstOrNull()
            } else activeCurrencies.firstOrNull { it.isBaseCurrency } ?: activeCurrencies.firstOrNull()
        )
    }
    var personFromAmountText by remember(editingTransferFromTxn) {
        mutableStateOf(
            if (editingTransferFromTxn != null && editingTransferFromTxn.recipientId != null) {
                if (editingTransferFromTxn.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", editingTransferFromTxn.amount)
                else String.format(Locale.US, "%.3f", editingTransferFromTxn.amount).trimEnd('0').trimEnd('.')
            } else ""
        )
    }
    var personRateText by remember(editingTransferFromTxn, editingTransferToTxn) {
        mutableStateOf(
            if (editingTransferFromTxn != null && editingTransferToTxn != null && editingTransferFromTxn.amount > 0) {
                String.format(Locale.US, "%.4f", editingTransferToTxn.amount / editingTransferFromTxn.amount).trimEnd('0').trimEnd('.')
            } else {
                computePersonAutoRate(personFromCurrency, personToCurrency)
            }
        )
    }
    var personToAmountText by remember(editingTransferToTxn) {
        mutableStateOf(
            if (editingTransferToTxn != null && editingTransferToTxn.recipientId != null) {
                if (editingTransferToTxn.amount % 1.0 == 0.0) String.format(Locale.US, "%.0f", editingTransferToTxn.amount)
                else String.format(Locale.US, "%.3f", editingTransferToTxn.amount).trimEnd('0').trimEnd('.')
            } else ""
        )
    }
    var personNote by remember(editingTransferFromTxn) { mutableStateOf(editingTransferFromTxn?.note ?: "") }
    var isPersonDebtTransfer by remember { mutableStateOf(true) } // True: transfer debt/claim
    var activeCalculatorField by remember { mutableStateOf<String?>(null) }

    // --- Unified Cash & Card submit action ---
    val onCashCardSubmit: () -> Unit = {
        if (isCashCardTransferValid) {
            if (isEditingTransfer && editingTransferFromTxn != null && editingTransferToTxn != null) {
                val origWasWallet = editingTransferFromTxn.accountId > 0L && editingTransferToTxn.accountId > 0L
                val newIsWallet = !isSourceCash && !isDestCash
                if (origWasWallet && newIsWallet) {
                    viewModel.updateWalletTransfer(
                        txn1Id = editingTransferFromTxn.id,
                        txn2Id = editingTransferToTxn.id,
                        oldFromAccountId = editingTransferFromTxn.accountId,
                        oldToAccountId = editingTransferToTxn.accountId,
                        oldFromAmount = editingTransferFromTxn.amount,
                        oldToAmount = editingTransferToTxn.amount,
                        newFromAccount = sourceCard!!,
                        newToAccount = destCard!!,
                        newFromAmount = parsedCashCardFromAmt,
                        newToAmount = parsedCashCardToAmt,
                        newRate = parsedCashCardRate,
                        newNote = cashCardNote.ifBlank { null },
                        onSuccess = onDismiss
                    )
                } else if (!origWasWallet && !newIsWallet) {
                    val effCashCurrency = if (isSourceCash) sourceCashCurrency!! else destCashCurrency!!
                    val effCashAmt = if (isSourceCash) parsedCashCardFromAmt else parsedCashCardToAmt
                    val effCardAmt = if (isSourceCash) parsedCashCardToAmt else parsedCashCardFromAmt
                    val effCard = if (isSourceCash) destCard!! else sourceCard!!
                    viewModel.updateCashCardTransfer(
                        txn1Id = editingTransferFromTxn.id,
                        txn2Id = editingTransferToTxn.id,
                        oldIsCashToCard = initialIsSourceCash,
                        oldCardAccountId = if (initialIsSourceCash) editingTransferToTxn.accountId else editingTransferFromTxn.accountId,
                        oldCardAmount = if (initialIsSourceCash) editingTransferToTxn.amount else editingTransferFromTxn.amount,
                        newIsCashToCard = isSourceCash,
                        newCashCurrency = effCashCurrency,
                        newCardAccount = effCard,
                        newCashAmount = effCashAmt,
                        newCardAmount = effCardAmt,
                        newRate = parsedCashCardRate,
                        newTargetCurrency = effCashCurrency,
                        newNote = cashCardNote.ifBlank { null },
                        onSuccess = onDismiss
                    )
                } else {
                    viewModel.deleteTransaction(editingTransferFromTxn)
                    if (newIsWallet) {
                        viewModel.executeWalletTransfer(
                            fromAccount = sourceCard!!,
                            toAccount = destCard!!,
                            fromAmount = parsedCashCardFromAmt,
                            toAmount = parsedCashCardToAmt,
                            rate = parsedCashCardRate,
                            note = cashCardNote.ifBlank { null },
                            onSuccess = onDismiss
                        )
                    } else {
                        val effCashCurrency = if (isSourceCash) sourceCashCurrency!! else destCashCurrency!!
                        val effCashAmt = if (isSourceCash) parsedCashCardFromAmt else parsedCashCardToAmt
                        val effCardAmt = if (isSourceCash) parsedCashCardToAmt else parsedCashCardFromAmt
                        val effCard = if (isSourceCash) destCard!! else sourceCard!!
                        viewModel.executeCashCardTransfer(
                            isCashToCard = isSourceCash,
                            cashCurrency = effCashCurrency,
                            cardAccount = effCard,
                            cashAmount = effCashAmt,
                            cardAmount = effCardAmt,
                            rate = parsedCashCardRate,
                            targetCurrency = effCashCurrency,
                            note = cashCardNote.ifBlank { null },
                            onSuccess = onDismiss
                        )
                    }
                }
            } else {
                if (!isSourceCash && !isDestCash) {
                    viewModel.executeWalletTransfer(
                        fromAccount = sourceCard!!,
                        toAccount = destCard!!,
                        fromAmount = parsedCashCardFromAmt,
                        toAmount = parsedCashCardToAmt,
                        rate = parsedCashCardRate,
                        note = cashCardNote.ifBlank { null },
                        onSuccess = onDismiss
                    )
                } else {
                    val effCashCurrency = if (isSourceCash) sourceCashCurrency!! else destCashCurrency!!
                    val effCashAmt = if (isSourceCash) parsedCashCardFromAmt else parsedCashCardToAmt
                    val effCardAmt = if (isSourceCash) parsedCashCardToAmt else parsedCashCardFromAmt
                    val effCard = if (isSourceCash) destCard!! else sourceCard!!
                    viewModel.executeCashCardTransfer(
                        isCashToCard = isSourceCash,
                        cashCurrency = effCashCurrency,
                        cardAccount = effCard,
                        cashAmount = effCashAmt,
                        cardAmount = effCardAmt,
                        rate = parsedCashCardRate,
                        targetCurrency = effCashCurrency,
                        note = cashCardNote.ifBlank { null },
                        onSuccess = onDismiss
                    )
                }
            }
        }
    }
    val onWalletSubmit: () -> Unit = onCashCardSubmit

    // --- Person to Person validation and submit ---
    val isPersonDiffCurrency = remember(personFromCurrency, personToCurrency) {
        if (personFromCurrency != null && personToCurrency != null) {
            !personFromCurrency!!.code.equals(personToCurrency!!.code, ignoreCase = true)
        } else false
    }
    val parsedPersonFromAmt = parseLocalizedNumber(personFromAmountText) ?: 0.0
    val parsedPersonToAmt = (if (isPersonDiffCurrency) parseLocalizedNumber(personToAmountText) else parsedPersonFromAmt) ?: 0.0
    val parsedPersonRate = (if (isPersonDiffCurrency) parseLocalizedNumber(personRateText) else 1.0) ?: 1.0

    val fromPersonDebtInfo = remember(fromPerson?.id, personFromCurrency?.code, allTransactions) {
        if (fromPerson != null && personFromCurrency != null) {
            viewModel.getRecipientDebtForCurrency(fromPerson!!.id, personFromCurrency!!.code)
        } else null
    }
    val toPersonDebtInfo = remember(toPerson?.id, personToCurrency?.code, allTransactions) {
        if (toPerson != null && personToCurrency != null) {
            viewModel.getRecipientDebtForCurrency(toPerson!!.id, personToCurrency!!.code)
        } else null
    }
    val detectedIsDebtor = fromPersonDebtInfo?.isDebtor ?: true
    val rawSourcePersonAvailable = kotlin.math.abs(fromPersonDebtInfo?.netAmount ?: 0.0)
    val isEditingThisPersonFrom = isEditingTransfer &&
            editingTransferFromTxn?.recipientId != null && editingTransferFromTxn.recipientId == fromPerson?.id &&
            editingTransferFromTxn.currencyCode.equals(personFromCurrency?.code, ignoreCase = true)
    val sourcePersonAvailable = if (isEditingThisPersonFrom) {
        rawSourcePersonAvailable + (editingTransferFromTxn?.amount ?: 0.0)
    } else {
        rawSourcePersonAvailable
    }
    val isPersonSourceZero = (fromPersonDebtInfo == null || (fromPersonDebtInfo.isSettled && !isEditingThisPersonFrom)) && sourcePersonAvailable <= 0.0
    val isPersonExceeding = !isPersonSourceZero && parsedPersonFromAmt > sourcePersonAvailable

    val isPersonTransferValid = fromPerson != null && toPerson != null &&
            fromPerson!!.id != toPerson!!.id && parsedPersonFromAmt > 0.0 && parsedPersonToAmt > 0.0 &&
            personFromCurrency != null && personToCurrency != null &&
            !isPersonSourceZero && !isPersonExceeding

    val onPersonSubmit: () -> Unit = {
        if (isPersonTransferValid) {
            viewModel.transferPersonDebt(
                fromRecipientId = fromPerson!!.id,
                fromPersonName = fromPerson!!.name,
                toRecipientId = toPerson!!.id,
                toPersonName = toPerson!!.name,
                fromCurrency = personFromCurrency!!,
                toCurrency = personToCurrency!!,
                fromAmount = parsedPersonFromAmt,
                toAmount = parsedPersonToAmt,
                rate = parsedPersonRate,
                note = personNote.ifBlank { null },
                isDebtor = detectedIsDebtor,
                onSuccess = onDismiss
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = Modifier.testTag("luxury_transfer_bottom_sheet")
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .heightIn(max = 720.dp)
            ) {
                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BentoIndigoAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SyncAlt,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val isUnifiedTransferMode = selectedTab == TransferTab.CASH_AND_CARD || selectedTab == TransferTab.WALLET_TO_WALLET
                            val dialogTitle = when {
                                isEditingTransfer -> when {
                                    selectedTab == TransferTab.PERSON_TO_PERSON -> "ویرایش انتقال حساب اشخاص"
                                    !isSourceCash && !isDestCash -> "ویرایش انتقال کارت به کارت"
                                    isSourceCash -> "ویرایش انتقال نقد به کارت"
                                    else -> "ویرایش انتقال کارت به نقد"
                                }
                                selectedTab == TransferTab.PERSON_TO_PERSON -> "انتقال حساب و حواله اشخاص"
                                else -> "انتقال نقد و کارت"
                            }
                            val dialogSubtitle = when {
                                isEditingTransfer -> "تنظیم مبالغ و به‌روزرسانی موجودی"
                                selectedTab == TransferTab.PERSON_TO_PERSON -> "حواله و تسویه طلب یا بدهی بین طرف‌حساب‌ها"
                                else -> "انتقال بین کارت‌های بانکی یا جابجایی بین صندوق نقد و کارت"
                            }
                            Text(
                                text = dialogTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Text(
                                text = dialogSubtitle,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = BentoNavyDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Mode Tabs (Unified Cash & Card vs Person Transfer) - Only show when creating new transfer AND not locked to a specific mode
                if (!isEditingTransfer && lockedTab == null) {
                    val isUnifiedTransferTab = selectedTab == TransferTab.CASH_AND_CARD || selectedTab == TransferTab.WALLET_TO_WALLET
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = TransferTab.CASH_AND_CARD }
                                .testTag("tab_cash_and_card"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUnifiedTransferTab) BentoNavyDark else Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = if (isUnifiedTransferTab) Color.White else BentoNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "انتقال نقد و کارت",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnifiedTransferTab) Color.White else BentoNavyDark
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedTab = TransferTab.PERSON_TO_PERSON }
                                .testTag("tab_person_to_person"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedTab == TransferTab.PERSON_TO_PERSON) BentoNavyDark else Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (selectedTab == TransferTab.PERSON_TO_PERSON) Color.White else BentoNavyDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "حواله اشخاص",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == TransferTab.PERSON_TO_PERSON) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }
                }

                // ============================================================
                // 0. UNIFIED CASH AND CARD TRANSFER CONTENT (نقد و کارت، کارت به کارت، کارت به نقد)
                // ============================================================
                val isUnifiedTransferTab = selectedTab == TransferTab.CASH_AND_CARD || selectedTab == TransferTab.WALLET_TO_WALLET
                if (isUnifiedTransferTab) {
                    if (eligibleAccounts.isEmpty() && (!isSourceCash || !isDestCash)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = "هیچ کارت یا حساب بانکی فعالی ثبت نشده است. لطفاً ابتدا از بخش کارت‌ها یک کارت تعریف کنید.",
                                color = Color(0xFF92400E),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        // Source Selection Card (مبدأ)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isSourceCash) Icons.Default.AccountBalance else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "مبدأ:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Account Type Selector for Source
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE2E8F0))
                                                .padding(2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            // Cash Option for Source (if Destination is cash, clicking this switches destination to card)
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        isSourceCash = true
                                                        if (isDestCash) isDestCash = false
                                                        updateCashCardRate(sourceCashCurrency?.code ?: "AFN", destCurrCode)
                                                    }
                                                    .testTag("source_pick_cash"),
                                                color = if (isSourceCash) BentoNavyDark else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "صندوق نقد",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSourceCash) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSourceCash) Color.White else TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            // Card Option for Source
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        isSourceCash = false
                                                        updateCashCardRate(sourceCard?.currencyCode ?: "AFN", destCurrCode)
                                                    }
                                                    .testTag("source_pick_card"),
                                                color = if (!isSourceCash) BentoNavyDark else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "کارت بانکی",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (!isSourceCash) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (!isSourceCash) Color.White else TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Live Available Balance Badge
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (effSourceAvailableBal > 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        border = BorderStroke(0.5.dp, if (effSourceAvailableBal > 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                                    ) {
                                        Text(
                                            text = "موجودی: ${viewModel.formatAmount(effSourceAvailableBal, sourceCurrCode)} $sourceCurrSymbol",
                                            color = if (effSourceAvailableBal > 0) Color(0xFF166534) else Color(0xFF991B1B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (isSourceCash) {
                                    // Pick Cash Currency for Source
                                    Box {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                                .clickable { isSourceCashDropdownOpen = true },
                                            color = SurfaceWhite
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = sourceCashCurrency?.flagEmoji ?: "💵", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${sourceCashCurrency?.name ?: ""} (${sourceCashCurrency?.code ?: ""})",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BentoNavyDark
                                                    )
                                                }
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isSourceCashDropdownOpen,
                                            onDismissRequest = { isSourceCashDropdownOpen = false }
                                        ) {
                                            activeCurrencies.forEach { curr ->
                                                val cBal = currencyBalances.find { (curr.id > 0 && it.currency.id == curr.id) || it.currency.code.equals(curr.code, ignoreCase = true) }?.balance ?: 0.0
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("${curr.flagEmoji} ${curr.name} (${curr.code})")
                                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                                Text(
                                                                    text = "${curr.symbol} ${viewModel.formatAmount(cBal)}",
                                                                    color = TextSecondary,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        sourceCashCurrency = curr
                                                        isSourceCashDropdownOpen = false
                                                        updateCashCardRate(curr.code, destCurrCode)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Pick Source Card
                                    Box {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                                .clickable { isSourceCardDropdownOpen = true },
                                            color = SurfaceWhite
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = "💳", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = sourceCard?.name ?: "انتخاب کارت مبدأ",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BentoNavyDark
                                                    )
                                                }
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isSourceCardDropdownOpen,
                                            onDismissRequest = { isSourceCardDropdownOpen = false }
                                        ) {
                                            eligibleAccounts.forEach { acct ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("💳 ${acct.name}")
                                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                                Text(
                                                                    text = "${acct.currencySymbol} ${viewModel.formatAmount(acct.balance)}",
                                                                    color = TextSecondary,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        sourceCard = acct
                                                        isSourceCardDropdownOpen = false
                                                        updateCashCardRate(acct.currencyCode, destCurrCode)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Direction Swap Button
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (isSourceCash && !isDestCash) {
                                            isSourceCash = false
                                            isDestCash = true
                                        } else if (!isSourceCash && isDestCash) {
                                            isSourceCash = true
                                            isDestCash = false
                                        } else if (!isSourceCash && !isDestCash) {
                                            val temp = sourceCard
                                            sourceCard = destCard
                                            destCard = temp
                                        }
                                        updateCashCardRate(sourceCurrCode, destCurrCode)
                                    }
                                    .testTag("swap_cash_card_direction"),
                                shape = CircleShape,
                                color = SurfaceWhite,
                                border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.4f)),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.SwapVert,
                                        contentDescription = "تعویض مبدأ و مقصد",
                                        tint = BentoIndigoAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }

                        // Destination Selection Card (مقصد)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, BentoBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isDestCash) Icons.Default.AccountBalance else Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "مقصد:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Account Type Selector for Destination
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE2E8F0))
                                                .padding(2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            // Card Option for Destination
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        isDestCash = false
                                                        updateCashCardRate(sourceCurrCode, destCard?.currencyCode ?: "AFN")
                                                    }
                                                    .testTag("dest_pick_card"),
                                                color = if (!isDestCash) BentoNavyDark else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "کارت بانکی",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (!isDestCash) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (!isDestCash) Color.White else TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            // Cash Option for Destination (if Source is cash, clicking this switches source to card)
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        isDestCash = true
                                                        if (isSourceCash) isSourceCash = false
                                                        updateCashCardRate(sourceCurrCode, destCashCurrency?.code ?: "AFN")
                                                    }
                                                    .testTag("dest_pick_cash"),
                                                color = if (isDestCash) BentoNavyDark else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "صندوق نقد",
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isDestCash) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isDestCash) Color.White else TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Current Destination Balance Badge
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(0.5.dp, BentoBorder)
                                    ) {
                                        Text(
                                            text = "موجودی فعلی: ${viewModel.formatAmount(destCurrentBal, destCurrCode)} $destCurrSymbol",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (!isDestCash) {
                                    // Target Card Selector
                                    Box {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                                .clickable { isDestCardDropdownOpen = true },
                                            color = SurfaceWhite
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = "💳", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = destCard?.name ?: "انتخاب کارت مقصد",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BentoNavyDark
                                                    )
                                                }
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isDestCardDropdownOpen,
                                            onDismissRequest = { isDestCardDropdownOpen = false }
                                        ) {
                                            eligibleAccounts.forEach { acct ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("💳 ${acct.name}")
                                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                                Text(
                                                                    text = "${acct.currencySymbol} ${viewModel.formatAmount(acct.balance)}",
                                                                    color = TextSecondary,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        destCard = acct
                                                        isDestCardDropdownOpen = false
                                                        updateCashCardRate(sourceCurrCode, acct.currencyCode)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Warning if both are cards and same card is selected
                                    if (!isSourceCash && sourceCard != null && destCard != null && sourceCard?.id == destCard?.id) {
                                        Text(
                                            text = "کارت مبدأ و مقصد نمی‌توانند یکسان باشند. لطفاً کارت دیگری برای مقصد انتخاب کنید.",
                                            color = Color(0xFFB91C1C),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Static currency info for destination card
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(0.5.dp, BentoBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("ارز کارت مقصد:", fontSize = 11.sp, color = TextSecondary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${destCard?.currencyCode ?: ""} (${destCard?.currencySymbol ?: ""}) - ثبت شده برای این کارت",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoNavyDark
                                            )
                                        }
                                    }
                                } else {
                                    // Target Cash Currency Selector
                                    Box {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                                                .clickable { isDestCashDropdownOpen = true },
                                            color = SurfaceWhite
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = destCashCurrency?.flagEmoji ?: "💵", fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${destCashCurrency?.name ?: ""} (${destCashCurrency?.code ?: ""})",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = BentoNavyDark
                                                    )
                                                }
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = isDestCashDropdownOpen,
                                            onDismissRequest = { isDestCashDropdownOpen = false }
                                        ) {
                                            activeCurrencies.forEach { curr ->
                                                val cBal = currencyBalances.find { (curr.id > 0 && it.currency.id == curr.id) || it.currency.code.equals(curr.code, ignoreCase = true) }?.balance ?: 0.0
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("${curr.flagEmoji} ${curr.name} (${curr.code})")
                                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                                Text(
                                                                    text = "${curr.symbol} ${viewModel.formatAmount(cBal)}",
                                                                    color = TextSecondary,
                                                                    fontSize = 11.sp
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        destCashCurrency = curr
                                                        isDestCashDropdownOpen = false
                                                        updateCashCardRate(sourceCurrCode, curr.code)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Destination Currency Quick Selector
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "تعیین ارز صندوق مقصد (تبدیل مستقیم):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoNavyDark
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(activeCurrencies) { curr ->
                                            val isSelected = destCurrCode.equals(curr.code, ignoreCase = true)
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        destCashCurrency = curr
                                                        updateCashCardRate(sourceCurrCode, curr.code)
                                                    }
                                                    .testTag("target_currency_${curr.code}"),
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) BentoIndigoAccent else Color.White,
                                                border = BorderStroke(1.dp, if (isSelected) BentoIndigoAccent else BentoBorder)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(curr.flagEmoji, fontSize = 12.sp)
                                                    Text(
                                                        text = curr.code,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else BentoNavyDark
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Currency Conversion Rate and Target Amount (If different currencies)
                        if (isCashCardDiff) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "تبدیل ارز ($sourceCurrCode ➔ $destCurrCode)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                        Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Conversion Rate Field
                                        OutlinedTextField(
                                            value = cashCardRateText,
                                            onValueChange = { onCashCardRateChanged(it) },
                                            label = { Text("نرخ تبدیل (۱ $sourceCurrCode)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = {
                                                CalculatorMiniButton(
                                                    onClick = { activeCalculatorField = "cash_card_rate" }
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = BentoNavyDark,
                                                unfocusedTextColor = BentoNavyDark,
                                                cursorColor = Color(0xFF166534),
                                                focusedBorderColor = Color(0xFF166534),
                                                unfocusedBorderColor = Color(0xFF86EFAC),
                                                focusedContainerColor = Color(0xFFF0FDF4),
                                                unfocusedContainerColor = Color(0xFFF0FDF4),
                                                focusedLabelColor = Color(0xFF166534),
                                                unfocusedLabelColor = TextSecondary
                                            )
                                        )

                                        // Converted Target Amount Field
                                        OutlinedTextField(
                                            value = cashCardToAmountText,
                                            onValueChange = { onCashCardToChanged(it) },
                                            label = { Text("دریافتی مقصد ($destCurrSymbol)") },
                                            modifier = Modifier.weight(1.2f),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            trailingIcon = {
                                                CalculatorMiniButton(
                                                    onClick = { activeCalculatorField = "cash_card_to" }
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = BentoNavyDark,
                                                unfocusedTextColor = BentoNavyDark,
                                                cursorColor = Color(0xFF166534),
                                                focusedBorderColor = Color(0xFF166534),
                                                unfocusedBorderColor = Color(0xFF86EFAC),
                                                focusedContainerColor = Color(0xFFF0FDF4),
                                                unfocusedContainerColor = Color(0xFFF0FDF4),
                                                focusedLabelColor = Color(0xFF166534),
                                                unfocusedLabelColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // -------------------------------------------------------------
                        // Balance Validation Warning Alerts (اعتبارسنجی موجودی)
                        // -------------------------------------------------------------
                        if (effSourceAvailableBal <= 0.0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "موجودی مبدأ صفر یا منفی است (${viewModel.formatAmount(effSourceAvailableBal, sourceCurrCode)} $sourceCurrSymbol). امکان انتقال وجود ندارد.",
                                        color = Color(0xFF991B1B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else if (parsedCashCardFromAmt > effSourceAvailableBal) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "مبلغ وارد شده (${viewModel.formatAmount(parsedCashCardFromAmt, sourceCurrCode)} $sourceCurrSymbol) بیشتر از موجودی در دسترس (${viewModel.formatAmount(effSourceAvailableBal, sourceCurrCode)} $sourceCurrSymbol) است.",
                                        color = Color(0xFF991B1B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // -------------------------------------------------------------
                        // Amount Input Field with Calculator Button
                        // -------------------------------------------------------------
                        OutlinedTextField(
                            value = cashCardFromAmountText,
                            onValueChange = { onCashCardFromChanged(it) },
                            label = { Text("مبلغ انتقالی از مبدأ ($sourceCurrSymbol)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cash_card_amount_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = {
                                Text(
                                    text = sourceCurrSymbol,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoNavyDark,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    CalculatorMiniButton(
                                        onClick = { activeCalculatorField = "cash_card_from" }
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoNavyDark,
                                unfocusedTextColor = BentoNavyDark,
                                cursorColor = BentoIndigoAccent,
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedLabelColor = BentoIndigoAccent,
                                unfocusedLabelColor = TextSecondary
                            )
                        )

                        // Note / Memo Field
                        OutlinedTextField(
                            value = cashCardNote,
                            onValueChange = { cashCardNote = it },
                            label = { Text("بابت / توضیحات انتقال (اختیاری)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoNavyDark,
                                unfocusedTextColor = BentoNavyDark,
                                cursorColor = BentoIndigoAccent,
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedLabelColor = BentoIndigoAccent,
                                unfocusedLabelColor = TextSecondary
                            )
                        )

                        // Summary Pill
                        if (parsedCashCardFromAmt > 0) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("کسر از مبدأ:", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            text = "- ${viewModel.formatAmount(parsedCashCardFromAmt)} $sourceCurrSymbol",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("واریز به مقصد:", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            text = "+ ${viewModel.formatAmount(parsedCashCardToAmt)} $destCurrSymbol",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }
                        }

                        if (isEditingTransfer && editingTransferFromTxn != null) {
                            var showDeleteCashCardConfirm by remember { mutableStateOf(false) }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { showDeleteCashCardConfirm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حذف این معامله انتقال (بازگشت موجودی‌ها به قبل)",
                                    color = ExpenseRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (showDeleteCashCardConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteCashCardConfirm = false },
                                    title = { Text("حذف معامله انتقال", fontWeight = FontWeight.Bold) },
                                    text = {
                                        Text("آیا از حذف این معامله انتقال اطمینان دارید؟ هر دو تراکنش واریز و برداشت حذف شده و موجودی کارت و نقد به حالت قبلی بازمی‌گردد.")
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                viewModel.deleteTransaction(editingTransferFromTxn)
                                                showDeleteCashCardConfirm = false
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                                        ) {
                                            Text("حذف انتقال", color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteCashCardConfirm = false }) {
                                            Text("انصراف")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 2. PERSON TO PERSON DEBT TRANSFER CONTENT
                // ==========================================
                if (selectedTab == TransferTab.PERSON_TO_PERSON) {
                    if (recipients.size < 2) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = "برای انتقال یا حواله حساب بین اشخاص، حداقل به دو مخاطب نیاز است. لطفاً ابتدا در صفحه اشخاص، مخاطب دوم را اضافه فرمایید.",
                                color = Color(0xFF92400E),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        // Source Person Selector
                        Text(text = "شخص مبدا (انتقال دهنده / تسویه):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recipients, key = { it.id }) { person ->
                                val isSelected = person.id == fromPerson?.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            fromPerson = person
                                            if (toPerson?.id == person.id) {
                                                toPerson = recipients.firstOrNull { it.id != person.id }
                                            }
                                        }
                                ) {
                                    Text(
                                        text = person.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // Destination Person Selector
                        Text(text = "شخص مقصد (انتقال گیرنده / ثبت طلب یا بدهی):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(recipients.filter { it.id != fromPerson?.id }, key = { it.id }) { person ->
                                val isSelected = person.id == toPerson?.id
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) BentoNavyDark else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { toPerson = person }
                                ) {
                                    Text(
                                        text = person.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BentoNavyDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        var fromCurrencyDropdownExpanded by remember { mutableStateOf(false) }
                        var toCurrencyDropdownExpanded by remember { mutableStateOf(false) }

                        val isPersonDiffCurrency = personFromCurrency != null && personToCurrency != null &&
                                !personFromCurrency!!.code.equals(personToCurrency!!.code, ignoreCase = true)

                        // Amount and Currency of Source Side-by-Side
                        Text(text = "مبلغ و ارز حواله (مبدا):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = personFromAmountText,
                                onValueChange = { newVal ->
                                    personFromAmountText = newVal
                                    val fAmt = parseLocalizedNumber(newVal)
                                    val r = parseLocalizedNumber(personRateText)
                                    if (fAmt > 0 && r > 0 && isPersonDiffCurrency) {
                                        personToAmountText = if ((fAmt * r) % 1.0 == 0.0) String.format(Locale.US, "%.0f", fAmt * r) else String.format(Locale.US, "%.3f", fAmt * r).trimEnd('0').trimEnd('.')
                                    } else if (!isPersonDiffCurrency) {
                                        personToAmountText = newVal
                                    }
                                },
                                label = { Text("مبلغ حواله") },
                                placeholder = { Text("مثلاً 1000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoNavyDark,
                                    unfocusedTextColor = BentoNavyDark,
                                    cursorColor = BentoIndigoAccent,
                                    focusedBorderColor = BentoIndigoAccent,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite,
                                    focusedLabelColor = BentoIndigoAccent,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                trailingIcon = {
                                    CalculatorMiniButton(
                                        onClick = { activeCalculatorField = "person_from" },
                                        contentDescription = "ماشین‌حساب مبلغ حواله"
                                    )
                                }
                            )

                            // Vertical Dropdown Currency Selector for Source
                            Box {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, BentoBorder),
                                    modifier = Modifier
                                        .height(56.dp)
                                        .padding(top = 8.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { fromCurrencyDropdownExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${personFromCurrency?.flagEmoji ?: ""} ${personFromCurrency?.code ?: "ارز"}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "انتخاب ارز مبدا",
                                            tint = BentoNavyDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = fromCurrencyDropdownExpanded,
                                    onDismissRequest = { fromCurrencyDropdownExpanded = false }
                                ) {
                                    activeCurrencies.forEach { curr ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${curr.flagEmoji} ${curr.code} (${curr.symbol})",
                                                    fontWeight = if (curr.code == personFromCurrency?.code) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                personFromCurrency = curr
                                                fromCurrencyDropdownExpanded = false
                                                val autoRateStr = computePersonAutoRate(curr, personToCurrency)
                                                personRateText = autoRateStr
                                                val fAmt = parseLocalizedNumber(personFromAmountText) ?: 0.0
                                                val r = parseLocalizedNumber(autoRateStr) ?: 1.0
                                                if (fAmt > 0 && r > 0 && !curr.code.equals(personToCurrency?.code, ignoreCase = true)) {
                                                    personToAmountText = if ((fAmt * r) % 1.0 == 0.0) String.format(Locale.US, "%.0f", fAmt * r) else String.format(Locale.US, "%.3f", fAmt * r).trimEnd('0').trimEnd('.')
                                                } else if (curr.code.equals(personToCurrency?.code, ignoreCase = true)) {
                                                    personToAmountText = personFromAmountText
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Currency of Destination Person with Vertical Dropdown
                        Text(text = "ارز شخص مقصد:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                        Box {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { toCurrencyDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${personToCurrency?.flagEmoji ?: ""} ${personToCurrency?.code ?: "انتخاب ارز مقصد"} (${personToCurrency?.symbol ?: ""})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoNavyDark
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "انتخاب ارز مقصد",
                                        tint = BentoNavyDark
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = toCurrencyDropdownExpanded,
                                onDismissRequest = { toCurrencyDropdownExpanded = false }
                            ) {
                                activeCurrencies.forEach { curr ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${curr.flagEmoji} ${curr.code} (${curr.symbol})",
                                                fontWeight = if (curr.code == personToCurrency?.code) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            personToCurrency = curr
                                            toCurrencyDropdownExpanded = false
                                            val autoRateStr = computePersonAutoRate(personFromCurrency, curr)
                                            personRateText = autoRateStr
                                            val fAmt = parseLocalizedNumber(personFromAmountText) ?: 0.0
                                            val r = parseLocalizedNumber(autoRateStr) ?: 1.0
                                            if (fAmt > 0 && r > 0 && !personFromCurrency?.code.equals(curr.code, ignoreCase = true)) {
                                                personToAmountText = if ((fAmt * r) % 1.0 == 0.0) String.format(Locale.US, "%.0f", fAmt * r) else String.format(Locale.US, "%.3f", fAmt * r).trimEnd('0').trimEnd('.')
                                            } else if (personFromCurrency?.code.equals(curr.code, ignoreCase = true)) {
                                                personToAmountText = personFromAmountText
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Simultaneous exchange rate and destination amount
                        if (isPersonDiffCurrency) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFD)),
                                border = BorderStroke(1.dp, BentoBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "تبدیل ارز حساب:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoNavyDark
                                        )
                                        Text(
                                            text = "۱ ${personFromCurrency?.code} به ${personToCurrency?.code}",
                                            fontSize = 11.sp,
                                            color = BentoIndigoAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = personRateText,
                                            onValueChange = { newRate ->
                                                personRateText = newRate
                                                val r = parseLocalizedNumber(newRate)
                                                val fAmt = parseLocalizedNumber(personFromAmountText)
                                                if (r > 0 && fAmt > 0) {
                                                    personToAmountText = if ((fAmt * r) % 1.0 == 0.0) String.format(Locale.US, "%.0f", fAmt * r) else String.format(Locale.US, "%.3f", fAmt * r).trimEnd('0').trimEnd('.')
                                                }
                                            },
                                            label = { Text("نرخ ارز") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = BentoNavyDark,
                                                unfocusedTextColor = BentoNavyDark,
                                                cursorColor = BentoIndigoAccent,
                                                focusedBorderColor = BentoIndigoAccent,
                                                unfocusedBorderColor = BentoBorder,
                                                focusedContainerColor = Color(0xFFF8FAFD),
                                                unfocusedContainerColor = Color(0xFFF8FAFD),
                                                focusedLabelColor = BentoIndigoAccent,
                                                unfocusedLabelColor = TextSecondary
                                            ),
                                            trailingIcon = {
                                                CalculatorMiniButton(
                                                    onClick = { activeCalculatorField = "person_rate" },
                                                    contentDescription = "ماشین‌حساب نرخ ارز"
                                                )
                                            }
                                        )

                                        OutlinedTextField(
                                            value = personToAmountText,
                                            onValueChange = { newToAmt ->
                                                personToAmountText = newToAmt
                                                val tAmt = parseLocalizedNumber(newToAmt)
                                                val fAmt = parseLocalizedNumber(personFromAmountText)
                                                if (tAmt > 0 && fAmt > 0) {
                                                    personRateText = String.format(Locale.US, "%.4f", tAmt / fAmt)
                                                }
                                            },
                                            label = { Text("مبلغ مقصد (${personToCurrency?.symbol ?: ""})") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = BentoNavyDark,
                                                unfocusedTextColor = BentoNavyDark,
                                                cursorColor = BentoIndigoAccent,
                                                focusedBorderColor = BentoIndigoAccent,
                                                unfocusedBorderColor = BentoBorder,
                                                focusedContainerColor = Color(0xFFF8FAFD),
                                                unfocusedContainerColor = Color(0xFFF8FAFD),
                                                focusedLabelColor = BentoIndigoAccent,
                                                unfocusedLabelColor = TextSecondary
                                            ),
                                            trailingIcon = {
                                                CalculatorMiniButton(
                                                    onClick = { activeCalculatorField = "person_to" },
                                                    contentDescription = "ماشین‌حساب مبلغ مقصد"
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = personNote,
                            onValueChange = { personNote = it },
                            label = { Text("یادداشت حواله (اختیاری)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoNavyDark,
                                unfocusedTextColor = BentoNavyDark,
                                cursorColor = BentoIndigoAccent,
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedLabelColor = BentoIndigoAccent,
                                unfocusedLabelColor = TextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val parsedPersonFromAmt = parseLocalizedNumber(personFromAmountText)
                        val parsedPersonToAmt = if (isPersonDiffCurrency) parseLocalizedNumber(personToAmountText) else parsedPersonFromAmt
                        val parsedPersonRate = if (isPersonDiffCurrency) parseLocalizedNumber(personRateText) else 1.0

                        val fromPersonDebtInfo = remember(fromPerson?.id, personFromCurrency?.code, allTransactions) {
                            if (fromPerson != null && personFromCurrency != null) {
                                viewModel.getRecipientDebtForCurrency(fromPerson!!.id, personFromCurrency!!.code)
                            } else null
                        }
                        val toPersonDebtInfo = remember(toPerson?.id, personToCurrency?.code, allTransactions) {
                            if (toPerson != null && personToCurrency != null) {
                                viewModel.getRecipientDebtForCurrency(toPerson!!.id, personToCurrency!!.code)
                            } else null
                        }

                        // Auto-detect whether transferring debt or credit:
                        // If fromPersonDebtInfo.isDebtor == true -> transferring debt (بدهی شخص مبدا به ما)
                        // If fromPersonDebtInfo.isDebtor == false -> transferring credit/claim (طلب شخص مبدا از ما)
                        val detectedIsDebtor = fromPersonDebtInfo?.isDebtor ?: true
                        val rawSourcePersonAvailable = kotlin.math.abs(fromPersonDebtInfo?.netAmount ?: 0.0)
                        val isEditingThisPersonFrom = isEditingTransfer &&
                                editingTransferFromTxn?.recipientId != null && editingTransferFromTxn.recipientId == fromPerson?.id &&
                                editingTransferFromTxn?.currencyCode.equals(personFromCurrency?.code, ignoreCase = true)
                        val sourcePersonAvailable = if (isEditingThisPersonFrom) {
                            rawSourcePersonAvailable + (editingTransferFromTxn?.amount ?: 0.0)
                        } else {
                            rawSourcePersonAvailable
                        }
                        val isPersonSourceZero = (fromPersonDebtInfo == null || (fromPersonDebtInfo.isSettled && !isEditingThisPersonFrom)) && sourcePersonAvailable <= 0.0
                        val isPersonExceeding = !isPersonSourceZero && parsedPersonFromAmt > sourcePersonAvailable

                        val isPersonTransferValid = fromPerson != null && toPerson != null &&
                                fromPerson!!.id != toPerson!!.id && parsedPersonFromAmt > 0.0 && parsedPersonToAmt > 0.0 &&
                                personFromCurrency != null && personToCurrency != null &&
                                !isPersonSourceZero && !isPersonExceeding

                        if (isPersonSourceZero && fromPerson != null && personFromCurrency != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ExpenseRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "مانده حساب شخص مبدا در این ارز صفر یا تسویه است و امکان انتقال وجود ندارد.",
                                    color = ExpenseRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else if (isPersonExceeding && fromPersonDebtInfo != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ExpenseRed.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "مبلغ انتقال نمی‌تواند بیشتر از مانده حساب شخص مبدا (${fromPersonDebtInfo.statusText}) باشد.",
                                    color = ExpenseRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        } else if (fromPerson != null && toPerson != null && fromPersonDebtInfo != null && !fromPersonDebtInfo.isSettled) {
                            val transferTypeName = if (detectedIsDebtor) "انتقال بدهی شخص مبدا" else "انتقال طلب شخص مبدا"
                            val fromImpact = if (detectedIsDebtor) "کاهش و تسویه بدهی شخص مبدا (${fromPerson?.name})" else "کاهش و تسویه طلب شخص مبدا (${fromPerson?.name})"
                            val toImpact = when {
                                toPersonDebtInfo == null || toPersonDebtInfo.isSettled -> {
                                    if (detectedIsDebtor) "ثبت بدهی برای شخص مقصد (${toPerson?.name})" else "ثبت طلب برای شخص مقصد (${toPerson?.name})"
                                }
                                !detectedIsDebtor && toPersonDebtInfo.isDebtor -> {
                                    "✨ تسویه دوطرفه: بدهی شخص مقصد (${toPerson?.name}) نیز متقابلاً کاهش می‌یابد"
                                }
                                detectedIsDebtor && !toPersonDebtInfo.isDebtor -> {
                                    "✨ تسویه دوطرفه: طلب شخص مقصد (${toPerson?.name}) نیز متقابلاً کاهش می‌یابد"
                                }
                                else -> {
                                    if (detectedIsDebtor) "افزایش بدهی شخص مقصد (${toPerson?.name})" else "افزایش طلب شخص مقصد (${toPerson?.name})"
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SyncAlt,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "تشخیص هوشمند: $transferTypeName",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = IncomeGreen
                                        )
                                    }
                                    Text(
                                        text = "• مبدا: $fromImpact (${fromPersonDebtInfo.statusText})",
                                        fontSize = 11.sp,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = "• مقصد: $toImpact" + if (toPersonDebtInfo != null && !toPersonDebtInfo.isSettled) " (${toPersonDebtInfo.statusText})" else "",
                                        fontSize = 11.sp,
                                        color = BentoNavyDark,
                                        fontWeight = if (toImpact.startsWith("✨")) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                    }
                }
            }

            // Sticky Action Buttons at the bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("cancel_transfer_sticky_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text(
                            text = "انصراف",
                            color = BentoNavyDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    when (selectedTab) {
                        TransferTab.CASH_AND_CARD -> {
                            Button(
                                onClick = onCashCardSubmit,
                                enabled = isCashCardTransferValid,
                                modifier = Modifier
                                    .weight(1.6f)
                                    .height(48.dp)
                                    .testTag("submit_cash_card_transfer_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoNavyDark,
                                    disabledContainerColor = BentoNavyDark.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = if (isEditingTransfer) "ذخیره تغییرات انتقال" else "ثبت جابجایی بیلانس",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TransferTab.WALLET_TO_WALLET -> {
                            Button(
                                onClick = onWalletSubmit,
                                enabled = isWalletTransferValid,
                                modifier = Modifier
                                    .weight(1.6f)
                                    .height(48.dp)
                                    .testTag("submit_wallet_transfer_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoNavyDark,
                                    disabledContainerColor = BentoNavyDark.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = if (isEditingTransfer) "ذخیره تغییرات انتقال" else "ثبت انتقال کارت به کارت",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        TransferTab.PERSON_TO_PERSON -> {
                            Button(
                                onClick = onPersonSubmit,
                                enabled = isPersonTransferValid,
                                modifier = Modifier
                                    .weight(1.6f)
                                    .height(48.dp)
                                    .testTag("submit_person_transfer_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoNavyDark,
                                    disabledContainerColor = BentoNavyDark.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = if (isEditingTransfer) "ذخیره تغییرات حواله" else "ثبت انتقال حساب",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    activeCalculatorField?.let { field ->
        val initVal = when (field) {
            "cash_card_from" -> cashCardFromAmountText
            "cash_card_rate" -> cashCardRateText
            "cash_card_to" -> cashCardToAmountText
            "wallet_from" -> walletFromAmountText
            "wallet_rate" -> walletRateText
            "wallet_to" -> walletToAmountText
            "person_from" -> personFromAmountText
            "person_rate" -> personRateText
            "person_to" -> personToAmountText
            else -> ""
        }
        val title = when (field) {
            "cash_card_from" -> "محاسبه مبلغ انتقال"
            "cash_card_rate" -> "محاسبه نرخ تبدیل ارز"
            "cash_card_to" -> "محاسبه مبلغ دریافتی مقصد"
            "wallet_from" -> "محاسبه مبلغ انتقال"
            "wallet_rate", "person_rate" -> "محاسبه نرخ ارز"
            "wallet_to", "person_to" -> "محاسبه مبلغ مقصد"
            "person_from" -> "محاسبه مبلغ حواله"
            else -> "ماشین‌حساب"
        }

        val isWalletDiff = fromAccount != null && toAccount != null &&
                !fromAccount!!.currencyCode.equals(toAccount!!.currencyCode, ignoreCase = true)
        val isPersonDiff = personFromCurrency != null && personToCurrency != null &&
                !personFromCurrency!!.code.equals(personToCurrency!!.code, ignoreCase = true)

        MinimalCalculatorDialog(
            initialValue = initVal,
            title = title,
            onConfirm = { calcVal ->
                when (field) {
                    "cash_card_from", "wallet_from" -> {
                        onCashCardFromChanged(calcVal)
                    }
                    "cash_card_rate", "wallet_rate" -> {
                        onCashCardRateChanged(calcVal)
                    }
                    "cash_card_to", "wallet_to" -> {
                        onCashCardToChanged(calcVal)
                    }
                    "person_from" -> {
                        personFromAmountText = calcVal
                        val fAmt = parseLocalizedNumber(calcVal)
                        val r = parseLocalizedNumber(personRateText)
                        if (fAmt > 0 && r > 0 && isPersonDiff) {
                            personToAmountText = if ((fAmt * r) % 1.0 == 0.0) String.format(Locale.US, "%.0f", fAmt * r) else String.format(Locale.US, "%.3f", fAmt * r).trimEnd('0').trimEnd('.')
                        } else if (!isPersonDiff) {
                            personToAmountText = calcVal
                        }
                    }
                    "person_rate" -> {
                        personRateText = calcVal
                        val r = parseLocalizedNumber(calcVal)
                        val fAmt = parseLocalizedNumber(personFromAmountText)
                        if (r > 0 && fAmt > 0) {
                            personToAmountText = if ((fAmt * r) % 1.0 == 0.0) String.format(Locale.US, "%.0f", fAmt * r) else String.format(Locale.US, "%.3f", fAmt * r).trimEnd('0').trimEnd('.')
                        }
                    }
                    "person_to" -> {
                        personToAmountText = calcVal
                        val tAmt = parseLocalizedNumber(calcVal)
                        val fAmt = parseLocalizedNumber(personFromAmountText)
                        if (tAmt > 0 && fAmt > 0) {
                            personRateText = String.format(Locale.US, "%.4f", tAmt / fAmt)
                        }
                    }
                }
            },
            onDismiss = { activeCalculatorField = null }
        )
    }
}
