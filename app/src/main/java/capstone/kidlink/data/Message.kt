package capstone.kidlink.data

data class Message(
    val senderId: String? = null,
    val messageText: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val censor : String? = "UNSET"

    )

data class Chat(
    val userId: String = "",
    val userName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0,
    val profileImageUrl: String = "",
    val lastMessageTimestamp: Long = 0,
    val participants: List<String> = listOf()
)
