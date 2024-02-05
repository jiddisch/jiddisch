package com.jiddisch.app5.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jiddisch.app5.R
import com.jiddisch.app5.api.PreferencesHelper
import com.jiddisch.app5.api.Word
import java.util.Locale

class WordAdapter(
    private var words: MutableList<Word>,
    private val interactionListener: WordInteractionListener
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {
    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pluralWordContainer: ViewGroup = itemView.findViewById(R.id.plural_word_container)

        val yiddishWord: TextView = itemView.findViewById(R.id.yiddish_word)
        val meaningTextView: TextView = itemView.findViewById(R.id.english_word)
        val wordImage: ImageView = itemView.findViewById(R.id.word_image)
        val indefiniteArticle: TextView = itemView.findViewById(R.id.indefinite_article)
        val pluralForm: TextView = itemView.findViewById(R.id.plural_form)
        val yiddishTranscription: TextView = itemView.findViewById(R.id.yiddish_transcription)
        val options: List<TextView> = listOf(
            itemView.findViewById(R.id.option_1),
            itemView.findViewById(R.id.option_2),
            itemView.findViewById(R.id.option_3),
            itemView.findViewById(R.id.option_4)
        )
    }

    interface WordInteractionListener {
        fun onCorrectWordSelected(isFirstAttempt: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.word_item, parent, false)
        return WordViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val currentWord = words[position]
        val glide = Glide.with(holder.itemView.context)

        for (i in position + 1 until words.size) {
            glide.load(words[i].image).preload()
        }

        val singularIndicator: TextView = holder.itemView.findViewById(R.id.sing_indicator)
        val pluralIndicator: TextView = holder.itemView.findViewById(R.id.pl_indicator)

        if (currentWord.plural_form.isNullOrEmpty()) {
            singularIndicator.visibility = View.GONE
            pluralIndicator.visibility = View.GONE
        } else {
            singularIndicator.visibility = View.VISIBLE
            pluralIndicator.visibility = View.VISIBLE
        }

        glide.load(currentWord.image).centerCrop().into(holder.wordImage)

        holder.options.forEach { it.setBackgroundColor(Color.TRANSPARENT) }

        val preferencesHelper = PreferencesHelper(holder.itemView.context)
        val portalMode = preferencesHelper.get(Constants.PORTAL_MODE)

        if (portalMode == Constants.PRACTICE_MODE) {
            setupPracticeMode(holder, currentWord)
        } else {
            setupLearnMode(holder)
        }

        holder.yiddishWord.text = currentWord.yiddish_word
        holder.meaningTextView.text =
            currentWord.english_word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        holder.indefiniteArticle.text = currentWord.indefinite_article ?: ""
        holder.pluralForm.text = currentWord.plural_form
        if (currentWord.plural_form.isNullOrEmpty()) {
            holder.pluralWordContainer.visibility = View.GONE
        } else {
            holder.pluralWordContainer.visibility = View.VISIBLE
        }
        holder.yiddishTranscription.text = holder.itemView.context.getString(
            R.string.yiddish_transcription,
            currentWord.yiddish_transcription
        )

        holder.wordImage.clipToOutline = true
        Glide.with(holder.itemView.context).load(currentWord.image).centerCrop()
            .into(holder.wordImage)
    }

    private fun setupPracticeMode(holder: WordViewHolder, currentWord: Word) {
        holder.itemView.findViewById<ConstraintLayout>(R.id.nested_constraint_layout).visibility =
            View.GONE
        holder.itemView.findViewById<ConstraintLayout>(R.id.practice_layout).visibility =
            View.VISIBLE
        holder.yiddishTranscription.visibility = View.GONE

        val otherWords = words.filter { it != currentWord }
        val optionWords = otherWords.shuffled().take(3).toMutableList()
        optionWords.add(currentWord)
        optionWords.shuffle()

        var isFirstAttempt = true
        var hasMovedToNext = false
        optionWords.forEachIndexed { index, word ->
            holder.options[index].text = word.yiddish_word
            holder.options[index].setBackgroundResource(R.drawable.rounded_background)
            holder.options[index].setOnClickListener { v ->
                if (word == currentWord) {
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

    private fun setupLearnMode(holder: WordViewHolder) {
        holder.itemView.findViewById<ConstraintLayout>(R.id.practice_layout).visibility = View.GONE
        holder.itemView.findViewById<ConstraintLayout>(R.id.nested_constraint_layout).visibility =
            View.VISIBLE
    }

    override fun getItemCount() = words.size
}
