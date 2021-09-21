package com.example.pokeapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pokeapp.databinding.RecyclerItemBinding
import com.example.pokeapp.domain.Pokemon

class PokemonAdapter(private val onClickListener: OnClickListener) :
    PagingDataAdapter<Pokemon, PokemonAdapter.ViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClickListener)
    }

    class ViewHolder(private val binding: RecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pokemon?, clickListener: OnClickListener) {
            binding.pokemon = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }
}

class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}

class OnClickListener(val clickListener: (pokemon: Pokemon) -> Unit) {
    fun onClick(pokemon: Pokemon) = clickListener(pokemon)
}