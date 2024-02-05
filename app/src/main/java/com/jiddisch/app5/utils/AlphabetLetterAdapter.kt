package com.jiddisch.app5.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jiddisch.app5.R
import com.jiddisch.app5.api.PreferencesHelper
import java.io.IOException

data class AlphabetLetterModel(
    val id: Int,
    val yiddish_letter: String,
    val yiddish_letter_name: String,
    val latin_letter_name: String,
    val sort: Int,
    val transcription: String?,
    val voice: String,
)

class AlphabetLetterAdapter(
    private val alphabetLetterList: List<AlphabetLetterModel>,
    private val interactionListener: LetterInteractionListener
) :
    RecyclerView.Adapter<AlphabetLetterAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val yiddishLetter: TextView = itemView.findViewById(R.id.yiddish_letter)
        val yiddishLetterName: TextView = itemView.findViewById(R.id.yiddish_letter_name)
        val latinLetterName: TextView = itemView.findViewById(R.id.latin_letter_name)
        val transcription: TextView = itemView.findViewById(R.id.transcription)
        val options: List<TextView> = listOf(
            itemView.findViewById(R.id.option_1),
            itemView.findViewById(R.id.option_2),
            itemView.findViewById(R.id.option_3),
            itemView.findViewById(R.id.option_4)
        )
        val speakerIcon: ImageView = itemView.findViewById(R.id.speaker_icon)
    }

    interface LetterInteractionListener {
        fun onCorrectWordSelected(isFirstAttempt: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alphabet_letter_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alphabetLetterModel = alphabetLetterList[position]
        holder.yiddishLetter.text = alphabetLetterModel.yiddish_letter
        holder.yiddishLetterName.text = alphabetLetterModel.yiddish_letter_name
        holder.latinLetterName.text = alphabetLetterModel.latin_letter_name
        holder.transcription.text = alphabetLetterModel.transcription

        holder.speakerIcon.setOnClickListener {
            val voiceUrl = alphabetLetterModel.voice
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                mediaPlayer.setDataSource(voiceUrl)
                mediaPlayer.prepare()  // might take time for buffering
                mediaPlayer.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        val preferencesHelper = PreferencesHelper(holder.itemView.context)
        val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)

        val analyticsBundle = Bundle().apply {
            putString("letter", alphabetLetterModel.yiddish_letter)
            putString("portal_mode", portalMode)
        }
        AnalyticsUtils.logEvent(EventConstants.ALPHABET_SLIDE, analyticsBundle)

        if (portalMode == Constants.PRACTICE_MODE) {
            setupPracticeMode(holder, alphabetLetterModel)
        } else {
            setupLearnMode(holder)
        }
    }

    private fun setupPracticeMode(holder: ViewHolder, currentLetter: AlphabetLetterModel) {
        holder.itemView.findViewById<ConstraintLayout>(R.id.layout_letter_name).visibility =
            View.GONE
        holder.transcription.visibility = View.GONE
        holder.itemView.findViewById<ConstraintLayout>(R.id.practice_layout).visibility =
            View.VISIBLE

        val otherLetters = alphabetLetterList.filter { it != currentLetter }
        val optionLetters = otherLetters.shuffled().take(3).toMutableList()
        optionLetters.add(currentLetter)
        optionLetters.shuffle()

        var isFirstAttempt = true
        var hasMovedToNext = false
        optionLetters.forEachIndexed { index, letter ->
            holder.options[index].text = letter.transcription
            holder.options[index].setBackgroundResource(R.drawable.rounded_background)
            holder.options[index].setOnClickListener { v ->

                val analyticsBundle = Bundle().apply {
                    putString("letter", currentLetter.yiddish_letter)
                    putString("portal_mode", Constants.PRACTICE_MODE)
                    putBoolean("is_correct", letter == currentLetter)
                    putBoolean("is_first_attempt", isFirstAttempt)
                }
                AnalyticsUtils.logEvent(
                    EventConstants.PRACTICE_MODE_OPTION_SELECTED,
                    analyticsBundle
                )

                if (letter == currentLetter) {
                    v.setBackgroundResource(R.drawable.rounded_background_green)
                    if (!hasMovedToNext) {
                        interactionListener.onCorrectWordSelected(isFirstAttempt)
                        hasMovedToNext = true
                    }
                    isFirstAttempt = false
                } else {
                    v.setBackgroundResource(R.drawable.rounded_background_red)
                    isFirstAttempt = false
                }
            }
        }
    }

    private fun setupLearnMode(holder: ViewHolder) {
        val nestedConstraintLayout =
            holder.itemView.findViewById<ConstraintLayout>(R.id.nested_constraint_layout)
        if (nestedConstraintLayout == null) {
            Log.d("AlphabetLetterAdapter", "nested_constraint_layout is null")
        } else {
            nestedConstraintLayout.visibility = View.GONE
        }

        val practiceLayout = holder.itemView.findViewById<ConstraintLayout>(R.id.practice_layout)
        if (practiceLayout == null) {
            Log.d("AlphabetLetterAdapter", "practice_layout is null")
        } else {
            practiceLayout.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = alphabetLetterList.size
}
