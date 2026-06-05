package com.vesti.app.ui.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
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

data class ChatMessageDto(
    val text: String,
    val time: String,
    val isMe: Boolean,
    val isSwapOffer: Boolean = false,
    val swapStatus: String = "none" // "none", "pending", "accepted", "rejected"
)

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
    var chatMessages: List<ChatMessageDto>

    when (userId) {
        "aysek" -> {
            name = "Ayşe K."
            product = AppConfig.t("Zara Keten Blazer - Bej", "Zara Linen Blazer - Beige")
            initials = "AK"
            chatMessages = listOf(
                ChatMessageDto(AppConfig.t("Merhaba! İlan hâlâ satılıkta mı?", "Hello! Is this listing still for sale?"), "17:44 ✓✓", true),
                ChatMessageDto(AppConfig.t("Merhaba, evet satılıkta! Nasıl yardımcı olabilirim?", "Hello, yes it is! How can I help you?"), "17:54", false),
                ChatMessageDto(
                    text = AppConfig.t(
                        "🔄 Takas Teklifi\n\nMango Midi Etek karşılığında Zara Keten Blazer ile takas yapmak ister misiniz?",
                        "🔄 Swap Offer\n\nWould you like to swap Zara Linen Blazer for a Mango Midi Skirt?"
                    ),
                    time = "18:04",
                    isMe = false,
                    isSwapOffer = true,
                    swapStatus = "pending"
                )
            )
        }
        "elift" -> {
            name = "Elif T."
            product = AppConfig.t("Mango Midi Etek - Siyah", "Mango Midi Skirt - Black")
            initials = "ET"
            chatMessages = listOf(
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
            chatMessages = listOf(
                ChatMessageDto(AppConfig.t("Ceket için 200 TL teklif ediyorum.", "I offer 200 TL for the jacket."), "Dün ✓✓", true),
                ChatMessageDto(AppConfig.t("Takas teklifi için teşekkürler ama geçiyorum 🙏", "Thanks for the swap offer, but I'll pass 🙏"), "Dün", false)
            )
        }
    }

    // Swap durumunu track et
    var messages by remember { mutableStateOf(chatMessages) }

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
                        placeholder = { Text(AppConfig.t("Mesaj yaz...", "Type a message...")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = VestiColors.Background,
                            focusedContainerColor = VestiColors.Background
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                messages = messages + ChatMessageDto(messageText, "Şimdi ✓", true)
                                messageText = ""
                            }
                        },
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
            messages.forEachIndexed { index, msg ->
                if (msg.isSwapOffer) {
                    SwapOfferBubble(
                        text = msg.text,
                        time = msg.time,
                        isMe = msg.isMe,
                        swapStatus = msg.swapStatus,
                        onAccept = {
                            messages = messages.mapIndexed { i, m ->
                                if (i == index) m.copy(swapStatus = "accepted") else m
                            }
                        },
                        onReject = {
                            messages = messages.mapIndexed { i, m ->
                                if (i == index) m.copy(swapStatus = "rejected") else m
                            }
                        }
                    )
                } else {
                    ChatBubble(
                        text = msg.text,
                        time = msg.time,
                        isMe = msg.isMe
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SwapOfferBubble(
    text: String,
    time: String,
    isMe: Boolean,
    swapStatus: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F0FF))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = VestiColors.Primary, modifier = Modifier.size(16.dp))
                        Text(
                            AppConfig.t("Takas Teklifi", "Swap Offer"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VestiColors.Primary
                        )
                    }
                    when (swapStatus) {
                        "accepted" -> Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(AppConfig.t("Kabul Edildi", "Accepted"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        "rejected" -> Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f)
                        ) {
                            Text(AppConfig.t("Reddedildi", "Rejected"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        "pending" -> Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Text(AppConfig.t("Yanıt Bekleniyor", "Awaiting Reply"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }

                // Content
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        color = VestiColors.TextMain,
                        lineHeight = 18.sp
                    )

                    // Action buttons (sadece pending ve karşı taraftan geldiyse göster)
                    if (swapStatus == "pending" && !isMe) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppConfig.t("Kabul Et", "Accept"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppConfig.t("Reddet", "Reject"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppConfig.t(
                                "Onaylayarak takas sözleşmesini kabul etmiş olursunuz. Cayma durumunda ücret iadesi yapılmaz.",
                                "By accepting, you agree to the swap agreement. No refunds for cancellations."
                            ),
                            fontSize = 9.sp,
                            color = Color.Gray,
                            lineHeight = 12.sp
                        )
                    } else if (swapStatus == "pending" && isMe) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppConfig.t("Karşı tarafın teklifinizi incelemesi bekleniyor.", "Waiting for the other party to review your offer."),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = time,
                        fontSize = 9.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(if (isMe) Alignment.End else Alignment.Start)
                    )
                }
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
