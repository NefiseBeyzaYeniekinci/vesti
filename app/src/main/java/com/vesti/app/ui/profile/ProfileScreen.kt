package com.vesti.app.ui.profile

import androidx.compose.foundation.BorderStroke
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
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val tabs = listOf(
        AppConfig.t("Genel Profil", "General Profile"),
        AppConfig.t("Siparişlerim", "My Orders"),
        AppConfig.t("Satışlarım & Kazançlarım", "My Sales & Earnings"),
        AppConfig.t("Ödeme Yöntemleri", "Payment Methods"),
        AppConfig.t("Kampanyalar & Kodlar", "Campaigns & Coupons"),
        AppConfig.t("Gizlilik ve Görünüm", "Privacy & Appearance"),
        AppConfig.t("Güvenlik", "Security")
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = AppConfig.t("Profil ve Ayarlar", "Profile & Settings"),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = if (AppConfig.isDarkMode) Color.White else VestiColors.DarkIndigo
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
                
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = VestiColors.Primary,
                            height = 3.dp
                        )
                    },
                    divider = { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) VestiColors.Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ) 
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> GeneralProfileContent(onLogout = onLogout)
                5 -> PrivacyAndAppearanceContent()
                else -> PlaceholderTabContent(tabs[selectedTabIndex])
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralProfileContent(onLogout: () -> Unit) {
    var name by remember { mutableStateOf("Nefise Beyza") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = AppConfig.t("Çıkış Yap", "Log Out"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = AppConfig.t(
                        "Çıkış yapmak istediğinizden emin misiniz?", 
                        "Are you sure you want to log out?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(text = AppConfig.t("Evet, Çıkış Yap", "Yes, Log Out"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = AppConfig.t("İptal", "Cancel"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VestiColors.LightPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NB", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VestiColors.Primary)
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = "Nefise Beyza",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("nefisebeyzaa05@gmail.com", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                            Text(
                                text = "0.0 " + AppConfig.t("Güvenilirlik Puanı", "Trust Score"),
                                fontSize = 12.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = AppConfig.t("Ad Soyad", "Full Name"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedBorderColor = VestiColors.Primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = AppConfig.t("Hakkımda (Biyografi)", "About Me (Bio)"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    placeholder = { 
                        Text(
                            text = AppConfig.t(
                                "Kendi moda tarzınızdan ve sevdiğiniz markalardan bahsedin...", 
                                "Talk about your fashion style and brands you love..."
                            ), 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f), 
                            fontSize = 14.sp
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedBorderColor = VestiColors.Primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = AppConfig.t("Konum / Şehir", "Location / City"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { 
                        Text(
                            text = AppConfig.t("Örn: İstanbul, Beşiktaş", "e.g. London, Soho"), 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f), 
                            fontSize = 14.sp
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        focusedBorderColor = VestiColors.Primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                    Text(
                        text = AppConfig.t("Tüm Değişiklikleri Kaydet", "Save All Changes"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text(
                        text = AppConfig.t("Çıkış Yap", "Log Out"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for nav bar
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyAndAppearanceContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        
        // Section: Language (Dil Ayarları)
        Text(
            text = AppConfig.t("Dil Ayarları", "Language Settings"),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Turkish Option Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Türkçe",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppConfig.t("Uygulama dilini Türkçe yapın", "Change the application language to Turkish"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    RadioButton(
                        selected = AppConfig.language == "tr",
                        onClick = {
                            AppConfig.language = "tr"
                            prefs.edit().putString("language", "tr").apply()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = VestiColors.Primary)
                    )
                }
                
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                
                // English Option Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "English (US)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppConfig.t("Uygulama dilini İngilizce yapın", "Change the application language to English"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    RadioButton(
                        selected = AppConfig.language == "en",
                        onClick = {
                            AppConfig.language = "en"
                            prefs.edit().putString("language", "en").apply()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = VestiColors.Primary)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun PlaceholderTabContent(tabName: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = AppConfig.t(
                    "$tabName verileriniz burada listelenecek.", 
                    "Your $tabName data will be listed here."
                ), 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                fontSize = 16.sp, 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

