package com.example.chart.presentation

import com.example.chart.data.Bar
import kotlin.math.roundToInt

sealed class ChartScreenState {
    object Initial: ChartScreenState()

    data class Content(
        val bars: List<Bar>,
        val visibleBarsCount: Int = 100,
        val terminalWidth: Float = 0f,
        val scrolledBy: Float = 0f,
        val barWidth: Float = 0f,
        val visibleBars: List<Bar> = bars.subList(0, visibleBarsCount),

    ) : ChartScreenState() {
//        val barWidth: Float
//            get() =
//                terminalWidth / visibleBarsCount
//
//
//
//        val visibleBars: List<Bar>
//            get() {
//
//                val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
//                val endIndex = (startIndex + visibleBarsCount).coerceAtMost(bars.size)
//                return bars.subList(startIndex, endIndex)
//            }
    }
}