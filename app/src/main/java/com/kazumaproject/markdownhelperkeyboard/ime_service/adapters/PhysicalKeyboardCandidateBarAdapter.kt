package com.kazumaproject.markdownhelperkeyboard.ime_service.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kazumaproject.core.data.floating_candidate.CandidateItem
import com.kazumaproject.markdownhelperkeyboard.R

class PhysicalKeyboardCandidateBarAdapter :
    ListAdapter<CandidateItem, PhysicalKeyboardCandidateBarAdapter.CandidateViewHolder>(
        DiffCallback()
    ) {

    var onCandidateClicked: ((CandidateItem) -> Unit)? = null

    private var highlightedPosition: Int = RecyclerView.NO_POSITION

    fun updateHighlightPosition(newPosition: Int) {
        val normalizedPosition = newPosition.takeIf { it in currentList.indices }
            ?: RecyclerView.NO_POSITION
        if (highlightedPosition == normalizedPosition) return

        val previousPosition = highlightedPosition
        highlightedPosition = normalizedPosition
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
        if (highlightedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(highlightedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        return CandidateViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.physical_keyboard_candidate_bar_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(
            candidate = getItem(position),
            highlighted = position == highlightedPosition
        )
    }

    inner class CandidateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val candidateText: TextView =
            itemView.findViewById(R.id.physical_keyboard_candidate_text)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCandidateClicked?.invoke(getItem(position))
                }
            }
        }

        fun bind(candidate: CandidateItem, highlighted: Boolean) {
            candidateText.text = candidate.word
            itemView.isActivated = highlighted
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CandidateItem>() {
        override fun areItemsTheSame(oldItem: CandidateItem, newItem: CandidateItem): Boolean {
            return oldItem.word == newItem.word && oldItem.length == newItem.length
        }

        override fun areContentsTheSame(oldItem: CandidateItem, newItem: CandidateItem): Boolean {
            return oldItem == newItem
        }
    }
}
