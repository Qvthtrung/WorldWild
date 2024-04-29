package com.example.animalgallery.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.animalgallery.databinding.ActivityUpdateEmailBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UpdateEmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateEmailBinding
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    private lateinit var userOldEmail: String
    private lateinit var userNewEmail: String
    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        binding.inputEmail.isEnabled = false
        binding.btnUpdateEmail.isEnabled = false

        userOldEmail = mUser!!.email.toString()
        binding.oldEmail.text = userOldEmail

        reAuthenticate(mUser!!)

    }

    private fun reAuthenticate(mUser: FirebaseUser) {
        binding.btnAuthenticate.setOnClickListener{
            val userPwd = binding.inputPassword.text.toString()

            if(userPwd.isEmpty()) {
                Toast.makeText(this, "You need to verify your password to continue", Toast.LENGTH_SHORT).show()
                binding.inputPassword.error = "Please enter your password"
                binding.inputPassword.requestFocus()
            } else {
                binding.progressBar.visibility = View.VISIBLE

                val credential = EmailAuthProvider.getCredential(userOldEmail,userPwd)

                mUser.reauthenticate(credential).addOnCompleteListener{task ->
                    if(task.isSuccessful) {
                        binding.progressBar.visibility = View.GONE

                        Toast.makeText(this, "Password has been verified." + "You can update your email now.", Toast.LENGTH_SHORT).show()
                        val authenticateSuccessfully = "Verified successfully, you can update your email now."
                        binding.instructions.text = authenticateSuccessfully
                        binding.inputPassword.isEnabled = false
                        binding.btnAuthenticate.isEnabled = false

                        binding.inputEmail.isEnabled = true
                        binding.btnUpdateEmail.isEnabled = true

                        binding.btnUpdateEmail.setOnClickListener{
                            userNewEmail = binding.inputEmail.text.toString()
                            if(userNewEmail.isEmpty()) {
                                Toast.makeText(this, "New email is required", Toast.LENGTH_SHORT).show()
                                binding.inputEmail.error = "Please enter new email"
                                binding.inputEmail.requestFocus()
                            } else if (!userNewEmail.matches(emailPattern)) {
                                Toast.makeText(this, "Invalid email, please try again.", Toast.LENGTH_SHORT).show()
                                binding.inputEmail.error = "Invalid Email"
                            } else if (userOldEmail == userNewEmail) {
                                Toast.makeText(this, "New email cannot be the same as old email", Toast.LENGTH_SHORT).show()
                                binding.inputEmail.error = "Please enter a different email"
                            } else {
                                binding.progressBar.visibility = View.GONE
                                updateEmail(mUser)
                            }
                        }
                    } else {
                        binding.progressBar.visibility = View.GONE
                        try {
                            throw task.exception!!
                        } catch (e:Exception) {
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateEmail(mUser: FirebaseUser) {
        mUser.updateEmail(userNewEmail).addOnCompleteListener{task ->
            if(task.isSuccessful) {
                Toast.makeText(this, "Email has been updated successfully.", Toast.LENGTH_SHORT).show()
                sendUserToNextActivity()
                finish()
            } else {
                try {
                    throw task.exception!!
                } catch (e:Exception) {
                    binding.inputPassword.error = "Password incorrect, please try again."
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendUserToNextActivity() {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}