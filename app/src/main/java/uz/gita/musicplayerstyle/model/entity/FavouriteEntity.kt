package uz.gita.musicplayerstyle.model.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class FavouriteEntity(
    @PrimaryKey (autoGenerate = true)
    var id: Int,
    var artist:String,
    var musicTitle:String,
    val uri: String,
    val duration:Long,
    val imageUri: String,
    val size : String,
    val date : String
    ):Serializable