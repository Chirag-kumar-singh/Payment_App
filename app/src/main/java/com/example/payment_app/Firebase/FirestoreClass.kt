package com.example.payment_app.Firebase

import android.app.Activity
import android.util.Log
import com.example.payment_app.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class FirestoreClass {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun sendVerificationCode(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun checkUserAccount(
        bank: String,
        phoneNumber: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Clean the phone number by removing any non-digit characters
        val cleanedPhoneNumber = phoneNumber.filter { it.isDigit() }
        val phoneNumberLong = cleanedPhoneNumber.toLongOrNull()

        if (phoneNumberLong != null) {
            db.collection("Banks").document(bank).collection("Accounts")
                .whereEqualTo("Phone number", phoneNumberLong).get()
                .addOnSuccessListener { documents ->
                    if (documents.size() > 0) {
                        // Account found
                        onSuccess(true)
                    } else {
                        // No account found
                        onSuccess(false)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors that occur during the query
                    onFailure(exception.message ?: "Unknown error")
                }
        } else {
            // Handle invalid phone number format
            onFailure("Invalid phone number format")
        }
    }

    fun createUserDatabase(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        db.collection("Users").document(user.phoneNumber)
            .set(user)
            .addOnSuccessListener {
                Log.d("FirestoreClass", "User database created successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.d("FirestoreClass", "Error creating user database: ${e.message}")
                onFailure(e.message ?: "Unknown error occurred")
            }
    }

    // Method to get the current user's ID
    fun getCurrentUserID(): String {
        return auth.currentUser?.uid ?: ""
    }

    fun processPayment(
        senderPhoneNumber: String,
        senderBank: String,
        receiverPhoneNumber: String,
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val cleanedSenderPhoneNumber = senderPhoneNumber.filter { it.isDigit() }
        val cleanedReceiverPhoneNumber = receiverPhoneNumber.filter { it.isDigit() }
            .removePrefix("91")
            .trimStart('0')

        Log.d("FirestoreClass", "Cleaned sender phone number: $cleanedSenderPhoneNumber")
        Log.d("FirestoreClass", "Cleaned receiver phone number: $cleanedReceiverPhoneNumber")
        Log.d("FirestoreClass", "Senders account: $senderBank")

        val senderAccountRef = db.collection("Banks")
            .document(senderBank)
            .collection("Accounts")
            .whereEqualTo("Phone number", cleanedSenderPhoneNumber.toLong()) // Querying by phone number field

        senderAccountRef.get().addOnSuccessListener { senderAccountSnapshot ->
            if (!senderAccountSnapshot.isEmpty) {
                val senderAccountDocument = senderAccountSnapshot.documents.first()
                val senderBalance = senderAccountDocument.getDouble("balance")
                Log.d("FirestoreClass", "Sender's current balance: $senderBalance")

                if (senderBalance != null && senderBalance >= amount) {
                    val updatedSenderBalance = senderBalance - amount
                    senderAccountDocument.reference.update("balance", updatedSenderBalance)
                        .addOnSuccessListener {
                            Log.d("FirestoreClass", "Sender's balance updated successfully")
                            // Proceed with receiver balance update
                            updateReceiverBalance(cleanedReceiverPhoneNumber, amount, onSuccess, onFailure)
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreClass", "Failed to update sender balance: ${e.message}")
                            onFailure("Failed to update sender balance: ${e.message}")
                        }
                } else {
                    Log.e("FirestoreClass", "Insufficient balance: Sender's current balance: $senderBalance")
                    onFailure("Insufficient balance")
                }
            } else {
                Log.e("FirestoreClass", "No accounts found with phone number: $cleanedSenderPhoneNumber")
                onFailure("No accounts found with phone number: $cleanedSenderPhoneNumber")
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreClass", "Failed to retrieve sender balance: ${e.message}")
            onFailure("Failed to retrieve sender balance: ${e.message}")
        }
    }

    private fun updateReceiverBalance(
        cleanedReceiverPhoneNumber: String,
        amount: Double,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Banks").get().addOnSuccessListener { bankDocuments ->
            var receiverAccountFound = false

            bankDocuments.forEach { bankDocument ->
                val accountsCollection = bankDocument.reference.collection("Accounts")
                Log.d("FirestoreClass", "Querying collection: ${accountsCollection.path}")

                accountsCollection.whereEqualTo("Phone number", cleanedReceiverPhoneNumber.toLong()).get()
                    .addOnSuccessListener { accountDocuments ->
                        if (accountDocuments.isEmpty) {
                            Log.d("FirestoreClass", "No accounts found with phone number: $cleanedReceiverPhoneNumber")
                            return@addOnSuccessListener
                        }

                        accountDocuments.forEach { accountDocument ->
                            receiverAccountFound = true
                            val receiverBalance = accountDocument.getDouble("balance") ?: 0.0
                            val updatedReceiverBalance = receiverBalance + amount

                            // Update receiver's balance
                            accountDocument.reference.update("balance", updatedReceiverBalance)
                                .addOnSuccessListener {
                                    Log.d("FirestoreClass", "Receiver's balance updated successfully")
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreClass", "Failed to update receiver balance: ${e.message}")
                                    onFailure("Failed to update receiver balance: ${e.message}")
                                }
                        }
                        if (!receiverAccountFound) {
                            onFailure("Receiver account not found")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreClass", "Failed to retrieve accounts: ${e.message}")
                        onFailure("Failed to retrieve accounts: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("FirestoreClass", "Failed to retrieve banks: ${e.message}")
            onFailure("Failed to retrieve banks: ${e.message}")
        }
    }

    // New method to get user balance
    fun getUserBalance(
        phoneNumber: String,
        onSuccess: (Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val cleanedPhoneNumber = phoneNumber.filter { it.isDigit() }
        val phoneNumberLong = cleanedPhoneNumber.toLongOrNull()

        if (phoneNumberLong != null) {
            db.collection("Banks")
                .get() // Retrieve all bank documents
                .addOnSuccessListener { bankDocuments ->
                    var balanceFound = false

                    bankDocuments.forEach { bankDocument ->
                        val accountsCollection = bankDocument.reference.collection("Accounts")
                        accountsCollection
                            .whereEqualTo("Phone number", phoneNumberLong)
                            .get()
                            .addOnSuccessListener { accountDocuments ->
                                if (accountDocuments.isEmpty) {
                                    Log.d("FirestoreClass", "No accounts found with phone number: $phoneNumberLong")
                                    return@addOnSuccessListener
                                }

                                accountDocuments.forEach { accountDocument ->
                                    balanceFound = true
                                    val balance = accountDocument.getDouble("balance") ?: 0.0
                                    Log.d("FirestoreClass", "User's balance: $balance")

                                    // Call onSuccess with the fetched balance
                                    onSuccess(balance)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirestoreClass", "Failed to retrieve accounts: ${e.message}")
                                onFailure("Failed to retrieve accounts: ${e.message}")
                            }
                    }

                    if (!balanceFound) {
                        onFailure("Account not found")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreClass", "Failed to retrieve banks: ${e.message}")
                    onFailure("Failed to retrieve banks: ${e.message}")
                }
        } else {
            onFailure("Invalid phone number format")
        }
    }


}
