package com.example.mymusic.ui.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.util.SongGridItemDecoration
import kotlinx.android.synthetic.main.song_grid_fragment.view.*
import kotlinx.android.synthetic.main.song_grid_fragment.view.song_grid

class SongGridFragment : Fragment() {

    companion object {
        fun newInstance() = SongGridFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.song_grid_fragment, container, false)

        // Set up the toolbar

        // Set up the RecyclerView
        view.recycler_view.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        view.recycler_view.layoutManager = gridLayoutManager
        // Set up adapter

        val largePadding = resources.getDimensionPixelSize(R.dimen.song_grid_spacing_large)
        val smallPadding = resources.getDimensionPixelSize(R.dimen.song_grid_spacing_small)
        view.recycler_view.addItemDecoration(SongGridItemDecoration(largePadding, smallPadding))

        // Set cut corner background for API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.song_grid.background = context?.getDrawable(R.drawable.song_grid_background_shape)
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}