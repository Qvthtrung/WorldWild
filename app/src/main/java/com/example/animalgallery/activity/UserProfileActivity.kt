package com.example.animalgallery.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.animalgallery.databinding.ActivityUserProfileBinding
import com.example.animalgallery.model.ReadWriteUserDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    private lateinit var username: String
    private lateinit var email: String
    private lateinit var dob: String
    private lateinit var gender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        if(mUser == null) {
            Toast.makeText(this, "Something went wrong! User's detail are not available at the moment", Toast.LENGTH_LONG).show()
        } else {
            binding.progressBar.visibility = View.VISIBLE
            showUserProfile(mUser!!)
        }
    }

    private fun showUserProfile(mUser: FirebaseUser) {
        val userID = mUser.uid

        //Extracting User Reference from Database for "Registered Users"
        val referenceProfile: DatabaseReference = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Registered Users")
        referenceProfile.child(userID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val readUserDetails: ReadWriteUserDetail? = dataSnapshot.getValue(ReadWriteUserDetail::class.java)
                if(readUserDetails != null) {
                    username = mUser.displayName!!
                    email = mUser.email!!
                    dob = readUserDetails.dob
                    gender = readUserDetails.gender

                    val welcomeText = "Welcome, $username!"
                    binding.greetingText.text = welcomeText
                    binding.usernameShow.text = username
                    binding.emailShow.text = email
                    binding.dobShow.text = dob
                    binding.genderShow.text = gender
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@UserProfileActivity, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}