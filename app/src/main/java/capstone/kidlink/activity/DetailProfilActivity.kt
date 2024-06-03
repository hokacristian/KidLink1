package capstone.kidlink.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import capstone.kidlink.R
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class DetailProfilActivity : AppCompatActivity() {

    private lateinit var profileImageView: CircleImageView
    private lateinit var changeProfilePhotoButton: Button
    private lateinit var deleteProfilePhotoButton: Button
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_profil)

        supportActionBar?.apply {
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.custom_actionbar)
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        profileImageView = findViewById(R.id.userImageView)
        changeProfilePhotoButton = findViewById(R.id.changeProfilePhotoButton)
        deleteProfilePhotoButton = findViewById(R.id.deleteProfilePhotoButton)

        loadProfileImage()

        changeProfilePhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        deleteProfilePhotoButton.setOnClickListener {
            setDefaultProfileImage()
        }
    }

    private fun loadProfileImage() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUserId).get().addOnSuccessListener { document ->
            if (document != null && document.contains("profileImageUrl")) {
                val profileImageUrl = document.getString("profileImageUrl")
                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(profileImageUrl).into(profileImageView)
                }
            }
        }
    }

    private fun setDefaultProfileImage() {
        val defaultPhotoUri = "https://firebasestorage.googleapis.com/v0/b/kidlink222.appspot.com/o/profileImages%2FzkVaWQ6z6tVniOIz1ElhmabjeS23%2Fdefault_photo.png?alt=media&token=f740b55e-b0ed-4f05-a56c-b8552d885a53"
        Glide.with(this).load(defaultPhotoUri).into(profileImageView)
        updateProfileImageUrl(defaultPhotoUri)
        Toast.makeText(this, "Foto profil telah dihapus", Toast.LENGTH_SHORT).show()
    }

    private fun updateProfileImageUrl(profileImageUrl: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUserId).update("profileImageUrl", profileImageUrl)
            .addOnSuccessListener {
                Log.d("Firestore", "Profile image URL updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating profile image URL", e)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImage = data?.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                        profileImageView.setImageBitmap(bitmap)

                        uploadProfileImageToFirebase(selectedImage)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun uploadProfileImageToFirebase(imageUri: Uri?) {
        if (imageUri == null) return

        val currentUserId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().reference.child("profileImages/$currentUserId/profile_photo.png")

        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val profileImageUrl = uri.toString()
                    updateProfileImageUrl(profileImageUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Error uploading profile image", e)
                Toast.makeText(this, "Failed to upload profile image: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
