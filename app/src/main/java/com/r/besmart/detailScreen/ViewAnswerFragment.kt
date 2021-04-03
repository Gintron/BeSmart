package com.r.besmart.detailScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.r.besmart.R
import com.r.besmart.databinding.FragmentViewAnswerBinding


class ViewAnswerFragment : Fragment() {
    private lateinit var viewModel: ViewAnswerViewModel
    private lateinit var binding: FragmentViewAnswerBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_answer, container, false)
        viewModel = ViewModelProvider(this).get(ViewAnswerViewModel::class.java)

        viewModel.answerid = arguments?.getString("answerid")

        if(viewModel.answer.value == null)
            viewModel.getAnswer()

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.answer.observe(viewLifecycleOwner, Observer {
            binding.description.text = viewModel.answer.value!!.description
        })

        viewModel.loaded.observe(viewLifecycleOwner, Observer {loaded->
            Glide.with(binding.zoomInImageView).load(viewModel.answer.value!!.image?.get(viewModel.position))
                .into(binding.zoomInImageView)
        })
        binding.next.setOnClickListener {
            viewModel.increment()
            Glide.with(binding.zoomInImageView).load(viewModel.answer.value!!.image?.get(viewModel.position))
                .into(binding.zoomInImageView)
        }
        binding.before.setOnClickListener {
            viewModel.decrement()
            Glide.with(binding.zoomInImageView).load(viewModel.answer.value!!.image?.get(viewModel.position))
                .into(binding.zoomInImageView)
        }
    }
}

