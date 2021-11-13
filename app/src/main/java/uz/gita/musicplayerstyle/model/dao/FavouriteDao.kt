package uz.gita.musicplayerstyle.model.dao

import androidx.room.*
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity

@Dao
interface FavouriteDao {

    @Query("SELECT * FROM FavouriteEntity")
    fun getAllFavouriteMusics () :List <FavouriteEntity>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(data : FavouriteEntity)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update (data: FavouriteEntity)

    @Delete
    fun delete (data: FavouriteEntity)
}