package com.ext.draggablerotationalcubelibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.ext.draggablerotationalcubelibrary.databinding.FloatingSliderItemBinding
import com.bumptech.glide.Glide

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

        // Set header visibility and text
        binding.txtHeader.visibility = if (itemData.headerVisible && itemData.header.isNotEmpty()) View.VISIBLE else View.GONE
        binding.txtHeader.text = itemData.header

        // Set image visibility and load image
        binding.imgContent.visibility = if (itemData.imageVisible && itemData.image != null) View.VISIBLE else View.GONE
        if (itemData.imageVisible && itemData.image != null) {
            when (itemData.image) {
                is Int -> Glide.with(context)
                    .load(itemData.image)
                    .into(binding.imgContent)
                is String -> if (itemData.image.isNotEmpty()) {
                    Glide.with(context)
                        .load(itemData.image)
                        .into(binding.imgContent)
                }
            }
        }

        // Set description visibility and text
        binding.txtDescription.visibility = if (itemData.descriptionVisible && itemData.description.isNotEmpty()) View.VISIBLE else View.GONE
        binding.txtDescription.text = itemData.description

        container.addView(binding.root)
        return binding.root
    }
}