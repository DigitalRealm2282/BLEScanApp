package com.ats.apple.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ats.apple.MainActivity

interface Connectable {
    val bluetoothGattCallback: BluetoothGattCallback

    fun broadcastUpdate(action: String) =
        LocalBroadcastManager.getInstance(MainActivity().applicationContext)
            .sendBroadcast(Intent(action))

    @SuppressLint("MissingPermission")
    fun disconnect(gatt: BluetoothGatt?) {
        gatt?.disconnect()
        gatt?.close()
    }
}