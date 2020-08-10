package com.example.mymusic.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.R
import com.example.mymusic.models.Song
import com.example.mymusic.util.Utils

class SongAdapter internal constructor(var songList: List<Song>) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return ViewHolder(layoutView)
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.songTitle.text = songList[position].title
        holder.songArtist.text = songList[position].artist
        Utils.setImageUrl(holder.albumArt, songList[position].albumId)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var albumArt: ImageView = itemView.findViewById(R.id.album_art)
        var songTitle: TextView = itemView.findViewById(R.id.song_title)
        var songArtist: TextView = itemView.findViewById(R.id.song_artist)
    }

    fun updateDate(songs: List<Song>) {
        this.songList = songs
        notifyDataSetChanged()
    }
}