package com.example.chart.data

import retrofit2.http.GET

interface ApiService {

    @GET("aggs/ticker/AAPL/range/1/hour/2022-01-09/2023-02-10?adjusted=true&sort=desc&limit=50000&apiKey=hgaMUOmFtKiPf5Opse0Jm_2IEtZUFnFD")
    suspend fun loadBar() : Result
}