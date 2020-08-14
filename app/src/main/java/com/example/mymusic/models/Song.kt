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
        .build(), FLAG_PLAYABLE)