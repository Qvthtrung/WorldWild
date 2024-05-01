package com.example.animalgallery.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.animalgallery.databinding.ActivityRegisterBinding
import com.example.animalgallery.model.ReadWriteUserDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+")
    private lateinit var checkedGender: RadioButton

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        binding.inputGender.clearCheck()

        //Setting up DatePicker for Birthday
        binding.inputBirthday.setOnClickListener{
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

        binding.btnRegister.setOnClickListener{
            performAuth()
        }


        //Navigation
        binding.alreadyHaveAccount.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.searchBtn.setOnClickListener{
            val intent2 = Intent(this, SearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent2)
        }
    }

    private fun performAuth() {
        checkedGender = findViewById(binding.inputGender.checkedRadioButtonId)
        val name = binding.inputName.text.toString()
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()
        val confirmPassword = binding.inputConfirmPassword.text.toString()
        val birthday = binding.inputBirthday.text.toString()
        val gender: String

        if(name.isEmpty()) {
            binding.inputName.error = "Username is required"
        } else if(email.isEmpty()) {
            binding.inputEmail.error = "Email is required"
        } else if (!email.matches(emailPattern)) {
            binding.inputEmail.error = "Invalid Email"
        } else if(birthday.isEmpty()) {
            binding.inputBirthday.error = "Birthday is required"
        } else if(binding.inputGender.checkedRadioButtonId == -1) {
            checkedGender.error = "Gender is required"
        } else if(password.isEmpty()) {
            binding.inputPassword.error = "Password is required"
        }  else if(password.length < 6) {
            binding.inputPassword.error = "Password should have more than 6 characters"
        } else if(password != confirmPassword){
            binding.inputConfirmPassword.error = "Password does not match"
        } else {
            gender = checkedGender.text.toString()
            val dialog = AlertDialog.Builder(this)
                .setTitle("Registration")
                .setMessage("Registering...")
                .setCancelable(false)
                .create()
            dialog.show()



            //create account
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    dialog.dismiss()
                    Toast.makeText(this, "User registered successfully, check your email inbox for verification", Toast.LENGTH_SHORT).show()
                    mUser = mAuth.currentUser
                    mUser!!.sendEmailVerification()

                    //Update username
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    mUser!!.updateProfile(profileChangeRequest)

                    //Store user data to Firebase realtime database
                    val writeUserDetails = ReadWriteUserDetail(birthday, gender)

                    //Extracting user reference from database for "Registered Users"
                    val referenceProfile = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Registered Users")
                    referenceProfile.child(mUser!!.uid).setValue(writeUserDetails).addOnCompleteListener{task2 ->
                        if(task2.isSuccessful) {
                            Log.d("RegisterActivity", "User data stored successfully")
                            sendUserToNextActivity()
                            finish()
                        } else {
                            Log.e("RegisterActivity", "Failed to store user data: ${task2.exception}")
                            Toast.makeText(this, "Registration failed, please try again", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    dialog.dismiss()
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthUserCollisionException) {
                        binding.inputEmail.error = "Email is already registered by another user"
                    }
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