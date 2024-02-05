package com.jiddisch.app5.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.jiddisch.app5.R
import com.jiddisch.app5.api.PreferencesHelper
import com.jiddisch.app5.databinding.ActivityMainBinding
import com.jiddisch.app5.utils.AnalyticsUtils
import com.jiddisch.app5.utils.Constants
import com.jiddisch.app5.utils.EventConstants


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsUtils.init()
        setTheme(R.style.Theme_Yiddish)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainFragmentLayout) as? NavHostFragment
        navController = navHostFragment?.navController!!

        binding.menuButton.setOnClickListener {
            binding.menuLinearLayout.visibility = View.VISIBLE
            binding.dimmingView.visibility = View.VISIBLE
            AnalyticsUtils.logEvent(EventConstants.MENU_BUTTON_CLICKED)
        }
        binding.closeMenu.setOnClickListener {
            AnalyticsUtils.logEvent(EventConstants.CLOSE_MENU_BUTTON_CLICKED)
            closeMenu()
        }
        binding.dimmingView.setOnClickListener {
            AnalyticsUtils.logEvent(EventConstants.DIMMING_VIEW_CLICKED)
            closeMenu()
        }
        binding.homeMenuText.setOnClickListener {
            navController.navigate(R.id.home_fragment)
            closeMenu()
            AnalyticsUtils.logEvent(EventConstants.HOME_MENU_CLICKED)
        }

        binding.learnMenuText.setOnClickListener {
            preferencesHelper.save(Constants.PORTAL_MODE, Constants.LEARN)
            navController.navigate(R.id.base_portal_fragment)
            closeMenu()
            AnalyticsUtils.logEvent(EventConstants.LEARN_MENU_CLICKED)
        }

        binding.practiceMenuText.setOnClickListener {
            preferencesHelper.save(Constants.PORTAL_MODE, Constants.PRACTICE_MODE)
            navController.navigate(R.id.base_portal_fragment)
            closeMenu()
            AnalyticsUtils.logEvent(EventConstants.PRACTICE_MENU_CLICKED)
        }
    }

    private fun closeMenu() {
        binding.menuLinearLayout.visibility = View.GONE
        binding.dimmingView.visibility = View.GONE
    }

    fun updateBreadcrumb(text: String) {
        binding.toolbarBreadcrumb.text = text
        binding.toolbarBreadcrumb.visibility = View.VISIBLE
    }

    fun hideBreadcrumb() {
        binding.toolbarBreadcrumb.visibility = View.GONE
    }
}
