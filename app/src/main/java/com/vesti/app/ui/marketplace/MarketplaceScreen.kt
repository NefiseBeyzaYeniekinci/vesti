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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
    onNavigateToCheckout: (String, Float) -> Unit,
    onNavigateToMessages: () -> Unit
) {
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showSellDialog by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputPrice by remember { mutableStateOf("") }
    var inputCondition by remember { mutableStateOf("Yeni Gibi") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            showSellDialog = true
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
                    if (cameraPermissionState.status.isGranted) {
                        val (file, uri) = CameraHelper.createTempImageFile(context)
                        currentPhotoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                containerColor = VestiColors.Primary,
                contentColor = Color.White
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
            Text(
                text = "Kullanmadığın kıyafetleri sat veya yenileriyle takasla.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Kıyafet, marka veya kategori ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { },
                        label = { Text("Filtreler") },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filtreler", modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White)
                    )
                }
                item { FilterChipItem("Cinsiyet", true) }
                item { FilterChipItem("Ürün Tipi", false) }
                item { FilterChipItem("Beden", false) }
                item { FilterChipItem("Ücret", false) }
                item { FilterChipItem("Konum", false) }
                item { FilterChipItem("Satıcı Puanı", false) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of products
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp, bottom=100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mockProducts) { product ->
                    ProductCard(product) {
                        // Dummy price conversion 1.250 -> 1250f
                        val p = product.price.replace(".", "").replace(" ₺", "").toFloatOrNull() ?: 0f
                        onNavigateToCheckout(product.id, p)
                    }
                }
            }
        }
    }

    if (showSellDialog) {
        AlertDialog(
            onDismissRequest = { showSellDialog = false },
            title = { Text("Yeni İlan Ekle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Ürün Başlığı (örn. Mavi Ceket)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = inputPrice,
                        onValueChange = { inputPrice = it },
                        label = { Text("Fiyat (₺)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = inputCondition,
                        onValueChange = { inputCondition = it },
                        label = { Text("Durum (örn. Yeni Gibi)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showSellDialog = false
                    val title = if (inputTitle.isBlank()) "Yeni İlan" else inputTitle
                    val price = if (inputPrice.isBlank()) "0" else inputPrice
                    val cond = if (inputCondition.isBlank()) "Bilinmiyor" else inputCondition
                    viewModel.createListing(title, price, cond, currentPhotoUri)
                    
                    // Reset
                    inputTitle = ""
                    inputPrice = ""
                    inputCondition = "Yeni Gibi"
                }) {
                    Text("İlanı Ver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = false }) {
                    Text("İptal")
                }
            }
        )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
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
                        Text("⇄ TAKAS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Surface(
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)
                ) {
                    Text(product.condition, color = VestiColors.TextMain, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(product.brand, color = Color.Gray, fontSize = 12.sp)
                    Text(product.size, color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.title, color = VestiColors.TextMain, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.price, color = VestiColors.DarkIndigo, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(product.sellerInitials, fontSize = 10.sp, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(product.sellerName, fontSize = 11.sp, color = VestiColors.TextMain, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(60.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                        Text(product.rating, fontSize = 11.sp, color = VestiColors.TextMain, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
