package capstone.kidlink.fragment

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.databinding.FragmentKontakBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KontakFragment : Fragment() {
    private var _binding: FragmentKontakBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var contactsAdapter: ArrayAdapter<String>
    private val contactList = mutableListOf<String>()
    private val contactEmailList = mutableListOf<String>()

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

        contactsAdapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, contactList)
        binding.contactsListView.adapter = contactsAdapter

        db.collection("users").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    name?.let {
                        contactList.add(it)
                        contactEmailList.add(email!!)
                    }
                }
                contactsAdapter.notifyDataSetChanged()
            }

        binding.contactsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedContactName = contactList[position]
            val selectedContactEmail = contactEmailList[position]
            val currentUserEmail = auth.currentUser?.email

            // Create or get chat room ID
            val chatRoomId = if (currentUserEmail!! < selectedContactEmail) {
                "$currentUserEmail-$selectedContactEmail"
            } else {
                "$selectedContactEmail-$currentUserEmail"
            }

            val intent = Intent(requireActivity(), ChatActivity::class.java)
            intent.putExtra("chatRoomId", chatRoomId)
            intent.putExtra("contactName", selectedContactName)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}