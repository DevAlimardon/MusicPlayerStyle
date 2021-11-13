package uz.gita.musicplayerstyle.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.model.EventMusic
import uz.gita.musicplayerstyle.model.EventMusic.cursor
import uz.gita.musicplayerstyle.model.MusicData
import uz.gita.musicplayerstyle.model.MusicEnum
import uz.gita.musicplayerstyle.model.dao.FavouriteDao
import uz.gita.musicplayerstyle.model.database.MusicDatabase
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import uz.gita.musicplayerstyle.repository.FavouriteRepository
import uz.gita.musicplayerstyle.shared.SharedPref
import uz.gita.musicplayerstyle.utils.getMusicListCursor
import uz.gita.musicplayerstyle.utils.songArt
import uz.gita.musicplayerstyle.utils.timber
import java.io.File
import java.net.URI
import javax.inject.Inject

@SuppressLint("RestrictedApi")
@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class ForegroundService  : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    override fun onBind(p0: Intent?): IBinder?=null
    private val CHANNEL_ID = "MY_MUSIC"
    private var _mediaPlay : MediaPlayer? = null
    private val mediaPlayer get() = _mediaPlay!!
    private var job : Job? = null
  //  @Inject lateinit var dao : FavouriteRepository
    private val dao = MusicDatabase.getDatabase()
    private lateinit var list: ArrayList<FavouriteEntity>
    private val mBuilder by lazy { NotificationCompat.Builder(this, CHANNEL_ID)  }
    private lateinit var remoteView: RemoteViews
    private val manager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private val notification by lazy {
        mBuilder
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_music)
            .setContentTitle(getMusicByPos(EventMusic.selectMusicPos).musicTitle)
            .setContentText(getMusicByPos(EventMusic.selectMusicPos).artist)
          //  .setStyle(NotificationCompat.DecoratedCustomViewStyle())



    }
    private val pref by lazy { SharedPref.getShared() }
    override fun onCreate() {
        createChannel()
        list = dao.getFavouriteDao().getAllFavouriteMusics() as ArrayList<FavouriteEntity>
        _mediaPlay = MediaPlayer()
        startForeground(1, createNotification())
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID,"My music app", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
    }
    private fun createNotification() : Notification{
        return notification.setCustomContentView(createRemoteView())
            .build()
    }


    private fun createRemoteView() : RemoteViews {
        remoteView = RemoteViews(this.packageName, R.layout.notification_layout)
     //   val musicData = getMusicByPos(EventMusic.selectMusicPos)
        val musicData = selectScreenMusic()

        if (mediaPlayer.isPlaying) remoteView.setImageViewResource(R.id.play, R.drawable.ic_pause)
         else remoteView.setImageViewResource(R.id.play, R.drawable.ic_play)
        remoteView.setTextViewText(R.id.artist, musicData.artist)
        remoteView.setTextViewText(R.id.title, musicData.musicTitle)
        musicData.imageUri?.let {
            remoteView.setImageViewUri(R.id.remoteImage,it)
        }

        remoteView.setOnClickPendingIntent(R.id.play,createPendingIntent(MusicEnum.MANAGE))
        remoteView.setOnClickPendingIntent(R.id.next,createPendingIntent(MusicEnum.NEXT))
        remoteView.setOnClickPendingIntent(R.id.previous,createPendingIntent(MusicEnum.PREVIOUS))
        remoteView.setOnClickPendingIntent(R.id.close,createPendingIntent(MusicEnum.CLOSE))
        return remoteView
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createPendingIntent(action :MusicEnum) : PendingIntent{
        val intent = Intent(this, ForegroundService::class.java)
        intent.putExtra("action", action)
        return  PendingIntent.getService(this,action.ordinal,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        list = dao.getFavouriteDao().getAllFavouriteMusics() as ArrayList<FavouriteEntity>
        val command = intent?.extras?.getSerializable("action") as MusicEnum
        val progress = intent.extras?.getInt("progress",0)
        startCommand(command, progress!!)

        return START_NOT_STICKY
    }


    private fun startCommand(command: MusicEnum, progress : Int){
        when(command){
            MusicEnum.MANAGE -> {
                if (mediaPlayer.isPlaying){
                    startCommand(MusicEnum.PAUSE, progress)
                }else  startCommand(MusicEnum.PLAY, progress)
            }
            MusicEnum.PLAY -> {
                if (mediaPlayer.isPlaying)
                    mediaPlayer.stop()
                val musicData = selectScreenMusic()
                _mediaPlay = MediaPlayer.create(this, Uri.fromFile(File(musicData.uri ?: "")))
                mediaPlayer.start()
                mediaPlayer.seekTo(progress)
                mediaPlayer.setOnCompletionListener {
                    EventMusic.selectMusicPos += 1
                    startCommand(MusicEnum.PLAY,0)
                }
                EventMusic.isPlaying = true
                EventMusic.lastActionEnum = MusicEnum.PLAY
                EventMusic.playingLiveData.value = true
                EventMusic.selectMusicDataLiveData.value = musicData
                startForeground(1,createNotification())

                EventMusic.currentTime = progress.toLong()
                EventMusic.durationMusic = musicData.duration
                EventMusic.durationMusicLiveData.value = musicData.duration
                job?.let {
                    it.cancel()
                }
                job = changeProgress().onEach {
                    EventMusic.changeProgressLiveData.postValue(it)
                    EventMusic.currentTime = it
                }.launchIn(scope)
            }
            MusicEnum.PAUSE ->{
                mediaPlayer.seekTo(progress)
                job?.let {
                    it.cancel()
                }
                val musicData = selectScreenMusic()
                mediaPlayer.pause()
                EventMusic.isPlaying = false
                EventMusic.lastActionEnum = MusicEnum.PAUSE
                EventMusic.playingLiveData.value = false
                EventMusic.selectMusicDataLiveData.value = musicData
                startForeground(1,  createNotification())

            }
            MusicEnum.CLOSE ->{
                mediaPlayer.stop()
                EventMusic.lastActionEnum = MusicEnum.PAUSE
                EventMusic.playingLiveData.value = false
                pref.firstTime = true
                pref.firstTimeFavourite = true
                stopForeground(true)

            }
            MusicEnum.NEXT -> {
                if (pref.isfavourite){
                    if (EventMusic.selectMusicPos == list.size-1) EventMusic.selectMusicPos = 0
                    else EventMusic.selectMusicPos += 1
                }else{
                    if (EventMusic.selectMusicPos == cursor!!.count-1) EventMusic.selectMusicPos = 0
                    else EventMusic.selectMusicPos += 1
                }

                setSeekBarBegin()
                startCommand(EventMusic.lastActionEnum,0)
            }
            MusicEnum.PREVIOUS-> {
                if (pref.isfavourite){
                    if (EventMusic.selectMusicPos ==0) EventMusic.selectMusicPos = 0
                    else EventMusic.selectMusicPos -= 1
                }else{
                    if (EventMusic.selectMusicPos == 0) EventMusic.selectMusicPos = 0
                    else EventMusic.selectMusicPos -= 1
                }
                setSeekBarBegin()
                startCommand(EventMusic.lastActionEnum,0)
            }
        }
    }

    private fun selectScreenMusic():MusicData {
        return if (!pref.isfavourite){
            getMusicByPos(EventMusic.selectMusicPos)

        } else {
             if (list.size >0){
                 val data = list[EventMusic.selectMusicPos]
                EventMusic.lastMusic = MusicData(data.id, data.artist, data.musicTitle, data.uri, data.duration, Uri.parse(data.imageUri), data.size, data.date)
                MusicData(data.id, data.artist, data.musicTitle, data.uri, data.duration, Uri.parse(data.imageUri), data.size, data.date)

            } else{
                EventMusic.lastMusic
            }

        }
    }

    private fun getMusicByPos(pos :Int) : MusicData{
        cursor?.moveToPosition(pos)
        return MusicData(
            cursor!!.getInt(0),
            cursor!!.getString(1),
            cursor!!.getString(2),
            cursor!!.getString(3),
            cursor!!.getLong(4),
            App.instance.songArt(cursor!!.getLong(5)),
            cursor!!.getString(6),
            cursor!!.getString(7)
        )
    }

    private fun changeProgress() : Flow<Long> = flow {
        for (i in EventMusic.currentTime until EventMusic.durationMusic step 250) {
            emit(i)
            kotlinx.coroutines.delay(250)
        }
    }

    private fun setSeekBarBegin(){
        EventMusic.currentTime = 0L
        job?.let {
            it.cancel()
        }
        job = changeProgress().onEach {
            EventMusic.changeProgressLiveData.postValue(0L)
            EventMusic.currentTime = 0L
        }.launchIn(scope)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _mediaPlay = null
    }


}