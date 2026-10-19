package com.s8139858.assignment2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.s8139858.assignment2.R
import com.s8139858.assignment2.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity hosting the navigation graph. The shared toolbar follows the
 * NavController: Login and Dashboard are top-level destinations, Details shows
 * an Up affordance automatically.
 *
 * @AndroidEntryPoint opts this Android framework class into Hilt injection
 * (same annotation sits on every Fragment); @Inject constructors are used in
 * ViewModels because framework classes cannot be constructor-injected directly.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.dashboardFragment),
        )
        binding.toolbar.setupWithNavController(navHost.navController, appBarConfiguration)
    }
}
