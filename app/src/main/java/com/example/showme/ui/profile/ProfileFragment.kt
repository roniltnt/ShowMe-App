package com.example.showme.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.showme.R
import com.example.showme.databinding.FragmentProfileBinding
import com.example.showme.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val root = binding.root

        viewModel.userName.observe(viewLifecycleOwner) {
            binding.textUserName.text = it
        }

        viewModel.userEmail.observe(viewLifecycleOwner) {
            binding.textUserEmail.text = it
        }

        // load profile image
        viewModel.profileImageUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.profileImageView)
            } else {
                binding.profileImageView.setImageResource(com.example.showme.R.drawable.ic_profile_24)
            }
        }

        binding.profileImageView.setOnClickListener {
            openImageChooser()
        }

        // edit username and password
        binding.buttonEditUser.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user, null)
            val editName = dialogView.findViewById<EditText>(R.id.editTextUserName)
            val editPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)

            editName.setText(viewModel.userName.value)

            AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->
                    val newName = editName.text.toString().trim()

                    val newPassword = editPassword.text.toString().trim()

                    val currentPassword = dialogView.findViewById<EditText>(R.id.editTextPassword).text.toString().trim()

                    if (newName.isNotEmpty()) viewModel.updateUserName(newName)

                    if (newPassword.isNotEmpty()) {
                        if (currentPassword.isEmpty()) {
                            Toast.makeText(requireContext(), "Please enter current password to change it", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.reauthenticateAndChangePassword(currentPassword, newPassword, requireContext())
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.buttonLogout.setOnClickListener {
            viewModel.logOut(requireContext())

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.buttonFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
        }

        return root
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "choose picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data

            if (selectedImageUri != null) {
                viewModel.uploadProfileImage(selectedImageUri, requireContext())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
