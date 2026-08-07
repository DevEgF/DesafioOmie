package com.omie.desafio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omie.desafio.core.database.dao.ProductDao
import com.omie.desafio.core.database.dao.SaleDao
import com.omie.desafio.core.database.entity.ProductEntity
import com.omie.desafio.core.database.entity.SaleEntity
import com.omie.desafio.core.database.entity.SaleItemEntity

@Database(
    entities = [ProductEntity::class, SaleEntity::class, SaleItemEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
}
