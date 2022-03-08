package com.example.bletest2

import android.Manifest
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bletest2.databinding.ActivityMainBinding

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 3
private const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 5
private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 7
private const val PENDING_INTENT_REQUEST_CODE = 9

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .build()

    private var filters: List<ScanFilter> = listOf(
        ScanFilter.Builder()
            .setDeviceName("Feather nRF52840 Express")
            .build()
    )

    private val isLocationPermissionGranted
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) and hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    private val isBlePermissionGranted
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) and hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            true
        }

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }else if(!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.i("ble_test","Fine location permission is required.")
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                Log.i("ble_test","Background location permission is required.")
                if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
                }
            }
        }
    }

    private fun requestBlePermission() {
        if (isBlePermissionGranted) {
            return
        } else {
            Log.i("ble_test","Bluetooth permission is required.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT),
                    BLUETOOTH_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    Log.i("ble_test","Denied adding FINE_LOCATION permission.")
                } else {
                    Log.i("ble_test","Applied adding FINE_LOCATION permission.")
                    requestLocationPermission()
                }
            }
            BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    Log.i("ble_test","Denied adding BACKGROUND_LOCATION permission.")
                } else {
                    Log.i("ble_test","Applied adding BACKGROUND_LOCATION permission.")
                }
            }
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    Log.i("ble_test","Denied adding BLUETOOTH_SCAN/CONNECT permission.")
                } else {
                    Log.i("ble_test","Applied adding BLUETOOTH_SCAN/CONNECT permission.")
                }
            }
        }
    }

//    private fun promptEnableBluetooth() {
//        if (!bluetoothAdapter.isEnabled) {
//            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            ENABLE_BLUETOOTH_REQUEST_CODE -> {
//                if (resultCode != RESULT_OK) {
//                    Log.i("ble_test","BLE is not enabled.")
//                } else {
//                    Log.i("ble_test","BLE is enabled.")
//                }
//            }
//        }
//    }

    private fun startBleScan() {
        Log.i("ble_test", "Started scan using PendingIntent ...")
        bleScanner.startScan(filters, scanSettings, getPendingIntent())
    }

    private fun stopBleScan() {
        Log.i("ble_test", "Stopped scan.")
        bleScanner.stopScan(getPendingIntent())
    }

    private fun getPendingIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            this,
            PENDING_INTENT_REQUEST_CODE,
            Intent(applicationContext, BleScanReceiver::class.java),
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
    }

//    private fun getPendingIntent(): PendingIntent {
//        return PendingIntent.getService(
//            this,
//            PENDING_INTENT_REQUEST_CODE,
//            Intent(applicationContext, BleScanService::class.java),
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        var isScanning = false
        binding.scanningState.text = getString(R.string.scanningStateFalse)
        binding.button.text = getString(R.string.startButton)

        binding.button.setOnClickListener {
            if (isScanning) {
                if (!isLocationPermissionGranted) {
                    requestLocationPermission()
                } else if (!isBlePermissionGranted){
                    requestBlePermission()
                } else {
                    binding.button.text = getString(R.string.startButton)
                    binding.scanningState.text = getString(R.string.scanningStateFalse)
                    stopBleScan()
                    isScanning = false
                }
            }else{
                if (!isLocationPermissionGranted) {
                    requestLocationPermission()
                } else if (!isBlePermissionGranted){
                    requestBlePermission()
                } else {
                    binding.button.text = getString(R.string.stopButton)
                    binding.scanningState.text = getString(R.string.scanningStateTrue)
                    startBleScan()
                    isScanning = true
                }
            }
        }
    }
}