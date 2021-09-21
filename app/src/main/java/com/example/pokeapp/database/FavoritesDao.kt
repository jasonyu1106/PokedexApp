package com.example.pokeapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pokemon: FavoriteEntry)

    @Delete
    suspend fun remove(pokemon: FavoriteEntry)

    @Query("SELECT * from favorite_pokemon_table")
    fun getFavorites(): PagingSource<Int, FavoriteEntry>

    @Query("SELECT * from favorite_pokemon_table ORDER BY id ASC")
    fun getFavoritesById(): PagingSource<Int, FavoriteEntry>

    @Query("SELECT COUNT() from favorite_pokemon_table WHERE id = :id")
    suspend fun getCountById(id: Int): Int
}