package com.example.mymusic.presenter

interface BaseView<T> {
    fun setPresenter(presenter: T)
}