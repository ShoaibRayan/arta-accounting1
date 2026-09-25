package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TransactionEntity::class,
        AccountCardEntity::class,
        RecipientEntity::class,
        BudgetEntity::class,
        CurrencyEntity::class,
        CategoryEntity::class,
        FinancialGoalEntity::class,
        GoalTransactionEntity::class,
        QuickActionEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 18,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun recipientDao(): RecipientDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun goalTransactionDao(): GoalTransactionDao
    abstract fun quickActionDao(): QuickActionDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE shopping_list_items ADD COLUMN unitPrice REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN recipientId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN kind TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN originalTransactionId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN settledAmount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceCurrencyCode TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceCurrencySymbol TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceAmount REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN targetCurrencyCode TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN targetCurrencySymbol TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN targetAmount REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN conversionRate REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN historicalBaseCurrencyCode TEXT NOT NULL DEFAULT 'AFN'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN historicalExchangeRateToBase REAL NOT NULL DEFAULT 1.0")

                // Backfill recipientId from recipients table only if unique (unambiguous)
                db.execSQL("""
                    UPDATE transactions 
                    SET recipientId = (
                        SELECT id FROM recipients 
                        WHERE recipients.name = transactions.recipientName 
                        AND (SELECT COUNT(*) FROM recipients r2 WHERE r2.name = transactions.recipientName) = 1
                    )
                    WHERE recipientName IS NOT NULL AND recipientName != ''
                """.trimIndent())

                // Backfill kind based on category and title
                db.execSQL("UPDATE transactions SET kind = 'CURRENCY_EXCHANGE' WHERE category = 'Exchange' OR category = 'تبدیل اسعار' OR title LIKE '%تبدیل اسعار%'")
                db.execSQL("UPDATE transactions SET kind = 'TRANSFER' WHERE category = 'انتقالات' OR title LIKE '%انتقال کارت به کارت%' OR title LIKE '%انتقال نقدی به کارت%' OR title LIKE '%انتقال کارت به نقدی%'")
                db.execSQL("UPDATE transactions SET kind = 'GOAL_DEPOSIT' WHERE category = 'واریز به هدف' OR title LIKE '%انتقال به هدف%'")
                db.execSQL("UPDATE transactions SET kind = 'GOAL_WITHDRAW' WHERE category = 'برداشت از هدف' OR title LIKE '%انتقال از هدف%'")
                db.execSQL("UPDATE transactions SET kind = 'PERSON_TRANSFER' WHERE category = 'انتقال حساب اشخاص' OR category = 'PersonDebt'")
                db.execSQL("UPDATE transactions SET kind = 'PERSON_CURRENCY_EXCHANGE' WHERE category = 'PersonExchange'")
                db.execSQL("UPDATE transactions SET kind = 'PERSON_PAYMENT' WHERE (recipientName IS NOT NULL AND recipientName != '') AND type = 'EXPENSE' AND kind = 'NORMAL'")
                db.execSQL("UPDATE transactions SET kind = 'PERSON_RECEIVE' WHERE (recipientName IS NOT NULL AND recipientName != '') AND type = 'INCOME' AND kind = 'NORMAL'")

                // Historical exchange rate backfill
                db.execSQL("UPDATE transactions SET historicalExchangeRateToBase = exchangeRate WHERE exchangeRate > 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN categoryId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE budgets ADD COLUMN categoryId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE quick_actions ADD COLUMN categoryId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE shopping_lists ADD COLUMN categoryId INTEGER DEFAULT NULL")

                // Backfill categoryId from categories table only if unique (unambiguous)
                db.execSQL("""
                    UPDATE transactions 
                    SET categoryId = (
                        SELECT id FROM categories 
                        WHERE categories.name = transactions.category 
                        AND (SELECT COUNT(*) FROM categories c2 WHERE c2.name = transactions.category) = 1
                    )
                    WHERE category IS NOT NULL AND category != ''
                """.trimIndent())

                db.execSQL("""
                    UPDATE budgets 
                    SET categoryId = (
                        SELECT id FROM categories 
                        WHERE categories.name = budgets.category 
                        AND (SELECT COUNT(*) FROM categories c2 WHERE c2.name = budgets.category) = 1
                    )
                    WHERE category IS NOT NULL AND category != ''
                """.trimIndent())

                db.execSQL("""
                    UPDATE quick_actions 
                    SET categoryId = (
                        SELECT id FROM categories 
                        WHERE categories.name = quick_actions.categoryName 
                        AND (SELECT COUNT(*) FROM categories c2 WHERE c2.name = quick_actions.categoryName) = 1
                    )
                    WHERE categoryName IS NOT NULL AND categoryName != ''
                """.trimIndent())

                db.execSQL("""
                    UPDATE shopping_lists 
                    SET categoryId = (
                        SELECT id FROM categories 
                        WHERE categories.name = shopping_lists.category 
                        AND (SELECT COUNT(*) FROM categories c2 WHERE c2.name = shopping_lists.category) = 1
                    )
                    WHERE category IS NOT NULL AND category != ''
                """.trimIndent())
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE recipients ADD COLUMN isAmountProtected INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_recipientId ON transactions(recipientId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions(accountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_currencyCode ON transactions(currencyCode)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_timestamp ON transactions(timestamp)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_relatedTransactionId ON transactions(relatedTransactionId)")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Transactions table
                db.execSQL("ALTER TABLE transactions ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN ledgerCurrencyId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN sourceCurrencyId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN targetCurrencyId INTEGER DEFAULT NULL")

                // 2. Accounts table
                db.execSQL("ALTER TABLE accounts ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")

                // 3. Budgets table
                db.execSQL("ALTER TABLE budgets ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")

                // 4. Financial Goals table
                db.execSQL("ALTER TABLE financial_goals ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")

                // 5. Goal Transactions table
                db.execSQL("ALTER TABLE goal_transactions ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")

                // 6. Quick Actions table
                db.execSQL("ALTER TABLE quick_actions ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")

                // 7. Shopping Lists table
                db.execSQL("ALTER TABLE shopping_lists ADD COLUMN currencyId INTEGER NOT NULL DEFAULT 0")

                // Indexes
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_currencyId ON transactions(currencyId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_ledgerCurrencyId ON transactions(ledgerCurrencyId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_currencyId ON accounts(currencyId)")

                // Strict atomic backfill from currencies table:
                // Only match if unambiguous: exactly 1 currency with this code
                db.execSQL("""
                    UPDATE transactions
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(transactions.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(transactions.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(transactions.currencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE transactions
                    SET ledgerCurrencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(transactions.ledgerCurrencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(transactions.ledgerCurrencyCode)) = 1
                    )
                    WHERE ledgerCurrencyCode IS NOT NULL AND ledgerCurrencyCode != ''
                    AND (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(transactions.ledgerCurrencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE transactions
                    SET sourceCurrencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(transactions.sourceCurrencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(transactions.sourceCurrencyCode)) = 1
                    )
                    WHERE sourceCurrencyCode IS NOT NULL AND sourceCurrencyCode != ''
                    AND (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(transactions.sourceCurrencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE transactions
                    SET targetCurrencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(transactions.targetCurrencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(transactions.targetCurrencyCode)) = 1
                    )
                    WHERE targetCurrencyCode IS NOT NULL AND targetCurrencyCode != ''
                    AND (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(transactions.targetCurrencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE accounts
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(accounts.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(accounts.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(accounts.currencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE budgets
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(budgets.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(budgets.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(budgets.currencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE financial_goals
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(financial_goals.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(financial_goals.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(financial_goals.currencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE goal_transactions
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(goal_transactions.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(goal_transactions.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(goal_transactions.currencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE quick_actions
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(quick_actions.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(quick_actions.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(quick_actions.currencyCode)) = 1
                """.trimIndent())

                db.execSQL("""
                    UPDATE shopping_lists
                    SET currencyId = (
                        SELECT id FROM currencies
                        WHERE UPPER(currencies.code) = UPPER(shopping_lists.currencyCode)
                        AND (SELECT COUNT(*) FROM currencies c2 WHERE UPPER(c2.code) = UPPER(shopping_lists.currencyCode)) = 1
                    )
                    WHERE (SELECT COUNT(*) FROM currencies c3 WHERE UPPER(c3.code) = UPPER(shopping_lists.currencyCode)) = 1
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_app_db"
                )
                    .addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Migrate goal deposits and withdrawals to TRANSFER type
                            db.execSQL("""
                                UPDATE transactions 
                                SET type = 'TRANSFER', kind = 'GOAL_DEPOSIT' 
                                WHERE category = 'واریز به هدف' OR title LIKE '%انتقال به هدف%'
                            """.trimIndent())
                            db.execSQL("""
                                UPDATE transactions 
                                SET type = 'TRANSFER', kind = 'GOAL_WITHDRAW' 
                                WHERE category = 'برداشت از هدف' OR title LIKE '%انتقال از هدف%'
                            """.trimIndent())
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
