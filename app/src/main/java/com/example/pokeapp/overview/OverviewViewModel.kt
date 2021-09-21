package com.example.pokeapp.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.pokeapp.domain.Pokemon
import com.example.pokeapp.domain.PokemonType
import com.example.pokeapp.repository.OverviewRepository

class OverviewViewModel(private val overviewRepository: OverviewRepository) :
    ViewModel() {

    private val _retryEvent = MutableLiveData<Boolean>()
    val retryEvent: LiveData<Boolean>
        get() = _retryEvent

    private val typeFilter = MutableLiveData<Set<PokemonType>>(mutableSetOf())

    private var pagingData: LiveData<PagingData<Pokemon>>

    init {
        pagingData = Transformations.switchMap(typeFilter) { filter ->
            overviewRepository.getPokemonListData(filter).cachedIn(viewModelScope)
        }
    }

    fun getPokemonData(): LiveData<PagingData<Pokemon>> {
        return pagingData
    }

    fun updateFilter(types: Set<PokemonType>) {
        typeFilter.value = types
    }

    fun requestPagerRetry() {
        _retryEvent.value = true
    }

    fun retryEventComplete() {
        _retryEvent.value = false
    }

}