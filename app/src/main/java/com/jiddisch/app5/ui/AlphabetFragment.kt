package com.jiddisch.app5.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.jiddisch.app5.R
import com.jiddisch.app5.api.AlphabetLetter
import com.jiddisch.app5.api.ApiService
import com.jiddisch.app5.api.CustomCallback
import com.jiddisch.app5.api.PreferencesHelper
import com.jiddisch.app5.utils.AlphabetLetterAdapter
import com.jiddisch.app5.utils.AlphabetLetterModel
import com.jiddisch.app5.utils.AlphabetLetterRecyclerViewAdapter
import com.jiddisch.app5.utils.AnalyticsUtils
import com.jiddisch.app5.utils.Constants
import com.jiddisch.app5.utils.EventConstants
import retrofit2.Call
import retrofit2.Response

class AlphabetFragment : Fragment(), AlphabetLetterAdapter.LetterInteractionListener {
    private lateinit var alphabetLetterList: MutableList<AlphabetLetterModel>
    private lateinit var viewPager: ViewPager2
    private lateinit var alphabetRecyclerView: RecyclerView
    private lateinit var rootView: View
    private lateinit var progressBar: ProgressBar
    private var correctFirstTryAnswers: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // not sure what is this about
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        rootView = inflater.inflate(R.layout.fragment_alphabet, container, false)

        progressBar = rootView.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        viewPager = rootView.findViewById(R.id.viewPager)
        viewPager.layoutDirection = View.LAYOUT_DIRECTION_RTL

        alphabetRecyclerView = rootView.findViewById(R.id.alphabetRecyclerView)
        alphabetRecyclerView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        alphabetRecyclerView.visibility = View.GONE

        val preferencesHelper = PreferencesHelper(requireContext())
        val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)

        ApiService(requireContext()).getAlphabetLetters(object :
            CustomCallback<List<AlphabetLetter>> {
            override fun onResponse(
                call: Call<List<AlphabetLetter>>?, response: Response<List<AlphabetLetter>>
            ) {
                progressBar.visibility = View.GONE
                val toggleButton = rootView.findViewById<ToggleButton>(R.id.toggleButton)
                val alphabetLetters = response.body()
                alphabetLetterList = alphabetLetters?.sortedWith(compareBy { it.sort })?.map {
                    AlphabetLetterModel(
                        id = it.id,
                        yiddish_letter = it.yiddish_letter,
                        yiddish_letter_name = it.yiddish_letter_name,
                        latin_letter_name = it.latin_letter_name,
                        sort = it.sort,
                        transcription = it.transcription.joinToString(separator = ", "),
                        voice = it.voice!!
                    )
                }?.toMutableList() ?: mutableListOf()
                if (portalMode == Constants.PRACTICE_MODE) {
                    alphabetLetterList.shuffled()
                    toggleButton.visibility = View.VISIBLE
                } else {
                    toggleButton.isChecked = false
                    toggleButton.visibility = View.VISIBLE
                }
                val alphabetLetterAdapter = AlphabetLetterAdapter(
                    alphabetLetterList, this@AlphabetFragment
                )
                viewPager.adapter = alphabetLetterAdapter

                alphabetRecyclerView.layoutManager =
                    GridLayoutManager(context, 6, GridLayoutManager.VERTICAL, false)
                alphabetRecyclerView.adapter = AlphabetLetterRecyclerViewAdapter(
                    alphabetLetterList, viewPager, requireContext()
                )

                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        val adapter =
                            alphabetRecyclerView.adapter as AlphabetLetterRecyclerViewAdapter
                        val oldSelectedPosition = adapter.selectedPosition
                        adapter.selectedPosition = position
                        adapter.notifyItemChanged(oldSelectedPosition)
                        adapter.notifyItemChanged(position)
                    }
                })
            }

            override fun onFailure(call: Call<List<AlphabetLetter>>?, t: Throwable) {
                Log.e("AlphabetFragment", "Error fetching alphabet", t)
            }
        })

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toggleButton = rootView.findViewById<ToggleButton>(R.id.toggleButton)
        val preferencesHelper = PreferencesHelper(requireContext())
        val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)

        if (portalMode == Constants.LEARN) {
            toggleButton.setOnCheckedChangeListener { _, isChecked ->
                val bundle = Bundle()
                bundle.putString("button", "toggleButton")
                bundle.putBoolean("is_checked", isChecked)
                AnalyticsUtils.logEvent(EventConstants.ALPHABET_TOGGLE_BUTTON_CLICK, bundle)

                if (isChecked) {
                    alphabetRecyclerView.visibility = View.VISIBLE
                    toggleButton.setBackgroundResource(R.drawable.toggle_arrow_down)
                } else {
                    alphabetRecyclerView.visibility = View.GONE
                    toggleButton.setBackgroundResource(R.drawable.toggle_arrow_up)
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            alphabetRecyclerView.visibility = View.GONE

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            alphabetRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCorrectWordSelected(isFirstAttempt: Boolean) {
        if (isFirstAttempt) {
            correctFirstTryAnswers++
        }
        if (viewPager.currentItem == alphabetLetterList.size - 1) {
            showCompletionDialog()
        } else {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }
    }

    private fun showCompletionDialog() {
        val message = "You answered $correctFirstTryAnswers of ${alphabetLetterList.size}"
        val builder =
            AlertDialog.Builder(requireContext()).setTitle("Quiz Completed").setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    val action =
                        AlphabetFragmentDirections.actionAlphabetFragmentToBasePortalFragment()
                    findNavController().navigate(action)
                }

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(requireContext(), R.color.color01)
        )
    }
}
