package com.vesti.app.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vesti.app.AppConfig
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    itemId: String,
    price: Double,
    viewModel: CheckoutViewModel,
    marketplaceViewModel: com.vesti.app.ui.marketplace.MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val tokenManager = remember { com.vesti.app.data.local.TokenManager(context) }
    val currentUserId by tokenManager.userIdFlow.collectAsState(initial = "")
    val marketplaceState by marketplaceViewModel.state.collectAsStateWithLifecycle()
    val product = (marketplaceState as? com.vesti.app.ui.marketplace.MarketplaceState.Success)?.items?.find { it.id == itemId }
    val isOwner = product?.sellerId == currentUserId && currentUserId.isNotEmpty()

    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var isSecurePaymentEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppConfig.t("Güvenli Ödeme", "Secure Payment"), color = VestiColors.Background) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VestiColors.DarkIndigo)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            when (val currentState = state) {
                is CheckoutState.Processing -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = VestiColors.Accent)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(AppConfig.t("3D Secure İşlemi ve Banka Onayı Bekleniyor...", "3D Secure Processing & Bank Approval Pending..."))
                        Text(AppConfig.t("Lütfen pencereyi kapatmayın.", "Please do not close this window."), style = MaterialTheme.typography.bodySmall)
                    }
                }
                is CheckoutState.Success -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = AppConfig.t("Siparişiniz Alınmıştır!", "Your Order Has Been Placed!"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = AppConfig.t("Sipariş No: ", "Order No: ") + currentState.response.transactionId)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { /* Siparişi Takip Et mantığı buraya eklenecek */ },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VestiColors.DarkIndigo)
                        ) {
                            Text(AppConfig.t("Siparişi Takip Et", "Track Order"))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { 
                                viewModel.resetState()
                                onNavigateBack() 
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(AppConfig.t("Market'e Dön", "Back to Market"))
                        }
                    }
                }
                else -> {
                    // Idle or Error State
                    Column(modifier = Modifier.fillMaxSize()) {
                        
                        if (currentState is CheckoutState.Error) {
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Sipariş Özeti
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = VestiColors.LightPurple)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = AppConfig.t("Sipariş Özeti", "Order Summary"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(AppConfig.t("Ürün Kodu:", "Product Code:"))
                                    Text(itemId, fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(AppConfig.t("Toplam Tutar:", "Total Amount:"))
                                    Text("$price TRY", fontWeight = FontWeight.Bold, color = VestiColors.DarkIndigo)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Kredi Kartı Görseli Simülasyonu
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(VestiColors.DarkIndigo, RoundedCornerShape(16.dp))
                                .padding(24.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Vesti Card", color = VestiColors.Background, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", tint = VestiColors.Background)
                                }
                                Text(
                                    text = if (cardNumber.isEmpty()) "**** **** **** ****" else cardNumber,
                                    color = VestiColors.Background,
                                    style = MaterialTheme.typography.headlineSmall,
                                    letterSpacing = 2.sp
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("CARDHOLDER", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                        Text(if (cardholderName.isEmpty()) AppConfig.t("AD SOYAD", "FULL NAME") else cardholderName.uppercase(), color = VestiColors.Background)
                                    }
                                    Column {
                                        Text("EXPIRES", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                        Text(if (expiryDate.isEmpty()) "MM/YY" else expiryDate, color = VestiColors.Background)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Form
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it },
                            label = { Text(AppConfig.t("Kart Üzerindeki İsim", "Name on Card")) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                label = { Text(AppConfig.t("SKT (AA/YY)", "Expiry (MM/YY)")) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { if (it.length <= 3) cvv = it.filter { char -> char.isDigit() } },
                                label = { Text("CVV") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isSecurePaymentEnabled, onCheckedChange = { isSecurePaymentEnabled = it })
                            Text(AppConfig.t("3D Güvenli Ödeme", "3D Secure Payment"), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (isOwner) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = AppConfig.t("Kendi Ürününüzü Satın Alamazsınız", "You Cannot Purchase Your Own Product"),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626),
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = AppConfig.t("Pazar yerinde kendi paylaştığınız ürünler için ödeme veya takas işlemi yapamazsınız.", "You cannot complete payment or swap transactions for products you listed on the marketplace."),
                                        color = Color(0xFF991B1B),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // Ödeme Butonu
                            Button(
                                onClick = { 
                                    val cleanCard = cardNumber.filter { it.isDigit() }
                                    if (cardholderName.trim().isEmpty()) {
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
                                    if (cvv.length < 3) {
                                        Toast.makeText(context, AppConfig.tStr("Lütfen 3 haneli CVV kodunu girin.", "Please enter 3-digit CVV code."), Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    if (isSecurePaymentEnabled) {
                                        // Gelecekte buraya SMS 6 haneli kod doğrulama modalı tetiklenecek
                                        // Şimdilik doğrudan backend/simülasyon katmanına atıyoruz
                                    }
                                    viewModel.processPayment(
                                        itemId = itemId,
                                        amount = price,
                                        cardNumber = cardNumber,
                                        expiryDate = expiryDate,
                                        cvv = cvv,
                                        cardholderName = cardholderName
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary)
                            ) {
                                Text(AppConfig.t("Sipariş Ver", "Place Order") + " ($price TRY)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
