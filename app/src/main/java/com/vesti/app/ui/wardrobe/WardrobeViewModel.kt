package com.vesti.app.ui.wardrobe

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vesti.app.data.network.WardrobeApi
import com.vesti.app.data.network.WardrobeItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

sealed class WardrobeState {
    object Loading : WardrobeState()
    data class Success(val items: List<WardrobeItemDto>) : WardrobeState()
    data class Error(val message: String) : WardrobeState()
}

class WardrobeViewModel(private val api: WardrobeApi) : ViewModel() {

    private val _state = MutableStateFlow<WardrobeState>(WardrobeState.Loading)
    val state: StateFlow<WardrobeState> = _state.asStateFlow()

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _state.value = WardrobeState.Loading
            try {
                val response = api.getWardrobeItems()
                if (response.isSuccessful && response.body() != null) {
                    _state.value = WardrobeState.Success(response.body()!!)
                } else {
                    _state.value = WardrobeState.Error("Failed to load items: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = WardrobeState.Error("Network error: ${e.message}")
            }
        }
    }

    fun uploadImage(context: Context, fileUri: Uri, category: String = "Uncategorized") {
        viewModelScope.launch {
            _uploading.value = true
            try {
                val requestFile = getFileFromUri(context, fileUri)?.asRequestBody("image/jpeg".toMediaTypeOrNull())
                
                if (requestFile != null) {
                    val body = MultipartBody.Part.createFormData("image", "camera_photo.jpg", requestFile)
                    val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

                    val response = api.uploadClothing(
                        image = body,
                        category = categoryBody,
                        color = null,
                        brand = null,
                        size = null
                    )

                    if (response.isSuccessful) {
                        loadItems() // Reload state after successful upload
                    } else {
                        // Normally handle UI error notification here
                    }
                }
            } catch (e: Exception) {
                // Normally handle UI error notification here
            } finally {
                _uploading.value = false
            }
        }
    }
    
    // Yardımcı fonksiyon URI'den Cache içindeki asıl File nesnesini bulur
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        // file_paths.xml ve CameraHelper'da "camera_photos" pathini kullanmıştık.
        // URI doğrudan cache pathine işaret ediyor.
        val path = uri.path
        if (path != null) {
            // content://com.vesti.app.fileprovider/camera_photos/JPEG_2023...jpg
            val fileName = path.substringAfterLast("/")
            val file = File(context.cacheDir, "camera_photos/$fileName")
            if (file.exists()) {
                return file
            }
        }
        return null
    }
}
