package com.omie.desafio.core.presentation

import java.text.NumberFormat
import java.util.Locale

fun Long.centsToBrl(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(this / 100.0)
}
