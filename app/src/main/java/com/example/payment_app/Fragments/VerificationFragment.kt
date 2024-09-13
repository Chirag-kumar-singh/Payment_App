package com.example.payment_app.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.payment_app.Firebase.FirestoreClass
import com.example.payment_app.databinding.FragmentVerificationBinding
import com.example.payment_app.Models.ForceResendingTokenParcelable
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class VerificationFragment : Fragment() {

    private lateinit var binding: FragmentVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var firestoreHelper: FirestoreClass // Initialize FirestoreHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestoreHelper = FirestoreClass() // Instantiate FirestoreHelper

        // Initially hide the progress bar
        binding.phoneProgressBar.visibility = View.GONE

        binding.sendOTPBtn.setOnClickListener {
            val phoneNumber = binding.phoneEditTextNumber.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                // Show the progress bar when sending the OTP
                binding.phoneProgressBar.visibility = View.VISIBLE
                // Prepend the country code to the phone number
                val formattedPhoneNumber = "+91$phoneNumber"
                firestoreHelper.sendVerificationCode(formattedPhoneNumber, requireActivity(), callbacks)
            } else {
                Toast.makeText(requireContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Auto-retrieval or Instant validation
            binding.phoneProgressBar.visibility = View.GONE
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            binding.phoneProgressBar.visibility = View.GONE
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            this@VerificationFragment.verificationId = verificationId
            this@VerificationFragment.resendToken = token

            // Hide the progress bar and navigate to OTPFragment
            binding.phoneProgressBar.visibility = View.GONE
            val action = VerificationFragmentDirections.actionVerificationFragmentToOTPFragment(
                verificationId,
                ForceResendingTokenParcelable(token),
                binding.phoneEditTextNumber.text.toString().trim()
            )
            findNavController().navigate(action)
        }
    }
}
