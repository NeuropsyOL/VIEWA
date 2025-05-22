package de.uol.neuropsy.viewa.utils
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt

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
            "#1f77b4".toColorInt(),
            "#ff7f0e".toColorInt(),
            "#2ca02c".toColorInt(),
            "#d62728".toColorInt(),
            "#9467bd".toColorInt(),
            "#8c564b".toColorInt(),
            "#e377c2".toColorInt(),
            "#7f7f7f".toColorInt(),
            "#bcbd22".toColorInt(),
            "#17becf".toColorInt()
        )
    }
}