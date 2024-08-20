package com.example.chart.data

import com.example.chart.BarRepository
import javax.inject.Inject

class BarRepositoryImpl @Inject constructor(
    private val apiService: ApiService
): BarRepository {
    override suspend fun loadBar(): List<Bar> {
       return apiService.loadBar().barList
    }
}