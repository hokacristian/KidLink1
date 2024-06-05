package capstone.kidlink.fragment

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
import capstone.kidlink.R
import capstone.kidlink.activity.DetailProfilActivity
import capstone.kidlink.databinding.FragmentProfilBinding
import capstone.kidlink.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilFragment : Fragment() {

    private val viewModel by activityViewModels<UserProfileViewModel>()

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loadUserProfile()
        setupAction()
    }

    private fun loadUserProfile() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUserId).get().addOnSuccessListener { document ->
            if (document != null) {
                val profileImageUrl = document.getString("profileImageUrl")
                val username = document.getString("name")

                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(profileImageUrl).into(binding.userImageView)
                }

                if (!username.isNullOrEmpty()) {
                    binding.userNameTextView.text = username
                }
            }
        }
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(viewLifecycleOwner, Observer { userProfile ->
            if (userProfile != null) {
                Glide.with(this).load(userProfile.profileImageUrl).into(binding.userImageView)
            }
            if (userProfile != null) {
                binding.userNameTextView.text = userProfile.name
            }
        })
    }

    private fun setupAction() {
        binding.profilButtonMenu.setOnClickListener {
            val intent = Intent(requireContext(), DetailProfilActivity::class.java)
            startActivity(intent)
        }

        binding.bahasaButtonMenu.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        binding.keluarButtonMenu.setOnClickListener {
            // Add log out functionality here
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
