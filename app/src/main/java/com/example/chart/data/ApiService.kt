package com.example.chart.data

import com.example.chart.presentation.TimeFrame
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("aggs/ticker/AAPL/range/{timeframe}/2022-01-09/2023-02-10?adjusted=true&sort=desc&limit=50000&apiKey=hgaMUOmFtKiPf5Opse0Jm_2IEtZUFnFD")
    suspend fun loadBars(
        @Path("timeframe") timeFrame: String
    ): Result
}