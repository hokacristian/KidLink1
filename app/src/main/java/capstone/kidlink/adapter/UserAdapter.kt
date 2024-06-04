package capstone.kidlink.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import capstone.kidlink.data.User
import capstone.kidlink.databinding.ItemUserBinding
import com.bumptech.glide.Glide

class UserAdapter(private val users: MutableList<User>, private val listener: UserClickListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    interface UserClickListener {
        fun onUserClicked(user: User)
        fun onImageClicked(user: User)
    }

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.userImageView.setOnClickListener {
                listener.onImageClicked(users[adapterPosition])
            }
            binding.userNameTextView.setOnClickListener {
                listener.onUserClicked(users[adapterPosition])
            }
        }

        fun bind(user: User) {
            binding.userNameTextView.text = user.name
            Glide.with(binding.root.context).load(user.profileImageUrl).into(binding.userImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}
