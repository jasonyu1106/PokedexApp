package com.example.pokeapp.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pokeapp.database.FavoriteEntry
import com.example.pokeapp.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class FavoritesViewModel(private val favoritesRepository: FavoritesRepository) : ViewModel() {

    private lateinit var _favoritesPagingFlow: Flow<PagingData<FavoriteEntry>>
    val favoritePagingFlow: Flow<PagingData<FavoriteEntry>>
        get() = _favoritesPagingFlow

    init {
        setupPagingData()
    }

    private fun setupPagingData() {
        _favoritesPagingFlow = Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { favoritesRepository.getFavoritesList() }
        ).flow.cachedIn(viewModelScope)
    }

}