package com.roadcast.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StopStatus {
    PENDING,
    COMPLETED,
    SKIPPED
}

@Entity(
    tableName = "route_stops",
    foreignKeys = [ForeignKey(
        entity = Supermarket::class,
        parentColumns = ["id"],
        childColumns = ["supermarketId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("supermarketId"), Index("date")]
)
data class RouteStop(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val supermarketId: Long,
    val orderIndex: Int,
    val status: StopStatus = StopStatus.PENDING,
    val completedAt: Long? = null,
    val remark: String? = null,
    val deliveryItems: String? = null  // 送货清单，每行一项
)
