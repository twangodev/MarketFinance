package com.marketfinance.app.ui.activities

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.marketfinance.app.R
import com.marketfinance.app.utils.RequestSingleton
import com.marketfinance.app.utils.security.EncryptedPreference

class SplashActivity : AppCompatActivity() {

    private var networkAvailable: Boolean? = null
    private val tag = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()

        // Animations (Take a total of 1000ms)
        (findViewById<ImageView>(R.id.splash_marketfinance_imageView).drawable as AnimatedVectorDrawable).start()
        Thread {
            Thread.sleep(500)
            runOnUiThread {
                findViewById<TextView>(R.id.splash_marketfinance_textView).apply {
                    visibility = View.VISIBLE
                    startAnimation(
                        AnimationUtils.loadAnimation(
                            this@SplashActivity,
                            R.anim.text_fadein
                        )
                    )
                }
            }
        }.start()

        // Actual background work
        Thread {
            // Connection Checker
            val queue = RequestSingleton.getInstance(applicationContext)
            val connectionCheckRequest = JsonObjectRequest(
                Request.Method.GET,
                "https://api.ipify.org?format=json",
                null,
                { response ->
                    Log.i(tag, "Connection Found. IP: ${response.getString("ip")}")
                    networkAvailable = true
                },
                {
                    Log.e(tag, "No Connection Found.")
                    it.printStackTrace()
                    networkAvailable = false
                })
            queue.addToRequestQueue(connectionCheckRequest)

            Thread.sleep(2000) // Let animation play & Stay

            // Retrieve UserData for onboard intent.
            val userData = EncryptedPreference("userData").getPreference(this)

            // Go into Main Activity
            runOnUiThread {
                val intent = Intent(this, MainActivity()::class.java)
                if (!userData.getBoolean("completedOnboarding", false)) {
                    Log.i(
                        tag,
                        "completedOnboarding value is false. Adding intent \"promptOnboard\" and value is set to \"true\"."
                    )
                    intent.putExtra("promptOnboard", true)
                }
                startActivity(intent)
            }
        }.start()
    }
}