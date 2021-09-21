package com.example.pokeapp.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

interface PokemonInfo : Parcelable {
    val name: String
    var id: Int
    var imgUrl: String?
}

/**
 * Domain model for Pokemon in overview.
 * Name is set in first query, Other properties set in second query
 */
@Parcelize
data class Pokemon(
    override val name: String,
    override var id: Int = 0,
    override var imgUrl: String? = null
) : PokemonInfo

/**
 * Domain model for Pokemon details
 */
@Parcelize
data class PokemonDetails(
    override val name: String,
    override var id: Int,
    override var imgUrl: String? = null,
    val height: Int,
    val weight: Int,
    val type: List<String> = listOf(),
    val abilities: List<String> = listOf(),
    val moveset: List<String> = listOf(),
) : PokemonInfo {
    var stats = PokemonStats()
}

data class PokemonStats(
    val hp: Int = 0,
    val attack: Int = 0,
    val defense: Int = 0,
    val spAttack: Int = 0,
    val spDefense: Int = 0,
    val speed: Int = 0
)

@Parcelize
data class PokemonEvolution(
    override val name: String,
    override var id: Int = 0,
    override var imgUrl: String?,
    val isPreEvolution: Boolean
) : PokemonInfo

enum class PokemonType(val text: String) {
    NORMAL("normal"),
    FIGHTING("fighting"),
    FLYING("flying"),
    POISON("poison"),
    GROUND("ground"),
    ROCK("rock"),
    BUG("bug"),
    GHOST("ghost"),
    STEEL("steel"),
    FIRE("fire"),
    WATER("water"),
    GRASS("grass"),
    ELECTRIC("electric"),
    PSYCHIC("psychic"),
    ICE("ice"),
    DRAGON("dragon"),
    DARK("dark"),
    FAIRY("fairy")
}