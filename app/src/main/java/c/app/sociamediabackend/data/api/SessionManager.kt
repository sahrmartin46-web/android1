package c.app.sociamediabackend.data.api

import android.content.Context
import android.content.SharedPreferences
import c.app.sociamediabackend.data.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "social_media_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_BASE_URL = "base_url"
        
        // Default base URL uses 10.0.2.2 to connect to host machine's localhost from Android emulators
        const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/api/"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUser(user: User) {
        val json = gson.toJson(user)
        prefs.edit().putString(KEY_USER_DATA, json).apply()
    }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun saveBaseUrl(url: String) {
        var formattedUrl = url.trim()
        if (!formattedUrl.endsWith("/")) {
            formattedUrl += "/"
        }
        if (!formattedUrl.endsWith("api/")) {
            formattedUrl += "api/"
        }
        prefs.edit().putString(KEY_BASE_URL, formattedUrl).apply()
    }

    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    fun clearSession() {
        val currentBaseUrl = getBaseUrl()
        prefs.edit().clear().apply()
        saveBaseUrl(currentBaseUrl)
    }
}
