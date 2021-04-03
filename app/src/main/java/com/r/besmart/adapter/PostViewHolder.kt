package com.r.besmart.adapter


import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.r.besmart.databinding.PostItemBinding
import com.r.besmart.model.Post

class PostViewHolder(val binding: PostItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.post = post
        Glide.with(binding.postImage).load(post.image?.get(0)).into(binding.postImage)

    }

}