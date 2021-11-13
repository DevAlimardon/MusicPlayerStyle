package uz.gita.musicplayerstyle.shared

import android.content.Context
import uz.gita.musicplayerstyle.app.App


class SharedPref private constructor(){
    private val pref = App.instance.getSharedPreferences("PlayMusic", Context.MODE_PRIVATE)
    companion object{
      private lateinit var instance :SharedPref

      fun getShared(): SharedPref{
          if (!::instance.isInitialized){
              instance = SharedPref()
          }
          return instance
      }
    }

    var isNew : Int
    get() = pref.getInt("isNew", 0)
    set(value) = pref.edit().putInt("isNew", value).apply()

    var title :String?
    get() = pref.getString("title", "")
    set(value) = pref.edit().putString("title", value).apply()

    var artist :String?
        get() = pref.getString("artist", "")
        set(value) = pref.edit().putString("artist", value).apply()

    var image :String?
        get() = pref.getString("image", "")
        set(value) = pref.edit().putString("image", value).apply()

    var music :String?
        get() = pref.getString("music", "")
        set(value) = pref.edit().putString("music", value).apply()

    var firstTime :Boolean
        get() = pref.getBoolean("firstTime", true)
        set(value) = pref.edit().putBoolean("firstTime", value).apply()

    var firstTimeFavourite :Boolean
        get() = pref.getBoolean("firstTimeFavourite", true)
        set(value) = pref.edit().putBoolean("firstTimeFavourite", value).apply()

    var isfavourite :Boolean
        get() = pref.getBoolean("isfavourite", false)
        set(value) = pref.edit().putBoolean("isfavourite", value).apply()






}