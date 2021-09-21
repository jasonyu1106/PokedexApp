package com.example.pokeapp.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.pokeapp.domain.Pokemon
import com.example.pokeapp.domain.PokemonType
import com.example.pokeapp.network.PokeApiService
import com.example.pokeapp.network.PokemonPagingSource

class OverviewRepository(private val pokeApiService: PokeApiService) {
    fun getPokemonListData(typeFilter: Set<PokemonType>): LiveData<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = { PokemonPagingSource(pokeApiService, typeFilter) }
        ).liveData
    }

    companion object {
        private const val PAGE_SIZE = 20
    }

}