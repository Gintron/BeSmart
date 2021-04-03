package com.r.besmart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.r.besmart.mainScreen.MainViewModel



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        val model: MainViewModel by viewModels()
        model.authenticationState.observe(this, Observer {authenticationState->
            when(authenticationState){
                MainViewModel.AuthenticationState.UNAUTHENTICATED->{

                    bottomNavView.visibility = View.GONE
                }
                else ->{
                    bottomNavView.visibility = View.VISIBLE
                }
            }
        })
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment?: return

        // Set up Action Bar
        val navController = host.navController

        setupBottomNavMenu(navController)


    }
    private fun setupBottomNavMenu(navController: NavController) {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNav?.setupWithNavController(navController)

    }
}