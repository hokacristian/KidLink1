package capstone.kidlink.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize

data class User(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val userId: String = "",
    val parentEmail: String = ""
) : Parcelable