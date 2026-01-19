package com.ust.discovery.domain

data class IpGeoInfo(
    val ip: String,
    val city: String,
    val region: String,
    val country: String,
    val loc: String,
    val org: String,
    val timezone: String
)
