package com.r.besmart.profileScreen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.firestore.Query
import com.r.besmart.adapter.PostViewHolder
import com.r.besmart.R
import com.r.besmart.databinding.FragmentMyAskedBinding
import com.r.besmart.databinding.PostItemBinding
import com.r.besmart.model.Post

class MyAskedFragment : Fragment() {

    private lateinit var binding: FragmentMyAskedBinding
    private lateinit var viewModel: AskedViewModel
    private lateinit var mAdapter: FirestorePagingAdapter<Post, PostViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_asked, container, false
        )
        viewModel = ViewModelProvider(this).get(AskedViewModel::class.java)
        // Inflate the layout for this fragment
        binding.lifecycleOwner = viewLifecycleOwner

        binding.askedRecycler.setHasFixedSize(true)
        binding.askedRecycler.layoutManager = LinearLayoutManager(context)

        setupAdapter()
        mAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.swipeRefresh.setOnRefreshListener {
            mAdapter.refresh()
        }
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        mAdapter.startListening();
    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    private fun setupAdapter() {
        val mQuery = viewModel.mPostsCollection!!.orderBy("time", Query.Direction.DESCENDING)
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(2)
            .setPageSize(2)
            .build()

        val options = FirestorePagingOptions.Builder<Post>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, config, Post::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<Post, PostViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PostItemBinding.inflate(inflater, parent, false)
                return PostViewHolder(binding)
            }

            override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, post: Post) {
                val postRef = getItem(position)
                viewHolder.bind(post)

                viewHolder.itemView.setOnClickListener {
                    val bundle = bundleOf(
                        "category" to "Mathematics",
                        "id" to postRef!!.id
                    )
                    findNavController().navigate(
                        R.id.action_profileFragment_to_detailFragment,
                        bundle
                    )
                }
            }

            override fun onError(e: Exception) {
                super.onError(e)
                e.message?.let { Log.e("MainActivity", it) }
            }

            override fun onLoadingStateChanged(state: LoadingState) {
                when (state) {
                    LoadingState.LOADING_INITIAL -> {
                        binding.swipeRefresh.isRefreshing = true
                    }

                    LoadingState.LOADING_MORE -> {
                        binding.swipeRefresh.isRefreshing = true
                    }

                    LoadingState.LOADED -> {
                        binding.swipeRefresh.isRefreshing = false
                    }

                    LoadingState.ERROR -> {
                        Toast.makeText(
                            context,
                            "Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.swipeRefresh.isRefreshing = false
                    }

                    LoadingState.FINISHED -> {
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
            }

        }

        binding.askedRecycler.adapter = mAdapter


    }


}