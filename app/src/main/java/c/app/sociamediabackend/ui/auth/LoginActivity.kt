package c.app.sociamediabackend.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import c.app.sociamediabackend.MainActivity
import c.app.sociamediabackend.data.api.ApiClient
import c.app.sociamediabackend.data.api.SessionManager
import c.app.sociamediabackend.data.repository.AuthRepository
import c.app.sociamediabackend.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        sessionManager = SessionManager(this)

        // Temporarily bypass login authentication
        if (sessionManager.getAuthToken().isNullOrEmpty()) {
            sessionManager.saveAuthToken("guest_bypass_token")
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        return

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val apiService = ApiClient.getService(this)
        val repository = AuthRepository(apiService)
        viewModel = AuthViewModel(repository)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeViewModel()
    }

    private fun performLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        viewModel.login(email, password)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginUiState.Idle -> {
                            setLoading(false)
                        }
                        is LoginUiState.Loading -> {
                            setLoading(true)
                        }
                        is LoginUiState.Success -> {
                            setLoading(false)
                            val authResponse = state.authResponse
                            authResponse.token?.let { sessionManager.saveAuthToken(it) }
                            authResponse.user?.let { sessionManager.saveUser(it) }

                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        is LoginUiState.Error -> {
                            setLoading(false)
                            if (state.message == "Email required") {
                                binding.tilEmail.error = state.message
                            } else if (state.message == "Password required") {
                                binding.tilPassword.error = state.message
                            } else {
                                Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
