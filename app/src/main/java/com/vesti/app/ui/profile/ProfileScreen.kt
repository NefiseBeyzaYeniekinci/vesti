package com.vesti.app.ui.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.AppConfig
import com.vesti.app.data.network.*
import com.vesti.app.ui.theme.VestiColors
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    tokenManager: com.vesti.app.data.local.TokenManager,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
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

    // Reload info on view open
    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

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
                0 -> GeneralProfileContent(viewModel = viewModel, tokenManager = tokenManager, onLogout = onLogout)
                1 -> OrdersTabContent(viewModel = viewModel)
                2 -> SalesTabContent(viewModel = viewModel)
                3 -> PaymentMethodsContent(viewModel = viewModel)
                4 -> PromotionsContent(viewModel = viewModel)
                5 -> PrivacyAndAppearanceContent(viewModel = viewModel)
                6 -> SecurityContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralProfileContent(
    viewModel: ProfileViewModel,
    tokenManager: com.vesti.app.data.local.TokenManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateResult by viewModel.updateResult.collectAsState()

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateResult) {
        updateResult?.let {
            if (it == "SUCCESS") {
                Toast.makeText(context, AppConfig.tStr("Değişiklikler kaydedildi!", "Changes saved!"), Toast.LENGTH_SHORT).show()
                viewModel.clearUpdateResult()
            } else {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearUpdateResult()
            }
        }
    }

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

    when (val state = profileState) {
        is ProfileLoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VestiColors.Primary)
            }
        }
        is ProfileLoadState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.message, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile() }) {
                        Text(AppConfig.t("Tekrar Dene", "Try Again"))
                    }
                }
            }
        }
        is ProfileLoadState.Success -> {
            val user = state.profile
            LaunchedEffect(user) {
                name = user.name ?: ""
                bio = user.bio ?: ""
                city = user.location ?: ""
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
                            val initials = if (name.length >= 2) name.substring(0, 2).uppercase() else if (name.isNotEmpty()) name.take(1).uppercase() else "VS"
                            Text(initials, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = VestiColors.Primary)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = name.ifEmpty { "Vesti User" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(user.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                                        text = String.format(Locale.US, "%.1f ", user.trustScore ?: 0.0) + AppConfig.t("Güvenilirlik Puanı", "Trust Score"),
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
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                focusedBorderColor = VestiColors.Primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                viewModel.updateProfile(ProfileUpdateRequest(name = name, bio = bio, location = city))
                                viewModel.updateCity(city)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                            enabled = !isUpdating
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = AppConfig.t("Tüm Değişiklikleri Kaydet", "Save All Changes"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun OrdersTabContent(viewModel: ProfileViewModel) {
    val ordersState by viewModel.ordersState.collectAsState()
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    when (val state = ordersState) {
        is OrdersLoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VestiColors.Primary)
            }
        }
        is OrdersLoadState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadOrders() }) {
                        Text(AppConfig.t("Tekrar Dene", "Try Again"))
                    }
                }
            }
        }
        is OrdersLoadState.Success -> {
            val orders = state.orders
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(AppConfig.t("Henüz hiç siparişiniz yok.", "You have no orders yet."), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(AppConfig.t("Marketplace üzerinden alışveriş yapabilirsiniz.", "You can start shopping on the Marketplace."), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    orders.forEach { order ->
                        val isOpen = expandedOrderId == order.id
                        OrderCard(order = order, isOpen = isOpen, onClick = {
                            expandedOrderId = if (isOpen) null else order.id
                        })
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderDto, isOpen: Boolean, onClick: () -> Unit) {
    val statusLabel = when (order.status) {
        "pending" -> AppConfig.t("Onay Bekleniyor", "Pending Approval")
        "paid" -> AppConfig.t("Ödendi", "Paid")
        "shipped" -> AppConfig.t("Kargoda", "Shipped")
        "delivered" -> AppConfig.t("Teslim Edildi", "Delivered")
        else -> AppConfig.t("İptal Edildi", "Cancelled")
    }

    val statusColor = when (order.status) {
        "pending" -> Color(0xFFD97706)
        "paid" -> Color(0xFF2563EB)
        "shipped" -> Color(0xFF4F46E5)
        "delivered" -> Color(0xFF16A34A)
        else -> Color(0xFFDC2626)
    }

    val statusBg = statusColor.copy(alpha = 0.1f)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Listing Image
                val imgUrl = order.listing.images.firstOrNull() ?: ""
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imgUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(com.vesti.app.AppConfig.resolveImageSource(imgUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = order.listing.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(order.listing.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Text(
                        text = "${order.listing.category ?: "Moda"} · " + order.createdAt.take(10),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f %s", order.price, order.currency),
                        fontWeight = FontWeight.ExtraBold,
                        color = VestiColors.Primary
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isOpen) {
                Column(modifier = Modifier.padding(top = 16.dp).fillMaxWidth()) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!order.trackingNumber.isNullOrEmpty()) {
                        Surface(
                            color = Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF4F46E5))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(order.trackingCarrier ?: "Kargo", fontSize = 11.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                                    Text(order.trackingNumber, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF312E81))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = AppConfig.t("Sipariş Geçmişi", "Order Timeline"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (order.events.isNullOrEmpty()) {
                        Text(AppConfig.t("Takip geçmişi bulunmuyor.", "No tracking history available."), fontSize = 13.sp, color = Color.Gray)
                    } else {
                        order.events.forEachIndexed { index, event ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
                                    Box(
                                        modifier = Modifier.size(8.dp).clip(CircleShape).background(if (index == 0) VestiColors.Primary else Color.LightGray)
                                    )
                                    if (index < order.events.size - 1) {
                                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(event.description, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    if (!event.location.isNullOrEmpty()) {
                                        Text(event.location, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(event.createdAt.take(16).replace("T", " "), fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesTabContent(viewModel: ProfileViewModel) {
    val salesState by viewModel.salesState.collectAsState()
    val context = LocalContext.current
    var showStatsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSales()
    }

    if (showStatsDialog && salesState is SalesLoadState.Success) {
        val sales = (salesState as SalesLoadState.Success).sales
        val totalEarned = sales.filter { it.status == "shipped" || it.status == "delivered" }.sumOf { it.price }
        val pendingPayment = sales.filter { it.status == "paid" }.sumOf { it.price }
        val totalSales = sales.size

        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = { Text(AppConfig.t("Satış ve Kazanç İstatistikleri", "Sales & Earnings Stats"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(title = AppConfig.t("Toplam Kazanç", "Total Earnings"), value = String.format(Locale.US, "%.2f ₺", totalEarned), color = Color(0xFF16A34A))
                    StatCard(title = AppConfig.t("Bekleyen Ödeme", "Pending Payment"), value = String.format(Locale.US, "%.2f ₺", pendingPayment), color = Color(0xFF2563EB))
                    StatCard(title = AppConfig.t("Toplam Satış", "Total Sales"), value = "$totalSales " + AppConfig.t("ürün", "items"), color = Color(0xFF8B5CF6))
                }
            },
            confirmButton = {
                Button(onClick = { showStatsDialog = false }) {
                    Text("Tamam")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    when (val state = salesState) {
        is SalesLoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VestiColors.Primary)
            }
        }
        is SalesLoadState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadSales() }) {
                        Text(AppConfig.t("Tekrar Dene", "Try Again"))
                    }
                }
            }
        }
        is SalesLoadState.Success -> {
            val sales = state.sales
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Button(
                    onClick = { showStatsDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = VestiColors.Primary),
                    border = BorderStroke(1.dp, VestiColors.Primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppConfig.t("İstatistikleri Gör", "Show Stats"), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (sales.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(AppConfig.t("Henüz satışınız yok.", "You have no sales yet."), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(AppConfig.t("Marketplace'te ilan oluşturarak satış yapmaya başlayabilirsiniz.", "Start selling by creating a listing on Marketplace."), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        sales.forEach { sale ->
                            SaleCard(sale = sale, viewModel = viewModel)
                        }
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(title, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleCard(sale: OrderDto, viewModel: ProfileViewModel) {
    val context = LocalContext.current
    var trackingNo by remember { mutableStateOf("") }
    var selectedCarrier by remember { mutableStateOf("") }
    var expandedCarrierDropdown by remember { mutableStateOf(false) }
    var isSavingTracking by remember { mutableStateOf(false) }

    val carriers = listOf("MNG Kargo", "Yurtiçi Kargo", "PTT Kargo", "Aras Kargo", "Sürat Kargo", "UPS", "DHL")

    val statusLabel = when (sale.status) {
        "pending" -> AppConfig.t("Onay Bekleniyor", "Pending Approval")
        "paid" -> AppConfig.t("Ödendi - Kargola!", "Paid - Ship It!")
        "shipped" -> AppConfig.t("Kargoda", "Shipped")
        "delivered" -> AppConfig.t("Teslim Edildi", "Delivered")
        else -> AppConfig.t("İptal Edildi", "Cancelled")
    }

    val statusColor = when (sale.status) {
        "pending" -> Color(0xFFD97706)
        "paid" -> Color(0xFF0EA5E9)
        "shipped" -> Color(0xFF4F46E5)
        "delivered" -> Color(0xFF10B981)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Listing Image
                val imgUrl = sale.listing.images.firstOrNull() ?: ""
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAFAFA)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imgUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(com.vesti.app.AppConfig.resolveImageSource(imgUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = sale.listing.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(sale.listing.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                    Text(
                        text = AppConfig.t("Alıcı: ", "Buyer: ") + (sale.buyer.name ?: "Bilinmiyor"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.2f ₺", sale.price),
                        fontWeight = FontWeight.ExtraBold,
                        color = VestiColors.Primary
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (sale.status == "paid" && sale.trackingNumber.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppConfig.t("Kargo Takip Bilgisi Girin", "Enter Shipping Info"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCarrierDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(selectedCarrier.ifEmpty { AppConfig.t("Kargo Firması Seçin", "Select Carrier") })
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = expandedCarrierDropdown,
                        onDismissRequest = { expandedCarrierDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        carriers.forEach { carrier ->
                            DropdownMenuItem(
                                text = { Text(carrier) },
                                onClick = {
                                    selectedCarrier = carrier
                                    expandedCarrierDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = trackingNo,
                    onValueChange = { trackingNo = it },
                    placeholder = { Text(AppConfig.t("Takip Numarası", "Tracking Number")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (selectedCarrier.isEmpty() || trackingNo.trim().isEmpty()) {
                            Toast.makeText(context, AppConfig.tStr("Lütfen firma seçin ve takip numarasını girin.", "Please select carrier and enter tracking number."), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSavingTracking = true
                        viewModel.updateTracking(sale.id, trackingNo, selectedCarrier) { success, msg ->
                            isSavingTracking = false
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    enabled = !isSavingTracking
                ) {
                    if (isSavingTracking) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(AppConfig.t("Kargoyu Kaydet", "Save Tracking"), color = Color.White)
                    }
                }
            } else if (!sale.trackingNumber.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${sale.trackingCarrier} • ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        text = sale.trackingNumber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodsContent(viewModel: ProfileViewModel) {
    val profileState by viewModel.profileState.collectAsState()
    val context = LocalContext.current

    var isAddingCard by remember { mutableStateOf(false) }
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    when (val state = profileState) {
        is ProfileLoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VestiColors.Primary)
            }
        }
        is ProfileLoadState.Error -> {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile() }) {
                        Text(AppConfig.t("Tekrar Dene", "Try Again"))
                    }
                }
            }
        }
        is ProfileLoadState.Success -> {
            val user = state.profile
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            AppConfig.t("Ödeme Yöntemleri", "Payment Methods"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            AppConfig.t("Kayıtlı kredi/banka kartlarınızı yönetin.", "Manage your saved credit/debit cards."),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = { isAddingCard = !isAddingCard },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isAddingCard) Color.Gray else VestiColors.Primary)
                    ) {
                        Text(if (isAddingCard) AppConfig.t("İptal", "Cancel") else AppConfig.t("Kart Ekle", "Add Card"), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = isAddingCard) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(AppConfig.t("Yeni Kart Ekle", "Add New Card"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            
                            OutlinedTextField(
                                value = cardName,
                                onValueChange = { cardName = it },
                                label = { Text(AppConfig.t("Kart Sahibi Adı", "Cardholder Name")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { input ->
                                    val clean = input.filter { it.isDigit() }.take(16)
                                    cardNumber = buildString {
                                        for (i in clean.indices) {
                                            append(clean[i])
                                            if (i % 4 == 3 && i < 15) {
                                                append(" ")
                                            }
                                        }
                                    }
                                },
                                label = { Text(AppConfig.t("Kart Numarası", "Card Number")) },
                                placeholder = { Text("5528 7900 0000 0008") },
                                supportingText = { Text(AppConfig.t("Test için Iyzico Sandbox kartını (5528 7900 0000 0008) kullanabilirsiniz.", "You can use Iyzico Sandbox card (5528 7900 0000 0008) for testing."), fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { newValue ->
                                    val unformatted = newValue.replace("/", "").filter { it.isDigit() }.take(4)
                                    expiryDate = buildString {
                                        for (i in unformatted.indices) {
                                            append(unformatted[i])
                                            if (i == 1 && unformatted.length > 2) append("/")
                                        }
                                    }
                                },
                                label = { Text(AppConfig.t("Son Kullanma Tarihi (AA/YY)", "Expiry Date (MM/YY)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val cleanCard = cardNumber.filter { it.isDigit() }
                                    if (cardName.trim().isEmpty()) {
                                        Toast.makeText(context, AppConfig.tStr("Lütfen kart sahibinin adını girin.", "Please enter cardholder name."), Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (cleanCard.length != 16) {
                                        Toast.makeText(context, AppConfig.tStr("Lütfen 16 haneli geçerli bir kart numarası girin.", "Please enter a valid 16-digit card number."), Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (!expiryDate.matches(Regex("^[0-9]{2}/[0-9]{2}$"))) {
                                        Toast.makeText(context, AppConfig.tStr("Lütfen son kullanma tarihini AA/YY formatında girin.", "Please enter expiry date in MM/YY format."), Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    isSubmitting = true
                                    viewModel.addCard(cardName, cardNumber, expiryDate) { success, msg ->
                                        isSubmitting = false
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            isAddingCard = false
                                            cardName = ""
                                            cardNumber = ""
                                            expiryDate = ""
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                                enabled = !isSubmitting
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(AppConfig.t("Kartı Kaydet", "Save Card"), color = Color.White)
                                }
                            }
                        }
                    }
                }

                if (user.savedCards.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(AppConfig.t("Kayıtlı kartınız bulunmuyor.", "No cards saved."), color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        user.savedCards.forEach { card ->
                            CreditCardItem(card = card, onDelete = {
                                viewModel.deleteCard(card.id)
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun CreditCardItem(card: SavedCardDto, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)) // Indigo-cyan premium gradient
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Mock Card Logo
                Text(
                    text = "Vesti Card",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete card",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = card.cardNumber.chunked(4).joinToString(" "),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(AppConfig.t("KART SAHİBİ", "CARD HOLDER"), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(card.cardName.uppercase(), fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(AppConfig.t("SKT", "EXPIRY"), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(card.expiryDate, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PromotionsContent(viewModel: ProfileViewModel) {
    val promotionsState by viewModel.promotionsState.collectAsState()
    val context = LocalContext.current
    var promoCodeInput by remember { mutableStateOf("") }
    var isRedeeming by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPromotions()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text(AppConfig.t("Kampanyalar & Kodlar", "Campaigns & Coupons"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            AppConfig.t("Promosyon kodlarınızı uygulayın ve kazandıklarınızı takip edin.", "Redeem promo codes and track your rewards."),
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card style Input form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, VestiColors.Primary.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                VestiColors.LightPurple.copy(alpha = 0.5f),
                                Color(0xFFECEFF1).copy(alpha = 0.2f)
                            )
                        )
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(AppConfig.t("Promosyon Kodu Girin", "Enter Promo Code"), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = promoCodeInput,
                        onValueChange = { promoCodeInput = it.toUpperCase() },
                        placeholder = { Text("VESTI20") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    Button(
                        onClick = {
                            if (promoCodeInput.trim().isEmpty()) return@Button
                            isRedeeming = true
                            viewModel.redeemPromo(promoCodeInput.trim()) { success, msg ->
                                isRedeeming = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    promoCodeInput = ""
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                        enabled = !isRedeeming && promoCodeInput.trim().isNotEmpty()
                    ) {
                        if (isRedeeming) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(AppConfig.t("Uygula", "Apply"), color = Color.White)
                        }
                    }
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(24.dp))

        Text(AppConfig.t("Kullandığım Kodlar", "Redeemed Codes"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        when (val state = promotionsState) {
            is PromotionsLoadState.Loading -> {
                CircularProgressIndicator(color = VestiColors.Primary, modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
            }
            is PromotionsLoadState.Error -> {
                Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
            }
            is PromotionsLoadState.Success -> {
                val promos = state.promotions
                if (promos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        Text(AppConfig.t("Henüz kod kullanmadınız.", "You have not redeemed any codes yet."), color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        promos.forEach { promo ->
                            val discountText = when (promo.promoCode.discountType) {
                                "percentage" -> "%${promo.promoCode.discountValue.toInt()} İndirim"
                                "fixed" -> "${promo.promoCode.discountValue.toInt()} ₺ İndirim"
                                "wallet" -> "${promo.promoCode.discountValue.toInt()} ₺ Cüzdan"
                                else -> "${promo.promoCode.discountValue}"
                            }

                            val badgeBg = when (promo.promoCode.discountType) {
                                "percentage" -> Color(0xFFFFF1F2)
                                "fixed" -> Color(0xFFFFF7ED)
                                "wallet" -> Color(0xFFF0FDF4)
                                else -> Color(0xFFF3F4F6)
                            }

                            val badgeTextCol = when (promo.promoCode.discountType) {
                                "percentage" -> Color(0xFFE11D48)
                                "fixed" -> Color(0xFFEA580C)
                                "wallet" -> Color(0xFF16A34A)
                                else -> Color(0xFF4B5563)
                            }

                            val symbol = when (promo.promoCode.discountType) {
                                "percentage" -> "%"
                                "fixed" -> "₺"
                                "wallet" -> "+"
                                else -> "★"
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            color = badgeTextCol,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = promo.promoCode.code,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(promo.promoCode.description ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = AppConfig.t("Kullanıldı: ", "Used: ") + promo.redeemedAt.take(10),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    }

                                    Text(
                                        text = discountText,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = badgeTextCol,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun PrivacyAndAppearanceContent(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    val profileState by viewModel.profileState.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()

    var isPublic by remember { mutableStateOf(true) }

    LaunchedEffect(profileState) {
        if (profileState is ProfileLoadState.Success) {
            isPublic = (profileState as ProfileLoadState.Success).profile.isPublic
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = AppConfig.t("Gizlilik ve Görünüm", "Privacy & Appearance"),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppConfig.t("Kamuya Açık Profil", "Public Profile"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppConfig.t("Profilinizin herkes tarafından görünmesini sağlar.", "Allows your profile to be viewed by everyone."),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = {
                            isPublic = it
                            viewModel.updateProfile(ProfileUpdateRequest(isPublic = it))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = VestiColors.Primary, checkedTrackColor = VestiColors.Primary.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
                            AppConfig.updateLocale(context, "tr")
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
                            AppConfig.updateLocale(context, "en")
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
fun SecurityContent(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val isUpdating by viewModel.isUpdating.collectAsState()
    val updateResult by viewModel.updateResult.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    LaunchedEffect(updateResult) {
        updateResult?.let {
            if (it == "SUCCESS") {
                Toast.makeText(context, AppConfig.tStr("Şifre güncellendi!", "Password updated!"), Toast.LENGTH_SHORT).show()
                currentPassword = ""
                newPassword = ""
                viewModel.clearUpdateResult()
            } else {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                viewModel.clearUpdateResult()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = AppConfig.t("Güvenlik", "Security"),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = AppConfig.t("Şifre Değiştir", "Change Password"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(AppConfig.t("Mevcut Şifre", "Current Password")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text(AppConfig.t("Yeni Şifre", "New Password")) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                            Toast.makeText(context, AppConfig.tStr("Lütfen tüm alanları doldurun.", "Please fill all fields."), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateProfile(ProfileUpdateRequest(currentPassword = currentPassword, newPassword = newPassword))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(AppConfig.t("Şifreyi Güncelle", "Update Password"), color = Color.White)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleProfileContent(viewModel: ProfileViewModel) {
    val context = LocalContext.current
    val styleState by viewModel.styleProfileState.collectAsState()
    val isSaving by viewModel.isSavingStyleProfile.collectAsState()
    val saveResult by viewModel.styleSaveResult.collectAsState()

    var activeTab by remember { mutableStateOf(0) }

    var favoriteColors by remember { mutableStateOf("") }
    var unwantedColors by remember { mutableStateOf("") }
    var stylePreference by remember { mutableStateOf("CASUAL") }
    var fitPreference by remember { mutableStateOf("") }
    var fabricPreference by remember { mutableStateOf("") }
    var bodyType by remember { mutableStateOf("UNKNOWN") }
    var sizeTops by remember { mutableStateOf("") }
    var sizeBottoms by remember { mutableStateOf("") }
    var sizeShoes by remember { mutableStateOf("") }

    var expandedStyle by remember { mutableStateOf(false) }
    var expandedBodyType by remember { mutableStateOf(false) }

    val stylesList = listOf(
        "CASUAL" to AppConfig.t("Casual (Gündelik / Rahat)", "Casual (Comfortable)"),
        "FORMAL" to AppConfig.t("Formal (Klasik / Şık)", "Formal (Classic/Smart)"),
        "SPORT" to AppConfig.t("Sport (Spor Giyim)", "Sport (Activewear)"),
        "MINIMAL" to AppConfig.t("Minimal (Yalın Tarz)", "Minimal (Clean)"),
        "STREETWEAR" to AppConfig.t("Streetwear (Sokak Modası)", "Streetwear")
    )

    val bodyTypesList = listOf(
        "UNKNOWN" to AppConfig.t("Belirtmek İstemiyorum", "Prefer not to say"),
        "ECTOMORPH" to AppConfig.t("Zayıf (Ektomorf)", "Slim (Ectomorph)"),
        "MESOMORPH" to AppConfig.t("Atletik (Mezomorf)", "Athletic (Mesomorph)"),
        "ENDOMORPH" to AppConfig.t("Geniş (Endomorf)", "Broad (Endomorph)")
    )

    LaunchedEffect(styleState) {
        if (styleState is StyleProfileLoadState.Success) {
            val profile = (styleState as StyleProfileLoadState.Success).styleProfile
            favoriteColors = profile.favoriteColors?.joinToString(", ") ?: ""
            unwantedColors = profile.unwantedColors?.joinToString(", ") ?: ""
            stylePreference = profile.stylePreference ?: "CASUAL"
            fitPreference = profile.fitPreference ?: ""
            fabricPreference = profile.fabricPreference ?: ""
            bodyType = profile.bodyType ?: "UNKNOWN"
            sizeTops = profile.sizeTops ?: ""
            sizeBottoms = profile.sizeBottoms ?: ""
            sizeShoes = profile.sizeShoes ?: ""
        }
    }

    LaunchedEffect(saveResult) {
        if (saveResult == "SUCCESS") {
            Toast.makeText(context, AppConfig.tStr("Tarz profiliniz kaydedildi! ✨", "Style profile saved! ✨"), Toast.LENGTH_SHORT).show()
            viewModel.clearStyleSaveResult()
        } else if (saveResult != null) {
            Toast.makeText(context, saveResult, Toast.LENGTH_SHORT).show()
            viewModel.clearStyleSaveResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VestiColors.LightPurple.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, VestiColors.Primary.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = VestiColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = AppConfig.t("Tarz Profilini Detaylandır", "Detailed Style Profile"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = VestiColors.DarkIndigo
                    )
                    Text(
                        text = AppConfig.t("Sana en özel kombin tavsiyelerini hazırlamamız için burayı doldurabilirsin.", "Fill this out so we can prepare the best custom outfit recommendations for you."),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = VestiColors.Primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = VestiColors.Primary,
                    height = 2.dp
                )
            },
            divider = { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)) }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text(AppConfig.t("Renkler & Tarz", "Colors & Style"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text(AppConfig.t("Kalıp & Kumaş", "Fit & Fabric"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text(AppConfig.t("Beden & Fiziksel", "Size & Physical"), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        when (activeTab) {
            0 -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = AppConfig.t("Renk ve Tarz Seçenekleri", "Color & Style Options"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = favoriteColors,
                        onValueChange = { favoriteColors = it },
                        label = { Text(AppConfig.t("Favori Renklerin", "Favorite Colors")) },
                        placeholder = { Text(AppConfig.t("Örn: Siyah, Lacivert, Haki Yeşili, Krem", "e.g. Black, Navy Blue, earth tones")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text(
                        text = AppConfig.t("Favori renklerini aralarına virgül koyarak yazabilirsin.", "You can list favorite colors separated by commas."),
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, top = (-10).dp)
                    )

                    OutlinedTextField(
                        value = unwantedColors,
                        onValueChange = { unwantedColors = it },
                        label = { Text(AppConfig.t("Kaçındığın & İstemediğin Renkler", "Colors You Avoid")) },
                        placeholder = { Text(AppConfig.t("Örn: Fosforlu sarı, Parlak Turuncu", "e.g. Neon yellow, orange")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = stylesList.find { it.first == stylePreference }?.second ?: stylePreference,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(AppConfig.t("Genel Tarz Tercihi", "General Style Preference")) },
                            trailingIcon = {
                                IconButton(onClick = { expandedStyle = true }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedStyle,
                            onDismissRequest = { expandedStyle = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            stylesList.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        stylePreference = value
                                        expandedStyle = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = AppConfig.t("Kalıp & Kumaş Tercihleri", "Fit & Fabric Preferences"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = fitPreference,
                        onValueChange = { fitPreference = it },
                        label = { Text(AppConfig.t("Kalıp & Kesim Tercihi", "Preferred Fit & Cut")) },
                        placeholder = { Text(AppConfig.t("Örn: Oversized, Standart, Slim Fit", "e.g. Oversized, regular fit, slim fit")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = fabricPreference,
                        onValueChange = { fabricPreference = it },
                        label = { Text(AppConfig.t("Tercih Ettiğin Kumaş Türleri", "Preferred Fabric Types")) },
                        placeholder = { Text(AppConfig.t("Örn: Pamuk, Keten, Denim, Deri, Kaşe", "e.g. Cotton, linen, denim, leather, wool")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            2 -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = AppConfig.t("Beden & Fiziksel Bilgiler", "Size & Physical Info"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = bodyTypesList.find { it.first == bodyType }?.second ?: bodyType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(AppConfig.t("Vücut Tipi", "Body Type")) },
                            trailingIcon = {
                                IconButton(onClick = { expandedBodyType = true }) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedBodyType,
                            onDismissRequest = { expandedBodyType = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            bodyTypesList.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        bodyType = value
                                        expandedBodyType = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = sizeTops,
                        onValueChange = { sizeTops = it },
                        label = { Text(AppConfig.t("Üst Beden", "Top Size")) },
                        placeholder = { Text(AppConfig.t("Örn: M veya L", "e.g. M or L")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sizeBottoms,
                        onValueChange = { sizeBottoms = it },
                        label = { Text(AppConfig.t("Alt Beden (Pantolon)", "Bottom Size (Pants)")) },
                        placeholder = { Text(AppConfig.t("Örn: 32 veya 40", "e.g. 32 or 40")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sizeShoes,
                        onValueChange = { sizeShoes = it },
                        label = { Text(AppConfig.t("Ayakkabı Numarası", "Shoe Size")) },
                        placeholder = { Text(AppConfig.t("Örn: 42", "e.g. 42")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeTab > 0) {
                OutlinedButton(
                    onClick = { activeTab -= 1 },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(AppConfig.t("Geri", "Back"))
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (activeTab < 2) {
                Button(
                    onClick = { activeTab += 1 },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(AppConfig.t("İleri", "Next"), color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        val payload = StyleProfileDto(
                            favoriteColors = favoriteColors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            unwantedColors = unwantedColors.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            stylePreference = stylePreference,
                            fitPreference = fitPreference,
                            fabricPreference = fabricPreference,
                            bodyType = bodyType,
                            sizeTops = sizeTops,
                            sizeBottoms = sizeBottoms,
                            sizeShoes = sizeShoes
                        )
                        viewModel.saveStyleProfile(payload)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                    modifier = Modifier.height(48.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(AppConfig.t("Profilimi Kaydet", "Save Profile"), color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
        

