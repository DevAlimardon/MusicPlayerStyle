package uz.gita.musicplayerstyle.viewmodels

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.musicplayerstyle.repository.Repository
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel@Inject constructor(private val repository : Repository) : ViewModel() {

    private var _cursorLivedata = MutableLiveData <Cursor>()
     val cursorLiveData :LiveData<Cursor?> get() = _cursorLivedata



    fun getCursorList() {
        repository.listCursor().onEach {
            _cursorLivedata.value = it
        }.launchIn(viewModelScope)
    }

}