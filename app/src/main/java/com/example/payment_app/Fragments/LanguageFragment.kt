package com.example.payment_app.Fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.payment_app.R

class LanguageFragment : Fragment(), LanguageAdapter.OnLanguageSelectedListener {

    private lateinit var adapter: LanguageAdapter
    private val languages = listOf(
        "English", "Hindi", "Bengali", "Gujarati", "Punjabi",
        "Kannada", "Marathi", "Tamil", "Telugu"
    )

    private lateinit var nextButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_language, container, false)

        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val languageList: ListView = view.findViewById(R.id.language_list)
        nextButton = view.findViewById(R.id.next_button)

        adapter = LanguageAdapter(requireContext(), languages, this)
        languageList.adapter = adapter

        nextButton.setOnClickListener {
            val selectedLanguage = languages[adapter.selectedPosition]
            saveLanguageToPreferences(selectedLanguage)
            findNavController().navigate(R.id.action_languageFragment_to_permissionFragment)
        }

        return view
    }

    override fun onLanguageSelected(position: Int) {
        nextButton.isEnabled = position != -1
    }

    private fun saveLanguageToPreferences(language: String) {
        val editor = sharedPreferences.edit()
        editor.putString("PreferredLanguage", language)
        editor.apply()
    }
}
