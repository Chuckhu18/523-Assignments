package edu.uw.eep523.assignment_1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val num :String = intent.getStringExtra("KEYRESULT")
        if(num == "-1"){
            textView3.text = "No one wins!"
        }else {
            textView3.text = "Player" + num + " Won!"
        }
    }

    fun send_main(view: View){
//        val main_page = Intent(this, MainActivity::class.java)
//        val send_data = "222"
        val intentSecond = Intent(this, MainActivity::class.java)
        startActivity(intentSecond)
//        intentSecond.putExtra("KEY_FROM_RESULT", send_data)
//        setResult(Activity.RESULT_OK, intentSecond)
//        finish()
    }
}
