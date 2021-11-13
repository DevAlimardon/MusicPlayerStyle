package uz.gita.musicplayerstyle.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import uz.gita.musicplayerstyle.repository.PlayScreenRepository
import javax.inject.Inject

@HiltViewModel
class PlayScreenViewModel @Inject constructor(private val repository: PlayScreenRepository) :ViewModel() {
    private  val _addFavouriteLivaData = MutableLiveData<Unit>()
    val addFavouriteLivaData : LiveData<Unit> get() = _addFavouriteLivaData

    fun addFavourite ( data : FavouriteEntity){
        _addFavouriteLivaData.value = repository.addFavourite(data)
    }

    fun getAllFavourite() : List<FavouriteEntity>{
        return repository.getAllFavouriteMusic()
    }
    fun deleteFavourite(data:FavouriteEntity){
        repository.deleteFavouriteMusic(data)
    }

}