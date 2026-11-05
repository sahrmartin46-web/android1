package c.app.sociamediabackend.data.api

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

@OptIn(UnstableApi::class)
object DashPlayerManager {

    fun createAdaptivePlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    fun buildMediaSource(context: Context, url: String): MediaSource {
        val uri = Uri.parse(url)
        val dataSourceFactory = DefaultDataSource.Factory(context)

        return if (url.endsWith(".mpd", ignoreCase = true) || url.contains(".mpd")) {
            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .build()
            DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        } else {
            val mediaItem = MediaItem.fromUri(uri)
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
    }
}
