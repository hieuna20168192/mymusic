package com.example.mymusic.repository

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.example.mymusic.models.MediaID
import com.example.mymusic.models.Song
import java.lang.IllegalStateException

interface SongRepository {
    var songList: List<Song>
    fun loadSongs(caller: String?): List<Song>
}

class SongReposImpl(
    private val contentResolver: ContentResolver
) : SongRepository {

    override var songList: List<Song> = mutableListOf()

    override fun loadSongs(caller: String?): List<Song> {
        MediaID.currentCaller = caller
        return makeSongCursor(null, null)

    }

    @Suppress("Recycle")
    private fun makeSongCursor(selection: String?, paramArrayOfString: Array<String>?): List<Song> {
//        val selectionStatement = StringBuilder("is_music=1 AND title != ''")
//        if (!selection.isNullOrEmpty()) {
//            selectionStatement.append(" AND $selection")
//        }

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
            null,
            null,
            null
        ) ?: throw IllegalStateException("Unable to query ${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}, system returned null.")

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
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val album = cursor.getString(albumColumn)
                val duration = cursor.getInt(durationColumn)
                val track = cursor.getInt(trackColumn)
                val artistID = cursor.getLong(artistIDColumn)
                val albumID = cursor.getLong(albumIDColumn)

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                songList += (Song(id, albumID, artistID, title, artist, album, duration, track))
            }
            Log.d("songList.size() = ", songList.size.toString())
            cursor?.close()
            return songList
        }
    }
}