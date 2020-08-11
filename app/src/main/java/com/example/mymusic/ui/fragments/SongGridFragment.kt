package com.example.mymusic.ui.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.extensions.addOnItemClick
import com.example.mymusic.extensions.getExtraBundle
import com.example.mymusic.extensions.toSongIds
import com.example.mymusic.models.Song
import com.example.mymusic.presenter.MainContract
import com.example.mymusic.ui.adapter.SongAdapter
import com.example.mymusic.util.InjectorUtils
import com.example.mymusic.util.SongGridItemDecoration
import kotlinx.android.synthetic.main.song_grid_fragment.view.recycler_view
import kotlinx.android.synthetic.main.song_grid_fragment.view.song_grid

class SongGridFragment : MediaItemFragment(), MainContract.View {

    private lateinit var reposPresenter: MainContract.ReposPresenter

    private lateinit var itemClickCallback: MediaItemSelected


    interface MediaItemSelected {
        fun mediaItemClick(mediaItem: MediaBrowserCompat.MediaItem, extras: Bundle?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    private val songAdapter = SongAdapter(listOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.song_grid_fragment, container, false)

        // Inject Presenter
        reposPresenter = InjectorUtils.provideReposPresenter(this)

        reposPresenter.onViewCreated()

        // Set up the RecyclerView
        view.recycler_view.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
        view.recycler_view.layoutManager = gridLayoutManager
        // Set up adapter
        val largePadding = resources.getDimensionPixelSize(R.dimen.song_grid_spacing_large)
        val smallPadding = resources.getDimensionPixelSize(R.dimen.song_grid_spacing_small)
        view.recycler_view.addItemDecoration(SongGridItemDecoration(largePadding, smallPadding))

        // Set SongList click event
        view.recycler_view.apply {
            view.recycler_view.adapter = songAdapter
            addOnItemClick { position: Int, _: View ->
                songAdapter.getSongForPosition(position)?.let {
                    val extras = getExtraBundle(
                        songAdapter.songList.toSongIds(),
                        getString(R.string.all_songs)
                    )
                    // Handle mediaItemClick with song, extras
                    Log.d("HandEvent click ", "pos = $position and id = ${it.id}")
                    itemClickCallback.mediaItemClick(it, extras)
                }
            }
        }

        // Set cut corner background for API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.song_grid.background = context?.getDrawable(R.drawable.song_grid_background_shape)
        }

        return view
    }

    override fun displaySong(list: List<Song>) {
        songAdapter.updateDate(list)
    }

    override fun setPresenter(presenter: MainContract.ReposPresenter) {
        this.reposPresenter = presenter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        itemClickCallback = context as MediaItemSelected
    }

}