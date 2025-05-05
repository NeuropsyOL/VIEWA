package de.uol.viewa.ui.main.utils
import android.graphics.Color
import androidx.annotation.ColorInt
/**
 * Simple next-color generator for chart datasets.
 * Uses a five-color palette optimized for mobile screens.
 * Loops if you ever need more than five colors.
 */
class ColorPalette(@ColorInt private val palette: IntArray = DEFAULT_PALETTE){
    private var index = 0

    /**
     * Returns the next color in the cycle, looping back to start as needed.
     */
    @ColorInt
    fun nextColor(): Int {
        val color = palette[index]
        index = (index + 1) % palette.size
        return color
    }

    /** Reset the cycle back to the first color. */
    fun reset() {
        index = 0
    }

    companion object {
        /** A palette of five Material-style colors. */
        @JvmField
        val DEFAULT_PALETTE: IntArray = intArrayOf(
            Color.parseColor("#2196F3"), // Blue 500
            Color.parseColor("#4CAF50"), // Green 500
            Color.parseColor("#F44336"), // Red 500
            Color.parseColor("#FF9800"), // Orange 500
            Color.parseColor("#9C27B0")  // Purple 500
        )
    }
}