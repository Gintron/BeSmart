package com.r.besmart.profileScreen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.r.besmart.R
import com.r.besmart.adapter.PagerAdapter
import com.r.besmart.databinding.FragmentProfileBinding
import com.r.besmart.mainScreen.MainViewModel
import kotlinx.android.synthetic.main.fragment_main.*

class ProfileFragment : Fragment() {

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_profile, container, false )
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAuthenticationState()
        binding.userName.text = FirebaseAuth.getInstance().currentUser?.displayName

        val adapter = PagerAdapter(childFragmentManager)
        adapter.addFragment(MyAskedFragment(), "Asked")
        adapter.addFragment(MyAnsweredFragment(), "Answered")
        pager.adapter = adapter
        tab_layout.setupWithViewPager(pager)
    }
    private fun observeAuthenticationState() {
        binding.logOut.setOnClickListener {
            AuthUI.getInstance().signOut(requireContext())

        }
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer {authenticationState->
            when(authenticationState){
                MainViewModel.AuthenticationState.UNAUTHENTICATED -> {
                    findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
                }
            }
        })

    }

}