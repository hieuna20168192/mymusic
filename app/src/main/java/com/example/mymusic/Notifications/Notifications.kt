package com.example.mymusic.Notifications

import android.app.*
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata.METADATA_KEY_ALBUM
import android.media.MediaMetadata.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import androidx.media.session.MediaButtonReceiver
import androidx.palette.graphics.Palette
import com.example.mymusic.R
import com.example.mymusic.constants.Constants.ACTION_NEXT
import com.example.mymusic.constants.Constants.ACTION_PLAY_PAUSE
import com.example.mymusic.constants.Constants.ACTION_PREVIOUS
import com.example.mymusic.extensions.isPlaying
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.playback.server.MusicService
import com.example.mymusic.ui.activities.MainActivity
import com.example.mymusic.util.Utils.isOreo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis

private const val CHANNEL_ID = "music_channel_01"
private const val NOTIFICATION_ID = 888

interface Notifications {

    fun updateNotification(mediaSession: MediaSessionCompat)

    fun buildNotification(mediaSession: MediaSessionCompat): Notification
}

class NotificationImpl(
    private val context: Context,
    private val notificationManager: NotificationManager
) : Notifications {
    private var postTime: Long = -1

    override fun updateNotification(mediaSession: MediaSessionCompat) {
        GlobalScope.launch {
            notificationManager.notify(NOTIFICATION_ID, buildNotification(mediaSession))
        }
    }

    override fun buildNotification(mediaSession: MediaSessionCompat): Notification {
        if (mediaSession.controller.metadata == null || mediaSession.controller.playbackState == null) {
            return getEmptyNotification()
        }

        val albumName = mediaSession.controller.metadata.getString(METADATA_KEY_ALBUM)
        val artistName = mediaSession.controller.metadata.getString(METADATA_KEY_ARTIST)
        val trackName =
            mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        val artwork =
            mediaSession.controller.metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
        val isPlaying = mediaSession.isPlaying()

        val playButtonResId = if (isPlaying) {
            R.drawable.ic_pause_white_24dp
        } else {
            R.drawable.ic_play_arrow_white_24dp
        }

        val nowPlayingIntent = Intent(context, MainActivity::class.java)
        val clickIntent =
            PendingIntent.getActivity(context, 0, nowPlayingIntent, FLAG_UPDATE_CURRENT)

        if (postTime == -1L) {
            postTime = currentTimeMillis()
        }
        createNotificationChannel()

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 1, 2)
            .setCancelButtonIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(style)
            setSmallIcon(R.drawable.ic_notification)
            setLargeIcon(artwork)
            setContentIntent(clickIntent)
            setContentTitle(trackName)
            setContentText(artistName)
            setSubText(albumName)
            setColorized(true)
            setShowWhen(false)
            setWhen(postTime)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            addAction(getPreviousAction(context))
            addAction(getPlayPauseAction(context, playButtonResId))
            addAction(getNextAction(context))
        }

        if (artwork != null) {
            builder.color = Palette.from(artwork)
                .generate()
                .getVibrantColor("#403f4d".toColorInt())
        }

        return builder.build()
    }

    private fun getPreviousAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply {
            action = ACTION_PREVIOUS
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, 0)
        return NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp, "", pendingIntent)
    }

    private fun getPlayPauseAction(context: Context, @IdRes playButtonResId: Int): NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, 0)
        return NotificationCompat.Action(playButtonResId, "", pendingIntent)
    }

    private fun getNextAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, MusicService::class.java).apply {
            action = ACTION_NEXT
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, 0)
        return NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp, "", pendingIntent)
    }

    private fun getEmptyNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_notification)
            setContentTitle("My Music")
            setColorized(true)
            setShowWhen(false)
            setWhen(postTime)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }.build()
    }

    private fun createNotificationChannel() {
        if (!isOreo()) return
        val name = context.getString(R.string.media_playback)
        val channel = NotificationChannel(CHANNEL_ID, name, IMPORTANCE_LOW).apply {
            description = context.getString(R.string.media_playback_controls)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        public var instance: NotificationImpl? = null

        fun getInstance(context: Context, notificationManager: NotificationManager) : NotificationImpl =
            instance ?: synchronized(this) {
                instance ?: NotificationImpl(context, notificationManager)
                    .also { instance = it }
                return instance!!
            }
    }
}