package com.example.chart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chart.data.Bar
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel()) {
    val chartState = chartViewModel.state.collectAsState()
    when (val currentState = chartState.value) {
        is ChartScreenState.Content -> {
            ChartScreenContent(
                currentState.bars,
                currentState.visibleBars,
                currentState.visibleBarsCount,
                currentState.terminalWidth,
                currentState.scrolledBy,
                currentState.barWidth,
                onScrolledChanged = { bars, scroll ->
                    chartViewModel.onScrolledChanged(
                        bars,
                        scroll
                    )
                },
                onTerminalWidthChanged = { chartViewModel.onTerminalWidthChanged(it) })
        }

        ChartScreenState.Initial -> {

        }
    }
}

@Composable
fun ChartScreenContent(
    bars: List<Bar>,
    visibleBars: List<Bar>,
    visibleBarsCount: Int,
    terminalWidth: Float,
    scrolledBy: Float,
    barWidth: Float,
    onScrolledChanged: (Int, Float) -> Unit,
    onTerminalWidthChanged: (Float) -> Unit
) {

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val visibleBarsCount = (visibleBarsCount / zoomChange).roundToInt()
            .coerceAtLeast(MIN_VISIBLE_BARS_COUNT)
            .coerceAtMost(bars.size)

        val scrolledBy = (scrolledBy + panChange.x)
            .coerceAtMost(barWidth * bars.size - terminalWidth)
            .coerceAtLeast(0f)

        onScrolledChanged(
            visibleBarsCount,
            scrolledBy
        )

    }

    val textMeasure = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clipToBounds()
            .padding(
                top = 64.dp,
                bottom = 64.dp
            )
            .transformable(transformableState)
            .onSizeChanged {
                onTerminalWidthChanged(it.width.toFloat())
            }
    ) {

        val max = visibleBars.maxOf { it.high }
        val min = visibleBars.minOf { it.low }
        val pxPerPoint = size.height / (max - min)
        translate(left = scrolledBy) {
            bars.forEachIndexed { index, bar ->
                val offsetX = size.width - (barWidth * index)
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
                    strokeWidth = barWidth / 2
                )
            }
        }
        bars.firstOrNull()?.let {
            drawPrices(
                max,
                min,
                pxPerPoint,
                it.close,
                textMeasure
            )
        }

    }
}

private fun DrawScope.drawPrices(
    max: Float,
    min: Float,
    pxPerPoint: Float,
    lastPrice: Float,
    textMeasure: TextMeasurer
) {
    //max price
    drawDashedLine(
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
    )

    drawTextPrice(
        textMeasure = textMeasure,
        price = max,
        offsetY = 0f
    )


    //last price
    val lastPriceY = size.height - (lastPrice - min) * pxPerPoint
    drawDashedLine(
        start = Offset(0f, lastPriceY),
        end = Offset(size.width, lastPriceY),
    )

    drawTextPrice(
        textMeasure = textMeasure,
        price = lastPrice,
        offsetY = lastPriceY
    )

    //min price
    drawDashedLine(
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
    )

    drawTextPrice(
        textMeasure = textMeasure,
        price = min,
        offsetY = size.height
    )
}

private fun DrawScope.drawTextPrice(
    textMeasure: TextMeasurer,
    price: Float,
    offsetY: Float
) {
    val textLayoutResult = textMeasure.measure(
        text = price.toString(),
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(size.width - textLayoutResult.size.width, offsetY)
    )
}

private fun DrawScope.drawDashedLine(
    color: Color = Color.White,
    start: Offset,
    end: Offset,
    strokeWidth: Float = 1f
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(
                4.dp.toPx(), 4.dp.toPx()
            )
        )
    )
}





















