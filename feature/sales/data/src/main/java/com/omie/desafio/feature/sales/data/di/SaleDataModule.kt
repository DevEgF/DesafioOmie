package com.omie.desafio.feature.sales.data.di

import com.omie.desafio.feature.sales.data.repository.SaleRepositoryImpl
import com.omie.desafio.feature.sales.domain.repository.SaleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SaleDataModule {
    @Binds
    @Singleton
    abstract fun bindSaleRepository(impl: SaleRepositoryImpl): SaleRepository
}
