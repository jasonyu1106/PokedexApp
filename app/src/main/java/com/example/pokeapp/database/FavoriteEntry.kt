package com.example.pokeapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_pokemon_table")
data class FavoriteEntry(
    @PrimaryKey
    val id: Int = -1,
    val name: String,
    val imgUrl: String?,
)