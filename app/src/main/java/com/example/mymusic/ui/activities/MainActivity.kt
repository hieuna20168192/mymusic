package com.example.mymusic.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.mymusic.R
import com.example.mymusic.util.NavigationIconClickListener
import com.example.mymusic.ui.fragments.SongGridFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.container,
                    SongGridFragment()
                )
                .commit()
        }

        // Set up the tool bar
        (this as AppCompatActivity).setSupportActionBar(app_bar)
        this.app_bar.setNavigationOnClickListener(
            NavigationIconClickListener(
                this,
                sliding_layout,
                AccelerateDecelerateInterpolator(),
                ContextCompat.getDrawable(this,
                    R.drawable.branch_menu
                ),
                ContextCompat.getDrawable(this,
                    R.drawable.close_menu
                )
            )
        )
    }
}