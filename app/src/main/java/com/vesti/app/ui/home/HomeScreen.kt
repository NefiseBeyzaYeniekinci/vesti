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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors
import com.vesti.app.ui.wardrobe.WardrobeState
import com.vesti.app.ui.wardrobe.WardrobeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    wardrobeViewModel: WardrobeViewModel,
    notificationViewModel: NotificationViewModel,
    profileViewModel: com.vesti.app.ui.profile.ProfileViewModel,
    onNavigateToOutfit: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val wardrobeState by wardrobeViewModel.state.collectAsStateWithLifecycle()
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val userName = when (val ps = profileState) {
        is com.vesti.app.ui.profile.ProfileLoadState.Success -> ps.profile.name ?: "Nefise Beyza"
        else -> "Nefise Beyza"
    }

    var weatherTemp by remember { mutableStateOf(24) }
    var weatherCity by remember { mutableStateOf("İstanbul") }
    var weatherCondition by remember { mutableStateOf("Güneşli") }
    var weatherTip by remember { mutableStateOf("Bugün hava sıcak ve güneşli! İnce tişörtler ve keten pantolonlar giymek için harika bir gün. ☀️") }
    var isSunny by remember { mutableStateOf(true) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    androidx.compose.runtime.LaunchedEffect(profileState) {
        val userProfile = (profileState as? com.vesti.app.ui.profile.ProfileLoadState.Success)?.profile
        val userLocation = userProfile?.location?.split(",")?.firstOrNull()?.trim() ?: "Istanbul"
        
        try {
            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.vesti.app.data.network.RetrofitClient.getWeatherApi().getCurrentWeatherByCity(
                    city = userLocation,
                    appId = "46d68849b621de45187315bdcbfd1121"
                )
            }
            if (response.isSuccessful && response.body() != null) {
                val weatherData = response.body()!!
                weatherTemp = Math.round(weatherData.main.temp).toInt()
                weatherCity = weatherData.name ?: userLocation
                val mainCond = weatherData.weather.firstOrNull()?.main ?: "Clear"
                val desc = weatherData.weather.firstOrNull()?.description ?: "açık"
                weatherCondition = desc.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
                
                isSunny = !mainCond.contains("rain", ignoreCase = true) && 
                          !mainCond.contains("snow", ignoreCase = true) && 
                          !mainCond.contains("cloud", ignoreCase = true)

                weatherTip = if (AppConfig.language == "tr") {
                    when {
                        weatherTemp > 22 -> "Bugün hava sıcak ve güneşli! İnce tişörtler ve keten pantolonlar giymek için harika bir gün. ☀️"
                        weatherTemp < 14 -> "Bugün hava soğuk! Kalın kabanlar, ceketler ve montlar giymek için harika bir gün. ❄️"
                        else -> "Bugün hava ılık! Rahat bir hırka veya ceket giymek için harika bir gün. ⛅"
                    }
                } else {
                    when {
                        weatherTemp > 22 -> "Today is warm and sunny! A great day to wear light t-shirts and linen pants. ☀️"
                        weatherTemp < 14 -> "Today is cold! A great day to wear warm coats, jackets and boots. ❄️"
                        else -> "Today is mild! A great day to wear a comfortable cardigan or jacket. ⛅"
                    }
                }
            }
        } catch (e: Exception) {
            // keep default
        }
    }

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
                HomeHeader(
                    userName = userName,
                    onNotificationsClick = { showNotificationsDialog = true },
                    onProfileClick = onNavigateToProfile
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                WeatherCard(
                    temp = weatherTemp,
                    city = weatherCity,
                    condition = weatherCondition,
                    tip = weatherTip,
                    isSunny = isSunny
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 1. Recently Added Clothes (Son Eklenenler)
            item {
                val items = when (val ws = wardrobeState) {
                    is WardrobeState.Success -> ws.items.take(5)
                    else -> emptyList()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppConfig.t("Son Eklenen Parçalar", "Recently Added Clothes"),
                        color = VestiColors.TextMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = AppConfig.t("Tümünü Gör", "View All"),
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = AppConfig.t("Dolabın henüz boş duruyor 🧥", "Your wardrobe is empty 🧥"),
                                color = VestiColors.TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppConfig.t(
                                    "Gardırop sekmesinden kıyafetlerini ekleyerek stil önerileri almaya başla!",
                                    "Add clothes from the wardrobe tab to start getting style recommendations!"
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                                                .data(com.vesti.app.AppConfig.resolveImageSource(cloth.imageUrl))
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
                                        text = AppConfig.translateCategory(cloth.category),
                                        color = VestiColors.TextMain,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    Text(
                                        text = cloth.brand ?: AppConfig.translateColor(cloth.color),
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

                        // Yana kaydırınca "Daha Fazla Gör" kartı
                        item {
                            Card(
                                modifier = Modifier
                                    .width(95.dp)
                                    .height(146.dp)
                                    .clickable { onNavigateToWardrobe() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = VestiColors.LightPurple.copy(alpha = 0.3f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, VestiColors.Primary.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = VestiColors.Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = AppConfig.t("Daha Fazla Gör", "See More"),
                                        color = VestiColors.Primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    text = AppConfig.t("Dolabının Analizi", "Wardrobe Analysis"),
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
                            .maxByOrNull { it.value.size }?.key ?: AppConfig.t("Yok", "None")
                        val colors = items.groupBy { it.color }
                            .mapValues { it.value.size }
                            .toList()
                            .sortedByDescending { it.second }
                            .take(4)
                        Triple(total, favCat, colors)
                    }
                    else -> Triple(0, AppConfig.t("Yükleniyor...", "Loading..."), emptyList())
                }

                val isDark = AppConfig.isDarkMode
                val innerBg = if (isDark) Color(0xFF1E2030) else Color(0xFFF8F9FB)
                val innerBorder = if (isDark) Color(0xFF2E3147) else Color(0xFFE2E8F0)
                val dividerColor = if (isDark) Color(0xFF2E3147) else Color(0xFFECEFF1)
                val compartmentBg = if (isDark) Color(0xFF25273C) else Color(0xFFF3F4F6)
                val compartmentBorder = if (isDark) Color(0xFF2D3046) else Color(0xFFE5E7EB)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Section title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(VestiColors.LightPurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = VestiColors.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = AppConfig.t("Stil Analitiği", "Style Analytics"),
                                        color = VestiColors.TextMain,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (totalItems > 8) {
                                            AppConfig.t("Kombin Hazırlığı: Yüksek", "Outfit Readiness: High")
                                        } else {
                                            AppConfig.t("Kombin Hazırlığı: Başlangıç", "Outfit Readiness: Starter")
                                        },
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Segmented Color Distribution Bar
                        if (colorPalette.isNotEmpty()) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = AppConfig.t("Dolap Renk Dağılımı", "Wardrobe Color Ratios"),
                                        color = VestiColors.TextMain.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    val dominantColorName = colorPalette.firstOrNull()?.first ?: ""
                                    Text(
                                        text = AppConfig.t("Baskın: ", "Dominant: ") + AppConfig.translateColor(dominantColorName),
                                        color = VestiColors.Primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Color bar segments
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (AppConfig.isDarkMode) Color(0xFF282B3D) else Color(0xFFF3F4F6))
                                ) {
                                    val totalColorCount = colorPalette.sumOf { it.second }.toFloat()
                                    colorPalette.forEach { (colorName, count) ->
                                        val weight = count / totalColorCount
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(weight.coerceAtLeast(0.04f))
                                                .background(resolveColorName(colorName))
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Color legend in a 2-column grid layout to prevent text clipping
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val chunks = colorPalette.take(4).chunked(2)
                                    chunks.forEach { rowColors ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            rowColors.forEach { (colorName, count) ->
                                                val totalColorCount = colorPalette.sumOf { it.second }.toFloat()
                                                val percentage = ((count / totalColorCount) * 100).toInt()
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(resolveColorName(colorName))
                                                            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${AppConfig.translateColor(colorName)} %$percentage",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            if (rowColors.size < 2) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Empty state indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (AppConfig.isDarkMode) Color(0xFF282B3D) else Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = AppConfig.t("Yeterli renk verisi yok.", "Not enough color data."),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Sleek horizontal divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Bottom Row: Modern metrics cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Items card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(compartmentBg)
                                    .border(1.dp, compartmentBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = AppConfig.t("Toplam Parça", "Total Items"),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$totalItems " + AppConfig.t("Adet", "Items"),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = VestiColors.Primary
                                    )
                                }
                            }

                            // Favorite Category card
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(compartmentBg)
                                    .border(1.dp, compartmentBorder, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = AppConfig.t("Favori Kategori", "Fav Category"),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = AppConfig.translateCategory(favCategory),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = VestiColors.Primary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            item {
                DailyRecommendationCard(onNavigateToOutfit = onNavigateToOutfit)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 3. Tarzına Özel Seçimler (En az 10 kıyafet barajı)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppConfig.t("Tarzına Özel Seçimler", "Selections for Your Style"),
                        color = VestiColors.TextMain,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = AppConfig.t("Tümünü Gör", "View All"),
                        color = VestiColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToMarket() }
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                val wardrobeCount = when (val ws = wardrobeState) {
                    is WardrobeState.Success -> ws.items.size
                    else -> 0
                }

                if (wardrobeCount < 10) {
                    // 10 kıyafet barajı uyarısı
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "👗",
                                fontSize = 32.sp
                            )
                            Text(
                                text = AppConfig.t(
                                    "Tarzına özel seçimler için dolabında en az 10 kıyafet olmalı!",
                                    "You need at least 10 items in your wardrobe for style selections!"
                                ),
                                color = VestiColors.TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text(
                                text = AppConfig.t(
                                    "Şu an ${wardrobeCount}/10 kıyafet var. ${10 - wardrobeCount} tane daha eklemen yeterli!",
                                    "You have ${wardrobeCount}/10 items. Add ${10 - wardrobeCount} more to unlock this!"
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = onNavigateToWardrobe,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary)
                            ) {
                                Text(AppConfig.t("Kıyafet Ekle", "Add Clothes"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Yeterli kıyafet var, Market'e yönlendir
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("✨", fontSize = 28.sp)
                            Text(
                                text = AppConfig.t(
                                    "Tarzına uygun seçimler seni bekliyor!",
                                    "Selections matching your style await you!"
                                ),
                                color = VestiColors.TextMain,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = onNavigateToMarket,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary)
                            ) {
                                Text(AppConfig.t("Keşfet", "Explore"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showNotificationsDialog) {
            NotificationsDialog(
                viewModel = notificationViewModel,
                onDismiss = { showNotificationsDialog = false },
                onNavigateToOutfit = onNavigateToOutfit,
                onNavigateToMarket = onNavigateToMarket
            )
        }
    }
}

@Composable
fun HomeHeader(
    userName: String,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 6..11 -> AppConfig.t("Günaydın", "Good Morning")
        in 12..17 -> AppConfig.t("Tünaydın", "Good Afternoon")
        in 18..22 -> AppConfig.t("İyi Akşamlar", "Good Evening")
        else -> AppConfig.t("İyi Geceler", "Good Night")
    }

    val initials = if (userName.length >= 2) {
        val parts = userName.split(" ")
        if (parts.size >= 2) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            userName.take(2).uppercase()
        }
    } else {
        "NB"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting,",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = userName,
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
                    .background(VestiColors.LightPurple)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = VestiColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

@Composable
fun WeatherCard(
    temp: Int,
    city: String,
    condition: String,
    tip: String,
    isSunny: Boolean
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable {
                val toastMsg = if (AppConfig.language == "tr") {
                    "${condition} havaya uygun kombin tavsiyeleri VesVes chatbotta hazır! 🌤️"
                } else {
                    "Outfit suggestions suitable for ${condition.lowercase()} weather are ready in VesVes chatbot! 🌤️"
                }
                android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
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
                        text = tip,
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
                            imageVector = if (isSunny) Icons.Default.WbSunny else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (isSunny) Color(0xFFFFF9C4) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "${temp}°",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            fontWeight = FontWeight.Light
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${city.uppercase()} • ${condition.uppercase()}",
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
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    text = AppConfig.t("BUGÜNÜN İLHAMI", "DAILY INSPIRATION"),
                    color = VestiColors.Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppConfig.t("Zamansız Vintage Ruhu", "Timeless Vintage Spirit"),
                    color = VestiColors.TextMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppConfig.t(
                        "Dolabındaki parçaları Retro esintilerle birleştirerek çaba gerektirmeyen şıklığı yakala.",
                        "Combine items in your closet with Retro vibes to capture effortless elegance."
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppConfig.t("Kombini Keşfet", "Explore Outfit"),
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
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = VestiColors.Primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsDialog(
    viewModel: NotificationViewModel,
    onDismiss: () -> Unit,
    onNavigateToOutfit: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.markAllAsRead()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .padding(horizontal = 24.dp, vertical = 8.dp)
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
                        text = AppConfig.t("Bildirimler", "Notifications"),
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
                        contentDescription = AppConfig.t("Kapat", "Close"),
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Notifications List
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                when (val currentState = state) {
                    is NotificationState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = VestiColors.Primary
                        )
                    }
                    is NotificationState.Error -> {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 14.sp
                        )
                    }
                    is NotificationState.Success -> {
                        val notifications = currentState.notifications
                        if (notifications.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = AppConfig.t("Henüz bildiriminiz yok.", "You don't have any notifications yet."),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(notifications) { item ->
                                    val isNew = !item.read
                                    val initials = if (item.title.length >= 2) item.title.substring(0, 2).uppercase() else "VS"
                                    NotificationRow(
                                        title = item.title,
                                        message = item.description,
                                        time = item.createdAt.split("T").firstOrNull() ?: "",
                                        isNew = isNew,
                                        avatarContent = {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (item.type == "message") VestiColors.Primary.copy(alpha = 0.15f)
                                                        else Color(0xFFFFF9C4)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (item.type == "message") {
                                                    Text(
                                                        text = initials,
                                                        color = VestiColors.Primary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFBC02D),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            onDismiss()
                                            if (item.type == "message") {
                                                onNavigateToMarket()
                                            } else {
                                                onNavigateToOutfit()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
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
    avatarContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isNew) VestiColors.LightPurple.copy(alpha = 0.4f) else Color(0xFFF9FAFB))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar on the left
        avatarContent()
        
        // Content on the right
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = time,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
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
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

private fun resolveColorName(name: String): Color {
    val clean = name.lowercase().trim()
    return when {
        clean.contains("siyah") || clean.contains("black") -> Color(0xFF1A1A1A)
        clean.contains("beyaz") || clean.contains("white") -> Color(0xFFFAFAFA)
        clean.contains("lacivert") || clean.contains("navy") -> Color(0xFF1A237E)
        clean.contains("mavi") || clean.contains("blue") -> Color(0xFF42A5F5)
        clean.contains("kırmızı") || clean.contains("red") -> Color(0xFFEF5350)
        clean.contains("yeşil") || clean.contains("green") -> Color(0xFF66BB6A)
        clean.contains("gri") || clean.contains("grey") || clean.contains("gray") || clean.contains("antrasit") || clean.contains("anthracite") -> Color(0xFF757575)
        clean.contains("krem") || clean.contains("cream") -> Color(0xFFFFFDD0)
        clean.contains("bej") || clean.contains("beige") || clean.contains("ekru") || clean.contains("ecru") -> Color(0xFFF5F5DC)
        clean.contains("sarı") || clean.contains("yellow") -> Color(0xFFFFEE58)
        clean.contains("turuncu") || clean.contains("orange") -> Color(0xFFFFA726)
        clean.contains("mor") || clean.contains("purple") || clean.contains("lila") || clean.contains("lilac") || clean.contains("indigo") -> Color(0xFF8E24AA)
        clean.contains("pembe") || clean.contains("pink") || clean.contains("pudra") -> Color(0xFFF06292)
        clean.contains("kahve") || clean.contains("brown") || clean.contains("taba") || clean.contains("tan") || clean.contains("camel") -> Color(0xFF8D6E63)
        clean.contains("haki") || clean.contains("khaki") -> Color(0xFF6B8E23)
        clean.contains("bordo") || clean.contains("burgundy") -> Color(0xFF800020)
        clean.contains("turkuaz") || clean.contains("turquoise") -> Color(0xFF00CED1)
        clean.contains("somon") || clean.contains("salmon") -> Color(0xFFFFA07A)
        clean.contains("mint") -> Color(0xFF98FF98)
        clean.contains("vişne") -> Color(0xFF800000)
        clean.contains("şeftali") || clean.contains("peach") -> Color(0xFFFFDAB9)
        clean.contains("altın") || clean.contains("gold") -> Color(0xFFFFD700)
        clean.contains("gümüş") || clean.contains("silver") -> Color(0xFFC0C0C0)
        else -> VestiColors.Primary
    }
}
