package capstone.kidlink.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import capstone.kidlink.data.User
import capstone.kidlink.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class UserProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    public val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val userProfile = MutableLiveData<User?>()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        if (userId.isNotEmpty()) {
            userRepository.getUserProfile(userId).addOnSuccessListener { document ->
                val userProfileData = document.toObject(User::class.java)
                userProfile.value = userProfileData
            }
        }
    }

    fun updateUserProfileImage(imageUrl: String) {
        if (userId.isNotEmpty()) {
            userRepository.updateProfileImageUrl(userId, imageUrl).addOnSuccessListener {
                loadUserProfile()
            }
        }
    }
}
