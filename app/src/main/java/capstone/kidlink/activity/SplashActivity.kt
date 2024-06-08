package capstone.kidlink.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import capstone.kidlink.data.UserPreference
import capstone.kidlink.data.dataStore
import capstone.kidlink.databinding.ActivitySplashBinding
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var isNavigated = false  // Flag to track if navigation has occurred

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        checkLogin()
    }

    private fun checkLogin() {
        val userPref = UserPreference.getInstance(applicationContext.dataStore)
        lifecycleScope.launch {
            userPref.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    navigateToMainActivity()
                } else {
                    navigateToWelcomeActivity()
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        if (!isNavigated) {
            isNavigated = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun navigateToWelcomeActivity() {
        if (!isNavigated) {
            isNavigated = true
            Handler(mainLooper).postDelayed({
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }, 2000)  // Optional delay to mimic splash screen duration
        }
    }
}
