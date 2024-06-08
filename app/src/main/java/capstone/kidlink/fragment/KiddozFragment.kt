package capstone.kidlink.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.adapter.UserAdapter
import capstone.kidlink.data.User
import capstone.kidlink.databinding.FragmentKiddozBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KiddozFragment : Fragment(), UserAdapter.UserClickListener {
    private var _binding: FragmentKiddozBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKiddozBinding.inflate(inflater, container, false)
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchUsers(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchUsers(it) }
                return true
            }
        })
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userAdapter = UserAdapter(userList, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        fetchUsers()
    }

    private fun searchUsers(query: String) {
        val searchQuery = query.trim()
        if (searchQuery.isNotEmpty()) {
            db.collection("users")
                .orderBy("name")
                .startAt(searchQuery)
                .endAt("$searchQuery\uf8ff")
                .get()
                .addOnSuccessListener { result ->
                    userList.clear()
                    for (document in result) {
                        val user = document.toObject(User::class.java)
                        userList.add(user)
                    }
                    userAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("KiddozFragment", "Error loading users", e)
                }
        } else {
            fetchUsers()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchUsers() {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (document in result) {
                    val user = document.toObject(User::class.java)
                    userList.add(user)
                }
                userAdapter.notifyDataSetChanged()
                Log.d("KiddozFragment", "Users loaded: ${userList.size}")
            }
            .addOnFailureListener { e ->
                Log.e("KiddozFragment", "Error loading users", e)
            }
    }

    override fun onUserClicked(user: User) {
        navigateToChat(user)
    }

    override fun onImageClicked(user: User) {
        UserProfileDialogFragment.newInstance(user).show(childFragmentManager, "profilePopup")
    }

    private fun navigateToChat(user: User) {
        val currentUser = auth.currentUser ?: return
        val currentUserEmail = currentUser.email ?: return
        val currentUserId = currentUser.uid
        val chatRoomId = if (currentUserEmail < user.email) {
            "$currentUserEmail-${user.email}"
        } else {
            "${user.email}-$currentUserEmail"
        }

        val chatRoomRef = db.collection("chatRooms").document(chatRoomId)
        chatRoomRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val chatRoomData = mapOf(
                    "chatRoomId" to chatRoomId,
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
                        navigateToChatActivity(chatRoomId, user)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error creating chat room", e)
                    }
            } else {
                navigateToChatActivity(chatRoomId, user)
            }
        }
    }

    private fun navigateToChatActivity(chatRoomId: String, user: User) {
        val intent = Intent(requireActivity(), ChatActivity::class.java).apply {
            putExtra("chatRoomId", chatRoomId)
            putExtra("contactName", user.name)
            putExtra("contactPhotoUrl", user.profileImageUrl)
            putExtra("contactEmail", user.email)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}
