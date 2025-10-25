package com.example.showme.ui.profile

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _profileImageUri = MutableLiveData<Uri?>()
    val profileImageUri: LiveData<Uri?> = _profileImageUri

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        _userName.value = user?.displayName ?: "Unknown User"
        _userEmail.value = user?.email ?: "No email"
        _profileImageUri.value = user?.photoUrl
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("profile_images/${UUID.randomUUID()}.jpg")

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    _profileImageUri.value = downloadUri

                    // update in Firebase auth
                    auth.currentUser?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUri)
                            .build()
                    )?.addOnSuccessListener {
                        Toast.makeText(context, "Profile image updated!", Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener {
                        Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateUserName(newName: String) {
        val user = auth.currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _userName.value = newName
            }
        }
    }

    fun reauthenticateAndChangePassword(currentPass: String, newPass: String, context: Context) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, currentPass)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPass).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to update password: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Authentication failed. Wrong current password?", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun logOut(context: Context) {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

}
