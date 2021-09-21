package com.example.pokeapp.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pokeapp.repository.OverviewRepository

class OverviewViewModelFactory(
    private val overviewRepository: OverviewRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            return OverviewViewModel(overviewRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}