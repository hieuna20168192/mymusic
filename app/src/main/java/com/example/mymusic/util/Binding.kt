package com.example.mymusic.util

import android.graphics.Bitmap
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.mymusic.R
import com.example.mymusic.util.Utils.getAlbumArtUri

object Binding {
    val IMAGE_ROUND_CORNERS_TRANSFORMER: Transformation<Bitmap>
        get() = RoundedCorners(2)

    val LARGE_IMAGE_ROUND_CORNERS_TRANSFORMER: Transformation<Bitmap>
        get() = RoundedCorners(5)

    val EXTRA_LARGE_IMAGE_ROUND_CORNERS_TRANSFORMER: Transformation<Bitmap>
        get() = RoundedCorners(8)


    fun setImageUrl(view: ImageView, albumId: Long) {
        val size = view.resources.getDimensionPixelSize(R.dimen.album_art)
        val options = RequestOptions()
            .centerCrop()
            .override(size, size)
            .transform(IMAGE_ROUND_CORNERS_TRANSFORMER)
            .placeholder(R.drawable.ic_music_note)
        Glide.with(view)
            .load(getAlbumArtUri(albumId))
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(options)
            .into(view)
    }

    fun setPlayState(view: ImageView, state: Int) {
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            view.setImageResource(R.drawable.ic_pause_white_24dp)
        } else {
            view.setImageResource(R.drawable.ic_play_outline)
        }
    }

    fun setRepeatMode(view: ImageView, mode: Int) {
        when (mode) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> view.setImageResource(R.drawable.ic_repeat_none)
            PlaybackStateCompat.REPEAT_MODE_ONE -> view.setImageResource(R.drawable.ic_repeat_one)
            PlaybackStateCompat.REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat_all)
            else -> view.setImageResource(R.drawable.ic_repeat_none)
        }
    }

    fun setShuffleMode(view: ImageView, mode: Int) {
        when (mode) {
            PlaybackStateCompat.SHUFFLE_MODE_NONE -> view.setImageResource(R.drawable.ic_shuffle_none)
            PlaybackStateCompat.SHUFFLE_MODE_ALL -> view.setImageResource(R.drawable.ic_shuffle_all)
            else -> view.setImageResource(R.drawable.ic_shuffle_none)
        }
    }

    fun setDuration(view: TextView, duration: Int) {
        view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
    }

}
