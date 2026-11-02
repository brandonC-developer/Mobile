package app.apartment.mobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Insert suspend fun insert(property: PropertyEntity): Long
    @Update suspend fun update(property: PropertyEntity)
    @Query("SELECT * FROM property ORDER BY name") fun observeAll(): Flow<List<PropertyEntity>>
    @Query("SELECT * FROM property WHERE id = :id") suspend fun getById(id: Long): PropertyEntity?
    // No delete on purpose: properties are never deleted (FR-003).
}

@Dao
interface TenantDao {
    @Insert suspend fun insert(tenant: TenantEntity): Long
    @Update suspend fun update(tenant: TenantEntity)
    @Delete suspend fun delete(tenant: TenantEntity)
    @Query("SELECT * FROM tenant ORDER BY fullName") fun observeAll(): Flow<List<TenantEntity>>
    @Query("SELECT COUNT(*) FROM rental WHERE tenantId = :tenantId") suspend fun rentalCount(tenantId: Long): Int
    @Query("SELECT * FROM tenant") suspend fun getAll(): List<TenantEntity>
    @Query("SELECT * FROM tenant WHERE id = :id") suspend fun getById(id: Long): TenantEntity?
}

@Dao
interface RentalDao {
    @Insert suspend fun insert(rental: RentalEntity): Long
    @Update suspend fun update(rental: RentalEntity)
    @Delete suspend fun delete(rental: RentalEntity)
    @Query("SELECT * FROM rental") fun observeAll(): Flow<List<RentalEntity>>
    @Query("SELECT * FROM rental") suspend fun getAll(): List<RentalEntity>
    @Query("SELECT * FROM rental WHERE id = :id") suspend fun getById(id: Long): RentalEntity?
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1") suspend fun get(): SettingsEntity?
    @Query("SELECT * FROM settings WHERE id = 1") fun observe(): Flow<SettingsEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(settings: SettingsEntity)
}
