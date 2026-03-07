package com.lym.quietmind.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 设备姿态监听器 (Device Orientation Tracker)
 * 监听重力传感器，判断设备当前是否被翻转倒扣在桌面上。
 */
class DeviceOrientationTracker(private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /**
     * @return Flow<Boolean> 发送设备是否面朝下（Face Down）。
     * true = 屏幕朝下倒扣 (Z 轴加速度大约为 -9.8)
     * false = 屏幕面朝上 (Z 轴大约为 9.8)
     */
    fun getFaceDownFlow(): Flow<Boolean> = callbackFlow {
        if (accelerometer == null) {
            // 设备无重力加速度传感器（如虚拟机某些环境）
            // 返回默认 false 或者抛出异常
            trySend(false)
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val zAxis = event.values[2]
                    
                    // 地心引力 G ≈ 9.8。
                    // 屏幕朝上放置在桌面时，Z 轴加速度应该在 +9.8 左右。
                    // 屏幕朝下放置在桌面时，Z 轴加速度应该在 -9.8 左右。
                    // 这里设定一个宽松的阈值：-7.0 以下即可认为被倒扣。
                    val isFaceDown = zAxis < -7.0f
                    
                    trySend(isFaceDown)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed
            }
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI // 采用 UI 级别的频率避免过度耗电
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
