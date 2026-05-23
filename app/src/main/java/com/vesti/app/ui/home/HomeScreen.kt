package com.vesti.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vesti.app.ui.theme.VestiColors
import com.vesti.app.ui.wardrobe.WardrobeState
import com.vesti.app.ui.wardrobe.WardrobeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    wardrobeViewModel: WardrobeViewModel,
    onNavigateToOutfit: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    val wardrobeState by wardrobeViewModel.state.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VestiColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                HomeHeader(onNotificationsClick = { showNotificationsDialog = true })
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                WeatherCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 1. Recently Added Clothes (Son Eklenenler)
            item {
                val items = when (val ws = wardrobeState) {
                    is WardrobeState.Success -> ws.items.takeLast(6).reversed()
                    else -> emptyList()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Son Eklenen Parçalar",
                        color = VestiColors.TextMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Tümünü Gör",
                        color = VestiColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToWardrobe() }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                if (items.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Dolabın henüz boş duruyor 🧥",
                                color = VestiColors.TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gardırop sekmesinden kıyafetlerini ekleyerek stil önerileri almaya başla!",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(items) { cloth ->
                            Card(
                                modifier = Modifier
                                    .width(95.dp)
                                    .clickable { onNavigateToWardrobe() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(95.dp)
                                            .background(Color(0xFFFAFAFA))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(cloth.imageUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = cloth.category,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .align(Alignment.BottomStart)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = cloth.size ?: "M",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = cloth.category,
                                        color = VestiColors.TextMain,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    Text(
                                        text = cloth.brand ?: cloth.color,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // 2. Wardrobe Analysis (Dolabının Analizi)
            item {
                Text(
                    text = "Dolabının Analizi",
                    color = VestiColors.TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                val (totalItems, favCategory, colorPalette) = when (val ws = wardrobeState) {
                    is WardrobeState.Success -> {
                        val items = ws.items
                        val total = items.size
                        val favCat = items.groupBy { it.category }
                            .maxByOrNull { it.value.size }?.key ?: "Yok"
                        val colors = items.groupBy { it.color }
                            .mapValues { it.value.size }
                            .toList()
                            .sortedByDescending { it.second }
                            .take(4)
                        Triple(total, favCat, colors)
                    }
                    else -> Triple(0, "Yükleniyor...", emptyList())
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Dolap Çeşitliliği",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (totalItems > 8) "Kombin Hazırlığı: Zengin" else "Kombin Hazırlığı: Başlangıç",
                                    color = VestiColors.Primary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VestiColors.LightPurple)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$totalItems Parça",
                                    color = VestiColors.Primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F1F1))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Favori Kategori",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = favCategory,
                                    color = VestiColors.TextMain,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(
                                    text = "Renk Paletin",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (colorPalette.isEmpty()) {
                                    Text(
                                        text = "Renk verisi henüz yok",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        colorPalette.forEach { (colorName, _) ->
                                            val resolvedColor = when (colorName.lowercase()) {
                                                "siyah" -> Color.Black
                                                "beyaz" -> Color.White
                                                "mavi" -> Color(0xFF2196F3)
                                                "kırmızı" -> Color(0xFFF44336)
                                                "yeşil" -> Color(0xFF4CAF50)
                                                "gri" -> Color(0xFF9E9E9E)
                                                "krem" -> Color(0xFFFFFDD0)
                                                else -> VestiColors.Primary
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(resolvedColor)
                                                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                DailyRecommendationCard(onNavigateToOutfit = onNavigateToOutfit)
            }
        }

        if (showNotificationsDialog) {
            NotificationsDialog(
                onDismiss = { showNotificationsDialog = false },
                onNavigateToOutfit = onNavigateToOutfit,
                onNavigateToMarket = onNavigateToMarket
            )
        }
    }
}

@Composable
fun HomeHeader(onNotificationsClick: () -> Unit) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> "Günaydın"
        in 12..17 -> "Tünaydın"
        in 18..22 -> "İyi Akşamlar"
        else -> "İyi Geceler"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting,",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Nefise Beyza",
                color = VestiColors.DarkIndigo,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF1F1F1), CircleShape)
                    .clickable { onNotificationsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Bildirimler",
                    tint = VestiColors.TextMain,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(VestiColors.LightPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NB",
                    color = VestiColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
fun WeatherCard() {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                android.widget.Toast.makeText(context, "Güneşli havaya uygun kombin tavsiyeleri VesVes chatbotta hazır! 🌤️", android.widget.Toast.LENGTH_SHORT).show()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            VestiColors.Primary,
                            Color(0xFF9FA8DA)
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left text block (Dynamic style tip)
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = "Bugün hava sıcak ve güneşli! İnce tişörtler ve keten pantolonlar giymek için harika bir gün. ☀️",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 22.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Right weather widget block (Clean hovering icons without boundary box)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFFFF9C4),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "24°",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "İSTANBUL • GÜNEŞLİ",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DailyRecommendationCard(onNavigateToOutfit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToOutfit() }
            .border(1.dp, Color(0xFFF1F1F1), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BUGÜNÜN İLHAMI",
                    color = VestiColors.Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Zamansız Vintage Ruhu",
                    color = VestiColors.TextMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dolabındaki parçaları Retro esintilerle birleştirerek çaba gerektirmeyen şıklığı yakala.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kombini Keşfet",
                        color = VestiColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = VestiColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Premium circular fashion icon container on the right
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(VestiColors.LightPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.filled.AutoAwesome ?: Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = VestiColors.Primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun NotificationsDialog(
    onDismiss: () -> Unit,
    onNavigateToOutfit: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Title and Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = VestiColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Bildirimler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = VestiColors.TextMain
                        )
                    }
                    
                    // Elegant close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF3F4F6), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Notifications List
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NotificationRow(
                        title = "🤖 VesVes Güncellemesi",
                        message = "VesVes stil asistanınız başarıyla güncellendi! Yepyeni stil ipuçlarını hemen denemek için Kombin sekmesine göz atın.",
                        time = "10 dakika önce",
                        isNew = true,
                        onClick = {
                            onDismiss()
                            onNavigateToOutfit()
                        }
                    )
                    NotificationRow(
                        title = "💼 Bugünün Önerisi",
                        message = "İstanbul'da hava sıcaklığı 24° ve güneşli! Dolabındaki keten gömlek tam bugün giymek için harika bir parça.",
                        time = "2 saat önce",
                        isNew = true,
                        onClick = {
                            onDismiss()
                            onNavigateToOutfit()
                        }
                    )
                    NotificationRow(
                        title = "🛍️ Takas Teklifi",
                        message = "Siyah Ceket ilanınıza @beyza kullanıcısından yeni bir takas teklifi geldi. Değerlendirmek için ilan detayına gidin.",
                        time = "5 saat önce",
                        isNew = false,
                        onClick = {
                            onDismiss()
                            onNavigateToMarket()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationRow(
    title: String,
    message: String,
    time: String,
    isNew: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isNew) VestiColors.LightPurple.copy(alpha = 0.5f) else Color(0xFFF9FAFB))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = VestiColors.TextMain
                )
                if (isNew) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(VestiColors.Primary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = time,
                color = Color.LightGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
