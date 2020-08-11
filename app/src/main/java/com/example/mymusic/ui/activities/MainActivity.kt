package com.example.mymusic.ui.activities

import Permission.Companion.REQUEST_CODE_PERMISSION
import android.content.ComponentName
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mymusic.R
import com.example.mymusic.extensions.addFragment
import com.example.mymusic.extensions.replaceFragment
import com.example.mymusic.models.MediaID
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.playback.server.MusicService
import com.example.mymusic.playback.server.MusicService.Companion.MEDIA_ID_NAV_SONG
import com.example.mymusic.playback.server.MusicService.Companion.TYPE_ALL_SONGS
import com.example.mymusic.presenter.MainContract
import com.example.mymusic.ui.fragments.BottomSheetFragment
import com.example.mymusic.ui.fragments.MediaItemFragment
import com.example.mymusic.util.NavigationIconClickListener
import com.example.mymusic.ui.fragments.SongGridFragment
import com.example.mymusic.util.InjectorUtils
import com.example.mymusic.widgets.BottomSheetListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.music_backdrop.*

class MainActivity : AppCompatActivity(), SongGridFragment.MediaItemSelected{

    private lateinit var mediaSessionConnection: MediaSessionConnection
    private lateinit var bottomSheetFragment: BottomSheetFragment

    private var bottomSheetListener: BottomSheetListener? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaSessionConnection = MediaSessionConnectionImpl.getInstance(
            application,
            ComponentName(application, MusicService::class.java)
        )

        Permission.checkPermission(this, REQUEST_CODE_PERMISSION)
        if (savedInstanceState == null) {

            navigateToMediaItem(MediaID(
                type = TYPE_ALL_SONGS.toString(),
                mediaId = MEDIA_ID_NAV_SONG.toString(),
                caller = "self"))

            // Set up Bottom Sheet Layout
            bottomSheetFragment = BottomSheetFragment()
            Handler().postDelayed({
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.bottomControlsContainer,
                        bottomSheetFragment
                    ).commit()
            }, 150)
        }

        // Drop Menu Event
        nav_list.setOnClickListener {
            navigateToMediaItem(MediaID(
                type = TYPE_ALL_SONGS.toString(),
                mediaId = MEDIA_ID_NAV_SONG.toString(),
                caller = "self"))
        }

        // Set up volume control system
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Associated bottom_sheet_view with the bottomSheetBehavior
        val rootViewAssociated = bottom_sheet_parent as FrameLayout
        bottomSheetBehavior = BottomSheetBehavior.from(rootViewAssociated)
        bottomSheetBehavior?.isHideable = true
        bottomSheetBehavior?.setBottomSheetCallback(BottomSheetCallback())

        // invoke unFocusable screen event
        dimOverlay.setOnClickListener{ collapseBottomSheet() }

        // Set up the tool bar
        (this as AppCompatActivity).setSupportActionBar(app_bar)
        this.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                this,
                sliding_layout,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(
                    this,
                    R.drawable.branch_menu
                ),
                ContextCompat.getDrawable(
                    this,
                    R.drawable.close_menu
                )
            )
        )

    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener
    }

    fun collapseBottomSheet() {
        bottomSheetBehavior?.state = STATE_COLLAPSED
    }

    fun hideBottomSheet() {
        bottomSheetBehavior?.state = STATE_HIDDEN
    }

    fun showBottomSheet() {
        if (bottomSheetBehavior?.state == STATE_HIDDEN) {
            bottomSheetBehavior?.state = STATE_COLLAPSED
        }
    }

    override fun onBackPressed() {
        bottomSheetBehavior?.let {
            if (it.state == STATE_EXPANDED) {
                collapseBottomSheet()
            } else {
                super.onBackPressed()
            }
        }
    }

    private inner class BottomSheetCallback: BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                dimOverlay.alpha = slideOffset
            } else if (slideOffset == 0f) {
                dimOverlay.visibility = View.GONE
            }
            bottomSheetListener?.onSlide(bottomSheet, slideOffset)
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == STATE_DRAGGING || newState == STATE_EXPANDED ) {
                dimOverlay.visibility = View.VISIBLE
            } else if (newState == STATE_COLLAPSED) {
                dimOverlay.visibility = View.GONE
            }
            bottomSheetListener?.onStateChanged(bottomSheet, newState)
        }
    }

    /**
     * = mediaID.type ==
    mediaSessionConnection.rootMediaId?.let {
    MediaID().fromString(
    it
    ).type
    }
     */
    private fun isRootId(mediaID: MediaID) : Boolean {
        var isRoot = false
        mediaSessionConnection.connected { isConnected ->
            if (isConnected) {
                isRoot = mediaID.type == MediaID().fromString(mediaSessionConnection.rootMediaId).type
            }
        }
        return isRoot
    }

    private fun getBrowseFragment(mediaID: MediaID): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaID.type) as MediaItemFragment?
    }

    // Observer event browse request from Main Presenter
    private fun navigateToMediaItem(mediaId: MediaID) {
        Log.d("nav mediaId is ${mediaId.mediaId} is ", getBrowseFragment(mediaId).toString())
        if (getBrowseFragment(mediaId) == null) {
            val fragment = MediaItemFragment.newInstance(mediaId)
            // Add Fragment
            addFragment(
                fragment = fragment,
                tag = mediaId.type,
                addToBackStack = !isRootId(mediaId)
            )
        }
    }

    override fun mediaItemClick(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?) {
        bottomSheetFragment.playerPresenter.mediaItemClick(mediaItem, extras)
    }
}