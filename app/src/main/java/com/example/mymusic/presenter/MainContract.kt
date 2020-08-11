package com.example.mymusic.presenter

import com.example.mymusic.models.Song

interface MainContract {
    interface Presenter : BasePresenter {
        fun onViewCreated()
    }

    interface View : BaseView<Presenter> {
        fun displaySong(list: List<Song>)
    }

    interface Player: BaseView<Presenter> {
        fun updateIcon()
    }
}