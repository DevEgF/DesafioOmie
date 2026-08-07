package com.omie.desafio.core.domain

interface DeviceMetricsProvider {
    fun getAppMemoryUsageMb(): Long
    fun getAppStorageUsageMb(): Long
}
