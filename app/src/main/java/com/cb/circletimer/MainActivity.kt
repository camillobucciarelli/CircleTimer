package com.cb.circletimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.cb.circletimerview.CircleTimerView
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val a = findViewById<CircleTimerView>(R.id.timer)

        a.totalTimeInSecond = 15
        a.totalTimeInSecond = 50
    }
}
