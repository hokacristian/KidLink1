package capstone.kidlink.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import capstone.kidlink.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Menyembunyikan Action Bar
        supportActionBar?.hide()
        startSplash()
    }

        private fun startSplash() {
            Handler(mainLooper).postDelayed({
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }, 2000)
        }

}