@file:OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)

package com.vesti.app.ui.wardrobe

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.material.icons.filled.Label
import com.vesti.app.AppConfig
import com.vesti.app.data.network.WardrobeItemDto
import com.vesti.app.ui.theme.VestiColors

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isUploading by viewModel.uploading.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.loadItems()
    }
    
    // UI filters and selectors state
    var selectedCategoryFilter by remember { mutableStateOf("Hepsi") }
    var selectedSubCategoryFilter by remember { mutableStateOf("Hepsi") }
    var inputSubCategory by remember { mutableStateOf("Standart") }
    
    LaunchedEffect(selectedCategoryFilter) {
        selectedSubCategoryFilter = "Hepsi"
    }
    
    val categories = listOf("Hepsi", "Tişört", "Gömlek", "Pantolon", "Ceket", "Ayakkabı", "Elbise", "Aksesuar", "Etek")
    
    var showSourceSelectorDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<WardrobeItemDto?>(null) }
    
    // Personalization Input state
    var inputCategory by remember { mutableStateOf("Tişört") }
    var inputColor by remember { mutableStateOf("Siyah") }
    var inputSize by remember { mutableStateOf("M") }
    var inputBrand by remember { mutableStateOf("") }
    var isCustomColorSelected by remember { mutableStateOf(false) }
    var isCustomCategorySelected by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            showDetailsDialog = true
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            currentPhotoUri = uri
            showDetailsDialog = true
        }
    }
    
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var shouldLaunchCameraAfterPermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted && shouldLaunchCameraAfterPermission) {
            shouldLaunchCameraAfterPermission = false
            val (file, uri) = CameraHelper.createTempImageFile(context)
            currentPhotoUri = uri
            cameraLauncher.launch(uri)
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSourceSelectorDialog = true },
                containerColor = VestiColors.Primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = AppConfig.t("Yeni Kıyafet Ekle", "Add New Clothes"),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(VestiColors.Background)
                .padding(paddingValues)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = AppConfig.t("Dijital Gardırobun", "Digital Wardrobe"),
                    color = VestiColors.TextMain,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Category Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategoryFilter),
                containerColor = Color.Transparent,
                edgePadding = 24.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategoryFilter)]),
                        color = VestiColors.Primary,
                        height = 2.dp
                    )
                },
                divider = { Divider(color = Color(0xFFF1F1F1)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    val catDisplay = AppConfig.t(cat, when(cat) {
                        "Hepsi" -> "All"
                        "Tişört" -> "T-Shirt"
                        "Gömlek" -> "Shirt"
                        "Pantolon" -> "Pants"
                        "Ceket" -> "Jacket"
                        "Ayakkabı" -> "Shoes"
                        "Elbise" -> "Dress"
                        "Aksesuar" -> "Accessory"
                        "Etek" -> "Skirt"
                        else -> cat
                    })
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        text = {
                            Text(
                                text = catDisplay,
                                color = if (isSelected) VestiColors.Primary else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    )
                }
            }
            
            // Core List / Grid Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (val currentState = state) {
                    is WardrobeState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            HangerLoadingAnimation()
                        }
                    }
                    is WardrobeState.Error -> {
                        Text(
                            text = currentState.message, 
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is WardrobeState.Success -> {
                        if (currentState.items.isEmpty()) {
                            // High-end elegant empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(VestiColors.LightPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = VestiColors.Primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = AppConfig.t("Gardırobunuz Henüz Boş", "Your Wardrobe is Empty"),
                                    color = VestiColors.TextMain,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = AppConfig.t(
                                        "Yeni parçalar ekleyerek dijital gardırobunuzu oluşturun.",
                                        "Create your digital wardrobe by adding new items."
                                    ),
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            if (selectedCategoryFilter == "Hepsi") {
                                // 1. LANDING COLLECTION GRID (Boutique Grid of curated categories)
                                val mainCategories = listOf(
                                    "Tişört",
                                    "Gömlek",
                                    "Kazak",
                                    "Ceket",
                                    "Pantolon",
                                    "Takım",
                                    "Aksesuar",
                                    "Ayakkabı",
                                    "Elbise",
                                    "Etek"
                                )
                                
                                val activeMainCategories = mainCategories.filter { mainCat ->
                                    getItemsForMainCategory(currentState.items, mainCat).isNotEmpty()
                                }
                                
                                val unmatchedCategories = currentState.items.map { it.category }
                                    .distinctBy { it.lowercase() }
                                    .filter { cat ->
                                        mainCategories.none { mainCat -> 
                                            val itemMatches = when (mainCat.lowercase()) {
                                                "tişört" -> cat.lowercase().contains("tişört") || cat.lowercase().contains("tshirt") || cat.lowercase().contains("t-shirt")
                                                "pantolon" -> cat.lowercase().contains("pantolon") || cat.lowercase().contains("jean")
                                                "ceket" -> cat.lowercase().contains("ceket") || cat.lowercase().contains("mont") || cat.lowercase().contains("kaban")
                                                "ayakkabı" -> cat.lowercase().contains("ayakkabı") || cat.lowercase().contains("sneaker") || cat.lowercase().contains("bot")
                                                "takım" -> cat.lowercase().contains("takım")
                                                "aksesuar" -> cat.lowercase().contains("aksesuar") || cat.lowercase().contains("gözlük") || cat.lowercase().contains("şapka") || cat.lowercase().contains("bere") || cat.lowercase().contains("saat") || cat.lowercase().contains("kemer")
                                                else -> cat.lowercase().contains(mainCat.lowercase())
                                            }
                                            itemMatches
                                        }
                                    }
                                
                                val activeCategoriesForGrid = activeMainCategories + unmatchedCategories
                                
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(bottom = 80.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(activeCategoriesForGrid) { cat ->
                                        val img = getStockImageForCategory(cat)
                                        val count = if (mainCategories.contains(cat)) {
                                            getItemsForMainCategory(currentState.items, cat).size
                                        } else {
                                            currentState.items.count { it.category.equals(cat, ignoreCase = true) }
                                        }
                                        CategoryCollectionCard(
                                            categoryName = cat,
                                            imageUrl = img,
                                            itemCount = count,
                                            onClick = { 
                                                selectedCategoryFilter = cat
                                                selectedSubCategoryFilter = "Hepsi"
                                            }
                                        )
                                    }
                                }
                            } else {
                                // 2. FILTERED INDIVIDUAL CLOTHING ITEMS VIEW
                                val mainCategories = listOf(
                                    "Tişört",
                                    "Gömlek",
                                    "Kazak",
                                    "Ceket",
                                    "Pantolon",
                                    "Takım",
                                    "Aksesuar",
                                    "Ayakkabı",
                                    "Elbise",
                                    "Etek"
                                )
                                
                                val subCategories = when (selectedCategoryFilter) {
                                    "Pantolon" -> listOf("Hepsi", "Kot", "Kumaş", "Keten", "Deri")
                                    "Ceket" -> listOf("Hepsi", "Deri", "Kot", "Kumaş", "Kaban")
                                    "Gömlek" -> listOf("Hepsi", "Keten", "Kot", "Klasik")
                                    "Tişört" -> listOf("Hepsi", "Basic", "Oversize", "Polo")
                                    "Takım" -> listOf("Hepsi", "Eşofman", "Blazer", "Takım Elbise")
                                    "Aksesuar" -> listOf("Hepsi", "Gözlük", "Şapka", "Bere", "Saat", "Kemer")
                                    "Ayakkabı" -> listOf("Hepsi", "Sneaker", "Klasik", "Bot")
                                    else -> emptyList()
                                }
                                
                                val baseItems = if (mainCategories.contains(selectedCategoryFilter)) {
                                    getItemsForMainCategory(currentState.items, selectedCategoryFilter)
                                } else {
                                    currentState.items.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
                                }
                                val filteredItems = baseItems.filter { isItemInSubCategory(it, selectedCategoryFilter, selectedSubCategoryFilter) }
                                
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Gorgeous Back Navigation Button Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { selectedCategoryFilter = "Hepsi" },
                                            colors = ButtonDefaults.textButtonColors(contentColor = VestiColors.Primary)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = AppConfig.t("Geri Dön", "Go Back"),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = AppConfig.t("Tüm Koleksiyonlar", "All Collections"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
  
                                    // Sub-category / Material selection horizontal chips
                                    if (subCategories.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        ) {
                                            items(subCategories) { sub ->
                                                val isSelected = selectedSubCategoryFilter == sub
                                                val displaySub = AppConfig.t(sub, when (sub) {
                                                    "Hepsi" -> "All"
                                                    "Kot" -> "Denim"
                                                    "Kumaş" -> "Fabric"
                                                    "Keten" -> "Linen"
                                                    "Deri" -> "Leather"
                                                    "Kaban" -> "Coat"
                                                    "Klasik" -> "Classic"
                                                    "Basic" -> "Basic"
                                                    "Oversize" -> "Oversize"
                                                    "Polo" -> "Polo"
                                                    "Eşofman" -> "Tracksuit"
                                                    "Blazer" -> "Blazer"
                                                    "Takım Elbise" -> "Suit"
                                                    "Gözlük" -> "Glasses"
                                                    "Şapka" -> "Hat"
                                                    "Bere" -> "Beanie"
                                                    "Saat" -> "Watch"
                                                    "Kemer" -> "Belt"
                                                    "Sneaker" -> "Sneaker"
                                                    "Bot" -> "Boots"
                                                    else -> sub
                                                })
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(if (isSelected) VestiColors.Primary else Color(0xFFF7F7F7))
                                                        .clickable { selectedSubCategoryFilter = sub }
                                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = displaySub,
                                                        color = if (isSelected) Color.White else VestiColors.TextMain,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (filteredItems.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val categoryDisp = AppConfig.t(selectedCategoryFilter, when(selectedCategoryFilter) {
                                                "Tişört" -> "T-Shirt"
                                                "Gömlek" -> "Shirt"
                                                "Pantolon" -> "Pants"
                                                "Ceket" -> "Jacket"
                                                "Ayakkabı" -> "Shoes"
                                                "Elbise" -> "Dress"
                                                "Aksesuar" -> "Accessory"
                                                "Etek" -> "Skirt"
                                                else -> selectedCategoryFilter
                                            })
                                            val subDisp = AppConfig.t(selectedSubCategoryFilter, when(selectedSubCategoryFilter) {
                                                "Kot" -> "Denim"
                                                "Kumaş" -> "Fabric"
                                                "Keten" -> "Linen"
                                                "Deri" -> "Leather"
                                                "Kaban" -> "Coat"
                                                "Klasik" -> "Classic"
                                                "Basic" -> "Basic"
                                                "Oversize" -> "Oversize"
                                                "Polo" -> "Polo"
                                                "Eşofman" -> "Tracksuit"
                                                "Blazer" -> "Blazer"
                                                "Takım Elbise" -> "Suit"
                                                "Gözlük" -> "Glasses"
                                                "Şapka" -> "Hat"
                                                "Bere" -> "Beanie"
                                                "Saat" -> "Watch"
                                                "Kemer" -> "Belt"
                                                "Sneaker" -> "Sneaker"
                                                "Bot" -> "Boots"
                                                else -> selectedSubCategoryFilter
                                            })
                                            Text(
                                                text = if (selectedSubCategoryFilter == "Hepsi") {
                                                    AppConfig.t(
                                                        "$categoryDisp kategorisinde ürün yok.",
                                                        "No items in the $categoryDisp category."
                                                    )
                                                } else {
                                                    AppConfig.t(
                                                        "$categoryDisp kategorisinde $subDisp ürünü bulunmuyor.",
                                                        "No $subDisp items found in the $categoryDisp category."
                                                    )
                                                },
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }
                                    } else {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(2),
                                            contentPadding = PaddingValues(bottom = 80.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(filteredItems) { item ->
                                                WardrobeItemCard(
                                                    item = item,
                                                    onDeleteClick = { itemToDelete = item }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable(enabled = false) {}, // Block clicks
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            modifier = Modifier.fillMaxWidth(0.82f)
                        ) {
                            HangerLoadingAnimation()
                        }
                    }
                }
            }
        }
    }

    // Luxury Source Selector Bottom Sheet (Dialog + Box Wrapper)
    if (showSourceSelectorDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSourceSelectorDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { showSourceSelectorDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                        .clickable(enabled = false) {} // Prevent click-through closing
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // 1. Drag Handle
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 2. Header
                    Text(
                        text = AppConfig.t("Görsel Yükle", "Upload Image"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = VestiColors.TextMain
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Option 1: Camera
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF7F7F7))
                            .clickable {
                                showSourceSelectorDialog = false
                                if (cameraPermissionState.status.isGranted) {
                                    val (file, uri) = CameraHelper.createTempImageFile(context)
                                    currentPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    shouldLaunchCameraAfterPermission = true
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(VestiColors.LightPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = VestiColors.Primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = AppConfig.t("Kamera ile Çek", "Take with Camera"),
                                fontWeight = FontWeight.Bold,
                                color = VestiColors.TextMain,
                                fontSize = 15.sp
                            )
                            Text(
                                text = AppConfig.t("Kamerayı kullanarak anlık fotoğrafla", "Take a photo using the camera"),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Gallery
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF7F7F7))
                            .clickable {
                                showSourceSelectorDialog = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF2E7D32))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = AppConfig.t("Galeriden Seç", "Select from Gallery"),
                                fontWeight = FontWeight.Bold,
                                color = VestiColors.TextMain,
                                fontSize = 15.sp
                            )
                            Text(
                                text = AppConfig.t("Mevcut görseller arasından tercih yap", "Choose from your existing gallery"),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Luxury Boutique Bottom Sheet Editor (Dialog + Box Wrapper)
    if (showDetailsDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {
                showDetailsDialog = false
                inputCategory = "Tişört"
                inputColor = "Siyah"
                inputSize = "M"
                inputBrand = ""
                isCustomColorSelected = false
                isCustomCategorySelected = false
                customCategoryText = ""
                inputSubCategory = "Standart"
            },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { 
                        showDetailsDialog = false
                        inputCategory = "Tişört"
                        inputColor = "Siyah"
                        inputSize = "M"
                        inputBrand = ""
                        isCustomColorSelected = false
                        isCustomCategorySelected = false
                        customCategoryText = ""
                        inputSubCategory = "Standart"
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.88f)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(Color.White)
                        .clickable(enabled = false) {} // Prevent clicking inside the editor from closing it
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // 1. Drag Handle
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 2. Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppConfig.t("Kıyafet Detayları", "Clothing Details"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = VestiColors.TextMain
                        )
                        IconButton(onClick = {
                            showDetailsDialog = false
                            inputCategory = "Tişört"
                            inputColor = "Siyah"
                            inputSize = "M"
                            inputBrand = ""
                            isCustomColorSelected = false
                            isCustomCategorySelected = false
                            customCategoryText = ""
                            inputSubCategory = "Standart"
                        }) {
                            Icon(Icons.Default.Close, contentDescription = AppConfig.t("Kapat", "Close"), tint = Color.Gray)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 3. Scrollable Configuration Content
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Selected Image Preview
                        if (currentPhotoUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFF7F7F7))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(currentPhotoUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Önizleme",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Kategori Seçimi (Kutusuz, Saf Minimalist ve Yumuşak Renkler)
                        Column {
                            Text(AppConfig.t("Kategori", "Category"), fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            val categoryOptions = listOf("Tişört", "Gömlek", "Pantolon", "Ceket", "Takım", "Aksesuar", "Ayakkabı", "Elbise", "Etek")
                            val allOptions = categoryOptions + "Özel"
                            
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                allOptions.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { cat ->
                                            val isSelected = if (cat == "Özel") isCustomCategorySelected else (!isCustomCategorySelected && inputCategory == cat)
                                            val catLabel = if (cat == "Özel") {
                                                AppConfig.t("+ Diğer", "+ Other")
                                            } else {
                                                AppConfig.t(cat, when(cat) {
                                                    "Tişört" -> "T-Shirt"
                                                    "Gömlek" -> "Shirt"
                                                    "Pantolon" -> "Pants"
                                                    "Ceket" -> "Jacket"
                                                    "Takım" -> "Suit"
                                                    "Aksesuar" -> "Accessory"
                                                    "Ayakkabı" -> "Shoes"
                                                    "Elbise" -> "Dress"
                                                    "Etek" -> "Skirt"
                                                    else -> cat
                                                })
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(if (isSelected) VestiColors.Primary else Color(0xFFF7F7F7))
                                                    .clickable { 
                                                        if (cat == "Özel") {
                                                            isCustomCategorySelected = true
                                                        } else {
                                                            isCustomCategorySelected = false
                                                            inputCategory = cat 
                                                        }
                                                    }
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = catLabel,
                                                    color = if (isSelected) Color.White else VestiColors.TextMain,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        if (rowItems.size < 3) {
                                            repeat(3 - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if (isCustomCategorySelected) {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = customCategoryText,
                                    onValueChange = { customCategoryText = it },
                                    label = { Text(AppConfig.t("Özel Kategori Adı", "Custom Category Name")) },
                                    placeholder = { Text(AppConfig.t("Örn: Şapka, Gözlük, Bere, Çorap", "e.g. Hat, Glasses, Beanie, Socks"), color = Color.LightGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VestiColors.Primary,
                                        unfocusedBorderColor = Color(0xFFE0E0E0)
                                    ),
                                    singleLine = true
                                )
                            }
                            
                            // Sub-category / Material Type selection row inside the editor modal
                            val availableSubTypes = when (inputCategory) {
                                "Pantolon" -> listOf("Standart", "Kot", "Kumaş", "Keten", "Deri")
                                "Ceket" -> listOf("Standart", "Deri", "Kot", "Kumaş", "Kaban")
                                "Gömlek" -> listOf("Standart", "Keten", "Kot", "Klasik")
                                "Tişört" -> listOf("Standart", "Basic", "Oversize", "Polo")
                                "Takım" -> listOf("Standart", "Eşofman", "Blazer", "Takım Elbise")
                                "Aksesuar" -> listOf("Standart", "Gözlük", "Şapka", "Bere", "Saat", "Kemer")
                                "Ayakkabı" -> listOf("Standart", "Sneaker", "Klasik", "Bot")
                                else -> emptyList()
                            }
                            
                            if (!isCustomCategorySelected && availableSubTypes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column {
                                    Text(AppConfig.t("Tür / Kumaş Seçimi", "Style / Fabric Selection"), fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(availableSubTypes) { sub ->
                                            val isSelected = inputSubCategory == sub
                                            val displaySub = AppConfig.t(sub, when (sub) {
                                                "Standart" -> "Standard"
                                                "Kot" -> "Denim"
                                                "Kumaş" -> "Fabric"
                                                "Keten" -> "Linen"
                                                "Deri" -> "Leather"
                                                "Kaban" -> "Coat"
                                                "Klasik" -> "Classic"
                                                "Basic" -> "Basic"
                                                "Oversize" -> "Oversize"
                                                "Polo" -> "Polo"
                                                "Eşofman" -> "Tracksuit"
                                                "Blazer" -> "Blazer"
                                                "Takım Elbise" -> "Suit"
                                                "Gözlük" -> "Glasses"
                                                "Şapka" -> "Hat"
                                                "Bere" -> "Beanie"
                                                "Saat" -> "Watch"
                                                "Kemer" -> "Belt"
                                                "Sneaker" -> "Sneaker"
                                                "Bot" -> "Boots"
                                                else -> sub
                                            })
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(if (isSelected) VestiColors.Primary else Color(0xFFF7F7F7))
                                                    .clickable { inputSubCategory = sub }
                                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = displaySub,
                                                    color = if (isSelected) Color.White else VestiColors.TextMain,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Beden Seçimi (Kutusuz, Yuvarlak Şık Kapsüller)
                        Column {
                            Text(AppConfig.t("Beden Seçimi", "Size Selection"), fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            val sizeOptions = listOf("XS", "S", "M", "L", "XL", "XXL", "Standart")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(sizeOptions) { sz ->
                                    val isSelected = inputSize == sz
                                    val szDisplay = if (sz == "Standart") AppConfig.t("Standart", "Standard") else sz
                                    Box(
                                        modifier = Modifier
                                            .size(width = 72.dp, height = 40.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSelected) VestiColors.Primary else Color(0xFFF7F7F7))
                                            .clickable { inputSize = sz }
                                            .wrapContentSize(Alignment.Center)
                                    ) {
                                        Text(
                                            text = szDisplay,
                                            color = if (isSelected) Color.White else VestiColors.TextMain,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Renk Kataloğu (Kutusuz, Canlı Önizleme)
                        Column {
                            Text(AppConfig.t("Renk Kataloğu", "Color Catalog"), fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val resolvedColor = resolveColorFromName(inputColor)
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (resolvedColor != null) Brush.linearGradient(listOf(resolvedColor, resolvedColor))
                                            else Brush.sweepGradient(listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta, Color.Red))
                                        )
                                        .border(2.dp, Color.White, CircleShape)
                                        .border(3.dp, Color(0xFFE0E0E0), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (resolvedColor == null && inputColor.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.85f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(AppConfig.t("Özel", "Custom"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = VestiColors.TextMain)
                                        }
                                    }
                                }
                                
                                OutlinedTextField(
                                    value = inputColor,
                                    onValueChange = { inputColor = it },
                                    label = { Text(AppConfig.t("Kıyafet Rengi", "Clothing Color")) },
                                    placeholder = { Text(AppConfig.t("Örn: Siyah, Beyaz, Mavi", "e.g. Black, White, Blue")) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VestiColors.Primary,
                                        unfocusedBorderColor = Color(0xFFE0E0E0)
                                    ),
                                    singleLine = true
                                )
                            }
                        }

                        // Marka Bilgisi (Kutusuz, Şık Giriş)
                        Column {
                            Text(AppConfig.t("Marka Bilgisi", "Brand Information"), fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = inputBrand,
                                onValueChange = { inputBrand = it },
                                placeholder = { Text(AppConfig.t("örn. Zara, H&M, Boutique", "e.g. Zara, H&M, Boutique"), color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = VestiColors.Primary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VestiColors.Primary,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                singleLine = true
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 4. Pinned Bottom Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                showDetailsDialog = false
                                val category = if (isCustomCategorySelected) {
                                    if (customCategoryText.isBlank()) "Tişört" else customCategoryText
                                } else {
                                    val base = if (inputCategory.isBlank()) "Tişört" else inputCategory
                                    if (inputSubCategory == "Standart") base else "$inputSubCategory $base"
                                }
                                val color = if (inputColor.isBlank()) "Siyah" else inputColor
                                val brand = if (inputBrand.isBlank()) null else inputBrand
                                val size = if (inputSize.isBlank()) "M" else inputSize
                                viewModel.uploadImage(context, currentPhotoUri!!, category, color, brand, size)
                                
                                // Reset inputs
                                inputCategory = "Tişört"
                                inputColor = "Siyah"
                                inputSize = "M"
                                inputBrand = ""
                                isCustomColorSelected = false
                                isCustomCategorySelected = false
                                customCategoryText = ""
                                inputSubCategory = "Standart"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(AppConfig.t("Gardıroba Kaydet", "Save to Wardrobe"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                        
                        TextButton(
                            onClick = {
                                showDetailsDialog = false
                                inputCategory = "Tişört"
                                inputColor = "Siyah"
                                inputSize = "M"
                                inputBrand = ""
                                isCustomColorSelected = false
                                isCustomCategorySelected = false
                                customCategoryText = ""
                                inputSubCategory = "Standart"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(AppConfig.t("İptal Et", "Cancel"), color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(AppConfig.t("Kıyafeti Sil", "Delete Item"), fontWeight = FontWeight.Bold) },
            text = { Text(AppConfig.t("Bu kıyafeti gardırobunuzdan kalıcı olarak silmek istediğinize emin misiniz?", "Are you sure you want to permanently delete this item from your wardrobe?")) },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteItem(it.id) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(AppConfig.t("Sil", "Delete"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(AppConfig.t("İptal", "Cancel"), color = Color.Gray)
                }
            }
        )
    }
}

// Smart casing-insensitive stock fashion image supplier with pure product focus (no faces/beards)
fun getStockImageForCategory(cat: String): String {
    val lower = cat.lowercase()
    return when {
        lower.contains("tişört") || lower.contains("tshirt") || lower.contains("t-shirt") -> 
            "https://images.unsplash.com/photo-1581655353564-df123a1eb820?q=80&w=600&auto=format&fit=crop"
        lower.contains("gömlek") -> 
            "https://images.unsplash.com/photo-1603252109303-2751441dd157?q=80&w=600&auto=format&fit=crop"
        lower.contains("pantolon") || lower.contains("jean") -> 
            "https://images.unsplash.com/photo-1542272604-787c3835535d?q=80&w=600&auto=format&fit=crop"
        lower.contains("ceket") || lower.contains("mont") || lower.contains("kaban") -> 
            "https://images.unsplash.com/photo-1551028719-00167b16eac5?q=80&w=600&auto=format&fit=crop"
        lower.contains("ayakkabı") || lower.contains("sneaker") || lower.contains("bot") -> 
            "https://images.unsplash.com/photo-1549298916-b41d501d3772?q=80&w=600&auto=format&fit=crop"
        lower.contains("takım") -> 
            "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?q=80&w=600&auto=format&fit=crop"
        lower.contains("aksesuar") || lower.contains("gözlük") || lower.contains("şapka") || lower.contains("bere") || lower.contains("saat") || lower.contains("kemer") -> 
            "https://images.unsplash.com/photo-1509319117193-57bab727e09d?q=80&w=600&auto=format&fit=crop"
        lower.contains("elbise") -> 
            "https://images.unsplash.com/photo-1595777457583-95e059d581b8?q=80&w=600&auto=format&fit=crop"
        lower.contains("etek") -> 
            "https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?q=80&w=600&auto=format&fit=crop"
        else -> 
            "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?q=80&w=600&auto=format&fit=crop"
    }
}

// Map user wardrobe items to our static premium main categories safely
fun getItemsForMainCategory(items: List<WardrobeItemDto>, mainCat: String): List<WardrobeItemDto> {
    val lowerMain = mainCat.lowercase()
    return items.filter { item ->
        val itemCat = item.category.lowercase()
        when (lowerMain) {
            "tişört" -> itemCat.contains("tişört") || itemCat.contains("tshirt") || itemCat.contains("t-shirt")
            "pantolon" -> itemCat.contains("pantolon") || itemCat.contains("jean")
            "ceket" -> itemCat.contains("ceket") || itemCat.contains("mont") || itemCat.contains("kaban")
            "ayakkabı" -> itemCat.contains("ayakkabı") || itemCat.contains("sneaker") || itemCat.contains("bot")
            "takım" -> itemCat.contains("takım")
            "aksesuar" -> itemCat.contains("aksesuar") || itemCat.contains("gözlük") || itemCat.contains("şapka") || itemCat.contains("bere") || itemCat.contains("saat") || itemCat.contains("kemer")
            else -> itemCat.contains(lowerMain)
        }
    }
}

// Casing-insensitive helper to filter items by sub-category/material type
fun isItemInSubCategory(item: WardrobeItemDto, mainCategory: String, subCategory: String): Boolean {
    if (subCategory == "Hepsi") return true
    val itemCat = item.category.lowercase()
    val sub = subCategory.lowercase()
    return itemCat.contains(sub)
}

@Composable
fun CategoryCollectionCard(
    categoryName: String,
    imageUrl: String,
    itemCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = categoryName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Subtle dark overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            
            // Bottom Info Column
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                val displayName = AppConfig.t(categoryName, when (categoryName) {
                    "Tişört" -> "T-Shirt"
                    "Gömlek" -> "Shirt"
                    "Pantolon" -> "Pants"
                    "Ceket" -> "Jacket"
                    "Ayakkabı" -> "Shoes"
                    "Elbise" -> "Dress"
                    "Aksesuar" -> "Accessory"
                    "Etek" -> "Skirt"
                    "Kazak" -> "Sweater"
                    "Takım" -> "Suit"
                    else -> categoryName
                })
                Text(
                    text = displayName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppConfig.t("$itemCount Parça Kıyafet", "$itemCount Clothes Items"),
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun WardrobeItemCard(item: WardrobeItemDto, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image Content
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(com.vesti.app.AppConfig.resolveImageSource(item.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = item.category,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
            
            // Bottom Info Overlay with soft gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Column {
                    val itemCategoryDisplay = AppConfig.translateCategory(item.category)
                    Text(
                        text = itemCategoryDisplay,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.color.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val itemColorDisplay = AppConfig.translateColor(item.color)
                        Text(
                            text = itemColorDisplay,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Delete Action on Top Right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = AppConfig.t("Sil", "Delete"),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HangerLoadingAnimation(
    title: String? = null,
    subtitle: String = ""
) {
    val resolvedTitle = title ?: AppConfig.t("Kıyafetiniz Askıya Asılıyor", "Hanging your clothes...")
    val infiniteTransition = rememberInfiniteTransition(label = "hanger")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    val clothingColor by infiniteTransition.animateColor(
        initialValue = VestiColors.Primary,
        targetValue = Color(0xFF81C784),
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp).fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                val hookPath = Path().apply {
                    moveTo(width * 0.5f, height * 0.35f)
                    quadraticBezierTo(width * 0.5f, height * 0.15f, width * 0.58f, height * 0.15f)
                    quadraticBezierTo(width * 0.65f, height * 0.15f, width * 0.65f, height * 0.22f)
                }
                drawPath(
                    path = hookPath,
                    color = Color.DarkGray,
                    style = Stroke(width = 2.5f.dp.toPx())
                )
                
                val hangerTriangle = Path().apply {
                    moveTo(width * 0.5f, height * 0.35f)
                    lineTo(width * 0.2f, height * 0.5f)
                    lineTo(width * 0.8f, height * 0.5f)
                    close()
                }
                drawPath(
                    path = hangerTriangle,
                    color = Color.DarkGray,
                    style = Stroke(width = 2.5f.dp.toPx())
                )
                
                val clothingPath = Path().apply {
                    moveTo(width * 0.25f, height * 0.5f)
                    lineTo(width * 0.12f, height * 0.65f)
                    lineTo(width * 0.2f, height * 0.72f)
                    lineTo(width * 0.32f, height * 0.62f)
                    
                    lineTo(width * 0.32f, height * 0.9f)
                    lineTo(width * 0.68f, height * 0.9f)
                    lineTo(width * 0.68f, height * 0.62f)
                    
                    lineTo(width * 0.8f, height * 0.72f)
                    lineTo(width * 0.88f, height * 0.65f)
                    lineTo(width * 0.75f, height * 0.5f)
                    close()
                }
                drawPath(
                    path = clothingPath,
                    color = clothingColor
                )
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 6.dp.toPx(),
                    center = Offset(width * 0.5f, height * 0.53f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(18.dp))
        
        Text(
            text = resolvedTitle,
            color = VestiColors.TextMain,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp,
            textAlign = TextAlign.Center
        )
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun resolveColorFromName(name: String): Color? {
    val clean = name.lowercase().trim()
    return when {
        clean.isBlank() -> null
        clean.contains("gül kurusu") || clean.contains("rose dust") -> Color(0xFFC08081)
        clean.contains("vişne çürüğü") || clean.contains("cherry dregs") -> Color(0xFF722F37)
        clean.contains("petrol") || clean.contains("teal") -> Color(0xFF005F73)
        clean.contains("haki") || clean.contains("khaki") -> Color(0xFF5E6737)
        clean.contains("saks") || clean.contains("royal blue") -> Color(0xFF0038A8)
        clean.contains("pudra") || clean.contains("powder pink") -> Color(0xFFFFD1DC)
        clean.contains("ekru") || clean.contains("ecru") -> Color(0xFFF3EAD3)
        clean.contains("taba") || clean.contains("tan") -> Color(0xFFB35A2B)
        clean.contains("antrasit") || clean.contains("anthracite") || clean.contains("charcoal") -> Color(0xFF36454F)
        clean.contains("hardal") || clean.contains("mustard") -> Color(0xFFE1AD01)
        clean.contains("mint") || clean.contains("nane") -> Color(0xFFAAF0D1)
        clean.contains("lila") || clean.contains("lilac") -> Color(0xFFC8A2C8)
        clean.contains("somon") || clean.contains("salmon") -> Color(0xFFFF8C69)
        clean.contains("gece mavisi") || clean.contains("midnight blue") -> Color(0xFF191970)
        clean.contains("fıstık") || clean.contains("pistachio") -> Color(0xFFB2EC5D)
        clean.contains("kiremit") || clean.contains("brick red") -> Color(0xFFB64227)
        clean.contains("vizon") || clean.contains("mink") -> Color(0xFF7A6855)
        clean.contains("şeftali") || clean.contains("peach") -> Color(0xFFFFDAB9)
        clean.contains("bordo") || clean.contains("burgundy") -> Color(0xFF800020)
        clean.contains("indigo") -> Color(0xFF4B0082)
        clean.contains("camel") || clean.contains("deve tüyü") -> Color(0xFFC19A6B)
        clean.contains("altın") || clean.contains("dore") || clean.contains("gold") -> Color(0xFFD4AF37)
        clean.contains("gümüş") || clean.contains("lame") || clean.contains("silver") -> Color(0xFFC0C0C0)
        clean.contains("fuşya") || clean.contains("fuchsia") -> Color(0xFFFF00FF)
        clean.contains("turkuaz") || clean.contains("turquoise") -> Color(0xFF40E0D0)
        clean.contains("mercan") || clean.contains("coral") -> Color(0xFFFF7F50)
        
        // Base colors
        clean.contains("siyah") || clean.contains("black") -> Color.Black
        clean.contains("beyaz") || clean.contains("white") -> Color.White
        clean.contains("mavi") || clean.contains("blue") -> Color(0xFF2196F3)
        clean.contains("kırmızı") || clean.contains("red") -> Color(0xFFF44336)
        clean.contains("yeşil") || clean.contains("green") -> Color(0xFF4CAF50)
        clean.contains("gri") || clean.contains("gray") || clean.contains("grey") -> Color(0xFF9E9E9E)
        clean.contains("krem") || clean.contains("cream") -> Color(0xFFFFFDD0)
        clean.contains("bej") || clean.contains("beige") -> Color(0xFFF5F5DC)
        clean.contains("sarı") || clean.contains("yellow") -> Color(0xFFFFEB3B)
        clean.contains("turuncu") || clean.contains("orange") -> Color(0xFFFF9800)
        clean.contains("mor") || clean.contains("purple") || clean.contains("violet") -> Color(0xFF9C27B0)
        clean.contains("pembe") || clean.contains("pink") -> Color(0xFFE91E63)
        clean.contains("kahve") || clean.contains("brown") -> Color(0xFF795548)
        clean.contains("lacivert") || clean.contains("navy") -> Color(0xFF1A237E)
        
        else -> null
    }
}
