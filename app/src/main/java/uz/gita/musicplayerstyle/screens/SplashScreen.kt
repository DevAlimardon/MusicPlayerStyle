package uz.gita.musicplayerstyle.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.musicplayerstyle.R
import uz.gita.musicplayerstyle.model.EventMusic
import uz.gita.musicplayerstyle.utils.checkPermissions
import uz.gita.musicplayerstyle.utils.getMusicListCursor
import java.util.jar.Manifest

class SplashScreen :Fragment(R.layout.screen_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().checkPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requireContext().getMusicListCursor().onEach {
                EventMusic.cursor = it
               findNavController().navigate(R.id.action_splashScreen_to_mainScreen)
            }.launchIn(lifecycleScope)
        }
    }
}