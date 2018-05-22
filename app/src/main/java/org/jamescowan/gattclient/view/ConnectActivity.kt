package org.jamescowan.gattclient.view

import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.jamescowan.gattclient.R
import org.jamescowan.gattclient.Constants
import org.jamescowan.gattclient.client.GattClient
import org.jamescowan.gattclient.client.IGattClient
import org.jamescowan.gattclient.client.IGattClientListener
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class ConnectActivity : AppCompatActivity() {

    private lateinit var gattClient: IGattClient
    private var connected = false;
    private lateinit var view: ConnectActivityUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ConnectActivity.ConnectActivityUI()
        view.setContentView(this)

        val device: BluetoothDevice = intent.getParcelableExtra(Constants.BLUETOOTH_ARG);
        val name: String = if (device.name != null) device.name else device.address

        setTitle(name)

        gattClient = GattClient(Constants.TIME_SERVICE, Constants.CURRENT_TIME, object : IGattClientListener {
            override fun isConnected() {
                connected = true
                updateConnectionStatus()
                gattClient.read()
            }

            override fun isClosed() {
                connected = false
                updateConnectionStatus()
            }

            override fun response(message: String) {
                updateTime(message)
            }
        })
        gattClient.connect(this, device)
    }

    override fun onDestroy() {
        super.onDestroy()

        gattClient.close()
    }

    private fun getConnectionStatus(): String {
        return if (connected) "Connected" else "Not connected"
    }

    private fun updateConnectionStatus() {
        this@ConnectActivity.runOnUiThread(Runnable {
            view.status.text = getConnectionStatus()
        });
    }

    private fun updateTime(text: String) {
        this@ConnectActivity.runOnUiThread(Runnable {
            view.time.setText(text)
        });
    }

    // inner classes
    // inner UI class

    class ConnectActivityUI() : AnkoComponent<ConnectActivity> {
        lateinit var time: TextView
        lateinit var status: TextView

        override fun createView(ui: AnkoContext<ConnectActivity>) = with(ui) {

            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                status = textView(owner.getConnectionStatus()) {
                    gravity = Gravity.CENTER
                    padding = 15
                    textSize = 25f
                    typeface = Typeface.DEFAULT_BOLD
                }.lparams(width = matchParent, height = wrapContent)

                view {
                    backgroundColor = Color.BLACK
                }.lparams(width = matchParent, height = dip(1))

                view {
                }.lparams(width = matchParent, height = dip(5))

                textView("Time") {
                    gravity = Gravity.CENTER
                    padding = 15
                    textSize = 25f
                    typeface = Typeface.DEFAULT_BOLD
                }.lparams(width = matchParent, height = wrapContent)

                view {
                }.lparams(width = matchParent, height = dip(3))

                time = textView("") {
                    gravity = Gravity.CENTER
                    padding = 15
                    textSize = 25f
                    typeface = Typeface.DEFAULT_BOLD
                }.lparams(width = matchParent, height = wrapContent)

                view {
                }.lparams(width = matchParent, height = dip(3))

                button(text = "Quit") {

                    onClick {
                        owner.finish();
                    }
                }.lparams(width = matchParent, height = wrapContent)
            }
        }
    }

}