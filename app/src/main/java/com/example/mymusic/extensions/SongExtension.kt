package com.example.mymusic.extensions

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.example.mymusic.db.SongEntity
import com.example.mymusic.models.Song
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.util.Utils

fun Song.toSongEntity() = SongEntity(null, this.id)

fun List<Song>.toSongEntityList() = map { it.toSongEntity() }

fun List<SongEntity>.toSongIDs() = map { it.id }.toLongArray()

fun List<Song>?.toSongIds() = this?.map { it.id }?.toLongArray() ?: LongArray(0)

fun List<Song?>.toQueue(): List<MediaSessionCompat.QueueItem> {
    return filter { it != null }.map {
        require(it != null)
        MediaSessionCompat.QueueItem(it.toDescription(), it.id)
    }
}

fun LongArray.toQueue(songsRepository: SongRepository): List<MediaSessionCompat.QueueItem> {
    val songList = songsRepository.getSongsForIds(this)
    // the list returned above is sorted in default order, need to map it to same as the input array and preserve the original order
    songList.keepInOrder(this)?.let {
        return it.toQueue()
    } ?: return songList.toQueue()
}

fun List<Song>.keepInOrder(queue: LongArray): List<Song>? {
    //this may happen if user deletes some item from his library and then comes back to app after we stored the current queue ids
    //if the two arrays are different return the array as is
    if (size != queue.size) return this
    return if (isNotEmpty() && queue.isNotEmpty()) {
        val keepOrderList = Array(size, init = { Song() })
        forEach {
            keepOrderList[queue.indexOf(it.id)] = it
        }
        keepOrderList.asList()
    } else null
}

fun LongArray.toSongEntityList(songsRepository: SongRepository): List<SongEntity> {
    return songsRepository.getSongsForIds(this).toSongEntityList()
}

fun List<MediaSessionCompat.QueueItem>?.toIDList(): LongArray {
    return this?.map { it.queueId }?.toLongArray() ?: LongArray(0)
}

fun Song.toDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
        .setTitle(title)
        .setMediaId(id.toString())
        .setSubtitle(artist)
        .setDescription(album)
        .setIconUri(Utils.getAlbumArtUri(albumId)).build()
}
