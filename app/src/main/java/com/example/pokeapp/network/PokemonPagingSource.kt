package com.example.pokeapp.network

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.pokeapp.domain.Pokemon
import com.example.pokeapp.domain.PokemonType

class PokemonPagingSource(
    private val service: PokeApiService,
    private val typeFilter: Set<PokemonType>
) : PagingSource<Int, Pokemon>() {

    private var typeFilterResult: List<String> = listOf()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        return try {
            val pageNumber = params.key ?: 0
            var nextPageKey: Int? = pageNumber + 1

            val pokemonNameList: List<String> = if (typeFilter.isEmpty()) {
                Log.i("PagingService", "pageNumber: $pageNumber, loadSize: ${params.loadSize}")
                val response =
                    service.getPokemonList(params.loadSize, params.loadSize * pageNumber)

                /** End of pagination reached indicated by API */
                if (response.next == null) {
                    nextPageKey = null
                }

                response.asNameList()
            } else {
                /** Initial load with filter */
                if (typeFilterResult.isEmpty()) {
                    typeFilterResult = fetchFilteredResult()
                }

                val startIndex = pageNumber * params.loadSize
                var endIndex = startIndex + params.loadSize - 1

                /** End of pagination reached indicated by API */
                if (typeFilterResult.size <= endIndex + 1) {
                    nextPageKey = null
                    if (typeFilterResult.size < endIndex + 1) {
                        endIndex = typeFilterResult.size - 1
                    }
                }

                typeFilterResult.slice(startIndex..endIndex)
            }

            val pokemonListData: MutableList<Pokemon> = mutableListOf()

            pokemonNameList.forEach { name ->
                val pokemonData = service.getPokemonListData(name)
                pokemonListData.add(
                    Pokemon(
                        pokemonData.name,
                        pokemonData.id,
                        pokemonData.spriteContainer.spriteGroups.spriteType.spriteUrl
                            ?: pokemonData.spriteContainer.secondarySpriteUrl
                    )
                )
            }

            LoadResult.Page(
                data = pokemonListData,
                prevKey = null,
                nextKey = nextPageKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    /** Return list of pokemon names that intersect with each filter result */
    private suspend fun fetchFilteredResult(): List<String> {
        var filterNameResult: List<String> = listOf()
        typeFilter.forEachIndexed { index, type ->
            filterNameResult = when {
                index == 0 -> {
                    service.getPokemonListFromType(type.text).asNameList()
                }
                filterNameResult.isNotEmpty() -> {
                    filterNameResult.intersect(
                        service.getPokemonListFromType(type.text).asNameList()
                    ).toList()
                }
                else -> {
                    return filterNameResult
                }
            }
        }
        return filterNameResult
    }
}