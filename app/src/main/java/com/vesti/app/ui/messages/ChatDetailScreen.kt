package com.vesti.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors

data class ChatMessageDto(val text: String, val time: String, val isMe: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    userId: String,
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    // Dynamic resolution based on userId
    val name: String
    val product: String
    val initials: String
    val messages: List<ChatMessageDto>

    when (userId) {
        "aysek" -> {
            name = "Ayşe K."
            product = AppConfig.t("Zara Keten Blazer - Bej", "Zara Linen Blazer - Beige")
            initials = "AK"
            messages = listOf(
                ChatMessageDto(AppConfig.t("Merhaba! İlan hâlâ satılıkta mı?", "Hello! Is this listing still for sale?"), "17:44 ✓✓", true),
                ChatMessageDto(AppConfig.t("Merhaba, evet satılıkta! Nasıl yardımcı olabilirim?", "Hello, yes it is! How can I help you?"), "17:54", false),
                ChatMessageDto(AppConfig.t("Evet, hâlâ satılıkta. Fiyatta anlaşabiliriz \uD83D\uDE0A", "Yes, still for sale. We can negotiate the price \uD83D\uDE0A"), "18:04", false)
            )
        }
        "elift" -> {
            name = "Elif T."
            product = AppConfig.t("Mango Midi Etek - Siyah", "Mango Midi Skirt - Black")
            initials = "ET"
            messages = listOf(
                ChatMessageDto(AppConfig.t("Merhaba, etek için takas düşünür müsün?", "Hello, would you consider a swap for the skirt?"), "14:10 ✓✓", true),
                ChatMessageDto(AppConfig.t("Ürün temiz mi, herhangi bir yıpranma var mı?", "Is the item clean, any wear and tear?"), "14:11 ✓✓", true),
                ChatMessageDto(AppConfig.t("Etek çok temiz, sadece 1 kez giyildi. Takas teklifiniz nedir?", "The skirt is very clean, only worn once. What is your swap offer?"), "14:30", false),
                ChatMessageDto(AppConfig.t("Sen: Kargo bilgilerini paylaşır mısın?", "You: Could you share the shipping details?"), "15:20 ✓✓", true)
            )
        }
        else -> {
            name = "Zeynep A."
            product = AppConfig.t("H&M Denim Ceket", "H&M Denim Jacket")
            initials = "ZA"
            messages = listOf(
                ChatMessageDto(AppConfig.t("Ceket için 200 TL teklif ediyorum.", "I offer 200 TL for the jacket."), "Dün ✓✓", true),
                ChatMessageDto(AppConfig.t("Takas teklifi için teşekkürler ama geçiyorum \uD83D\uDE4F", "Thanks for the swap offer, but I'll pass \uD83D\uDE4F"), "Dün", false)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = VestiColors.TextMain)
                            Text(product, fontSize = 11.sp, color = VestiColors.Primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppConfig.t("Geri Dön", "Go Back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text(AppConfig.t("Mesaj yaz... (Enter ile gönder)", "Type a message... (Press enter to send)")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = VestiColors.Background,
                            focusedContainerColor = VestiColors.Background
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* Send logic */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(VestiColors.LightPurple, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = AppConfig.t("Gönder", "Send"), tint = VestiColors.Primary)
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
                .padding(16.dp)
        ) {
            messages.forEach { msg ->
                ChatBubble(
                    text = msg.text,
                    time = msg.time,
                    isMe = msg.isMe
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, time: String, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isMe) VestiColors.Primary else Color.White,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = text,
                    color = if (isMe) Color.White else VestiColors.TextMain,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = time, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 4.dp))
        }
    }
}
