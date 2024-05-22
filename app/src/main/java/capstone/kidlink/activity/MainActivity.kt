package capstone.kidlink.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import capstone.kidlink.R
import capstone.kidlink.fragment.BerandaFragment
import capstone.kidlink.fragment.KontakFragment
import capstone.kidlink.fragment.PesanFragment
import capstone.kidlink.fragment.ProfilFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_beranda -> BerandaFragment()
                R.id.nav_pesan -> PesanFragment()
                R.id.nav_kiddoz -> KontakFragment()
                R.id.nav_profil -> ProfilFragment()
                else -> BerandaFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()
            true
        }

        // Menampilkan HomeFragment secara default
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_beranda
        }
    }
}
