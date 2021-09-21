package com.example.pokeapp.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokeapp.R
import com.example.pokeapp.domain.Pokemon
import com.example.pokeapp.formatPokemonName
import kotlinx.android.synthetic.main.evolution_item.view.*

class EvolutionRecyclerAdapter(private var evolutionList: List<Pokemon>) :
    RecyclerView.Adapter<EvolutionRecyclerAdapter.EvolutionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EvolutionViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.evolution_item, parent,
            false
        )
        return EvolutionViewHolder(layout)
    }

    override fun onBindViewHolder(holder: EvolutionViewHolder, position: Int) {
        return holder.bind(evolutionList[position])
    }

    override fun getItemCount(): Int = evolutionList.size

    fun updateList(data: List<Pokemon>) {
        evolutionList = data
    }

    class EvolutionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(pokemon: Pokemon) {
            itemView.text_name.formatPokemonName(pokemon.name)
            pokemon.imgUrl?.let {
                Glide.with(itemView.context)
                    .load(it)
                    .into(itemView.image_pokemon)
            }
        }
    }
}