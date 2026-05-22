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
                    _state.value = WardrobeState.Success(getMockWardrobeItems())
                }
            } catch (e: Exception) {
                _state.value = WardrobeState.Success(getMockWardrobeItems())
            }
        }
    }

    private fun getMockWardrobeItems(): List<WardrobeItemDto> {
        return listOf(
            WardrobeItemDto("1", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab", "Tişört", "Beyaz", "Zara", "M", "2024-05-15"),
            WardrobeItemDto("2", "https://images.unsplash.com/photo-1542272604-787c3835535d", "Kot Pantolon", "Mavi", "Mavi", "32", "2024-05-14"),
            WardrobeItemDto("3", "https://images.unsplash.com/photo-1551028719-00167b16eac5", "Deri Ceket", "Siyah", "Mango", "L", "2024-05-13"),
            WardrobeItemDto("4", "https://images.unsplash.com/photo-1591047139829-d91aecb6caea", "Gömlek", "Kırmızı", "LCW", "M", "2024-05-12"),
            WardrobeItemDto("5", "https://images.unsplash.com/photo-1543163521-1bf539c55dd2", "Sneaker", "Beyaz", "Nike", "42", "2024-05-10")
        )
    }

    fun uploadImage(context: Context, fileUri: Uri, category: String = "Uncategorized", color: String? = null, brand: String? = null, size: String? = null) {
        viewModelScope.launch {
            _uploading.value = true
            try {
                val requestFile = getFileFromUri(context, fileUri)?.asRequestBody("image/jpeg".toMediaTypeOrNull())
                
                if (requestFile != null) {
                    val body = MultipartBody.Part.createFormData("image", "camera_photo.jpg", requestFile)
                    val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
                    val colorBody = color?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val brandBody = brand?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val sizeBody = size?.toRequestBody("text/plain".toMediaTypeOrNull())

                    val response = api.uploadClothing(
                        image = body,
                        category = categoryBody,
                        color = colorBody,
                        brand = brandBody,
                        size = sizeBody
                    )

                    if (response.isSuccessful) {
                        loadItems() // Reload state after successful upload
                    } else {
                        // Eğer hata verirse yine de listeyi yenile ki mock'lar tekrar yüklensin (test için)
                        loadItems()
                    }
                }
            } catch (e: Exception) {
                // Test için yükleme hatasında bile listeyi yenile
                loadItems()
            } finally {
                _uploading.value = false
            }
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                val response = api.deleteItem(itemId)
                if (response.isSuccessful) {
                    loadItems()
                } else {
                    loadItems()
                }
            } catch (e: Exception) {
                loadItems()
            }
        }
    }
    
    // Yardımcı fonksiyon URI'den Cache içindeki asıl File nesnesini bulur
    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val path = uri.path
        if (path != null) {
            val fileName = path.substringAfterLast("/")
            val file = File(context.cacheDir, "camera_photos/$fileName")
            if (file.exists()) {
                return file
            }
        }
        
        // Galeriden seçilen veya başka content providerlardan gelen URI'leri geçici dosyaya kopyalayarak destekleme
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val storageDir = File(context.cacheDir, "camera_photos")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            val tempFile = File.createTempFile("upload_${System.currentTimeMillis()}", ".jpg", storageDir)
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
