package uz.gita.musicplayerstyle.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.model.MusicData
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity


class DetailDialog :BottomSheetDialogFragment() {
    private lateinit var data :MusicData


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.dialog_music_details, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        return  view
    }



    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.recycle_view_bg)
        view.setBackgroundColor(Color.TRANSPARENT)
        val image : ImageView = view.findViewById(R.id.mainImage)
        val artist :TextView = view.findViewById(R.id.music_artist2)
        val title:TextView = view.findViewById(R.id.music_title2)
        val duration :TextView = view.findViewById(R.id.duration2)
        val size :TextView = view.findViewById(R.id.size)
        val  date :TextView = view.findViewById(R.id.date)
        image.clipToOutline = true


        arguments?.let {
             data = it.getSerializable("data") as MusicData

        }
        data.imageUri?.let {
            image.setImageURI(it)
        }

        artist.text = data.artist
        title.text = data.musicTitle
        duration.text = convertTime(data.duration)
        size.text = "${data.size} bits"
        date.text = convertDate( data.date.toLong())

    }

    private fun convertTime(time :Long) :String{
        val minutes: Int = time.toInt() / (60 * 1000)
        val seconds: Int = time.toInt() / 1000 % 60
        return  String.format("%d:%02d", minutes, seconds)
    }

    private fun convertDate(millisUntilFinished: Long) :String {
        var diff = millisUntilFinished
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val elapsedDays = diff / daysInMilli
        diff %= daysInMilli

        val elapsedHours = diff / hoursInMilli
        diff %= hoursInMilli

        val elapsedMinutes = diff / minutesInMilli
        diff %= minutesInMilli

        val elapsedSeconds = diff / secondsInMilli

       return "$elapsedDays days ago"

    }
}