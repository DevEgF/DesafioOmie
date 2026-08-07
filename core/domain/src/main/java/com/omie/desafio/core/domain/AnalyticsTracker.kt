package com.omie.desafio.core.domain

interface AnalyticsTracker {
    fun logScreenView(screenName: String)
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun recordException(throwable: Throwable)
}
