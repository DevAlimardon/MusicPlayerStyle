package uz.gita.musicplayerstyle.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.utils.getMusicListCursor
import javax.inject.Inject

class Repository @Inject constructor() {
fun listCursor () :Flow <Cursor> = App.instance.getMusicListCursor()
}