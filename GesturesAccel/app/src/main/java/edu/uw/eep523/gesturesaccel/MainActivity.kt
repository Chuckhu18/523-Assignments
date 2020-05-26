package edu.uw.eep523.gesturesaccel


import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

// Label 0 indicates WING
// Label 1 indicates Unrecognized gesture

   const val TAG = "GestureClassifier"
   const val FLOAT_TYPE_SIZE = 4

   // ********** TO DO ************
    const val MODEL_FILE = "lastFirst.tflite"
    const val OUTPUT_CLASSES_COUNT = 2
    const val MAX_SAMPLES = 128
// ******************************


class MainActivity : AppCompatActivity() , SensorEventListener {

    private var gestureClassifier = GestureClassifier(this)
    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensor: Sensor
    private val linear_acceleration: Array<Float> = arrayOf(0.0f,0.0f,0.0f)
    private val gravity: Array<Float> = arrayOf(0.0f,0.0f,0.0f)

    var nsamples = 0
    val capturedData = FloatArray(MAX_SAMPLES*3) { i -> 0f}
    private var inferenceResult = ""

    private var counter = 0
    private var k = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialize_accel_sensor()

        // Setup digit classifier
        gestureClassifier.initializeInterpreter()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        //To change body of created functions use File | Settings | File Templates.
        // Do something here if sensor accuracy changes.
    }

    private fun initialize_accel_sensor(){
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            // Sorry, there are no accelerometers on your device.
            null!!
        }

        mSensorManager.registerListener(this, mSensor, 40000)
    }


    //  ``````````` TO DO ````````````````````````
    //1. Capture samples for some time and fill an array with the values
    //2. Stop capturing data when we have enough samples (unregister the listener)
    //3 . Call the model to predict the result
    //4 . Display the prediction result
    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER)
            return
        // Isolate the force of gravity with the low-pass filter.
        val alpha: Float = 0.8f

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

        //1. Capture samples for some time and fill an array with the values
        // Remove the gravity contribution with the high-pass filter.
        linear_acceleration[0] = event.values[0] - gravity[0]
        linear_acceleration[1] = event.values[1] - gravity[1]
        linear_acceleration[2] = event.values[2] - gravity[2]

        acc_data.text = "i:$counter\n" + "x:${linear_acceleration[0]*1000}\n" +
                        "y:${linear_acceleration[1]*1000}\n" +
                        "z:${linear_acceleration[2]*1000}\n"

        //save data into an array to make inference about the gesture
        //pay attention to the units
        //capturedData.set(Whichposition, WhichValue) // repeat for x, y, z
        if (k >= 128*3)
            k = 0
        for(i in 0..2){
            capturedData[k] = linear_acceleration[i] * 1000
//            Log.d(TAG,"k$k, $i")
            k++
        }

//        Log.d(TAG, "k:$k"+ ",x:"+capturedData[k-2].toString()+
//                ",y:"+capturedData[k-1].toString()+
//                ",z:"+capturedData[k].toString())

        //2. Stop capturing data when we have "enough" samples
      //  if(***condition to stop recording data is satisfied ****){
        counter++
        if(counter >= MAX_SAMPLES){
            counter = 0
            // Unregister listener
            mSensorManager.unregisterListener(this)
            //3. Call the model to get the predicted result
            inferenceResult = gestureClassifier.classify(capturedData)

            // Display the prediction result
            result_view.text = inferenceResult

        }
    }

    fun make_prediction(view: View){
        //Register the accelerometer listener to start recording data
        initialize_accel_sensor()
//        mSensorManager.registerListener(this, mSensor, 40000)
    }

    fun clear_prediction(view:View){
        result_view.text = ""
        mSensorManager.unregisterListener(this)
        k = 0
        counter=0
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorManager.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
        k = 0
        counter=0
    }

    override fun onStop() {
        super.onStop()
        mSensorManager.unregisterListener(this)
        k = 0
        counter=0
    }
}
