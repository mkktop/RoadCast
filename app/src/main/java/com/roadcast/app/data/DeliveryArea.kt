package com.roadcast.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_areas")
data class DeliveryArea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
