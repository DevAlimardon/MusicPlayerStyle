package uz.gita.musicplayerstyle.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.skydoves.elasticviews.ElasticFinishListener
import com.skydoves.elasticviews.elasticAnimation
import com.skydoves.transformationlayout.TransformationCompat
import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.adapter.MainAdapter
import uz.gita.musicplayerstyle.model.EventMusic
import uz.gita.musicplayerstyle.model.MusicEnum
import uz.gita.musicplayerstyle.services.ForegroundService
import uz.gita.musicplayerstyle.viewmodels.MainScreenViewModel

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.musicplayerstyle.BuildConfig
import uz.gita.musicplayerstyle.adapter.FavouriteAdapter
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.databinding.ScreenFavouriteBinding
import uz.gita.musicplayerstyle.databinding.ScreenMainBinding
import uz.gita.musicplayerstyle.dialog.DetailDialog
import uz.gita.musicplayerstyle.model.MusicData
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import uz.gita.musicplayerstyle.shared.SharedPref
import uz.gita.musicplayerstyle.utils.checkPermissions
import uz.gita.musicplayerstyle.utils.getMusicListCursor
import uz.gita.musicplayerstyle.utils.showToast
import uz.gita.musicplayerstyle.utils.songArt
import uz.gita.musicplayerstyle.viewmodels.FavouriteViewModel
import java.io.File
import java.text.FieldPosition
import kotlin.math.log
import kotlin.system.measureNanoTime

@AndroidEntryPoint
class FavouriteScreen :Fragment (R.layout.screen_favourite) {
    private lateinit var adapter: FavouriteAdapter
    private lateinit var anim : AnimationDrawable
    private var favouriteList = ArrayList<FavouriteEntity>()
    private val vb by viewBinding (ScreenFavouriteBinding::bind)
    private val viewModel  by viewModels<FavouriteViewModel> ()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TransformationCompat.onTransformationStartContainer(this);
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = SharedPref.getShared()
        viewsVisibility(pref.firstTimeFavourite)
        loadMusic()
        anim = vb.screenMain.background as AnimationDrawable
        anim.setEnterFadeDuration(6000)
        anim.setExitFadeDuration(2000)
        val list = view.findViewById<RecyclerView>(R.id.list)
        adapter = FavouriteAdapter(favouriteList)
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(requireContext())

        vb.back.setOnClickListener {
            findNavController().popBackStack()
        }
        vb.bottomNav.setOnClickListener {
            findNavController().navigate(R.id.action_favouriteScreen_to_playScreen2)
        }

        adapter.setListener { anim, pos->
            pref.firstTimeFavourite = false
            pref.isfavourite = true
            EventMusic.selectMusicPos = pos
            startCommand(MusicEnum.PLAY)
            viewsVisibility(pref.firstTimeFavourite)
        }

        adapter.setListenerItem {music, pos, more->
            val popUp = PopupMenu(requireContext(), more)
            popUp.setOnMenuItemClickListener {
                if (it.itemId == R.id.share){
                    shareAudioFile(music)

                }
                if (it.itemId ==R.id.delete){
                    viewModel.deleteFavourite(music)
                   adapter.notifyItemRemoved(pos)
                    favouriteList.removeAt(pos)
                   // loadMusic()
//                    if (favouriteList.size == 0){
//                        EventMusic.selectMusicPos =0
//                        pref.isfavourite =false
//                        findNavController().popBackStack()
//                    }
                }
                if (it.itemId == R.id.details){
                    val dialog = DetailDialog()
                    val bundle = Bundle()
                    val d = MusicData(music.id, music.artist, music.musicTitle, music.uri, music.duration, Uri.parse(music.imageUri), music.size, music.date)
                    bundle.putSerializable("data", d)
                    dialog.arguments = bundle
                    dialog.show(childFragmentManager, "details")
                }

                return@setOnMenuItemClickListener true
            }
            popUp.inflate(R.menu.pop_up_menu)
            popUp.show()
        }

        vb.bottomPlay.setOnClickListener {
            EventMusic.lastActionEnum = if (EventMusic.lastActionEnum == MusicEnum.PAUSE) MusicEnum.PLAY
            else MusicEnum.PAUSE
            startCommand(EventMusic.lastActionEnum)

        }
        vb.bottomPrevious.setOnClickListener {

            if (EventMusic.selectMusicPos-1 > -1){
                startCommand(MusicEnum.PREVIOUS)
            }

        }
        vb.bottomNext.setOnClickListener {

            if (EventMusic.selectMusicPos+1 < favouriteList.size) {
                startCommand(MusicEnum.NEXT)
            }

        }
        EventMusic.selectMusicDataLiveData.observe(viewLifecycleOwner,selectMusicDataObserver)
        EventMusic.playingLiveData.observe(viewLifecycleOwner,playingObserver)
        viewModel.getAllFavouriteLivaData.observe(viewLifecycleOwner,getAllFavouriteLivaDataObserver)

    }

    private fun loadMusic(){
        favouriteList.clear()
        favouriteList = viewModel.getAllFavourite() as ArrayList<FavouriteEntity>
    }

    private val getAllFavouriteLivaDataObserver = Observer<List<FavouriteEntity>>{
//        favouriteList.clear()
//        favouriteList = it as ArrayList<FavouriteEntity>
       // adapter = FavouriteAdapter(favouriteList)
    }

    private val playingObserver = Observer<Boolean>{
        if (it) vb.bottomPlay.setImageResource(R.drawable.ic_pause)
        else vb.bottomPlay.setImageResource(R.drawable.ic_play)
    }
    private fun shareAudioFile(data :FavouriteEntity) {
        try {
            val file = File(data.uri)
            if(file.exists()) {
                val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                startActivity(intent)
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()

        }
    }

    private val selectMusicDataObserver = Observer<MusicData>{
        adapter.notifyDataSetChanged()
        vb.bottomArtist.text = it.artist
        vb.bottomTitle.text = it.musicTitle
        it.imageUri?.let {uri->
            vb.bottomImage.setImageURI(uri)
        }
    }


    private fun viewsVisibility(bool:Boolean) {
        if (bool) {
            vb.bottomImage.visibility = View.GONE
            vb.bottomArtist.visibility = View.GONE
            vb.bottomTitle.visibility = View.GONE
            vb.bottomNext.visibility = View.GONE
            vb.bottomPrevious.visibility = View.GONE
            vb.bottomPlay.visibility = View.GONE
            vb.bottomNav.visibility = View.GONE
        } else {
            vb.bottomImage.visibility = View.VISIBLE
            vb.bottomArtist.visibility = View.VISIBLE
            vb.bottomTitle.visibility = View.VISIBLE
            vb.bottomNext.visibility = View.VISIBLE
            vb.bottomPrevious.visibility = View.VISIBLE
            vb.bottomPlay.visibility = View.VISIBLE
            vb.bottomNav.visibility = View.VISIBLE
        }
    }

    private fun startCommand(command : MusicEnum){
        val intent = Intent(requireContext(), ForegroundService::class.java)
        val bundle = Bundle()
        bundle.putSerializable("action",command)
        intent.putExtras(bundle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(requireContext(), intent)
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




