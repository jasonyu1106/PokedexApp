package com.example.pokeapp

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

@BindingAdapter("name")
fun TextView.formatPokemonName(name: String?) {
    name?.let {
        val subWords = name.split('-')
        val formatResult = mutableListOf<String>()
        var prefix = ""
        var postfix = ""
        val exceptions = listOf("o")
        val formsList = listOf("mega", "ultra", "gmax")
        val genderSymbol = mapOf(
            Pair("m", "\u2642"), Pair("f", "\u2640")
        )

        subWords
            .forEach { word ->
                val formatted = if (exceptions.contains(word)) {
                    word
                } else {
                    word.replaceFirstChar { it.uppercaseChar() }
                }

                when {
                    formsList.contains(word) -> prefix = "$formatted "
                    genderSymbol.containsKey(word) -> postfix = " ${genderSymbol[word]}"
                    else -> formatResult.add(formatted)
                }
            }
        text = formatResult.joinToString("-", prefix, postfix)
    }
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        val placeholderDrawable = AppCompatResources.getDrawable(
            imgView.context,
            R.drawable.ic_placeholder
        )
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(placeholderDrawable)
                    .error(placeholderDrawable)
            )
            .into(imgView)
    }
}

@BindingAdapter("name", "list")
fun TextView.formatStringList(name: String, list: List<String>?) {
    list?.let {
        val modifiedList: MutableList<String> = mutableListOf()
        for (item in list) {
            val modifiedWord = item.split('-')
            val buffer: MutableList<String> = mutableListOf()
            for (word in modifiedWord) {
                buffer.add(word.replaceFirstChar { it.uppercaseChar() })
            }
            modifiedList.add(buffer.joinToString(" "))
        }
        text = context.getString(R.string.list, name, modifiedList.joinToString())
    }
}

/** Convert Pokemon ID to String */
@BindingAdapter("pokemonId")
fun TextView.formatId(id: Int?) {
    id?.let {
        text = context.getString(R.string.id_string, id)
    }
}

const val INCHES_PER_DECIMETER = 3.937f

/** Convert Pokemon height from decimeters */
@BindingAdapter("pokemonHeight")
fun TextView.formatHeight(height: Int?) {
    height?.let {
        val heightMeters = height.toFloat() / 10
        val heightInches = (height * INCHES_PER_DECIMETER).roundToInt()
        text =
            context.getString(R.string.height, heightMeters, heightInches / 12, heightInches % 12)
    }
}

const val HECTOGRAMS_PER_POUND = 4.536f

/** Convert Pokemon weight from hectograms */
@BindingAdapter("pokemonWeight")
fun TextView.formatWeight(weight: Int?) {
    weight?.let {
        val weightKilograms = weight.toFloat() / 10
        val weightPounds = (weight / HECTOGRAMS_PER_POUND).roundToInt()
        text = context.getString(R.string.weight, weightKilograms, weightPounds)
    }
}

@BindingAdapter("paletteUrl", "paletteTargetView")
fun bindApplyPaletteColor(view: ImageView, url: String, targetView: MaterialCardView) {
    Glide.with(view.context)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                val palette =
                    Palette.from(resource).generate()

                palette.dominantSwatch?.rgb?.let {
                    val modifiedColor = ColorUtils.blendARGB(it, Color.BLACK, 0.3f)
                    targetView.setCardBackgroundColor(modifiedColor)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}