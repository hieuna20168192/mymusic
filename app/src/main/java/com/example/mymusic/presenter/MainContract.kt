package com.example.mymusic.presenter

import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.example.mymusic.models.MediaData
import com.example.mymusic.models.MediaID
import com.example.mymusic.models.Song

interface MainContract {
    interface ReposPresenter : BasePresenter {
        fun onViewCreated()
    }

    interface View : BaseView<ReposPresenter> {
        fun displaySong(list: List<Song>)
    }

    interface BottomSheetView : BaseView<PlayerPresenter> {
        fun bindView(currentData: MediaData)
        fun bindViewByState(currentData: MediaData)
    }

    interface PlayerPresenter: BasePresenter {
        fun playMedia(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?)
        fun playCurrent(extras: Bundle?)
        fun mediaItemClick(clickedItem: MediaBrowserCompat.MediaItem, extras: Bundle?)
    }
}