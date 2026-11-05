package c.app.sociamediabackend.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.FragmentProfileBinding
import c.app.sociamediabackend.ui.analytics.AnalyticsActivity
import c.app.sociamediabackend.ui.auth.LoginActivity
import c.app.sociamediabackend.ui.live.CreatorWalletActivity
import c.app.sociamediabackend.ui.live.GoLiveStudioActivity
import c.app.sociamediabackend.ui.live.LiveSetupActivity
import c.app.sociamediabackend.ui.settings.SettingsActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager
    private lateinit var gridAdapter: ProfileGridAdapter

    private var showOnlyMedia = false

    private val selectAvatarLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { uploadAvatar(it) }
    }

    private val selectCoverLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { uploadCover(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        gridAdapter = ProfileGridAdapter { post ->
            deletePost(post.id)
        }
        binding.rvUserPosts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvUserPosts.adapter = gridAdapter

        binding.cvAvatar.setOnClickListener {
            selectAvatarLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnChangeCover.setOnClickListener {
            selectCoverLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnEditProfile.setOnClickListener {
            val editSheet = EditProfileBottomSheetDialogFragment {
                loadProfile()
            }
            editSheet.show(childFragmentManager, EditProfileBottomSheetDialogFragment.TAG)
        }

        binding.btnGoLive.setOnClickListener {
            startActivity(Intent(requireContext(), LiveSetupActivity::class.java))
        }

        binding.btnCreatorWallet.setOnClickListener {
            startActivity(Intent(requireContext(), CreatorWalletActivity::class.java))
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.cvStats.setOnClickListener {
            startActivity(Intent(requireContext(), AnalyticsActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }

        binding.chipGroupProfile.setOnCheckedChangeListener { _, checkedId ->
            showOnlyMedia = (checkedId == R.id.chipMedia)
            loadProfile()
        }

        loadProfile()
    }

    private fun loadProfile() {
        val user = sessionManager.getUser() ?: return

        binding.tvDisplayName.text = user.displayName ?: user.username
        binding.tvUsername.text = "@${user.username}"
        binding.tvBio.text = if (!user.bio.isNullOrEmpty()) user.bio else "No bio added yet"

        val resolvedAvatarUrl = resolveUrl(user.avatarUrl, sessionManager)
        if (!resolvedAvatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(resolvedAvatarUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        val resolvedCoverUrl = resolveUrl(user.coverUrl, sessionManager)
        if (!resolvedCoverUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(resolvedCoverUrl)
                .into(binding.ivCover)
        }

        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(requireContext()).getProfile(user.username)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()?.user
                    updatedUser?.let {
                        sessionManager.saveUser(it)
                        binding.tvPostsCount.text = (it.count?.posts ?: 0).toString()
                        binding.tvFollowersCount.text = (it.count?.followers ?: 0).toString()
                        binding.tvFollowingCount.text = (it.count?.following ?: 0).toString()
                    }
                }

                // Filter user's posts
                val postsRes = ApiClient.getService(requireContext()).getPosts()
                if (postsRes.isSuccessful && postsRes.body()?.success == true) {
                    val allPosts = postsRes.body()?.posts.orEmpty()
                    var myPosts = allPosts.filter { it.author?.username == user.username }

                    if (showOnlyMedia) {
                        myPosts = myPosts.filter { !it.imageUrl.isNullOrEmpty() || !it.videoUrl.isNullOrEmpty() }
                    }

                    gridAdapter.setPosts(myPosts)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading profile details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePost(postId: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getService(requireContext()).deletePost(postId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                    loadProfile()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadAvatar(uri: Uri) {
        val file = uriToFile(uri, "avatar_${System.currentTimeMillis()}.jpg") ?: return
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, reqFile)

        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).uploadImage(body)
                if (res.isSuccessful && res.body()?.success == true) {
                    val avatarUrl = res.body()?.url ?: return@launch
                    val updateRes = ApiClient.getService(requireContext()).updateProfile(mapOf("avatarUrl" to avatarUrl))
                    if (updateRes.isSuccessful) {
                        Toast.makeText(requireContext(), "Avatar updated!", Toast.LENGTH_SHORT).show()
                        loadProfile()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to upload avatar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadCover(uri: Uri) {
        val file = uriToFile(uri, "cover_${System.currentTimeMillis()}.jpg") ?: return
        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, reqFile)

        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).uploadImage(body)
                if (res.isSuccessful && res.body()?.success == true) {
                    val coverUrl = res.body()?.url ?: return@launch
                    val updateRes = ApiClient.getService(requireContext()).updateProfile(mapOf("coverUrl" to coverUrl))
                    if (updateRes.isSuccessful) {
                        Toast.makeText(requireContext(), "Cover photo updated!", Toast.LENGTH_SHORT).show()
                        loadProfile()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to upload cover", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(uri: Uri, fileName: String): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveUrl(rawUrl: String?, sessionManager: SessionManager): String? {
        if (rawUrl.isNullOrEmpty()) return null
        val baseUrl = sessionManager.getBaseUrl()
        val serverHost = baseUrl.substringBefore("api/")

        return if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            "${serverHost}${rawUrl.removePrefix("/")}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
