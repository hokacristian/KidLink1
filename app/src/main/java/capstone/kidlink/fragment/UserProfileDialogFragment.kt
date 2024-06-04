package capstone.kidlink.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.data.User
import capstone.kidlink.databinding.ItemProfilePopupBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class UserProfileDialogFragment : DialogFragment() {
    private var _binding: ItemProfilePopupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ItemProfilePopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = arguments?.getParcelable<User>("user")

        binding.userNameTextView.text = user?.name
        Glide.with(this).load(user?.profileImageUrl).into(binding.userImageView)

        binding.chatButton.setOnClickListener {
            dismiss()
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("chatRoomId", generateChatRoomId(user?.email))
                putExtra("contactName", user?.name)
                putExtra("contactPhotoUrl", user?.profileImageUrl)
                putExtra("contactEmail", user?.email)
            }
            startActivity(intent)
        }

        binding.blockButton.setOnClickListener {
            dismiss()
            // Implement block logic here
        }
    }

    private fun generateChatRoomId(email: String?): String {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        return if (currentUserEmail < email!!) "$currentUserEmail-$email" else "$email-$currentUserEmail"
    }

    companion object {
        fun newInstance(user: User): UserProfileDialogFragment {
            val fragment = UserProfileDialogFragment()
            val args = Bundle().apply {
                putParcelable("user", user)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
