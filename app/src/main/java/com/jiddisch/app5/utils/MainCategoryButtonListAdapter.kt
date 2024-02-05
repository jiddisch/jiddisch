package com.jiddisch.app5.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiddisch.app5.R
import com.jiddisch.app5.api.ApiService
import com.jiddisch.app5.api.Category
import com.jiddisch.app5.api.CustomCallback
import com.jiddisch.app5.api.PreferencesHelper
import retrofit2.Call
import retrofit2.Response

data class CategoryModel(val text: String, val id: Int)

class MainCategoryButtonListAdapter(
    private val mainCategoryList: List<CategoryModel>?,
    private val context: Context,
    private val navController: NavController
) : RecyclerView.Adapter<MainCategoryButtonListAdapter.MainCategoryViewHolder>() {
    private val preferencesHelper = PreferencesHelper(context)
    private var lastOpenedMainCategory: Int = -1

    init {
        lastOpenedMainCategory =
            preferencesHelper.get("lastOpenedMainCategory_$preferencesHelper.get('portalMode')")
                ?.toIntOrNull() ?: -1
    }

    inner class MainCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainCategoryButton: TextView = itemView.findViewById(R.id.mainCategoryButton)
        val subCategoryList: RecyclerView = itemView.findViewById(R.id.subCategoryList)

        init {
            subCategoryList.layoutManager = LinearLayoutManager(context)
            subCategoryList.adapter = SubCategoryListAdapter(emptyList(), navController)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_button_item, parent, false)
        return MainCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainCategoryViewHolder, position: Int) {
        holder.subCategoryList.visibility = View.GONE

        val currentMainCategory = mainCategoryList?.getOrNull(position) ?: CategoryModel("", -1)

        holder.mainCategoryButton.text = currentMainCategory.text
        holder.mainCategoryButton.setOnClickListener {
            onMainCategoryButtonClick(holder, currentMainCategory)
        }

        if (position % 2 == 0) {
            holder.mainCategoryButton.setBackgroundResource(R.drawable.bg_odd_btn)
        } else {
            holder.mainCategoryButton.setBackgroundResource(R.drawable.bg_even_btn)
        }

        if (currentMainCategory.id == lastOpenedMainCategory) {
            onMainCategoryButtonClick(holder, currentMainCategory)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onMainCategoryButtonClick(
        holder: MainCategoryViewHolder,
        currentMainCategory: CategoryModel,
    ) {
        val bundle = Bundle().apply {
            putString("main_category", currentMainCategory.text)
        }
        AnalyticsUtils.logEvent(EventConstants.CATEGORY_BUTTON_CLICKED, bundle)

        // if the mainCategory is the hardcoded Alphabet item
        if (currentMainCategory.id == 0) {
            navController.navigate(R.id.action_basePortalFragment_to_alphabetFragment)
        } else {
            if (holder.subCategoryList.visibility == View.VISIBLE) {
                holder.subCategoryList.visibility = View.GONE
                if (currentMainCategory.id == lastOpenedMainCategory) {
                    preferencesHelper.save(
                        "lastOpenedMainCategory_$preferencesHelper.get('portalMode')",
                        "-1"
                    )
                    lastOpenedMainCategory = -1
                }
            } else {
                holder.mainCategoryButton.text = currentMainCategory.text + " ..."
                getSubCategoriesByMainCategoryId(currentMainCategory.id) { subCategories, error ->
                    holder.mainCategoryButton.text = currentMainCategory.text
                    if (error != null) {
                        Log.e("API", error)
                        return@getSubCategoriesByMainCategoryId
                    }

                    if (subCategories?.size == 1) {

                        val bundle2 = Bundle()
                        bundle2.putInt("categoryId", subCategories[0].id)
                        navController.navigate(
                            R.id.action_basePortalFragment_to_word_fragment,
                            bundle2
                        )
                    } else {
                        holder.subCategoryList.visibility = View.VISIBLE
                        (holder.subCategoryList.adapter as SubCategoryListAdapter).updateSubCategoryList(
                            subCategories?.toList() ?: emptyList()
                        )
                        preferencesHelper.save(
                            "lastOpenedMainCategory_$preferencesHelper.get('portalMode')",
                            currentMainCategory.id.toString()
                        )
                        lastOpenedMainCategory = currentMainCategory.id
                    }
                }
            }
        }
    }

    private fun getSubCategoriesByMainCategoryId(
        categoryId: Int, callback: (List<Category>?, String?) -> Unit
    ) {
        ApiService(context).getSubCategoriesByCategoryId(
            categoryId,
            object : CustomCallback<List<Category>> {
                override fun onResponse(
                    call: Call<List<Category>>?, response: Response<List<Category>>
                ) {
                    if (response.isSuccessful) {
                        val subCategories = response.body()
                        callback(subCategories, null)
                    } else {
                        callback(
                            null,
                            "Error getting subcategories for category $categoryId: ${response.code()} ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<List<Category>>?, t: Throwable) {
                    callback(
                        null, "Error getting subcategories for category $categoryId: ${t.message}"
                    )
                }
            })
    }

    override fun getItemCount(): Int {
        return mainCategoryList?.size ?: 0
    }
}

