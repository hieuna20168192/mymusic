package com.example.mymusic.constants

import android.provider.MediaStore

enum class SongSortOrder(val rawValue: String) {
    /* Song sort order A-Z */
    SONG_A_Z(MediaStore.Audio.Media.DEFAULT_SORT_ORDER),
    /* Song sort order Z-A */
    SONG_Z_A(MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " DESC"),
    /* Song sort order year */
    SONG_YEAR(MediaStore.Audio.Media.YEAR + " DESC"),
    /* Song sort order duration */
    SONG_DURATION(MediaStore.Audio.Media.DURATION + " DESC");

    companion object {
        fun fromString(raw: String): SongSortOrder {
            return SongSortOrder.values().single { it.rawValue == raw }
        }

        fun toString(value: SongSortOrder): String = value.rawValue
    }
}

