package com.embebidos.gpstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float,
    val battery: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)