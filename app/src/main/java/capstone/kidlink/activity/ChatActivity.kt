package capstone.kidlink.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.kidlink.adapter.MessageAdapter
import capstone.kidlink.data.Message
import capstone.kidlink.databinding.ActivityChatBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val chatRoomId = intent.getStringExtra("chatRoomId") ?: return
        val contactName = intent.getStringExtra("contactName")
        val contactPhotoUrl = intent.getStringExtra("contactPhotoUrl")
        val currentUserId = auth.currentUser?.uid
        val currentUserEmail = auth.currentUser?.email
        val contactEmail = intent.getStringExtra("contactEmail")

        // Set contact name and photo
        binding.userNameTextView.text = contactName
        Glide.with(this).load(contactPhotoUrl).into(binding.userImageView)

        messageAdapter = MessageAdapter(messageList, currentUserId ?: "")
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message(senderId = currentUserId, messageText = messageText, censor = "UNSET")
                db.collection("chatRooms").document(chatRoomId).collection("messages").add(message)
                    .addOnSuccessListener {
                        binding.messageEditText.text.clear()

                        // Update lastMessageTimestamp and participants
                        val updateData = mapOf(
                            "lastMessage" to messageText,
                            "lastMessageTimestamp" to System.currentTimeMillis(),
                            "participants" to listOf(currentUserEmail, contactEmail),
                            "userId" to currentUserId,
                            "userName" to contactName,
                            "profileImageUrl" to contactPhotoUrl,
                            "timestamp" to System.currentTimeMillis()
                        )
                        db.collection("chatRooms").document(chatRoomId)
                            .update(updateData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "lastMessageTimestamp and participants updated successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error updating lastMessageTimestamp and participants", e)
                            }

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        db.collection("chatRooms").document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    messageList.clear()
                    for (document in snapshots) {
                        val message = document.toObject(Message::class.java)
                        messageList.add(message)
                    }
                    messageAdapter.notifyDataSetChanged()
                    binding.chatRecyclerView.scrollToPosition(messageList.size - 1) // Scroll to bottom
                }
            }
    }

}
