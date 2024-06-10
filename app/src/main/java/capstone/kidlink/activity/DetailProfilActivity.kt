package capstone.kidlink.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import capstone.kidlink.R
import capstone.kidlink.viewmodel.UserProfileViewModel
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("DEPRECATION")
class DetailProfilActivity : AppCompatActivity() {

    private val viewModel by viewModels<UserProfileViewModel>()
    private lateinit var profileImageView: CircleImageView
    private lateinit var changeProfilePhotoButton: Button
    private lateinit var deleteProfilePhotoButton: Button

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profil)

        val customToolbar = findViewById<Toolbar>(R.id.customToolbar)
        setSupportActionBar(customToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        profileImageView = findViewById(R.id.userImageView)
        changeProfilePhotoButton = findViewById(R.id.changeProfilePhotoButton)
        deleteProfilePhotoButton = findViewById(R.id.deleteProfilePhotoButton)

        observeUserProfile()

        changeProfilePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        deleteProfilePhotoButton.setOnClickListener {
            viewModel.updateUserProfileImage("https://firebasestorage.googleapis.com/v0/b/kidlink.appspot.com/o/profileImages%2FRbgGcBfubLQQoYjTB8iFglVad8K2%2Fdefault_photo.png?alt=media&token=4c303271-2994-4bc7-98dd-5be8236b7cca")
        }
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(this) { userProfile ->
            if (userProfile != null) {
                Glide.with(this).load(userProfile.profileImageUrl).into(profileImageView)
            }
            if (userProfile != null) {
                findViewById<TextView>(R.id.userNameTextView).text = userProfile.name
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST) {
            data?.data?.let {
                uploadProfileImageToFirebase(it)
            }
        }
    }

    private fun uploadProfileImageToFirebase(imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference.child("profileImages/${FirebaseAuth.getInstance().currentUser?.uid}/profile_photo.png")

        storageReference.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                viewModel.updateUserProfileImage(downloadUri)
            } else {
                Log.e("FirebaseStorage", "Error uploading profile image", task.exception)
                Toast.makeText(this, "Failed to upload profile image: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
