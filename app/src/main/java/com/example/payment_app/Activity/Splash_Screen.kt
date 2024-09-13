package com.example.payment_app.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.payment_app.Firebase.FirestoreClass
import com.example.payment_app.Fragments.LanguageFragment
import com.example.payment_app.R

class Splash_Screen : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val isSetupComplete = sharedPreferences.getBoolean("SetupComplete", false)

        android.os.Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (isSetupComplete) {
                // If setup is complete, start HomeActivity
                Intent(this, HomeActivity::class.java)
            } else {
                // Check if user is signed in
                val currentUserId = FirestoreClass().getCurrentUserID()
                if (currentUserId.isNotEmpty()) {
                    // If user is signed in, start MainActivity
                    Intent(this, HomeActivity::class.java)
                } else {
                    // If user is not signed in, start IntroActivity
                    Intent(this, MainActivity::class.java)
                }
            }

            startActivity(intent)
            finish() // Finish Splash_Screen activity after starting the target activity
        }, 2000) // 2-second delay
    }
}
