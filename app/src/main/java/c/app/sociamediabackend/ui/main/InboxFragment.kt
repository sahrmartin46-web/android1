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
import kotlinx.coroutines.async
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
            fetchAdvancedMessagesCenter()
        }

        binding.root.getChildAt(2).setOnClickListener {
            fetchAdvancedNotificationCenter()
        }
    }

    private fun fetchAdvancedMessagesCenter() {
        Toast.makeText(requireContext(), "Loading Direct Messages...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(requireContext()).getPosts(feedType = "foryou", limit = 10)
                if (res.isSuccessful && res.body()?.success == true) {
                    val posts = res.body()?.posts.orEmpty()
                    val messages = if (posts.isNotEmpty()) {
                        posts.map { "💬 Feed Interaction\n🕒 Just now" }.toTypedArray()
                    } else {
                        arrayOf("💬 No new messages or feed interactions yet.")
                    }
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("💬 Advanced Message & Interaction Center")
                        .setItems(messages, null)
                        .setPositiveButton("Refresh") { _, _ -> fetchAdvancedMessagesCenter() }
                        .setNegativeButton("Close", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAdvancedNotificationCenter() {
        Toast.makeText(requireContext(), "Fetching Real-Time Notifications...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val liveRes = async { ApiClient.getService(requireContext()).getLiveSessions() }
                val liveData = liveRes.await()

                val notificationsList = mutableListOf<String>()

                if (liveData.isSuccessful && liveData.body()?.success == true) {
                    val sessions = liveData.body()?.liveSessions.orEmpty()
                    for (session in sessions) {
                        notificationsList.add("🔴 LIVE Alert: Active stream detected!")
                    }
                }

                if (notificationsList.isEmpty()) {
                    notificationsList.add("🔔 All caught up! No new notifications at the moment.")
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("🔔 Advanced Notification & LIVE Alert Center")
                    .setItems(notificationsList.toTypedArray(), null)
                    .setPositiveButton("Refresh") { _, _ -> fetchAdvancedNotificationCenter() }
                    .setNegativeButton("Close", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading notifications: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
