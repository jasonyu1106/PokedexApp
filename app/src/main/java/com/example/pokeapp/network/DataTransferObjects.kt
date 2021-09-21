package com.example.pokeapp.network

import com.example.pokeapp.domain.PokemonDetails
import com.example.pokeapp.domain.PokemonEvolution
import com.example.pokeapp.domain.PokemonStats
import com.squareup.moshi.Json
import io.reactivex.rxjava3.core.Observable

/**
 * Container for NamedAPIResourceList type from PokeApi
 */
data class NetworkPokemonResultContainer(
    val next: String?,
    val results: List<NetworkPokemonResult>
)

fun NetworkPokemonResultContainer.asNameList(): List<String> {
    return results.map {
        it.name
    }
}

/**
 * Holder for NamedAPIResource type from PokeApi
 */
data class NetworkPokemonResult(val name: String)

/**
 * Holder for APIResource type from PokeApi
 */
data class NetworkAPIResource(val url: String)

/**
 * Holder for Pokemon data displayed in overview list from PokeApi
 */
data class NetworkPokemonListData(
    val name: String,
    val id: Int,
    @Json(name = "sprites") val spriteContainer: NetworkSpriteCategories
)

data class NetworkSpriteCategories(
    @Json(name = "front_default") val secondarySpriteUrl: String?,
    @Json(name = "other") val spriteGroups: NetworkSpriteGroups
)

data class NetworkSpriteGroups(
    @Json(name = "official-artwork") val spriteType: NetworkSpriteTypes
)

data class NetworkSpriteTypes(
    @Json(name = "front_default") val spriteUrl: String?
)

/**
 * Holder for Pokemon detail data from PokeApi
 */
data class NetworkPokemonDetails(
    val name: String,
    val id: Int,
    val height: Int,
    val weight: Int,
    @Json(name = "sprites") val spriteContainer: NetworkSpriteCategories,
    @Json(name = "types") val typesContainer: List<NetworkPokemonType>,
    @Json(name = "stats") val statsContainer: List<NetworkStatsType>,
    val abilities: List<NetworkAbilityType>,
    @Json(name = "moves") val movesContainer: List<NetworkPokemonMove>
)

data class NetworkPokemonType(
    @Json(name = "type") val typeHolder: NetworkPokemonResult
)

data class NetworkPokemonMove(
    @Json(name = "move") val moveHolder: NetworkPokemonResult
)

data class NetworkStatsType(
    @Json(name = "base_stat") val statValue: Int,
    @Json(name = "stat") val statsHolder: NetworkPokemonResult
)

data class NetworkAbilityType(
    val ability: NetworkPokemonResult
)

fun NetworkPokemonDetails.asDomainModel(): PokemonDetails {
    val details = PokemonDetails(
        name,
        id,
        spriteContainer.spriteGroups.spriteType.spriteUrl ?: spriteContainer.secondarySpriteUrl,
        height,
        weight,
        typesContainer.map {
            it.typeHolder.name
        },
        abilities.map {
            it.ability.name
        },
        movesContainer.map {
            it.moveHolder.name
        }
    )

    val statsMap = hashMapOf<String, Int>()
    statsContainer.forEach {
        statsMap[it.statsHolder.name] = it.statValue
    }

    details.stats = PokemonStats(
        statsMap["hp"] ?: 0,
        statsMap["attack"] ?: 0,
        statsMap["defense"] ?: 0,
        statsMap["special-attack"] ?: 0,
        statsMap["special-defense"] ?: 0,
        statsMap["speed"] ?: 0
    )
    return details
}

/**
 * Holder for Pokemon names by type from PokeApi
 */
data class NetworkTypeContainer(
    @Json(name = "pokemon") val list: List<NetworkTypePokemon>
)

fun NetworkTypeContainer.asNameList(): List<String> {
    return list.map {
        it.pokemon.name
    }
}

data class NetworkTypePokemon(val pokemon: NetworkPokemonResult)

/**
 * Holder for Pokemon evolution chain endpoint url by species from PokeApi
 */
data class NetworkSpeciesContainer(
    @Json(name = "evolution_chain") val chainUrlContainer: NetworkAPIResource
)

/**
 * Holder for Pokemon evolution chain from PokeApi
 */
data class NetworkEvolutionChain(
    val id: Int,
    val chain: NetworkChainLink
) {
    data class NetworkChainLink(
        val species: NetworkPokemonResult,
        @Json(name = "is_baby") val isBaby: Boolean,
        @Json(name = "evolves_to") val evolvesTo: List<NetworkChainLink>
    )
}

/**
 * Parse species evolution chain object (implemented as a tree) and return pre-evolution and
 * evolution pokemon
 *
 * Parameters: pokemon: Name of pokemon
 *
 * Return:
 * PokemonEvolution Single object containing two lists that represent the immediate pre-evolutions
 * and evolutions of the pokemon. If the pokemon has no pre-evolution or evolution,
 * the list will be empty.
 */
fun NetworkEvolutionChain.asDomainModel(pokemon: String): Observable<PokemonEvolution> {
    val evolutionList: MutableList<PokemonEvolution> = mutableListOf()

    /** Stack to store evolution object for depth traversing tree */
    val stack: MutableList<NetworkEvolutionChain.NetworkChainLink> = mutableListOf()
    stack.add(chain)

    while (stack.isNotEmpty()) {
        val head = stack.removeLast()

        if (head.species.name == pokemon && head.evolvesTo.isNotEmpty()) {
            head.evolvesTo.forEach {
                evolutionList.add(
                    PokemonEvolution(
                        name = it.species.name,
                        imgUrl = null,
                        isPreEvolution = false
                    )
                )
            }
        }
        head.evolvesTo.forEach {
            stack.add(it)
            if (it.species.name == pokemon) {
                evolutionList.add(
                    PokemonEvolution(
                        name = head.species.name,
                        imgUrl = null,
                        isPreEvolution = true
                    )
                )
            }
        }
    }
    return Observable.fromIterable(evolutionList)
}