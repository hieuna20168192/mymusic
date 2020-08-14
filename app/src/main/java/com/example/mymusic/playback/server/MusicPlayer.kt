package com.example.mymusic.playback.server

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mymusic.repository.SongReposImpl
import com.example.mymusic.repository.SongRepository
import java.lang.Exception
import java.lang.IllegalStateException

interface MusicPlayer {
    fun play()

    fun setSource(path: String): Boolean

    fun setSource(Uri: Uri): Boolean

    fun prepare()

    fun seekTo(position: Int)

    fun isPrepared(): Boolean

    fun isPlaying(): Boolean

    fun position(): Int

    fun pause()

    fun stop()

    fun reset()

    fun release()

    fun onPrepared(prepared: OnPrepared<MusicPlayer>)

    fun onError(error: OnError<MusicPlayer>)

    fun onCompletion(completion: OnCompletion<MusicPlayer>)

}

class MusicPlayerImpl(internal val context: Application) : MusicPlayer,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    private var _player: MediaPlayer? = null
    private val player: MediaPlayer
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    get() {
        if (_player == null) {
            _player = createPlayer(this)
        }
        return _player ?: throw IllegalStateException("Impossible")
    }

    private var didPrepare = false
    var onPrepared: OnPrepared<MusicPlayer> = {}
    private var onError: OnError<MusicPlayer> = {}
    private var onCompletion: OnCompletion<MusicPlayer> = {}


    override fun play() {
        Log.d("Order", "play()")
        player.start()
    }

    override fun setSource(path: String): Boolean {
        Log.d("Order", "setSource(path: String) $path")
        try {
            player.setDataSource(path)
        } catch (e: Exception) {
            onError(this, e)
            return false
        }
        return true
    }

    override fun setSource(uri: Uri): Boolean {
        Log.d("Order", "setSource(uri: Uri) $uri")
        try {
            player.setDataSource(context, uri)
        } catch (e: Exception) {
            onError(this, e)
            return false
        }
        return true
    }

    override fun prepare() {
        Log.d("Order", "prepare")
        player.prepare()
    }

    override fun seekTo(position: Int) {
        player.seekTo(position)
    }

    override fun isPrepared() = didPrepare

    override fun isPlaying() = player.isPlaying

    override fun position() = player.currentPosition

    override fun pause() {
        Log.d("Order", "pause")
        player.pause()
    }

    override fun stop() {
        Log.d("Order", "stop")
        player.stop()
    }

    override fun reset() {
        Log.d("Order", "reset")
        player.reset()
    }

    override fun release() {
        Log.d("Order", "release")
        player.release()
        _player = null
    }

    override fun onPrepared(prepared: OnPrepared<MusicPlayer>) {
        Log.d("Order", "onPrepared(prepared: OnPrepared<MusicPlayer>")
        this.onPrepared = prepared
    }

    override fun onError(error: OnError<MusicPlayer>) {
        Log.d("Order", "onError")
        this.onError = error
    }

    override fun onCompletion(completion: OnCompletion<MusicPlayer>) {
        Log.d("Order", "onCompletion")
        this.onCompletion = completion
    }

    // This callback is very important that observes state from MediaPlayer
    override fun onPrepared(p0: MediaPlayer?) {
        Log.d("Order", "onPrepared")
        didPrepare = true
        onPrepared(this)
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        Log.d("Order", "onError")
        didPrepare = false
        return false
    }

    override fun onCompletion(p0: MediaPlayer?) {
        Log.d("Order", "onCompletion(mp: MediaPlayer)")
        onCompletion(this)
    }
    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MusicPlayer? = null

        fun getInstance(context: Context) : MusicPlayer {
            instance ?: synchronized(this) {
                instance ?: MusicPlayerImpl(context as Application)
                    .also { instance = it }
            }
            return instance!!
        }
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun createPlayer(owner: MusicPlayerImpl): MediaPlayer {
    return MediaPlayer().apply {
        setWakeMode(owner.context, PowerManager.PARTIAL_WAKE_LOCK)
        val attr = AudioAttributes.Builder().apply {
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            setUsage(AudioAttributes.USAGE_MEDIA)
        }.build()
        setAudioAttributes(attr)
        setOnPreparedListener(owner)
        setOnCompletionListener(owner)
        setOnErrorListener(owner)
    }
}