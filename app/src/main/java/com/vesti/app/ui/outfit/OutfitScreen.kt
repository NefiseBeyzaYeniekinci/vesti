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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OutfitScreen(viewModel: OutfitViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Otomatik olarak ekran açıldığında bir öneri iste
    LaunchedEffect(Unit) {
        if (state is OutfitState.Idle) {
            viewModel.getRecommendation()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val currentState = state) {
            is OutfitState.Idle -> { }
            is OutfitState.Loading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI Gardırobunuzu inceliyor ve kombin hazırlıyor...")
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "AI Kombin Önerisi",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currentState.recommendation.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Önerilen Parçalar:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentState.recommendation.items) { item ->
                            RecommendationItemCard(item = item)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.getRecommendation() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Farklı Bir Öneri Getir")
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationItemCard(item: WardrobeItemDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Şimdilik wardrobe service ayağa kalkmadığı için fallback placeholder gösteriyoruz (mock data)
            val fullImageUrl = "http://10.0.2.2:8081${item.imageUrl}"
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fullImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.category,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(text = item.category, style = MaterialTheme.typography.titleMedium)
                Text(text = item.color, style = MaterialTheme.typography.bodyMedium)
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
