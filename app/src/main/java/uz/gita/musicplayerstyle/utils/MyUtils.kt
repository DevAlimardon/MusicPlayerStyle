package uz.gita.musicplayerstyle.utils

import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import timber.log.Timber
import uz.gita.musicplayerstyle.model.EventMusic

fun Fragment.showToast(message : String) {
    Toast.makeText(this.requireContext(),message, Toast.LENGTH_SHORT).show()
}

fun <T : ViewBinding> T.scope(block : T.() -> Unit) {
    block(this)
}

fun timber(message : String, tag : String = "TTT"){
    Timber.tag(tag).d(message)
}

fun SeekBar.setOnChangeListener(block : (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        private var _progress = 0

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            _progress = progress
            EventMusic.progressMusicLiveData.value = progress.toLong()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            block(_progress)
            EventMusic.currentTime = _progress * 1L
        }
    })
}