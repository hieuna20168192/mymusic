package com.example.mymusic.presenter

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.ui.fragments.MediaItemFragment

class ReposPresenter(
    private var view: MainContract.View?,
    private val songRepository: SongRepository,
    private val mediaSessionConnection: MediaSessionConnection
) : MainContract.ReposPresenter{

    private lateinit var rootId: String

    companion object {

        // For Singleton instantiation.
        @Volatile
        private var instance: ReposPresenter? = null

        fun getInstance(view: MainContract.View?,
                        repos: SongRepository,
                        mediaSessionConnection: MediaSessionConnection) : ReposPresenter{
            instance ?: synchronized(this) {
                instance ?: ReposPresenter(view, repos, mediaSessionConnection)
                    .also { instance = it }
            }
            return instance!!
        }
    }

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            loadSong()
        }
    }

    override fun onViewCreated() {
        subscribe()
    }

    private fun subscribe() {
        mediaSessionConnection.connected { isConnected ->
            if (isConnected) {
                rootId = mediaSessionConnection.rootMediaId
                mediaSessionConnection.subscribe((view as MediaItemFragment).getCurrentMediaId().asString(), subscriptionCallback)
            } else {
                null
            }
        }
    }

    private fun loadSong() {
        val list = songRepository.loadSongs("self")
        view?.displaySong(list)
    }

    override fun onDestroy() {
        this.view = null
    }
}