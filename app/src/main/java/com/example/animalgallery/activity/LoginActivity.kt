package com.example.animalgallery.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.animalgallery.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+")

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        binding.forgotPassword.setOnClickListener{
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener{
            performLogin()
        }

        //Navigation
        binding.createNewAccount.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        binding.searchBtn.setOnClickListener{
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null) {
            val intent = Intent(this, UserActivity::class.java)
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
                    Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    dialog.dismiss()
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        binding.inputEmail.error = "User does not exist or is no longer valid"
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Email or Password are not correct, please try again", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun sendUserToNextActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }
}

