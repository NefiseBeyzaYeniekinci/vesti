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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import com.vesti.app.ui.theme.VestiColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String,
    val text: String,
    val isMe: Boolean,
    val timestamp: String
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OutfitScreen(viewModel: OutfitViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION)

    // Otomatik olarak ekran açıldığında bir öneri iste
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (state is OutfitState.Idle) {
            if (locationPermissionState.status.isGranted) {
                fetchLocationAndRecommend(context, viewModel)
            } else {
                viewModel.getRecommendation()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Chatbot States
    var activeTab by remember { mutableStateOf("öneri") } // "öneri" or "sohbet"
    var chatInputText by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf<ChatMessage>(
            ChatMessage("1", "Merhaba! Ben Vesti AI Stil Asistanın. Bugün sana tarzın, gardırobundaki kıyafetler veya kombin önerileri hakkında nasıl yardımcı olabilirim? 🤖✨", false, "09:00")
        )
    }
    var isAiTyping by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    val sendMessage: (String) -> Unit = { text ->
        val userText = text.trim()
        if (userText.isNotEmpty()) {
            val now = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
            chatMessages.add(ChatMessage(java.util.UUID.randomUUID().toString(), userText, true, now))
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
                val aiResponse = when {
                    lower.contains("mülakat") || lower.contains("is") || lower.contains("iş") -> {
                        "Mülakatlar için her zaman profesyonel ve sade bir duruş sergilemek önemlidir. Gardırobundaki Siyah Blazer Ceket ile Beyaz Gömleği kombinleyebilirsin. Altına kumaş pantolon ve deri ayakkabı ekleyerek mükemmel bir ilk izlenim yaratabilirsin! 💼✨"
                    }
                    lower.contains("yağmur") || lower.contains("havalar") || lower.contains("soğuk") -> {
                        "Yağmurlu ve kapalı havalarda hem şık hem de korunaklı olmak için gardırobundaki Deri Ceket ile Siyah Kot Pantolonu tercih edebilirsin. Altına sneaker yerine bot veya su geçirmez bir ayakkabı eklemek harika olacaktır! 🌧️🧥"
                    }
                    lower.contains("sokak") || lower.contains("retro") || lower.contains("vintage") -> {
                        "Harika bir sokak stili için gardırobundaki Vintage Deri Ceketi, geniş kesim bir kot pantolon ve basic beyaz tişört ile eşleştirebilirsin. Birkaç minimal aksesuar ekleyerek retro esintili mükemmel bir sokak kombini oluşturabilirsin! 🕺👟"
                    }
                    lower.contains("bugün") || lower.contains("giy") || lower.contains("öner") -> {
                        "Bugün hava oldukça güzel! Gardırobundaki Beyaz Tişört ile Mavi Kot Pantolonu kombinleyerek rahat ve spor-şık bir gün geçirebilirsin. Üzerine ince bir bomber ceket de alabilirsin! ☀️✨"
                    }
                    else -> {
                        "Tarzın hakkında harika bir soru! Gardırobundaki parçaları daha yakından inceledim. Sana spor-şık bir hava katması için Beyaz Tişört ile Mavi Kot Pantolonu birleştirmeyi öneriyorum. Üzerine alacağın deri ceket veya trençkot stiline mükemmel bir son dokunuş yapacaktır! 🌟"
                    }
                }
                
                val aiNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                chatMessages.add(ChatMessage(java.util.UUID.randomUUID().toString(), aiResponse, false, aiNow))
                
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
        // 1. Apple-style Segmented Switcher Capsule
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3F4F6))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeTab == "öneri") Color.White else Color.Transparent)
                    .clickable { activeTab = "öneri" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✨ Kombin Önerisi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (activeTab == "öneri") VestiColors.Primary else Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeTab == "sohbet") Color.White else Color.Transparent)
                    .clickable { activeTab = "sohbet" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖 AI Stil Sohbet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (activeTab == "sohbet") VestiColors.Primary else Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Tab Content Switcher
        if (activeTab == "öneri") {
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (val currentState = state) {
                    is OutfitState.Idle -> { }
                    is OutfitState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            com.vesti.app.ui.wardrobe.HangerLoadingAnimation(
                                title = "AI Kombin Hazırlıyor",
                                subtitle = "Dijital gardırobunuz inceleniyor..."
                            )
                        }
                    }
                    is OutfitState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = currentState.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getRecommendation() }) {
                                Text("Yeniden Dene")
                            }
                        }
                    }
                    is OutfitState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Premium Editorial AI Banner Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = VestiColors.Primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Vesti AI Kombini",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = VestiColors.Primary,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = currentState.recommendation.description,
                                        color = VestiColors.TextMain,
                                        fontSize = 14.sp,
                                        lineHeight = 21.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = "Önerilen Parçalar",
                                fontWeight = FontWeight.Bold,
                                color = VestiColors.TextMain,
                                fontSize = 16.sp,
                                letterSpacing = (-0.3).sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // High-Fashion 2-Column Grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                items(currentState.recommendation.items) { item ->
                                    OutfitItemCard(item = item)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Luxurious Floating Action Button
                            Button(
                                onClick = { viewModel.getRecommendation() },
                                colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .padding(bottom = 4.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                    Text(
                                        text = "Farklı Bir Öneri Getir",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Chatbot Content!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Horizontal Quick-Action Chips
                val quickChips = listOf(
                    "☀️ Bugün ne giymeliyim?",
                    "💼 Mülakat kombini öner",
                    "🌧️ Yağmurlu gün stili",
                    "🕺 Sokak stili tarzı"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatMessages) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start
                        ) {
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
                                        text = "Vesti AI Asistanı",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VestiColors.Primary
                                    )
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
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
                                    text = msg.text,
                                    color = if (msg.isMe) Color.White else VestiColors.TextMain,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
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
                                            text = "Vesti AI yazıyor",
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

                Spacer(modifier = Modifier.height(8.dp))

                // Input Bar
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
                        placeholder = { Text("Asistana tarzını sor...", fontSize = 13.sp) },
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
                            contentDescription = "Gönder",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitItemCard(item: WardrobeItemDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val fullImageUrl = if (item.imageUrl.startsWith("http")) item.imageUrl else "http://192.168.1.103:8080${item.imageUrl}"
            
            // Image Content
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
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Column {
                    Text(
                        text = item.category,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.color,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationAndRecommend(context: android.content.Context, viewModel: OutfitViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            viewModel.fetchRecommendationWithLocation(
                lat = location.latitude,
                lon = location.longitude
            )
        } else {
            // Konum bulunamazsa generic çağır
            viewModel.getRecommendation()
        }
    }.addOnFailureListener {
        viewModel.getRecommendation()
    }
}
