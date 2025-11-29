package com.example.freshcookapp.ui.screen.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class EditProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // --- State cho UI ---
    private val _fullName = MutableStateFlow("")
    val fullName = _fullName.asStateFlow()

    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _dateOfBirth = MutableStateFlow("")
    val dateOfBirth = _dateOfBirth.asStateFlow()

    private val _gender = MutableStateFlow("Giới tính")
    val gender = _gender.asStateFlow()

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl = _photoUrl.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    // --- State điều khiển ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _saveResult = MutableStateFlow<Result<Unit>?>(null)
    val saveResult = _saveResult.asStateFlow()

    // --- Hàm cập nhật state từ UI ---
    fun onFullNameChange(name: String) { _fullName.value = name }
    fun onUsernameChange(name: String) { _username.value = name }
    fun onDateOfBirthChange(date: String) { _dateOfBirth.value = date }
    fun onGenderChange(newGender: String) { _gender.value = newGender }
    fun onSaveResultConsumed() { _saveResult.value = null }

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val currentUser = auth.currentUser ?: return
        _email.value = currentUser.email ?: ""
        val defaultUsername = currentUser.email?.split('@')?.get(0) ?: ""

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = firestore.collection("users").document(currentUser.uid).get().await()
                if (document != null && document.exists()) {
                    _fullName.value = document.getString("fullName") ?: currentUser.displayName ?: ""
                    _username.value = document.getString("username")?.takeIf { it.isNotBlank() } ?: defaultUsername
                    _dateOfBirth.value = document.getString("dateOfBirth") ?: ""
                    _gender.value = document.getString("gender") ?: "Giới tính"
                    _photoUrl.value = document.getString("photoUrl")
                } else {
                    _fullName.value = currentUser.displayName ?: ""
                    _username.value = defaultUsername
                    _photoUrl.value = currentUser.photoUrl?.toString()
                }
            } catch (e: Exception) {
                // Xử lý lỗi tải dữ liệu nếu cần
            }
        }
    }

    fun saveProfile(context: Context, selectedImageUri: Uri?) {
        val currentUser = auth.currentUser ?: return
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // BƯỚC 1: UPLOAD ẢNH (NẾU CÓ)
                val finalPhotoUrl = if (selectedImageUri != null) {
                    val compressedImageData = context.compressImage(selectedImageUri)
                    val photoRef = storage.reference.child("profile_images/${currentUser.uid}")
                    val uploadTask = photoRef.putBytes(compressedImageData).await()
                    uploadTask.storage.downloadUrl.await().toString()
                } else {
                    _photoUrl.value
                }

                // BƯỚC 2: CẬP NHẬT FIREBASE AUTH
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(_fullName.value.trim())
                    .setPhotoUri(finalPhotoUrl?.let { Uri.parse(it) })
                    .build()
                currentUser.updateProfile(profileUpdates).await()

                // BƯỚC 3: CẬP NHẬT FIRESTORE
                val userProfileData = mutableMapOf<String, Any>()
                userProfileData["fullName"] = _fullName.value.trim()
                userProfileData["username"] = _username.value.trim()
                if (_dateOfBirth.value.isNotBlank()) userProfileData["dateOfBirth"] = _dateOfBirth.value
                if (_gender.value != "Giới tính") userProfileData["gender"] = _gender.value
                if (finalPhotoUrl != null) userProfileData["photoUrl"] = finalPhotoUrl

                firestore.collection("users").document(currentUser.uid)
                    .set(userProfileData, SetOptions.merge()).await()
                
                _saveResult.value = Result.success(Unit)

            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun Context.compressImage(uri: Uri): ByteArray {
        val inputStream = this.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditProfileViewModel() as T
            }
        }
    }
}
