package capstone.kidlink.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtil {

    fun getUserDetails(userId: String) = FirebaseFirestore.getInstance().collection("users").document(userId).get()

    fun timestampToString(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}
