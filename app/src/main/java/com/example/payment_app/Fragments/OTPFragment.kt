package com.example.payment_app.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.payment_app.R
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.payment_app.databinding.FragmentOTPBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.example.payment_app.Models.ForceResendingTokenParcelable
import java.util.concurrent.TimeUnit

class OTPFragment : Fragment() {

    private lateinit var binding: FragmentOTPBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private lateinit var resendToken: ForceResendingTokenParcelable
    private lateinit var phoneNumber: String
    private lateinit var preferredlanguage: String

    private val args: OTPFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOTPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        OTP = args.verificationId
        resendToken = args.resendToken
        phoneNumber = args.phoneNumber

        init()
        binding.otpProgressBar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPTvVisibility()

        binding.resendTextView.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }

        binding.verifyOTPBtn.setOnClickListener {
            val typedOTP = (binding.otpEditText1.text.toString() +
                    binding.otpEditText2.text.toString() +
                    binding.otpEditText3.text.toString() +
                    binding.otpEditText4.text.toString() +
                    binding.otpEditText5.text.toString() +
                    binding.otpEditText6.text.toString())

            if (typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    binding.otpProgressBar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(requireContext(), "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resendOTPTvVisibility() {
        binding.otpEditText1.setText("")
        binding.otpEditText2.setText("")
        binding.otpEditText3.setText("")
        binding.otpEditText4.setText("")
        binding.otpEditText5.setText("")
        binding.otpEditText6.setText("")
        binding.resendTextView.visibility = View.INVISIBLE
        binding.resendTextView.isEnabled = false

        Handler(Looper.myLooper()!!).postDelayed({
            binding.resendTextView.visibility = View.VISIBLE
            binding.resendTextView.isEnabled = true
        }, 60000)
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken.toForceResendingToken())
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
            binding.otpProgressBar.visibility = View.VISIBLE
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            OTP = verificationId
            resendToken = ForceResendingTokenParcelable(token)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Authenticate Successfully", Toast.LENGTH_SHORT).show()
                    //navigateToMain()
                    val bundle = Bundle().apply {
                        putString("phoneNumber", phoneNumber)

                    }
                    findNavController().navigate(R.id.action_OTPFragment_to_bankFragment, bundle)
                } else {
                    Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    }
                }
                binding.otpProgressBar.visibility = View.VISIBLE
            }
    }

    private fun navigateToMain() {
        // Replace this with the correct navigation action or intent to navigate to MainActivity
        // findNavController().navigate(R.id.action_otpFragment_to_mainFragment)
        Toast.makeText(requireContext(), "Navigating to Main", Toast.LENGTH_SHORT).show()
    }

    private fun addTextChangeListener() {
        binding.otpEditText1.addTextChangedListener(EditTextWatcher(binding.otpEditText1))
        binding.otpEditText2.addTextChangedListener(EditTextWatcher(binding.otpEditText2))
        binding.otpEditText3.addTextChangedListener(EditTextWatcher(binding.otpEditText3))
        binding.otpEditText4.addTextChangedListener(EditTextWatcher(binding.otpEditText4))
        binding.otpEditText5.addTextChangedListener(EditTextWatcher(binding.otpEditText5))
        binding.otpEditText6.addTextChangedListener(EditTextWatcher(binding.otpEditText6))
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        // Other initializations
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            when (view.id) {
                R.id.otpEditText1 -> if (p0!!.length == 1) binding.otpEditText2.requestFocus()
                R.id.otpEditText2 -> if (p0!!.length == 1) binding.otpEditText3.requestFocus()
                else if (p0.isEmpty()) binding.otpEditText1.requestFocus()
                R.id.otpEditText3 -> if (p0!!.length == 1) binding.otpEditText4.requestFocus()
                else if (p0.isEmpty()) binding.otpEditText2.requestFocus()
                R.id.otpEditText4 -> if (p0!!.length == 1) binding.otpEditText5.requestFocus()
                else if (p0.isEmpty()) binding.otpEditText3.requestFocus()
                R.id.otpEditText5 -> if (p0!!.length == 1) binding.otpEditText6.requestFocus()
                else if (p0.isEmpty()) binding.otpEditText4.requestFocus()
                R.id.otpEditText6 -> if (p0!!.isEmpty()) binding.otpEditText5.requestFocus()
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
    }
}
