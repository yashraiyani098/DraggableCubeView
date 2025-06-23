package com.ext.draggablerotationalcubelibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ext.draggablerotationalcubelibrary.CubeItemData
import com.ext.draggablerotationalcubelibrary.databinding.FloatingSliderItemBinding

class SliderAdapter(
    private val context: Context,
    private val data: List<CubeItemData>
) : PagerAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = data.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = FloatingSliderItemBinding.inflate(inflater, container, false)
        val itemData = data[position]

        // Set the data to views
        binding.txtHeader.text = itemData.header
        binding.txtDetected.text = "Detected: ${itemData.detected}"
        binding.txtDeath.text = "Deaths: ${itemData.death}"

        container.addView(binding.root)
        return binding.root
    }
}
