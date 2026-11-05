package c.app.sociamediabackend.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.FragmentInboxBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.getChildAt(1).setOnClickListener {
            fetchRealMessagesAndInteractions()
        }

        binding.root.getChildAt(2).setOnClickListener {
            fetchRealActivityNotifications()
        }
    }

    private fun fetchRealMessagesAndInteractions() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).getPosts(feedType = "foryou", limit = 5)
                if (res.isSuccessful && res.body()?.success == true) {
                    val posts = res.body()?.posts.orEmpty()
                    val messages = if (posts.isNotEmpty()) {
                        posts.map { "• @${it.author?.username ?: "creator"}: ${it.content}" }.toTypedArray()
                    } else {
                        arrayOf("No new messages or interaction feeds.")
                    }
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("💬 Direct Messages & Feed Interactions")
                        .setItems(messages, null)
                        .setPositiveButton("Close", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Failed to load inbox feed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchRealActivityNotifications() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).getLiveSessions()
                if (res.isSuccessful && res.body()?.success == true) {
                    val sessions = res.body()?.liveSessions.orEmpty()
                    val notifs = if (sessions.isNotEmpty()) {
                        sessions.map { "• LIVE Stream Active: @${it.host?.username ?: "host"} is streaming \"${it.title}\"" }.toTypedArray()
                    } else {
                        arrayOf("No active LIVE streams or notifications.")
                    }
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("🔔 Real-Time Notifications & LIVE Alerts")
                        .setItems(notifs, null)
                        .setPositiveButton("Close", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
