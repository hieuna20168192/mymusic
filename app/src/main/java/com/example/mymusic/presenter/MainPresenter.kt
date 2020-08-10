package com.example.mymusic.presenter

import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.repository.SongRepository

class MainPresenter(
    private var view: MainContract.View?,
    private val songRepository: SongRepository
) : MainContract.Presenter{

    override fun onViewCreated() {
        loadSong()
    }

    private fun loadSong() {
        val list = songRepository.loadSongs("self")
        view?.displaySong(list)
    }

    override fun onDestroy() {
        this.view = null
    }
}