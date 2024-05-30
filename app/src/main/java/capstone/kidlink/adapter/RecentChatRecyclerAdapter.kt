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

class RecentChatRecyclerAdapter(
    private val chatList: List<Chat>,
    private val context: Context
) : RecyclerView.Adapter<RecentChatRecyclerAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
        val usernameText: TextView = itemView.findViewById(R.id.userNameTextView)
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        val lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val userId = chat.userId
        if (userId.isNotEmpty()) {
            FirebaseUtil.getUserDetails(userId)
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    holder.usernameText.text = user?.name ?: "Unknown User"
                    Glide.with(context).load(user?.profileImageUrl).into(holder.profilePic)
                }
                .addOnFailureListener { e ->
                    holder.usernameText.text = "Unknown User"
                }
        }

        holder.lastMessageText.text = chat.lastMessage
        holder.lastMessageTime.text = FirebaseUtil.timestampToString(chat.timestamp)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("chatRoomId", chat.userId)
                putExtra("contactName", holder.usernameText.text.toString())
                putExtra("contactPhotoUrl", chat.profileImageUrl)
                putExtra("contactEmail", chat.participants.find { it != userId }) // Ensure this is correct
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}
