package org.jamescowan.gattclient

import java.util.*

class Constants {
    companion object {
        const val BLUETOOTH_ARG = "BLUETOOTH_ARG"
        val TIME_SERVICE: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
        /* Mandatory Current Time Information Characteristic */
        val CURRENT_TIME: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
        /* Optional Local Time Information Characteristic */
        val LOCAL_TIME_INFO: UUID = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")
        /* Mandatory Client Characteristic Config Descriptor */
        val CLIENT_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }
}