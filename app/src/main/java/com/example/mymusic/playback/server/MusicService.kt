package com.example.mymusic.playback.server

import android.app.PendingIntent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
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
import com.example.mymusic.repository.SongReposImpl
import com.example.mymusic.repository.SongRepository
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

        const val NOTIFICATION_ID = 888
    }

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    private val lifecycle = LifecycleRegistry(this)
    private lateinit var repository: SongRepository

    override fun onCreate() {
        super.onCreate()
        lifecycle.currentState = Lifecycle.State.RESUMED
        Log.d("onCreate MusicService", "RESUME")

        repository = SongReposImpl(baseContext.contentResolver)
        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, APP_PACKAGE_NAME).apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // My SessionCallback() has methods that handle callbacks from a media controller
            setCallback(MediaSessionCallback())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
            Log.d("setSessionToken ", "is call")
            val sessionIntent =
                baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
            val sessionActivityPendingIntent =
                PendingIntent.getActivity(baseContext, 0, sessionIntent, 0)
            setSessionActivity(sessionActivityPendingIntent)
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
}

