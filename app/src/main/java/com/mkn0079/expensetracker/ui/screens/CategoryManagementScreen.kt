package com.mkn0079.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mkn0079.expensetracker.data.constants.categoryMap
import com.mkn0079.expensetracker.data.constants.paymentTypeMap
import com.mkn0079.expensetracker.data.constants.transactionList
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.Transaction
import com.mkn0079.expensetracker.models.UserProfile
import com.mkn0079.expensetracker.models.defaultUserProfile
import com.mkn0079.expensetracker.ui.models.CategoryIconOption
import com.mkn0079.expensetracker.ui.models.CategoryManagementItemUi
import com.mkn0079.expensetracker.ui.models.CategoryManagementTab
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurpleGlow
import com.mkn0079.expensetracker.ui.theme.PurplePrimary
import com.mkn0079.expensetracker.ui.components.AppHeader
import com.mkn0079.expensetracker.ui.viewmodels.CategoryManagementViewModel

private val categoryFallbackDescriptions = mapOf(
    1 to "Meals & dining",
    2 to "Fuel & commute",
    3 to "Retail & essentials",
    4 to "Monthly recurring",
    5 to "Wellness & care",
    6 to "Streaming & leisure",
    7 to "Home & stay",
    8 to "Daily essentials",
    9 to "Courses & books",
    10 to "Bills & renewals",
    11 to "Coverage & safety",
    12 to "Celebrations & giving",
    13 to "Self care routine",
    14 to "Fuel & commute",
    15 to "Repairs & upkeep",
    16 to "Mandatory dues",
    17 to "Pet care expenses",
    18 to "Kids & school",
    19 to "Charity & giving",
    20 to "Flexible spending",
    101 to "Salary & payroll",
    102 to "Business earnings",
    103 to "Returns & gains",
    104 to "Projects & gigs",
    105 to "Other credits"
)

private val paymentFallbackDescriptions = mapOf(
    1 to "UPI transfers & scans",
    2 to "Cash in hand",
    3 to "Bank transfers",
    4 to "Debit & credit cards",
    5 to "Flexible payment mode"
)

private val categoryIconOptions = listOf(
    CategoryIconOption("shopping_cart", "Shopping Cart", Icons.Filled.ShoppingCart),
    CategoryIconOption("restaurant", "Restaurant", Icons.Filled.Restaurant),
    CategoryIconOption("home", "Home", Icons.Filled.Home),
    CategoryIconOption("directions_bus", "Bus", Icons.Filled.DirectionsBus),
    CategoryIconOption("directions_car", "Car", Icons.Filled.DirectionsCar),
    CategoryIconOption("flight", "Flight", Icons.Filled.Flight),
    CategoryIconOption("local_cafe", "Cafe", Icons.Filled.LocalCafe),
    CategoryIconOption("pets", "Pets", Icons.Filled.Pets),
    CategoryIconOption("school", "School", Icons.Filled.School),
    CategoryIconOption("fitness_center", "Fitness", Icons.Filled.FitnessCenter),
    CategoryIconOption("spa", "Spa", Icons.Filled.Spa),
    CategoryIconOption("movie", "Movie", Icons.Filled.Movie),
    CategoryIconOption("music_note", "Music", Icons.Filled.MusicNote),
    CategoryIconOption("sports_esports", "Gaming", Icons.Filled.SportsEsports),
    CategoryIconOption("favorite", "Health", Icons.Filled.Favorite),
    CategoryIconOption("work", "Work", Icons.Filled.Work),
    CategoryIconOption("business", "Business", Icons.Filled.Business),
    CategoryIconOption("laptop_mac", "Laptop", Icons.Filled.LaptopMac),
    CategoryIconOption("phone_android", "Phone", Icons.Filled.PhoneAndroid),
    CategoryIconOption("camera_alt", "Camera", Icons.Filled.CameraAlt),
    CategoryIconOption("celebration", "Party", Icons.Filled.Celebration),
    CategoryIconOption("card_giftcard", "Gift", Icons.Filled.CardGiftcard),
    CategoryIconOption("child_care", "Childcare", Icons.Filled.ChildCare),
    CategoryIconOption("volunteer_activism", "Charity", Icons.Filled.VolunteerActivism),
    CategoryIconOption("local_hospital", "Hospital", Icons.Filled.LocalHospital),
    CategoryIconOption("medication", "Medicine", Icons.Filled.Medication),
    CategoryIconOption("two_wheeler", "Bike", Icons.Filled.TwoWheeler),
    CategoryIconOption("train", "Train", Icons.Filled.Train),
    CategoryIconOption("hotel", "Hotel", Icons.Filled.Hotel),
    CategoryIconOption("beach_access", "Beach", Icons.Filled.BeachAccess),
    CategoryIconOption("park", "Park", Icons.Filled.Park),
    CategoryIconOption("hiking", "Hiking", Icons.Filled.Hiking),
    CategoryIconOption("sports_soccer", "Soccer", Icons.Filled.SportsSoccer),
    CategoryIconOption("sports_basketball", "Basketball", Icons.Filled.SportsBasketball),
    CategoryIconOption("pool", "Pool", Icons.Filled.Pool),
    CategoryIconOption("directions_boat", "Boat", Icons.Filled.DirectionsBoat),
    CategoryIconOption("build", "Build", Icons.Filled.Build),
    CategoryIconOption("subscriptions", "Subscription", Icons.Filled.Subscriptions),
    CategoryIconOption("receipt_long", "Bills", Icons.AutoMirrored.Filled.ReceiptLong),
    CategoryIconOption("account_balance", "Bank", Icons.Filled.AccountBalance),
    CategoryIconOption("credit_card", "Card", Icons.Filled.CreditCard),
    CategoryIconOption("payments", "Cash", Icons.Filled.Payments),
    CategoryIconOption("qr_code", "QR", Icons.Filled.QrCode),
    CategoryIconOption("savings", "Savings", Icons.Filled.Savings),
    CategoryIconOption("attach_money", "Money", Icons.Filled.AttachMoney),
    CategoryIconOption("wallet", "Wallet", Icons.Filled.AccountBalanceWallet),
    CategoryIconOption("currency_exchange", "Exchange", Icons.Filled.CurrencyExchange),
    CategoryIconOption("storefront", "Store", Icons.Filled.Storefront),
    CategoryIconOption("fastfood", "Fast Food", Icons.Filled.Fastfood),
    CategoryIconOption("cake", "Cake", Icons.Filled.Cake)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    userProfile: UserProfile = defaultUserProfile,
    transactions: List<Transaction> = transactionList,
    customCategories: List<CategoryType> = emptyList(),
    customPaymentTypes: List<PaymentType> = emptyList(),
    onBackClick: () -> Unit = {},
    onCreateCustomCategory: (String, String, Int) -> Unit = { _, _, _ -> },
    onCreateCustomPaymentType: (String, String) -> Unit = { _, _ -> },
    onDeleteCustomCategory: (Int) -> Unit = {},
    onDeleteCustomPaymentType: (Int) -> Unit = {},
    onAddCategoryClick: () -> Unit = {}
) {
    val categoryManagementViewModel: CategoryManagementViewModel = viewModel()
    val addCategorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isAddSheetVisible by rememberSaveable { mutableStateOf(false) }
    var createTargetTabName by rememberSaveable { mutableStateOf(CategoryManagementTab.Expense.name) }
    var newCategoryName by rememberSaveable { mutableStateOf("") }
    var selectedIconId by rememberSaveable { mutableStateOf(defaultIconIdFor(CategoryManagementTab.Expense)) }
    androidx.compose.runtime.LaunchedEffect(customCategories, customPaymentTypes) {
        categoryManagementViewModel.updateInputs(
            customCategories = customCategories,
            customPaymentTypes = customPaymentTypes
        )
    }
    val uiState by categoryManagementViewModel.uiState.collectAsStateWithLifecycle()
    val activeTab = uiState.selectedTab
    val createTargetTab = remember(createTargetTabName) { CategoryManagementTab.fromName(createTargetTabName) }
    val selectedIconOption = remember(selectedIconId) {
        categoryIconOptions.firstOrNull { it.id == selectedIconId } ?: categoryIconOptions.first()
    }
    val existingNamesForTarget = remember(createTargetTab, customCategories, customPaymentTypes) {
        when (createTargetTab) {
            CategoryManagementTab.Income -> {
                (categoryMap.values + customCategories)
                    .filter { it.transactionTypeId == 1 }
                    .map { it.name }
            }

            CategoryManagementTab.Expense -> {
                (categoryMap.values + customCategories)
                    .filter { it.transactionTypeId == 2 }
                    .map { it.name }
            }

            CategoryManagementTab.Payment -> {
                (paymentTypeMap.values + customPaymentTypes).map { it.name }
            }
        }
    }
    val trimmedCategoryName = newCategoryName.trim()
    val isDuplicateName = existingNamesForTarget.any { it.equals(trimmedCategoryName, ignoreCase = true) }
    val canCreateCategory = trimmedCategoryName.isNotBlank() && !isDuplicateName

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF09090A),
                        BackgroundDark
                    )
                )
            )
    ) {
        CategoryManagementGlow()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            AppHeader(
                title = "Manage Category",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            CategoryTabSwitcher(
                selectedTab = activeTab,
                onTabSelected = categoryManagementViewModel::selectTab
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = uiState.categoryCountLabel,
                color = Color(0xFF9F98AE),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(
                    items = uiState.items,
                    key = { item -> item.id },
                    contentType = { "category_management_item" }
                ) { item ->
                    CategoryManagementCard(
                        item = item,
                        onDeleteClick = {
                            when (activeTab) {
                                CategoryManagementTab.Income,
                                CategoryManagementTab.Expense -> {
                                    onDeleteCustomCategory(item.id)
                                }

                                CategoryManagementTab.Payment -> {
                                    onDeleteCustomPaymentType(item.id)
                                }
                            }
                        }
                    )
                }
            }
        }

        AddCategoryFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 22.dp, bottom = 28.dp),
            onClick = {
                createTargetTabName = activeTab.name
                newCategoryName = ""
                selectedIconId = defaultIconIdFor(activeTab)
                isAddSheetVisible = true
            }
        )
    }

    if (isAddSheetVisible) {
        AddCategoryBottomSheet(
            sheetState = addCategorySheetState,
            targetTab = createTargetTab,
            categoryName = newCategoryName,
            selectedIcon = selectedIconOption,
            isDuplicateName = isDuplicateName,
            canCreate = canCreateCategory,
            onCategoryNameChange = { newCategoryName = it },
            onIconSelected = { selectedIconId = it },
            onCreateClick = {
                when (createTargetTab) {
                    CategoryManagementTab.Income -> {
                        onCreateCustomCategory(
                            trimmedCategoryName,
                            selectedIconOption.id,
                            1
                        )
                    }

                    CategoryManagementTab.Expense -> {
                        onCreateCustomCategory(
                            trimmedCategoryName,
                            selectedIconOption.id,
                            2
                        )
                    }

                    CategoryManagementTab.Payment -> {
                        onCreateCustomPaymentType(
                            trimmedCategoryName,
                            selectedIconOption.id
                        )
                    }
                }
                onAddCategoryClick()
                isAddSheetVisible = false
            },
            onDismiss = { isAddSheetVisible = false }
        )
    }
}

@Composable
private fun BoxScope.CategoryManagementGlow() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 92.dp)
            .size(width = 260.dp, height = 190.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PurplePrimary.copy(alpha = 0.14f),
                        PurpleGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryBottomSheet(
    sheetState: SheetState,
    targetTab: CategoryManagementTab,
    categoryName: String,
    selectedIcon: CategoryIconOption,
    isDuplicateName: Boolean,
    canCreate: Boolean,
    onCategoryNameChange: (String) -> Unit,
    onIconSelected: (String) -> Unit,
    onCreateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0E0D12),
        scrimColor = Color.Black.copy(alpha = 0.72f),
        dragHandle = null
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
        ) {
            val gridHeight = if (maxHeight < 760.dp) 250.dp else 320.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .size(width = 56.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Add Category",
                    color = Color(0xFFF5F0FA),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 30.sp,
                        lineHeight = 34.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PERSONALIZE YOUR VAULT",
                    color = Color(0xFFB5AFC0),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 3.sp,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                CategorySheetLabel(text = "CATEGORY TYPE")

                Spacer(modifier = Modifier.height(12.dp))

                TypePreviewChip(targetTab = targetTab)

                Spacer(modifier = Modifier.height(28.dp))

                CategorySheetLabel(text = "CATEGORY NAME")

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = categoryName,
                    onValueChange = onCategoryNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = when (targetTab) {
                                CategoryManagementTab.Income -> "e.g. Performance Bonus"
                                CategoryManagementTab.Expense -> "e.g. Weekend Escapes"
                                CategoryManagementTab.Payment -> "e.g. Digital Wallet"
                            },
                            color = Color(0xFF5C5768)
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF8D63FF), PurplePrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedIcon.icon,
                                contentDescription = selectedIcon.label,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = PurplePrimary,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.12f),
                        cursorColor = PurpleAccent,
                        focusedTextColor = Color(0xFFF2EDF8),
                        unfocusedTextColor = Color(0xFFF2EDF8),
                        focusedLeadingIconColor = Color.White,
                        unfocusedLeadingIconColor = Color.White
                    )
                )

                if (isDuplicateName) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This name already exists in ${targetTab.title.lowercase()}.",
                        color = Color(0xFFFFAAA0),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                CategorySheetLabel(text = "SELECT VISUAL IDENTITY")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose from 50 icons. Color styling stays automatic.",
                    color = Color(0xFF8E879B),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = gridHeight),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(categoryIconOptions, key = { it.id }) { option ->
                        IconSelectionItem(
                            option = option,
                            selected = option.id == selectedIcon.id,
                            onClick = { onIconSelected(option.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onCreateClick,
                    enabled = canCreate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        contentColor = Color(0xFF25124E),
                        disabledContentColor = Color(0xFF8A8396)
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (canCreate) {
                                        listOf(Color(0xFF7D53FF), Color(0xFFC6B6FF))
                                    } else {
                                        listOf(Color(0xFF2A2734), Color(0xFF35313F))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (targetTab == CategoryManagementTab.Payment) {
                                "Create Payment Type"
                            } else {
                                "Create Category"
                            },
                            color = if (canCreate) Color(0xFF24114C) else Color(0xFF8A8396),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "CANCEL",
                        color = Color(0xFFD0CADB),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CategorySheetLabel(text: String) {
}

@Composable
private fun TypePreviewChip(targetTab: CategoryManagementTab) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF17151F))
            .border(
                width = 1.dp,
                color = PurplePrimary.copy(alpha = 0.22f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF8D63FF), PurplePrimary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (targetTab) {
                    CategoryManagementTab.Income -> Icons.AutoMirrored.Filled.TrendingUp
                    CategoryManagementTab.Expense -> Icons.Filled.Category
                    CategoryManagementTab.Payment -> Icons.Filled.Payments
                },
                contentDescription = targetTab.title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Column {
            Text(
                text = if (targetTab == CategoryManagementTab.Payment) {
                    "Payment Type"
                } else {
                    "${targetTab.title} Category"
                },
                color = Color(0xFFF3EEF8),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "This new item will be added under ${targetTab.title}.",
                color = Color(0xFF9F98AE),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}


@Composable
private fun CategoryTabSwitcher(
    selectedTab: CategoryManagementTab,
    onTabSelected: (CategoryManagementTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF121214))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryManagementTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .shadow(
                        elevation = if (isSelected) 18.dp else 0.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = if (isSelected) PurplePrimary.copy(alpha = 0.28f) else Color.Transparent,
                        spotColor = if (isSelected) PurpleGlow.copy(alpha = 0.22f) else Color.Transparent
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = if (isSelected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    PurplePrimary,
                                    Color(0xFF8A5EFF)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent
                                )
                            )
                        }
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) Color(0xFFF9F4FF) else Color(0xFFC9C2D4),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryManagementCard(
    item: CategoryManagementItemUi,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(34.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF19191B),
                        Color(0xFF202022)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(34.dp)
            )
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF2B2A31)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = PurpleAccent,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                color = Color(0xFFF3EDF9),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.subtitle,
                color = Color(0xFFABA4B6),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }

        if (item.isUserCreated) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A1A22))
                    .clickable(onClick = onDeleteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete ${item.title}",
                    tint = Color(0xFFFFA8B0),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun IconSelectionItem(
    option: CategoryIconOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .shadow(
                elevation = if (selected) 18.dp else 0.dp,
                shape = CircleShape,
                ambientColor = PurplePrimary.copy(alpha = 0.34f),
                spotColor = PurpleGlow.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                brush = if (selected) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF7B4DFF), Color(0xFF9E7CFF))
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF242226), Color(0xFF1D1B20))
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = option.label,
            tint = if (selected) Color(0xFFF5F1FF) else Color(0xFFD2CAE0),
            modifier = Modifier.size(30.dp)
        )
    }
}

private fun defaultIconIdFor(tab: CategoryManagementTab): String {
    return when (tab) {
        CategoryManagementTab.Income -> "wallet"
        CategoryManagementTab.Expense -> "shopping_cart"
        CategoryManagementTab.Payment -> "payments"
    }
}

private fun buildCategoryManagementItems(
    categories: List<CategoryType>,
    transactionTypeId: Int,
    fallbackSubtitle: String
): List<CategoryManagementItemUi> {
    val customItems = categories
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedByDescending { it.id }
    val builtinItems = categoryMap.values
        .filter { it.transactionTypeId == transactionTypeId }
        .sortedBy { it.id }

    return (customItems + builtinItems).map { category ->
        CategoryManagementItemUi(
            id = category.id,
            title = category.name,
            subtitle = categoryFallbackDescriptions[category.id] ?: fallbackSubtitle,
            icon = category.icon,
            isUserCreated = category.id !in categoryMap
        )
    }
}

@Composable
private fun AddCategoryFab(
        modifier: Modifier = Modifier,
        onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PurplePrimary.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(66.dp)
                .shadow(
                    elevation = 22.dp,
                    shape = CircleShape,
                    ambientColor = PurplePrimary.copy(alpha = 0.34f),
                    spotColor = PurpleGlow.copy(alpha = 0.30f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8D63FF),
                            PurplePrimary
                        )
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add category",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun CategoryManagementScreenPreview() {
    ExpenseTrackerTheme(darkTheme = true) {
        CategoryManagementScreen()
    }
}
