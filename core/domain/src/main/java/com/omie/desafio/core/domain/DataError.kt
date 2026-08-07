package com.omie.desafio.core.domain

sealed interface DataError : Error {
    enum class Local : DataError {
        UNKNOWN,
    }

    enum class Validation : DataError {
        BLANK_PRODUCT_NAME,
        BLANK_PRODUCT_DESCRIPTION,
        INVALID_UNIT_PRICE,
        BLANK_CLIENT_NAME,
        NO_ITEMS,
        NO_PRODUCT_SELECTED,
        INVALID_QUANTITY,
    }
}
