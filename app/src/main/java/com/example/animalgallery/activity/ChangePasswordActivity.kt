package com.example.animalgallery.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.animalgallery.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    private lateinit var userPwd: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        binding.inputNewPassword.isEnabled = false
        binding.inputConfirmNewPassword.isEnabled = false
        binding.btnUpdatePassword.isEnabled = false

        reAuthenticate(mUser!!)

    }

    private fun reAuthenticate(mUser: FirebaseUser) {
        binding.btnAuthenticate.setOnClickListener{
            userPwd = binding.inputPassword.text.toString()

            if(userPwd.isEmpty()) {
                Toast.makeText(this, "You need to verify your password to continue", Toast.LENGTH_SHORT).show()
                binding.inputPassword.error = "Please enter your password"
                binding.inputPassword.requestFocus()
            } else {
                binding.progressBar.visibility = View.VISIBLE

                val credential = EmailAuthProvider.getCredential(mUser.email!!,userPwd)

                mUser.reauthenticate(credential).addOnCompleteListener{task ->
                    if(task.isSuccessful) {
                        binding.progressBar.visibility = View.GONE

                        Toast.makeText(this, "Password has been verified." + "You can update your password now.", Toast.LENGTH_SHORT).show()
                        val authenticateSuccessfully = "Verified successfully, you can update your password now."
                        binding.instructions.text = authenticateSuccessfully
                        binding.inputPassword.isEnabled = false
                        binding.btnAuthenticate.isEnabled = false

                        binding.inputNewPassword.isEnabled = true
                        binding.inputConfirmNewPassword.isEnabled = true
                        binding.btnUpdatePassword.isEnabled = true

                        binding.btnUpdatePassword.setOnClickListener{
                            changePassword(mUser)
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

    private fun changePassword(mUser: FirebaseUser) {
        val newPassword = binding.inputNewPassword.text.toString()
        val confirmNewPassword = binding.inputConfirmNewPassword.text.toString()

        if(newPassword.isEmpty()) {
            Toast.makeText(this, "New password is required", Toast.LENGTH_SHORT).show()
            binding.inputNewPassword.error = "New password is required"
            binding.inputNewPassword.requestFocus()
        } else if(confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Confirm password is required", Toast.LENGTH_SHORT).show()
            binding.inputConfirmNewPassword.error = "Confirm password is required"
            binding.inputConfirmNewPassword.requestFocus()
        } else if(newPassword != confirmNewPassword) {
            Toast.makeText(this, "The passwords does not match", Toast.LENGTH_SHORT).show()
            binding.inputConfirmNewPassword.error = "The passwords does not match"
            binding.inputConfirmNewPassword.requestFocus()
        } else if(userPwd == newPassword) {
            Toast.makeText(this, "New password cannot be the same as old password", Toast.LENGTH_SHORT).show()
            binding.inputNewPassword.error = "New password cannot be the same as old password"
            binding.inputNewPassword.requestFocus()
        } else {
            binding.progressBar.visibility = View.VISIBLE
            mUser.updatePassword(newPassword).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Password has been changed", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, UserProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    try {
                        throw task.exception!!
                    } catch (e:Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}