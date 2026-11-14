package c.app.sociamediabackend

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.databinding.ActivityMainBinding
import c.app.sociamediabackend.ui.auth.LoginActivity
import c.app.sociamediabackend.ui.main.DiscoverFragment
import c.app.sociamediabackend.ui.main.FeedFragment
import c.app.sociamediabackend.ui.main.InboxFragment
import c.app.sociamediabackend.ui.main.ProfileFragment
import c.app.sociamediabackend.ui.sound.CameraStudioActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Set icon tint for standard navigation items while keeping the center (+) button multi-color
        binding.bottomNavigation.itemIconTintList = ContextCompat.getColorStateList(this, R.color.nav_item_color_state)
        binding.bottomNavigation.menu.findItem(R.id.navigation_create_post).iconTintList = null

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, 0, 0, navigationBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            replaceFragment(FeedFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_feed -> {
                    replaceFragment(FeedFragment())
                    true
                }
                R.id.navigation_discover -> {
                    replaceFragment(DiscoverFragment())
                    true
                }
                R.id.navigation_create_post -> {
                    startActivity(Intent(this, CameraStudioActivity::class.java))
                    false
                }
                R.id.navigation_inbox -> {
                    replaceFragment(InboxFragment())
                    true
                }
                R.id.navigation_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
