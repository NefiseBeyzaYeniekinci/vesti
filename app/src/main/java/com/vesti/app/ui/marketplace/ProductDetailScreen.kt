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
import androidx.compose.material.icons.filled.Delete
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
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors
import com.vesti.app.ui.wardrobe.WardrobeState
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    marketplaceViewModel: MarketplaceViewModel,
    wardrobeViewModel: com.vesti.app.ui.wardrobe.WardrobeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: (String, Float) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val marketplaceState by marketplaceViewModel.state.collectAsStateWithLifecycle()
    val product = (marketplaceState as? MarketplaceState.Success)?.items?.find { it.id == productId }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VestiColors.Primary)
        }
        return
    }

    val priceVal = (product.price ?: 0.0).toFloat()
    val sellerId = product.sellerId ?: ""
    val isSwap = true // enable swap offering for all items to showcase UI
    val sellerName = "Satıcı: ${sellerId.take(5)}"
    val sellerInitials = if (sellerId.length >= 2) sellerId.substring(0, 2).uppercase() else "VS"
    val rating = "4.9"

    val context = LocalContext.current
    val tokenManager = remember { com.vesti.app.data.local.TokenManager(context) }
    val currentUserId by tokenManager.userIdFlow.collectAsState(initial = "")
    val isOwner = sellerId == currentUserId && currentUserId.isNotEmpty()

    var showSwapSheet by remember { mutableStateOf(false) }
    var selectedWardrobeItem by remember { mutableStateOf<com.vesti.app.data.network.WardrobeItemDto?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var selectedWardrobeCategory by remember { mutableStateOf("Hepsi") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppConfig.t("Ürün Detayı", "Product Details"), fontWeight = FontWeight.Bold, color = VestiColors.DarkIndigo) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppConfig.t("Geri Dön", "Go Back"))
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
                    if (isOwner) {
                        var isDeleting by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                isDeleting = true
                                marketplaceViewModel.deleteListing(productId) {
                                    isDeleting = false
                                    Toast.makeText(context, AppConfig.t("İlan başarıyla kaldırıldı.", "Listing removed successfully."), Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isDeleting
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(AppConfig.t("Bu İlanı Kaldır", "Remove This Listing"), fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onNavigateToCheckout(productId, priceVal) },
                            colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Text(AppConfig.t("Sipariş Ver", "Order Now"), fontWeight = FontWeight.Bold)
                        }
                        
                        OutlinedButton(
                            onClick = { onNavigateToChat(sellerId) },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.weight(1f).height(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = AppConfig.t("Mesaj", "Message"), tint = VestiColors.TextMain, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppConfig.t("Mesaj At", "Send Message"), color = VestiColors.TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Highly interactive active/inactive Swap Button!
                        val swapNotEligibleMsg = AppConfig.t("Bu ürün takasa uygun değil!", "This item is not eligible for swap!")
                        Button(
                            onClick = {
                                if (isSwap) {
                                    showSwapSheet = true
                                } else {
                                    Toast.makeText(context, swapNotEligibleMsg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSwap) VestiColors.Primary else Color(0xFFE5E7EB)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(50.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = AppConfig.t("Takas", "Swap"),
                                tint = if (isSwap) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppConfig.t("Takas", "Swap"),
                                color = if (isSwap) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                        .data(com.vesti.app.AppConfig.resolveImageSource(product.imageUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = product.title ?: "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isSwap) {
                    Surface(
                        color = VestiColors.Primary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                    ) {
                        Text(AppConfig.t("⇄ Takasa Uygun", "⇄ Available for Swap"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = VestiColors.LightPurple, shape = RoundedCornerShape(4.dp)) {
                        Text(AppConfig.t("DIŞ GİYİM", "OUTERWEAR"), color = VestiColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                    }
                    Surface(color = Color(0xFFF3F4F6), shape = RoundedCornerShape(4.dp)) {
                        val sz = product.size ?: "Standart"
                        val sizeDisp = AppConfig.t(sz, when {
                            sz == "Standart" -> "Standard"
                            sz.startsWith("Beden:") -> sz.replace("Beden:", "Size:")
                            else -> sz
                        })
                        Text(sizeDisp, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(product.title ?: "", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = VestiColors.TextMain)
                val priceDisp = "${(product.price ?: 0.0).toInt()} ₺"
                Text(priceDisp, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = VestiColors.Primary)

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
                            Text(AppConfig.t("Satıcı Açıklaması", "Seller Description"), color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description ?: "",
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
                            Text(AppConfig.t("Kategori", "Category"), color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(product.category ?: "", color = VestiColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Surface(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(AppConfig.t("Durumu", "Condition"), color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val cond = product.condition ?: "Yeni Gibi"
                            val condDisp = AppConfig.t(cond, when (cond) {
                                "Sıfır" -> "Brand New"
                                "Yeni Gibi" -> "Like New"
                                "Az Kullanılmış" -> "Lightly Used"
                                "Kullanılmış" -> "Used"
                                else -> cond
                            })
                            Text(condDisp, color = VestiColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                            Text(sellerInitials, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(sellerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VestiColors.TextMain)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Puan", tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                Text("$rating  ·  İstanbul, TR", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = AppConfig.t("Güvenli", "Secure"), tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppConfig.t("Vesti Güvencesi", "Vesti Guarantee"), color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showSwapSheet && !isOwner) {
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
                        text = AppConfig.t("Takas Teklifi Yap", "Make a Swap Offer"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = VestiColors.TextMain
                    )
                    IconButton(onClick = {
                        showSwapSheet = false
                        selectedWardrobeItem = null
                    }) {
                        Icon(Icons.Default.Close, contentDescription = AppConfig.t("Kapat", "Close"), tint = Color.Gray)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = AppConfig.t("Takas etmek istediğin kıyafetini gardırobundan hızlıca seç:", "Quickly choose the clothes you want to swap from your wardrobe:"),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val wardrobeState by wardrobeViewModel.state.collectAsStateWithLifecycle()
                if (wardrobeState is WardrobeState.Success) {
                    val wItems = (wardrobeState as WardrobeState.Success).items
                    if (wItems.isEmpty()) {
                        Text(
                            text = AppConfig.t("Gardırobunuzda henüz kıyafet yok. Takas yapmak için önce gardırobunuza kıyafet eklemelisiniz.", "Your wardrobe is empty. You must add clothes to your wardrobe first to make a swap."),
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
                                val displayCat = AppConfig.t(cat, when(cat) {
                                    "Hepsi" -> "All"
                                    "Tişört" -> "T-Shirt"
                                    "Gömlek" -> "Shirt"
                                    "Kazak" -> "Sweater"
                                    "Ceket" -> "Jacket"
                                    "Pantolon" -> "Pants"
                                    "Takım" -> "Suit"
                                    "Aksesuar" -> "Accessory"
                                    "Ayakkabı" -> "Shoes"
                                    else -> cat
                                })
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) VestiColors.Primary.copy(alpha = 0.15f) else Color(0xFFF3F4F6))
                                        .clickable { selectedWardrobeCategory = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = displayCat,
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
                                text = AppConfig.t("Bu kategoride kıyafetiniz bulunmuyor.", "You don't have clothes in this category."),
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
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(com.vesti.app.AppConfig.resolveImageSource(wItem.imageUrl))
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
                                                val catDisplay = AppConfig.t(wItem.category, when(wItem.category.lowercase()) {
                                                    "tişört" -> "T-Shirt"
                                                    "gömlek" -> "Shirt"
                                                    "pantolon" -> "Pants"
                                                    "ceket" -> "Jacket"
                                                    "takım" -> "Suit"
                                                    "aksesuar" -> "Accessory"
                                                    "ayakkabı" -> "Shoes"
                                                    "elbise" -> "Dress"
                                                    "etek" -> "Skirt"
                                                    else -> wItem.category
                                                })
                                                Text(
                                                    text = catDisplay,
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
                        text = if (selectedWardrobeItem != null) AppConfig.t("Takas Teklifini Gönder", "Send Swap Offer") else AppConfig.t("Önce Kıyafet Seçin", "Select Clothes First"),
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
                    contentDescription = AppConfig.t("Başarılı", "Success"),
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = AppConfig.t("Teklifin İletildi!", "Offer Sent!"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = VestiColors.TextMain
                )
            },
            text = {
                Text(
                    text = AppConfig.t(
                        "$sellerName teklifini inceleyip kısa süre içerisinde sana mesaj kutusu üzerinden dönüş yapacak.",
                        "$sellerName will review your offer and get back to you via messages shortly."
                    ),
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
                    Text(AppConfig.t("Tamam", "OK"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
