package org.jamescowan.gattclient.scanner

import android.bluetooth.BluetoothDevice

interface IScannerListener {
    fun foundDevice(device:BluetoothDevice)
}