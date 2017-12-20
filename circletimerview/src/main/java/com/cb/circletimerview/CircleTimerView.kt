package com.cb.circletimerview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.View
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *Copyright 2017 Camillo Bucciarelli
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain timeNotZero copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
class CircleTimerView : View {

    private val logTag = CircleTimerView::class.java.canonicalName

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        readAttributesAndSetupFields(context.obtainStyledAttributes(attrs, R.styleable.CircleTimerView))
    }

    //  --------------------------------------------------------------------------------------------
    //      default values
    //  --------------------------------------------------------------------------------------------
    var timeNotZero = false
    private val timeDivider = SparseArray<Long>()
    private val dividedTime = SparseArray<Long>()
    private val timeLabel = SparseArray<String>()
    private val circleTimeLimitInMillisDef = 0
    private val startTimeInMillisDef = 0
    private val swipeAngleDef = 360f
    private val swipeAngleAddDef = 0f
    private val timerElapseVelocity = 100L
    private val additiveModeDef = false
    private val labelSizeDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, context.resources.displayMetrics)
    private val labelColorDef = Color.GRAY
    private val valueTopSizeDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics)
    private val valueTopColorDef = Color.GRAY
    private val valueBottomSizeDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, context.resources.displayMetrics)
    private val valueBottomColorDef = Color.LTGRAY
    private val labelForDaysDef = "DAY"
    private val labelForHoursDef = "HR"
    private val labelForMinutesDef = "MIN"
    private val labelForSecondsDef = "SEC"
    private val lineSeparatorColorDef = Color.parseColor("#b0bec5")
    private val lineSeparatorStrokeWidthDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, context.resources.displayMetrics)
    private val backCircleColorDef = Color.parseColor("#b0bec5")
    private val backCircleStrokeWidthDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, context.resources.displayMetrics)
    private val foregroundCircleColorDef = Color.parseColor("#263238")
    private val foregroundCircleStrokeWidthDef = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15f, context.resources.displayMetrics)
    private val foregroundCircleStartValueDef = -90f

    //  --------------------------------------------------------------------------------------------
    //      properties
    //  --------------------------------------------------------------------------------------------
    private val rect: RectF by lazy {
        RectF(
                (width / 2).toFloat() - radius(),
                (height / 2).toFloat() - radius(),
                (width / 2).toFloat() + radius(),
                (height / 2).toFloat() + radius()
        )
    }
    private var timer: Timer? = null

    var circleTimeLimitInMillis = circleTimeLimitInMillisDef.toLong()
        set(value) {
            field = value
            postInvalidate()
        }
    var startTimeInMillis = startTimeInMillisDef.toLong()
        set(value) {
            field = value
            setTimeValue(startTimeInMillis)
            foregroundMovement()
            setTimeValue(value)
            postInvalidate()
        }
    private var labelTop = labelForMinutesDef
    private var valueTop = "0"
    private var labelBottom = labelForSecondsDef
    private var valueBottom = "0"
    private var swipeAngle = swipeAngleDef
    var additiveMode = additiveModeDef
        set(value) {
            field = value
            Log.d(logTag, "additiveMode: " + additiveMode)
            postInvalidate()
        }

    private var foregroundCircleStartValue = foregroundCircleStartValueDef
    private val lineSeparatorPaint = Paint()
    private val backCirclePaint = Paint()
    private val foregroundCirclePaint = Paint()
    private val labelPaint = Paint()
    private val valueTopPaint = Paint()
    private val valueBottomPaint = Paint()

    //  --------------------------------------------------------------------------------------------
    //      initializer property method
    //  --------------------------------------------------------------------------------------------
    private fun readAttributesAndSetupFields(attrs: TypedArray) {
        circleTimeLimitInMillis = attrs.getInteger(R.styleable.CircleTimerView_circleTimeLimitInMillis, circleTimeLimitInMillisDef).toLong()
        startTimeInMillis = attrs.getInt(R.styleable.CircleTimerView_startTimeInMillis, startTimeInMillisDef).toLong()
        additiveMode = attrs.getBoolean(R.styleable.CircleTimerView_additiveMode, additiveModeDef)
        swipeAngle = if (additiveMode) swipeAngleAddDef else swipeAngleDef
        timeLabel.put(0, attrs.getString(R.styleable.CircleTimerView_labelForDays, labelForDaysDef))
        timeLabel.put(1, attrs.getString(R.styleable.CircleTimerView_labelForHours, labelForHoursDef))
        timeLabel.put(2, attrs.getString(R.styleable.CircleTimerView_labelForMinutes, labelForMinutesDef))
        timeLabel.put(3, attrs.getString(R.styleable.CircleTimerView_labelForSeconds, labelForSecondsDef))
        labelTop = timeLabel.get(2)
        labelBottom = timeLabel.get(3)
        timeDivider.put(0, 0)
        timeDivider.put(1, 24)
        timeDivider.put(2, 60)
        timeDivider.put(3, 60)
        paintSetup(
                lineSeparatorPaint,
                attrs.getColor(R.styleable.CircleTimerView_lineSeparatorColor, lineSeparatorColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_lineSeparatorStrokeWidth, lineSeparatorStrokeWidthDef)
        )
        paintSetup(
                backCirclePaint,
                attrs.getColor(R.styleable.CircleTimerView_backCircleColor, backCircleColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_backCircleStrokeWidth, backCircleStrokeWidthDef)
        )
        paintSetup(
                foregroundCirclePaint,
                attrs.getColor(R.styleable.CircleTimerView_foregroundCircleColor, foregroundCircleColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_foregroundCircleStrokeWidth, foregroundCircleStrokeWidthDef)
        )
        textSetup(
                labelPaint,
                attrs.getColor(R.styleable.CircleTimerView_labelColor, labelColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_labelSize, labelSizeDef)
        )
        textSetup(
                valueTopPaint,
                attrs.getColor(R.styleable.CircleTimerView_valueTopColor, valueTopColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_valueTopSize, valueTopSizeDef),
                true
        )
        textSetup(
                valueBottomPaint,
                attrs.getColor(R.styleable.CircleTimerView_valueBottomColor, valueBottomColorDef),
                attrs.getDimension(R.styleable.CircleTimerView_valueBottomSize, valueBottomSizeDef),
                true
        )
        foregroundMovement()
        postInvalidate()
    }

    private fun paintSetup(paint: Paint, color: Int, strokeWidth: Float) {
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.isAntiAlias = true
    }

    private fun textSetup(paint: Paint, color: Int, size: Float, bold: Boolean = false) {
        paint.color = color
        paint.textSize = size
        paint.textAlign = Paint.Align.CENTER
        if (bold) paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.isAntiAlias = true
    }

    //  --------------------------------------------------------------------------------------------
    //      draw timer
    //  --------------------------------------------------------------------------------------------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //  line separator
        canvas.drawLine(
                (width / 2).toFloat() - radius(),
                (height / 2).toFloat(),
                (width / 2).toFloat() + radius(),
                (height / 2).toFloat(),
                lineSeparatorPaint
        )

        //  back circle
        canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                radius(),
                backCirclePaint
        )

        //  foreground circle
        canvas.drawArc(
                rect,
                foregroundCircleStartValue,
                swipeAngle,
                false,
                foregroundCirclePaint
        )

        // top label
        canvas.drawText(
                labelTop,
                (width / 2).toFloat(),
                (height / 2).toFloat() - (radius() / 3) * 2,
                labelPaint
        )

        // top value
        canvas.drawText(
                valueTop,
                (width / 2).toFloat(),
                (height / 2).toFloat()
                        - ((valueTopPaint.descent() + valueTopPaint.ascent()) / 2)
                        - (radius() / 3),
                valueTopPaint
        )

        // bottom value
        canvas.drawText(
                valueBottom,
                (width / 2).toFloat(),
                (height / 2).toFloat()
                        - ((valueBottomPaint.descent() + valueBottomPaint.ascent()) / 2)
                        + (radius() / 3),
                valueBottomPaint
        )

        // bottom label
        canvas.drawText(
                labelBottom,
                (width / 2).toFloat(),
                (height / 2).toFloat()
                        - (labelPaint.descent() + labelPaint.ascent())
                        + (radius() / 3) * 2,
                labelPaint
        )
    }

    private fun radius(): Float {
        return when (width < height) {
            true -> (width / 3).toFloat()
            else -> (height / 3).toFloat()
        }
    }


    private fun setTimeValue(timeValue: Long) {
        dividedTime.put(0, TimeUnit.MILLISECONDS.toDays(timeValue))
        dividedTime.put(1, TimeUnit.MILLISECONDS.toHours(timeValue))
        dividedTime.put(2, TimeUnit.MILLISECONDS.toMinutes(timeValue))
        dividedTime.put(3, TimeUnit.MILLISECONDS.toSeconds(timeValue))
        Log.d(logTag, "days: ${TimeUnit.MILLISECONDS.toDays(timeValue)}")
        Log.d(logTag, "hours: ${TimeUnit.MILLISECONDS.toHours(timeValue)}")
        Log.d(logTag, "minutes: ${TimeUnit.MILLISECONDS.toMinutes(timeValue)}")
        Log.d(logTag, "seconds: ${TimeUnit.MILLISECONDS.toSeconds(timeValue)}")
        Log.d(logTag, "dividedTime.size(): ${dividedTime.size()}")
        if (!dividedTime.isEmpty()) {
            timeNotZero = true
            for (i in 0 until dividedTime.size() - 1) {
                when (dividedTime.get(i)) {
                    0L -> {
                        if (i == dividedTime.size() - 2) {
                            labelTop = label(i)
                            valueTop = (dividedTime.get(i)).toString()
                            setBottomStrings(i + 1)
                        }
                    }
                    else -> {
                        labelTop = label(i)
                        valueTop = (dividedTime.get(i)).toString()
                        setBottomStrings(i + 1)
                        return
                    }
                }
            }
        }

        if (timeNotZero && dividedTime.isEmpty()){
            labelTop = label(2)
            valueTop = "0"
            labelBottom = label(3)
            valueBottom = "0"
        }
    }

    private fun setBottomStrings(position: Int) {
        labelBottom = label(position)
        valueBottom = if (dividedTime.get(position) >= timeDivider.get(position)) {
            (dividedTime.get(position) % timeDivider.get(position)).toString()
        } else {
            (dividedTime.get(position)).toString()
        }
    }

    private fun foregroundMovement() {
        swipeAngle = (swipeAngleDef * startTimeInMillis) / circleTimeLimitInMillis
    }

    private fun label(position: Int) = timeLabel.get(position)

    fun startTimer() {
        Log.w(logTag, "Timer additive mode: $additiveMode")
        timer = Timer()
        timer!!.schedule(
                when (additiveMode) {
                    true -> object : TimerTask() {
                        override fun run() {
                            handler.post {
                                Log.d(logTag, "additiveTimerTask - actual second: $startTimeInMillis")
                                startTimeInMillis += timerElapseVelocity
                            }
                        }
                    }
                    else -> object : TimerTask() {
                        override fun run() {
                            when (startTimeInMillis) {
                                0L -> {
                                    Log.d(logTag, "subtractiveTimerTask - Timer 0... cancelled")
                                    stopTimer()
                                }
                                else -> {
                                    Log.d(logTag, "subtractiveTimerTask - actual second: $startTimeInMillis")
                                    startTimeInMillis -= timerElapseVelocity

                                }
                            }
                        }
                    }
                },
                timerElapseVelocity,
                timerElapseVelocity
        )
    }

    fun stopTimer() {
        Log.d(logTag, "Timer cancelled")
        timer?.purge()
        timer?.cancel()
    }

}


//  ------------------------------------------------------------------------------------------------
//      extension of TypedArray get string that accept default
//  ------------------------------------------------------------------------------------------------
private fun TypedArray.getString(stringId: Int, default: String): String {
    return when (this.getString(stringId)) {
        null -> default
        else -> this.getString(stringId)
    }
}

//  ------------------------------------------------------------------------------------------------
//      extension of SparseArray isEmpty
//  ------------------------------------------------------------------------------------------------
private fun SparseArray<Long>.isEmpty(): Boolean {
    val size = this.size()
    for (i in 0 until size) {
        if (size != this.size()) throw ConcurrentModificationException()
        if (this.get(i) != 0L) return false
    }
    return true
}