package com.example.animalgallery.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.animalgallery.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+")
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Sending password reset link")
            .setCancelable(false)
            .create()

        binding.btnResetPassword.setOnClickListener{
            val email = binding.inputEmail.text.toString()

            if(email.isEmpty()) {
                Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_SHORT).show()
                binding.inputEmail.error = "Email is required"
                binding.inputEmail.requestFocus()
            } else if (!email.matches(emailPattern)) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                binding.inputEmail.error = "Invalid Email"
                binding.inputEmail.requestFocus()
            } else {
                dialog.show()
                resetPassword(email)
            }
        }
    }

    private fun resetPassword(email: String) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener{task ->
            if(task.isSuccessful) {
                Toast.makeText(this, "Please check your inbox for password reset link", Toast.LENGTH_SHORT).show()

                //Clear backstack to prevent user from coming back to ForgotPasswordActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                try {
                    throw task.exception!!
                } catch(e: FirebaseAuthInvalidUserException) {
                    binding.inputEmail.error = "User does not exist or is no longer valid, please try again."
                } catch(e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
    }
}