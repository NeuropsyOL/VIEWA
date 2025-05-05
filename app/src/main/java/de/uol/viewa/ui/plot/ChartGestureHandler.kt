package de.uol.viewa.ui.plot

import android.view.MotionEvent
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener

class ChartGestureHandler(
    private val onTogglePause: () -> Unit,
    private val onReset: () -> Unit
) : OnChartGestureListener {

    override fun onChartLongPressed(me: MotionEvent) {
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
    }

    // no‐ops for now
    override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
    override fun onChartSingleTapped(me: MotionEvent?) {onTogglePause()}
    override fun onChartDoubleTapped(me: MotionEvent?) {onReset()}
    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
}