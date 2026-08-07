package com.omie.desafio.core.analytics.di

import com.omie.desafio.core.analytics.DataStoreCrashCounter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CrashCounterEntryPoint {
    fun crashCounter(): DataStoreCrashCounter
}
