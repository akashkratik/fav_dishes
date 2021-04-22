package com.example.dishes.application

import android.app.Application
import com.example.dishes.model.database.FavDishRepository
import com.example.dishes.model.database.FavDishRoomDatabase

class FavDishApplication: Application() {

    private val database by lazy { FavDishRoomDatabase.getDatabase((this@FavDishApplication))}

    val repository by lazy { FavDishRepository(database.favDishDao())}



}