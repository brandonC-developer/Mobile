package app.apartment.mobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** CMP-002 (ADR-0002). Version 1; every later change adds a migration. */
@Database(
    entities = [PropertyEntity::class, TenantEntity::class, RentalEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun properties(): PropertyDao
    abstract fun tenants(): TenantDao
    abstract fun rentals(): RentalDao
    abstract fun settings(): SettingsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "apartment.db")
                .build().also { instance = it }
        }
    }
}
