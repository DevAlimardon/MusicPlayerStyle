package uz.gita.musicplayerstyle.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.gita.musicplayerstyle.app.App
import uz.gita.musicplayerstyle.model.database.MusicDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun database(@ApplicationContext app : Context) =
        Room.databaseBuilder(App.instance, MusicDatabase::class.java, "musicDatabase")
        .allowMainThreadQueries()
        .build()
    @Provides
    @Singleton
    fun dbDao(db : MusicDatabase) = db.getFavouriteDao()
}