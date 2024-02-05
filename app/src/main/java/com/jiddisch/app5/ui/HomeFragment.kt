package com.jiddisch.app5.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jiddisch.app5.R
import com.jiddisch.app5.api.PreferencesHelper
import com.jiddisch.app5.databinding.FragmentHomeBinding
import com.jiddisch.app5.utils.AnalyticsUtils
import com.jiddisch.app5.utils.Constants
import com.jiddisch.app5.utils.EventConstants


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.learnPortalBtn.setOnClickListener {
            navigateToPortal(Constants.LEARN)
            AnalyticsUtils.logEvent(EventConstants.LEARN_PORTAL_BTN_CLICKED)
        }

        binding.practicePortalBtn.setOnClickListener {
            navigateToPortal(Constants.PRACTICE_MODE)
            AnalyticsUtils.logEvent(EventConstants.PRACTICE_PORTAL_BTN_CLICKED)
        }

        return binding.root
    }


    private fun navigateToPortal(portalMode: String) {
        if (isAdded) {
            preferencesHelper.save(Constants.PORTAL_MODE, portalMode)
            findNavController().navigate(R.id.action_homeFragment_to_basePortalFragment)
        } else {
            Log.d("HomeFragment", "Fragment not added when trying to navigate")
        }
    }

}
