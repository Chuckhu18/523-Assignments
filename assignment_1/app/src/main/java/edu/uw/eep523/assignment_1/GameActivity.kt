package edu.uw.eep523.assignment_1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    var icon_picked = false
    var first_pick = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        pick_x.setOnClickListener {
            icon_picked = true
            first_pick = "X"
            pick_x.isEnabled = false
            pick_o.isEnabled = false
        }

        pick_o.setOnClickListener {
            icon_picked = true
            first_pick = "O"
            pick_x.isEnabled = false
            pick_o.isEnabled = false
        }
    }

    fun Click(view: View){
        val selected = view as Button
        var cellID = 0

        when(selected.id){
            R.id.button -> cellID = 1
            R.id.button2 -> cellID = 2
            R.id.button3 -> cellID = 3
            R.id.button4 -> cellID = 4
            R.id.button5 -> cellID = 5
            R.id.button6 -> cellID = 6
            R.id.button7 -> cellID = 7
            R.id.button8 -> cellID = 8
            R.id.button9 -> cellID = 9
        }

//        Toast.makeText(this, "Cell ID: $cellID",Toast.LENGTH_LONG).show()

        playGame(cellID, selected)
    }

    var p1 = ArrayList<Int>()
    var p2 = ArrayList<Int>()

    var activePlayer = true

    private fun playGame(cellID:Int, selected: Button){

        if(activePlayer){
            selected.text = first_pick
            if(selected.text != "") {
                p1.add(cellID)
                selected.isEnabled = false
                activePlayer = !activePlayer
            }

        }else{
            if(first_pick == "X")
                selected.text = "O"
            else if(first_pick == "O")
                selected.text = "X"
            else
                selected.text = ""

            if(selected.text != "") {
                p2.add(cellID)
                selected.isEnabled = false
                activePlayer = !activePlayer
            }
        }

        checkWin()
    }

    var flag = false
    var winner = -1

    private fun checkWin(){

        //rows
        for (i in 0..2){
            if(p1.contains(i*3+1) && p1.contains(i*3+2) && p1.contains(i*3+3))
                winner = 1
            if(p2.contains(i*3+1) && p2.contains(i*3+2) && p2.contains(i*3+3))
                winner = 2
        }
        //cols
        for (i in 0..2){
            if(p1.contains(i+1) && p1.contains(i+4) && p1.contains(i+7))
                winner = 1
            if(p2.contains(i+1) && p2.contains(i+4) && p2.contains(i+7))
                winner = 2
        }

        //dig
        if(p1.contains(1) && p1.contains(5) && p1.contains(9))
            winner = 1
        if(p2.contains(1) && p2.contains(5) && p2.contains(9))
            winner = 2
        if(p1.contains(3) && p1.contains(5) && p1.contains(7))
            winner = 1
        if(p2.contains(3) && p2.contains(5) && p2.contains(7))
            winner = 2

        if(winner != -1 && flag == false){
            if(winner == 1) {
                Toast.makeText(this, "Player 1 Won", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Player 2 Won", Toast.LENGTH_LONG).show()
            }
            flag = true
        }

    }

    fun send_result(view:View){
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(KEY, winner.toString())
        startActivityForResult(intent, RCODE)
    }
/*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 123){
            if(resultCode == Activity.RESULT_OK){
                val received_data = data?.getStringExtra("KEY_FROM_RESULT")
                Toast.makeText(this,received_data , Toast.LENGTH_LONG).show()
            }
        }
    }*/

    companion object{
        const val KEY = "KEYRESULT"
        const val RCODE = 123
    }



}
