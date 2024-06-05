package capstone.kidlink.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.data.User
import capstone.kidlink.databinding.ItemProfilePopupBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class UserProfileDialogFragment : DialogFragment() {
    private var _binding: ItemProfilePopupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemProfilePopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = arguments?.getParcelable<User>("user")

        binding.userNameTextView.text = user?.name
        Glide.with(this).load(user?.profileImageUrl).into(binding.userImageView)

        binding.chatButton.setOnClickListener {
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
            val chatRoomId = generateChatRoomId(user?.email)
            val db = FirebaseFirestore.getInstance()

            val chatRoomRef = db.collection("chatRooms").document(chatRoomId)
            chatRoomRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    val chatRoomData = mapOf(
                        "chatRoomId" to chatRoomId,
                        "lastMessage" to "",
                        "lastMessageTimestamp" to System.currentTimeMillis(),
                        "participants" to listOf(currentUserEmail, user?.email),
                        "userId" to FirebaseAuth.getInstance().currentUser?.uid,
                        "userName" to user?.name,
                        "profileImageUrl" to user?.profileImageUrl,
                        "timestamp" to System.currentTimeMillis()
                    )
                    chatRoomRef.set(chatRoomData).addOnSuccessListener {
                        Log.d("Firestore", "Chat room created successfully")
                        navigateToChatActivity(chatRoomId, user)
                        dismiss()
                    }.addOnFailureListener { e ->
                        Log.e("Firestore", "Error creating chat room", e)
                        dismiss()
                    }
                } else {
                    navigateToChatActivity(chatRoomId, user)
                    dismiss()
                }
            }
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

    private fun navigateToChatActivity(chatRoomId: String, user: User?) {
        activity?.let { act ->
            val intent = Intent(act, ChatActivity::class.java).apply {
                putExtra("chatRoomId", chatRoomId)
                putExtra("contactName", user?.name)
                putExtra("contactPhotoUrl", user?.profileImageUrl)
                putExtra("contactEmail", user?.email)
            }
            act.startActivity(intent)
        }
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
