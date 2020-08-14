package com.example.mymusic.playback.server

import android.media.session.PlaybackState.STATE_NONE
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.example.mymusic.constants.Constants.ACTION_PLAY_NEXT
import com.example.mymusic.constants.Constants.ACTION_REPEAT_SONG
import com.example.mymusic.constants.Constants.ACTION_RESTORE_MEDIA_SESSION
import com.example.mymusic.constants.Constants.ACTION_SET_MEDIA_STATE
import com.example.mymusic.constants.Constants.QUEUE_TITLE
import com.example.mymusic.constants.Constants.SEEK_TO_POS
import com.example.mymusic.constants.Constants.SONG
import com.example.mymusic.constants.Constants.SONGS_LIST
import com.example.mymusic.models.MediaID

class MediaSessionCallback (
    private val mediaSession: MediaSessionCompat,
    private val songPlayer: SongPlayer
) : MediaSessionCompat.Callback() {

    override fun onPause() = songPlayer.pause()

    override fun onPlay() = songPlayer.playSong()

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        super.onPlayFromMediaId(mediaId, extras)
        Log.d("Hello", "World $mediaId")

        val songId = MediaID().fromString(mediaId!!).mediaId!!.toLong()
        songPlayer.playSong(songId)

        if (extras == null) return
        val queue = extras.getLongArray(SONGS_LIST)
        val seekTo = extras.getInt(SEEK_TO_POS)
        val queueTitle = extras.getString(QUEUE_TITLE) ?: ""

        if (queue != null) {
            songPlayer.setQueue(queue, queueTitle)
        }
        if (seekTo > 0) {
            songPlayer.seekTo(seekTo)
        }
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        Log.d("Order", " MediaSessionCallback onCustomAction(action: String?, extras: Bundle?) $action")
        when (action) {
            ACTION_SET_MEDIA_STATE -> setSavedMediaSessionState()
            ACTION_REPEAT_SONG -> songPlayer.repeatSong()

            ACTION_PLAY_NEXT -> {
                val nextSongId = extras!!.getLong(SONG)
                songPlayer.playNext(nextSongId)
            }

            ACTION_RESTORE_MEDIA_SESSION -> restoreMediaSession()
        }
    }

    private fun setSavedMediaSessionState() {
        Log.d("Order", " MediaSessionCallback setSavedMediaSessionState()")

        // If Only set saved session from deb if we know there is not any active media session
        val controller = mediaSession.controller ?: return
        if (controller.playbackState == null || controller.playbackState.state == STATE_NONE) {
            // -> Sync data from queue

        } else {
            // Force update the playback state and metadata from the media session so that the
            // attached State in MainPresenter gets the current state.
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        Log.d("Order", " MediaSessionCallback restoreMediaSession()")
        songPlayer.setPlaybackState(mediaSession.controller.playbackState)
        mediaSession.setMetadata(mediaSession.controller.metadata)
    }
}