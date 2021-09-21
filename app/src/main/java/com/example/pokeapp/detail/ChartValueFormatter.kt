package com.example.pokeapp.detail

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChartValueFormatter(private val barEntries: List<BarEntry>) : IndexAxisValueFormatter() {

    init {
        values = listOf("Speed", "Sp. Def", "Sp. Atk", "Defense", "Attack", "HP").toTypedArray()
    }

    override fun getFormattedValue(value: Float): String {
        val axisLabel = StringBuilder(super.getFormattedValue(value))
        val statValue = barEntries[value.toInt()].y.toInt().toString()
        axisLabel.append(
            statValue.padStart(
                AXIS_LABEL_LENGTH - statValue.length
            )
        )
        return axisLabel.toString()
    }

}