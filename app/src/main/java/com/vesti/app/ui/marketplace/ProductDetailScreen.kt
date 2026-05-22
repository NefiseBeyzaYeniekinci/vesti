package com.vesti.app.ui.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vesti.app.ui.theme.VestiColors
import com.vesti.app.ui.wardrobe.WardrobeState
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    wardrobeViewModel: com.vesti.app.ui.wardrobe.WardrobeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (String, Float) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val product = mockProducts.find { it.id == productId } ?: mockProducts.first()
    val mockPrice = product.price.replace(".", "").replace(" ₺", "").toFloatOrNull() ?: 0f
    val sellerId = "user_${product.sellerInitials.lowercase()}"

    var showSwapSheet by remember { mutableStateOf(false) }
    var selectedWardrobeItem by remember { mutableStateOf<com.vesti.app.data.network.WardrobeItemDto?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedWardrobeCategory by remember { mutableStateOf("Hepsi") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ürün Detayı", fontWeight = FontWeight.Bold, color = VestiColors.DarkIndigo) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri Dön")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VestiColors.Background)
            )
        },
        containerColor = VestiColors.Background,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateToCheckout(productId, mockPrice) },
                        colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Sipariş Ver", fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { onNavigateToChat(sellerId) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.weight(1f).height(50.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Mesaj", tint = VestiColors.TextMain, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mesaj At", color = VestiColors.TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Highly interactive active/inactive Swap Button!
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            if (product.isSwap) {
                                showSwapSheet = true
                            } else {
                                Toast.makeText(context, "Bu ürün takasa uygun değil!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (product.isSwap) VestiColors.Primary else Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(50.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Takas",
                            tint = if (product.isSwap) Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Takas",
                            color = if (product.isSwap) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Big Image Mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                        modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                    ) {
                        Text("⇄ Takasa Uygun", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = VestiColors.LightPurple, shape = RoundedCornerShape(4.dp)) {
                        Text("DIŞ GİYİM", color = VestiColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                    }
                    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                        Text(product.size, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(product.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = VestiColors.TextMain)
                Text(product.price, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = VestiColors.Primary)

                Spacer(modifier = Modifier.height(16.dp))
                
                // Seller Description
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Açıklama", tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Satıcı Açıklaması", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "80'lerden kalma, harika durumda hakiki deri ceket. Hiçbir yırtığı veya söküğü yoktur.",
                            color = VestiColors.TextMain,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Boxes
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Marka", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(product.brand, color = VestiColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Surface(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Durumu", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(product.condition, color = VestiColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Seller Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(product.sellerInitials, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(product.sellerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VestiColors.TextMain)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Puan", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                Text("${product.rating}  ·  İstanbul, TR", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Güvenli", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vesti Güvencesi", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSwapSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSwapSheet = false 
                selectedWardrobeItem = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Takas Teklifi Yap",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = VestiColors.TextMain
                    )
                    IconButton(onClick = {
                        showSwapSheet = false
                        selectedWardrobeItem = null
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Takas etmek istediğin kıyafetini gardırobundan hızlıca seç:",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val wardrobeState by wardrobeViewModel.state.collectAsStateWithLifecycle()
                if (wardrobeState is WardrobeState.Success) {
                    val wItems = (wardrobeState as WardrobeState.Success).items
                    if (wItems.isEmpty()) {
                        Text(
                            text = "Gardırobunuzda henüz kıyafet yok. Takas yapmak için önce gardırobunuza kıyafet eklemelisiniz.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        // Category Filters Row for wardrobe select inside swap
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
                            Text(
                                text = "Bu kategoride kıyafetiniz bulunmuyor.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 24.dp).align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(filteredWItems) { wItem ->
                                    val isSelected = selectedWardrobeItem?.id == wItem.id
                                    Card(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(150.dp)
                                            .clickable { selectedWardrobeItem = wItem },
                                        shape = RoundedCornerShape(16.dp),
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
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = wItem.category,
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        showSwapSheet = false
                        showSuccessDialog = true
                    },
                    enabled = selectedWardrobeItem != null,
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (selectedWardrobeItem != null) "Takas Teklifini Gönder" else "Önce Kıyafet Seçin",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false 
                selectedWardrobeItem = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Başarılı",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Teklifin İletildi!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = VestiColors.TextMain
                )
            },
            text = {
                Text(
                    text = "${product.sellerName} teklifini inceleyip kısa süre içerisinde sana mesaj kutusu üzerinden dönüş yapacak.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSuccessDialog = false 
                        selectedWardrobeItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tamam", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
