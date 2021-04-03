package com.r.besmart.detailScreen

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.r.besmart.R
import com.r.besmart.adapter.ApplyViewHolder
import com.r.besmart.databinding.AppliedItemBinding
import com.r.besmart.databinding.FragmentDetailBinding
import com.r.besmart.model.Apply
import com.r.besmart.repository.FirebaseRepository

class DetailFragment : Fragment() {
    private lateinit var mAdapter: FirestoreRecyclerAdapter<Apply, ApplyViewHolder>
    private lateinit var viewModel: DetailViewModel
    private lateinit var binding: FragmentDetailBinding
    private val firebaseRepository = FirebaseRepository()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detail, container, false
        )

        viewModel = ViewModelProvider(this).get(DetailViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner

        val id = arguments?.getString("id")
        val category = arguments?.getString("category")
        viewModel.postID = id
        viewModel.category = category

        if (viewModel.post.value == null)
            viewModel.getPostInfo()

        binding.recyclerDetail.setHasFixedSize(true)
        binding.recyclerDetail.layoutManager = LinearLayoutManager(context)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.post.observe(viewLifecycleOwner, Observer {
            binding.description.text = viewModel.post.value!!.description
            setupAdapter()
            mAdapter.startListening()
            if (firebaseRepository.userid != viewModel.post.value!!.userid) {
                viewModel.haveIAppliedForThisPost()
                viewModel.amIAccepted()

            } else {
                viewModel.haveIacceptedAnyAnswer()
            }
            viewModel.isAnsweredF()
        })
        viewModel.loaded.observe(viewLifecycleOwner, Observer {
            Glide.with(binding.image).load(viewModel.post.value!!.image?.get(viewModel.position))
                .into(binding.image)
            displayButtonsAndRecycler()

        })

        viewModel.isappliedForSolving.observe(viewLifecycleOwner, Observer { applied ->
            if (applied) {
                binding.applyBtn.setBackgroundResource(R.drawable.cancel_button_shape)
                binding.applyBtn.setText(R.string.cancel)
            } else {
                binding.applyBtn.setBackgroundResource(R.drawable.apply_button_shape)
                binding.applyBtn.setText(R.string.answer)
            }
        })

        viewModel.submitAnswerButton.observe(viewLifecycleOwner, Observer { accepted ->
            if (accepted) {
                if (firebaseRepository.userid != viewModel.post.value!!.userid) {
                    binding.viewSubmitAnswer.setText("Submit Answer")
                    binding.viewSubmitAnswer.visibility = View.VISIBLE
                }
            } else {
                binding.viewSubmitAnswer.visibility = View.GONE
            }

        })
        viewModel.isAnswered.observe(viewLifecycleOwner, Observer { answered ->
            if (answered && viewModel.post.value!!.userid!! != firebaseRepository.userid)
                binding.viewSubmitAnswer.setText("View my Answer")
        })
        viewModel.viewAnswerButton.observe(viewLifecycleOwner, Observer { accepted ->
            if (accepted) {
                binding.viewSubmitAnswer.setText("View Answer")
                binding.viewSubmitAnswer.visibility = View.VISIBLE


            } else {
                binding.viewSubmitAnswer.visibility = View.GONE
            }

        })



        binding.viewSubmitAnswer.setOnClickListener {
            if (binding.viewSubmitAnswer.text == "Submit Answer") {
                val bundle = bundleOf("id" to viewModel.postID)
                findNavController().navigate(R.id.action_detailFragment_to_addFragment, bundle)
            } else if (binding.viewSubmitAnswer.text == "View Answer") {
                if (viewModel.isAnswered.value!! && !viewModel.post.value!!.answerid!!.isEmpty()) {
                    val bundle = bundleOf("answerid" to viewModel.post.value!!.answerid)
                    findNavController().navigate(
                        R.id.action_detailFragment_to_viewAnswerFragment,
                        bundle
                    )
                } else {
                    Toast.makeText(context, "Answer not supplied yet", Toast.LENGTH_SHORT).show()
                }
            }


        }

        binding.next.setOnClickListener {
            viewModel.increment()
            Glide.with(binding.image).load(viewModel.post.value!!.image?.get(viewModel.position))
                .into(binding.image)
        }
        binding.before.setOnClickListener {
            viewModel.decrement()
            Glide.with(binding.image).load(viewModel.post.value!!.image?.get(viewModel.position))
                .into(binding.image)
        }

        binding.applyBtn.setOnClickListener {
            if (viewModel.isappliedForSolving.value == true) {
                viewModel.deleteApply()
            } else {
                viewModel.applyUserForSolvingProblem()
            }
        }

    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    private fun setupAdapter() {

        val mPostsCollection =
            firebaseRepository.firestoreDB.collection("Apply")
                .whereEqualTo("postid", viewModel.post.value!!.id)
        val mQuery = mPostsCollection.orderBy("time", Query.Direction.DESCENDING)


        val options = FirestoreRecyclerOptions.Builder<Apply>()
            .setLifecycleOwner(this)
            .setQuery(mQuery, Apply::class.java)
            .build()

        mAdapter = object : FirestoreRecyclerAdapter<Apply, ApplyViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplyViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = AppliedItemBinding.inflate(inflater, parent, false)
                return ApplyViewHolder(binding)
            }

            override fun onBindViewHolder(viewHolder: ApplyViewHolder, p1: Int, apply: Apply) {
                viewHolder.bind(apply)
                val applyRef = snapshots.getSnapshot(p1)
                viewHolder.itemView.setOnClickListener {
                    viewModel.haveIacceptedAnyAnswer()
                    if (!apply.accepted!!){
                        val builder = AlertDialog.Builder(context, R.style.DialogTheme)
                        builder.setMessage(R.string.pick_user_dialog)
                            .setPositiveButton(R.string.positive,
                                DialogInterface.OnClickListener { dialog, id ->

                                    if (!viewModel.viewAnswerButton.value!!)
                                        viewModel.acceptUser(applyRef.id)
                                    else
                                        Toast.makeText(
                                            context,
                                            "You can only accept one user",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                })
                            .setNegativeButton(R.string.negative,
                                DialogInterface.OnClickListener { dialog, id ->
                                    // User cancelled the dialog
                                })
                        // Create the AlertDialog object and return it
                        builder.create()
                            ?: throw IllegalStateException("Activity cannot be null")
                        val mDialog = builder.create()
                        mDialog.show()

                    }

                    else if(apply.accepted!! && !viewModel.post.value!!.answered!!){
                        val builder = AlertDialog.Builder(context, R.style.DialogTheme)
                        builder.setMessage(R.string.unpick_user_dialog)
                            .setPositiveButton(R.string.positive,
                                DialogInterface.OnClickListener { dialog, id ->

                                    if (!viewModel.viewAnswerButton.value!!)
                                        //viewModel.unAcceptUser(applyRef.id)
                                    else
                                        Toast.makeText(
                                            context,
                                            "I don`t know",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                })
                            .setNegativeButton(R.string.negative,
                                DialogInterface.OnClickListener { dialog, id ->
                                    // User cancelled the dialog
                                })
                        // Create the AlertDialog object and return it
                        builder.create()
                            ?: throw IllegalStateException("Activity cannot be null")
                        val mDialog = builder.create()
                        mDialog.show()

                    }
                }
            }

        }
        binding.recyclerDetail.adapter = mAdapter
    }

    private fun displayButtonsAndRecycler() {
        if (viewModel.post.value!!.userid != firebaseRepository.userid) {
            binding.applyBtn.visibility = View.VISIBLE
            binding.recyclerDetail.visibility = View.GONE
            binding.pickUserTxt.visibility = View.GONE
        }
    }
}
