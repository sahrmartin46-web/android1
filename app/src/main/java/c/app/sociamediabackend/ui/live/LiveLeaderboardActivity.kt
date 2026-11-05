package c.app.sociamediabackend.ui.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.databinding.ActivityLiveLeaderboardBinding
import c.app.sociamediabackend.databinding.ItemLeaderboardUserBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.launch

class LiveLeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLiveLeaderboardBinding
    private val adapter = LeaderboardAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLiveLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.setHasFixedSize(true)
        binding.rvLeaderboard.adapter = adapter

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                val res = ApiClient.getService(this@LiveLeaderboardActivity).getLiveSessions()
                if (res.isSuccessful && res.body()?.success == true) {
                    val sessions = res.body()?.liveSessions.orEmpty()
                    val leaderboardUsers = sessions.mapIndexed { index, session ->
                        LeaderboardUser(
                            rank = "#${index + 1}",
                            username = session.host?.username ?: "User",
                            contribution = "💎 ${session.totalDiamonds ?: 0} Diamonds",
                            avatarUrl = session.host?.avatarUrl.orEmpty()
                        )
                    }
                    adapter.setUsers(leaderboardUsers)
                } else {
                    adapter.setUsers(emptyList())
                }
            } catch (e: Exception) {
                adapter.setUsers(emptyList())
                Toast.makeText(this@LiveLeaderboardActivity, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class LeaderboardUser(val rank: String, val username: String, val contribution: String, val avatarUrl: String)

class LeaderboardAdapter(private val users: MutableList<LeaderboardUser>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    fun setUsers(newUsers: List<LeaderboardUser>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemLeaderboardUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val u = users[position]
        holder.binding.tvRank.text = u.rank
        holder.binding.tvUsername.text = "@${u.username}"
        holder.binding.tvContribution.text = u.contribution
        if (u.avatarUrl.isNotEmpty()) {
            Glide.with(holder.binding.root.context)
                .load(u.avatarUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(holder.binding.ivAvatar)
        }
    }

    override fun getItemCount(): Int = users.size
}
