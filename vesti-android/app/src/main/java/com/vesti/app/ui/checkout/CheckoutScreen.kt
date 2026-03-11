package com.vesti.app.ui.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import汇import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    itemId: String,
    price: Double,
    viewModel: CheckoutViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Güvenli Ödeme", color = VestiColors.Background) },
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
                        Text("3D Secure İşlemi ve Banka Onayı Bekleniyor...")
                        Text("Lütfen pencereyi kapatmayın.", style = MaterialTheme.typography.bodySmall)
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
                            text = "Ödemeniz Başarılı!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "İşlem No: ${currentState.response.transactionId}")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { 
                            viewModel.resetState()
                            onNavigateBack() 
                        }) {
                            Text("Market'e Dön")
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
                                Text(text = "Sipariş Özeti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), justifyContent = Arrangement.SpaceBetween) {
                                    Text("Ürün Kodu:")
                                    Text(itemId, fontWeight = FontWeight.Medium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), justifyContent = Arrangement.SpaceBetween) {
                                    Text("Toplam Tutar:")
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
                                Row(modifier = Modifier.fillMaxWidth(), justifyContent = Arrangement.SpaceBetween) {
                                    Text("Vesti Card", color = VestiColors.Background, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", tint = VestiColors.Background)
                                }
                                Text(
                                    text = if (cardNumber.isEmpty()) "**** **** **** ****" else cardNumber.chunked(4).joinToString(" "),
                                    color = VestiColors.Background,
                                    style = MaterialTheme.typography.headlineSmall,
                                    letterSpacing = 2.dp
                                )
                                Row(modifier = Modifier.fillMaxWidth(), justifyContent = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("CARDHOLDER", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                        Text(if (cardholderName.isEmpty()) "AD SOYAD" else cardholderName.uppercase(), color = VestiColors.Background)
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
                            label = { Text("Kart Üzerindeki İsim") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { if (it.length <= 16) cardNumber = it },
                            label = { Text("Kart Numarası") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { if (it.length <= 5) expiryDate = it },
                                label = { Text("SKT (AA/YY)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { if (it.length <= 4) cvv = it },
                                label = { Text("CVV") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Ödeme Butonu
                        Button(
                            onClick = { 
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
                            colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Accent)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Güvenli Ödeme Yap ($price TRY)")
                        }
                    }
                }
            }
        }
    }
}
