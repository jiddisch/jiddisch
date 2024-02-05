package com.jiddisch.app5.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.jiddisch.app5.R
import com.jiddisch.app5.api.PreferencesHelper

class AlphabetLetterRecyclerViewAdapter(
    private val alphabetLetterList: List<AlphabetLetterModel>,
    private val viewPager: ViewPager2,
    private val context: Context
) :
    RecyclerView.Adapter<AlphabetLetterRecyclerViewAdapter.ViewHolder>() {
    var selectedPosition: Int = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alphabetLetter: TextView = itemView.findViewById(R.id.alphabetLetter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alphabet_bullet_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alphabetLetterModel = alphabetLetterList[position]
        holder.alphabetLetter.text = alphabetLetterModel.yiddish_letter

        if (selectedPosition == position) {
            holder.alphabetLetter.setBackgroundResource(R.drawable.round_bullet_active)
            holder.alphabetLetter.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
        } else {
            holder.alphabetLetter.setBackgroundResource(R.drawable.round_bullet)
            holder.alphabetLetter.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.color01
                )
            )
        }

        holder.alphabetLetter.setOnClickListener {
            selectedPosition = position
            viewPager.setCurrentItem(position, true)
            notifyDataSetChanged()

            // Logging the bullet click
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, "bullet_click")
                putInt("bullet_number", position)
                val preferencesHelper = PreferencesHelper(context)
                val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)
                putString("portal_mode", portalMode)
            }
            AnalyticsUtils.logEvent(EventConstants.ALPHABET_BULLET_SELECTED, bundle)
        }

    }

    override fun getItemCount(): Int = alphabetLetterList.size
}

