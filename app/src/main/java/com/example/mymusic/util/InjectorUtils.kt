package com.example.mymusic.util

import android.app.Application
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import androidx.fragment.app.Fragment
import com.example.mymusic.Notifications.NotificationImpl
import com.example.mymusic.playback.server.*
import com.example.mymusic.presenter.MainContract
import com.example.mymusic.presenter.PlayerPresenter
import com.example.mymusic.presenter.ReposPresenter
import com.example.mymusic.repository.SongReposImpl
import com.example.mymusic.repository.SongRepository

object InjectorUtils {

    private fun provideMusicServiceConnection(context: Context): MediaSessionConnection {
        return MediaSessionConnectionImpl.getInstance(
            context,
            ComponentName(context, MusicService::class.java)
        )
    }

    fun provideSongRepository(context: Context): SongRepository {
        return SongReposImpl.getInstance(context)
    }

    private fun provideMusicPlayer(context: Context): MusicPlayer {
        return MusicPlayerImpl.getInstance(context)
    }

    private fun provideQueue(context: Context, repository: SongRepository): Queue {
        return QueueImpl.getInstance(context, repository)
    }

    fun provideSongPlayer(context: Context): SongPlayer {
        val repository = provideSongRepository(context)
        val musicPlayer = provideMusicPlayer(context)
        val queue = provideQueue(context, repository)
        return SongPlayerImpl.getInstance(context, musicPlayer, repository, queue)
    }

    // For Presenter
    fun provideReposPresenter(view: MainContract.View?): ReposPresenter {
        val context = (view as Fragment).context!!
        val repository = provideSongRepository(context)
        val mediaSessionConnection = provideMusicServiceConnection(context)
        return ReposPresenter.getInstance(view, repository, mediaSessionConnection)
    }

    fun providePlayerPresenter(view: MainContract.BottomSheetView?): PlayerPresenter {
        val context = (view as Fragment).context!!
        val repository = provideSongRepository(context)
        val mediaSessionConnection = provideMusicServiceConnection(context)
        return PlayerPresenter.getInstance(view, repository, mediaSessionConnection)
    }

    // Notification
    fun provideNotification(context: Context) : NotificationImpl{
        val notificationManager : NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return NotificationImpl.getInstance(context, notificationManager)
    }
}