package uz.gita.musicplayerstyle.viewmodels

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import uz.gita.musicplayerstyle.repository.FavouriteRepository
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(private val repository: FavouriteRepository):ViewModel() {
    private  val _getAllFavouriteLivaData = MutableLiveData<List<FavouriteEntity>>()
    val getAllFavouriteLivaData : LiveData<List<FavouriteEntity>> get() = _getAllFavouriteLivaData

    private  val _deleteFavouriteLivaData = MutableLiveData<Unit>()
    val deleteFavouriteLivaData : LiveData<Unit> get() = _deleteFavouriteLivaData

    fun getAllFavourite() : List<FavouriteEntity>{
        _getAllFavouriteLivaData.value = repository.getAllFavouriteMusic()
        return repository.getAllFavouriteMusic()
    }

    fun deleteFavourite(data:FavouriteEntity){
         repository.deleteFavouriteMusic(data)
    }


}