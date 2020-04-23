package com.vincent.android.pirsensor

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.vincent.android.pirsensor.utils.*
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {
    private var mAnimator: ObjectAnimator? = null
    private val mRxPermission = RxPermissions(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        var bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            showToast(R.string.device_not_support_bluetooth)
        } else {
            if (!bluetoothAdapter.isEnabled) {
                turnOnBluetooth()
            } else {
                checkLocationPermission()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                checkLocationPermission()
            } else {
                showToast(R.string.trun_on_failed)
            }
        }
    }

    private fun checkLocationPermission() {
        if (!mRxPermission.isGranted(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            mRxPermission.request(android.Manifest.permission.ACCESS_FINE_LOCATION).subscribe(Consumer { isGranted ->
                if (isGranted) {
                    findPirDevice()
                } else {
                    showToast(R.string.require_premission_failed)
                }

            })

        } else {
            findPirDevice()
        }
    }


    private fun turnOnBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    private fun findPirDevice() {

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    if (result?.device?.address == PIR_DEVICE_ADDRESS) {
                        bluetoothLeScanner?.stopScan(this)
                        result.device?.apply {
                            connectDevice(this)
                        }

                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    showToast(R.string.find_no_device)
                }
            }
            bluetoothLeScanner?.startScan(scanCallback)
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        device.connectGatt(this, true, object : BluetoothGattCallback() {

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                runOnUiThread {
                    when (newState) {
                        BluetoothProfile.STATE_CONNECTED -> {
                            showToast(R.string.connected)
                            gatt?.discoverServices()?.apply {
                                if (this) {
                                    showToast(R.string.discover_services)
                                }
                            }

                        }
                        BluetoothProfile.STATE_DISCONNECTED -> showToast(R.string.disconnected)
                        BluetoothProfile.STATE_CONNECTING -> showToast(R.string.connecting)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)

                gatt?.services?.apply {
                    this.forEach { service ->
                        if (service.uuid.toString() == PIR_SERVICE_UUID) {
                            checkCharacteristic(gatt, service)
                            return@forEach
                        }
                    }
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
                val value = characteristic?.value?.toString(Charsets.UTF_8)
                value?.apply {
                    if (this == BLINK_VALUE) {
                        runOnUiThread {
                            blinkScreen()
                        }
                    }
                }
            }
        })
    }

    /**
     *
     */
    private fun checkCharacteristic(gatt: BluetoothGatt, service: BluetoothGattService?) {

        service?.characteristics?.forEach { characteristic ->
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0 && characteristic.uuid.toString() == PIR_CHARACTERISTIC_UUID) {
                gatt.setCharacteristicNotification(characteristic, true)
                showToast(R.string.subscribe_success)
                return@forEach
            }

        }


    }

    private fun blinkScreen() {
        if (mAnimator != null) {
            mAnimator!!.cancel()
            mAnimator = null
        }
        mAnimator = ObjectAnimator.ofInt(ll_container, "backgroundColor", Color.WHITE, Color.RED, Color.WHITE)
        mAnimator!!.apply {
            duration = BLINK_DURATION
            setEvaluator(ArgbEvaluator())
            repeatMode = ValueAnimator.REVERSE
            repeatCount = BLINK_REPEAT_COUNT
            start()

        }

    }

    private fun showToast(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(stringId: Int) {
        Toast.makeText(this@MainActivity, stringId, Toast.LENGTH_SHORT).show()
    }
}
