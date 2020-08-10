package com.example.mymusic.ui.activities

import Permission.Companion.REQUEST_CODE_PERMISSION
import android.content.ComponentName
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.mymusic.R
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.playback.server.MusicService
import com.example.mymusic.repository.SongReposImpl
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.ui.fragments.BottomSheetFragment
import com.example.mymusic.util.NavigationIconClickListener
import com.example.mymusic.ui.fragments.SongGridFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var mediaSessionConnection: MediaSessionConnection
    private lateinit var songRepository: SongRepository

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
                .add(
                    R.id.container,
                    SongGridFragment()
                )
                .commit()

            // Set up Bottom Sheet Layout
            Handler().postDelayed({
                supportFragmentManager
                    .beginTransaction()
                    .add(
                        R.id.bottomControlsContainer,
                        BottomSheetFragment()
                    )
            }, 150)
        }

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

}