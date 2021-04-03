package com.r.besmart.adapter

import androidx.recyclerview.widget.RecyclerView
import com.r.besmart.R
import com.r.besmart.databinding.AppliedItemBinding
import com.r.besmart.model.Apply

class ApplyViewHolder (val binding:AppliedItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(apply: Apply) {
        binding.apply = apply
        if(apply.accepted == true)
            binding.card.setBackgroundResource(R.drawable.cardview_resource)
        else{
            binding.card.setBackgroundResource(R.drawable.cardview_resource_not)
        }
    }
}

