package com.example.mymusic.presenter

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.repository.SongRepository

class PlayerPresenter(
    private var view: MainContract.Player?,
    private val repos: SongRepository,
    private val mediaSessionConnection: MediaSessionConnection
) : MainContract.Presenter {

    override fun onViewCreated() {

    }

    override fun onDestroy() {
        this.view = null
    }

}