package com.example.animalgallery.activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.example.animalgallery.databinding.ActivityUpdateProfileBinding
import com.example.animalgallery.model.ReadWriteUserDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var checkedGender: RadioButton

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    private lateinit var username: String
    private lateinit var email: String
    private lateinit var dob: String
    private lateinit var gender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        //Show profile data
        showProfileData(mUser!!)

        //Setting up DatePicker for Birthday
        binding.inputBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, yearSelected, monthOfYear, dayOfMonthSelected ->

                    val selectedDate = "$dayOfMonthSelected/${monthOfYear + 1}/$yearSelected"
                    binding.inputBirthday.setText(selectedDate)
                },
                year,
                month,
                dayOfMonth
            )
            datePickerDialog.show()
        }

        //Update profile data
        binding.btnUpdateProfile.setOnClickListener{
            updateProfile(mUser!!)
        }
    }

    private fun updateProfile(mUser: FirebaseUser) {
        val selectedGender = binding.inputGender.checkedRadioButtonId
        checkedGender = findViewById(selectedGender)

        if(username.isEmpty()) {
            binding.inputName.error = "Username is required"
        }  else if(dob.isEmpty()) {
            binding.inputBirthday.error = "Birthday is required"
        } else if(checkedGender.text.isEmpty()) {
            checkedGender.error = "Gender is required"
        } else {
            gender = checkedGender.text.toString()
            username = binding.inputName.text.toString()
            dob = binding.inputBirthday.text.toString()

            val writeUserDetails = ReadWriteUserDetail(dob, gender)

            val referenceProfile = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Registered Users")
            referenceProfile.child(mUser.uid).setValue(writeUserDetails).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    //Set new display name
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    mUser.updateProfile(profileChangeRequest)

                    Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show()
                    sendUserToNextActivity()
                    finish()
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                binding.progressBar.visibility = View.GONE
            }

        }

    }

    private fun sendUserToNextActivity() {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showProfileData(mUser: FirebaseUser) {
        val userId = mUser.uid

        //Extract User Reference from Database for "Registered Users"
        val referenceProfile: DatabaseReference =
            FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Registered Users")
        referenceProfile.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val readUserDetails: ReadWriteUserDetail? = dataSnapshot.getValue(
                    ReadWriteUserDetail::class.java
                )
                if (readUserDetails != null) {
                    username = mUser.displayName!!
                    email = mUser.email!!
                    dob = readUserDetails.dob
                    gender = readUserDetails.gender

                    binding.inputName.setText(username)
                    binding.inputBirthday.setText(dob)
                    checkedGender = if (gender == "Male") {
                        binding.genderMale
                    } else {
                        binding.genderFemale
                    }
                    checkedGender.isChecked = true
                } else {
                    Toast.makeText(
                        this@UpdateProfileActivity,
                        "Something went wrong!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@UpdateProfileActivity,
                    "Something went wrong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}