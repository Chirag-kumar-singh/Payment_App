package com.example.payment_app.Activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.payment_app.Database.UserDatabase
import com.example.payment_app.Firebase.FirestoreClass
import com.example.payment_app.R
import kotlinx.coroutines.launch

class Payment_chat_screen : AppCompatActivity() {

    private lateinit var userDatabase: UserDatabase
    private val firestoreClass = FirestoreClass()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_chat_screen)

        userDatabase = UserDatabase.getDatabase(this)

        // Retrieve the contact name and number from the intent
        val contactName = intent.getStringExtra("CONTACT_NAME")
        val contactNumber = intent.getStringExtra("CONTACT_NUMBER")

        // Find views in the layout
        val nameTextView = findViewById<TextView>(R.id.contactName)
        val numberTextView = findViewById<TextView>(R.id.contactNumber)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val enterAmount = findViewById<EditText>(R.id.enterAmount)
        val payButton = findViewById<TextView>(R.id.payButton)

        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val senderPhoneNumber = sharedPreferences.getString("phoneNumber", "")
        val senderBank = sharedPreferences.getString("bank", "")


        // Set the contact name and number to the respective TextViews
        nameTextView.text = contactName
        numberTextView.text = contactNumber

        // Handle back button click
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Handle pay button click
        payButton.setOnClickListener {
            val amountString = enterAmount.text.toString()
            if (amountString.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            } else {
                val amount = amountString.toDouble()
                // Handle the payment logic here
                processPayment(contactNumber, amount)
            }
        }

    }

    private fun processPayment(contactNumber: String?, amount: Double) {
        lifecycleScope.launch {
            val user = userDatabase.userDao().getSingleUser()
            if (user != null) {
                val senderPhoneNumber = user.phoneNumber
                val senderBank = user.bank

                firestoreClass.processPayment(
                    senderPhoneNumber,
                    senderBank,
                    contactNumber ?: "",
                    amount,
                    {
                        Toast.makeText(this@Payment_chat_screen, "Payment successful", Toast.LENGTH_SHORT).show()
                        // Navigate or update UI as needed
                    },
                    {
                        errorMessage ->
                        Toast.makeText(this@Payment_chat_screen, "Payment failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this@Payment_chat_screen, "User not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}