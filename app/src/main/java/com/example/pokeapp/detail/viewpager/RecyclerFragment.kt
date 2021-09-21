package com.example.pokeapp.detail.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pokeapp.R
import com.example.pokeapp.detail.EvolutionRecyclerAdapter
import com.example.pokeapp.domain.Pokemon

class RecyclerFragment(private val data: List<Pokemon>) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.evolution_recycler, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById(R.id.evolution_recycler) as RecyclerView
        recyclerView.adapter = EvolutionRecyclerAdapter(data)
    }
}