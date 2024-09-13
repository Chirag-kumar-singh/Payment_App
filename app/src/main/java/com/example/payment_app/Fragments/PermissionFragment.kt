package com.example.payment_app.Fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.payment_app.R
import com.example.payment_app.databinding.FragmentPermissionBinding

class PermissionFragment : Fragment() {

    private var _binding: FragmentPermissionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.proceedButton.setOnClickListener {
            requestPermissions()
        }

        binding.goBackButton.setOnClickListener {
            findNavController().navigate(R.id.action_permissionFragment_to_languageFragment)
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS
        )

        val requestCode = 123 // Arbitrary number to track the request

        if (permissions.all { permission ->
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            // All permissions are already granted, navigate to the next fragment
            findNavController().navigate(R.id.action_permissionFragment_to_verificationFragment)
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.all { result -> result == PackageManager.PERMISSION_GRANTED }) {
                findNavController().navigate(R.id.action_permissionFragment_to_verificationFragment)
            } else {
                Toast.makeText(requireContext(), "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
