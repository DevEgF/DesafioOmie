package com.omie.desafio.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.omie.desafio.core.database.entity.SaleEntity
import com.omie.desafio.core.database.entity.SaleItemEntity
import com.omie.desafio.core.database.entity.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun observeAllWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    fun observeWithItemsById(saleId: Long): Flow<SaleWithItems?>

    @Query("SELECT COALESCE(SUM(totalValueCents), 0) FROM sales")
    fun observeTotalValueCents(): Flow<Long>

    @Insert
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert
    suspend fun insertItems(items: List<SaleItemEntity>)

    @Transaction
    suspend fun insertSaleWithItems(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = insertSale(sale)
        insertItems(items.map { it.copy(saleId = saleId) })
        return saleId
    }
}
