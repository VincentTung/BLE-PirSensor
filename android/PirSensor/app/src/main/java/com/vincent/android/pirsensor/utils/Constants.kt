package com.vincent.android.pirsensor.utils


const val LOG_TAG = "pir_sensor"

/**
 * 打开蓝牙 request code
 */
const val REQUEST_ENABLE_BLUETOOTH = 0x16
/**
 * 蓝牙设备地址
 */
const val PIR_DEVICE_ADDRESS = "D8:A0:1D:55:66:3A"
/**
 * 蓝牙设备名称
 */
const val PIR_DEVICE_NAME = "PIR_SENSOR"

/**
 * 服务UUID
 */
const val PIR_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
/**
 * 特征UUID
 */
const val PIR_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

/**
 * 红外检测到人的值
 */
const val BLINK_VALUE = "1"

/**
 * 闪烁时间
 */
const val BLINK_DURATION = 200L
/**
 * 闪烁的次数
 */
const val BLINK_REPEAT_COUNT = 2