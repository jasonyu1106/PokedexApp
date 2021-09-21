package com.example.pokeapp.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokeapp.database.FavoriteEntry
import com.example.pokeapp.domain.Pokemon
import com.example.pokeapp.domain.PokemonDetails
import com.example.pokeapp.repository.DetailRepository
import com.example.pokeapp.repository.FavoritesRepository
import kotlinx.coroutines.launch

class DetailViewModel(
    private val pokemonName: String,
    private val detailRepository: DetailRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _pokemonDetails = MutableLiveData<PokemonDetails>()
    val pokemonDetails: LiveData<PokemonDetails>
        get() = _pokemonDetails

    private val _evolutionDataUpdated = MutableLiveData<Boolean>()
    val evolutionDataUpdated: LiveData<Boolean>
        get() = _evolutionDataUpdated

    var evolveFromList: List<Pokemon> = listOf()
        private set
    var evolveToList: List<Pokemon> = listOf()
        private set

    private val _isAlreadyFavorite = MutableLiveData<Boolean>()
    val isAlreadyFavorite: LiveData<Boolean>
        get() = _isAlreadyFavorite

    init {
        fetchPokemonDetails()
        fetchPokemonEvolution()
    }

    private fun fetchPokemonDetails() {
        viewModelScope.launch {
            try {
                val details = detailRepository.getPokemonProfile(pokemonName)
                _pokemonDetails.postValue(details)
                checkIfFavorite(details.id)
                Log.i("DetailViewModel", "Successful detail request")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Failed detail request: ${e.message}")
            }
        }
    }

    private fun fetchPokemonEvolution() {
        detailRepository.getEvolutionChain(pokemonName).subscribe(
            { evolutionData ->
                val evolveFrom = mutableListOf<Pokemon>()
                val evolveTo = mutableListOf<Pokemon>()
                evolutionData.forEach {
                    if (it.isPreEvolution) {
                        evolveFrom.add(Pokemon(it.name, it.id, it.imgUrl))
                    } else {
                        evolveTo.add(Pokemon(it.name, it.id, it.imgUrl))
                    }
                }
                evolveFromList = evolveFrom
                evolveToList = evolveTo
                _evolutionDataUpdated.value = true
            },
            { throwable ->
                Log.e("DetailViewModel", throwable.message ?: "onErrorEvolutionChain")
            })
    }

    fun addToFavorites() {
        _pokemonDetails.value?.let { pokemon ->
            viewModelScope.launch {
                favoritesRepository.addFavoriteEntry(
                    FavoriteEntry(
                        pokemon.id, pokemon.name, pokemon.imgUrl
                    )
                )
                Log.i("DetailVM", "Favorite Added")
            }
        }
    }

    fun removeFromFavorites() {
        _pokemonDetails.value?.let { pokemon ->
            viewModelScope.launch {
                favoritesRepository.removeFavoriteEntry(
                    FavoriteEntry(
                        pokemon.id, pokemon.name, pokemon.imgUrl
                    )
                )
                Log.i("DetailVM", "Favorite Removed")
            }
        }
    }

    private fun checkIfFavorite(id: Int) {
        viewModelScope.launch {
            _isAlreadyFavorite.postValue(
                favoritesRepository.isFavorite(id)
            )
        }
    }
}