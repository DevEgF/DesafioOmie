package com.omie.desafio.core.domain

interface RemoteConfigProvider {
    fun isSaleDetailEnabled(): Boolean
    fun getSaleDetailEnabledOverride(): Boolean?
    suspend fun setSaleDetailEnabledOverride(enabled: Boolean?)
}
