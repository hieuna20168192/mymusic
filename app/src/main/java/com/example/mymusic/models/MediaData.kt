package com.example.mymusic.models

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.mymusic.constants.Constants.REPEAT_MODE
import com.example.mymusic.constants.Constants.SHUFFLE_MODE

data class MediaData(
    var mediaId: String? = "",
    var title: String? = "",
    var artist: String? = "",
    var album: String ? = "",
    var artwork: Bitmap? = null,
    var artworkId: Long? = 0,
    var position: Int? = 0,
    var duration: Int? = 0,
    var shuffleMode: Int? = 0,
    var repeatMode: Int? = 0,
    var state: Int? = 0
) {

    fun pullMediaMetadata(metaData: MediaMetadataCompat): MediaData {
        mediaId = metaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
        title = metaData.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: ""
        album = metaData.getString(MediaMetadataCompat.METADATA_KEY_ALBUM) ?: ""
        artist = metaData.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: ""
        duration = metaData.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()
        artwork = metaData.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
        //this is the album id
        artworkId = metaData.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)?.let { it.toLong() } ?: 0
        return this
    }

    fun pullPlaybackState(playbackState: PlaybackStateCompat): MediaData {
        position = playbackState.position.toInt()
        state = playbackState.state
        playbackState.extras?.let {
            repeatMode = it.getInt(REPEAT_MODE)
            shuffleMode = it.getInt(SHUFFLE_MODE)
        }
        return this
    }

    /** only used to check the song id for play pause purposes, do not use this elsewhere since it doesn't have any other data */
    fun toDummySong() = Song(id = mediaId?.toLong() ?: 0)
}