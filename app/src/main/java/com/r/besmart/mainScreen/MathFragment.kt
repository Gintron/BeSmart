package com.r.besmart.mainScreen


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
import com.r.besmart.databinding.FragmentMathBinding
import com.r.besmart.databinding.PostItemBinding
import com.r.besmart.model.Post
import com.r.besmart.repository.FirebaseRepository



class MathFragment(val categoryName:String) : Fragment() {

    private lateinit var mAdapter: FirestorePagingAdapter<Post, PostViewHolder>
    private val firebaseRepository = FirebaseRepository()
    private lateinit var binding: FragmentMathBinding
    private val mPostsCollection = firebaseRepository.firestoreDB.collection("Posts").whereEqualTo("category", categoryName)
    private val mQuery = mPostsCollection.orderBy("time", Query.Direction.DESCENDING)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_math, container, false)
        // Inflate the layout for this fragment
        binding.mathRecycler

        binding.mathRecycler.setHasFixedSize(true)
        binding.mathRecycler.layoutManager = LinearLayoutManager(context)

        setupAdapter()
        mAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.swipeRefresh.setOnRefreshListener {
            mAdapter.refresh()
        }
        return binding.root

    }

    override fun onStart() {
        super.onStart()

        mAdapter.startListening();
    }

    override fun onPause() {
        super.onPause()
        mAdapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        mAdapter.startListening()
    }
    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter.stopListening()
    }

    private fun setupAdapter() {
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
                        "category" to categoryName,
                        "id" to postRef!!.id)
                    findNavController().navigate(R.id.action_mainFragment_to_detailFragment, bundle)
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

        binding.mathRecycler.adapter = mAdapter


    }


}