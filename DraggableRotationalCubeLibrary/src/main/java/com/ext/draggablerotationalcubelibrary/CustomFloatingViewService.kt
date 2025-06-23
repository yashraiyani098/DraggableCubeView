package com.ext.draggablerotationalcubelibrary

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.ext.draggablerotationalcubelibrary.InitApplication.Companion.instance
import com.ext.draggablerotationalcubelibrary.CubeItemData
import java.util.ArrayList


class CustomFloatingViewService : Service(), FloatingViewListener {
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var mFloatingViewManager: FloatingViewManager? = null
    private var width: Int = 0
    private var height: Int = 0
    @SuppressLint("ServiceCast")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (mFloatingViewManager != null) {
            return START_STICKY
        }

        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        height = metrics.heightPixels
        width = metrics.widthPixels
        val inflater = LayoutInflater.from(this)
        val iconView = inflater.inflate(R.layout.floating_layout_cube, null, false)
        val imageSlider = iconView.findViewById<ViewPagerCustomDuration>(R.id.imageSlider)
        iconView.setOnClickListener { v: View? -> }
        if (handler != null) {
            handler!!.removeCallbacks(runnable!!)
        }

        // Get the cube data from intent
        val cubeData = intent.getParcelableArrayListExtra<CubeItemData>(EXTRA_CUBE_DATA)
        
        val sliderAdapter = SliderAdapter(this, cubeData ?: emptyList())
        val mAdapater = EndlessPagerAdapter(sliderAdapter)
        imageSlider.adapter = mAdapater
        imageSlider.setPageTransformer(false, CubeOutTransformer() as ViewPager.PageTransformer)
        imageSlider.setScrollDurationFactor(10.0)

        handler = Handler()
        runnable = Runnable {
            imageSlider.currentItem += 1
            handler!!.postDelayed(runnable!!, 5000)
        }

        handler!!.post(runnable!!)

        mFloatingViewManager = FloatingViewManager(this, this)
        loadDynamicOptions()
        val options = loadOptions()
        mFloatingViewManager!!.addViewToWindow(iconView, options)

        return START_REDELIVER_INTENT
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        destroy()
        super.onDestroy()
    }

    /**
     * {@inheritDoc}
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * {@inheritDoc}
     */
    override fun onFinishFloatingView() {
        stopSelf()
    }

    /**
     * {@inheritDoc}
     */
    override fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int) {
        Log.e("TAG", "(X,Y) = $x $y")
        Log.e("TAG", "width = $width")

        val dimenPix1 = 50
        if (x > (width - dimenPix1) || x < 0) {
            stopSelf()

            instance.setIsCubeModeEnabled(false)
            return
        } else if (y > (height - dimenPix1) || y < 0) {
            stopSelf()

            instance.setIsCubeModeEnabled(false)
            return
        }
        if (!isFinishing) {
            val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            editor.putInt(PREF_KEY_LAST_POSITION_X, x)
            editor.putInt(PREF_KEY_LAST_POSITION_Y, y)
            editor.apply()
        }
    }

    private fun destroy() {
        try {
            if (mFloatingViewManager != null) {
                mFloatingViewManager!!.removeAllViewToWindow()
                mFloatingViewManager = null
            }
        } catch (e: Exception) {
        }
    }

    private fun loadDynamicOptions() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        val displayModeSettings = sharedPref.getString("settings_display_mode", "")
        if ("Always" == displayModeSettings) {
            mFloatingViewManager!!.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)
        } else if ("FullScreen" == displayModeSettings) {
            mFloatingViewManager!!.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_FULLSCREEN)
        } else if ("Hide" == displayModeSettings) {
            mFloatingViewManager!!.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_ALWAYS)
        }
    }


    private fun loadOptions(): FloatingViewManager.Options {
        val options = FloatingViewManager.Options()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)

        // Shape
        val shapeSettings = sharedPref.getString("settings_shape", "")
        if ("Circle" == shapeSettings) {
            options.shape = FloatingViewManager.SHAPE_CIRCLE
        } else if ("Rectangle" == shapeSettings) {
            options.shape = FloatingViewManager.SHAPE_RECTANGLE
        }

        // Margin
        val marginSettings = sharedPref.getString("settings_margin", options.overMargin.toString())
        options.overMargin = marginSettings!!.toInt()

        // MoveDirection
        options.moveDirection = FloatingViewManager.MOVE_DIRECTION_NONE
        options.usePhysics = sharedPref.getBoolean("settings_use_physics", true)

        // Last position
        val isUseLastPosition = sharedPref.getBoolean("settings_save_last_position", false)
        if (isUseLastPosition) {
            val defaultX = options.floatingViewX
            val defaultY = options.floatingViewY
            options.floatingViewX = sharedPref.getInt(PREF_KEY_LAST_POSITION_X, defaultX)
            options.floatingViewY = sharedPref.getInt(PREF_KEY_LAST_POSITION_Y, defaultY)
        } else {
            // Init X/Y
            val dimenPix = 100
            val dimenPix1 = 100
            options.floatingViewX = dimenPix1.toInt()
            options.floatingViewY = dimenPix.toInt()
        }

        // Initial Animation
        val animationSettings =
            sharedPref.getBoolean("settings_animation", options.animateInitialMove)
        options.animateInitialMove = animationSettings

        return options
    }


    companion object {
        private const val PREF_KEY_LAST_POSITION_X = "last_position_x"
        private const val PREF_KEY_LAST_POSITION_Y = "last_position_y"
        const val EXTRA_CUTOUT_SAFE_AREA = "EXTRA_CUTOUT_SAFE_AREA"
        const val EXTRA_CUBE_DATA = "EXTRA_CUBE_DATA"
    }
}
