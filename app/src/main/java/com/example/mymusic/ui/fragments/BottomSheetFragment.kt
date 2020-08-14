package com.example.mymusic.ui.fragments

import android.media.session.MediaController
import android.os.Build
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.mymusic.R
import com.example.mymusic.models.MediaData
import com.example.mymusic.models.Song
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.presenter.MainContract
import com.example.mymusic.ui.activities.MainActivity
import com.example.mymusic.util.Binding
import com.example.mymusic.util.InjectorUtils
import com.example.mymusic.util.Utils
import com.example.mymusic.widgets.BottomSheetListener
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import kotlinx.android.synthetic.main.item_song.*

class BottomSheetFragment : Fragment(), BottomSheetListener, MainContract.BottomSheetView {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }

    lateinit var playerPresenter: MainContract.PlayerPresenter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set up Presenter
        playerPresenter = InjectorUtils.providePlayerPresenter(this)

        // Set up UI
        val layoutParams = progressBar.layoutParams as LinearLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams
        songTitle.isSelected = true

        btnTogglePlayPause.setOnClickListener {
            // Handle event play/pause
            playerPresenter.playCurrent(null)
        }

        btnPlayPause.setOnClickListener {
            // Handle event quick play/pause
            playerPresenter.playCurrent(null)
        }

        btnNext.setOnClickListener {
            // Handle event Switch forward the Item pos
        }

        btnPrevious.setOnClickListener {
            // Handle event Switch back the Item pos
        }

        btnRepeat.setOnClickListener {
            // Handle event Repeat
        }

        btnShuffle.setOnClickListener {
            // Handle event shuffle
        }

        // Sign for the interface of MainActivity that refer to this
        // Now, we can observe events from MainActivity omits as well as be able to
        // refer to it.
        (activity as? MainActivity)?.let { mainActivity ->
            btnCollapse.setOnClickListener { mainActivity.collapseBottomSheet() }
            mainActivity.setBottomSheetListener(this)
        }
    }

    // What's bottomSheet view that the fragment observes events from the parent Activity?
    override fun onStateChanged(bottomSheet: View, newState: Int) {

        if (newState == STATE_DRAGGING || newState == STATE_EXPANDED) {
            btnPlayPause.visibility = View.GONE
            btnCollapse.visibility = View.VISIBLE

            // Handle some other event flows
        } else if (newState == STATE_COLLAPSED) {
            btnPlayPause.visibility = View.VISIBLE
            btnCollapse.visibility = View.GONE
        }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0) {
            btnPlayPause.visibility = View.GONE
            progressBar.visibility = View.GONE
            btnCollapse.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun bindView(currentData: MediaData) {
        /**
         *  var mediaId: String? = "",
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
         */

        Log.d("currentData is ", "${currentData.toString()}")
        // Bind View
        songTitle.text = currentData.title
        songArtist.text = currentData.artist
        bottomContolsAlbumart.setImageBitmap(currentData.artwork)
        Binding.setDuration(progressText, currentData.position!!)
        Binding.setDuration(durationText, currentData.duration!!)

    }

    override fun bindViewByState(currentData: MediaData) {
        Binding.setPlayState(btnTogglePlayPause, currentData.state!!)
        Binding.setPlayState(btnPlayPause, currentData.state!!)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun bindProgress(mediaController: MediaControllerCompat) {
        Log.d("bindProgress", "run")
        progressBar.setMediaController(mediaController)
        progressText.setMediaController(mediaController)
        seekBar.setMediaController(mediaController)
    }

    override fun setPresenter(presenter: MainContract.PlayerPresenter) {
        this.playerPresenter = presenter
    }

    override fun onResume() {
        super.onResume()
    }
}