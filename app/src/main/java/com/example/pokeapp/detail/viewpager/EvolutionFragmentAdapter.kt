package com.example.pokeapp.detail.viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pokeapp.domain.Pokemon

class EvolutionFragmentAdapter(
    fragment: Fragment,
    private val evolveFrom: List<Pokemon>,
    private val evolveTo: List<Pokemon>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return if (evolveFrom.isEmpty() || evolveTo.isEmpty()) {
            1
        } else {
            2
        }
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0 && evolveFrom.isNotEmpty()) {
            RecyclerFragment(evolveFrom)
        } else
            RecyclerFragment(evolveTo)
    }
}