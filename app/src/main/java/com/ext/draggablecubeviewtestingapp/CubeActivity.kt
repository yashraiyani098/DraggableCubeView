package com.ext.draggablecubeviewtestingapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.ext.draggablecubeviewtestingapp.databinding.ActivityCubeBinding
import com.ext.draggablerotationalcubelibrary.CubeItemData
import com.ext.draggablerotationalcubelibrary.CustomFloatingViewService
import com.ext.draggablerotationalcubelibrary.FloatingViewManager
import com.ext.draggablerotationalcubelibrary.InitApplication

class CubeActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityCubeBinding
    private lateinit var cubeItemData: MutableList<CubeItemData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCubeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the cube mode
        InitApplication.instance.setIsCubeModeEnabled(true)

        initCubeData()

        binding.buttonCube.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (this.window.attributes.layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                    throw RuntimeException("'windowLayoutInDisplayCutoutMode' must not be set to 'never'")
                }
            }

            // Start the floating view service
            val intent = Intent(this, CustomFloatingViewService::class.java)
            intent.putExtra(CustomFloatingViewService.EXTRA_CUTOUT_SAFE_AREA, FloatingViewManager.findCutoutSafeArea(this))
            intent.putParcelableArrayListExtra(CustomFloatingViewService.EXTRA_CUBE_DATA, ArrayList(cubeItemData))
            startService(intent)
        }


    }

    private fun initCubeData() {
        cubeItemData = mutableListOf(
            CubeItemData("COVID-19", "1,234", "567"),
            CubeItemData("vaccation", "20 days", "789"),
            CubeItemData("uasfsgf", "4,54", "846"),
            CubeItemData("Active Cases", "2,345", "678"),
            CubeItemData("Recovered", "9,012", "345")
        )
    }

    private fun modifyCubeData() {
        // Example of modifying data
        cubeItemData[0] = cubeItemData[0].copy(
            header = "Updated COVID-19",
            detected = "1,500",
            death = "600"
        )
        
        // Notify the floating view to update
        val intent = Intent(this, CustomFloatingViewService::class.java)
        intent.action = "UPDATE_CUBE_DATA"
        intent.putParcelableArrayListExtra("CUBE_ITEM_DATA", ArrayList(cubeItemData))
        startService(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check for overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivityForResult(intent, 0)
            }
        }
    }

}