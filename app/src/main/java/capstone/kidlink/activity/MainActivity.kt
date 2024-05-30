package capstone.kidlink.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import capstone.kidlink.R
import capstone.kidlink.fragment.BerandaFragment
import capstone.kidlink.fragment.KiddozFragment
import capstone.kidlink.fragment.PesanFragment
import capstone.kidlink.fragment.ProfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var activeFragment: Fragment? = null
    private lateinit var berandaFragment: BerandaFragment
    private lateinit var pesanFragment: PesanFragment
    private lateinit var kiddozFragment: KiddozFragment
    private lateinit var profilFragment: ProfilFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
