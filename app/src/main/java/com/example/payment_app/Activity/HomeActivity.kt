package com.example.payment_app.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.payment_app.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

private val REQUEST_RECORD_AUDIO_PERMISSION = 1
private val REQUEST_CODE_SPEECH_INPUT = 100
private const val REQUEST_READ_CONTACTS_PERMISSION = 2


class HomeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }

        // Request contacts permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_READ_CONTACTS_PERMISSION)
        }

        // Setup Floating Action Button
        val fab: FloatingActionButton = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show()
            }
        }

        val tocontactlinearlayout = findViewById<LinearLayout>(R.id.tocontactlinearlayout)
        tocontactlinearlayout.setOnClickListener {
            openPhoneNumberScreen()
        }

        val checkbalancelinearlayout = findViewById<LinearLayout>(R.id.checkbalancelinearlayout)
        checkbalancelinearlayout.setOnClickListener {
            val intent = Intent(this, CheckBalanceActivity::class.java)
            startActivity(intent)
        }

    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something")

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""

            handleVoiceCommand(spokenText)
        }
    }

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("pay to", ignoreCase = true) -> {
                val name = command.replace("pay to", "").trim()
                // Open screen with phone numbers filtered by name
                openPhoneNumberScreen(name)
            }
            command.contains("pay", ignoreCase = true) -> {
                // Open screen with all phone numbers
                openPhoneNumberScreen()
            }
            else -> {
                Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openPhoneNumberScreen(name: String? = null) {
        // Implement the logic to open the phone number screen
        // You can pass the 'name' as an extra to filter the numbers
        val intent = Intent(this, PhoneNumberActivity::class.java)
        name?.let {
            intent.putExtra("FILTER_NAME", it)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_CONTACTS_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, proceed with loading contacts or other related tasks
                } else {
                    Toast.makeText(this, "Contacts permission is required to access contacts", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}