package com.roadcast.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supermarkets",
    foreignKeys = [ForeignKey(
        entity = DeliveryArea::class,
        parentColumns = ["id"],
        childColumns = ["areaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("areaId")]
)
data class Supermarket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val areaId: Long,
    val contactPerson: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val remark: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
