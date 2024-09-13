package com.example.payment_app.Fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.payment_app.R

class LanguageAdapter(
    private val context: Context,
    private val languages: List<String>,
    private val listener: OnLanguageSelectedListener,
    var selectedPosition: Int = -1
) : BaseAdapter() {

    override fun getCount(): Int {
        return languages.size
    }

    override fun getItem(position: Int): Any {
        return languages[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_language, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        val language = getItem(position) as String
        viewHolder.languageText.text = language

        viewHolder.languageCheckBox.isChecked = position == selectedPosition

        viewHolder.languageCheckBox.setOnClickListener {
            selectedPosition = if (viewHolder.languageCheckBox.isChecked) position else -1
            notifyDataSetChanged()
            listener.onLanguageSelected(selectedPosition)
        }

        return view
    }

    private class ViewHolder(view: View) {
        val languageText: TextView = view.findViewById(R.id.language_text)
        val languageCheckBox: CheckBox = view.findViewById(R.id.language_checkbox)
    }

    interface OnLanguageSelectedListener {
        fun onLanguageSelected(position: Int)
    }
}
