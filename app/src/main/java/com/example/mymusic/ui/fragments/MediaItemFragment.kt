package com.example.mymusic.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.mymusic.extensions.argumentOrEmpty
import com.example.mymusic.models.MediaID
import com.example.mymusic.playback.server.MusicService.Companion.MEDIA_CALLER
import com.example.mymusic.playback.server.MusicService.Companion.MEDIA_ID_ARG
import com.example.mymusic.playback.server.MusicService.Companion.MEDIA_TYPE_ARG
import com.example.mymusic.playback.server.MusicService.Companion.TYPE_ALL_SONGS

open class MediaItemFragment : Fragment() {

    private lateinit var mediaType: String
    private var mediaId: String? = null
    private var caller: String? = null

    companion object {
        fun newInstance(mediaID: MediaID): MediaItemFragment {
            val args = Bundle().apply {
                putString(MEDIA_TYPE_ARG, mediaID.type)
                putString(MEDIA_ID_ARG, mediaID.mediaId)
                putString(MEDIA_CALLER, mediaID.caller)
            }
            return when (mediaID.type?.toInt()) {
                TYPE_ALL_SONGS -> SongGridFragment().apply { arguments = args }
                else
                    // The features aren't temporarily implemented. Hope to complete them soon!
                -> MediaItemFragment().apply { arguments = args }
            }
        }
    }

    fun getCurrentMediaId() : MediaID {
        mediaType = argumentOrEmpty(MEDIA_TYPE_ARG)
        mediaId = argumentOrEmpty(MEDIA_ID_ARG)
        caller = argumentOrEmpty(MEDIA_CALLER)

        return MediaID(mediaType, mediaId, caller)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mediaId = getCurrentMediaId()

        // Handle omitting events with mediaId when a new fragment instances

    }
}