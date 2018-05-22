package org.jamescowan.gattclient.client

import android.bluetooth.BluetoothDevice
import android.content.Context

interface IGattClient {
    fun close()
    fun connect(context: Context, device: BluetoothDevice)
    fun read():Boolean
}