package com.example.chart.presentation


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.chart.data.Bar
import java.sql.Time
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

private const val MIN_VISIBLE_BARS_COUNT = 20

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel()) {
    val chartState = chartViewModel.state.collectAsState()
    when (val currentState = chartState.value) {
        is ChartScreenState.Content -> {

            Chart(
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
                onTerminalWidthChanged = { chartViewModel.onTerminalWidthChanged(it) },
                currentState.timeFrame)
            TimeFrames(selectedFrame = currentState.timeFrame) {
                chartViewModel.loadBarList(it)
            }
        }

        ChartScreenState.Initial -> {

        }

        ChartScreenState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun TimeFrames(
    selectedFrame: TimeFrame,
    onTimeFrameSelected: (TimeFrame) -> Unit
) {

    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeFrame.entries.forEach { timeframe ->
            val isSelected = selectedFrame == timeframe
            AssistChip(
                onClick = { onTimeFrameSelected(timeframe) },
                label = {
                    Text(
                        text = stringResource(id = timeframe.timeframe)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) Color.White else Color.Black,
                    labelColor = if (!isSelected) Color.White else Color.Black,
                )
            )

        }
    }

}


@Composable
fun Chart(
    bars: List<Bar>,
    visibleBars: List<Bar>,
    visibleBarsCount: Int,
    terminalWidth: Float,
    scrolledBy: Float,
    barWidth: Float,
    onScrolledChanged: (Int, Float) -> Unit,
    onTerminalWidthChanged: (Float) -> Unit,
    timeFrame: TimeFrame
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
                drawTimeDelimiter(
                    bar = bar,
                    nextBar = if (index < bars.size - 1) {
                        bars[index + 1]
                    } else {
                        null
                    },
                    timeFrame = timeFrame,
                    offsetX = offsetX,
                    textMeasurer = textMeasure
                )
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

private fun DrawScope.drawTimeDelimiter(
    bar: Bar,
    nextBar: Bar?,
    timeFrame: TimeFrame,
    offsetX: Float,
    textMeasurer: TextMeasurer

){
    val calendar = bar.calendar

    val minutes = calendar.get(Calendar.MINUTE)

    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val shouldDrawDelimiter = when (timeFrame) {
        TimeFrame.MIN_5 -> {
            minutes == 0
        }

        TimeFrame.MIN_15 -> {
            minutes == 0 && hours % 2 == 0
        }

        TimeFrame.MIN_30, TimeFrame.HOUR -> {
            val nextBarDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            day != nextBarDay
        }
    }
    if (!shouldDrawDelimiter) return

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.dp.toPx(), 4.dp.toPx())
        )
    )

    val nameOfMonth = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
    val text = when (timeFrame) {
        TimeFrame.MIN_5, TimeFrame.MIN_15 -> {
            String.format("%02d:00", hours)
        }

        TimeFrame.MIN_30, TimeFrame.HOUR -> {
            String.format("%s %s", day, nameOfMonth)
        }
    }
    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = Color.White,
            fontSize = 12.sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(offsetX - textLayoutResult.size.width / 2, size.height)
    )
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





















