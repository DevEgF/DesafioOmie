package com.omie.desafio.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.omie.desafio.core.database.entity.ProductEntity
import com.omie.desafio.core.database.entity.SaleEntity
import com.omie.desafio.core.database.entity.SaleItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaleDaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertSaleWithItems_persistsSaleAndItemsAndUpdatesTotal() = runBlocking {
        val productId = db.productDao().upsert(
            ProductEntity(name = "Caneta", description = "Caneta azul", unitPriceCents = 500, createdAt = 0L),
        )

        val sale = SaleEntity(clientName = "Maria", totalQuantity = 2, totalValueCents = 1000, createdAt = 1L)
        val items = listOf(
            SaleItemEntity(
                saleId = 0,
                productId = productId,
                productName = "Caneta",
                productDescription = "Caneta azul",
                quantity = 2,
                unitPriceCents = 500,
                totalValueCents = 1000,
            ),
        )

        db.saleDao().insertSaleWithItems(sale, items)

        val salesWithItems = db.saleDao().observeAllWithItems().first()
        assertEquals(1, salesWithItems.size)
        assertEquals("Maria", salesWithItems[0].sale.clientName)
        assertEquals(1, salesWithItems[0].items.size)
        assertEquals(1000L, db.saleDao().observeTotalValueCents().first())
    }
}
