package com.vesti.app.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppConfig.t("Mesajlar", "Messages"), fontWeight = FontWeight.Bold, color = VestiColors.DarkIndigo) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppConfig.t("Geri", "Back"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VestiColors.Background)
            )
        },
        containerColor = VestiColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = AppConfig.t("1 okunmamış konuşma", "1 unread conversation"),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                LazyColumn {
                    item { 
                        MessageItem(
                            name = "Ayşe K.",
                            product = AppConfig.t("Zara Keten Blazer - Bej", "Zara Linen Blazer - Beige"),
                            message = AppConfig.t("Evet, hâlâ satılıkta. Fiyatta anlaşabiliriz \uD83D\uDE0A", "Yes, still for sale. We can negotiate the price \uD83D\uDE0A"),
                            time = AppConfig.t("10 dk önce", "10m ago"),
                            unread = true,
                            initials = "AK"
                        ) {
                            onNavigateToChat("aysek")
                        } 
                    }
                    item { Divider(color = Color(0xFFF3F4F6)) }
                    item { 
                        MessageItem(
                            name = "Elif T.",
                            product = AppConfig.t("Mango Midi Etek - Siyah", "Mango Midi Skirt - Black"),
                            message = AppConfig.t("Sen: Kargo bilgilerini paylaşır mısın?", "You: Could you share the shipping details?"),
                            time = AppConfig.t("3 sa önce", "3h ago"),
                            unread = false,
                            initials = "ET"
                        ) {
                            onNavigateToChat("elift")
                        } 
                    }
                    item { Divider(color = Color(0xFFF3F4F6)) }
                    item { 
                        MessageItem(
                            name = "Zeynep A.",
                            product = AppConfig.t("H&M Denim Ceket", "H&M Denim Jacket"),
                            message = AppConfig.t("Takas teklifi için teşekkürler ama geçiyorum \uD83D\uDE4F", "Thanks for the swap offer, but I'll pass \uD83D\uDE4F"),
                            time = AppConfig.t("1 gün önce", "1 day ago"),
                            unread = false,
                            initials = "ZA"
                        ) {
                            onNavigateToChat("zeynepa")
                        } 
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(name: String, product: String, message: String, time: String, unread: Boolean, initials: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(VestiColors.Accent)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                ) {
                    Text("2", color = Color.White, fontSize = 9.sp, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium, fontSize = 15.sp, color = VestiColors.TextMain)
                Text(time, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(product, fontSize = 12.sp, color = VestiColors.Primary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = if (unread) VestiColors.TextMain else Color.Gray,
                fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
