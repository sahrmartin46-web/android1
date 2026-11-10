package c.app.sociamediabackend.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import c.app.sociamediabackend.MainActivity
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.model.RegisterRequest
import c.app.sociamediabackend.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        sessionManager = SessionManager(this)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val displayName = binding.etDisplayName.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        if (username.length < 3) {
            binding.tilUsername.error = "Min 3 characters"
            return
        } else {
            binding.tilUsername.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required"
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 8) {
            binding.tilPassword.error = "Min 8 characters"
            return
        } else {
            binding.tilPassword.error = null
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val req = RegisterRequest(
                    username = username,
                    email = email,
                    password = password,
                    displayName = if (displayName.isNotEmpty()) displayName else username
                )

                val response = ApiClient.getService(this@RegisterActivity).register(req)
                setLoading(false)

                if (response.isSuccessful && response.body()?.success == true) {
                    val authResponse = response.body()!!
                    authResponse.token?.let { sessionManager.saveAuthToken(it) }
                    authResponse.user?.let { sessionManager.saveUser(it) }

                    Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finishAffinity()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        val json = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        json.get("message")?.asString ?: errorBody ?: "Registration failed."
                    } catch (e: Exception) {
                        errorBody ?: "Registration failed."
                    }
                    Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@RegisterActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}
