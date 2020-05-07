package edu.uw.eep523.assignment_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun send_main(view: View){
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    fun show_instruction(view:View){
        val inst = Intent(this, Main2Activity::class.java)
        startActivity(inst)
    }
}
