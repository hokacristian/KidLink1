package capstone.kidlink.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import capstone.kidlink.R
import capstone.kidlink.adapter.MessageAdapter
import capstone.kidlink.data.Message
import capstone.kidlink.databinding.ActivityChatBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val chatRoomId = intent.getStringExtra("chatRoomId") ?: return
        val contactName = intent.getStringExtra("contactName")
        val contactPhotoUrl = intent.getStringExtra("contactPhotoUrl")
        val currentUserId = auth.currentUser?.uid
        val currentUserEmail = auth.currentUser?.email
        val contactEmail = intent.getStringExtra("contactEmail")

        binding.userNameTextView.text = contactName
        Glide.with(this).load(contactPhotoUrl).into(binding.userImageView)

        messageAdapter = MessageAdapter(messageList, currentUserId ?: "")
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message(senderId = currentUserId, messageText = messageText, censor = "UNSET")
                val messageRef = db.collection("chatRooms").document(chatRoomId).collection("messages").document()
                messageRef.set(message)
                    .addOnSuccessListener {
                        binding.messageEditText.text.clear()
                        checkCensorStatusAndDisplay(messageRef, chatRoomId, messageText, currentUserEmail, contactEmail, contactName, contactPhotoUrl)
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
                    snapshots.documents.forEach { document ->
                        messageList.add(document.toObject(Message::class.java)!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
                    }
                }
            }

        binding.backButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun checkCensorStatusAndDisplay(messageRef: DocumentReference, chatRoomId: String, messageText: String, currentUserEmail: String?, contactEmail: String?, contactName: String?, contactPhotoUrl: String?) {
        // Delay untuk memeriksa status censor, misalnya 5000 ms (5 detik)
        Handler(Looper.getMainLooper()).postDelayed({
            messageRef.get().addOnSuccessListener { documentSnapshot ->
                val censor = documentSnapshot.getString("censor")
                if ("UNSAFE" == censor) {
                    showWarningPopup()
                    messageRef.delete() // Opsional: Hapus pesan dari Firestore jika tidak aman
                } else {
                    updateLastMessageDetails(chatRoomId, messageText, currentUserEmail, contactEmail, contactName, contactPhotoUrl)
                }
            }
        }, 1700)
    }


    private fun updateLastMessageDetails(chatRoomId: String, messageText: String, currentUserEmail: String?, contactEmail: String?, contactName: String?, contactPhotoUrl: String?) {
        val updateData = mapOf(
            "lastMessage" to messageText,
            "lastMessageTimestamp" to System.currentTimeMillis(),
            "participants" to listOfNotNull(currentUserEmail, contactEmail),
            "userId" to auth.currentUser?.uid,
            "userName" to contactName,
            "profileImageUrl" to contactPhotoUrl,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("chatRooms").document(chatRoomId)
            .update(updateData)
            .addOnSuccessListener {
                Log.d("Firestore", "Last message and participants updated successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating last message and participants", e)
            }
    }

    private fun showWarningPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.item_warning_popup)
        dialog.setCancelable(true)
        dialog.show()
    }
}
