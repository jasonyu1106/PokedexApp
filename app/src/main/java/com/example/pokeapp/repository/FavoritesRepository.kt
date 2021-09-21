package com.example.pokeapp.repository

import androidx.paging.PagingSource
import com.example.pokeapp.database.FavoriteEntry
import com.example.pokeapp.database.FavoritesDao

class FavoritesRepository(private val database: FavoritesDao) {
    suspend fun addFavoriteEntry(pokemon: FavoriteEntry) {
        database.insert(pokemon)
    }

    suspend fun removeFavoriteEntry(pokemon: FavoriteEntry) {
        database.remove(pokemon)
    }

    fun getFavoritesList(): PagingSource<Int, FavoriteEntry> {
        return database.getFavorites()
    }

    suspend fun isFavorite(id: Int): Boolean {
        return database.getCountById(id) > 0
    }

}