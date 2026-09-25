package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.lazy.LazyRow
import com.example.data.local.TransactionType
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AccountCardEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CurrencyEntity
import com.example.data.local.ShoppingListEntity
import com.example.data.local.ShoppingListItemEntity
import com.example.data.local.ShoppingListWithItems
import com.example.ui.components.SolarDatePickerDialog
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.PersianDateHelper

import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun formatPrice(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    val formatter = if (amount % 1.0 == 0.0) {
        DecimalFormat("#,##0", symbols)
    } else {
        DecimalFormat("#,##0.###", symbols)
    }
    return formatter.format(amount)
}

private fun parseLocalizedDouble(text: String): Double? {
    if (text.isBlank()) return null
    val cleaned = text.trim()
        .replace("،", "")
        .replace(",", "")
        .map { ch ->
            when (ch) {
                '۰', '٠' -> '0'
                '۱', '١' -> '1'
                '۲', '٢' -> '2'
                '۳', '٣' -> '3'
                '۴', '٤' -> '4'
                '۵', '٥' -> '5'
                '۶', '٦' -> '6'
                '۷', '٧' -> '7'
                '۸', '٨' -> '8'
                '۹', '٩' -> '9'
                '٫', '/' -> '.'
                else -> ch
            }
        }.joinToString("")
    return cleaned.toDoubleOrNull()
}

private fun extractNumericQuantity(quantityStr: String): Double {
    if (quantityStr.isBlank()) return 1.0
    val cleaned = quantityStr.map { ch ->
        when (ch) {
            '۰', '٠' -> '0'
            '۱', '١' -> '1'
            '۲', '٢' -> '2'
            '۳', '٣' -> '3'
            '۴', '٤' -> '4'
            '۵', '٥' -> '5'
            '۶', '٦' -> '6'
            '۷', '٧' -> '7'
            '۸', '٨' -> '8'
            '۹', '٩' -> '9'
            '٫', '/' -> '.'
            else -> ch
        }
    }.joinToString("")

    val regex = Regex("""\d+(\.\d+)?""")
    val match = regex.find(cleaned)
    return match?.value?.toDoubleOrNull() ?: 1.0
}

enum class ShoppingListFilter {
    ALL,
    PENDING,
    LOGGED_AS_EXPENSE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: () -> Unit,
    initialListIdToOpen: Long? = null,
    modifier: Modifier = Modifier
) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    val activeCurrencies by viewModel.activeCurrencies.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.activeCategories.collectAsState()

    var filter by remember { mutableStateOf(ShoppingListFilter.PENDING) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for creating/editing list header
    var showCreateEditListDialog by remember { mutableStateOf(false) }
    var editingListHeader by remember { mutableStateOf<ShoppingListEntity?>(null) }

    // State for viewing/editing a full shopping list items
    var selectedListWithItems by remember { mutableStateOf<ShoppingListWithItems?>(null) }

    // Dialog for logging as expense
    var listToLogAsExpense by remember { mutableStateOf<ShoppingListWithItems?>(null) }

    // Dialog for delete confirmation
    var listToDelete by remember { mutableStateOf<ShoppingListWithItems?>(null) }

    // System back key handling: If in detail view, go back to lists; otherwise, handled by caller (goes back to Home)
    BackHandler(enabled = selectedListWithItems != null) {
        selectedListWithItems = null
    }

    // Auto-open initial list if requested
    LaunchedEffect(initialListIdToOpen, shoppingLists) {
        if (initialListIdToOpen != null && selectedListWithItems == null) {
            val found = shoppingLists.firstOrNull { it.list.id == initialListIdToOpen }
            if (found != null) {
                selectedListWithItems = found
            }
        }
    }

    // Keep selectedListWithItems in sync with latest db updates
    selectedListWithItems?.let { current ->
        val updated = shoppingLists.firstOrNull { it.list.id == current.list.id }
        if (updated != null && updated != current) {
            selectedListWithItems = updated
        }
    }

    val filteredLists = remember(shoppingLists, filter, searchQuery) {
        shoppingLists.filter { item ->
            val matchesFilter = when (filter) {
                ShoppingListFilter.ALL -> true
                ShoppingListFilter.PENDING -> !item.list.isLoggedAsExpense
                ShoppingListFilter.LOGGED_AS_EXPENSE -> item.list.isLoggedAsExpense
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.list.title.contains(searchQuery, ignoreCase = true) ||
                        item.items.any { it.title.contains(searchQuery, ignoreCase = true) }
            }
            matchesFilter && matchesSearch
        }
    }

    val totalListsCount = shoppingLists.size
    val pendingListsCount = shoppingLists.count { !it.list.isLoggedAsExpense }
    val loggedListsCount = shoppingLists.count { it.list.isLoggedAsExpense }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (selectedListWithItems != null) {
            ShoppingListDetailView(
                listWithItems = selectedListWithItems!!,
                activeCurrencies = activeCurrencies,
                accounts = accounts,
                categories = categories,
                onBack = { selectedListWithItems = null },
                onSaveListAndItems = { updatedList, updatedItems ->
                    viewModel.saveShoppingList(updatedList, updatedItems)
                },
                onTogglePurchased = { itemId, isPurchased ->
                    viewModel.toggleShoppingListItemPurchased(itemId, isPurchased)
                },
                onLogAsExpense = {
                    listToLogAsExpense = selectedListWithItems
                },
                onEditHeader = {
                    editingListHeader = selectedListWithItems!!.list
                    showCreateEditListDialog = true
                },
                onDelete = {
                    listToDelete = selectedListWithItems
                },
                modifier = modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                containerColor = BackgroundCanvas,
                topBar = {
                    Surface(
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IconButton(
                                        onClick = onNavigateBack,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .testTag("btn_back_to_home")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "بازگشت به صفحه اصلی",
                                            tint = BentoNavyDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BentoLavenderSubtle)
                                            .border(1.dp, BentoBorder, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            tint = BentoIndigoAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "لیست‌های خرید",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = BentoNavyDark
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        editingListHeader = null
                                        showCreateEditListDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(38.dp).testTag("btn_create_new_shopping_list")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("لیست جدید", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجو در لیست‌ها و کالاها...", fontSize = 12.5.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("search_shopping_lists_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "پاک کردن", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoNavyDark,
                                unfocusedTextColor = BentoNavyDark,
                                focusedContainerColor = BackgroundCanvas,
                                unfocusedContainerColor = BackgroundCanvas,
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filter == ShoppingListFilter.ALL,
                                onClick = { filter = ShoppingListFilter.ALL },
                                label = { Text("همه ($totalListsCount)", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BentoNavyDark,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("filter_all_shopping_lists")
                            )

                            FilterChip(
                                selected = filter == ShoppingListFilter.PENDING,
                                onClick = { filter = ShoppingListFilter.PENDING },
                                label = { Text("در انتظار خرید ($pendingListsCount)", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BentoIndigoAccent,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("filter_pending_shopping_lists")
                            )

                            FilterChip(
                                selected = filter == ShoppingListFilter.LOGGED_AS_EXPENSE,
                                onClick = { filter = ShoppingListFilter.LOGGED_AS_EXPENSE },
                                label = { Text("ثبت شده در مصارف ($loggedListsCount)", fontSize = 11.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeGreen,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("filter_logged_shopping_lists")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (filteredLists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(BentoLavenderSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "هیچ موردی مطابق جستجو یافت نشد" else "هنوز لیست خریدی ایجاد نشده است",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                        Text(
                            text = "با ایجاد لیست خرید، اجناس مورد نیاز را بنویسید، پس از خرید قیمت هر قلم یا کل را وارد کنید و مستقیماً به عنوان مصرف ثبت نمایید.",
                            fontSize = 12.5.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Button(
                            onClick = {
                                editingListHeader = null
                                showCreateEditListDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(top = 8.dp).testTag("empty_create_shopping_list_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ایجاد اولین لیست خرید", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 14.dp, bottom = 80.dp)
                ) {
                    items(filteredLists, key = { it.list.id }) { itemWithItems ->
                        ShoppingListCard(
                            listWithItems = itemWithItems,
                            onClick = { selectedListWithItems = itemWithItems },
                            onTogglePurchased = { itemId, isPurchased ->
                                viewModel.toggleShoppingListItemPurchased(itemId, isPurchased)
                            },
                            onLogAsExpense = { listToLogAsExpense = itemWithItems },
                            onEditHeader = {
                                editingListHeader = itemWithItems.list
                                showCreateEditListDialog = true
                            },
                            onDelete = { listToDelete = itemWithItems }
                        )
                    }
                }
            }
        }
        }

        // Dialog for Create / Edit List Header
        if (showCreateEditListDialog) {
            ShoppingListHeaderDialog(
                initialList = editingListHeader,
                activeCurrencies = activeCurrencies,
                categories = categories,
                onDismiss = { showCreateEditListDialog = false },
                onSave = { newList ->
                    val existingItems = editingListHeader?.let { old ->
                        shoppingLists.firstOrNull { it.list.id == old.id }?.items
                    } ?: emptyList()
                    viewModel.saveShoppingList(newList, existingItems) { listId ->
                        showCreateEditListDialog = false
                        val created = shoppingLists.firstOrNull { it.list.id == listId }
                        if (created != null) {
                            selectedListWithItems = created
                        }
                    }
                }
            )
        }

        // Dialog for Logging List as Expense
        listToLogAsExpense?.let { targetList ->
            LogShoppingListExpenseDialog(
                listWithItems = targetList,
                accounts = accounts,
                categories = categories,
                onDismiss = { listToLogAsExpense = null },
                onConfirm = { accountId, category, timestamp, note ->
                    viewModel.logShoppingListAsExpense(
                        listId = targetList.list.id,
                        accountId = accountId,
                        category = category,
                        timestamp = timestamp,
                        customNote = note
                    ) { success, _ ->
                        if (success) {
                            listToLogAsExpense = null
                        }
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        listToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { listToDelete = null },
                title = { Text("حذف لیست خرید", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("آیا از حذف لیست «${target.list.title}» اطمینان دارید؟")
                        if (target.list.isLoggedAsExpense) {
                            Text(
                                text = "توجه: این لیست قبلاً به عنوان مصرف در سیستم مالی ثبت شده است. مایلید تراکنش مصرف مرتبط نیز حذف و موجودی به حساب بازگردد؟",
                                fontSize = 12.5.sp,
                                color = ExpenseRed
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteShoppingList(
                                listId = target.list.id,
                                deleteLinkedExpense = target.list.isLoggedAsExpense
                            ) {
                                if (selectedListWithItems?.list?.id == target.list.id) {
                                    selectedListWithItems = null
                                }
                                listToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Text("حذف کامل")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { listToDelete = null }) {
                        Text("انصراف")
                    }
                }
            )
        }
    }
}

@Composable
fun ShoppingListsSheet(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    initialListIdToOpen: Long? = null
) {
    ShoppingListsScreen(
        viewModel = viewModel,
        onNavigateBack = onDismiss,
        initialListIdToOpen = initialListIdToOpen
    )
}

@Composable
fun ShoppingListCard(
    listWithItems: ShoppingListWithItems,
    onClick: () -> Unit,
    onTogglePurchased: (Long, Boolean) -> Unit,
    onLogAsExpense: () -> Unit,
    onEditHeader: () -> Unit,
    onDelete: () -> Unit
) {
    val list = listWithItems.list
    val items = listWithItems.items
    val effectiveTotal = listWithItems.effectiveTotal
    val purchasedCount = listWithItems.purchasedCount
    val totalCount = listWithItems.totalCount
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color(0x14001552))
            .clickable(onClick = onClick)
            .testTag("shopping_list_card_${list.id}"),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.2.dp, if (list.isLoggedAsExpense) Color(0xFFBBF7D0) else BentoBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Title, Jalali Date & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (list.isLoggedAsExpense) Color(0xFFF0FDF4) else BentoLavenderSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (list.isLoggedAsExpense) Icons.Default.Receipt else Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = if (list.isLoggedAsExpense) IncomeGreen else BentoIndigoAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = list.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoNavyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = PersianDateHelper.formatSolarDateTime(list.purchaseDate),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status Badge
                    if (list.isLoggedAsExpense) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(0.8.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(12.dp))
                                Text("مصرف ثبت شده", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                        }
                    } else if (totalCount > 0 && purchasedCount == totalCount) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE0E7FF),
                            border = BorderStroke(0.8.dp, Color(0xFFA5B4FC))
                        ) {
                            Text(
                                text = "تکمیل شده",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoIndigoAccent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(0.8.dp, BentoBorder)
                        ) {
                            Text(
                                text = "$purchasedCount از $totalCount قلم",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp).testTag("btn_shopping_list_menu_${list.id}")
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "گزینه‌ها", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("ویرایش عنوان و ارز") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onEditHeader()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("اشتراک‌گذاری لیست") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    shareShoppingListText(context, listWithItems)
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("حذف لیست", color = ExpenseRed) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            // Progress bar
            if (totalCount > 0) {
                val progress = purchasedCount.toFloat() / totalCount.toFloat()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress == 1f) IncomeGreen else BentoIndigoAccent,
                        trackColor = Color(0xFFF1F5F9)
                    )
                }
            }

            HorizontalDivider(color = BentoBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

            // Bottom Row: Total & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مجموع لیست",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatPrice(effectiveTotal),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoNavyDark
                        )
                        Text(
                            text = list.currencySymbol,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoIndigoAccent
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BentoBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp).testTag("btn_view_shopping_list_${list.id}")
                    ) {
                        Text("بررسی اقلام", fontSize = 12.sp, color = BentoNavyDark, fontWeight = FontWeight.SemiBold)
                    }

                    if (!list.isLoggedAsExpense) {
                        Button(
                            onClick = onLogAsExpense,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(38.dp).testTag("btn_log_expense_${list.id}")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ثبت مصرف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListDetailView(
    listWithItems: ShoppingListWithItems,
    activeCurrencies: List<CurrencyEntity>,
    accounts: List<AccountCardEntity>,
    categories: List<CategoryEntity>,
    onBack: () -> Unit,
    onSaveListAndItems: (ShoppingListEntity, List<ShoppingListItemEntity>) -> Unit,
    onTogglePurchased: (Long, Boolean) -> Unit,
    onLogAsExpense: () -> Unit,
    onEditHeader: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var listState by remember(listWithItems) { mutableStateOf(listWithItems.list) }
    val localItems = remember(listWithItems) {
        val sorted = listWithItems.items.sortedWith(
            compareBy<ShoppingListItemEntity> { it.isPurchased }
                .thenBy { it.displayOrder }
        )
        mutableStateListOf(*sorted.toTypedArray())
    }

    // Inline add item states
    var newItemTitle by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var newItemPriceText by remember { mutableStateOf("") }
    val titleFocusRequester = remember { FocusRequester() }

    // Inline editing expanded item state
    var expandedItemId by remember { mutableStateOf<Long?>(null) }
    var itemPendingDelete by remember { mutableStateOf<Pair<Int, ShoppingListItemEntity>?>(null) }

    // Manual total mode states
    var isItemizedMode by remember(listWithItems) { mutableStateOf(listWithItems.list.useItemizedSum) }
    var manualTotalText by remember(listWithItems) {
        mutableStateOf(
            listWithItems.list.manualTotalAmount?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        )
    }

    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showSaveBanner by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun recalculateAndSave(showFeedback: Boolean = false) {
        val manualVal = parseLocalizedDouble(manualTotalText)
        val updatedList = listState.copy(
            useItemizedSum = isItemizedMode,
            manualTotalAmount = manualVal
        )
        onSaveListAndItems(updatedList, localItems.toList())
        hasUnsavedChanges = false
        if (showFeedback) {
            Toast.makeText(context, "لیست خرید بروزرسانی شد", Toast.LENGTH_SHORT).show()
            showSaveBanner = true
            coroutineScope.launch {
                delay(2500)
                showSaveBanner = false
            }
        }
    }

    fun addNewItem() {
        if (newItemTitle.isNotBlank()) {
            val qtyNumber = extractNumericQuantity(newItemQuantity)
            val unitPrice = parseLocalizedDouble(newItemPriceText) ?: 0.0
            val totalPrice = if (unitPrice > 0.0) qtyNumber * unitPrice else 0.0

            val newItem = ShoppingListItemEntity(
                listId = listState.id,
                title = newItemTitle.trim(),
                quantity = newItemQuantity.trim(),
                unitPrice = unitPrice,
                price = totalPrice,
                isPurchased = false,
                displayOrder = localItems.size
            )
            val firstPurchasedIdx = localItems.indexOfFirst { it.isPurchased }
            if (firstPurchasedIdx != -1) {
                localItems.add(firstPurchasedIdx, newItem)
            } else {
                localItems.add(newItem)
            }
            for (i in localItems.indices) {
                localItems[i] = localItems[i].copy(displayOrder = i)
            }
            newItemTitle = ""
            newItemQuantity = ""
            newItemPriceText = ""
            hasUnsavedChanges = true
            recalculateAndSave()
            try {
                titleFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    BackHandler(enabled = true) {
        if (hasUnsavedChanges) recalculateAndSave()
        onBack()
    }

    val itemsSum = localItems.sumOf { it.price }
    val effectiveTotal = if (!isItemizedMode && (parseLocalizedDouble(manualTotalText) ?: 0.0) > 0.0) {
        parseLocalizedDouble(manualTotalText) ?: 0.0
    } else {
        itemsSum
    }

    Scaffold(
        modifier = modifier,
        containerColor = BackgroundCanvas,
        topBar = {
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BentoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    if (hasUnsavedChanges) recalculateAndSave()
                                    onBack()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .testTag("btn_back_to_shopping_lists")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "بازگشت به لیست‌ها",
                                    tint = BentoNavyDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoLavenderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(20.dp))
                            }

                            Column {
                                Text(
                                    text = listState.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = BentoNavyDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = PersianDateHelper.formatSolarDateTime(listState.purchaseDate),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconButton(
                                onClick = { shareShoppingListText(context, listWithItems.copy(list = listState, items = localItems.toList())) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BentoLavenderSubtle)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "اشتراک‌گذاری", tint = BentoNavyDark, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = onEditHeader,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش مشخصات", tint = BentoNavyDark, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed.copy(alpha = 0.12f))
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف لیست", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Bottom Fixed Summary & Actions Bar (پایین صفحه ثابت)
            Surface(
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Displays
                    val checkedSum = localItems.filter { it.isPurchased }.sumOf { it.price }
                    val checkedCount = localItems.count { it.isPurchased }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // جمله کل لیست
                            Column {
                                Text(
                                    text = if (isItemizedMode) "جمله کل لیست" else "مبلغ مقطوع لیست",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = formatPrice(effectiveTotal),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = BentoNavyDark
                                    )
                                    Text(
                                        text = listState.currencySymbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoIndigoAccent
                                    )
                                }
                            }

                            // جداکننده عمودی
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(32.dp)
                                    .background(BentoBorder)
                            )

                            // جمله اقلام تیک شده کنار جمله کل
                            Column {
                                Text(
                                    text = "جمله تیک‌شده ($checkedCount)",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = formatPrice(checkedSum),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color(0xFF059669)
                                    )
                                    Text(
                                        text = listState.currencySymbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }

                        if (hasUnsavedChanges) {
                            Button(
                                onClick = { recalculateAndSave(showFeedback = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ذخیره", fontSize = 12.sp)
                            }
                        }
                    }

                    // Notification that list was saved
                    AnimatedVisibility(
                        visible = showSaveBanner,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ذخیره شد",
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }

                    // Action Buttons: Update Shopping List + Log as expense
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                recalculateAndSave(showFeedback = true)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_update_shopping_list")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بروزرسانی لیست خرید", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (!listState.isLoggedAsExpense) {
                            Button(
                                onClick = {
                                    if (hasUnsavedChanges) recalculateAndSave()
                                    onLogAsExpense()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_sheet_log_expense")
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ثبت به عنوان مصرف", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        // Main Scrollable Items List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                    // Status Alert Banner if logged as expense
                    if (listState.isLoggedAsExpense) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "این لیست به عنوان مصرف در سیستم ثبت شده است",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF166534)
                                        )
                                        Text(
                                            text = "با ویرایش قیمت هر قلم یا کل لیست، مصرف ثبت شده و اثر حساب نیز خودکار به‌روزرسانی می‌شود.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Calculation mode switch (جدا جدا برای هر آیتم vs بصورت کلی)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BackgroundCanvas,
                            border = BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(18.dp))
                                        Text("نحوه محاسبه قیمت لیست", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                isItemizedMode = !isItemizedMode
                                                hasUnsavedChanges = true
                                                recalculateAndSave()
                                            }
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (isItemizedMode) "جدا جدا برای هر قلم" else "قیمت کلی مقطوع",
                                            fontSize = 11.5.sp,
                                            color = BentoIndigoAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Switch(
                                            checked = isItemizedMode,
                                            onCheckedChange = { checked ->
                                                isItemizedMode = checked
                                                hasUnsavedChanges = true
                                                recalculateAndSave()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = BentoIndigoAccent
                                            )
                                        )
                                    }
                                }

                                if (!isItemizedMode) {
                                    OutlinedTextField(
                                        value = manualTotalText,
                                        onValueChange = {
                                            manualTotalText = it
                                            hasUnsavedChanges = true
                                        },
                                        label = { Text("قیمت کلی برای کل لیست") },
                                        placeholder = { Text("مثلاً ۲۵۰۰") },
                                        suffix = { Text(listState.currencySymbol, fontWeight = FontWeight.Bold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { recalculateAndSave() }),
                                        modifier = Modifier.fillMaxWidth().testTag("manual_total_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = SurfaceWhite,
                                            unfocusedContainerColor = SurfaceWhite,
                                            focusedBorderColor = BentoIndigoAccent,
                                            unfocusedBorderColor = BentoBorder
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Quick Add New Item Box (ثبت سریع جنس جدید)
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.2.dp, BentoIndigoAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "ثبت سریع جنس جدید",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BentoIndigoAccent
                                        )
                                    }

                                    val currentQtyNum = extractNumericQuantity(newItemQuantity)
                                    val currentUnitPrice = parseLocalizedDouble(newItemPriceText) ?: 0.0
                                    val currentCalcTotal = if (currentUnitPrice > 0.0) currentQtyNum * currentUnitPrice else 0.0

                                    if (currentCalcTotal > 0.0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BentoLavenderSubtle,
                                            border = BorderStroke(0.8.dp, BentoIndigoAccent.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "مجموع: ${listState.currencySymbol} ${formatPrice(currentCalcTotal)}",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoIndigoAccent,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = newItemTitle,
                                    onValueChange = { newItemTitle = it },
                                    placeholder = { Text("نام جنس یا کالا (مثلاً برنج، روغن)...", fontSize = 12.5.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(titleFocusRequester)
                                        .testTag("new_item_title_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = BackgroundCanvas,
                                        unfocusedContainerColor = BackgroundCanvas,
                                        focusedBorderColor = BentoIndigoAccent,
                                        unfocusedBorderColor = BentoBorder
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newItemQuantity,
                                        onValueChange = { newItemQuantity = it },
                                        placeholder = { Text("مقدار (اختیاری)", fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f).testTag("new_item_quantity_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = BackgroundCanvas,
                                            unfocusedContainerColor = BackgroundCanvas,
                                            focusedBorderColor = BentoIndigoAccent,
                                            unfocusedBorderColor = BentoBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = newItemPriceText,
                                        onValueChange = { newItemPriceText = it },
                                        placeholder = { Text("قیمت فی (اختیاری)", fontSize = 12.sp) },
                                        suffix = { Text(listState.currencySymbol, fontSize = 11.sp, color = TextSecondary) },
                                        modifier = Modifier.weight(1.3f).testTag("new_item_price_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                addNewItem()
                                            }
                                        ),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = BackgroundCanvas,
                                            unfocusedContainerColor = BackgroundCanvas,
                                            focusedBorderColor = BentoIndigoAccent,
                                            unfocusedBorderColor = BentoBorder
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            addNewItem()
                                        },
                                        enabled = newItemTitle.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(52.dp).testTag("btn_add_shopping_item")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("افزودن", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Section Title: Items
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اقلام لیست خرید (${localItems.count { it.isPurchased }} از ${localItems.size} خریده شده)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = BentoNavyDark
                            )
                            if (localItems.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        val allBought = localItems.all { it.isPurchased }
                                        localItems.indices.forEach { idx ->
                                            localItems[idx] = localItems[idx].copy(isPurchased = !allBought)
                                        }
                                        hasUnsavedChanges = true
                                        recalculateAndSave()
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = if (localItems.all { it.isPurchased }) "لغو همه" else "تیک زدن همه",
                                        fontSize = 11.5.sp,
                                        color = BentoIndigoAccent
                                    )
                                }
                            }
                        }
                    }

                    // Empty state for items
                    if (localItems.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceWhite,
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                    Text("هنوز جنسی به این لیست اضافه نشده است", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                    Text("از کادر بالا نام جنس، مقدار و قیمت فی را وارد کرده و دکمه افزودن را بزنید.", fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    // Shopping List Items (Swipe to Delete with 'X' / 'Delete' & Inline Edit)
                    items(
                        count = localItems.size,
                        key = { localItems[it].id.takeIf { id -> id != 0L } ?: localItems[it].hashCode() }
                    ) { index ->
                        val item = localItems[index]
                        val itemKey = if (item.id != 0L) item.id else (index.toLong() + 100000L)
                        ShoppingListItemRow(
                            item = item,
                            currencySymbol = listState.currencySymbol,
                            isExpanded = expandedItemId == itemKey,
                            onTogglePurchased = { isChecked ->
                                val currentIdx = localItems.indexOfFirst {
                                    (it.id != 0L && it.id == item.id) || (it === item)
                                }
                                if (currentIdx != -1) {
                                    val updated = item.copy(isPurchased = isChecked)
                                    localItems.removeAt(currentIdx)
                                    if (isChecked) {
                                        // وقتی آیتم تیک می‌خوره بره آخر لیست
                                        localItems.add(updated)
                                    } else {
                                        // وقتی تیک برداشته میشه بره قبل از اولین آیتم تیک خورده
                                        val firstPurchasedIdx = localItems.indexOfFirst { it.isPurchased }
                                        if (firstPurchasedIdx != -1) {
                                            localItems.add(firstPurchasedIdx, updated)
                                        } else {
                                            localItems.add(updated)
                                        }
                                    }
                                    for (i in localItems.indices) {
                                        localItems[i] = localItems[i].copy(displayOrder = i)
                                    }
                                    hasUnsavedChanges = true
                                    recalculateAndSave()
                                }
                            },
                            onToggleExpand = {
                                expandedItemId = if (expandedItemId == itemKey) null else itemKey
                            },
                            onSaveInlineEdit = { updatedTitle, updatedQty, updatedUnitPrice, updatedTotalPrice ->
                                localItems[index] = item.copy(
                                    title = updatedTitle,
                                    quantity = updatedQty,
                                    unitPrice = updatedUnitPrice,
                                    price = updatedTotalPrice
                                )
                                hasUnsavedChanges = true
                                recalculateAndSave()
                            },
                            onDelete = {
                                itemPendingDelete = Pair(index, item)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
    }

    // دیالوگ تأیید حذف قلم کالا از لیست خرید
    itemPendingDelete?.let { (_, item) ->
        AlertDialog(
            onDismissRequest = { itemPendingDelete = null },
            title = {
                Text(
                    text = "حذف قلم از لیست خرید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BentoNavyDark
                )
            },
            text = {
                Text(
                    text = "آیا از حذف «${item.title}» از لیست خرید اطمینان دارید؟",
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentIdx = localItems.indexOfFirst {
                            (it.id != 0L && it.id == item.id) || (it === item)
                        }
                        if (currentIdx != -1) {
                            val itemKey = if (item.id != 0L) item.id else (currentIdx.toLong() + 100000L)
                            if (expandedItemId == itemKey) {
                                expandedItemId = null
                            }
                            localItems.removeAt(currentIdx)
                            hasUnsavedChanges = true
                            recalculateAndSave()
                        }
                        itemPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("بله، حذف شود", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { itemPendingDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("انصراف")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceWhite
        )
    }
}

@Composable
fun ShoppingListDetailSheet(
    listWithItems: ShoppingListWithItems,
    activeCurrencies: List<CurrencyEntity>,
    accounts: List<AccountCardEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSaveListAndItems: (ShoppingListEntity, List<ShoppingListItemEntity>) -> Unit,
    onTogglePurchased: (Long, Boolean) -> Unit,
    onLogAsExpense: () -> Unit,
    onEditHeader: () -> Unit,
    onDelete: () -> Unit
) {
    ShoppingListDetailView(
        listWithItems = listWithItems,
        activeCurrencies = activeCurrencies,
        accounts = accounts,
        categories = categories,
        onBack = onDismiss,
        onSaveListAndItems = onSaveListAndItems,
        onTogglePurchased = onTogglePurchased,
        onLogAsExpense = onLogAsExpense,
        onEditHeader = onEditHeader,
        onDelete = onDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListItemRow(
    item: ShoppingListItemEntity,
    currencySymbol: String,
    isExpanded: Boolean,
    onTogglePurchased: (Boolean) -> Unit,
    onToggleExpand: () -> Unit,
    onSaveInlineEdit: (title: String, quantity: String, unitPrice: Double, totalPrice: Double) -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
            } else {
                false
            }
        }
    )

    var editTitle by remember(item.title, isExpanded) { mutableStateOf(item.title) }
    var editQuantity by remember(item.quantity, isExpanded) { mutableStateOf(item.quantity) }
    var editUnitPriceText by remember(item.unitPrice, item.price, isExpanded) {
        val initialUnitPrice = if (item.unitPrice > 0.0) {
            item.unitPrice
        } else if (item.price > 0.0) {
            item.price / extractNumericQuantity(item.quantity)
        } else 0.0
        mutableStateOf(
            if (initialUnitPrice > 0.0) {
                if (initialUnitPrice % 1.0 == 0.0) initialUnitPrice.toLong().toString() else initialUnitPrice.toString()
            } else ""
        )
    }

    val previewQty = extractNumericQuantity(editQuantity)
    val previewUnitPrice = parseLocalizedDouble(editUnitPriceText) ?: 0.0
    val previewTotal = if (previewUnitPrice > 0.0) previewQty * previewUnitPrice else 0.0

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExpenseRed)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف قلم",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "حذف",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        content = {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isExpanded) SurfaceWhite else if (item.isPurchased) Color(0xFFF8FAFC) else SurfaceWhite,
                border = BorderStroke(
                    if (isExpanded) 1.5.dp else 1.dp,
                    if (isExpanded) BentoIndigoAccent else if (item.isPurchased) Color(0xFFE2E8F0) else BentoBorder
                ),
                shadowElevation = if (isExpanded) 2.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header row: Checkbox + Title / SubDetails (toggle purchased) + Price / Expand (toggle expand)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTogglePurchased(!item.isPurchased) }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Checkbox(
                                checked = item.isPurchased,
                                onCheckedChange = onTogglePurchased,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = IncomeGreen,
                                    checkmarkColor = Color.White
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (item.isPurchased) FontWeight.Normal else FontWeight.SemiBold,
                                    color = if (item.isPurchased) TextSecondary else BentoNavyDark,
                                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val displayUnitPrice = if (item.unitPrice > 0.0) {
                                    item.unitPrice
                                } else if (item.price > 0.0) {
                                    item.price / extractNumericQuantity(item.quantity)
                                } else {
                                    0.0
                                }

                                val subDetails = buildString {
                                    if (item.quantity.isNotBlank() && displayUnitPrice > 0.0) {
                                        append("مقدار: ${item.quantity} • فی: $currencySymbol ${formatPrice(displayUnitPrice)}")
                                    } else if (item.quantity.isNotBlank()) {
                                        append("مقدار: ${item.quantity}")
                                    } else if (displayUnitPrice > 0.0) {
                                        append("قیمت فی: $currencySymbol ${formatPrice(displayUnitPrice)}")
                                    }
                                }

                                if (subDetails.isNotEmpty()) {
                                    Text(
                                        text = subDetails,
                                        fontSize = 11.sp,
                                        color = if (item.isPurchased) TextSecondary.copy(alpha = 0.7f) else TextSecondary,
                                        textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleExpand() }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            if (item.price > 0.0) {
                                Text(
                                    text = "$currencySymbol ${formatPrice(item.price)}",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (item.isPurchased) TextSecondary else BentoNavyDark,
                                    textDecoration = if (item.isPurchased) TextDecoration.LineThrough else null
                                )
                            }
                            IconButton(
                                onClick = onToggleExpand,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.Edit,
                                    contentDescription = if (isExpanded) "بستن کادر ویرایش" else "ویرایش جنس",
                                    tint = if (isExpanded) BentoIndigoAccent else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Inline Editing Section (Expands right underneath the item)
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundCanvas.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = BentoBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("نام جنس / کالا", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("inline_edit_title_${item.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceWhite,
                                    unfocusedContainerColor = SurfaceWhite,
                                    focusedBorderColor = BentoIndigoAccent,
                                    unfocusedBorderColor = BentoBorder
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editQuantity,
                                    onValueChange = { editQuantity = it },
                                    label = { Text("مقدار", fontSize = 12.sp) },
                                    placeholder = { Text("مثلاً ۲ کیلو", fontSize = 11.5.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("inline_edit_qty_${item.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceWhite,
                                        unfocusedContainerColor = SurfaceWhite,
                                        focusedBorderColor = BentoIndigoAccent,
                                        unfocusedBorderColor = BentoBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = editUnitPriceText,
                                    onValueChange = { editUnitPriceText = it },
                                    label = { Text("قیمت فی", fontSize = 12.sp) },
                                    suffix = { Text(currencySymbol, fontSize = 11.sp, color = TextSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f).testTag("inline_edit_price_${item.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceWhite,
                                        unfocusedContainerColor = SurfaceWhite,
                                        focusedBorderColor = BentoIndigoAccent,
                                        unfocusedBorderColor = BentoBorder
                                    )
                                )
                            }

                            // Live calculated total preview & action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (previewTotal > 0.0) {
                                    Text(
                                        text = "مجموع: $currencySymbol ${formatPrice(previewTotal)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoIndigoAccent
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onDelete,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                                        modifier = Modifier.height(34.dp).testTag("inline_btn_delete_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف قلم", modifier = Modifier.size(14.dp), tint = ExpenseRed)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("حذف", fontSize = 11.5.sp, color = ExpenseRed)
                                    }

                                    OutlinedButton(
                                        onClick = onToggleExpand,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("انصراف", fontSize = 11.5.sp, color = TextSecondary)
                                    }

                                    Button(
                                        onClick = {
                                            if (editTitle.isNotBlank()) {
                                                onSaveInlineEdit(editTitle.trim(), editQuantity.trim(), previewUnitPrice, previewTotal)
                                                onToggleExpand()
                                            }
                                        },
                                        enabled = editTitle.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("inline_btn_save_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ذخیره", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ShoppingListHeaderDialog(
    initialList: ShoppingListEntity?,
    activeCurrencies: List<CurrencyEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ShoppingListEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialList?.title ?: "") }
    var currencyId by remember {
        mutableStateOf(initialList?.currencyId ?: activeCurrencies.firstOrNull { it.isBaseCurrency }?.id ?: 1L)
    }
    var currencyCode by remember { mutableStateOf(initialList?.currencyCode ?: activeCurrencies.firstOrNull { it.isBaseCurrency }?.code ?: "AFN") }
    var currencySymbol by remember { mutableStateOf(initialList?.currencySymbol ?: activeCurrencies.firstOrNull { it.isBaseCurrency }?.symbol ?: "؋") }
    val expenseCategories = remember(categories) {
        categories.filter { it.type == TransactionType.EXPENSE && it.isActive }
    }

    var categoryId by remember {
        mutableStateOf<Long?>(initialList?.categoryId ?: expenseCategories.firstOrNull()?.id)
    }
    var category by remember {
        mutableStateOf(
            initialList?.category?.ifBlank { null }
                ?: expenseCategories.firstOrNull()?.name
                ?: "خرید روزمره"
        )
    }
    var note by remember { mutableStateOf(initialList?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BentoLavenderSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (initialList == null) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = BentoIndigoAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = if (initialList == null) "ایجاد لیست خرید جدید" else "ویرایش مشخصات لیست",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BentoNavyDark
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام / عنوان لیست") },
                    placeholder = { Text("مثلاً: خرید سوپرمارکت، خرید عید...") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_shopping_list_title"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Currency selector
                Text("واحد پولی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeCurrencies.take(4).forEach { curr ->
                        val isSelected = (currencyId > 0 && curr.id == currencyId) || currencyCode.equals(curr.code, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BentoNavyDark else BackgroundCanvas,
                            border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    currencyId = curr.id
                                    currencyCode = curr.code
                                    currencySymbol = curr.symbol
                                }
                        ) {
                            Text(
                                text = "${curr.flagEmoji} ${curr.symbol}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else BentoNavyDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // Category selection from available categories
                Text("دسته‌بندی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(expenseCategories, key = { it.id }) { cat ->
                        val isSelected = categoryId == cat.id || (categoryId == null && cat.name.equals(category, ignoreCase = true))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BentoNavyDark else BackgroundCanvas,
                            border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    categoryId = cat.id
                                    category = cat.name
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = cat.name,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("یادداشت اختیاری") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        Toast.makeText(context, "ذخیره شد", Toast.LENGTH_SHORT).show()
                        val base = initialList ?: ShoppingListEntity(title = title.trim())
                        onSave(
                            base.copy(
                                title = title.trim(),
                                currencyId = currencyId,
                                currencyCode = currencyCode,
                                currencySymbol = currencySymbol,
                                category = category.trim().ifBlank { "مصارف عمومی" },
                                categoryId = categoryId,
                                note = note.trim().ifBlank { null }
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent)
            ) {
                Text(if (initialList == null) "ایجاد لیست" else "ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun LogShoppingListExpenseDialog(
    listWithItems: ShoppingListWithItems,
    accounts: List<AccountCardEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long?, category: String, timestamp: Long, customNote: String?) -> Unit
) {
    val list = listWithItems.list
    val effectiveTotal = listWithItems.effectiveTotal

    val expenseCategories = remember(categories) {
        categories.filter { it.type == TransactionType.EXPENSE && it.isActive }
    }
    val availableCategoryNames = remember(expenseCategories) {
        val names = expenseCategories.map { it.name }.filter { it.isNotBlank() }.distinct()
        if (names.isNotEmpty()) names else listOf("خرید روزمره", "خوراک و غذا", "خرید و پوشاک", "مصارف عمومی", "کرایه و خانه", "ترانسپورت و سفر", "صحت و درمان")
    }

    var selectedAccountId by remember { mutableStateOf<Long?>(null) } // null = بیلانس کل / نقد
    var selectedCategory by remember {
        mutableStateOf(
            list.category.takeIf { it in availableCategoryNames }
                ?: availableCategoryNames.firstOrNull()
                ?: "خرید روزمره"
        )
    }
    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var customNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoLavenderSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = BentoIndigoAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("ثبت لیست خرید به عنوان مصرف", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoNavyDark)
                    Text("لیست «${list.title}»", fontSize = 12.sp, color = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoLavenderSubtle,
                    border = BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مبلغ قابل کسر (مجموع لیست):", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "${list.currencySymbol} ${formatPrice(effectiveTotal)}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ExpenseRed
                        )
                    }
                }

                // Date Picker Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundCanvas)
                        .border(1.dp, BentoBorder, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BentoIndigoAccent, modifier = Modifier.size(16.dp))
                        Text("تاریخ ثبت مصرف:", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text(
                        text = PersianDateHelper.formatSolarDateTime(selectedTimestamp),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )
                }

                // Account Selection
                Text("پرداخت از طریق:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // General cash option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedAccountId == null) BentoLavenderSubtle else BackgroundCanvas)
                            .border(1.dp, if (selectedAccountId == null) BentoIndigoAccent else BentoBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedAccountId = null }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAccountId == null,
                            onClick = { selectedAccountId = null },
                            colors = RadioButtonDefaults.colors(selectedColor = BentoIndigoAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بیلانس کل (پول نقد)", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                    }

                    // Bank cards options
                    accounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedAccountId == acc.id) BentoLavenderSubtle else BackgroundCanvas)
                                .border(1.dp, if (selectedAccountId == acc.id) BentoIndigoAccent else BentoBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedAccountId = acc.id }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAccountId == acc.id,
                            onClick = { selectedAccountId = acc.id },
                            colors = RadioButtonDefaults.colors(selectedColor = BentoIndigoAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("کارت ${acc.name} (${acc.cardNumberMasked})", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = BentoNavyDark)
                    }
                }
            }

                // Category selection from available categories
                Text("دسته‌بندی:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoNavyDark)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCategoryNames) { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BentoNavyDark else BackgroundCanvas,
                            border = BorderStroke(1.dp, if (isSelected) BentoNavyDark else BentoBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = cat,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }
                }

                // Optional custom note
                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text("یادداشت (اختیاری)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedAccountId, selectedCategory, selectedTimestamp, customNote.ifBlank { null })
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                enabled = effectiveTotal > 0.0
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تایید و ثبت مصرف", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )

    if (showDatePicker) {
        SolarDatePickerDialog(
            initialTimestamp = selectedTimestamp,
            title = "انتخاب تاریخ مصرف",
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                selectedTimestamp = it
                showDatePicker = false
            }
        )
    }
}

private fun shareShoppingListText(context: Context, listWithItems: ShoppingListWithItems) {
    val list = listWithItems.list
    val items = listWithItems.items
    val effectiveTotal = listWithItems.effectiveTotal

    val sb = StringBuilder()
    sb.append("🛒 لیست خرید: ${list.title}\n")
    sb.append("📅 تاریخ: ${PersianDateHelper.formatSolarDate(list.purchaseDate)}\n\n")

    if (items.isEmpty()) {
        sb.append("هیچ قلمی ثبت نشده است.\n")
    } else {
        items.forEach { item ->
            val status = if (item.isPurchased) "✓" else "▫"
            val qty = if (item.quantity.isNotBlank()) " (${item.quantity})" else ""
            val price = if (item.price > 0.0) " - ${list.currencySymbol} ${formatPrice(item.price)}" else ""
            sb.append("$status ${item.title}$qty$price\n")
        }
    }

    sb.append("\n💰 جمله کل: ${list.currencySymbol} ${formatPrice(effectiveTotal)}\n")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "لیست خرید: ${list.title}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری لیست خرید"))
}
