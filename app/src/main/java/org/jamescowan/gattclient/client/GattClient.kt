package org.jamescowan.gattclient.client

import android.bluetooth.*
import android.content.Context
import timber.log.Timber
import java.util.*

class GattClient(serviceUUID: UUID, characteristicUUID: UUID, listener: IGattClientListener) : IGattClient {

    private var characteristicUUID: UUID
    private lateinit var characteristic: BluetoothGattCharacteristic
    private lateinit var connection: BluetoothGatt
    private var connected = false;
    private var listener: IGattClientListener
    private var serviceUUID: UUID

    init {
        this.serviceUUID = serviceUUID
        this.characteristicUUID = characteristicUUID
        this.listener = listener
    }

    override public fun close() {
        if (connected) {
            connection.disconnect()
            connected = false;
        }
    }

    override public fun connect(context: Context, device: BluetoothDevice) {
        connection = device.connectGatt(context, false, GattCallback())
    }

    override public fun read(): Boolean {

        if (!connected) {
            return false;
        }

        connection.readCharacteristic(characteristic)
        return true;
    }

    private fun disconnect(gatt: BluetoothGatt) {
        gatt.disconnect()
        connected = false;
        listener.isClosed()
    }

    private fun findCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        if (gatt.getService(serviceUUID) == null) {
            return null
        }
        return gatt.getService(serviceUUID).getCharacteristic(characteristicUUID)
    }

    private inner class GattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState);

            when {
                newState == BluetoothProfile.STATE_DISCONNECTED -> {
                    gatt.close()
                    Timber.i("Disconnected " + gatt.device.address)
                    listener.isClosed()
                }
                newState == BluetoothProfile.STATE_CONNECTED -> {
                    Timber.i("Connected " + gatt.device.address)
                    gatt.discoverServices();
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            val value:ByteArray = characteristic.value
            val message:String = String(bytes=value)

            Timber.i("onCharacteristicChanged value " + message)

            listener.response(message)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status);

            val value:ByteArray = characteristic.value
            val message:String = String(bytes=value)

            Timber.i("onCharacteristicRead value " + message)

            listener.response(message)
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Timber.e("Device service discovery unsuccessful, status $status")
                disconnect(gatt)
                return
            }

            var characteristic: BluetoothGattCharacteristic? = findCharacteristic(gatt)

            if (characteristic == null) {
                Timber.e("Unable to find characteristic.")
                disconnect(gatt);
                return;
            }

            val characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true)

            if (!characteristicWriteSuccess) {
                Timber.e("Cannot initialise characteristic")
                return
            }

            for (descriptor in characteristic.descriptors) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }

            Timber.i("Characteristic initialised")
            this@GattClient.connected = true;
            this@GattClient.characteristic = characteristic;
            listener.isConnected()
        }
    }

}