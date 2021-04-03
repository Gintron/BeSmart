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
import com.r.besmart.R
import com.r.besmart.adapter.MyAnsweredViewHolder
import com.r.besmart.databinding.FragmentMyAnsweredBinding
import com.r.besmart.databinding.PostItemBinding
import com.r.besmart.model.Answer

class MyAnsweredFragment : Fragment() {
    private lateinit var binding:FragmentMyAnsweredBinding
    private lateinit var viewModel:MyAnsweredViewModel
    private lateinit var mAdapter: FirestorePagingAdapter<Answer, MyAnsweredViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_answered, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this).get(MyAnsweredViewModel::class.java)
        binding.answerRecycler.setHasFixedSize(true)
        binding.answerRecycler.layoutManager = LinearLayoutManager(context)

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

        val options = FirestorePagingOptions.Builder<Answer>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, config, Answer::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<Answer, MyAnsweredViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAnsweredViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PostItemBinding.inflate(inflater, parent, false)
                return MyAnsweredViewHolder(binding)
            }

            override fun onBindViewHolder(viewHolder: MyAnsweredViewHolder, position: Int, answer: Answer) {
                val postRef = getItem(position)
                viewHolder.bind(answer)

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

        binding.answerRecycler.adapter = mAdapter


    }
}