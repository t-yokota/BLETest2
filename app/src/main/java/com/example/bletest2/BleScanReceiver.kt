package com.example.bletest2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BleScanReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("ble_test", intent.toString())
        Log.i("ble_test", "extras = "+intent?.extras.toString())
//        main()
    }

    private fun main() = runBlocking {
        launch {
            doWorld()
            Log.i("ble_test", "Done.")
        }
        Log.i("ble_test", "Hello")
    }

    private suspend fun doWorld(){
        delay(1000L)
        Log.i("ble_test", "World!")
    }
}