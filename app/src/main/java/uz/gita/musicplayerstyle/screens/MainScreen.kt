package uz.gita.musicplayerstyle.screens

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.getExternalFilesDirs
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.skydoves.elasticviews.elasticAnimation
import com.skydoves.transformationlayout.TransformationCompat
import dagger.hilt.android.AndroidEntryPoint
import uz.gita.musicplayerstyle.BuildConfig
import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.adapter.MainAdapter
import uz.gita.musicplayerstyle.databinding.ScreenMainBinding
import uz.gita.musicplayerstyle.dialog.DetailDialog
import uz.gita.musicplayerstyle.model.EventMusic
import uz.gita.musicplayerstyle.model.MusicData
import uz.gita.musicplayerstyle.model.MusicEnum
import uz.gita.musicplayerstyle.services.ForegroundService
import uz.gita.musicplayerstyle.shared.SharedPref
import uz.gita.musicplayerstyle.viewmodels.MainScreenViewModel
import java.io.File
import kotlin.io.path.deleteExisting

@AndroidEntryPoint
class MainScreen :Fragment (R.layout.screen_main) {
    private val viewModel by viewModels<MainScreenViewModel>()
    private lateinit var adapter: MainAdapter
    private lateinit var anim : AnimationDrawable
    private val vb by viewBinding (ScreenMainBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TransformationCompat.onTransformationStartContainer(this);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = SharedPref.getShared()
        viewsVisibility(pref.firstTime)

        anim = vb.screenMain.background as AnimationDrawable
        anim.setEnterFadeDuration(6000)
        anim.setExitFadeDuration(2000)
        val list = view.findViewById<RecyclerView>(R.id.list)

        adapter = MainAdapter()
        adapter.cursor = EventMusic.cursor
        list.adapter = adapter
        list.layoutManager = LinearLayoutManager(requireContext())
        vb.favourite.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreen_to_favouriteScreen)
        }

        vb.bottomNav.setOnClickListener {
            it.elasticAnimation {  }
            findNavController().navigate(R.id.action_mainScreen_to_playScreen2)
        }

        adapter.setListener { anim, pos->
            pref.firstTime = false
            pref.isfavourite = false
            EventMusic.selectMusicPos = pos
            startCommand(MusicEnum.PLAY)
            viewsVisibility(pref.firstTime)
        }

        adapter.setListenerItem {music, pos, more->
            val popUp = PopupMenu(requireContext(), more)
            popUp.setOnMenuItemClickListener {
                if (it.itemId == R.id.share){
                    shareAudioFile(music)

                }
                if (it.itemId ==R.id.delete){
                    Toast.makeText(requireContext(), "Delete", Toast.LENGTH_SHORT).show()
//                   val file = File(music.uri)
//                   val b = requireContext().getExternalFilesDir()
//                    Log.d("TTT", "$b")
//                  b.delete()
//                    val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
//                    val a =requireActivity().contentResolver.delete(uri, null, null)
//                    val b = File(uri.toString())
//                    b.delete()

                  // val file2 = File(uri.toString())
//                    val file2 = File(Environment.getDataDirectory(), music.uri)
//                    file2.delete()
                  //  file.deleteRecursively()
//                    Log.d("TTT", "$file2")
//                    Log.d("TTT", "${file.absolutePath}")
//                    val resolver: ContentResolver = requireActivity().contentResolver
//                    val where = MediaStore.Audio.Media._ID + "=?"
//                    val whereVal = arrayOf(music.musicTitle)
//                    resolver.delete( uri, where, whereVal)
                  //  deletePlaylist(music)
                }
                if (it.itemId == R.id.details){
                    val dialog = DetailDialog()
                    val bundle = Bundle()
                    bundle.putSerializable("data", music)
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
            it.elasticAnimation {
            }
        }
        vb.bottomPrevious.setOnClickListener {
            it.elasticAnimation {  }
            if (EventMusic.selectMusicPos-1 > -1){
                startCommand(MusicEnum.PREVIOUS)
            }

        }
        vb.bottomNext.setOnClickListener {
            if (EventMusic.selectMusicPos+1 < EventMusic.cursor!!.count) {
                startCommand(MusicEnum.NEXT)
            }
            it.elasticAnimation {  }
        }
        viewModel.cursorLiveData.observe(viewLifecycleOwner, cursorObserver)
        EventMusic.selectMusicDataLiveData.observe(viewLifecycleOwner,selectMusicDataObserver)
        EventMusic.playingLiveData.observe(viewLifecycleOwner,playingObserver)
        handleBackPressed()
    }
    private fun handleBackPressed(){
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }
    private fun deletePlaylist(data :MusicData) {

//        val uri = getUriFromDisplayName(context, displayName)
//        if (uri != null) {
//            final ContentResolver resolver = context.getContentResolver();
//            String[] selectionArgsPdf = new String[]{displayName};
//
//            try {
//                resolver.delete(uri, MediaStore.Files.FileColumns.DISPLAY_NAME + "=?", selectionArgsPdf);
//                return true;
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                // show some alert message
//            }
//        }
//        return false;
        val uri = Uri.parse(data.uri)
        val resolver: ContentResolver = requireActivity().contentResolver
        val where = MediaStore.Audio.Media._ID + "=?"
        //val whereVal = arrayOf(playlistid)
        resolver.delete( uri, null, null)
    }

    private val playingObserver = Observer<Boolean>{
        if (it) vb.bottomPlay.setImageResource(R.drawable.ic_pause)
        else vb.bottomPlay.setImageResource(R.drawable.ic_play)
    }

    private val selectMusicDataObserver = Observer<MusicData>{
        adapter.notifyDataSetChanged()
        vb.bottomArtist.text = it.artist
        vb.bottomTitle.text = it.musicTitle
        it.imageUri?.let {uri->
            vb.bottomImage.setImageURI(uri)
        }
    }

    private val cursorObserver = Observer<Cursor?>{

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

    private fun shareAudioFile(data :MusicData) {
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




