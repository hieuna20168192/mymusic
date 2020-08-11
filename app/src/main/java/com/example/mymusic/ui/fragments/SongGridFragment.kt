package com.example.mymusic.ui.fragments

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.KeyEventDispatcher
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.models.MediaID
import com.example.mymusic.models.Song
import com.example.mymusic.playback.server.MediaSessionConnection
import com.example.mymusic.playback.server.MediaSessionConnectionImpl
import com.example.mymusic.playback.server.MusicService
import com.example.mymusic.presenter.MainContract
import com.example.mymusic.presenter.MainPresenter
import com.example.mymusic.repository.SongReposImpl
import com.example.mymusic.repository.SongRepository
import com.example.mymusic.ui.adapter.SongAdapter
import com.example.mymusic.util.SongGridItemDecoration
import kotlinx.android.synthetic.main.song_grid_fragment.view.recycler_view
import kotlinx.android.synthetic.main.song_grid_fragment.view.song_grid

class SongGridFragment : MediaItemFragment(), MainContract.View {

    private lateinit var presenter: MainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    val adapter = SongAdapter(listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.song_grid_fragment, container, false)

        // Set up Presenter
        setPresenter(MainPresenter(this, SongReposImpl(context!!.contentResolver),
            MediaSessionConnectionImpl.getInstance(activity!!, ComponentName(activity!!,
                MusicService::class.java))))

        presenter.onViewCreated()

        // Set up the RecyclerView
        view.recycler_view.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        view.recycler_view.layoutManager = gridLayoutManager
        // Set up adapter
        val largePadding = resources.getDimensionPixelSize(R.dimen.song_grid_spacing_large)
        val smallPadding = resources.getDimensionPixelSize(R.dimen.song_grid_spacing_small)
        view.recycler_view.addItemDecoration(SongGridItemDecoration(largePadding, smallPadding))
        view.recycler_view.adapter = adapter

        // Set cut corner background for API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.song_grid.background = context?.getDrawable(R.drawable.song_grid_background_shape)
        }

        return view
    }

    override fun displaySong(list: List<Song>) {
        adapter.updateDate(list)
    }

    override fun setPresenter(presenter: MainContract.Presenter) {
        this.presenter = presenter
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }
}