package com.example.pokeapp.detail

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.pokeapp.R
import com.example.pokeapp.database.FavoritesDatabase
import com.example.pokeapp.databinding.FragmentBottomDialogBinding
import com.example.pokeapp.detail.viewpager.EvolutionFragmentAdapter
import com.example.pokeapp.domain.PokemonStats
import com.example.pokeapp.domain.PokemonType
import com.example.pokeapp.network.PokeApi
import com.example.pokeapp.repository.DetailRepository
import com.example.pokeapp.repository.FavoritesRepository
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator

const val AXIS_LABEL_LENGTH = 12

class DetailFragment : BottomSheetDialogFragment() {

    private val args: DetailFragmentArgs by navArgs()
    private lateinit var viewModel: DetailViewModel
    private lateinit var viewModelFactory: DetailViewModelFactory
    private lateinit var binding: FragmentBottomDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val databaseSource =
            FavoritesDatabase.getInstance(requireNotNull(activity).applicationContext).favoritesDao
        viewModelFactory =
            DetailViewModelFactory(
                args.pokemonName,
                DetailRepository(PokeApi.retrofitService),
                FavoritesRepository(databaseSource)
            )
        viewModel = ViewModelProvider(this, viewModelFactory).get(DetailViewModel::class.java)
        binding = FragmentBottomDialogBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setupChart()
        viewModel.pokemonDetails.observe(this, { details ->
            generateTypeChips(details.type)
            updateStatData(details.stats)
            generateAbilityChips(details.abilities, details.imgUrl)
        })

        viewModel.isAlreadyFavorite.observe(this, {
            setupFavoriteButton(it)
        })

        setupTabLayout()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupFavoriteButton(isFavorite: Boolean) {
        binding.favoriteButton.apply {
            isChecked = isFavorite
            visibility = View.VISIBLE
        }
        binding.favoriteButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.addToFavorites()
            } else {
                viewModel.removeFromFavorites()
            }
        }
    }

    private fun setupTabLayout() {
        val tabLayout = binding.evolutionTabLayout
        val viewPager = binding.evolutionViewpager
        viewModel.evolutionDataUpdated.observe(this, { isUpdated ->
            if (isUpdated) {
                val evolveFrom = viewModel.evolveFromList
                val evolveTo = viewModel.evolveToList

                if (evolveFrom.isNotEmpty() || evolveTo.isNotEmpty()) {
                    if (evolveFrom.isNotEmpty()) tabLayout.addTab(tabLayout.newTab())
                    if (evolveTo.isNotEmpty()) tabLayout.addTab(tabLayout.newTab())

                    viewPager.adapter = EvolutionFragmentAdapter(this, evolveFrom, evolveTo)

                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        if (position == 0 && evolveFrom.isNotEmpty()) {
                            tab.text = "Evolves From"
                        } else {
                            tab.text = "Evolves To"
                        }
                    }.attach()

                    binding.evolutionProgress.visibility = View.GONE
                    tabLayout.visibility = View.VISIBLE
                    viewPager.visibility = View.VISIBLE
                } else {
                    binding.evolutionProgress.visibility = View.GONE
                    binding.textNoEvolution.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun generateTypeChips(typeList: List<String>) {
        typeList.forEach { type ->
            val chip = View.inflate(context, R.layout.type_chip, null) as Chip
            chip.text = type
            chip.setChipBackgroundColorResource(
                when (type) {
                    PokemonType.NORMAL.text -> R.color.normal
                    PokemonType.FIGHTING.text -> R.color.fighting
                    PokemonType.FLYING.text -> R.color.flying
                    PokemonType.POISON.text -> R.color.poison
                    PokemonType.GROUND.text -> R.color.ground
                    PokemonType.ROCK.text -> R.color.rock
                    PokemonType.BUG.text -> R.color.bug
                    PokemonType.GHOST.text -> R.color.ghost
                    PokemonType.STEEL.text -> R.color.steel
                    PokemonType.FIRE.text -> R.color.fire
                    PokemonType.WATER.text -> R.color.water
                    PokemonType.GRASS.text -> R.color.grass
                    PokemonType.ELECTRIC.text -> R.color.electric
                    PokemonType.PSYCHIC.text -> R.color.psychic
                    PokemonType.ICE.text -> R.color.ice
                    PokemonType.DRAGON.text -> R.color.dragon
                    PokemonType.DARK.text -> R.color.dark
                    PokemonType.FAIRY.text -> R.color.fairy
                    else -> R.color.black
                }
            )
            binding.typeChipGroup.addView(chip)
        }
    }

    private fun setupChart() {
        val context = requireContext()

        /** Placeholder values */
        val statBarEntries = listOf(
            BarEntry(0f, 0.0f),
            BarEntry(1f, 0.0f),
            BarEntry(2f, 0.0f),
            BarEntry(3f, 0.0f),
            BarEntry(4f, 0.0f),
            BarEntry(5f, 0.0f),
        )

        val barData = BarData(BarDataSet(statBarEntries, "")).apply {
            barWidth = 0.7f
            setDrawValues(false)
        }

        val statChart = binding.statsChart.apply {
            data = barData
            setTouchEnabled(false)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawBarShadow(true)
            animateY(500, Easing.Linear)
        }

        statChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = TextView(context).textColors.defaultColor
            textSize = 12f
            valueFormatter = ChartValueFormatter(statBarEntries)
        }

        statChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 255f
            textColor = TextView(context).textColors.defaultColor
        }

        statChart.axisRight.apply {
            isEnabled = false
            axisMinimum = 0f
            axisMaximum = 255f
        }

        statChart.invalidate()
    }

    private fun updateStatData(pokemonStats: PokemonStats) {
        val context = requireContext()

        val statBarEntries = listOf(
            BarEntry(0f, pokemonStats.speed.toFloat()),
            BarEntry(1f, pokemonStats.spDefense.toFloat()),
            BarEntry(2f, pokemonStats.spAttack.toFloat()),
            BarEntry(3f, pokemonStats.defense.toFloat()),
            BarEntry(4f, pokemonStats.attack.toFloat()),
            BarEntry(5f, pokemonStats.hp.toFloat()),
        )
        val statDataSet =
            BarDataSet(statBarEntries, "")
        statDataSet.colors = listOf(
            ContextCompat.getColor(context, R.color.speed),
            ContextCompat.getColor(context, R.color.spDefense),
            ContextCompat.getColor(context, R.color.spAttack),
            ContextCompat.getColor(context, R.color.defense),
            ContextCompat.getColor(context, R.color.attack),
            ContextCompat.getColor(context, R.color.hp)
        )

        val barData = BarData(statDataSet).apply {
            barWidth = 0.7f
            setDrawValues(false)
        }

        binding.statsChart.apply {
            data.removeDataSet(0)
            data = barData
            xAxis.valueFormatter = ChartValueFormatter(statBarEntries)
            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun generateAbilityChips(abilityList: List<String>, imgUrl: String?) {
        imgUrl?.let {
            Glide.with(requireContext())
                .asBitmap()
                .load(imgUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        val palette =
                            Palette.from(resource).generate()

                        abilityList.forEach { ability ->
                            val chip = View.inflate(context, R.layout.ability_chip, null) as Chip
                            chip.text = formatString(ability)
                            palette.dominantSwatch?.rgb?.let {
                                val modifiedColor = ColorUtils.blendARGB(it, Color.BLACK, 0.3f)
                                chip.chipBackgroundColor = ColorStateList.valueOf(modifiedColor)
                            }
                            binding.abilitiesChipGroup.addView(chip)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }

    private fun formatString(name: String): String {
        val modifiedWord = name.split('-')
        val buffer: MutableList<String> = mutableListOf()
        for (word in modifiedWord) {
            buffer.add(word.replaceFirstChar { it.uppercaseChar() })
        }
        return buffer.joinToString(" ")
    }
}