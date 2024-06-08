package capstone.kidlink.data

data class Message(
    val senderId: String? = null,
    val messageText: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val censor : String? = "UNSET"
    )
{
    fun getId(): String {
        return senderId + timestamp
    }
}

data class Chat(
    val chatRoomId: String = "",
    val lastMessage: String = "",
    val userName: String = "",
    val lastMessageTimestamp: Long = 0,
    val participants: List<String> = listOf()
)

