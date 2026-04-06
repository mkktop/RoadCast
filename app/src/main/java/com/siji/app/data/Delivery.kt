package com.siji.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeliveryStatus {
    PENDING,
    DELIVERING,
    COMPLETED
}

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val address: String,
    val phone: String = "",
    val deliveryTime: Long,        // timestamp in millis
    val remark: String = "",
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
