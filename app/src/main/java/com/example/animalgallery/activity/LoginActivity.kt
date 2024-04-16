package com.example.animalgallery.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.animalgallery.databinding.ActivityLoginBinding
import com.example.animalgallery.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    //Layout when not logged in
    private lateinit var binding: ActivityLoginBinding
    //Layout when logged in
    private lateinit var binding2: ActivityUserBinding

    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+")

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
        binding2 = ActivityUserBinding.inflate(layoutInflater)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        if(mUser != null) {
            //When logged in
            setContentView(binding2.root)

            binding2.btnLogout.setOnClickListener{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirm Log Out")
                builder.setMessage("Do you want to log out?")
                builder.setPositiveButton("Yes") { _, _ ->
                    // Option Yes
                    mAuth.signOut()
                    recreate()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    // Option No
                    dialog.dismiss()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }
        else{
            //When not logged in
            setContentView(binding.root)

            binding.btnLogin.setOnClickListener{
                performLogin()
            }
        }




        //Navigation
        binding.createNewAccount.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        if (!email.matches(emailPattern)){
            binding.inputEmail.error = "Invalid Email"
        } else if(password.isEmpty() || password.length < 6) {
            binding.inputPassword.error = "Password should have more than 6 characters"
        } else {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Login")
                .setMessage("Logging in...")
                .setCancelable(false)
                .create()
            dialog.show()

            //Sign in
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    dialog.dismiss()
                    sendUserToNextActivity()
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                    Toast.makeText(this, ""+task.exception, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendUserToNextActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

