package c.app.sociamediabackend.ui.live

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import c.app.sociamediabackend.R
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.FragmentLiveDiscoveryBinding
import kotlinx.coroutines.launch

class LiveDiscoveryFragment : Fragment() {

    private var _binding: FragmentLiveDiscoveryBinding? = null
    private val binding get() = _binding!!

    private lateinit var liveAdapter: LiveStreamsAdapter
    private var selectedCategory: String = "All"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLiveDiscoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        liveAdapter = LiveStreamsAdapter { session ->
            val intent = Intent(requireContext(), LiveViewerActivity::class.java).apply {
                putExtra("STREAM_ID", session.id)
                putExtra("HOST_NAME", session.host?.displayName ?: session.host?.username ?: "Creator")
            }
            startActivity(intent)
        }

        binding.rvLiveStreams.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvLiveStreams.adapter = liveAdapter

        binding.btnRefreshLive.setOnClickListener {
            loadLiveStreams()
        }

        binding.chipGroupCategories.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.chipCategoryAll -> "All"
                R.id.chipCategoryGaming -> "Gaming"
                R.id.chipCategoryMusic -> "Music"
                R.id.chipCategorySports -> "Sports"
                R.id.chipCategoryEdu -> "Education"
                else -> "All"
            }
            loadLiveStreams()
        }

        loadLiveStreams()
    }

    private fun loadLiveStreams() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).getLiveSessions(selectedCategory)
                if (res.isSuccessful && res.body()?.success == true) {
                    val sessions = res.body()?.liveSessions.orEmpty()
                    liveAdapter.setSessions(sessions)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading live streams", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
