package com.example.pokeapp.repository

import com.example.pokeapp.domain.PokemonDetails
import com.example.pokeapp.domain.PokemonEvolution
import com.example.pokeapp.network.PokeApiService
import com.example.pokeapp.network.asDomainModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class DetailRepository(private val pokeApiService: PokeApiService) {
    suspend fun getPokemonProfile(pokemon: String): PokemonDetails {
        return pokeApiService.getPokemonDetails(pokemon).asDomainModel()
    }

    fun getEvolutionChain(pokemon: String): Single<List<PokemonEvolution>> {
        return pokeApiService.getEvolutionChainUrl(pokemon)
            .map { it.chainUrlContainer.url }
            .flatMap { url -> pokeApiService.getEvolutionChainObject(url) }
            .flatMapObservable { evolutionChain -> evolutionChain.asDomainModel(pokemon) }
            .flatMap { evolution ->
                pokeApiService.getPokemonInfoSingle(evolution.name)
                    .flatMap { response ->
                        Observable.just(
                            PokemonEvolution(
                                response.name, response.id, response.spriteContainer.spriteGroups
                                    .spriteType.spriteUrl ?: response.spriteContainer
                                    .secondarySpriteUrl, evolution.isPreEvolution
                            )
                        )
                    }
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}