package com.vesti.app.ui.outfit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.vesti.app.data.network.WardrobeItemDto
import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import com.vesti.app.ui.theme.VestiColors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OutfitScreen(viewModel: OutfitViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_COARSE_LOCATION)

    // Otomatik olarak ekran açıldığında bir öneri iste
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (state is OutfitState.Idle) {
            if (locationPermissionState.status.isGranted) {
                fetchLocationAndRecommend(context, viewModel)
            } else {
                viewModel.getRecommendation()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val currentState = state) {
            is OutfitState.Idle -> { }
            is OutfitState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    com.vesti.app.ui.wardrobe.HangerLoadingAnimation(
                        title = "AI Kombin Hazırlıyor",
                        subtitle = "Dijital gardırobunuz inceleniyor..."
                    )
                }
            }
            is OutfitState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = currentState.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.getRecommendation() }) {
                        Text("Yeniden Dene")
                    }
                }
            }
            is OutfitState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    // 1. Premium Editorial AI Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = VestiColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Vesti AI Kombini",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = VestiColors.Primary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = currentState.recommendation.description,
                                color = VestiColors.TextMain,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Önerilen Parçalar",
                        fontWeight = FontWeight.Bold,
                        color = VestiColors.TextMain,
                        fontSize = 16.sp,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 2. High-Fashion 2-Column Grid (Just like the Wardrobe Screen!)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(currentState.recommendation.items) { item ->
                            OutfitItemCard(item = item)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 3. Luxurious Floating Action Button
                    Button(
                        onClick = { viewModel.getRecommendation() },
                        colors = ButtonDefaults.buttonColors(containerColor = VestiColors.Primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(bottom = 4.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Text(
                                text = "Farklı Bir Öneri Getir",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutfitItemCard(item: WardrobeItemDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val fullImageUrl = if (item.imageUrl.startsWith("http")) item.imageUrl else "http://192.168.1.103:8080${item.imageUrl}"
            
            // Image Content
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fullImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.category,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Bottom Info Overlay with soft gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Column {
                    Text(
                        text = item.category,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = (-0.2).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.color,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationAndRecommend(context: android.content.Context, viewModel: OutfitViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            viewModel.fetchRecommendationWithLocation(
                lat = location.latitude,
                lon = location.longitude
            )
        } else {
            // Konum bulunamazsa generic çağır
            viewModel.getRecommendation()
        }
    }.addOnFailureListener {
        viewModel.getRecommendation()
    }
}
