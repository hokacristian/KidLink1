package capstone.kidlink.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.databinding.FragmentKontakBinding
import capstone.kidlink.adapter.UserAdapter
import capstone.kidlink.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KontakFragment : Fragment() {
    private var _binding: FragmentKontakBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKontakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userAdapter = UserAdapter(userList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        // Ensure user is authenticated before querying Firestore
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val name = document.getString("name") ?: ""
                        val email = document.getString("email") ?: ""
                        val profileImageUrl = document.getString("profileImageUrl") ?: ""
                        val user = User(name, email, profileImageUrl)
                        userList.add(user)
                    }
                    userAdapter.notifyDataSetChanged()
                    Log.d("KontakFragment", "Users loaded: ${userList.size}")
                }
                .addOnFailureListener { e ->
                    Log.e("KontakFragment", "Error loading users", e)
                }
        } else {
            Log.e("KontakFragment", "User not authenticated")
        }

        userAdapter.setOnItemClickListener { user ->
            val currentUserEmail = auth.currentUser?.email ?: return@setOnItemClickListener
            val currentUserId = auth.currentUser?.uid ?: return@setOnItemClickListener

            // Create or get chat room ID
            val chatRoomId = if (currentUserEmail < user.email) {
                "$currentUserEmail-${user.email}"
            } else {
                "${user.email}-$currentUserEmail"
            }

            // Check if chat room exists
            val chatRoomRef = db.collection("chatRooms").document(chatRoomId)
            chatRoomRef.get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create the chat room document with the required fields
                    val chatRoomData = mapOf(
                        "lastMessage" to "",
                        "lastMessageTimestamp" to System.currentTimeMillis(),
                        "participants" to listOf(currentUserEmail, user.email),
                        "userId" to currentUserId,
                        "userName" to user.name,
                        "profileImageUrl" to user.profileImageUrl,
                        "timestamp" to System.currentTimeMillis()
                    )
                    chatRoomRef.set(chatRoomData)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Chat room created successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error creating chat room", e)
                        }
                }
            }

            val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
                putExtra("chatRoomId", chatRoomId)
                putExtra("contactName", user.name)
                putExtra("contactPhotoUrl", user.profileImageUrl)
                putExtra("contactEmail", user.email) // Add this line
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
