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
    private lateinit var userAdapter: UserAdapter // Changed
    private val userList = mutableListOf<User>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKiddozBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Correct placement for adapter initialization
        userAdapter = UserAdapter(userList, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = userAdapter
        }

        fetchUsers()
    }

    private fun fetchUsers() {
        if (auth.currentUser != null) {
            db.collection("users").get()
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
        }
    }

    override fun onUserClicked(user: User) {
        navigateToChat(user)
    }

    override fun onImageClicked(user: User) {
        UserProfileDialogFragment.newInstance(user).show(childFragmentManager, "profilePopup")
    }

    private fun navigateToChat(user: User) {
        val currentUserEmail = auth.currentUser?.email ?: return
        val chatRoomId = if (currentUserEmail < user.email) "$currentUserEmail-${user.email}" else "${user.email}-$currentUserEmail"

        Intent(context, ChatActivity::class.java).apply {
            putExtra("chatRoomId", chatRoomId)
            putExtra("contactName", user.name)
            putExtra("contactPhotoUrl", user.profileImageUrl)
            putExtra("contactEmail", user.email)
        }.also { startActivity(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
