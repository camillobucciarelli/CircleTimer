package com.cb.circletimer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import com.cb.circletimerview.CircleTimerView

class MainActivity : AppCompatActivity() {

    private val circleTimerView by lazy { findViewById<CircleTimerView>(R.id.timer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val isAdditive = findViewById<Switch>(R.id.isAdditive)
        val startTime = findViewById<EditText>(R.id.startTime)
        val circleLimit = findViewById<EditText>(R.id.circleLimit)
        val btnStartTimer = findViewById<Button>(R.id.btnStartTimer)
        val btnStopTimer = findViewById<Button>(R.id.btnStopTimer)

        isAdditive.setOnClickListener {
            circleTimerView.additiveMode = isAdditive.isChecked
        }
        btnStopTimer.setOnClickListener {
            circleTimerView.stopTimer()
            btnStartTimer.visibility = View.VISIBLE
            btnStopTimer.visibility = View.GONE
        }
        btnStartTimer.visibility = if (btnStopTimer.visibility == View.GONE) View.VISIBLE else View.GONE
        btnStartTimer.setOnClickListener {
            circleTimerView.startTimer()
            btnStartTimer.visibility = View.GONE
            btnStopTimer.visibility = View.VISIBLE
        }

        //  change start time

        startTime.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(time: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(time.toString() != ""){
                    circleTimerView.startTimeInMillis = time.toString().toLong()
                }else{
                    circleTimerView.startTimeInMillis = 0
                }
            }
        })

        //  change circleLimit time
        circleLimit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(time: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(time.toString() != ""){
                    circleTimerView.circleTimeLimitInMillis = time.toString().toLong()
                }else{
                    circleTimerView.circleTimeLimitInMillis = 0
                }
            }
        })
    }
}
