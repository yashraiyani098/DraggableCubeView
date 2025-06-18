package com.ext.draggablerotationalcubelibrary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ext.draggablerotationalcubelibrary.databinding.FloatingSliderItemBinding

class SliderAdapter(
    private val context: Context
) : RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {
    
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    
    override fun getItemCount(): Int = 5
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = FloatingSliderItemBinding.inflate(inflater, parent, false)
        return SliderViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        // Bind data if needed
    }
    
    class SliderViewHolder(val binding: FloatingSliderItemBinding) : RecyclerView.ViewHolder(binding.root)
}
