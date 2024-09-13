package com.example.payment_app.Activity

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.example.payment_app.Dao.UserDao
import com.example.payment_app.Database.UserDatabase
import com.example.payment_app.Firebase.FirestoreClass
import com.example.payment_app.R
import kotlinx.coroutines.launch
import com.bumptech.glide.request.target.Target


class CheckBalanceActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var loaderOverlay: FrameLayout
    private lateinit var loaderView: ProgressBar
    private lateinit var tickGifImageView: ImageView
    private var animate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check_balance)

        tickGifImageView = findViewById(R.id.tickGif)
        loaderOverlay = findViewById(R.id.loaderOverlay)
        loaderView = findViewById(R.id.loaderView)
        val bankAccountName = findViewById<TextView>(R.id.bankAccountName)

        // Load the GIF and ensure it runs exactly once
        //loadGifOnce()
        Glide.with(this)
            .asGif()
            .load(R.drawable.payment) // Replace with a valid URL
            .addListener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.setLoopCount(1) // Set the loop count to 1 to animate the GIF just once.
                    return false
                }
            })
            .into(tickGifImageView) // Replace with your ImageView ID


        // Initialize the Room database
        val db = Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java, "app_database"
        ).build()
        userDao = db.userDao()

        // Query the user's data and get the balance
        lifecycleScope.launch {
            val user = userDao.getSingleUser()
            Log.d("CheckBalanceActivity", "Retrieved user: $user")
            bankAccountName.text = user?.phoneNumber ?: "Bank Name Not Found"

            if (user != null) {
                // Show loader while fetching balance
                showLoader()
                // Fetch balance from Firebase
                fetchUserBalance(user.phoneNumber)
            } else {
                Log.e("CheckBalanceActivity", "User data not found")
            }
        }
    }

    private fun loadGifOnce() {
        if (animate) {
            val imageViewTarget = DrawableImageViewTarget(tickGifImageView)

            Glide.with(this)
                .asGif()
                .load(R.drawable.payment) // Replace with your GIF drawable resource
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true))
                .into(tickGifImageView)

            // Set the animate flag to false after GIF is loaded
            animate = false

            // Wait for the GIF to finish animating and then load the static frame
            tickGifImageView.postDelayed({
                loadStaticImage()
            }, 5500) // Adjust delay according to your GIF's duration
        } else {
            loadStaticImage()
        }
    }

    private fun loadStaticImage() {
        Glide.with(this)
            .asBitmap()
            .load(R.drawable.payment) // Replace with your GIF drawable resource
            .into(tickGifImageView)
    }

    private fun fetchUserBalance(phoneNumber: String) {
        FirestoreClass().getUserBalance(phoneNumber, onSuccess = { balance ->
            // Hide loader and update the UI with the fetched balance
            hideLoader()

            val amountTextView = findViewById<TextView>(R.id.amount)
            amountTextView.text = "₹${balance}" // Format the balance as needed

        }, onFailure = { error ->
            hideLoader()
            Log.e("CheckBalanceActivity", "Failed to fetch user balance: $error")
        })
    }

    private fun showLoader() {
        loaderOverlay.visibility = FrameLayout.VISIBLE
        loaderView.visibility = ProgressBar.VISIBLE
    }

    private fun hideLoader() {
        loaderOverlay.visibility = FrameLayout.GONE
        loaderView.visibility = ProgressBar.GONE
    }
}
