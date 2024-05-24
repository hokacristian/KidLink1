package capstone.kidlink.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import capstone.kidlink.data.User
import capstone.kidlink.databinding.ItemUserBinding
import com.bumptech.glide.Glide

class UserAdapter(private val users: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var onItemClickListener: ((User) -> Unit)? = null

    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.userNameTextView.text = user.name
        Glide.with(holder.itemView.context).load(user.profileImageUrl).into(holder.binding.userImageView)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun setOnItemClickListener(listener: (User) -> Unit) {
        onItemClickListener = listener
    }
}