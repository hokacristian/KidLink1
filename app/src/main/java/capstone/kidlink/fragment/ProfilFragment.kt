package capstone.kidlink.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import capstone.kidlink.activity.DetailProfilActivity
import capstone.kidlink.databinding.FragmentProfilBinding

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAction()
    }

    private fun setupAction() {
        // Binding untuk menu tombol Profil Button Menu
        // TODO : Ubah Fungsi bindingnya menjadi Intent yang akan membawa data User nya
        binding.profilButtonMenu.setOnClickListener {
            val intent = Intent(requireContext(), DetailProfilActivity::class.java)
            startActivity(intent)
        }

        // Binding untuk menu tombol Bahasa Button Menu
        binding.bahasaButtonMenu.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        // Binding untuk menu tombol Keluar Button Menu
        // TODO : Buat Fungsi Binding untuk Log Out User

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}