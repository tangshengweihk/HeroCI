package com.heroesports.heroci.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
suspend fun getLocation(
    context: Context,
    geocoder: Geocoder,
    onLocationResult: (String, Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // 先尝试获取最后已知位置
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        if (lastKnownLocation != null) {
            processLocation(lastKnownLocation.latitude, lastKnownLocation.longitude, geocoder, onLocationResult)
            return
        }

        // 如果没有最后已知位置，则请求位置更新
        val location = suspendCancellableCoroutine { continuation ->
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    locationManager.removeUpdates(this)
                    continuation.resume(location)
                }

                override fun onProviderDisabled(provider: String) {
                    if (!continuation.isCompleted) {
                        onError("位置服务已禁用")
                    }
                }

                override fun onProviderEnabled(provider: String) {}
            }

            try {
                // 先尝试网络定位
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                }
                // 同时也尝试 GPS 定位
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0L,
                        0f,
                        locationListener
                    )
                }

                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(locationListener)
                }
            } catch (e: Exception) {
                onError("获取位置失败: ${e.message}")
                continuation.cancel()
            }
        }

        location?.let {
            processLocation(it.latitude, it.longitude, geocoder, onLocationResult)
        } ?: onError("无法获取位置")
    } catch (e: Exception) {
        onError("获取位置失败: ${e.message}")
    }
}

private fun processLocation(
    latitude: Double,
    longitude: Double,
    geocoder: Geocoder,
    onLocationResult: (String, Double, Double) -> Unit
) {
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val addressText = buildString {
                if (!address.subLocality.isNullOrEmpty()) {
                    append(address.subLocality)
                    append(" ")
                }
                if (!address.thoroughfare.isNullOrEmpty()) {
                    append(address.thoroughfare)
                    append(" ")
                }
                if (!address.featureName.isNullOrEmpty()) {
                    append(address.featureName)
                }
            }.trim()

            onLocationResult(
                addressText.ifEmpty { "${latitude}, ${longitude}" },
                latitude,
                longitude
            )
        } else {
            onLocationResult("${latitude}, ${longitude}", latitude, longitude)
        }
    } catch (e: Exception) {
        onLocationResult("${latitude}, ${longitude}", latitude, longitude)
    }
} 