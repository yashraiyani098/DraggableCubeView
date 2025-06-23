package com.ext.draggablerotationalcubelibrary


import android.view.View
import androidx.viewpager.widget.ViewPager

abstract class ABaseTransformer : ViewPager.PageTransformer {

    /**
     * Indicates whether the default animations of the ViewPager2 should be used.
     */
    protected open val isPagingEnabled: Boolean
        get() = false

    /**
     * Apply custom transformation to the page.
     *
     * @param page The page to transform.
     * @param position Position of the page relative to the current front-and-center position.
     */
    protected abstract fun onTransform(page: View, position: Float)

    override fun transformPage(page: View, position: Float) {
        val clampedPosition = clampPosition(position)
        onPreTransform(page, clampedPosition)
        onTransform(page, clampedPosition)
        onPostTransform(page, clampedPosition)
    }

    /**
     * Clamp the position to fix rendering issues on older or edge cases.
     */
    private fun clampPosition(position: Float): Float {
        return position.coerceIn(-1f, 1f)
    }

    /**
     * Reset page properties before applying transformation.
     */
    protected open fun onPreTransform(page: View, position: Float) {
        page.apply {
            rotationX = 0f
            rotationY = 0f
            rotation = 0f
            scaleX = 1f
            scaleY = 1f
            pivotX = 0f
            pivotY = 0f
            translationY = 0f
            translationX = if (isPagingEnabled) 0f else -width * position

            if (hideOffscreenPages()) {
                alpha = if (position <= -1f || position >= 1f) 0f else 1f
                isEnabled = false
            } else {
                alpha = 1f
                isEnabled = true
            }
        }
    }

    /**
     * Optional hook after transformation logic.
     */
    protected open fun onPostTransform(page: View, position: Float) {
        // Optional override
    }

    /**
     * Whether to hide offscreen pages (alpha = 0)
     */
    protected open fun hideOffscreenPages(): Boolean = true

    companion object {
        /**
         * Returns the greater of value or min.
         */
        @JvmStatic
        protected fun min(value: Float, min: Float): Float {
            return if (value < min) min else value
        }
    }
}
