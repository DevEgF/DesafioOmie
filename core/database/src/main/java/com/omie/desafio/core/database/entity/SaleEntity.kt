package com.omie.desafio.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val totalQuantity: Int,
    val totalValueCents: Long,
    val createdAt: Long,
)
