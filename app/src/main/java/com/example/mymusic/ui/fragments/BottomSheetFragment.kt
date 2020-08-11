package com.example.mymusic.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.mymusic.R
import com.example.mymusic.ui.activities.MainActivity
import com.example.mymusic.widgets.BottomSheetListener
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*

class BottomSheetFragment : Fragment(), BottomSheetListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set up UI
        val layoutParams = progressBar.layoutParams as LinearLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams
        songTitle.isSelected = true

        btnTogglePlayPause.setOnClickListener {
            // Handle event play/pause
        }

        btnPlayPause.setOnClickListener {
            // Handle event quick play/pause
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
}