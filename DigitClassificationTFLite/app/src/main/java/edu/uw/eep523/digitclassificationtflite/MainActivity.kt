package edu.uw.eep523.digitclassificationtflite

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import com.divyanshu.draw.widget.DrawView


/// NAME OF YOUR MODEL - tflite file in the asset folder
const val MODEL_FILE = "model_group5_Q.tflite"


class MainActivity : AppCompatActivity() {

    private var drawView: DrawView? = null
    private var clearButton: Button? = null
    private var predictedTextView: TextView? = null
    private var digitClassifier = DigitClassifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup view instances
        drawView = findViewById(R.id.draw_view)
        drawView?.setStrokeWidth(70.0f)
        drawView?.setColor(Color.WHITE)
        drawView?.setBackgroundColor(Color.BLACK)

        clearButton = findViewById(R.id.clear_button)
        predictedTextView = findViewById(R.id.predicted_text)


        // Setup clear drawing button
        clearButton?.setOnClickListener {
            drawView?.clearCanvas()

            predictedTextView?.text = ""
        }

        // Setup classification trigger so that it classify after every stroke drew
        drawView?.setOnTouchListener { _, event ->
            // As we have interrupted DrawView's touch event,
            // we first need to pass touch events through to the instance for the drawing to show up
            drawView?.onTouchEvent(event)

            // Then if user finished a touch event, run classification
            if (event.action == MotionEvent.ACTION_UP) {
                classifyDrawing()
           }

            true
        }

        // Setup digit classifier
        digitClassifier
                .initialize()
                .addOnFailureListener { e -> Log.e(TAG, "Error to setting up digit classifier.", e) }
    }

    override fun onDestroy() {
        digitClassifier.close()
        super.onDestroy()
    }

    private fun classifyDrawing() {
        val bitmap = drawView?.getBitmap()

        if ((bitmap != null) && (digitClassifier.isInitialized)) {
            digitClassifier
                    .classifyAsync(bitmap) //Call function from DigitClassifier class

                    .addOnSuccessListener { resultText -> predictedTextView?.text = resultText }
                    .addOnFailureListener { e ->
                        predictedTextView?.text = "classification error"
                        Log.e(TAG, "Error classifying drawing.", e)
                    }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
