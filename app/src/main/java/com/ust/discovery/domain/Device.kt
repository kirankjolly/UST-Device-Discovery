package com.ust.discovery.domain

import java.util.Objects

data class Device(
    val name: String,
    val ipAddress: String,
    val port: Int = 0,
    val serviceType: String = "",
    val isOnline: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Device) return false
        return name == other.name && ipAddress == other.ipAddress
    }

    override fun hashCode(): Int {
        return Objects.hash(name, ipAddress)
    }
}
