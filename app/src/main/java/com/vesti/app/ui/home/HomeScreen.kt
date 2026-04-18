package com.vesti.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.ui.theme.VestiColors

@Composable
fun HomeScreen(
    onNavigateToOutfit: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    // Scaffold without its own background color to rely on Theme, explicitly setting background to match web.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VestiColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 24.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                HomeHeader()
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                WeatherCard()
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Hızlı İşlemler",
                    color = VestiColors.TextMain,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                QuickActionsRow(
                    onNavigateToOutfit = onNavigateToOutfit,
                    onNavigateToWardrobe = onNavigateToWardrobe,
                    onNavigateToMarket = onNavigateToMarket
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                DailyRecommendationCard()
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Günaydın,",
                color = Color.Gray,
                fontSize = 16.sp
            )
            Text(
                text = "Nefise Beyza",
                color = VestiColors.TextMain,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
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
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun WeatherCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "İstanbul",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "24°C Güneşli",
                    color = VestiColors.Primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Açık renkli kıyafetler \ntercih edebilirsin.",
                    color = VestiColors.TextMain,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Güneşli",
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun QuickActionsRow(
    onNavigateToOutfit: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToMarket: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionItem(
            icon = Icons.Default.AutoAwesome,
            title = "Kombin\nÖnerisi",
            color = VestiColors.Primary,
            onClick = onNavigateToOutfit
        )
        QuickActionItem(
            icon = Icons.Default.Checkroom,
            title = "Dijital\nGardırop",
            color = VestiColors.Accent,
            onClick = onNavigateToWardrobe
        )
        QuickActionItem(
            icon = Icons.Default.Storefront,
            title = "Vesti\nMarket",
            color = VestiColors.SuccessMood,
            iconTint = Color(0xFF2E7D32), // Darker green for icon
            onClick = onNavigateToMarket
        )
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    title: String,
    color: Color,
    iconTint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = color),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = VestiColors.TextMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun DailyRecommendationCard() {
    Text(
        text = "Sana Özel",
        color = VestiColors.TextMain,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VestiColors.LightPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Vintage Tarzını\nKeşfet!",
                    color = VestiColors.Primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Market'te yeni eklenen parçalara göz at.",
                    color = VestiColors.TextMain,
                    fontSize = 12.sp
                )
            }
        }
    }
}
