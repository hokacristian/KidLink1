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

    companion object {
        const val REQUEST_CODE_CHAT_ACTIVITY = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLoginStatus()

        val customToolbar = findViewById<Toolbar>(R.id.customToolbar)
        setSupportActionBar(customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            navigateToLoginActivity()
            return
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_beranda -> BerandaFragment()
                R.id.nav_pesan -> PesanFragment()
                R.id.nav_kiddoz -> KiddozFragment()
                R.id.nav_profil -> ProfilFragment()
                else -> BerandaFragment()
            }

            if (selectedFragment != activeFragment) {
                activeFragment = selectedFragment

                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container, selectedFragment)
                    commit()
                }
            }

            true
        }

        // Menampilkan HomeFragment secara default
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_beranda
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHAT_ACTIVITY && resultCode == RESULT_OK) {
            // Menampilkan PesanFragment setelah kembali dari ChatActivity
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, PesanFragment())
                commit()
            }
        }
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
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
}
