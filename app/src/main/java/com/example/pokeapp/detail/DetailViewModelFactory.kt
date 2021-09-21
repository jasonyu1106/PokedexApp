package com.example.pokeapp.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokeapp.repository.DetailRepository
import com.example.pokeapp.repository.FavoritesRepository

class DetailViewModelFactory(
    private val name: String,
    private val detailRepository: DetailRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(name, detailRepository, favoritesRepository) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}