package com.example.mymusic.playback.server

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media.MediaBrowserServiceCompat
import com.example.mymusic.R
import com.example.mymusic.constants.Constants.APP_PACKAGE_NAME
import com.example.mymusic.extensions.toRawMediaItems
import com.example.mymusic.models.MediaID
import com.example.mymusic.models.MediaID.Companion.CALLER_OTHER
import com.example.mymusic.models.MediaID.Companion.CALLER_SELF
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.util.InjectorUtils
import com.example.mymusic.util.Utils.EMPTY_ALBUM_ART_URI

class MusicService :
    MediaBrowserServiceCompat(), LifecycleOwner {

    companion object {
        const val MEDIA_ID_ARG = "MEDIA_ID"
        const val MEDIA_TYPE_ARG = "MEDIA_TYPE"
        const val MEDIA_CALLER = "MEDIA_CALLER"
        const val MEDIA_ID_ROOT = -1
        const val TYPE_ALL_ARTISTS = 0
        const val TYPE_ALL_ALBUMS = 1
        const val TYPE_ALL_SONGS = 2
        const val TYPE_ALL_PLAYLISTS = 3
        const val TYPE_SONG = 9
        const val TYPE_ALBUM = 10
        const val TYPE_ARTIST = 11
        const val TYPE_PLAYLIST = 12
        const val TYPE_ALL_FOLDERS = 13
        const val TYPE_ALL_GENRES = 14
        const val TYPE_GENRE = 15
        const val MEDIA_ID_NAV_SONG = 16

        const val NOTIFICATION_ID = 888
    }

    private lateinit var player: SongPlayer
    private lateinit var connection: MediaSessionConnection
    private val lifecycle = LifecycleRegistry(this)
    private lateinit var repository: SongRepository

    override fun onCreate() {
        super.onCreate()

        // Inject player
        player = InjectorUtils.provideSongPlayer(applicationContext)


        // Inject Repos
        repository = InjectorUtils.provideSongRepository(applicationContext)

        lifecycle.currentState = Lifecycle.State.RESUMED
        Log.d("onCreateMusicService", "RESUME")

        sessionToken = player.getSession().sessionToken
        player.onPlayingState { isPlaying ->
            Log.d("Order", "onCreateMusicService TimberMusicService isPlaying is $isPlaying")
            if (isPlaying) {
                // Register becoming Noisy Receiver
            } else {
                // Unregister Noisy Receiver
            }
            // Update Notification
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
//        Log.d("parentId ", parentId)
        result.detach()

        val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        val mediaIdParent = MediaID().fromString(parentId)

        val mediaType = mediaIdParent.type
        val caller = mediaIdParent.caller

        if (mediaType == MEDIA_ID_ROOT.toString()) {
            addMediaRoots(mediaItems, caller!!)
            Log.d("Nav", "MEDIA_ID_ROOT")
        } else {
            when (mediaType?.toInt() ?: 0) {
                TYPE_ALL_SONGS -> {
                    Log.d("Nav", "TYPE_ALL_SONGS")
//                     Fetch data from external media storage and save it into Song dir
                    mediaItems.addAll(repository.loadSongs(caller))
                }
            }
        }

        if (caller == CALLER_SELF) {
            Log.d("mediaItems.size() = ", mediaItems.size.toString())
            result.sendResult(mediaItems)
        } else {
            result.sendResult(mediaItems.toRawMediaItems())
        }
    }

    private fun addMediaRoots(
        mMediaRoot: MutableList<MediaBrowserCompat.MediaItem>,
        caller: String
    ) {
        mMediaRoot.add(
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder().apply {
                    setMediaId(MediaID(TYPE_ALL_SONGS.toString(), null, caller).asString())
                    setTitle(getString(R.string.songs))
                    setIconUri(Uri.parse(EMPTY_ALBUM_ART_URI))
                    setSubtitle(getString(R.string.songs))
                }.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
            )
        )
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//
//        if (intent == null) {
//            return START_STICKY
//        }
//
//        val mediaSession = mediaSession
//        val controller = mediaSession!!.controller
//
//        when (intent.action) {
//            ACTION_PLAY_PAUSE -> {
//                controller.playbackState?.let { playbackState ->
//                    when {
//                        playbackState.isPlaying -> controller.transportControls.pause()
//                        playbackState.isPlaying -> controller.transportControls.play()
//                    }
//                }
//            }
//            ACTION_NEXT -> {
//                controller.transportControls.skipToNext()
//            }
//            ACTION_PREVIOUS -> {
//                controller.transportControls.skipToPrevious()
//            }
//        }
//        MediaButtonReceiver.handleIntent(mediaSession, intent)
//        return START_STICKY
//    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        val caller = if (clientPackageName == APP_PACKAGE_NAME) {
            CALLER_SELF
        } else {
            CALLER_OTHER
        }

        Log.d("Root folder: ", MediaID(MEDIA_ID_ROOT.toString(), null, caller).asString(), null)
        return BrowserRoot(
            MediaID(
                MEDIA_ID_ROOT.toString(),
                null,
                caller
            ).asString(), null
        )
    }

    override fun getLifecycle() = lifecycle

    override fun onDestroy() {
        lifecycle.currentState = Lifecycle.State.DESTROYED
        player.release()
        super.onDestroy()
    }

}

