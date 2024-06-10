package capstone.kidlink.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import capstone.kidlink.R
import capstone.kidlink.data.UserPreference
import capstone.kidlink.data.dataStore
import capstone.kidlink.fragment.BerandaFragment
import capstone.kidlink.fragment.KiddozFragment
import capstone.kidlink.fragment.PesanFragment
import capstone.kidlink.fragment.ProfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var activeFragment: Fragment? = null
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val customToolbar = findViewById<Toolbar>(R.id.customToolbar)
        setSupportActionBar(customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            navigateToLoginActivity()
            return
        }

        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_beranda -> BerandaFragment()
                R.id.nav_pesan -> PesanFragment()
                R.id.nav_kiddoz -> KiddozFragment()
                R.id.nav_profil -> ProfilFragment()
                else -> BerandaFragment()
            }
            switchFragment(selectedFragment)
            true
        }

        // Menampilkan BerandaFragment secara default
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_beranda
        }

        checkLoginStatus()
    }

    private fun switchFragment(fragment: Fragment) {
        if (fragment != activeFragment) {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, fragment)
                commit()
            }
            activeFragment = fragment
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Jika di BerandaFragment, keluar dari aplikasi
        if (activeFragment is BerandaFragment) {
            super.onBackPressed()
        } else {
            // Jika tidak, kembali ke BerandaFragment
            bottomNav.selectedItemId = R.id.nav_beranda
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHAT_ACTIVITY && resultCode == RESULT_OK) {
            // Menampilkan PesanFragment setelah kembali dari ChatActivity
            switchFragment(PesanFragment())
        }
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkLoginStatus() {
        val userPref = UserPreference.getInstance(applicationContext.dataStore)
        lifecycleScope.launch {
            userPref.isLoggedIn.collect { isLoggedIn ->
                if (!isLoggedIn) {
                    navigateToLoginActivity()
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_CHAT_ACTIVITY = 1
    }
}
