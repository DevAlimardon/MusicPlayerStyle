package uz.gita.musicplayerstyle.adapter

import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import de.hdodenhof.circleimageview.CircleImageView
import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.model.EventMusic
import uz.gita.musicplayerstyle.model.MusicData
import uz.gita.musicplayerstyle.shared.SharedPref
import uz.gita.musicplayerstyle.utils.songArt
import java.util.zip.Inflater

class MainAdapter : RecyclerView.Adapter<MainAdapter.Vh>() {
    var cursor :Cursor? = null
    private var listener : ((View,Int)->Unit)? = null
    private var listenerItem : ((MusicData, Int, View)->Unit)? = null
    inner class Vh (view: View) :RecyclerView.ViewHolder(view){
        private val title : TextView = view.findViewById(R.id.music_title)
        private val artist : TextView = view.findViewById(R.id.music_artist)
        private val more :ImageView = view.findViewById(R.id.moreItem)
        private val image : CircleImageView = view.findViewById(R.id.item_image)
        private val item :ConstraintLayout = view.findViewById(R.id.item)
        private val anim : LottieAnimationView = view.findViewById(R.id.animation)
        private val pref = SharedPref.getShared()
        init {
            item.setOnClickListener {
                if (absoluteAdapterPosition>-1) listener?.invoke(anim, absoluteAdapterPosition)
            }
            more.setOnClickListener {
                if (absoluteAdapterPosition>-1) listenerItem?.invoke(getMusicByPos(absoluteAdapterPosition), absoluteAdapterPosition, it)
            }
        }

        fun bind(){
            val data = getMusicByPos(absoluteAdapterPosition)
            anim.visibility = View.GONE
            title.text = data.musicTitle
            artist.text =data.artist
            data.imageUri?.let {
                image.setImageURI(it)
            }

            if (EventMusic.selectMusicPos == absoluteAdapterPosition && !pref.firstTime){
                title.setTextColor(ContextCompat.getColor(App.instance, R.color.music_title1))
                artist.setTextColor(ContextCompat.getColor(App.instance, R.color.music_artist1))
            } else{
                title.setTextColor(ContextCompat.getColor(App.instance, R.color.music_title))
                artist.setTextColor(ContextCompat.getColor(App.instance, R.color.music_artist))
            }


           if (EventMusic.selectMusicPos == absoluteAdapterPosition && EventMusic.isPlaying){
               anim.visibility = View.VISIBLE
           } else{
               anim.visibility = View.GONE
           }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return Vh(view)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = cursor?.count?:0

    @SuppressLint("Range")
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

    fun setListener(f:(View, Int) -> Unit){
        listener = f
    }
    fun setListenerItem(f:(MusicData, Int, View) -> Unit){
        listenerItem = f
    }

}