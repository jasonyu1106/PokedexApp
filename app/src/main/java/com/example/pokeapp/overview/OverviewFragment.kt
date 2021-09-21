package com.example.pokeapp.overview

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.example.pokeapp.OnClickListener
import com.example.pokeapp.PagerLoadStateAdapter
import com.example.pokeapp.PokemonAdapter
import com.example.pokeapp.R
import com.example.pokeapp.databinding.FragmentOverviewBinding
import com.example.pokeapp.domain.PokemonType
import com.example.pokeapp.network.PokeApi
import com.example.pokeapp.repository.OverviewRepository
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverviewFragment : Fragment() {

    private lateinit var viewModel: OverviewViewModel
    private lateinit var viewModelFactory: OverviewViewModelFactory
    private lateinit var binding: FragmentOverviewBinding
    private lateinit var adapter: PokemonAdapter
    private lateinit var actionsMenu: Menu

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOverviewBinding.inflate(inflater)

        viewModelFactory = OverviewViewModelFactory(OverviewRepository(PokeApi.retrofitService))
        viewModel = ViewModelProvider(this, viewModelFactory).get(OverviewViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupList()
        handleLoadStates(adapter)

        viewModel.retryEvent.observe(viewLifecycleOwner, { isRetryEvent ->
            if (isRetryEvent) {
                adapter.retry()
                viewModel.retryEventComplete()
            }
        })

        setHasOptionsMenu(true)
        setupChips()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        actionsMenu = menu
        inflater.inflate(R.menu.actions_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                binding.filterMenu.visibility = when (binding.filterMenu.visibility) {
                    View.VISIBLE -> View.GONE
                    else -> View.VISIBLE
                }
            }
            R.id.action_clear -> {
                binding.chipGroup.clearCheck()
                viewModel.updateFilter(setOf())
                val clearAction = actionsMenu.findItem(R.id.action_clear)
                clearAction.isVisible = false
                clearAction.isEnabled = false
            }
        }
        return true
    }

    private fun handleLoadStates(adapter: PokemonAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                        Log.i("OverviewFragment", "Pager Load State: Loading")
                        render(OverviewState.LoadingState)
                    }
                    is LoadState.Error -> {
                        Log.i("OverviewFragment", "Pager Load State: Error")
                        val message = (loadStates.refresh as LoadState.Error).error.toString()
                        render(OverviewState.ErrorState(message))
                    }
                    is LoadState.NotLoading -> {
                        Log.i("OverviewFragment", "Pager Load State: Not Loading")

                        if (loadStates.append.endOfPaginationReached && adapter.itemCount < 1) {
                            Log.i("OverviewFragment", "No results found")
                            render(OverviewState.NoResultsState)
                        } else {
                            Log.i("OverviewFragment", "Results found")
                            render(OverviewState.ResultsState)
                        }
                    }
                }
            }
        }
    }

    private fun render(state: OverviewState) {
        when (state) {
            is OverviewState.LoadingState -> {
                Log.i("OverviewFragment", "Loading State detected")
                binding.progressIndicator.visibility = View.VISIBLE
                binding.noResultsPage.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
            }
            is OverviewState.ErrorState -> {
                binding.textError.text = state.error
                Log.i("OverviewFragment", "Error State detected")
                binding.layoutError.visibility = View.VISIBLE
                binding.noResultsPage.visibility = View.GONE
                binding.progressIndicator.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
            }
            is OverviewState.ResultsState -> {
                Log.i("OverviewFragment", "Results State detected")
                binding.recyclerView.visibility = View.VISIBLE
                binding.noResultsPage.visibility = View.GONE
                binding.progressIndicator.visibility = View.GONE
            }
            is OverviewState.NoResultsState -> {
                Log.i("OverviewFragment", "No Results State detected")
                binding.noResultsPage.visibility = View.VISIBLE
                binding.progressIndicator.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE

            }
        }
    }

    private fun addLoadStateAdapter(adapter: PokemonAdapter): ConcatAdapter {
        return adapter.withLoadStateFooter(footer = PagerLoadStateAdapter { adapter.retry() })
    }

    private fun setupList() {
        adapter = PokemonAdapter(OnClickListener { pokemon ->
            try {
                findNavController().navigate(
                    OverviewFragmentDirections.actionOverviewFragmentToDetailFragment(
                        pokemon.name
                    )
                )
            } catch (e: IllegalArgumentException) {
                Log.e("OverviewFragment", "Multiple navigation attempts registered")
            }
        })

        binding.recyclerView.adapter = addLoadStateAdapter(adapter)

        viewModel.getPokemonData().let {
            it.observe(viewLifecycleOwner, { pagingData ->
                adapter.submitData(lifecycle, pagingData)
            })
        }
    }

    /** Add a click listener to each chip, such that the updated filter is detected */
    private fun setupChips() {
        val chipGroup = binding.chipGroup
        for (i in 0 until chipGroup.childCount) {
            var chip = chipGroup.getChildAt(i) as Chip
            chip.setOnClickListener {
                val widthScreen = context?.resources?.displayMetrics?.widthPixels
                binding.filterMenu.post {
                    if (widthScreen != null) {
                        binding.filterMenu.smoothScrollTo(
                            chip.left - widthScreen / 2 + chip.width / 2,
                            0
                        )
                    }
                }

                val filterSet = mutableSetOf<PokemonType>()
                chipGroup.checkedChipIds.forEach { chipId ->
                    filterSet.add(
                        when (chipId) {
                            binding.chipNormal.id -> PokemonType.NORMAL
                            binding.chipFighting.id -> PokemonType.FIGHTING
                            binding.chipFlying.id -> PokemonType.FLYING
                            binding.chipPoison.id -> PokemonType.POISON
                            binding.chipGround.id -> PokemonType.GROUND
                            binding.chipRock.id -> PokemonType.ROCK
                            binding.chipBug.id -> PokemonType.BUG
                            binding.chipGhost.id -> PokemonType.GHOST
                            binding.chipSteel.id -> PokemonType.STEEL
                            binding.chipFire.id -> PokemonType.FIRE
                            binding.chipWater.id -> PokemonType.WATER
                            binding.chipGrass.id -> PokemonType.GRASS
                            binding.chipElectric.id -> PokemonType.ELECTRIC
                            binding.chipPsychic.id -> PokemonType.PSYCHIC
                            binding.chipIce.id -> PokemonType.ICE
                            binding.chipDragon.id -> PokemonType.DRAGON
                            binding.chipDark.id -> PokemonType.DARK
                            binding.chipFairy.id -> PokemonType.FAIRY
                            else -> throw IllegalArgumentException("Unknown chip filter")
                        }
                    )
                }
                viewModel.updateFilter(filterSet)

                val clearAction = actionsMenu.findItem(R.id.action_clear)
                if (filterSet.isEmpty()) {
                    clearAction.isVisible = false
                    clearAction.isEnabled = false
                } else {
                    clearAction.isVisible = true
                    clearAction.isEnabled = true
                }
            }
        }
    }
}