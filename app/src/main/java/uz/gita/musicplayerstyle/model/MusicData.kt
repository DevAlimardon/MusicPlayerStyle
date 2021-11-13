package uz.gita.musicplayerstyle.model


import android.net.Uri
import java.io.Serializable

class MusicData (
    var id : Int,
    var artist :String,
    var musicTitle :String,
    val uri : String,
    val duration :Long,
    val imageUri : Uri?,
    val size : String,
    val date : String
) :Serializable