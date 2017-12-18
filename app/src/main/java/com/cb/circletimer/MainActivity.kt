package com.cb.circletimer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.cb.circletimerview.CircleTimerView

class MainActivity : AppCompatActivity() {

    private val a by lazy { findViewById<CircleTimerView>(R.id.timer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        a.totalTimeInMillisecond = 1000
    }
}
