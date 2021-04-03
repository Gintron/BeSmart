package com.r.besmart.mainScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.r.besmart.R
import com.r.besmart.adapter.PagerAdapter
import kotlinx.android.synthetic.main.fragment_main.*



class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PagerAdapter(childFragmentManager)
        adapter.addFragment(MathFragment("Mathematics"), "Matematika")
        adapter.addFragment(MathFragment("Physics"), "Fizika")
        pager.adapter = adapter
        tab_layout.setupWithViewPager(pager)
        observeAuthenticateState()



    }

    private fun observeAuthenticateState() {
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authentucationState ->
            when (authentucationState) {
                MainViewModel.AuthenticationState.AUTHENTICATED -> {
                    viewModel.checkUserExistsInDatabase()
                }
                else -> {
                    findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
                }
            }

        })


    }

}
