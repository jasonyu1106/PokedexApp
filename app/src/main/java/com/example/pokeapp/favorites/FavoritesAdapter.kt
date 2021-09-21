package com.example.pokeapp.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pokeapp.database.FavoriteEntry
import com.example.pokeapp.databinding.FavoritesRecyclerItemBinding

class FavoritesAdapter(private val onClickListener: OnFavoriteClickListener) :
    PagingDataAdapter<FavoriteEntry, FavoritesAdapter.ViewHolder>(FavoriteDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FavoritesRecyclerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onClickListener)
    }

    class ViewHolder(private val binding: FavoritesRecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FavoriteEntry?, onClick: OnFavoriteClickListener) {
            binding.pokemon = item
            binding.onClickListener = onClick
            binding.executePendingBindings()
        }
    }
}

class FavoriteDiffCallback : DiffUtil.ItemCallback<FavoriteEntry>() {
    override fun areItemsTheSame(oldItem: FavoriteEntry, newItem: FavoriteEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FavoriteEntry, newItem: FavoriteEntry): Boolean {
        return oldItem == newItem
    }
}

class OnFavoriteClickListener(val clickListener: (pokemon: FavoriteEntry) -> Unit) {
    fun onClick(pokemon: FavoriteEntry) = clickListener(pokemon)
}