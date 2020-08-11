package com.example.mymusic.models

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.example.mymusic.extensions.toIDList

data class QueueData(
    var queueTitle: String = "All songs",
    var queue: LongArray = LongArray(0),
    var currentId: Long = 0
) {
    fun fromMediaController(mediaControllerCompat: MediaControllerCompat?): QueueData {
        mediaControllerCompat?.let {
            return QueueData(
                queueTitle = mediaControllerCompat.queueTitle?.toString().orEmpty().let {
                    if (it.isEmpty()) "All songs" else it
                },
                queue = mediaControllerCompat.queue?.toIDList() ?: LongArray(0),
                currentId = mediaControllerCompat.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toLong() ?: 0
            )
        }
        return QueueData()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QueueData

        if (queueTitle != other.queueTitle) return false
        if (!queue.contentEquals(other.queue)) return false
        if (currentId != other.currentId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = queueTitle.hashCode()
        result = 31 * result + queue.contentHashCode()
        result = 31 * result + currentId.hashCode()
        return result
    }
}