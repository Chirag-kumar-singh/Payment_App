package com.example.payment_app.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.payment_app.Activity.HomeActivity
import com.example.payment_app.Database.UserDatabase
import com.example.payment_app.Firebase.FirestoreClass
import com.example.payment_app.Models.User
import com.example.payment_app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BankFragment : Fragment() {

    private lateinit var bankListView: ListView
    private lateinit var searchView: SearchView
    private lateinit var bankNotListedButton: Button
    private lateinit var proceedButton: Button
    private lateinit var selectedBank: String
    private lateinit var phoneNumber: String
    private lateinit var preferredLanguage: String
    private lateinit var sharedPreferences: SharedPreferences

    private val bankList = listOf(
        "State Bank of India",
        "HDFC Bank",
        "ICICI Bank",
        "Axis Bank",
        "Punjab National Bank",
        "Bank of Baroda",
        "Canara Bank",
        "Kotak Mahindra Bank",
        "Indian Overseas Bank"
    )

    private lateinit var firestoreClass: FirestoreClass
    private lateinit var userDatabase: UserDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bank, container, false)

        bankListView = view.findViewById(R.id.bankListView)
        searchView = view.findViewById(R.id.searchView)
        bankNotListedButton = view.findViewById(R.id.bankNotListedButton)
        proceedButton = view.findViewById(R.id.proceedButton)

        firestoreClass = FirestoreClass()
        // Initialize Room Database
        userDatabase = UserDatabase.getDatabase(requireContext())


        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        // Get the phone number from arguments
        phoneNumber = arguments?.getString("phoneNumber") ?: ""

        // Retrieve the preferred language from SharedPreferences
        preferredLanguage = sharedPreferences.getString("PreferredLanguage", "DefaultLanguage") ?: "DefaultLanguage"

        Log.d("BankFragment", "Phone number received: $phoneNumber")
        Log.d("BankFragment", "Preferred language retrieved: $preferredLanguage")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, bankList)
        bankListView.adapter = adapter
        bankListView.choiceMode = ListView.CHOICE_MODE_SINGLE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        bankListView.setOnItemClickListener { _, _, position, _ ->
            selectedBank = bankList[position]
        }

        bankNotListedButton.setOnClickListener {
            Toast.makeText(requireContext(), "Bank not listed", Toast.LENGTH_SHORT).show()
        }

        proceedButton.setOnClickListener {
            if (::selectedBank.isInitialized) {
                firestoreClass.checkUserAccount(selectedBank, phoneNumber, { accountExists ->
                    if (accountExists) {
                        Log.d("BankFragment", "Account found for phone number: $phoneNumber")
                        Toast.makeText(requireContext(), "Account Found", Toast.LENGTH_SHORT).show()

                        // Create a User instance
                        val user = User(phoneNumber, preferredLanguage, selectedBank)

                        // Insert User into Room Database
                        CoroutineScope(Dispatchers.IO).launch {
                            userDatabase.userDao().insertUser(user)
                            Log.d("BankFragment", "User data saved to Room database")
                        }

                        // Create User Database
                        firestoreClass.createUserDatabase(user, {
                            Log.d("BankFragment", "User database created successfully")
                            Toast.makeText(requireContext(), "User data saved successfully", Toast.LENGTH_SHORT).show()
                            // Navigate to the home screen or next fragment
                            // findNavController().navigate(R.id.action_bankFragment_to_homeFragment)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish() // Optional: finish the current activity
                        }, { errorMessage ->
                            Log.d("BankFragment", "Error saving user data: $errorMessage")
                            Toast.makeText(requireContext(), "Error saving user data: $errorMessage", Toast.LENGTH_SHORT).show()
                        })
                    } else {
                        Log.d("BankFragment", "No account found for phone number: $phoneNumber")
                        Toast.makeText(requireContext(), "User does not have an account with $selectedBank", Toast.LENGTH_SHORT).show()
                    }
                }, { errorMessage ->
                    Log.d("BankFragment", "Error checking account: $errorMessage")
                    Toast.makeText(requireContext(), "Error checking account: $errorMessage", Toast.LENGTH_SHORT).show()
                })
            } else {
                Toast.makeText(requireContext(), "Please select a bank", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
