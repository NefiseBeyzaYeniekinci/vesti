package com.vesti.app.ui.marketplace

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
    val imageColor: Color
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
        imageColor = Color(0xFFD1D5DB)
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
        imageColor = Color(0xFFFBCFE8)
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
        imageColor = Color(0xFFE5E7EB)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToCheckout: (String, Float) -> Unit,
    onNavigateToMessages: () -> Unit
) {
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
                onClick = { /* İlan Ver */ },
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
                    .background(product.imageColor)
            ) {
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
