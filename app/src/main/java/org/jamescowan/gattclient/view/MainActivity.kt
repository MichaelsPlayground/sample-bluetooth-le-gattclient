package org.jamescowan.gattclient.view

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.Toast
import org.jamescowan.gattclient.Constants
import org.jamescowan.gattclient.scanner.IScanner
import org.jamescowan.gattclient.scanner.IScannerListener
import org.jamescowan.gattclient.scanner.Scanner
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {

    private val devices: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var scanner: IScanner
    private val timer = Timer()
    private lateinit var view: MainActivityUI
    
    private val START = "Start scan"
    private val STOP = "Stop scan"
    private val REQUEST_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = MainActivityUI()
        view.setContentView(this)

        setTitle("Devices")

        if (checkPermissions()) {
            initScanner()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initScanner();
                }
                else {
                    Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_LONG).show()
                    finish()
                }
                return
            }
        }
    }

    public fun connect(device: BluetoothDevice) {
        val intent = Intent(this, ConnectActivity::class.java)

        intent.putExtra(Constants.BLUETOOTH_ARG, device)
        startActivity(intent)
    }

    public fun getDevices(): List<BluetoothDevice> {
        return devices;
    }

    private fun checkPermissions():Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_PERMISSION)
            return false;
        }
        else {
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show()
            return true
        }
    }

    private fun handleScanButton() {
        if (view.scanButton.text == STOP) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun initScanner() {
        try {
            scanner = Scanner(this, object : IScannerListener {
                override fun foundDevice(device: BluetoothDevice) {
                    devices.add(device)
                    view.deviceListAdapter.rebuild()
                }
            })
            Timber.i("Bluetooth initialised")
            startScan()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun startScan() {
        view.scanButton.text = STOP

        scanner.startScan(Constants.TIME_SERVICE)
        devices.clear()
        view.deviceListAdapter.rebuild()

        timer.schedule(object : TimerTask() {
            override fun run() {
                this@MainActivity.runOnUiThread(Runnable {
                    stopScan()
                });
            }
        }, 1000 * 30)
    }

    private fun stopScan() {
        view.scanButton.text = START
        scanner.stopScan()
    }

    // inner UI class

    class MainActivityUI() : AnkoComponent<MainActivity> {
        lateinit var deviceListAdapter: DeviceListAdapter
        lateinit var scanButton: Button

        override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
            deviceListAdapter = DeviceListAdapter(owner)

            verticalLayout {

                lparams(width = matchParent, height = matchParent) {
                    weightSum = 1F
                }

                verticalLayout {
                    listView {
                        adapter = deviceListAdapter
                    }
                }.lparams(width = matchParent, height = 0, weight = 0.90F)

                verticalLayout {
                    scanButton = button(text = "") {

                        onClick {
                            owner.handleScanButton();
                        }
                    }
                }.lparams(width = matchParent, height = 0, weight = 0.10F)
            }
        }
    }

}

class DeviceListAdapter(val activity: MainActivity) : BaseAdapter() {

    override fun getView(i: Int, v: View?, parent: ViewGroup?): View {
        val device = getItem(i)
        return with(parent!!.context) {
            linearLayout {
                lparams(width = matchParent, height = wrapContent) {
                    weightSum = 2F
                }
                textView(device.address) {
                    textSize = 20f
                    padding = dip(20)
                }.lparams(weight = 1F)
                button(text = "Connect") {
                    padding = dip(20)
                    onClick {
                        activity.connect(device);
                    }
                }.lparams(width = wrapContent)
            }
        }
    }

    override fun getCount(): Int {
        return activity.getDevices().size
    }

    override fun getItem(position: Int): BluetoothDevice {
        return activity.getDevices().get(position)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).address.hashCode().toLong()
    }

    fun rebuild() {
        notifyDataSetChanged()
    }
}

