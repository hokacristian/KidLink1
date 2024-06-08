package capstone.kidlink.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getUserProfile(userId: String): Task<DocumentSnapshot> {
        return db.collection("users").document(userId).get()
    }

    fun updateProfileImageUrl(userId: String, imageUrl: String): Task<Void> {
        return db.collection("users").document(userId).update("profileImageUrl", imageUrl)
    }
}
