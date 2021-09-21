package com.example.pokeapp.favorites

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.pokeapp.database.FavoritesDatabase
import com.example.pokeapp.databinding.FragmentFavoritesBinding
import com.example.pokeapp.repository.FavoritesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: FavoritesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFavoritesBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        val databaseSource =
            FavoritesDatabase.getInstance(requireNotNull(activity).applicationContext).favoritesDao
        val viewModelFactory = FavoritesViewModelFactory(FavoritesRepository(databaseSource))
        viewModel = ViewModelProvider(this, viewModelFactory).get(FavoritesViewModel::class.java)

        val pagingAdapter = FavoritesAdapter(OnFavoriteClickListener {
            try {
                findNavController().navigate(
                    FavoritesFragmentDirections.actionFavoritesFragmentToDetailFragment(it.name)
                )
            } catch (e: IllegalArgumentException) {
                Log.e("FavoritesFragment", "Multiple navigation attempts registered")
            }
        })
        binding.recyclerViewFavorites.adapter = pagingAdapter

        lifecycleScope.launch {
            viewModel.favoritePagingFlow.collectLatest {
                pagingAdapter.submitData(it)
            }
        }

        return binding.root
    }
}