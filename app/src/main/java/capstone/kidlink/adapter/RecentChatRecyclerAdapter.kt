package capstone.kidlink.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import capstone.kidlink.R
import capstone.kidlink.activity.ChatActivity
import capstone.kidlink.data.Chat
import capstone.kidlink.data.User
import capstone.kidlink.utils.FirebaseUtil
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecentChatRecyclerAdapter(
    private val chatList: List<Chat>,
    private val context: Context,
    private val auth: FirebaseAuth
) : RecyclerView.Adapter<RecentChatRecyclerAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePic: ImageView = itemView.findViewById(R.id.userImageView)
        val usernameText: TextView = itemView.findViewById(R.id.userNameTextView)
        val lastMessageText: TextView = itemView.findViewById(R.id.lastMessageTextView)
        val lastMessageTime: TextView = itemView.findViewById(R.id.timestampTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val currentUserEmail = auth.currentUser?.email
        val otherParticipantEmail = chat.participants.find { it != currentUserEmail }

        if (otherParticipantEmail != null) {
            FirebaseFirestore.getInstance().collection("users").whereEqualTo("email", otherParticipantEmail)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val user = document.toObject(User::class.java)
                        holder.usernameText.text = user.name
                        if (user.profileImageUrl.isNotEmpty()) {
                            Glide.with(context).load(user.profileImageUrl).into(holder.profilePic)
                        } else {
                            Glide.with(context).load(R.drawable.default_photo).into(holder.profilePic)
                        }

                        holder.itemView.setOnClickListener {
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra("chatRoomId", chat.chatRoomId)
                                putExtra("contactName", user.name)
                                putExtra("contactPhotoUrl", user.profileImageUrl)
                                putExtra("contactEmail", otherParticipantEmail)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
        }

        holder.lastMessageText.text = chat.lastMessage
        holder.lastMessageTime.text = FirebaseUtil.timestampToString(chat.timestamp)
    }

    override fun getItemCount(): Int = chatList.size
}
