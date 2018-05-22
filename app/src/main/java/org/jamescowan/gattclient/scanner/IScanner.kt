package org.jamescowan.gattclient.scanner

import java.util.UUID

interface IScanner {
    fun startScan(serviceUUID: UUID)
    fun stopScan()
}