package capstone.kidlink.fragment

import capstone.kidlink.activity.DetailProfilActivity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import capstone.kidlink.databinding.FragmentProfilBinding
import capstone.kidlink.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilFragment : Fragment() {

    private val viewModel by activityViewModels<UserProfileViewModel>()
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        observeUserProfile()
        setupAction()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile() // Reload user profile to ensure data is up-to-date
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner, Observer { userProfile ->
            userProfile?.let {
                Glide.with(this).load(it.profileImageUrl).into(binding.userImageView)
                binding.userNameTextView.text = it.name
            }
        })
    }

    private fun setupAction() {
        binding.profilButtonMenu.setOnClickListener {
            startActivity(Intent(requireContext(), DetailProfilActivity::class.java))
        }

        binding.bahasaButtonMenu.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        binding.keluarButtonMenu.setOnClickListener {
            FirebaseAuth.getInstance().signOut() // Log out the current user
            // Optionally, navigate to the login or splash screen after logging out
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
