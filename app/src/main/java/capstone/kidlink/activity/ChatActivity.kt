package capstone.kidlink.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.kidlink.adapter.ChatAdapter
import capstone.kidlink.data.Message
import capstone.kidlink.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val chatRoomId = intent.getStringExtra("chatRoomId")
        val contactName = intent.getStringExtra("contactName")
        val currentUserId = auth.currentUser?.uid

        chatAdapter = ChatAdapter(messageList)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = chatAdapter

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message(currentUserId, contactName, messageText)
                db.collection("chatRooms").document(chatRoomId!!).collection("messages").add(message)
                    .addOnSuccessListener {
                        binding.messageEditText.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        db.collection("chatRooms").document(chatRoomId!!)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    messageList.clear()
                    for (document in snapshots) {
                        val message = document.toObject(Message::class.java)
                        messageList.add(message)
                    }
                    chatAdapter.notifyDataSetChanged()
                }
            }
    }
}