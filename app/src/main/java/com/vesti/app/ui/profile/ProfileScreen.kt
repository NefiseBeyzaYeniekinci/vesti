package com.vesti.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val tabs = listOf(
        "Genel Profil",
        "Siparişlerim",
        "Satışlarım & Kazançlarım",
        "Ödeme Yöntemleri",
        "Kampanyalar & Kodlar",
        "Gizlilik ve Görünüm",
        "Güvenlik"
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Profil ve Ayarlar", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = VestiColors.DarkIndigo)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
                
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = VestiColors.Primary,
                            height = 3.dp
                        )
                    },
                    divider = { Divider(color = Color(0xFFF3F4F6)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) VestiColors.Primary else Color.Gray
                                ) 
                            }
                        )
                    }
                }
            }
        },
        containerColor = VestiColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> GeneralProfileContent()
                else -> PlaceholderTabContent(tabs[selectedTabIndex])
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralProfileContent() {
    var name by remember { mutableStateOf("Nefise Beyza") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NB", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VestiColors.Primary)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text("Nefise Beyza", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = VestiColors.TextMain)
                    Text("nefisebeyzaa05@gmail.com", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFFF7ED),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("0.0 Güvenilirlik Puanı", fontSize = 12.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Ad Soyad", fontSize = 14.sp, color = VestiColors.TextMain, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = VestiColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Hakkımda (Biyografi)", fontSize = 14.sp, color = VestiColors.TextMain, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { Text("Kendi moda tarzınızdan ve sevdiğiniz markalardan bahsedin...", color = Color.LightGray, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = VestiColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text("Konum / Şehir", fontSize = 14.sp, color = VestiColors.TextMain, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Örn: İstanbul, Beşiktaş", color = Color.LightGray, fontSize = 14.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedBorderColor = VestiColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { /* Kaydet */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary)
                ) {
                    Text("Tüm Değişiklikleri Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for nav bar
    }
}

@Composable
fun PlaceholderTabContent(tabName: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "$tabName verileriniz burada listelenecek.", 
                color = Color.Gray, 
                fontSize = 16.sp, 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
