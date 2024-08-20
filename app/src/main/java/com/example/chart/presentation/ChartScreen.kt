package com.example.chart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chart.data.Bar
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel()) {
    val chartState = chartViewModel.state.collectAsState()
    when (val currentState = chartState.value) {
        is ChartScreenState.Content -> {
            ChartScreenContent(currentState.bars)
        }

        ChartScreenState.Initial -> {

        }
    }
}

@Composable
fun ChartScreenContent(bars: List<Bar>) {


    var chartState by rememberChartState(bars = bars)


    val transformableState = TransformableState{zoomChange, panChange, _ ->
        val visibleBarsCount = (chartState.visibleBarsCount / zoomChange).roundToInt()
            .coerceAtLeast(MIN_VISIBLE_BARS_COUNT)
            .coerceAtMost(bars.size)

        val scrolledBy =(chartState.scrolledBy + panChange.x)
            .coerceAtMost(chartState.barWidth*bars.size-chartState.terminalWidth)
            .coerceAtLeast(0f)

        chartState = chartState.copy(
            visibleBarsCount = visibleBarsCount,
            scrolledBy = scrolledBy
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(transformableState)
            .onSizeChanged {
                chartState = chartState.copy(terminalWidth = it.width.toFloat())
            }
    ) {

        val max = chartState.visibleBars.maxOf { it.high }
        val min = chartState.visibleBars.minOf { it.low }
        translate(left = chartState.scrolledBy){
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - (chartState.barWidth * index)
                val pxPerPoint = size.height / (max-min)
                drawLine(
                    color = Color.White,
                    start = Offset(offsetX, size.height - ((bar.low - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.high - min) * pxPerPoint)),
                    strokeWidth = 1f
                )
                drawLine(
                    color = if (bar.open < bar.close) Color.Green else Color.Red,
                    start = Offset(offsetX, size.height - ((bar.open - min) * pxPerPoint)),
                    end = Offset(offsetX, size.height - ((bar.close - min) * pxPerPoint)),
                    strokeWidth = chartState.barWidth / 2
                )
            }
        }

    }
}