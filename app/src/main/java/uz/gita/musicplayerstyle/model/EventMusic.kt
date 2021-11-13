package uz.gita.musicplayerstyle.model

import android.database.Cursor
import android.media.MediaPlayer
import androidx.lifecycle.MutableLiveData
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity

object EventMusic {
    var selectMusicPos = 0
    var cursor : Cursor? = null

    lateinit var lastMusic:MusicData

    var isPlaying = false

    var currentTime = 0L
    var durationMusic = 0L
    var lastActionEnum : MusicEnum = MusicEnum.PLAY

    val selectMusicDataLiveData = MutableLiveData<MusicData>()
    val playingLiveData = MutableLiveData<Boolean>()
    val changeProgressLiveData = MutableLiveData<Long>()
    val durationMusicLiveData = MutableLiveData<Long>()
    val progressMusicLiveData = MutableLiveData<Long>()

}