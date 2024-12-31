package com.heroesports.heroci.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "LocationUtils"

@SuppressLint("MissingPermission")
fun getLocation(
    context: Context,
    geocoder: Geocoder,
    onLocationResult: (String, Double, Double) -> Unit,
    onError: (String) -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGettingLocation = AtomicBoolean(true)

    // 检查GPS是否开启
    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    if (!isGpsEnabled && !isNetworkEnabled) {
        Log.e(TAG, "GPS和网络定位都未开启")
        onError("请开启GPS或网络定位")
        return
    }

    Log.d(TAG, "开始获取位置 GPS=${isGpsEnabled}, Network=${isNetworkEnabled}")

    // 位置监听器
    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (isGettingLocation.get()) {
                Log.d(TAG, "获取到位置更新: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}")
                isGettingLocation.set(false)
                locationManager.removeUpdates(this)
                processLocation(location, geocoder, onLocationResult)
            }
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "位置提供者已启用: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "位置提供者已禁用: $provider")
            if (isGettingLocation.get()) {
                if (provider == LocationManager.GPS_PROVIDER && !isNetworkEnabled) {
                    onError("GPS已禁用，请开启位置服务")
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d(TAG, "位置提供者状态改变: $provider, status=$status")
        }
    }

    try {
        // 先尝试获取最后已知位置
        val lastKnownLocation = if (isNetworkEnabled) {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } else if (isGpsEnabled) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else null

        lastKnownLocation?.let { location ->
            Log.d(TAG, "获取到最后已知位置")
            isGettingLocation.set(false)
            processLocation(location, geocoder, onLocationResult)
            return
        }

        // 如果没有最后已知位置，开始请求位置更新
        Log.d(TAG, "开始请求位置更新")
        
        // 优先使用网络定位
        if (isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                0f,
                locationListener
            )
            Log.d(TAG, "已请求网络定位更新")
        }
        
        // 同时也使用GPS定位
        if (isGpsEnabled) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )
            Log.d(TAG, "已请求GPS定位更新")
        }

        // 设置超时
        CoroutineScope(Dispatchers.Main).launch {
            delay(20000) // 20秒超时
            if (isGettingLocation.get()) {
                Log.d(TAG, "位置获取超时")
                isGettingLocation.set(false)
                locationManager.removeUpdates(locationListener)
                onError("获取位置超时，请重试或检查GPS信号")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "请求位置更新时出错", e)
        onError("获取位置信息失败，请检查定位权限")
    }
}

private fun processLocation(
    location: Location,
    geocoder: Geocoder,
    onLocationResult: (String, Double, Double) -> Unit
) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "使用新版API获取地址")
            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            ) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    Log.d(TAG, "获取到地址: $address")
                    val addressText = buildString {
                        if (!address.locality.isNullOrEmpty()) {
                            append(address.locality)
                            Log.d(TAG, "城市: ${address.locality}")
                        }
                        if (!address.subLocality.isNullOrEmpty()) {
                            append(address.subLocality)
                            Log.d(TAG, "区域: ${address.subLocality}")
                        }
                        if (!address.thoroughfare.isNullOrEmpty()) {
                            append(address.thoroughfare)
                            Log.d(TAG, "街道: ${address.thoroughfare}")
                        }
                        if (!address.featureName.isNullOrEmpty()) {
                            append(address.featureName)
                            Log.d(TAG, "地标: ${address.featureName}")
                        }
                    }
                    if (addressText.isNotEmpty()) {
                        Log.d(TAG, "最终地址: $addressText")
                        onLocationResult(addressText, location.latitude, location.longitude)
                    } else {
                        Log.d(TAG, "地址为空，使用坐标")
                        onLocationResult(
                            "${location.latitude}, ${location.longitude}",
                            location.latitude,
                            location.longitude
                        )
                    }
                } else {
                    Log.d(TAG, "未获取到地址信息")
                    onLocationResult(
                        "${location.latitude}, ${location.longitude}",
                        location.latitude,
                        location.longitude
                    )
                }
            }
        } else {
            Log.d(TAG, "使用旧版API获取地址")
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                Log.d(TAG, "获取到地址: $address")
                val addressText = buildString {
                    if (!address.locality.isNullOrEmpty()) {
                        append(address.locality)
                        Log.d(TAG, "城市: ${address.locality}")
                    }
                    if (!address.subLocality.isNullOrEmpty()) {
                        append(address.subLocality)
                        Log.d(TAG, "区域: ${address.subLocality}")
                    }
                    if (!address.thoroughfare.isNullOrEmpty()) {
                        append(address.thoroughfare)
                        Log.d(TAG, "街道: ${address.thoroughfare}")
                    }
                    if (!address.featureName.isNullOrEmpty()) {
                        append(address.featureName)
                        Log.d(TAG, "地标: ${address.featureName}")
                    }
                }
                if (addressText.isNotEmpty()) {
                    Log.d(TAG, "最终地址: $addressText")
                    onLocationResult(addressText, location.latitude, location.longitude)
                } else {
                    Log.d(TAG, "地址为空，使用坐标")
                    onLocationResult(
                        "${location.latitude}, ${location.longitude}",
                        location.latitude,
                        location.longitude
                    )
                }
            } else {
                Log.d(TAG, "未获取到地址信息")
                onLocationResult(
                    "${location.latitude}, ${location.longitude}",
                    location.latitude,
                    location.longitude
                )
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "获取地址时出错", e)
        onLocationResult(
            "${location.latitude}, ${location.longitude}",
            location.latitude,
            location.longitude
        )
    }
} 