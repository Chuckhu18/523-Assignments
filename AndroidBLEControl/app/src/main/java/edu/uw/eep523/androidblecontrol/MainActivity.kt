package edu.uw.eep523.androidblecontrol


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

//const val DEVICE_NAME = "0_0_summer_is_comming_0_0"
const val DEVICE_NAME = "bluet"
const val EMERGENCY_CONTACT = "2093183388"

class MainActivity : AppCompatActivity(), BLEControl.Callback {


    // Bluetooth
    private var ble: BLEControl? = null
    private var messages: TextView? = null
    private var rssiAverage:Double = 0.0

    private var digStatus : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        val adapter: BluetoothAdapter?
        adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            if (!adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)

            }        }

        // Get Bluetooth
        messages = findViewById(R.id.bluetoothText)
        messages!!.movementMethod = ScrollingMovementMethod()
        ble = BLEControl(applicationContext, DEVICE_NAME)

        // Check permissions
        ActivityCompat.requestPermissions(this,
                arrayOf( Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)


    }
    //Function that reads the RSSI value associated with the bluetooth connection between the phone and the Arudino board
    //If you use the RSSI to calculate distance, you may want to record a set of values over a period of time
    //and obtain the average
    override fun onRSSIread(uart:BLEControl,rssi:Int){
        rssiAverage = rssi.toDouble()
        writeLine("RSSI $rssiAverage")
    }
    fun getRSSI (v:View){
        ble!!.getRSSI()
    }

    fun clearText (v:View){
        messages!!.text=""

    }

    override fun onResume() {
        super.onResume()
        //updateButtons(false)
        digStatus = false
        ble!!.registerCallback(this)

    }

    override fun onStop() {
        super.onStop()
        ble!!.unregisterCallback(this)
        ble!!.disconnect()
    }

    fun connect(v: View) {
        startScan()
    }

    private fun startScan() {
        writeLine("Scanning for devices ...")
        ble!!.connectFirstAvailable()
    }


    /**
     * Press button to receive the temperature value form the board
     */
    fun buttTouch(v: View) {
        ble!!.send("readtemp")
        Log.i("BLE", "READ TEMP")
    }

    /**
     * Press button to set the lEDs to color red (see arduino code)
     */
    fun buttRed(v: View) {
        ble!!.send("confirm")
        Log.i("BLE", "SEND Confirm")
    }


    /**
     * Writes a line to the messages textbox
     * @param text: the text that you want to write
     */
    private fun writeLine(text: CharSequence) {
        runOnUiThread {
            messages!!.append(text)
            messages!!.append("\n")
        }
    }

    /**
     * Called when a UART device is discovered (after calling startScan)
     * @param device: the BLE device
     */
    override fun onDeviceFound(device: BluetoothDevice) {
        writeLine("Found device : " + device.name)
        writeLine("Waiting for a connection ...")
    }

    /**
     * Prints the devices information
     */
    override fun onDeviceInfoAvailable() {
        writeLine(ble!!.deviceInfo)
    }

    /**
     * Called when UART device is connected and ready to send/receive data
     * @param ble: the BLE UART object
     */
    override fun onConnected(ble: BLEControl) {
        writeLine("Connected!")

    }

    /**
     * Called when some error occurred which prevented UART connection from completing
     * @param ble: the BLE UART object
     */
    override fun onConnectFailed(ble: BLEControl) {
        writeLine("Error connecting to device!")
    }

    /**
     * Called when the UART device disconnected
     * @param ble: the BLE UART object
     */
    override fun onDisconnected(ble: BLEControl) {
        writeLine("Disconnected!")
    }

    /**
     * Called when data is received by the UART
     * @param ble: the BLE UART object
     * @param rx: the received characteristic
     */
    override fun onReceive(ble: BLEControl, rx: BluetoothGattCharacteristic) {
        writeLine("Received value: " + rx.getStringValue(0))
//        Log.d("TAG", rx.getStringValue(0))
        if(rx.getStringValue(0) == "doorOpened" && !digStatus){
            Log.d("TAG", "in doorOpen")
            Log.d("TAG", rx.getStringValue(0))

            runOnUiThread {
                showDialog()
            }
            digStatus = true
        }
        if(rx.getStringValue(0) == "noResponse") {
            digStatus = false
            try {
                val uri: String = "tel:" + EMERGENCY_CONTACT
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse(uri))
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showDialog(){
        Log.d("TAG", "in showDialog")
        val alertDialog: AlertDialog? = this?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply { setPositiveButton(
                "Cancel Alert",
                DialogInterface.OnClickListener { dialog, id -> diaClick()
                    //user press the positive button
                })
//            setNegativeButton("Cancel Alert",
//                DialogInterface.OnClickListener { dialog, id -> diaClick()
//                    // User cancelled the dialog
//                })
            } // Set other dialog properties
             .setMessage("Do you want to cancel the arlarm")
             .setTitle("Arduino ALARM")
            // Create the AlertDialog
            builder.create()
        }

        Log.d("TAG", "before show")
        alertDialog?.show()
    }

    fun diaClick(){
        Log.d("TAG", "onClickDig")
        ble!!.send("cancel")
        digStatus = false
    }

    companion object {
        private val REQUEST_ENABLE_BT = 0
    }
}
