package com.example.animalgallery.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.animalgallery.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+")

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser


        binding.btnRegister.setOnClickListener{
            performAuth()
        }


        //Navigation
        binding.alreadyHaveAccount.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performAuth() {
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()
        val confirmPassword = binding.inputConfirmPassword.text.toString()

        if (!email.matches(emailPattern)){
            binding.inputEmail.error = "Invalid Email"
        } else if(password.isEmpty() || password.length < 6) {
            binding.inputPassword.error = "Password should have more than 6 characters"
        } else if(password != confirmPassword){
            binding.inputConfirmPassword.error = "Password and Confirm Password should be the same"
        } else {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Registration")
                .setMessage("Registering...")
                .setCancelable(false)
                .create()
            dialog.show()

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    dialog.dismiss()
                    sendUserToNextActivity()
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, ""+task.exception, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendUserToNextActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}