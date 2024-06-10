package capstone.kidlink.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import capstone.kidlink.R
import capstone.kidlink.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        playAnimation()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        supportActionBar?.hide()

        // Assume your EditTextConfirmPassword has a method to set the password to match
        binding.confirmPasswordEditText.setPasswordToMatch(binding.passwordEditText.text.toString())

        // Add TextChangedListener to update the password to match dynamically
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                binding.confirmPasswordEditText.setPasswordToMatch(s.toString())
            }
            override fun afterTextChanged(s: Editable) {}
        })

        binding.signupButton.setOnClickListener {
            showLoading(true)
            val name = binding.nameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val ortuEmail = binding.emailEditTextortu.text.toString().trim()

            // Update password to match di sini jika Anda mengubah password setelah yang pertama kali di-set
            binding.confirmPasswordEditText.setPasswordToMatch(password)

            if (name.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && email.isNotEmpty() && ortuEmail.isNotEmpty()) {
                if (binding.confirmPasswordEditText.validatePassword()) {
                    registerUser(name, email, password, ortuEmail)
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                showLoading(false)
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToWelcomeActivityActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
    }

    private fun registerUser(name: String, email: String, password: String, ortuEmail: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""
                    val storageReference = FirebaseStorage.getInstance().reference
                        .child("profileImages/$userId/default_photo.png")
                    val defaultPhotoUri = Uri.parse("android.resource://${packageName}/drawable/default_photo")

                    storageReference.putFile(defaultPhotoUri)
                        .addOnSuccessListener {
                            storageReference.downloadUrl.addOnSuccessListener { uri ->
                                val profileImageUrl = uri.toString()
                                saveUserToFirestore(userId, name, email, ortuEmail, profileImageUrl)
                                showLoading(false)
                                navigateToWelcomeActivityActivity()
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
                            }.addOnFailureListener { e ->
                                showLoading(false)
                                Log.e("FirebaseStorage", "Error getting download URL", e)
                                Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener { e ->
                            showLoading(false)
                            Log.e("FirebaseStorage", "Error uploading default photo", e)
                            Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String, ortuEmail: String, profileImageUrl: String) {
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "parentEmail" to ortuEmail,
            "userId" to userId,
            "profileImageUrl" to profileImageUrl
        )
        db.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_LONG).show()
                Log.d("Firestore", "User data saved successfully")
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("Firestore", "Error saving user data", e)
            }
    }

    private fun playAnimation() {
        val animations = listOf(
            binding.titleTextView, binding.nameTextView, binding.nameEditTextLayout,
            binding.passwordTextView, binding.passwordEditTextLayout, binding.confirmPasswordTextView, binding.confirmPasswordEditTextLayout,
            binding.emailTextView, binding.emailEditTextLayout, binding.emailortuTextView, binding.emailortuEditTextLayout,
            binding.signupButton
        ).map {
            ObjectAnimator.ofFloat(it, View.ALPHA, 1f).setDuration(ANIMATION_DURATION)
        }

        AnimatorSet().apply {
            playSequentially(animations)
            start()
        }
    }

    private fun isValidInput(name: String, email: String, password: String): Boolean {
        return name.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= MIN_PASSWORD_LENGTH
    }

    private fun showLoading(state: Boolean) {
        binding.lottieloading.visibility = if (state) View.VISIBLE else View.GONE
    }

    private fun showDialog(title: String, message: String, positiveAction: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { _, _ -> positiveAction.invoke() }
            show()
        }
    }

    private companion object {
        const val ANIMATION_DURATION = 500L
        const val MIN_PASSWORD_LENGTH = 8
    }
}
