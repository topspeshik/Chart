package com.example.chart.di

import com.example.chart.BarRepository
import com.example.chart.data.ApiFactory
import com.example.chart.data.ApiService
import com.example.chart.data.Bar
import com.example.chart.data.BarRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
interface AppBindModule {


    @Binds
    fun bindBarRepository(authImpl: BarRepositoryImpl) : BarRepository

    companion object {
        
        @Provides
        fun provideApiService(): ApiService {
            return ApiFactory.apiService
        }

    }
}