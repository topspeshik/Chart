package com.example.chart.presentation

import com.example.chart.R

enum class TimeFrame(val timeframe: Int, val value: String) {
    MIN_5(R.string.timeframe_5_min, "5/minute"),
    MIN_15(R.string.timeframe_15_min, "15/minute"),
    MIN_30(R.string.timeframe_30_min, "30/minute"),
    HOUR(R.string.timeframe_1_hour, "1/hour")
}