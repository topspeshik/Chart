package com.example.chart.presentation

import com.example.chart.data.Bar
import kotlin.math.roundToInt

sealed class ChartScreenState {

    object Initial: ChartScreenState()

    object Loading: ChartScreenState()

    data class Content(
        val bars: List<Bar>,
        val timeFrame: TimeFrame,
        val visibleBarsCount: Int = 100,
        val terminalWidth: Float = 0f,
        val scrolledBy: Float = 0f,
        val barWidth: Float = 0f,
        val visibleBars: List<Bar> = bars.subList(0, visibleBarsCount),

    ) : ChartScreenState()


}