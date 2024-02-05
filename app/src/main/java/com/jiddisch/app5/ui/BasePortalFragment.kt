package com.jiddisch.app5.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiddisch.app5.R
import com.jiddisch.app5.api.ApiService
import com.jiddisch.app5.api.CustomCallback
import com.jiddisch.app5.api.MainCategory
import com.jiddisch.app5.api.PreferencesHelper
import com.jiddisch.app5.utils.CategoryModel
import com.jiddisch.app5.utils.Constants
import com.jiddisch.app5.utils.MainCategoryButtonListAdapter
import retrofit2.Call
import retrofit2.Response

class BasePortalFragment : Fragment() {
    private lateinit var rootView: View
    private val preferencesHelper: PreferencesHelper by lazy {
        PreferencesHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_base_portal, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView: TextView = rootView.findViewById(R.id.title)
        val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)

        titleTextView.text = when (portalMode) {
            Constants.LEARN -> getString(R.string.learn_path)
            Constants.PRACTICE_MODE -> getString(R.string.practice_path)
            else -> getString(R.string.learn_path)
        }

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val navController = findNavController()

        val progressBar: ProgressBar = rootView.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        ApiService(requireContext()).getMainCategories(object : CustomCallback<List<MainCategory>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(
                call: Call<List<MainCategory>>?,
                response: Response<List<MainCategory>>
            ) {
                if (response.isSuccessful) {
                    progressBar.visibility = View.GONE
                    val mainCategories = response.body()
                    val buttonList = mutableListOf(CategoryModel("Alphabet", 0))
                    buttonList.addAll(mainCategories?.map { CategoryModel(it.name, it.id) }
                        ?: emptyList())
                    val buttonAdapter =
                        MainCategoryButtonListAdapter(
                            buttonList,
                            requireContext(),
                            navController,
                        )
                    recyclerView.adapter = buttonAdapter
                    buttonAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<List<MainCategory>>?, t: Throwable) {
                Log.d("BasePortalFragment", "Error fetching main categories", t)
            }
        })
    }

}
