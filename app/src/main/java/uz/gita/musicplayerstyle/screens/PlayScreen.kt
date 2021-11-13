package uz.gita.musicplayerstyle.screens

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.*
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding

import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.databinding.ScreenPlayBinding
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import uz.gita.musicplayerstyle.adapter.FavouriteAdapter
import uz.gita.musicplayerstyle.model.EventMusic
import uz.gita.musicplayerstyle.model.MusicData
import uz.gita.musicplayerstyle.model.MusicEnum
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import uz.gita.musicplayerstyle.services.ForegroundService
import uz.gita.musicplayerstyle.shared.SharedPref
import uz.gita.musicplayerstyle.utils.setOnChangeListener
import uz.gita.musicplayerstyle.viewmodels.PlayScreenViewModel

@AndroidEntryPoint
class PlayScreen : Fragment(R.layout.screen_play) {
    private val vb by viewBinding (ScreenPlayBinding::bind)
    private lateinit var anim : AnimationDrawable
    private lateinit var musicData: MusicData
    private lateinit var list : ArrayList<FavouriteEntity>
    private val pref by lazy { SharedPref.getShared() }
    private val viewModel by viewModels <PlayScreenViewModel> ()
    private val adapter by lazy { FavouriteAdapter(list) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = viewModel.getAllFavourite() as ArrayList<FavouriteEntity>
        anim = vb.playScreen.background as AnimationDrawable
        anim.setEnterFadeDuration(6000)
        anim.setExitFadeDuration(2000)
        vb.centerImage.clipToOutline = true
        vb.back.setOnClickListener {
            findNavController().popBackStack()
        }

        vb.addFavourite.setOnClickListener {
            if (!isFavourite(musicData)){
                vb.addFavourite.animate().setDuration(300).scaleY(2f).scaleX(2f).withEndAction {
                    vb.anim.playAnimation()
                    vb.addFavourite.setImageResource(R.drawable.ic__heart2)
                    vb.addFavourite.animate().setDuration(300).scaleY(1f).scaleX(1f).start()
                }.start()
                viewModel.addFavourite(FavouriteEntity(
                    musicData.id,
                    musicData.artist,
                    musicData.musicTitle,
                    musicData.uri,
                    musicData.duration,
                    musicData.imageUri.toString(),
                    musicData.size,
                    musicData.date,

                ))
             //   adapter.notifyDataSetChanged()

            }else{
                vb.addFavourite.animate().setDuration(300).scaleY(2f).scaleX(2f).withEndAction {
                  //  vb.anim.playAnimation()
                    vb.addFavourite.setImageResource(R.drawable.ic_heart)
                    vb.addFavourite.animate().setDuration(300).scaleY(1f).scaleX(1f).start()
                }.start()
               viewModel.deleteFavourite(FavouriteEntity(
                   musicData.id,
                   musicData.artist,
                   musicData.musicTitle,
                   musicData.uri,
                   musicData.duration,
                   musicData.imageUri.toString(),
                   musicData.size,
                   musicData.date
               ))
               // adapter.notifyDataSetChanged()

            }

        }

        vb.playMusic.setOnClickListener {
//            list = viewModel.getAllFavourite() as ArrayList<FavouriteEntity>
//            if (list.size == 0 && pref.isfavourite){
//
//                return@setOnClickListener
//            }
            EventMusic.lastActionEnum = if (EventMusic.lastActionEnum == MusicEnum.PAUSE) {
                vb.centerImage.animate().setDuration(500).scaleY(1f).scaleX(1f).start()
                vb.titleMusic.animate().setDuration(500).scaleY(1f).scaleX(1f).start()
                vb.artistMusic.animate().setDuration(500).scaleY(1f).scaleX(1f).start()
                vb.playMusic.animate().setDuration(500).scaleY(1f).scaleX(1f).start()
                MusicEnum.PLAY
            }
            else {
                vb.centerImage.animate().setDuration(500).scaleY(0.7f).scaleX(0.7f).start()
                vb.titleMusic.animate().setDuration(500).scaleY(1.2f).scaleX(1.2f).start()
                vb.artistMusic.animate().setDuration(500).scaleY(1.2f).scaleX(1.2f).start()
                vb.playMusic.animate().setDuration(500).scaleY(1.7f).scaleX(1.7f).start()
                MusicEnum.PAUSE
            }
            startCommand(EventMusic.lastActionEnum, EventMusic.currentTime.toInt())
        }

        vb.nextMusic.setOnClickListener {
            list = viewModel.getAllFavourite() as ArrayList<FavouriteEntity>
            if (list.size == 0 && pref.isfavourite){
                return@setOnClickListener
            }
            startCommand(MusicEnum.NEXT, 0)
        }



        vb.previousMusic.setOnClickListener {
            list = viewModel.getAllFavourite() as ArrayList<FavouriteEntity>
            if (list.size == 0 && pref.isfavourite)return@setOnClickListener
            startCommand(MusicEnum.PREVIOUS, 0)
        }


        vb.seekBar.setOnChangeListener {
            startCommand(EventMusic.lastActionEnum, it)
        }

        EventMusic.playingLiveData.observe(viewLifecycleOwner, playingLiveDataObserver)
        EventMusic.changeProgressLiveData.observe(viewLifecycleOwner, changeProgressLiveDataObserver)
        EventMusic.durationMusicLiveData.observe(viewLifecycleOwner,durationMusicLiveDataObserver )
        EventMusic.selectMusicDataLiveData.observe(viewLifecycleOwner, selectMusicDataLiveDataObserver)
        EventMusic.progressMusicLiveData.observe(viewLifecycleOwner,progressMusicLiveDataObserver )

    }

    private val playingLiveDataObserver = Observer<Boolean>{
        if (it) vb.playMusic.setImageResource(R.drawable.ic_pause)
        else vb.playMusic.setImageResource(R.drawable.ic_play)
    }
    private val progressMusicLiveDataObserver = Observer<Long>{
        vb.timer.text =convertTime(it)
    }
    private val changeProgressLiveDataObserver = Observer<Long>{
        vb.seekBar.progress = it.toInt()
        EventMusic.currentTime = it

    }
    private val durationMusicLiveDataObserver = Observer<Long>{
        vb.duration.text = convertTime(it)
        vb.seekBar.max = it.toInt()
    }
    private fun selectScreenMusic(d :MusicData):MusicData {
        return if (!pref.isfavourite){
            d
        } else {
            val data =list[EventMusic.selectMusicPos]
            MusicData(data.id, data.artist, data.musicTitle, data.uri, data.duration, Uri.parse(data.imageUri), data.size, data.date)

        }
    }

    private fun convertTime(time :Long) :String{
        val minutes: Int = time.toInt() / (60 * 1000)
        val seconds: Int = time.toInt() / 1000 % 60
        return  String.format("%d:%02d", minutes, seconds)
    }

    private val selectMusicDataLiveDataObserver = Observer<MusicData>{data->
        musicData = data
        data.imageUri?.let {
            vb.centerImage.setImageURI(it)
        }
        vb.artistMusic.text = data.artist
        vb.titleMusic.text = data.musicTitle
        vb.duration.text = convertTime(data.duration)
       if (isFavourite(data)){
           vb.addFavourite.setImageResource(R.drawable.ic__heart2)
       }
       else {
           vb.addFavourite.setImageResource(R.drawable.ic_heart)
       }

    }

    private fun isFavourite(data :MusicData) :Boolean{
        list = viewModel.getAllFavourite() as ArrayList<FavouriteEntity>
        for (i in list){
            if (data.id ==i.id){
                return true
            }
        }
        return false
    }

    private fun startCommand(command : MusicEnum, progress :Int){
        val intent = Intent(requireContext(), ForegroundService::class.java)
        val bundle = Bundle()
        bundle.putSerializable("action",command)
        intent.putExtra("progress",progress)
        intent.putExtras(bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ContextCompat.startForegroundService(
            requireContext(),
            intent)
        else requireActivity().startService(intent)
    }



    override fun onResume() {
        super.onResume()
        if (!anim.isRunning)
            anim.start()
    }

    override fun onPause() {
        super.onPause()
        if (anim.isRunning)
            anim.stop()
    }

}