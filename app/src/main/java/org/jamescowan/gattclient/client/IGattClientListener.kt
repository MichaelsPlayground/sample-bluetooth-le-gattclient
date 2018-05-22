package org.jamescowan.gattclient.client

interface IGattClientListener {
    fun isConnected()
    fun isClosed()
    fun response(message:String)
}