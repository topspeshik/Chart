package com.example.chart.presentation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.chart.data.Bar
import kotlinx.android.parcel.Parcelize
import kotlin.math.roundToInt

@kotlinx.parcelize.Parcelize
data class ChartState(
    val barList: List<Bar>,
    val visibleBarsCount: Int = 100,
    val terminalWidth: Float = 0f,
    val scrolledBy: Float = 0f
) : Parcelable {
    val barWidth: Float
        get() = terminalWidth / visibleBarsCount


    val visibleBars: List<Bar>
        get() {
            val startIndex = (scrolledBy / barWidth).roundToInt().coerceAtLeast(0)
            val endIndex = (startIndex + visibleBarsCount).coerceAtMost(barList.size)
            return barList.subList(startIndex, endIndex)
        }

}

@Composable
fun rememberChartState(bars: List<Bar>): MutableState<ChartState> {
    return rememberSaveable {
        mutableStateOf(ChartState(bars))
    }
}