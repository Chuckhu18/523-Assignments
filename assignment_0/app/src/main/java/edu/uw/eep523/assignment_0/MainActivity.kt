package edu.uw.eep523.assignment_0

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.TestLooperManager
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    //get the widgets
//    val result_view = findViewById<TextView>(R.id.result)
//    val user_pick_view = findViewById<TextView>(R.id.robot_pick)
//    val bot_pick_view = findViewById<TextView>(R.id.you_pick)

    //get user option
    var options: Int = 0
    var bot_counter: Int = 0
    var user_counter: Int = 0

    /* options:
       1 = rock
       2 = paper
       3 = scissors */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val result_view = findViewById<TextView>(R.id.result)
        val user_pick_view = findViewById<TextView>(R.id.you_pick)
        val bot_pick_view = findViewById<TextView>(R.id.robot_pick)

        button_rock.setOnClickListener {
            options = 1
//            println("rock clicked $options")
            checkResult(options, result_view, user_pick_view, bot_pick_view)
        }

        button_paper.setOnClickListener {
            options = 2
//            println("paper clicked $options")
            checkResult(options, result_view, user_pick_view, bot_pick_view)
        }

        button_scissors.setOnClickListener {
            options = 3
//            println("scissors clicked $options")
            checkResult(options, result_view, user_pick_view, bot_pick_view)
        }

    }

    /* random number:
       1 = rock
       2 = paper
       3 = scissors */
    fun checkResult(opt: Int, result_view: TextView, user_pick_view: TextView, bot_pick_view:TextView){
        val rand = (1..3).random()
//        println("rand:$rand")
        showPicks(rand, opt, user_pick_view, bot_pick_view)

        val bot_score_view = findViewById<TextView>(R.id.bot_score)
        val user_score_view = findViewById<TextView>(R.id.user_score)


        if (rand == opt) {
            result_view.text = "Tie!"
        }
        else if ((opt == 1 && rand == 3) || (opt == 2 && rand == 1) || (opt == 3 && rand == 2)) {
            result_view.text = "You Won!"
            user_counter++
            user_score_view.text = "$user_counter"
        }
        else {
            result_view.text = "You Lost!"
            bot_counter++
            bot_score_view.text = "$bot_counter"
        }
    }

    private fun showPicks(bot:Int, user:Int, user_pick_view:TextView, bot_pick_view:TextView){
        if(bot == 1)
            bot_pick_view.text = "Rock"
        if(bot == 2)
            bot_pick_view.text = "Paper"
        if(bot == 3)
            bot_pick_view.text = "Scissors"
        if(user == 1)
            user_pick_view.text = "Rock"
        if(user == 2)
            user_pick_view.text = "Paper"
        if(user == 3)
            user_pick_view.text = "Scissors"
    }
}
