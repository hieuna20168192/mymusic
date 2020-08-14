package com.example.mymusic.presenter

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import com.example.mymusic.extensions.id
import com.example.mymusic.extensions.isPlayEnabled
import com.example.mymusic.extensions.isPlaying
import com.example.mymusic.extensions.isPrepared
import com.example.mymusic.models.MediaData
import com.example.mymusic.models.MediaID
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.ui.fragments.BottomSheetFragment
import com.example.mymusic.ui.fragments.MediaItemFragment

class PlayerPresenter(
    private var view: MainContract.BottomSheetView?,
    private val repos: SongRepository,
    private val mediaSessionConnection: MediaSessionConnection

) : MainContract.PlayerPresenter {

    private var isPrepared: Boolean = false
    private lateinit var nowPlaying: MediaMetadataCompat
    private lateinit var currentState: PlaybackStateCompat
    private lateinit var currentMedia: MediaData

    init {
        mediaSessionConnection.connectedForPlayer { isConnected ->
            if (isConnected) {
                view?.bindProgress(mediaSessionConnection.mediaController)
            } else {
                null
            }
        }
    }

    companion object {

        // For Singleton instantiation.
        @Volatile
        private var instance: PlayerPresenter? = null

        fun getInstance(
            view: MainContract.BottomSheetView?,
            repos: SongRepository,
            mediaSessionConnection: MediaSessionConnection
        ): PlayerPresenter {
            instance ?: synchronized(this) {
                instance ?: PlayerPresenter(view, repos, mediaSessionConnection)
                    .also { instance = it }
            }
            return instance!!
        }
    }

    override fun playMedia(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
        Log.d("PlayMedia", "is running")
        mediaSessionConnection.postPlaybackState { state ->
            if (state != null) {
                val playbackState: MediaData = MediaData().pullPlaybackState(state)
                isPrepared = state.isPrepared
                view?.bindViewByState(playbackState)
                currentState = state
            }
        }

        mediaSessionConnection.postNowPlayingData { post ->
            if (post != null) {
                val currentData: MediaData = MediaData().pullMediaMetadata(post)
                nowPlaying = post
                view?.bindView(currentData)
                currentMedia = MediaData().pullMediaMetadata(post)
            }
        }

        val transportControls = mediaSessionConnection.transportControls
        Log.d("isPrepared is ", isPrepared.toString())
        if (isPrepared && MediaID().fromString(mediaItem.mediaId!!).mediaId == nowPlaying?.id) {
            currentState.let {
                when {
                    currentState.isPlaying -> transportControls.pause()
                    currentState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Toast.makeText(
                            (view as BottomSheetFragment).activity,
                            "\"Playable item clicked but neither play nor pause are enabled!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Log.d("mediaItem CLick has id ", "${mediaItem.mediaId}")
            transportControls.playFromMediaId(mediaItem.mediaId, extras)
        }
    }

    override fun playCurrent(extras: Bundle?) {
        mediaItemClick(currentMedia.toDummySong(), extras)
    }

    override fun mediaItemClick(clickedItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
//        mediaSessionConnection.invokeMediaControllerCallback()
        if (!clickedItem.isBrowsable) {
            playMedia(clickedItem, extras)
            // Show Fragment NowPlaying
        }
//        view?.bindView(clickedItem)
    }


    override fun onDestroy() {
        this.view = null
    }
}