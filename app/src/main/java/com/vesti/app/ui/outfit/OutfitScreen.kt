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
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val timestamp: String,
    val suggestedItems: List<WardrobeItemDto>? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun OutfitScreen(viewModel: OutfitViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadWardrobeItems()
    }
    val realWardrobeItems by viewModel.wardrobeItems.collectAsStateWithLifecycle()

    fun findItemsForCategory(items: List<WardrobeItemDto>, keywords: List<String>): List<WardrobeItemDto> {
        return items.filter { item ->
            keywords.any { keyword ->
                item.category.lowercase().contains(keyword) || 
                item.brand.lowercase().contains(keyword) ||
                item.color.lowercase().contains(keyword)
            }
        }
    }

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
                val location = fusedLocationClient.lastLocation.await()
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
                    val isEn = AppConfig.language == "en"
                    when {
                        temp < 10 -> if (isEn) "cold ($temp°C)" else "soğuk ($temp°C)"
                        temp < 18 -> if (isEn) "cool ($temp°C)" else "serin ($temp°C)"
                        temp < 25 -> if (isEn) "mild ($temp°C)" else "ılık ($temp°C)"
                        else -> if (isEn) "warm ($temp°C)" else "sıcak ($temp°C)"
                    }
                } else null

                val aiResponse: String
                val itemsToSuggest: List<WardrobeItemDto>?
                
                when {
                    lower.contains("satmalı mıyım") || lower.contains("satmali miyim") || lower.contains("satayım mı") || lower.contains("satsam mı") || lower.contains("satmalı") || lower.contains("satsam mi") || lower.contains("should i sell") || lower.contains("sell") -> {
                        val matchedItem = realWardrobeItems.firstOrNull { item ->
                            lower.contains(item.category.lowercase()) || lower.contains(item.brand.lowercase())
                        }
                        
                        if (matchedItem != null) {
                            aiResponse = if (AppConfig.language == "en") {
                                "I see your **${matchedItem.brand} ${matchedItem.category}** (${matchedItem.color}) in your wardrobe. " +
                                if (matchedItem.category.lowercase().contains("tişört") || matchedItem.category.lowercase().contains("t-shirt") || matchedItem.category.lowercase().contains("jeans") || matchedItem.category.lowercase().contains("pantolon")) {
                                    "This item is a basic, timeless piece. It's key to your daily combinations, so I suggest keeping it! But if you want to refresh your style, you can sell it on Vesti and replace it with a new item from the Marketplace! 💸"
                                } else {
                                    "If you haven't worn this jacket/item in the last 6 months, selling it on Vesti is a great way to earn balance! You can replace it with other popular items in the Marketplace. 💸"
                                }
                            } else {
                                "Dolabındaki **${matchedItem.brand} ${matchedItem.category}** (${matchedItem.color}) ürününü görüyorum. " +
                                if (matchedItem.category.lowercase().contains("tişört") || matchedItem.category.lowercase().contains("t-shirt") || matchedItem.category.lowercase().contains("jeans") || matchedItem.category.lowercase().contains("pantolon")) {
                                    "Bu parça dolabının kilit/temel parçalarından birisi. Günlük kombinlerin vazgeçilmezidir, bu yüzden bence satmamalısın. Ancak tarzını yenilemek istersen satıp Vesti Marketplace'teki yeni alternatiflerle yerini doldurabilirsin! 💸"
                                } else {
                                    "Eğer bu parçayı son 6 aydır pek giymediysen satebilirsin! Satıp elde edeceğin kazançla Marketplace'ten dolabındaki bu parçanın yerini dolduracak yeni trend parçalar alabilirsin. 💸"
                                }
                            }
                            itemsToSuggest = listOf(matchedItem)
                        } else {
                            aiResponse = if (AppConfig.language == "en") {
                                "Deciding whether to sell your clothes? Here's my advice:\n" +
                                "- If it's a key piece (like a black blazer, quality jeans, or basic t-shirt), you should keep it.\n" +
                                "- If you haven't worn it in 6 months, it's time to sell it on Vesti!\n" +
                                "Selling helps you earn balance, and you can easily find items in the Marketplace to replace it and refresh your wardrobe! 💫"
                            } else {
                                "Kıyafetlerini satıp satmamak konusunda kararsızsan:\n" +
                                "- Eğer dolabındaki kilit ve zamansız parçalardan biriyse (siyah blazer ceket, kaliteli kot pantolon veya basic beyaz tişört), bence satmamalısın.\n" +
                                "- Ancak son 6 aydır hiç giymediysen satabilirsin! Vesti'de satarak bakiye kazanabilir ve Marketplace'teki diğer parçalarla yerini doldurabilirsin. Dolabı tazelemek her zaman iyi hissettirir! 💫"
                            }
                            itemsToSuggest = null
                        }
                    }
                    lower.contains("spor") || lower.contains("koş") || lower.contains("gym") || lower.contains("egzersiz") || lower.contains("sport") -> {
                        val matches = findItemsForCategory(realWardrobeItems, listOf("tişört", "t-shirt", "şort", "tayt", "sneaker", "ayakkabı", "spor"))
                        aiResponse = if (AppConfig.language == "en") {
                            "For a workout, I suggest wearing lightweight and breathable fabrics. A comfortable t-shirt and shorts/leggings with sneakers is a perfect choice! 👟" + 
                            (if (matches.isNotEmpty()) " Here is what I recommend from your wardrobe:" else " (You don't have matching activewear in your wardrobe yet. Explore the Marketplace to find some!)")
                        } else {
                            "Spor yaparken nefes alan ve esnek kumaşlar tercih etmelisin. Rahat bir tişört, şort/tayt ve spor ayakkabı harika bir seçim olacaktır! 👟" + 
                            (if (matches.isNotEmpty()) " Dolabından sana uygun spor parçaları:" else " (Dolabında buna uygun spor kıyafeti bulamadım. Marketplace'e göz atarak yeni parçalar keşfedebilirsin!)")
                        }
                        itemsToSuggest = if (matches.isNotEmpty()) matches.take(3) else null
                    }
                    lower.contains("mülakat") || lower.contains("iş görüşme") || lower.contains("is") || lower.contains("iş") || lower.contains("interview") -> {
                        val matches = findItemsForCategory(realWardrobeItems, listOf("gömlek", "shirt", "blazer", "ceket", "pantolon", "jeans", "takım"))
                        aiResponse = if (AppConfig.language == "en") {
                            "For an interview, presenting a professional and clean look is essential. A neat shirt paired with classic trousers or a blazer will build great confidence. 💼" + 
                            (if (matches.isNotEmpty()) " Here is your power look:" else " (You don't have business formal clothes in your wardrobe yet. Explore the Marketplace to find some!)")
                        } else {
                            "İş görüşmesi veya mülakatlar için profesyonel, temiz ve sade bir duruş sergilemek önemlidir. Klasik bir gömlek, kumaş pantolon veya şık bir blazer ceket güven verecektir. 💼" + 
                            (if (matches.isNotEmpty()) " İşte dolabından seçtiğim profesyonel parçalar:" else " (Dolabında klasik mülakat kıyafeti bulamadım. Yeni parçalar için Marketplace'i ziyaret edebilirsin!)")
                        }
                        itemsToSuggest = if (matches.isNotEmpty()) matches.take(3) else null
                    }
                    lower.contains("yağmur") || lower.contains("yağmurlu") || lower.contains("soğuk") || lower.contains("rain") || lower.contains("cold") -> {
                        val matches = findItemsForCategory(realWardrobeItems, listOf("kaban", "mont", "ceket", "deri ceket", "yağmurluk", "bot", "boot", "pantolon"))
                        aiResponse = if (AppConfig.language == "en") {
                            "For rainy or cold days, layering is key. A water-resistant coat or leather jacket combined with dark jeans will keep you warm and stylish. 🌧️🧥" + 
                            (if (matches.isNotEmpty()) " Here are the best options from your wardrobe:" else " (No heavy coats or rainwear found in your wardrobe yet. Check Marketplace to stay warm!)")
                        } else {
                            "Yağmurlu ve soğuk havalarda katmanlı giyinmek önemlidir. Su geçirmeyen bir mont veya deri ceket ile koyu renk kot pantolon kombinasyonu hem sıcak tutar hem de şık gösterir. 🌧️🧥" + 
                            (if (matches.isNotEmpty()) " Dolabından seçtiğim uygun parçalar:" else " (Dolabında kalın mont veya yağmurluk bulamadım. Marketplace'teki kışlık ürünlere göz atabilirsin!)")
                        }
                        itemsToSuggest = if (matches.isNotEmpty()) matches.take(3) else null
                    }
                    lower.contains("sokak") || lower.contains("retro") || lower.contains("vintage") || lower.contains("street") -> {
                        val matches = findItemsForCategory(realWardrobeItems, listOf("deri ceket", "tişört", "t-shirt", "jeans", "pantolon", "vintage", "retro", "sweatshirt", "kapüşonlu"))
                        aiResponse = if (AppConfig.language == "en") {
                            "Street style is all about comfort and expression! Loose jeans, a cool graphic t-shirt, and sneakers create an effortless, retro vibe. 🕺👟" + 
                            (if (matches.isNotEmpty()) " Here is what I found in your wardrobe:" else " (No street style items found in your wardrobe. Check Marketplace for vintage items!)")
                        } else {
                            "Sokak stili tamamen rahatlık ve kendini ifade etmekle ilgilidir! Bol kesim bir kot pantolon, tişört ve sneaker spor ayakkabı çabasız ve havalı bir görünüm sunar. 🕺👟" + 
                            (if (matches.isNotEmpty()) " Dolabından senin için seçtiklerim:" else " (Dolabında sokak stiline uygun retro parça bulamadım. İlham almak için Marketplace'e bakabilirsin!)")
                        }
                        itemsToSuggest = if (matches.isNotEmpty()) matches.take(3) else null
                    }
                    lower.contains("bugün") || lower.contains("giy") || lower.contains("öner") || lower.contains("today") || lower.contains("wear") || lower.contains("ne giysem") -> {
                        val matches = if (temp != null && temp < 15) {
                            findItemsForCategory(realWardrobeItems, listOf("kaban", "mont", "ceket", "blazer", "gömlek", "shirt", "pantolon", "jeans"))
                        } else {
                            findItemsForCategory(realWardrobeItems, listOf("tişört", "t-shirt", "pantolon", "jeans", "sneaker", "ayakkabı", "şort"))
                        }
                        aiResponse = if (AppConfig.language == "en") {
                            if (weatherInfo != null) {
                                "Today is $weatherInfo! ${when {
                                    temp != null && temp < 10 -> "I recommend warm layers, a coat, and thick trousers."
                                    temp != null && temp < 18 -> "A light jacket or blazer over a shirt would be perfect."
                                    temp != null && temp < 25 -> "Light fabrics and comfortable pieces are great for today."
                                    else -> "It's warm! A breathable t-shirt and comfortable pants will keep you cool."
                                }}" + (if (matches.isNotEmpty()) " Here is a suggestion from your wardrobe:" else " (Your wardrobe seems empty for this weather. Check out the Marketplace!)")
                            } else {
                                "It's a beautiful day! Light fabrics and comfortable casual pieces are perfect." + 
                                (if (matches.isNotEmpty()) " Here is what I suggest from your wardrobe:" else " (No items found in your wardrobe. Check out the Marketplace!)")
                            }
                        } else {
                            if (weatherInfo != null) {
                                "Bugün hava $weatherInfo! ${when {
                                    temp != null && temp < 10 -> "Katmanlı giyinmeni, kalın bir kaban ve pantolon tercih etmeni öneririm."
                                    temp != null && temp < 18 -> "Gömlek üzerine ince bir ceket veya blazer harika olur."
                                    temp != null && temp < 25 -> "İnce kumaşlar ve rahat kıyafetler gün boyu harika hissettirecektir."
                                    else -> "Sıcak bir gün! İnce bir tişört ve rahat pantolon tercih edebilirsin."
                                }}" + (if (matches.isNotEmpty()) " Dolabından senin için seçtiğim kombin:" else " (Dolabında bu hava durumuna uygun parça bulamadım. Marketplace'e bakabilirsin!)")
                            } else {
                                "Bugün hava oldukça güzel! Rahat kumaşlar ve hafif kombinler tercih edebilirsin." + 
                                (if (matches.isNotEmpty()) " Dolabından önerim:" else " (Dolabında uygun parça bulamadım. Marketplace'e bakabilirsin!)")
                            }
                        }
                        itemsToSuggest = if (matches.isNotEmpty()) matches.take(3) else null
                    }
                    else -> {
                        aiResponse = if (AppConfig.language == "en") {
                            "Great question! I am here to help you manage your style. You can ask me what to wear, ask about selling your items, or request outfit ideas! 🌟"
                        } else {
                            "Harika bir soru! Stilini yönetmene yardımcı olmak için buradayım. Bana ne giymen gerektiğini sorabilir, dolabındaki parçaları satıp satmaman konusunda danışabilir veya kombin fikirleri isteyebilirsin! 🌟"
                        }
                        itemsToSuggest = null
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(com.vesti.app.AppConfig.resolveImageSource(item.imageUrl))
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
