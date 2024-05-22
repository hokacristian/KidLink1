package capstone.kidlink.data

data class Message(
    val senderId: String? = null,
    val receiverName: String? = null,
    val messageText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)