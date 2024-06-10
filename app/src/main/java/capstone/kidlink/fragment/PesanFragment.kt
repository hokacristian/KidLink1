package capstone.kidlink.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import capstone.kidlink.R
import capstone.kidlink.adapter.RecentChatRecyclerAdapter
import capstone.kidlink.data.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PesanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentChatRecyclerAdapter
    private var chatList = mutableListOf<Chat>() // Initialize the list here
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pesan, container, false)
        val searchBar = view.findViewById<SearchView>(R.id.searchBar)
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchChats(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchChats(it) }
                return true
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Adapter setup should be here after chatList has been initialized
        adapter = RecentChatRecyclerAdapter(chatList, requireContext(), auth)
        recyclerView.adapter = adapter

        loadChats()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchChats(query: String) {
        val currentUserEmail = auth.currentUser?.email ?: return
        val searchQuery = query.lowercase() // Convert the query to lowercase

        db.collection("chatRooms")
            .whereArrayContains("participants", currentUserEmail)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshots ->
                chatList.clear()
                for (document in snapshots) {
                    val chat = document.toObject(Chat::class.java)
                    if (chat.lastMessage.isNotEmpty() && (chat.userName.lowercase().contains(searchQuery) || chat.lastMessage.lowercase().contains(searchQuery))) {
                        chatList.add(chat)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("PesanFragment", "Error loading chats", e)
            }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun loadChats() {
        val currentUserEmail = auth.currentUser?.email ?: return
        db.collection("chatRooms")
            .whereArrayContains("participants", currentUserEmail)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("PesanFragment", "Error loading chats: ", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    chatList.clear()
                    for (document in snapshots.documents) {
                        val chat = document.toObject(Chat::class.java)
                        if (chat != null && chat.lastMessage.isNotEmpty()) {
                            chatList.add(chat)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    Log.d("PesanFragment", "Chats loaded: ${chatList.size}")
                }
            }
    }
}
