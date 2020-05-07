package edu.uw.eep523.assignment_3

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensorA: Sensor

    private val SHAKE_THRESHOLD = 3.25
    private val WIDS_SIZE = 20
    private var windowSlide = DoubleArray(WIDS_SIZE)
//    private var pullUpsWid = DoubleArray(WIDS_SIZE)
    private val pullUpsWid: Queue<Double> = LinkedList<Double>()

    private var pullUpCounter : Int = 0
    private var counter:Int = 0
    private var counter2:Int = 0
    private var timeCounter:Int = 0

    private var startFlag = false
    private var isFreeMode = true

    private var mediaPlayer: MediaPlayer? = null


    lateinit var editText: EditText
    var goal:String = "-1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("TAG", "onCreate")
        editText = findViewById(R.id.editText)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mSensorA = if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        } else {
            // Sorry, there are no accelerometers on your device.
            null!!
        }

        mSensorManager.registerListener(this, mSensorA, 40000)

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //To change body of created functions use File | Settings | File Templates.
        // Do something here if sensor accuracy changes.
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
//        Log.d("TAG", "onSensorChanged")

        val alpha= 0.8f
        var x: Float = event.values[0]
        var y: Float = event.values[1]
        var z: Float = event.values[2]

        //show xyz on the screen
        acc_data.text = "x:${event.values[0]}\n" +
                "y:${event.values[1]}\n" +
                "z:${event.values[2]}\n"

        val acceleration = sqrt(
            x.toDouble().pow(2.0) +
                    y.toDouble().pow(2.0) +
                    z.toDouble().pow(2.0)
        ) - SensorManager.GRAVITY_EARTH

        //Shaking status
        if(counter < WIDS_SIZE){
            windowSlide[counter] = acceleration
        }
//        Log.d("TAG", "counter: $counter")
        counter++
        if(counter == WIDS_SIZE) {
            counter = 0
            Log.d("DATA", "x:"+ windowSlide.contentToString())

            var accAvg = windowSlide.average()
//            Log.d("TAG", "Avg acceleration: " + accAvg + "m/s^2")
            if(accAvg >= SHAKE_THRESHOLD) {
                if(isFreeMode)
                    textView4.text =  "Free mode Started!"
                else textView4.text =  "Started!"
                startFlag = true
            }
        }

        //Pull-Ups status
        var calArray = DoubleArray(80)

        if(counter2 < 80){
            pullUpsWid.add(y.toDouble())
        }else{
            pullUpsWid.remove()
            pullUpsWid.add(y.toDouble())

            var temp:Int = 0
            for(i in pullUpsWid){
                calArray[temp] = i
                temp++
            }
        }
        counter2++
        timeCounter++
//        Log.d("TAG", "timeCounter: $timeCounter")
        if(startFlag && timeCounter>=80) {
            timeCounter = 0
            startCounting(calArray, 80)
//            startFlag = false
        }
    }

    private fun startCounting(wid: DoubleArray, freq: Int){
//        Log.d("TAG", "counting")
        // Distance Calculation
        var speedArr = DoubleArray(freq)
        var distArr = DoubleArray(freq)
        var k = 0
        var temp = 0.0
        for(i in wid){
            speedArr[k] = (i+temp)/freq
            temp = i
            k++
        }
        temp = 0.0
        k = 0
        for(i in speedArr){
            distArr[k] = (temp+i)/(2*freq)
            k++
            temp = i
        }
//        var totalDist = distArr.sum()
        var totalDist = 0.0
        for(i in distArr){
            totalDist += i.absoluteValue
        }
        Log.d("TEST", "totalDist: $totalDist")

        var low:Double = 999.9
        var high:Double = -999.9
        for(i in wid) {
            if (i < low)
                low = i
            if (i > high)
                high = i
        }

        var diff:Double = abs(low-high)
        Log.d("TAG", "diffY: $diff")

        if(diff > 21) {
//            Log.d("TAG", "diffY: $diff")
            pullUpCounter++
            Log.d("TAG", "PCounter: $pullUpCounter")
            textView.text = pullUpCounter.toString()
        }

        if(pullUpCounter.toString() == goal){
            Log.d("TAG", "goal!: $diff")
            playBGM()
        }
    }

    private fun playBGM(){
        Log.d("TAG", "playBGM called")
        if(!isFreeMode) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bikehornsound)
            mediaPlayer?.start();
        }
    }

    fun goalMode(view: View){
        isFreeMode = false
        textView.text = "0"
        pullUpCounter = 0
        startFlag = false
        textView4.text = "shake to start"
        goal = editText.text.toString()
        Toast.makeText(this, "goal set to $goal", Toast.LENGTH_SHORT).show()
    }

    fun freeMode(view: View){
        isFreeMode = true
        textView.text = "0"
        pullUpCounter = 0
        startFlag = false
        textView4.text = "shake to start"
    }

    override fun onResume() {
        Log.d("tag","onResume")
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        Log.d("tag","onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        mSensorManager.unregisterListener(this)
    }
}

