package capstone.kidlink.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.data.User
import capstone.kidlink.databinding.ItemProfilePopupBinding
import capstone.kidlink.viewmodel.BlockViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileDialogFragment : DialogFragment() {
    private var _binding: ItemProfilePopupBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BlockViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ItemProfilePopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(BlockViewModel::class.java)

        val user = arguments?.getParcelable<User>("user")
        binding.userNameTextView.text = user?.name
        user?.profileImageUrl?.let {
            Glide.with(this).load(it).into(binding.userImageView)
        }

        setupChatButton(user)
        setupBlockButton(user)
    }

    private fun setupChatButton(user: User?) {
        binding.chatButton.setOnClickListener {
            user?.email?.let { email ->
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                val chatRoomId = generateChatRoomId(email)

                val db = FirebaseFirestore.getInstance()
                val chatRoomRef = db.collection("chatRooms").document(chatRoomId)
                chatRoomRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val chatRoomData = mapOf(
                            "chatRoomId" to chatRoomId,
                            "lastMessage" to "",
                            "lastMessageTimestamp" to System.currentTimeMillis(),
                            "participants" to listOf(currentUserEmail, email),
                            "userId" to FirebaseAuth.getInstance().currentUser?.uid,
                            "userName" to user.name,
                            "profileImageUrl" to user.profileImageUrl,
                            "timestamp" to System.currentTimeMillis()
                        )
                        chatRoomRef.set(chatRoomData).addOnSuccessListener {
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
        }
    }

    private fun setupBlockButton(user: User?) {
        binding.blockButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            user?.userId?.let { userId ->
                currentUser?.uid?.let { currentUserId ->
                    val db = FirebaseFirestore.getInstance()
                    val blockData = mapOf(
                        "blockerId" to currentUserId,
                        "blockedId" to userId
                    )
                    db.collection("blocks").add(blockData).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            viewModel.updateBlockedUsers(userId)
                            Toast.makeText(context, "User blocked successfully", Toast.LENGTH_SHORT).show()
                            dismiss()
                        } else {
                            Toast.makeText(context, "Failed to block user", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun generateChatRoomId(email: String): String {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        return if (currentUserEmail < email) "$currentUserEmail-$email" else "$email-$currentUserEmail"
    }

    private fun navigateToChatActivity(chatRoomId: String, user: User?) {
        user?.let {
            val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
                putExtra("chatRoomId", chatRoomId)
                putExtra("contactName", it.name)
                putExtra("contactPhotoUrl", it.profileImageUrl)
                putExtra("contactEmail", it.email)
            }
            startActivity(intent)
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
