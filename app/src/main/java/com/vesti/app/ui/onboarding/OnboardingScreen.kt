package com.vesti.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesti.app.R
import com.vesti.app.ui.theme.VestiColors

@Composable
fun OnboardingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VestiColors.Background) // #F8F9FA matching image background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // T-Shirt Icon
        Icon(
            painter = painterResource(id = R.drawable.ic_tshirt),
            contentDescription = "Vesti Icon",
            modifier = Modifier.size(80.dp),
            tint = VestiColors.Primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Title 1
        Text(
            text = "Dolabındaki Giysiler,",
            color = VestiColors.DarkIndigo,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
            letterSpacing = (-1).sp
        )
        
        // Title 2
        Text(
            text = "Şimdi Daha Anlamlı.",
            color = VestiColors.Primary,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Body Text
        Text(
            text = "Vesti ile kıyafetlerini dijitalleştir, yapay zeka ile yepyeni kombinler keşfet ve artık kullanmadıklarını kolayca sat!",
            color = Color(0xFF6B7280),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                shape = RoundedCornerShape(30.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Hemen Başla",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Forward To Register",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedButton(
                onClick = onNavigateToLogin,
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VestiColors.Primary),
                border = androidx.compose.foundation.BorderStroke(1.dp, VestiColors.Primary),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Giriş Yap",
                    color = VestiColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
