package c.app.sociamediabackend.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.DialogEditProfileBinding
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EditProfileBottomSheetDialogFragment(
    private val onProfileUpdated: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private var selectedAvatarUri: Uri? = null

    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.ivEditAvatar)
            Toast.makeText(requireContext(), "New avatar selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val user = sessionManager.getUser()

        user?.let {
            binding.etDisplayName.setText(it.displayName ?: it.username)
            binding.etUsername.setText(it.username)
            binding.etBio.setText(it.bio ?: "")

            if (!it.avatarUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(it.avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.ivEditAvatar)
            }
        }

        val openPhotoPicker = View.OnClickListener {
            pickAvatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.ivEditAvatar.setOnClickListener(openPhotoPicker)
        binding.btnChangeAvatar.setOnClickListener(openPhotoPicker)
        binding.tvChangePhoto.setOnClickListener(openPhotoPicker)

        binding.btnSaveProfile.setOnClickListener {
            val displayName = binding.etDisplayName.text?.toString()?.trim().orEmpty()
            val username = binding.etUsername.text?.toString()?.trim().orEmpty()
            val bio = binding.etBio.text?.toString()?.trim().orEmpty()

            saveProfile(displayName, username, bio)
        }
    }

    private fun saveProfile(displayName: String, username: String, bio: String) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                var uploadedAvatarUrl: String? = sessionManager.getUser()?.avatarUrl

                if (selectedAvatarUri != null) {
                    val avatarFile = uriToTempFile(selectedAvatarUri!!)
                    if (avatarFile != null) {
                        val requestFile = avatarFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("image", avatarFile.name, requestFile)
                        val uploadRes = ApiClient.getService(requireContext()).uploadImage(body)
                        if (uploadRes.isSuccessful && uploadRes.body()?.success == true) {
                            uploadedAvatarUrl = uploadRes.body()?.url
                        }
                    }
                }

                val updateData = mutableMapOf(
                    "displayName" to displayName,
                    "bio" to bio
                )
                if (username.isNotEmpty()) {
                    updateData["username"] = username
                }
                if (!uploadedAvatarUrl.isNullOrEmpty()) {
                    updateData["avatarUrl"] = uploadedAvatarUrl
                }

                val response = ApiClient.getService(requireContext()).updateProfile(updateData)
                setLoading(false)

                if (response.isSuccessful && response.body()?.user != null) {
                    sessionManager.saveUser(response.body()!!.user!!)
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    onProfileUpdated()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToTempFile(uri: Uri): File? {
        return try {
            val tempFile = File(requireContext().cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            if (tempFile.exists() && tempFile.length() > 0) tempFile else null
        } catch (_: Exception) {
            null
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditProfileBottomSheet"
    }
}
