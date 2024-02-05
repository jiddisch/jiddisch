package com.jiddisch.app5.utils

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.jiddisch.app5.R
import com.jiddisch.app5.api.Category


class SubCategoryListAdapter(
    private var subCategoryList: List<Category>,
    private val navController: NavController,
) :
    RecyclerView.Adapter<SubCategoryListAdapter.SubCategoryViewHolder>() {

    class SubCategoryViewHolder(categoryItemView: View) :
        RecyclerView.ViewHolder(categoryItemView) {

        val categoryNameTextView: TextView =
            categoryItemView.findViewById(R.id.categoryNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubCategoryViewHolder {
        val categoryItemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_item, parent, false)
        return SubCategoryViewHolder(categoryItemView)
    }

    override fun onBindViewHolder(holder: SubCategoryViewHolder, position: Int) {
        val currentCategory = subCategoryList[position]
        holder.categoryNameTextView.text = currentCategory.name

        holder.itemView.setOnClickListener {
            onSubCategoryClick(currentCategory)
        }
    }

    private fun onSubCategoryClick(subCategory: Category) {
        val analyticsBundle = Bundle().apply {
            putString("sub_category", subCategory.name)
        }
        AnalyticsUtils.logEvent(EventConstants.SUB_CATEGORY_BUTTON_CLICKED, analyticsBundle)

        val navBundle = Bundle()
        navBundle.putInt("categoryId", subCategory.id)
        navController.navigate(R.id.action_basePortalFragment_to_word_fragment, navBundle)
    }

    override fun getItemCount() = subCategoryList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSubCategoryList(newCategoryList: List<Category>) {
        subCategoryList = newCategoryList
        notifyDataSetChanged()
    }
}

