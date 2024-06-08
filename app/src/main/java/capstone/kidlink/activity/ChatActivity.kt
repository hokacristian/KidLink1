package capstone.kidlink.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<Message>()
    private var sendingMessageIds = mutableSetOf<String>()


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

        binding.userNameTextView.text = contactName
        Glide.with(this).load(contactPhotoUrl).into(binding.userImageView)

        messageAdapter = MessageAdapter(messageList, currentUserId ?: "")
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = messageAdapter

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message(senderId = currentUserId, messageText = messageText, censor = "UNSET")
                sendingMessageIds.add(message.getId())
                val messageRef = db.collection("chatRooms").document(chatRoomId).collection("messages").document()
                messageRef.set(message)
                    .addOnSuccessListener {
                        binding.messageEditText.text.clear()
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
                    val newSentMessages = updateMessageState(snapshots)
                    scrollToLastMessage()
                    checkCensorAndDisplay(newSentMessages)
                }
            }

        binding.backButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    /**
     * update messageList, and other message states, and notify messageAdapter
     * return newSentMessages, messages that its state changed from sending to delivered to server
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateMessageState(snapshots: QuerySnapshot): Iterable<Message> {
        val unfilteredMessageList = snapshots.documents.map { it.toObject(Message::class.java)!! }
        val safeOrSendingMessageList = unfilteredMessageList
            .filter { it.censor == "SAFE" || (it.censor == "UNSET" && it.senderId == auth.currentUser?.uid) }
        messageList.clear()
        messageList.addAll(safeOrSendingMessageList)
        messageAdapter.notifyDataSetChanged()

        val newSentMessages = unfilteredMessageList
            .filter { sendingMessageIds.contains(it.getId()) && it.censor != "UNSET"}
        sendingMessageIds
            .removeAll(newSentMessages.map { it.getId() }.toSet())
        return newSentMessages
    }

    private fun scrollToLastMessage() {
        if (messageList.isNotEmpty()) {
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun checkCensorAndDisplay(newSentMessages: Iterable<Message>) {
        if (newSentMessages.any { it.censor == "UNSAFE" }) {
            showWarningPopup()
        }
    }

    private fun showWarningPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.item_warning_popup)
        dialog.setCancelable(true)
        dialog.show()
    }
}
