package com.example.mymusic.playback.server

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.media.session.PlaybackState.*
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.util.Log
import com.example.mymusic.R
import com.example.mymusic.constants.Constants.ACTION_REPEAT_QUEUE
import com.example.mymusic.constants.Constants.ACTION_REPEAT_SONG
import com.example.mymusic.constants.Constants.REPEAT_MODE
import com.example.mymusic.constants.Constants.SHUFFLE_MODE
import com.example.mymusic.extensions.isPlaying
import com.example.mymusic.extensions.position
import com.example.mymusic.models.QueueData
import com.example.mymusic.models.Song
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.util.MusicUtils

// Define some Type Alias
typealias OnIsPlaying = SongPlayer.(playing: Boolean) -> Unit
typealias OnConnected = MediaSessionConnection.(isConnected: Boolean) -> Unit
typealias OnPrepared<T> = T.() -> Unit
typealias OnError<T> = T.(error: Throwable) -> Unit
typealias OnCompletion<T> = T.() -> Unit

typealias UpdatePlaybackState = MediaSessionConnection.(playbackState: PlaybackStateCompat) -> Unit
typealias UpdateNowPlayingData = MediaSessionConnection.(nowPlayingData: MediaMetadataCompat) -> Unit
typealias UpdateQueueData = MediaSessionConnection.(queueData: QueueData) -> Unit


interface SongPlayer {

    fun setQueue(
        data: LongArray = LongArray(0),
        title: String = ""
    )

    fun getSession(): MediaSessionCompat

    fun playSong()

    fun playSong(id: Long)

    fun playSong(song: Song)

    fun seekTo(position: Int)

    fun pause()

    fun nextSong()

    fun repeatSong()

    fun repeatQueue()

    fun previousSong()

    fun playNext(id: Long)

    fun swapQueueSongs(from: Int, to: Int)

    fun removeFromQueue(id: Long)

    fun stop()

    fun release()

    fun onPlayingState(playing: OnIsPlaying)

    fun onError(error: OnError<SongPlayer>)

    fun onCompletion(completion: OnCompletion<SongPlayer>)

    fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit)

    fun setPlaybackState(state: PlaybackStateCompat)

}

class SongPlayerImpl(
    private val context: Application,
    private val musicPlayer: MusicPlayer,
    private val songRepository: SongRepository,
    private val queue: Queue
) : SongPlayer {

    private var isInitialized: Boolean = false
    private var isPlayingCallback: OnIsPlaying = {}
    private var errorCallback: OnError<SongPlayer> = {}
    private var completionCallback: OnCompletion<SongPlayer> = {}

    private var metadataBuilder = MediaMetadataCompat.Builder()
    private var stateBuilder = createDefaultPlaybackState()

    private var mediaSession = MediaSessionCompat(context, context.getString(R.string.app_name)).apply {
        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        setCallback(MediaSessionCallback(this, this@SongPlayerImpl))
        setPlaybackState(stateBuilder.build())

        val sessionIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(context, 0, sessionIntent, 0)
        setSessionActivity(sessionActivityPendingIntent)
        isActive = true
    }

    init {

        queue.setMediaSession(mediaSession)

        musicPlayer.onPrepared {
            Log.d("Order SongPlayer", "musicPlayer.onPrepared")
            playSong()
            seekTo(getSession().position().toInt())
        }

        musicPlayer.onCompletion {
            completionCallback(this@SongPlayerImpl)
            val controller = getSession().controller
            when (controller.repeatMode) {
                REPEAT_MODE_ONE -> {
                    controller.transportControls.sendCustomAction(ACTION_REPEAT_SONG, null)
                }
                REPEAT_MODE_ALL -> {
                    controller.transportControls.sendCustomAction(ACTION_REPEAT_QUEUE, null)
                }
                else -> controller.transportControls.skipToNext()
            }
            Log.d("Order SongPlayer", "musicPlayer.onCompletion")
        }
    }

    override fun setQueue(data: LongArray, title: String) {
        Log.d("Order SongPlayer", "setQueue")
        this.queue.ids = data
        this.queue.title = title
    }


    override fun getSession(): MediaSessionCompat = mediaSession

    @SuppressLint("WrongConstant")
    override fun playSong() {
        Log.d("Order SongPlayer", "playSong()")

        if (isInitialized) {
            updatePlaybackState {
                setState(STATE_PLAYING, mediaSession.position(), 1F)
            }
            musicPlayer.play()
            return
        }
        musicPlayer.reset()

        Log.d("queue.currentSongId is ", queue.currentSongId.toString())

        val path = MusicUtils.getSongUri(queue.currentSongId).toString()
        val isSourceSet = if (path.startsWith("content://")) {
            musicPlayer.setSource(Uri.parse(path))
        } else {
            musicPlayer.setSource(path)
        }
        if (isSourceSet) {
            isInitialized = true
            musicPlayer.prepare()
        }
    }

    override fun playSong(id: Long) {
        Log.d("Order SongPlayer", "playSong(id: Long)")
        val song = songRepository.getSongForId(id)
        Log.d("playSong(id: Long)",  "= ${song.id}")
        playSong(song)
    }

    @SuppressLint("WrongConstant")
    override fun playSong(song: Song) {
        Log.d("Order SongPlayer", "playSong(song: Song)")
        if (queue.currentSongId != song.id) {
            queue.currentSongId = song.id
            isInitialized = false
            updatePlaybackState {
                setState(STATE_STOPPED, 0, 1F)
            }
        }
        setMetaData(song)
        playSong()
    }

    override fun seekTo(position: Int) {
        if (isInitialized) {
            musicPlayer.seekTo(position)
            updatePlaybackState {
                setState(
                    mediaSession.controller.playbackState.state,
                    position.toLong(),
                    1F
                )
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun pause() {

        if (musicPlayer.isPlaying() && isInitialized) {
            musicPlayer.pause()
            updatePlaybackState {
                setState(STATE_PAUSED, mediaSession.position(), 1F)
            }
        }
    }

    override fun nextSong() {
        Log.d("Order SongPlayer", "nextSong()")
        queue.nextSongId?.let {
            playSong(it)
        } ?: pause()
    }

    @SuppressLint("WrongConstant")
    override fun repeatSong() {
        Log.d("Order SongPlayer", "repeatSong()")
        updatePlaybackState {
            setState(STATE_STOPPED, 0, 1F)
        }
        playSong(queue.currentSong())
    }

    override fun repeatQueue() {
        Log.d("Order SongPlayer", "repeatQueue")
        if (queue.currentSongId == queue.lastId())
            playSong(queue.firstId())
        else {
            nextSong()
        }
    }

    override fun previousSong() {
        Log.d("Order SongPlayer", "previousSong()")
        queue.previousSongId?.let(::playSong)
    }

    override fun playNext(id: Long) {
        Log.d("Order SongPlayer", "playNext(id: Long)")
        queue.moveToNext(id)
    }

    override fun swapQueueSongs(from: Int, to: Int) {
        Log.d("Order SongPlayer", "swapQueueSongs(from: Int, to: Int)")
        queue.swap(from, to)
    }

    override fun removeFromQueue(id: Long) {
        Log.d("Order SongPlayer", "removeFromQueue(id: Long)")
        queue.remove(id)
    }

    @SuppressLint("WrongConstant")
    override fun stop() {
        Log.d("Order SongPlayer", "stop()")
        musicPlayer.stop()
        updatePlaybackState {
            setState(STATE_NONE, 0, 1F)
        }
    }

    override fun release() {
        Log.d("Order SongPlayer", "release()")
        mediaSession.apply {
            isActive = false
            release()
        }
        musicPlayer.release()
        queue.reset()
    }

    override fun onPlayingState(playing: OnIsPlaying) {
        Log.d("Order SongPlayer", "onPlayingState(playing: OnIsPlaying) $playing")
        this.isPlayingCallback = playing
    }

    override fun onError(error: OnError<SongPlayer>) {
        Log.d("Order SongPlayer", "onError")

        this.errorCallback = error
        musicPlayer.onError { error: Throwable ->
            errorCallback(this@SongPlayerImpl, error)
        }
    }

    override fun onCompletion(completion: OnCompletion<SongPlayer>) {
        Log.d("Order SongPlayer", "onCompletion $completion")
        this.completionCallback = completion
    }

    override fun updatePlaybackState(applier: PlaybackStateCompat.Builder.() -> Unit) {
        Log.d("Order SongPlayer", "updatePlaybackState")
        applier(stateBuilder)
        setPlaybackState(stateBuilder.build())
    }

    override fun setPlaybackState(state: PlaybackStateCompat) {
        Log.d("Order SongPlayer", "setPlaybackState -> $state")

        mediaSession.setPlaybackState(state)
        state.extras?.let { bundle ->
            mediaSession.setRepeatMode(bundle.getInt(REPEAT_MODE))
            mediaSession.setShuffleMode(bundle.getInt(SHUFFLE_MODE))
        }
        if (state.isPlaying) {
            Log.d("Order SongPlayer", "setPlaybackState -> state.isPlaying")
            isPlayingCallback(this, true)
        } else {
            Log.d("Order SongPlayer", "setPlaybackState -> state.else")
            isPlayingCallback(this, false)
        }
    }

    private fun setMetaData(song: Song) {

        val artwork = MusicUtils.getAlbumArtBitmap(context, song.albumId)
        val mediaMetadata = metadataBuilder.apply {
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, song.albumId.toString())
            putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.id.toString())
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration.toLong())
        }.build()
        mediaSession.setMetadata(mediaMetadata)
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: SongPlayer? = null

        fun getInstance(context: Context, musicPlayer: MusicPlayer, songRepository: SongRepository, queue: Queue) : SongPlayer{
            instance ?: synchronized(this) {
                return instance ?: SongPlayerImpl(context as Application, musicPlayer, songRepository, queue)
                    .also { instance = it }
            }
            return instance!!
        }
    }
}

@SuppressLint("WrongConstant")
private fun createDefaultPlaybackState(): PlaybackStateCompat.Builder {
    Log.d("Order SongPlayer", "createDefaultPlaybackState()")
    return PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
    )
        .setState(STATE_NONE, 0, 1f)
}

