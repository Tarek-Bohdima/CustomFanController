package com.example.android.customfancontroller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Note that this enum is of type Int because the values are string resources rather than
// actual strings.
private enum class FanSpeed(val label: Int) {
    OFF(R.string.fan_off),
    LOW(R.string.fan_low),
    MEDIUM(R.string.fan_medium),
    HIGH(R.string.fan_high);

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }
}

// use these constants as part of drawing the dial indicators and labels.
private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35

// The @JvmOverloads annotation instructs the Kotlin compiler to generate overloads for
// this function that substitute default parameter values.
// https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/
class DialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    // variables to cache the attribute values.
    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSeedMaxColor = 0

    // several variables you need in order to draw the custom view.
    // These values are created and initialized here instead of when the view is actually drawn
    // to ensure that the actual drawing step runs as fast as possible.
    private var radius = 0.0f                   // Radius of the circle.
    private var fanSpeed = FanSpeed.OFF         // The active selection.
    // position variable which will be used to draw label and indicator circle position
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // As with the variables, these styles are initialized here to
        // help speed up the drawing step.
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    init {
        isClickable = true
        // supply the attributes and view, and and set your local variables using
        // the withStyledAttributes extension function.
        context.withStyledAttributes(attrs, R.styleable.DialView) {
            fanSpeedLowColor = getColor(R.styleable.DialView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.DialView_fanColor2, 0)
            fanSeedMaxColor = getColor(R.styleable.DialView_fanColor3, 0)
        }
    }

    override fun performClick(): Boolean {
        // The call to super.performClick() must happen first, which enables
        // accessibility events as well as calls onClickListener().
        if (super.performClick()) return true

        // increment the speed of the fan with the next() method, and set the view's content
        // description to the string resource representing the current speed (off, 1, 2 or 3).
        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        // invalidates the entire view, forcing a call to onDraw() to redraw the view.
        // If something in your custom view changes for any reason, including user interaction,
        // and the change needs to be displayed, call invalidate().
        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldheHight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    // This extension function on the PointF class calculates the X, Y coordinates on the screen
    // for the text label and current indicator (0, 1, 2, or 3), given
    // the current FanSpeed position and radius of the dial. You'll use this in onDraw().
    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Angles are in radians.
        val startAngle = Math.PI * (9 / 8.0)
        val angle = startAngle + pos.ordinal * (Math.PI / 4)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Set dial background color to gray or green if selection of fan speed is off.
//        paint.color = if (fanSpeed == FanSpeed.OFF) Color.GRAY else Color.GREEN
        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSeedMaxColor
        }

        // Draw the dial.
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // Draw the indicator circle.
        // This part uses the PointF.computeXYforSpeed() extension method to calculate
        // the X,Y coordinates for the indicator center based on the current fan speed.
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas.drawCircle(pointPosition.x, pointPosition.y, radius/12, paint)

        // Draw the text labels.
        // Finally, draw the fan speed labels (0, 1, 2, 3) at the appropriate positions around
        // the dial. This part of the method calls PointF.computeXYForSpeed() again to get
        // the position for each label, and reuses the pointPosition object each time
        // to avoid allocations. Use drawText() to draw the labels.
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas.drawText(label, pointPosition.x, pointPosition.y, paint)
        }
    }
}