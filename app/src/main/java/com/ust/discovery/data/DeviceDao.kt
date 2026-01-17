package com.ust.discovery.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ust.discovery.domain.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {

    @Query("SELECT * FROM devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<Device>>

    @Query("SELECT * FROM devices WHERE name = :name AND ipAddress = :ipAddress LIMIT 1")
    suspend fun getDevice(name: String, ipAddress: String): Device?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevices(devices: List<Device>)

    @Update
    suspend fun updateDevice(device: Device)

    @Query("UPDATE devices SET isOnline = :isOnline WHERE name = :name AND ipAddress = :ipAddress")
    suspend fun updateDeviceStatus(name: String, ipAddress: String, isOnline: Boolean)

    @Query("UPDATE devices SET isOnline = 0")
    suspend fun markAllDevicesOffline()

    @Query("DELETE FROM devices")
    suspend fun deleteAllDevices()

    @Query("DELETE FROM devices WHERE name = :name AND ipAddress = :ipAddress")
    suspend fun deleteDevice(name: String, ipAddress: String)
}
