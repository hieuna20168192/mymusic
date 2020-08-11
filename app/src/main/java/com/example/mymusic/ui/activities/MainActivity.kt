package com.example.mymusic.ui.activities

import Permission.Companion.REQUEST_CODE_PERMISSION
import android.content.ComponentName
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.mymusic.R
import com.example.mymusic.extensions.addFragment
import com.example.mymusic.models.MediaID
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.playback.server.MusicService
import com.example.mymusic.playback.server.MusicService.Companion.TYPE_ALL_SONGS
import com.example.mymusic.repository.SongReposImpl
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.ui.fragments.BottomSheetFragment
import com.example.mymusic.ui.fragments.MediaItemFragment
import com.example.mymusic.util.NavigationIconClickListener
import com.example.mymusic.ui.fragments.SongGridFragment
import com.example.mymusic.widgets.BottomSheetListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.music_backdrop.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaSessionConnection: MediaSessionConnection
    private lateinit var songRepository: SongRepository

    private var bottomSheetListener: BottomSheetListener? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songRepository = SongReposImpl(contentResolver)
        mediaSessionConnection = MediaSessionConnectionImpl.getInstance(
            application,
            ComponentName(application, MusicService::class.java)
        )
        Permission.checkPermission(this, REQUEST_CODE_PERMISSION)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.container,
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_SONGS.toString(), null))
                )
                .commit()

            // Set up Bottom Sheet Layout
            Handler().postDelayed({
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.bottomControlsContainer,
                        BottomSheetFragment()
                    ).commit()
            }, 150)
        }

        // Drop Menu Event
        nav_list.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.container,
                    MediaItemFragment.newInstance(MediaID(TYPE_ALL_SONGS.toString(), null))
                )
                .commit()
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

    private fun navigateToMediaItem(mediaId: MediaID) {
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

    private fun isRootId(mediaID: MediaID) = mediaID.type ==
            mediaSessionConnection.rootMediaId?.let {
                MediaID().fromString(
                    it
                ).type
            }

    private fun getBrowseFragment(mediaID: MediaID): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaID.type) as MediaItemFragment?
    }

}