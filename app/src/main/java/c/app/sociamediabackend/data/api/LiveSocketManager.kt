package c.app.sociamediabackend.data.api

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object LiveSocketManager {
    private const val TAG = "LiveSocketManager"
    private const val SOCKET_URL = "http://10.0.2.2:5000"
    private var socket: Socket? = null

    fun connect(context: Context, onConnected: () -> Unit = {}) {
        if (socket?.connected() == true) {
            onConnected()
            return
        }
        try {
            val sessionManager = SessionManager(context)
            val token = sessionManager.getAuthToken() ?: ""
            val baseUrl = sessionManager.getBaseUrl()
            val socketUrl = if (baseUrl.endsWith("/api/")) {
                baseUrl.substringBefore("/api/")
            } else if (baseUrl.endsWith("/api")) {
                baseUrl.substringBefore("/api")
            } else {
                baseUrl
            }

            val opts = IO.Options().apply {
                auth = mapOf("token" to token)
                transports = arrayOf("websocket")
            }
            socket = IO.socket(socketUrl, opts)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "Socket connected: ${socket?.id()}")
                onConnected()
            }?.on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "Socket disconnected")
            }?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e(TAG, "Socket connect error: ${args.getOrNull(0)}")
            }

            socket?.connect()
        } catch (e: URISyntaxException) {
            Log.e(TAG, "URI syntax exception", e)
        }
    }

    fun joinLiveSession(liveSessionId: String, userId: String) {
        try {
            val data = JSONObject().apply {
                put("liveSessionId", liveSessionId)
                put("userId", userId)
            }
            socket?.emit("live.join", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining live session", e)
        }
    }

    fun sendComment(liveSessionId: String, userId: String, text: String) {
        try {
            val data = JSONObject().apply {
                put("liveSessionId", liveSessionId)
                put("userId", userId)
                put("text", text)
            }
            socket?.emit("live.comment", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending comment", e)
        }
    }

    fun sendLike(liveSessionId: String, userId: String) {
        try {
            val data = JSONObject().apply {
                put("liveSessionId", liveSessionId)
                put("userId", userId)
            }
            socket?.emit("live.like", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending like", e)
        }
    }

    fun sendGift(liveSessionId: String, giftSlug: String, quantity: Int) {
        try {
            val data = JSONObject().apply {
                put("liveSessionId", liveSessionId)
                put("giftSlug", giftSlug)
                put("quantity", quantity)
            }
            socket?.emit("live.gift", data)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending gift", e)
        }
    }

    fun onEvent(eventName: String, listener: io.socket.emitter.Emitter.Listener) {
        socket?.on(eventName, listener)
    }

    fun offEvent(eventName: String) {
        socket?.off(eventName)
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
}
