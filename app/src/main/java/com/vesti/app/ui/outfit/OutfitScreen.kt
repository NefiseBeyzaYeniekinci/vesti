package com.vesti.app.ui.outfit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.vesti.app.data.network.WardrobeItemDto
import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val timestamp: String,
    val suggestedItems: List<WardrobeItemDto>? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OutfitScreen(viewModel: OutfitViewModel) {
    // Mock Clothing Items to Recommend Conversational Style
    val mockTshirt = WardrobeItemDto("1", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab", "Tişört", "Beyaz", "Zara", "M", "2024-05-15")
    val mockJeans = WardrobeItemDto("2", "https://images.unsplash.com/photo-1542272604-787c3835535d", "Kot Pantolon", "Mavi", "Mavi", "32", "2024-05-14")
    val mockSneaker = WardrobeItemDto("5", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2", "Sneaker", "Beyaz", "Nike", "42", "2024-05-10")
    val mockBlazer = WardrobeItemDto("3", "https://images.unsplash.com/photo-1591047139829-d91aecb6caea", "Blazer Ceket", "Siyah", "Mango", "S", "2024-05-13")
    val mockShirt = WardrobeItemDto("4", "https://images.unsplash.com/photo-1596755094514-f87e34085b2c", "Gömlek", "Beyaz", "Zara", "M", "2024-05-12")
    val mockLeatherJacket = WardrobeItemDto("6", "https://images.unsplash.com/photo-1551028719-00167b16eac5", "Deri Ceket", "Siyah", "Diesel", "L", "2024-05-09")
    val mockBlackJeans = WardrobeItemDto("7", "https://images.unsplash.com/photo-1541099649105-f69ad21f3246", "Kot Pantolon", "Siyah", "Zara", "31", "2024-05-08")

    val chatMessages = remember {
        mutableStateListOf<ChatMessage>(
            ChatMessage("1", "GREETING_MSG", false, "09:00")
        )
    }
    
    var chatInputText by remember { mutableStateOf("") }
    var isAiTyping by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Hava durumu durumu
    var currentTemp by remember { mutableStateOf<Int?>(null) }
    var currentWeatherDesc by remember { mutableStateOf<String?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Konum izni varsa hava durumunu çek
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            locationPermissionGranted = true
            try {
                @SuppressLint("MissingPermission")
                val location = kotlinx.coroutines.tasks.await(fusedLocationClient.lastLocation)
                if (location != null) {
                    try {
                        val weatherResp = viewModel.getWeatherForChat(location.latitude, location.longitude)
                        if (weatherResp != null) {
                            currentTemp = Math.round(weatherResp.main.temp).toInt()
                            currentWeatherDesc = weatherResp.weather.firstOrNull()?.description
                        }
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }

    val sendMessage: (String) -> Unit = { text ->
        val userText = text.trim()
        if (userText.isNotEmpty()) {
            val now = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            chatMessages.add(ChatMessage(UUID.randomUUID().toString(), userText, true, now))
            if (chatInputText.isNotBlank() && userText == chatInputText.trim()) {
                chatInputText = ""
            }
            
            // Auto scroll to bottom
            coroutineScope.launch {
                delay(100)
                listState.animateScrollToItem(chatMessages.size - 1)
            }
            
            // Simulate AI typing response
            isAiTyping = true
            coroutineScope.launch {
                delay(1500) // 1.5 seconds typing simulation
                isAiTyping = false
                
                val lower = userText.lowercase(java.util.Locale.getDefault())
                val temp = currentTemp
                val weatherDesc = currentWeatherDesc
                
                // Hava durumu bilgisini string olarak formatla
                val weatherInfo = if (temp != null) {
                    when {
                        temp < 10 -> AppConfig.t("soğuk ($temp°C)", "cold ($temp°C)")
                        temp < 18 -> AppConfig.t("serin ($temp°C)", "cool ($temp°C)")
                        temp < 25 -> AppConfig.t("ılık ($temp°C)", "mild ($temp°C)")
                        else -> AppConfig.t("sıcak ($temp°C)", "warm ($temp°C)")
                    }
                } else null

                val aiResponse: String
                val itemsToSuggest: List<WardrobeItemDto>?
                
                when {
                    lower.contains("spor") || lower.contains("koş") || lower.contains("gym") || lower.contains("egzersiz") || lower.contains("sport") -> {
                        aiResponse = if (AppConfig.language == "en") {
                            if (weatherInfo != null)
                                "For your workout in $weatherInfo weather, I suggest lightweight and breathable clothing. ${if (temp != null && temp < 15) "Don't forget a light jacket!" else "A t-shirt and shorts will be perfect! ☀️"} Here's a sporty combo from your wardrobe:"
                            else
                                "For your workout, breathable fabrics are key! A t-shirt with shorts and sneakers is the perfect sport combo. Here's what I recommend from your wardrobe:"
                        } else {
                            if (weatherInfo != null)
                                "Spor için $weatherInfo havada nefes alan kumaşlar tercih etmelisin. ${if (temp != null && temp < 15) "İnce bir ceket almayı unutma! 🧥" else "Tişört ve şort mükemmel bir tercih olacak! ☀️"} Dolabından sana harika bir spor kombini hazırladım:"
                            else
                                "Spor için nefes alan kumaşlar çok önemli! Tişört, şort ve spor ayakkabı harika bir spor kombini yapar. Dolabından önerdiklerim:"
                        }
                        itemsToSuggest = listOf(mockTshirt, mockJeans, mockSneaker)
                    }
                    lower.contains("mülakat") || lower.contains("iş görüşme") || lower.contains("is") || lower.contains("iş") || lower.contains("interview") -> {
                        aiResponse = if (AppConfig.language == "en") {
                            if (weatherInfo != null)
                                "For your interview in $weatherInfo weather, I've designed a professional and confident outfit. ${if (temp != null && temp > 22) "I went for lighter fabrics given the heat." else ""} Here's your power look:"
                            else
                                "For interviews, presenting a professional and clean look is essential. I've designed a confident and respectable outfit for you from your wardrobe: 💼✨"
                        } else {
                            if (weatherInfo != null)
                                "Mülakat için $weatherInfo havada profesyonel ve kendinden emin bir kombin hazırladım. ${if (temp != null && temp > 22) "Sıcaktan dolayı daha hafif kumaşlar seçtim." else ""} İşte senin için güçlü bir görünüm: 💼✨"
                            else
                                "Mülakatlar için profesyonel ve sade bir duruş sergilemek önemlidir. İşte güçlü kombinin: 💼✨"
                        }
                        itemsToSuggest = listOf(mockShirt, mockBlazer, mockBlackJeans)
                    }
                    lower.contains("yağmur") || lower.contains("yağmurlu") || lower.contains("soğuk") || lower.contains("rain") || lower.contains("cold") -> {
                        aiResponse = if (AppConfig.language == "en") {
                            "To stay stylish and protected in rainy or cold weather, I've prepared a layered, leather-focused combination. These pieces will keep you sharp: 🌧️🧥"
                        } else {
                            "Yağmurlu ve soğuk havalarda hem şık hem de korunaklı olmak için katmanlı bir kombin hazırladım. İşte önerim: 🌧️🧥"
                        }
                        itemsToSuggest = listOf(mockLeatherJacket, mockBlackJeans, mockSneaker)
                    }
                    lower.contains("sokak") || lower.contains("retro") || lower.contains("vintage") || lower.contains("street") -> {
                        aiResponse = if (AppConfig.language == "en") {
                            "For a great vintage street style, I paired a retro leather jacket and a basic t-shirt. Completed with loose-fit jeans for an effortless cool vibe! 🕺👟"
                        } else {
                            "Harika bir vintage sokak stili için retro deri ceket ve basic tişört birlikteliğinden yararlandım! 🕺👟"
                        }
                        itemsToSuggest = listOf(mockLeatherJacket, mockTshirt, mockJeans)
                    }
                    lower.contains("bugün") || lower.contains("giy") || lower.contains("öner") || lower.contains("today") || lower.contains("wear") || lower.contains("ne giysem") -> {
                        aiResponse = if (AppConfig.language == "en") {
                            if (weatherInfo != null)
                                "Today is $weatherInfo! ${when {
                                    temp != null && temp < 10 -> "Definitely go for layered warm clothing. A coat and thick pants are a must."
                                    temp != null && temp < 18 -> "A light jacket over a shirt would be perfect. Not too cold, not too warm!"
                                    temp != null && temp < 25 -> "Lovely weather! Light fabrics and comfortable clothes will keep you feeling great all day."
                                    else -> "It's hot! Breathable fabrics, a light t-shirt and comfortable pants are the way to go."
                                }} ☀️✨"
                            else
                                "Today is lovely! I've prepared a great outfit matching the spring breeze. ☀️✨"
                        } else {
                            if (weatherInfo != null)
                                "Bugün hava $weatherInfo! ${when {
                                    temp != null && temp < 10 -> "Kesinlikle katmanlı ve sıcak giysiler tercih etmelisin. Kaban ve kalın pantolon şart."
                                    temp != null && temp < 18 -> "Gömlek üzerine ince bir ceket harika olur. Ne çok soğuk ne çok sıcak!"
                                    temp != null && temp < 25 -> "Mükemmel bir hava! İnce kumaşlar ve rahat kıyafetler seni gün boyu iyi hissettiriri."
                                    else -> "Sıcak bir gün! Nefes alan kumaşlar, ince tişört ve rahat pantolon tam sana göre."
                                }} ☀️✨"
                            else
                                "Bugün hava oldukça güzel! Rahat kumaşlar ve açık renkler tercih ettim. ☀️✨"
                        }
                        itemsToSuggest = if (temp != null && temp < 15) {
                            listOf(mockBlazer, mockShirt, mockBlackJeans)
                        } else {
                            listOf(mockTshirt, mockJeans, mockSneaker)
                        }
                    }
                    else -> {
                        aiResponse = if (AppConfig.language == "en") {
                            if (weatherInfo != null)
                                "Great question! With $weatherInfo weather today, I've put together a stylish outfit from your wardrobe. Here's what I recommend: 🌟"
                            else
                                "Great question about your style! I looked closely at your wardrobe. I suggest pairing a White T-shirt with Blue Jeans for a casual-smart look. A leather jacket on top is the perfect finishing touch! 🌟"
                        } else {
                            if (weatherInfo != null)
                                "Harika bir soru! Bugün hava $weatherInfo, dolabındaki parçaları buna göre inceledim. İşte önerim: 🌟"
                            else
                                "Tarzın hakkında harika bir soru! Beyaz Tişört ile Mavi Kot'u birleştirmeyi öneriyorum. Üzerine deri ceket stiline mükemmel bir son dokunuş yapacak! 🌟"
                        }
                        itemsToSuggest = listOf(mockTshirt, mockJeans, mockLeatherJacket)
                    }
                }
                
                val aiNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                chatMessages.add(ChatMessage(UUID.randomUUID().toString(), aiResponse, false, aiNow, itemsToSuggest))
                
                // Scroll to bottom after AI response
                delay(100)
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Centered high-fashion minimalist title header for VesVes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VesVes",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                color = VestiColors.TextMain,
                letterSpacing = (-0.8).sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Minimal glowing online green dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Quick-Action Chips
        val quickChips = listOf(
            AppConfig.t("☀️ Bugün ne giymeliyim?", "☀️ What should I wear today?"),
            AppConfig.t("💼 Mülakat kombini öner", "💼 Suggest an interview outfit"),
            AppConfig.t("🌧️ Yağmurlu gün stili", "🌧️ Rainy day style"),
            AppConfig.t("🕺 Sokak stili tarzı", "🕺 Street style look")
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            items(quickChips) { chipText ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(VestiColors.LightPurple)
                        .clickable { sendMessage(chipText) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = chipText,
                        color = VestiColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Messages Conversation Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(chatMessages) { msg ->
                val isInitialGreeting = msg.id == "1" && msg.text == "GREETING_MSG"
                val rawMsgText = if (isInitialGreeting) {
                    AppConfig.t(
                        "Merhaba! Ben VesVes. Bugün sana tarzın, gardırobundaki kıyafetler veya kombin önerileri hakkında nasıl yardımcı olabilirim? 🤖✨",
                        "Hello! I am VesVes. How can I help you today with your style, clothes in your wardrobe, or outfit suggestions? 🤖✨"
                    )
                } else {
                    msg.text
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start
                ) {
                    // AI title badge
                    if (!msg.isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = VestiColors.Primary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VesVes",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VestiColors.Primary
                            )
                        }
                    }
                    
                    // Chat bubble
                    Box(
                        modifier = Modifier
                            .widthIn(max = 290.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (msg.isMe) 16.dp else 4.dp,
                                    bottomEnd = if (msg.isMe) 4.dp else 16.dp
                                )
                            )
                            .background(if (msg.isMe) VestiColors.Primary else Color(0xFFF3F4F6))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = rawMsgText,
                            color = if (msg.isMe) Color.White else VestiColors.TextMain,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Embedded suggested items if present!
                    if (!msg.isMe && msg.suggestedItems != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = AppConfig.t("Önerilen Kombin Parçaları:", "Suggested Outfit Items:"),
                                fontWeight = FontWeight.Bold,
                                color = VestiColors.TextMain,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(msg.suggestedItems) { item ->
                                    SuggestedItemCard(item = item)
                                }
                            }
                        }
                    }
                    
                    // Time indicator
                    Text(
                        text = msg.timestamp,
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(
                            start = if (msg.isMe) 0.dp else 8.dp,
                            end = if (msg.isMe) 8.dp else 0.dp,
                            top = 2.dp
                        )
                    )
                }
            }
            
            // typing animation loading
            if (isAiTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppConfig.t("VesVes yazıyor", "VesVes is typing"),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 2.dp,
                                    color = VestiColors.Primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input bar at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInputText,
                onValueChange = { chatInputText = it },
                placeholder = { Text(AppConfig.t("VesVes'e tarzını sor...", "Ask VesVes about your style..."), fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VestiColors.Primary,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )
            
            IconButton(
                onClick = {
                    if (chatInputText.isNotBlank()) {
                        sendMessage(chatInputText)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(VestiColors.Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = AppConfig.t("Gönder", "Send"),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SuggestedItemCard(item: WardrobeItemDto) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(170.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val fullImageUrl = if (item.imageUrl.startsWith("http")) item.imageUrl else "http://${com.vesti.app.data.network.RetrofitClient.HOST_IP}:3000${item.imageUrl}"
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fullImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.category,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Bottom Info Overlay with soft gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = AppConfig.t(
                            when (item.category.lowercase()) {
                                "tişört" -> "Tişört"
                                "kot pantolon" -> "Kot Pantolon"
                                "sneaker" -> "Sneaker"
                                "blazer ceket" -> "Blazer Ceket"
                                "gömlek" -> "Gömlek"
                                "deri ceket" -> "Deri Ceket"
                                else -> item.category
                            },
                            when (item.category.lowercase()) {
                                "tişört" -> "T-Shirt"
                                "kot pantolon" -> "Jeans"
                                "sneaker" -> "Sneaker"
                                "blazer ceket" -> "Blazer Jacket"
                                "gömlek" -> "Shirt"
                                "deri ceket" -> "Leather Jacket"
                                else -> item.category
                            }
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = AppConfig.t(
                            when (item.color.lowercase()) {
                                "beyaz" -> "Beyaz"
                                "siyah" -> "Siyah"
                                "mavi" -> "Mavi"
                                else -> item.color
                            },
                            when (item.color.lowercase()) {
                                "beyaz" -> "White"
                                "siyah" -> "Black"
                                "mavi" -> "Blue"
                                else -> item.color
                            }
                        ),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
