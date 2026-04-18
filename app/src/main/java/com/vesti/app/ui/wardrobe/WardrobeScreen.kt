@file:OptIn(ExperimentalPermissionsApi::class)

package com.vesti.app.ui.wardrobe

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.vesti.app.data.network.WardrobeItemDto

@Composable
fun WardrobeScreen(viewModel: WardrobeViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isUploading by viewModel.uploading.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            // Backend'e fotoğrafı yükle
            viewModel.uploadImage(context, currentPhotoUri!!)
        }
    }
    
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (cameraPermissionState.status.isGranted) {
                    val (file, uri) = CameraHelper.createTempImageFile(context)
                    currentPhotoUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionState.launchPermissionRequest()
                }
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Clothing")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val currentState = state) {
                is WardrobeState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WardrobeState.Error -> {
                    Text(
                        text = currentState.message, 
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is WardrobeState.Success -> {
                    if (currentState.items.isEmpty()) {
                        Text(
                            text = "Gardırobunuzda kıyafet bulunmuyor.\nAlttaki + butonuna basarak ekleyin.",
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentState.items) { item ->
                                WardrobeItemCard(item)
                            }
                        }
                    }
                }
            }
            
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Yükleniyor...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeItemCard(item: WardrobeItemDto) {
    Card(modifier = Modifier.fillMaxWidth().aspectRatio(0.8f)) {
        Column {
            // The item.imageUrl will be like "/uploads/xxx.jpg", from backend.
            // In a real app we'd construct the full url like "http://10.0.2.2:8081" + item.imageUrl
            val fullImageUrl = "http://10.0.2.2:8081${item.imageUrl}"
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(fullImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.category,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
