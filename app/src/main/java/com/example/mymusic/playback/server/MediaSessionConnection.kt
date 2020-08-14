package com.example.mymusic.playback.server

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.example.mymusic.models.QueueData


interface MediaSessionConnection {

    val rootMediaId: String
    val transportControls: MediaControllerCompat.TransportControls
    var mediaController: MediaControllerCompat

    fun connected(connected: OnConnected)

    fun connectedForPlayer(connected: OnConnected)

    fun postPlaybackState(playbackState: UpdatePlaybackState)

    fun postNowPlayingData(playingData: UpdateNowPlayingData)

    fun postQueueData(queueData: UpdateQueueData)

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback)

//    fun invokeMediaControllerCallback()
}

class MediaSessionConnectionImpl(
    context: Context,
    serviceComponent: ComponentName
) : MediaSessionConnection {


    private var isConnectedCallback : OnConnected = {}
    private var isConnectedPlayer: OnConnected = {}

    private var updatePlaybackState : UpdatePlaybackState = {}
    private var updateNowPlayingData : UpdateNowPlayingData = {}
    private var updateQueueData : UpdateQueueData = {}

    override val rootMediaId: String
        get() = mediaBrowser.root

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    // Connect client to server
    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback, null
    ).apply { connect() }

    override lateinit var mediaController: MediaControllerCompat

    override fun connected(connected: OnConnected) {
        this.isConnectedCallback = connected
    }

    override fun connectedForPlayer(connected: OnConnected) {
        this.isConnectedPlayer = connected
    }

    override fun postPlaybackState(playbackState: UpdatePlaybackState) {
        Log.d("Order", "MediaSessionConnection playbackState")
        this.updatePlaybackState = playbackState
    }

    override fun postNowPlayingData(playingData: UpdateNowPlayingData) {
        Log.d("Order", "MediaSessionConnection postNowPlayingData")
        this.updateNowPlayingData = playingData
    }

    override fun postQueueData(queueData: UpdateQueueData) {
        Log.d("Order", "MediaSessionConnection postQueueData")
        this.updateQueueData = queueData
    }

    override fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    override fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    override val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls


    private val mediaControllerCallback: MediaControllerCallback = MediaControllerCallback()

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(mediaControllerCallback)
            }
            isConnectedCallback(this@MediaSessionConnectionImpl, true)
            isConnectedPlayer(this@MediaSessionConnectionImpl, true)
        }

        override fun onConnectionSuspended() {
            isConnectedCallback(this@MediaSessionConnectionImpl, false)
            isConnectedPlayer(this@MediaSessionConnectionImpl, false)
        }

        override fun onConnectionFailed() {
            isConnectedCallback(this@MediaSessionConnectionImpl, false)
            isConnectedPlayer(this@MediaSessionConnectionImpl, false)
        }

    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            updatePlaybackState.invoke(this@MediaSessionConnectionImpl, state ?: EMPTY_PLAYBACK_STATE)
            Log.d("Playbackstate is ", state.toString())
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            Log.d("Hello ", "World")
            super.onMetadataChanged(metadata)
            updateNowPlayingData.invoke(this@MediaSessionConnectionImpl, metadata ?: NOTHING_PLAYING)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            updateQueueData.invoke(this@MediaSessionConnectionImpl, QueueData().fromMediaController(mediaController))
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        public var instance: MediaSessionConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) : MediaSessionConnection =
            instance ?: synchronized(this) {
                instance ?: MediaSessionConnectionImpl(context, serviceComponent)
                    .also { instance = it }
                return instance!!
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