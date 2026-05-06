package com.embebidos.gpstracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LocationDao {

    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Query("SELECT * FROM pending_locations WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingLocations(): List<LocationEntity>

    @Query("UPDATE pending_locations SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("DELETE FROM pending_locations WHERE synced = 1")
    suspend fun deleteSynced()

    @Query("SELECT COUNT(*) FROM pending_locations WHERE synced = 0")
    suspend fun getPendingCount(): Int
}