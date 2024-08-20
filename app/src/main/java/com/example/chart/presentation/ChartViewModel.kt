package com.example.chart.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chart.BarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ChartViewModel @Inject constructor(
    private val barRepository: BarRepository
): ViewModel() {

    private val _state = MutableStateFlow<ChartScreenState>(ChartScreenState.Initial)
    val state = _state.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler{ _,throwable->
        Log.d("ChartViewModel", throwable.toString())

    }

    init {
        loadBarList()
    }

    private fun loadBarList(){
        viewModelScope.launch(exceptionHandler) {
           val barList = barRepository.loadBar()
            _state.value = ChartScreenState.Content(bars = barList)
        }
    }


//    fun onScrolledChanged(bars:Int, scroll: Float){
//        _state.update {
//            val contentState = it as ChartScreenState.Content
//            val newBarWidth = contentState.terminalWidth / bars
//
//            val startIndex = (scroll / newBarWidth).roundToInt().coerceAtLeast(0)
//            val endIndex = (startIndex + bars).coerceAtMost(contentState.bars.size)
//
//            val newVisibleBars = contentState.bars.subList(startIndex, endIndex)
//
//            contentState.copy(
//                visibleBarsCount = bars,
//                scrolledBy = scroll,
//                visibleBars = newVisibleBars,
//                barWidth = newBarWidth
//            )
//        }
//    }
//
//
//    fun onTerminalWidthChanged(width : Float){
//        _state.update {
//            val contentState = it as ChartScreenState.Content
//
//
//            val newBarWidth = width / contentState.visibleBarsCount
//
//            val startIndex = (contentState.scrolledBy / newBarWidth).roundToInt().coerceAtLeast(0)
//            val endIndex = (startIndex + contentState.visibleBarsCount).coerceAtMost(contentState.bars.size)
//
//            val newVisibleBars = contentState.bars.subList(startIndex, endIndex)
//
//            contentState.copy(
//                terminalWidth = width,
//                barWidth = newBarWidth,
//                visibleBars = newVisibleBars
//            )
//        }
//    }
}