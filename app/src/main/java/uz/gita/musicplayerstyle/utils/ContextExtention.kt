package uz.gita.musicplayerstyle.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

val projection = arrayOf(
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media.DATA,
    MediaStore.Audio.Media.DURATION,
    MediaStore.Audio.Media.ALBUM_ID,
    MediaStore.Audio.Media.SIZE,
    MediaStore.Audio.Media.DATE_ADDED
)



fun Context.getMusicListCursor() : Flow<Cursor> = flow {
    val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

    val cursor : Cursor = contentResolver.query(
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        null,
        null
    )?: return@flow

    emit(cursor)
}.flowOn(Dispatchers.IO)

fun Context.songArt(albumId: Long): Uri? {
    try {
        val sArtworkUri: Uri = Uri.parse("content://media/external/audio/albumart")
        val uri = ContentUris.withAppendedId(sArtworkUri, albumId)
        val pfd: ParcelFileDescriptor? = this.contentResolver
            .openFileDescriptor(uri, "r")
        if (pfd != null) {
            return uri
        }
    } catch (e: Exception) {
        timber(e.message.toString())
    }
    return null
}