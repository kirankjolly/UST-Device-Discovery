package com.ust.discovery.domain

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Objects

@Entity(
    tableName = "devices",
    indices = [Index(value = ["name", "ipAddress"], unique = true)]
)
data class Device(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val ipAddress: String,
    val port: Int = 0,
    val serviceType: String = "",
    val isOnline: Boolean = true
)
