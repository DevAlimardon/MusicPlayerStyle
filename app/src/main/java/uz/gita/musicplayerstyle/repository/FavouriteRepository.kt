package uz.gita.musicplayerstyle.repository

import uz.gita.musicplayerstyle.model.dao.FavouriteDao
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import javax.inject.Inject

class FavouriteRepository @Inject constructor(private val dao: FavouriteDao) {
   fun getAllFavouriteMusic() = dao.getAllFavouriteMusics()
    fun deleteFavouriteMusic(data:FavouriteEntity) = dao.delete(data)


}