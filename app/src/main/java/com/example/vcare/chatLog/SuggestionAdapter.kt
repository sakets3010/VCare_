package com.example.vcare.chatLog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.list_item_suggestion.view.*

class SuggestionAdapter(private val suggestions: List<String>,private val listener:(String) -> Unit) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suggestionText: TextView = view.tvSuggestion
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_suggestion, parent, false
        )
    )

    override fun getItemCount() = suggestions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.suggestionText.text = suggestions[position]
        holder.suggestionText.setOnClickListener {
            listener(holder.suggestionText.text.toString())
        }
    }



}