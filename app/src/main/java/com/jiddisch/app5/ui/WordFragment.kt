package com.jiddisch.app5.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.flexbox.FlexboxLayout
import com.jiddisch.app5.R
import com.jiddisch.app5.api.ApiService
import com.jiddisch.app5.api.Category
import com.jiddisch.app5.api.CustomCallback
import com.jiddisch.app5.api.PreferencesHelper
import com.jiddisch.app5.api.Word
import com.jiddisch.app5.utils.Constants
import com.jiddisch.app5.utils.WordAdapter
import retrofit2.Call
import retrofit2.Response

class WordFragment : Fragment(), WordAdapter.WordInteractionListener {
    private lateinit var wordList: MutableList<Word>
    private lateinit var viewPager: ViewPager2
    private lateinit var progressBar: ProgressBar
    private lateinit var rootView: View
    private lateinit var bulletsContainer: FlexboxLayout
    private var correctFirstTryAnswers: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_word, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inflater = requireActivity().layoutInflater

        progressBar = rootView.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        viewPager = view.findViewById(R.id.wordViewPager)
        viewPager.layoutDirection = View.LAYOUT_DIRECTION_RTL

        bulletsContainer = view.findViewById(R.id.bulletsContainer)
        bulletsContainer.layoutDirection = View.LAYOUT_DIRECTION_RTL

        val categoryId = arguments?.getInt("categoryId") ?: 0
        fetchCategoryAndDisplayBreadcrumb(categoryId)

        wordList = mutableListOf()

        ApiService(requireContext()).getWordsByCategoryId(categoryId,
            object : CustomCallback<List<Word>> {
                @SuppressLint("InflateParams")
                override fun onResponse(call: Call<List<Word>>?, response: Response<List<Word>>) {
                    progressBar.visibility = View.GONE
                    wordList = response.body()?.toMutableList() ?: mutableListOf()

                    val preferencesHelper = PreferencesHelper(requireContext())
                    val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)

                    if (portalMode == Constants.PRACTICE_MODE) {
                        wordList.shuffle()
                        bulletsContainer.visibility = View.GONE
                    } else {
                        bulletsContainer.visibility = View.VISIBLE
                    }

                    val wordAdapter = WordAdapter(wordList, this@WordFragment)
                    viewPager.adapter = wordAdapter

                    for (i in wordList.indices) {
                        val bulletLayout = inflater.inflate(
                            R.layout.item_bullet, null, false
                        ) as LinearLayout
                        val bullet = bulletLayout.getChildAt(0) as TextView
                        bullet.text = getString(R.string.bullet_text, i + 1)
                        bulletsContainer.addView(bulletLayout)

                        bullet.setOnClickListener {
                            viewPager.currentItem = i
                        }
                    }
                    viewPager.registerOnPageChangeCallback(object :
                        ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            for (i in wordList.indices) {
                                val bulletLayout = bulletsContainer.getChildAt(i) as LinearLayout
                                val bullet = bulletLayout.getChildAt(0) as TextView

                                if (i == position) {
                                    bullet.setBackgroundResource(R.drawable.round_bullet_active)
                                    bullet.setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(), R.color.light
                                        )
                                    )
                                } else {
                                    bullet.setBackgroundResource(R.drawable.round_bullet)
                                    bullet.setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(), R.color.color01
                                        )
                                    )
                                }
                            }
                        }
                    })
                }

                override fun onFailure(call: Call<List<Word>>?, t: Throwable) {
                    Log.e("WordFragment", "Error fetching words", t)
                }
            })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                for (i in wordList.indices) {
                    val bulletLayout = bulletsContainer.getChildAt(i) as LinearLayout
                    val bullet = bulletLayout.getChildAt(0) as TextView

                    if (i == position) {
                        bullet.setBackgroundResource(R.drawable.round_bullet_active)
                        bullet.setTextColor(
                            ContextCompat.getColor(
                                requireContext(), R.color.light
                            )
                        )
                    } else {
                        bullet.setBackgroundResource(R.drawable.round_bullet)
                        bullet.setTextColor(
                            ContextCompat.getColor(
                                requireContext(), R.color.color01
                            )
                        )
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.updateBreadcrumb("")
        (activity as? MainActivity)?.hideBreadcrumb()
    }

    private fun fetchCategoryAndDisplayBreadcrumb(categoryId: Int) {
        ApiService(requireContext()).getCategoryById(
            categoryId,
            object : CustomCallback<List<Category>> {
                override fun onResponse(
                    call: Call<List<Category>>?, response: Response<List<Category>>
                ) {
                    if (response.isSuccessful) {
                        val categories = response.body()
                        if (categories != null && categories.isNotEmpty()) {
                            val currentCategory = categories[0]
                            (requireActivity() as MainActivity).updateBreadcrumb(currentCategory.name)
                        }
                    } else {
                        Log.e("WordFragment", "Error fetching category: ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<List<Category>>?, t: Throwable) {
                    Log.e("WordFragment", "Error fetching category", t)
                }
            })
    }

    override fun onCorrectWordSelected(isFirstAttempt: Boolean) {
        if (isFirstAttempt) {
            correctFirstTryAnswers++
        }
        if (viewPager.currentItem == wordList.size - 1) {
            showCompletionDialog()
        } else {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }
    }

    private fun showCompletionDialog() {
        val message = "You answered $correctFirstTryAnswers of ${wordList.size}"
        val builder =
            AlertDialog.Builder(requireContext()).setTitle("Quiz Completed").setMessage(message)
                .setPositiveButton("OK") { _, _ ->
                    val action = WordFragmentDirections.actionWordFragmentToHomeFragment()
                    findNavController().navigate(action)
                }

        val alertDialog = builder.create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(requireContext(), R.color.color01)
        )
    }
}

