package com.example.pokeapp.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

private const val BASE_URL = "https://pokeapi.co/api/v2/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") count: Int,
        @Query("offset") offset: Int
    ): NetworkPokemonResultContainer

    @GET("pokemon/{name}")
    suspend fun getPokemonListData(@Path("name") name: String): NetworkPokemonListData

    @GET("pokemon/{name}")
    suspend fun getPokemonDetails(@Path("name") name: String): NetworkPokemonDetails

    @GET("type/{typeName}")
    suspend fun getPokemonListFromType(@Path("typeName") type: String): NetworkTypeContainer

    @GET("pokemon-species/{name}")
    fun getEvolutionChainUrl(@Path("name") name: String): Single<NetworkSpeciesContainer>

    @GET
    fun getEvolutionChainObject(@Url evolutionChainUrl: String): Single<NetworkEvolutionChain>

    @GET("pokemon/{name}")
    fun getPokemonInfoSingle(@Path("name") name: String): Observable<NetworkPokemonListData>
}

object PokeApi {
    val retrofitService: PokeApiService by lazy {
        retrofit.create(PokeApiService::class.java)
    }
}




