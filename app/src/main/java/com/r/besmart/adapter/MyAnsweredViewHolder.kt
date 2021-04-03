package com.r.besmart.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.r.besmart.databinding.PostItemBinding
import com.r.besmart.model.Answer

class MyAnsweredViewHolder(val binding:PostItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(answer: Answer) {
        Glide.with(binding.postImage).load(answer.imageP?.get(0)).into(binding.postImage)

    }

}