package com.example.mymusic.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.mymusic.models.MediaID
import com.example.mymusic.models.Song
import java.lang.IllegalStateException

interface SongRepository {
    fun loadSongs(caller: String?): List<Song>
    fun getSongForId(id: Long): Song
    fun getSongsForIds(idList: LongArray): List<Song>
}

class SongReposImpl(
    private val context: Context
) : SongRepository {

    private val contentResolver = context.contentResolver

    override fun loadSongs(caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeSongCursor(null, null)

    }

    override fun getSongForId(id: Long): Song {
        val songs = makeSongCursor("_id = $id", null)
        Log.d("songs[0] is ", "${songs[0]}")
        return songs[0]
    }

    override fun getSongsForIds(idList: LongArray): List<Song> {
        var selection = "_id IN ("
        for (id in idList) {
            selection += "$id,"
        }
        if (idList.isNotEmpty()) {
            selection = selection.substring(0, selection.length - 1)
        }
        selection += ")"

        return makeSongCursor(selection, null)
    }

    @Suppress("Recycle")
    private fun makeSongCursor(selectionPar: String?, paramArrayOfString: Array<String>?): List<Song> {
        val selection = StringBuilder("is_music=1 AND title != ''")
        if (!selectionPar.isNullOrEmpty()) {
            selection.append(" AND $selectionPar")
        }

        Log.d("Selection is ", "$selection")

//        val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val query = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection.toString(),
            null,
            null
        )
            ?: throw IllegalStateException("Unable to query ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}, system returned null.")

        var songList: List<Song> = listOf()

        query?.use { cursor ->
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val trackColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val artistIDColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumIDColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                // Get values of columns for a given audio.
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getInt(durationColumn)
                val track = cursor.getInt(trackColumn)
                val artistID = cursor.getLong(artistIDColumn)
                val albumID = cursor.getLong(albumIDColumn)

//                var id: Long = 0,
//                var albumId: Long = 0,
//                var artistId: Long = 0,
//                var title: String = "",
//                var artist: String = "",
//                var album: String = "",
//                var duration: Int = 0,
//                var trackNumber: Int = 0

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songList += Song(id, albumID, artistID, title, artist, album, duration, track)
            }

            cursor?.close()
            return songList
        }
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: SongRepository? = null

        fun getInstance(context: Context) : SongRepository{
            instance ?: synchronized(this) {
                instance ?: SongReposImpl(context)
                    .also { instance = it }
            }
            return instance!!
        }
    }
}