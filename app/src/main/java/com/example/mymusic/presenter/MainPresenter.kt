package com.example.mymusic.presenter

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import com.example.mymusic.extensions.getCurrentMediaID
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.ui.fragments.MediaItemFragment

class MainPresenter(
    private var view: MainContract.View?,
    private val songRepository: SongRepository,
    private val mediaSessionConnection: MediaSessionConnection
) : MainContract.Presenter{

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            Log.d("parentId ", parentId)
        }
    }

    override fun onViewCreated() {
        loadSong()
        subscribe()
    }

    private fun subscribe() {
        mediaSessionConnection.subscribe((view as MediaItemFragment).getCurrentMediaId().asString(), subscriptionCallback)
    }

    private fun loadSong() {
        val list = songRepository.loadSongs("self")
        view?.displaySong(list)
    }

    override fun onDestroy() {
        this.view = null
    }
}