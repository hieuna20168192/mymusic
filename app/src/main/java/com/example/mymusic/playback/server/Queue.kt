package com.example.mymusic.playback.server

import android.app.Application
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import com.example.mymusic.R
import com.example.mymusic.extensions.moveElement
import com.example.mymusic.extensions.toQueue
import com.example.mymusic.models.Song
import com.example.mymusic.repository.SongRepository

const val SONG_ID_NONE = -1L

// Keeps track of previous shuffle index to reduce getting the same indices
// over and over. This is how large that tracker list can be.
private const val MAX_SHUFFLE_BUFFER_SIZE = 16

// Manages all things queues for [SongPlayer]

interface Queue {

    var ids: LongArray
    var title: String
    var currentSongId: Long
    val currentSongIndex: Int

    val previousSongId: Long?
    val nextSongIndex: Int?
    val nextSongId: Long?

    fun setMediaSession(session: MediaSessionCompat)

    fun swap(from: Int, to: Int)

    fun moveToNext(id: Long)

    fun firstId(): Long

    fun lastId(): Long

    fun remove(id: Long)

    fun asQueueItems(): List<MediaSessionCompat.QueueItem>

    fun currentSong(): Song

    fun reset()

}

class QueueImpl (
    private val context: Application,
    private val songRepository: SongRepository
) : Queue {

    private lateinit var session: MediaSessionCompat
    private val shuffleRandom = java.util.Random()
    private val previousShuffles = mutableListOf<Int>()


    override var ids: LongArray = LongArray(0)
        set(value) {
            field = value
            if (value.isNotEmpty()) {
                session.setQueue(asQueueItems())
            }
        }

    override var title: String = context.getString(R.string.all_songs)
        set(value) {
            val previousValue = field
            field = if (value.isEmpty()) {
                context.getString(R.string.all_songs)
            } else {
                value
            }

            if (value != previousValue) {
                previousShuffles.clear()
                session.setQueueTitle(value)
            }
        }


    override var currentSongId: Long = SONG_ID_NONE

    override val currentSongIndex: Int
        get() = ids.indexOf(currentSongId)

    override val previousSongId: Long?
        get() {
            val previousIndex = currentSongIndex - 1
            return if (previousIndex >= 0) {
                ids[previousIndex]
            } else {
                null
            }
        }

    override val nextSongIndex: Int?
        get() {
            val nextIndex = currentSongIndex + 1
            val controller = session.controller
            return when {
                controller.shuffleMode == SHUFFLE_MODE_ALL -> getShuffleIndex()
                nextIndex < ids.size -> nextIndex
                else -> null
            }
        }

    private fun getShuffleIndex(): Int {
        val newIndex = shuffleRandom.nextInt(ids.size - 1)
        if (previousShuffles.contains(newIndex)) {
            return getShuffleIndex()
        }

        previousShuffles.add(newIndex)
        if (previousShuffles.size > MAX_SHUFFLE_BUFFER_SIZE) {
            previousShuffles.removeAt(0)
        }
        return newIndex
    }

    override val nextSongId: Long?
        get() {
            val nextIndex = nextSongIndex
            return if (nextIndex != null) {
                ids[nextIndex]
            } else {
                null
            }
        }

    override fun setMediaSession(session: MediaSessionCompat) {
        this.session = session
    }

    override fun swap(from: Int, to: Int) {
        ids = ids.toMutableList()
            .moveElement(from, to)
            .toLongArray()
    }

    override fun moveToNext(id: Long) {
        val nextIndex = currentSongIndex + 1
        val list = ids.toMutableList().apply {
            remove(id)
            add(nextIndex, id)
        }
        ids = list.toLongArray()
    }

    override fun firstId() = ids.first()

    override fun lastId() = ids.last()

    override fun remove(id: Long) {
        val list = ids.toMutableList().apply {
            remove(id)
        }
        ids = list.toLongArray()
    }

    override fun asQueueItems(): List<MediaSessionCompat.QueueItem> {
        return ids.toQueue(songRepository)
    }

    override fun currentSong(): Song {
        return songRepository.getSongForId(currentSongId)
    }

    override fun reset() {
        previousShuffles.clear()
        ids = LongArray(0)
        currentSongId = SONG_ID_NONE
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: Queue? = null

        fun getInstance(context: Context, repository: SongRepository) : Queue{
            instance ?: synchronized(this) {
                instance ?: QueueImpl(context as Application, repository)
                    .also { instance = it }
            }
            return instance!!
        }
    }
}

