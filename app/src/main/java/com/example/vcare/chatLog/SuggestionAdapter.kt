package com.example.vcare.chatLog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vcare.R
import kotlinx.android.synthetic.main.suggestion_list_item.view.*

class SuggestionAdapter(
    private val suggestions: List<String>,
    private val listener: (String) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.suggestion_list_item, parent, false
        )
    )

    override fun getItemCount() = suggestions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.suggestionText.text = suggestions[position]
        holder.suggestionText.setOnClickListener {
            listener(holder.suggestionText.text.toString())
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val suggestionText: TextView = view.tvSuggestion
    }


}