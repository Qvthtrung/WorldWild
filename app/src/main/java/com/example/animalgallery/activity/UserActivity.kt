package com.example.animalgallery.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.animalgallery.databinding.ActivityUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserBinding

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
        val username = mUser!!.displayName
        val greetingText = "Hello, $username! How was you day?"
        binding.greeting.text = greetingText

        //Favorite list button
        binding.favoriteList.setOnClickListener{
            val intent = Intent(this, FavoriteListActivity::class.java)
            startActivity(intent)
        }

        //My collections button
        binding.myCollection.setOnClickListener{
            val intent = Intent(this, MyCollectionsActivity::class.java)
            startActivity(intent)
        }

        //User profile button
        binding.userProfile.setOnClickListener{
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        //Log out button
        binding.btnLogout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirm Log Out")
            builder.setMessage("Do you want to log out?")
            builder.setPositiveButton("Yes") { _, _ ->
                // Option Yes
                mAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            builder.setNegativeButton("No") { dialog, _ ->
                // Option No
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        //Navigation
        binding.home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.searchBtn.setOnClickListener {
            val intent2 = Intent(this, SearchActivity::class.java)
            startActivity(intent2)
        }
    }
}