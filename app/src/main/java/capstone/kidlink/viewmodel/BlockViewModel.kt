package capstone.kidlink.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BlockViewModel : ViewModel() {
    private val _blockedUsers = MutableLiveData<Set<String>>()
    val blockedUsers: LiveData<Set<String>> = _blockedUsers

    fun updateBlockedUsers(userId: String) {
        val currentBlocked = _blockedUsers.value ?: emptySet()
        _blockedUsers.value = currentBlocked + userId
    }
}
