package com.example.mymusic.playback.server

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

interface MediaSessionConnection {
    var isConnected: Boolean
    val rootMediaId: String?
    val transportControls: MediaControllerCompat.TransportControls
    var playbackState: PlaybackStateCompat
    var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)
}

class MediaSessionConnectionImpl(
    context: Context,
    serviceComponent: ComponentName
) : MediaSessionConnection {

    override var isConnected: Boolean = false

    override var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    // Connect client to server
    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback, null
    ).apply { connect() }

    override var rootMediaId: String? = null

    override lateinit var mediaController: MediaControllerCompat
    override fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    override fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    override val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            Log.d("parentId ", parentId)

        }
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            isConnected = true
            rootMediaId = mediaBrowser.root
            subscribe(rootMediaId.toString(), subscriptionCallback)
        }

        override fun onConnectionSuspended() {
            isConnected = false
        }

        override fun onConnectionFailed() {
            isConnected = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState = state ?: EMPTY_PLAYBACK_STATE
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MediaSessionConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
            instance ?: synchronized(this) {
                instance ?: MediaSessionConnectionImpl(context, serviceComponent)
                    .also { instance = it }
            }
    }
}


@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()