package uz.gita.musicplayerstyle.repository

import uz.gita.musicplayerstyle.model.dao.FavouriteDao
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity
import javax.inject.Inject

class PlayScreenRepository @Inject constructor(private val dao: FavouriteDao) {
    fun addFavourite (data : FavouriteEntity) = dao.insert(data)
    fun getAllFavouriteMusic() = dao.getAllFavouriteMusics()
    fun deleteFavouriteMusic(data:FavouriteEntity) = dao.delete(data)


}