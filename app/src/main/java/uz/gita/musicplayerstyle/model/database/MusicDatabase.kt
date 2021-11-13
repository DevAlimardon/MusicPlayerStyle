package uz.gita.musicplayerstyle.model.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.model.dao.FavouriteDao
import uz.gita.musicplayerstyle.model.entity.FavouriteEntity

@Database(entities = [FavouriteEntity::class], version = 1)
abstract class MusicDatabase :RoomDatabase() {
    abstract fun getFavouriteDao() : FavouriteDao

    companion object{
        private lateinit var instance :MusicDatabase
        fun getDatabase():MusicDatabase{
            if (!::instance.isInitialized){
                instance = Room.databaseBuilder(App.instance, MusicDatabase::class.java, "musicDatabase")
                    .allowMainThreadQueries()
                    .build()
            }
            return instance
        }
    }
}