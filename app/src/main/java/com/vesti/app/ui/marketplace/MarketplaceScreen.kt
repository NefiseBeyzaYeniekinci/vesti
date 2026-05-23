@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.vesti.app.ui.marketplace

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vesti.app.ui.wardrobe.CameraHelper
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vesti.app.ui.wardrobe.WardrobeState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.ui.theme.VestiColors

data class MockProduct(
    val id: String,
    val title: String,
    val brand: String,
    val size: String,
    val price: String,
    val condition: String,
    val isSwap: Boolean,
    val sellerInitials: String,
    val sellerName: String,
    val rating: String,
    val imageUrl: String
)

val mockProducts = listOf(
    MockProduct(
        id = "1",
        title = "Vintage Deri Ceket",
        brand = "Vintage",
        size = "Beden: M",
        price = "1.250 ₺",
        condition = "Kullanılmış",
        isSwap = true,
        sellerInitials = "AY",
        sellerName = "Ahmet Yılmaz",
        rating = "4.8",
        imageUrl = "https://images.unsplash.com/photo-1551028719-00167b16eac5"
    ),
    MockProduct(
        id = "2",
        title = "Nike Air Force 1",
        brand = "Nike",
        size = "Beden: 42",
        price = "3.500 ₺",
        condition = "Yeni Gibi",
        isSwap = false,
        sellerInitials = "AK",
        sellerName = "Ayşe Kaya",
        rating = "5.0",
        imageUrl = "https://images.unsplash.com/photo-1543163521-1bf539c55dd2"
    ),
    MockProduct(
        id = "3",
        title = "Zara Beyaz Keten Gömlek",
        brand = "Zara",
        size = "Beden: L",
        price = "850 ₺",
        condition = "Sıfır",
        isSwap = true,
        sellerInitials = "MD",
        sellerName = "Mehmet Demir",
        rating = "4.5",
        imageUrl = "https://images.unsplash.com/photo-1591047139829-d91aecb6caea"
    ),
    MockProduct(
        id = "4",
        title = "Levi's 501 Jean",
        brand = "Levi's",
        size = "Beden: 32",
        price = "1.100 ₺",
        condition = "Az Kullanılmış",
        isSwap = true,
        sellerInitials = "BK",
        sellerName = "Burak Koç",
        rating = "4.9",
        imageUrl = "https://images.unsplash.com/photo-1542272604-787c3835535d"
    ),
    MockProduct(
        id = "5",
        title = "Polo Yaka Tişört",
        brand = "Lacoste",
        size = "Beden: L",
        price = "650 ₺",
        condition = "Yeni Gibi",
        isSwap = false,
        sellerInitials = "ED",
        sellerName = "Elif Doğan",
        rating = "4.7",
        imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"
    ),
    MockProduct(
        id = "6",
        title = "Siyah Mini Elbise",
        brand = "Mango",
        size = "Beden: S",
        price = "900 ₺",
        condition = "Kullanılmış",
        isSwap = true,
        sellerInitials = "ZA",
        sellerName = "Zeynep Aslan",
        rating = "4.6",
        imageUrl = "https://images.unsplash.com/photo-1539008835657-9e8e9680c956"
    ),
    MockProduct(
        id = "7",
        title = "Güneş Gözlüğü",
        brand = "Ray-Ban",
        size = "Standart",
        price = "2.100 ₺",
        condition = "Sıfır",
        isSwap = false,
        sellerInitials = "CT",
        sellerName = "Can Tekin",
        rating = "5.0",
        imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083"
    ),
    MockProduct(
        id = "8",
        title = "Kışlık Şişme Mont",
        brand = "The North Face",
        size = "Beden: XL",
        price = "4.500 ₺",
        condition = "Yeni Gibi",
        isSwap = false,
        sellerInitials = "SO",
        sellerName = "Selin Öz",
        rating = "4.9",
        imageUrl = "https://images.unsplash.com/photo-1559551409-dadc959f76b8"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: MarketplaceViewModel,
    wardrobeViewModel: com.vesti.app.ui.wardrobe.WardrobeViewModel,
    onNavigateToCheckout: (String, Float) -> Unit,
    onNavigateToMessages: () -> Unit
) {
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var selectedWardrobeItem by remember { mutableStateOf<com.vesti.app.data.network.WardrobeItemDto?>(null) }
    var showSellDialog by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputPrice by remember { mutableStateOf("") }
    var inputBrand by remember { mutableStateOf("") }
    var inputSize by remember { mutableStateOf("M") }
    var inputCondition by remember { mutableStateOf("Yeni Gibi") }

    var searchQuery by remember { mutableStateOf("") }
    var activeChip by remember { mutableStateOf("Tümü") }
    var selectedWardrobeCategory by remember { mutableStateOf("Hepsi") }

    var showFilterDialog by remember { mutableStateOf(false) }

    // Advanced Filter states
    var filterSize by remember { mutableStateOf("Hepsi") }
    var filterBrand by remember { mutableStateOf("Hepsi") }
    var filterCondition by remember { mutableStateOf("Hepsi") }
    var filterTradeable by remember { mutableStateOf<Boolean?>(null) }
    var filterPriceMin by remember { mutableStateOf("") }
    var filterPriceMax by remember { mutableStateOf("") }

    val filteredProducts = remember(
        searchQuery, activeChip, filterSize, filterBrand, filterCondition, filterTradeable, filterPriceMin, filterPriceMax
    ) {
        mockProducts.filter { product ->
            val matchesSearch = product.title.contains(searchQuery, ignoreCase = true) ||
                                product.brand.contains(searchQuery, ignoreCase = true)
            
            // Standard quick filter chips (matches activeChip)
            val matchesQuickChip = when (activeChip) {
                "Takas Edilebilir" -> product.isSwap
                "Yeni Gibi" -> product.condition == "Yeni Gibi"
                "Sıfır" -> product.condition == "Sıfır"
                else -> true
            }

            // Advanced Filters
            val matchesSize = filterSize == "Hepsi" || product.size.contains(filterSize, ignoreCase = true)
            val matchesBrand = filterBrand == "Hepsi" || product.brand.equals(filterBrand, ignoreCase = true)
            val matchesCondition = filterCondition == "Hepsi" || product.condition.equals(filterCondition, ignoreCase = true)
            val matchesTradeable = filterTradeable == null || product.isSwap == filterTradeable
            
            val priceVal = product.price.replace(".", "").replace(" ₺", "").toFloatOrNull() ?: 0f
            val matchesPriceMin = filterPriceMin.isEmpty() || (filterPriceMin.toFloatOrNull() ?: 0f) <= priceVal
            val matchesPriceMax = filterPriceMax.isEmpty() || (filterPriceMax.toFloatOrNull() ?: Float.MAX_VALUE) >= priceVal

            matchesSearch && matchesQuickChip && matchesSize && matchesBrand && matchesCondition && matchesTradeable && matchesPriceMin && matchesPriceMax
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            showSellDialog = true
            selectedWardrobeItem = null
            selectedImageUri = null
        }
    }

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vesti Marketplace", fontWeight = FontWeight.Bold, color = VestiColors.DarkIndigo) },
                actions = {
                    IconButton(onClick = onNavigateToMessages) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Mesajlar",
                            tint = VestiColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VestiColors.Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showSellDialog = true
                    selectedWardrobeItem = null
                    currentPhotoUri = null
                    selectedImageUri = null
                    inputTitle = ""
                    inputPrice = ""
                    inputBrand = ""
                    inputSize = "M"
                    inputCondition = "Yeni Gibi"
                },
                containerColor = VestiColors.Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "İlan Ver")
            }
        },
        containerColor = VestiColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar (Ultra-Modern Borderless Search Pill)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Kıyafet, marka veya kategori ara...", color = Color.Gray, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara", tint = Color.Gray, modifier = Modifier.size(20.dp)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips (Elegant Active-State Pills)
            val filtersList = listOf("Filtrele", "Takas Edilebilir", "Yeni Gibi", "Sıfır")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtersList) { filterOpt ->
                    val isSelected = activeChip == filterOpt || (filterOpt == "Filtrele" && (filterSize != "Hepsi" || filterBrand != "Hepsi" || filterCondition != "Hepsi" || filterTradeable != null || filterPriceMin.isNotEmpty() || filterPriceMax.isNotEmpty()))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) VestiColors.Primary.copy(alpha = 0.15f) else Color(0xFFF3F4F6))
                            .clickable {
                                if (filterOpt == "Filtrele") {
                                    showFilterDialog = true
                                } else {
                                    activeChip = filterOpt
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (filterOpt == "Filtrele") {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (isSelected) VestiColors.Primary else Color.DarkGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = filterOpt,
                                color = if (isSelected) VestiColors.Primary else Color.DarkGray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of products (Guaranteed 16.dp Right Margin!)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductCard(product) {
                        val p = product.price.replace(".", "").replace(" ₺", "").toFloatOrNull() ?: 0f
                        onNavigateToCheckout(product.id, p)
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        ModalBottomSheet(
            onDismissRequest = { showFilterDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detaylı Filtreleme",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = VestiColors.DarkIndigo
                    )
                    IconButton(onClick = { showFilterDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Brand (Marka) Filter
                Text("Marka", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val brandOptions = listOf("Hepsi", "Vintage", "Nike", "Zara", "Levi's", "Lacoste", "Mango", "Ray-Ban", "The North Face")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(brandOptions) { opt ->
                        val isSelected = filterBrand == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VestiColors.Primary else Color(0xFFF3F4F6))
                                .clickable { filterBrand = opt }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(opt, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Size (Beden) Filter
                Text("Beden", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val sizeOptions = listOf("Hepsi", "S", "M", "L", "XL", "32", "42")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sizeOptions) { opt ->
                        val isSelected = filterSize == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VestiColors.Primary else Color(0xFFF3F4F6))
                                .clickable { filterSize = opt }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(opt, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Tradeability (Takas Edilebilirlik) Filter
                Text("İlan Türü (Takas Edilebilirlik)", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val swapOptions = listOf(
                        Triple("Tümü", null, "Hepsi"),
                        Triple("Takas Edilebilir", true, "Takas"),
                        Triple("Sadece Satılık", false, "Satış")
                    )
                    swapOptions.forEach { (label, value, short) ->
                        val isSelected = filterTradeable == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VestiColors.Primary else Color(0xFFF3F4F6))
                                .clickable { filterTradeable = value }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Condition (Kullanılma Derecesi) Filter
                Text("Kullanım Derecesi", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val conditionOptions = listOf("Hepsi", "Sıfır", "Yeni Gibi", "Az Kullanılmış", "Kullanılmış")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(conditionOptions) { opt ->
                        val isSelected = filterCondition == opt
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VestiColors.Primary else Color(0xFFF3F4F6))
                                .clickable { filterCondition = opt }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(opt, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 5. Price Range (Fiyat Aralığı) Filter
                Text("Fiyat Aralığı", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = filterPriceMin,
                        onValueChange = { filterPriceMin = it },
                        placeholder = { Text("En Az", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = VestiColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Text("—", color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = filterPriceMax,
                        onValueChange = { filterPriceMax = it },
                        placeholder = { Text("En Çok", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedBorderColor = VestiColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            filterBrand = "Hepsi"
                            filterSize = "Hepsi"
                            filterCondition = "Hepsi"
                            filterTradeable = null
                            filterPriceMin = ""
                            filterPriceMax = ""
                            activeChip = "Tümü"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                    ) {
                        Text("Temizle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Button(
                        onClick = { showFilterDialog = false },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary)
                    ) {
                        Text("Sonuçları Uygula", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showSellDialog) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSellDialog = false 
                selectedWardrobeItem = null
                currentPhotoUri = null
                selectedImageUri = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yeni İlan Oluştur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = VestiColors.TextMain
                    )
                    IconButton(onClick = {
                        showSellDialog = false
                        selectedWardrobeItem = null
                        currentPhotoUri = null
                        selectedImageUri = null
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Option 1: Choose from Digital Wardrobe
                Text("1. Gardırobumdan Hızlı Seç", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                val wardrobeState by wardrobeViewModel.state.collectAsStateWithLifecycle()
                if (wardrobeState is WardrobeState.Success) {
                    val wItems = (wardrobeState as WardrobeState.Success).items
                    if (wItems.isEmpty()) {
                        Text("Gardırobunuzda henüz kıyafet yok.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        // Category Filters Row for wardrobe select
                        val wardrobeCats = listOf("Hepsi", "Tişört", "Gömlek", "Kazak", "Ceket", "Pantolon", "Takım", "Aksesuar", "Ayakkabı")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            items(wardrobeCats) { cat ->
                                val isSelected = selectedWardrobeCategory == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) VestiColors.Primary.copy(alpha = 0.15f) else Color(0xFFF3F4F6))
                                        .clickable { selectedWardrobeCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) VestiColors.Primary else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        val filteredWItems = remember(wItems, selectedWardrobeCategory) {
                            if (selectedWardrobeCategory == "Hepsi") wItems
                            else wItems.filter { it.category.contains(selectedWardrobeCategory, ignoreCase = true) }
                        }
                        
                        if (filteredWItems.isEmpty()) {
                            Text("Bu kategoride kıyafetiniz bulunmuyor.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredWItems) { wItem ->
                                    val isSelected = selectedWardrobeItem?.id == wItem.id
                                    Card(
                                        modifier = Modifier
                                            .width(90.dp)
                                            .height(120.dp)
                                            .clickable {
                                                selectedWardrobeItem = wItem
                                                selectedImageUri = wItem.imageUrl
                                                currentPhotoUri = null
                                                inputTitle = "${wItem.color} ${wItem.category}"
                                                inputBrand = wItem.brand ?: ""
                                                inputSize = wItem.size ?: ""
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, VestiColors.Primary) else null,
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            val fullUrl = if (wItem.imageUrl.startsWith("http")) wItem.imageUrl else "http://192.168.1.103:8080${wItem.imageUrl}"
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(fullUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .padding(4.dp)
                                            ) {
                                                Text(wItem.category, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Option 2: Upload or Capture Custom Photo
                Text("2. Veya Fotoğraf Çek / Yükle", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        currentPhotoUri = uri
                        selectedWardrobeItem = null
                        selectedImageUri = null
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                val (file, uri) = CameraHelper.createTempImageFile(context)
                                currentPhotoUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Kamera İle Çek", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VestiColors.Primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VestiColors.Primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Galeriden Seç", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Image Preview if selected
                val previewData = selectedImageUri ?: currentPhotoUri
                if (previewData != null) {
                    Text("Seçilen Ürün Görseli", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        val finalUrl = if (previewData is String && !previewData.startsWith("http")) "http://192.168.1.103:8080$previewData" else previewData
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(finalUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Önizleme",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Form Fields
                Text("3. İlan Bilgileri", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = inputTitle,
                    onValueChange = { inputTitle = it },
                    label = { Text("Ürün Başlığı") },
                    placeholder = { Text("Örn: Vintage Deri Ceket") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VestiColors.Primary)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = inputPrice,
                    onValueChange = { inputPrice = it },
                    label = { Text("Fiyat (₺)") },
                    placeholder = { Text("Örn: 1250") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VestiColors.Primary)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = inputBrand,
                    onValueChange = { inputBrand = it },
                    label = { Text("Marka") },
                    placeholder = { Text("Örn: Zara, Vintage") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VestiColors.Primary)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Condition chip choices
                Text("Durum Seçimi", fontWeight = FontWeight.Bold, color = VestiColors.TextMain, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val conditionOptions = listOf("Yeni Gibi", "Sıfır", "Az Kullanılmış", "Kullanılmış")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(conditionOptions) { cond ->
                        val isSelected = inputCondition == cond
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) VestiColors.Primary else Color(0xFFF3F4F6))
                                .clickable { inputCondition = cond }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cond,
                                color = if (isSelected) Color.White else VestiColors.TextMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Submit Button
                Button(
                    onClick = {
                        showSellDialog = false
                        val title = if (inputTitle.isBlank()) "Yeni İlan" else inputTitle
                        val price = if (inputPrice.isBlank()) "0" else inputPrice
                        val cond = if (inputCondition.isBlank()) "Bilinmiyor" else inputCondition
                        
                        viewModel.createListing(
                            title = title,
                            price = price,
                            condition = cond,
                            imageUri = currentPhotoUri,
                            existingImageUrl = selectedImageUri
                        )
                        
                        // Reset
                        inputTitle = ""
                        inputPrice = ""
                        inputBrand = ""
                        inputCondition = "Yeni Gibi"
                        selectedWardrobeItem = null
                        currentPhotoUri = null
                        selectedImageUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("İlanı Yayınla", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun FilterChipItem(label: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) VestiColors.LightPurple else Color.White,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text = label,
                color = if (selected) VestiColors.Primary else Color.DarkGray,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: MockProduct, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(295.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (product.isSwap) {
                    Surface(
                        color = VestiColors.Primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                    ) {
                        Text("⇄ TAKAS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)
                ) {
                    Text(product.condition, color = VestiColors.TextMain, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.brand, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text(product.size, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.title,
                        color = VestiColors.TextMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Column {
                    Text(
                        text = product.price,
                        color = VestiColors.Primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 2.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(product.sellerInitials, fontSize = 9.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = product.sellerName,
                                fontSize = 11.sp,
                                color = VestiColors.TextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(product.rating, fontSize = 11.sp, color = VestiColors.TextMain, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
