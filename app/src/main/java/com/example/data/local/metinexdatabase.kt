package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.Product
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        StockInTransaction::class,
        StockOutTransaction::class,
        ActivityLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MetinexDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun stockInDao(): StockInDao
    abstract fun stockOutDao(): StockOutDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: MetinexDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MetinexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MetinexDatabase::class.java,
                    "metinex_aluminium_inventory.db"
                ).addCallback(DatabaseCallback(scope))
                 .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialProducts(database.productDao(), database.activityLogDao())
                    }
                }
            }

            private suspend fun seedInitialProducts(productDao: ProductDao, logDao: ActivityLogDao) {
                val initialProducts = listOf(
                    Product(
                        productId = "PRD-AL-001",
                        productName = "Aluminium Casement Window Outer Frame",
                        hsmCode = "76042100",
                        category = "Extrusions",
                        unit = "KG",
                        ratePerUnit = 340.0,
                        openingStock = 250.0,
                        minimumStock = 50.0,
                        currentStock = 250.0,
                        description = "Premium alloy 6063-T6 architectural window frame section"
                    ),
                    Product(
                        productId = "PRD-AL-002",
                        productName = "Aluminium Sliding Track 2-Track Channel",
                        hsmCode = "76042910",
                        category = "Channels",
                        unit = "KG",
                        ratePerUnit = 360.0,
                        openingStock = 180.0,
                        minimumStock = 40.0,
                        currentStock = 180.0,
                        description = "Heavy duty double track sliding door & window section"
                    ),
                    Product(
                        productId = "PRD-AL-003",
                        productName = "Aluminium Curtain Wall Mullion Profile",
                        hsmCode = "76101000",
                        category = "Extrusions",
                        unit = "PC",
                        ratePerUnit = 1250.0,
                        openingStock = 60.0,
                        minimumStock = 15.0,
                        currentStock = 60.0,
                        description = "Structural curtain wall vertical mullion 65mm x 150mm"
                    ),
                    Product(
                        productId = "PRD-AL-004",
                        productName = "Architectural Aluminium Louver Section",
                        hsmCode = "76042990",
                        category = "Architectural",
                        unit = "PC",
                        ratePerUnit = 880.0,
                        openingStock = 120.0,
                        minimumStock = 25.0,
                        currentStock = 120.0,
                        description = "Sun louver aero-foil blade for ventilation and shading"
                    ),
                    Product(
                        productId = "PRD-AL-005",
                        productName = "Heavy Duty Aluminium Door Bottom Rail",
                        hsmCode = "76101000",
                        category = "Extrusions",
                        unit = "PC",
                        ratePerUnit = 950.0,
                        openingStock = 90.0,
                        minimumStock = 20.0,
                        currentStock = 90.0,
                        description = "Commercial door bottom rail section with weather wool"
                    ),
                    Product(
                        productId = "PRD-AL-006",
                        productName = "Powder Coated Aluminium Angle 50x50mm",
                        hsmCode = "76042930",
                        category = "Powder Coated",
                        unit = "KG",
                        ratePerUnit = 380.0,
                        openingStock = 320.0,
                        minimumStock = 60.0,
                        currentStock = 320.0,
                        description = "Matt black powder coated equal angle profile"
                    ),
                    Product(
                        productId = "PRD-AL-007",
                        productName = "Aluminium Composite Panel Sheet (ACP)",
                        hsmCode = "76061200",
                        category = "Sheets",
                        unit = "SQFT",
                        ratePerUnit = 110.0,
                        openingStock = 1500.0,
                        minimumStock = 300.0,
                        currentStock = 1500.0,
                        description = "4mm PVDF coated metallic silver exterior grade ACP"
                    ),
                    Product(
                        productId = "PRD-AL-008",
                        productName = "EPDM Weather Stripping Rubber Gasket",
                        hsmCode = "40169320",
                        category = "Accessories",
                        unit = "ROLL",
                        ratePerUnit = 450.0,
                        openingStock = 45.0,
                        minimumStock = 10.0,
                        currentStock = 45.0,
                        description = "Anti-aging ozone resistant EPDM glazing gasket 100m/roll"
                    ),
                    Product(
                        productId = "PRD-AL-009",
                        productName = "SS Window Friction Stay Hinge 12 Inch",
                        hsmCode = "83021010",
                        category = "Hardware",
                        unit = "PAIR",
                        ratePerUnit = 220.0,
                        openingStock = 110.0,
                        minimumStock = 30.0,
                        currentStock = 110.0,
                        description = "Grade 304 stainless steel heavy duty casement window friction stay"
                    ),
                    Product(
                        productId = "PRD-AL-010",
                        productName = "Multipoint Casement Window Handle",
                        hsmCode = "83024110",
                        category = "Hardware",
                        unit = "SET",
                        ratePerUnit = 350.0,
                        openingStock = 85.0,
                        minimumStock = 25.0,
                        currentStock = 85.0,
                        description = "Zinc die-cast powder coated multipoint espag handle"
                    ),
                    Product(
                        productId = "PRD-AL-011",
                        productName = "Heavy Duty Nylon Sliding Roller Bearing",
                        hsmCode = "83024200",
                        category = "Hardware",
                        unit = "PAIR",
                        ratePerUnit = 180.0,
                        openingStock = 140.0,
                        minimumStock = 30.0,
                        currentStock = 140.0,
                        description = "Stainless steel carriage with quiet nylon wheels (load: 120kg)"
                    ),
                    Product(
                        productId = "PRD-AL-012",
                        productName = "Structural Glazing Neutral Silicone Sealant",
                        hsmCode = "32141000",
                        category = "Accessories",
                        unit = "BOX",
                        ratePerUnit = 1800.0,
                        openingStock = 24.0,
                        minimumStock = 8.0,
                        currentStock = 24.0,
                        description = "Weatherproofing silicone sealant sausage box (20 sausages/box)"
                    )
                )

                productDao.insertProducts(initialProducts)
                logDao.insertLog(
                    ActivityLog(
                        logId = "LOG-INIT-001",
                        userName = "System",
                        action = "System Initialized",
                        description = "Preloaded 12 standard Metinex Aluminium catalog products.",
                        productName = "Metinex Catalog",
                        transactionType = "SYSTEM",
                        quantity = 0.0,
                        previousStock = 0.0,
                        updatedStock = 0.0,
                        date = "02-11-2026",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
