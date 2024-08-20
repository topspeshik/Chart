package com.example.chart

import com.example.chart.data.Bar

interface BarRepository {
    suspend fun loadBar(): List<Bar>
}