package com.vesti.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vesti.app.data.network.LoginRequest
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    // Girdi alanlarındaki yazının beyaz olmasını engelleyen ve okunabilir kılan yeni kural
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = VestiColors.TextMain,
        unfocusedTextColor = VestiColors.TextMain,
        focusedBorderColor = VestiColors.Primary,
        unfocusedBorderColor = Color.LightGray,
        cursorColor = VestiColors.Primary,
        focusedLabelColor = VestiColors.Primary,
        unfocusedLabelColor = Color.Gray
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VestiColors.Background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık ve Karşılama
                Text(
                    text = "Hoş Geldiniz",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = VestiColors.DarkIndigo
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vesti hesabınıza giriş yapın",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // E-posta Kutusu
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-posta") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "E-posta İkonu", tint = Color.Gray)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = textFieldColors,
                    textStyle = LocalTextStyle.current.copy(color = VestiColors.TextMain),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Şifre Kutusu
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Şifre") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Şifre İkonu", tint = Color.Gray)
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Şifre Görünürlüğü", tint = Color.Gray)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = textFieldColors,
                    textStyle = LocalTextStyle.current.copy(color = VestiColors.TextMain),
                    singleLine = true
                )
                
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (authState as AuthState.Error).error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Giriş Yap Butonu
                Button(
                    onClick = { viewModel.login(LoginRequest(email, password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VestiColors.Primary,
                        contentColor = Color.White
                    ),
                    enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Giriş Yap", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Kayıt Ol Yönlendirmesi
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hesabınız yok mu?", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Kayıt Ol", color = VestiColors.Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // VEYA Ayırıcı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray, thickness = 1.dp)
                    Text(text = "VEYA", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray, fontSize = 12.sp)
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray, thickness = 1.dp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Google ile Giriş Butonu Placeholder
                OutlinedButton(
                    onClick = { /* TODO: Google Login */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("G", fontWeight = FontWeight.Bold, color = VestiColors.DarkIndigo, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Google ile Giriş Yap", color = VestiColors.DarkIndigo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
