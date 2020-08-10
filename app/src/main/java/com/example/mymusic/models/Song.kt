package com.example.mymusic.models

import android.database.Cursor
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.example.mymusic.extensions.value
import com.example.mymusic.extensions.valueOrDefault
import com.example.mymusic.extensions.valueOrEmpty
import com.example.mymusic.playback.server.MusicService.Companion.TYPE_SONG
import com.example.mymusic.util.Utils.getAlbumArtUri
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Song(
    var id: Long = 0,
    var albumId: Long = 0,
    var artistId: Long = 0,
    var title: String = "",
    var artist: String = "",
    var album: String = "",
    var duration: Int = 0,
    var trackNumber: Int = 0
) : MediaBrowserCompat.MediaItem(
    MediaDescriptionCompat.Builder()
        .setMediaId(MediaID("$TYPE_SONG", "$id").asString())
        .setTitle(title)
        .setIconUri(getAlbumArtUri(albumId))
        .setSubtitle(artist)
        .build(), FLAG_PLAYABLE) {
    companion object {
        fun fromCursor(cursor: Cursor, albumId: Long = -1, artistId: Long = -1): Song {
            return Song(
                id = cursor.value(MediaStore.Audio.Media._ID),
                albumId = cursor.valueOrDefault(MediaStore.Audio.Media.ALBUM_ID, albumId),
                artistId = cursor.valueOrDefault(MediaStore.Audio.Media.ARTIST_ID, artistId),
                title = cursor.valueOrEmpty(MediaStore.Audio.Media.TITLE),
                artist = cursor.valueOrEmpty(MediaStore.Audio.Media.ARTIST),
                album = cursor.valueOrEmpty(MediaStore.Audio.Media.ALBUM),
                duration = cursor.value(MediaStore.Audio.Media.DURATION),
                trackNumber = cursor.value<Int>(MediaStore.Audio.Media.TRACK).normalizeTrackNumber()
            )
        }

        fun fromPlaylistMembersCursor(cursor: Cursor): Song {
            return Song(
                id = cursor.value(MediaStore.Audio.Playlists.Members.AUDIO_ID),
                albumId = cursor.value(MediaStore.Audio.AudioColumns.ALBUM_ID),
                artistId = cursor.value(MediaStore.Audio.AudioColumns.ARTIST_ID),
                title = cursor.valueOrEmpty(MediaStore.Audio.AudioColumns.TITLE),
                artist = cursor.valueOrEmpty(MediaStore.Audio.AudioColumns.ARTIST),
                album = cursor.valueOrEmpty(MediaStore.Audio.AudioColumns.ALBUM),
                duration = (cursor.value<Long>(MediaStore.Audio.AudioColumns.DURATION) / 1000).toInt(),
                trackNumber = cursor.value<Int>(MediaStore.Audio.AudioColumns.TRACK).normalizeTrackNumber()
            )
        }
    }
}

private fun Int.normalizeTrackNumber(): Int {
    var returnValue = this
    // This fixes bug where some track numbers displayed as 100 or 200.
    while (returnValue >= 1000) {
        // When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
        returnValue -= 1000
    }
    return returnValue
}