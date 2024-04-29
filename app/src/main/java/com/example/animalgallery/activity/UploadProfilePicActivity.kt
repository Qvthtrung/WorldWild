package com.example.animalgallery.activity

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.animalgallery.R
import com.example.animalgallery.databinding.ActivityUploadProfilePicBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadProfilePicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProfilePicBinding

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorage: StorageReference
    private var mUser: FirebaseUser? = null
    private var uriImage: Uri? = null

    // Activity result launcher
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uriImage = it
            Glide.with(this@UploadProfilePicActivity)
                .load(uri)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_image_24)
                )
                .into(binding.picPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadProfilePicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
        mStorage = FirebaseStorage.getInstance().getReference("DisplayPics")
        val uri: Uri? = mUser!!.photoUrl

        //Set User's current profile picture in ImageView(If uploaded already)
        uri?.let {
            Glide.with(this@UploadProfilePicActivity)
                .load(uri)
                .apply(
                    RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_image_24)
                )
                .into(binding.picPreview)
        }

        //Choose image to upload
        binding.btnChoosePic.setOnClickListener{
            openFileChooser()
        }

        //Upload image
        binding.btnUploadPic.setOnClickListener{
            binding.progressBar.visibility = View.VISIBLE
            uploadPic()
        }
    }

    private fun uploadPic() {
        if(uriImage != null) {
            //Save the image with uid of the currently logged in user
            val mFile = mStorage.child(mUser!!.uid + "."
                    + getFileExtension(uriImage!!))
            //Upload image to Storage
            mFile.putFile(uriImage!!).addOnSuccessListener {

                mFile.downloadUrl.addOnSuccessListener {downloadUri ->

                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUri)
                        .build()
                    mUser!!.updateProfile(profileUpdates)
                        .addOnSuccessListener {
                            // Profile update success
                            Toast.makeText(this, "Upload successfully", Toast.LENGTH_SHORT).show()

                            // Navigate to UserProfileActivity
                            val intent = Intent(this, UserProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            // Profile update failed
                            Toast.makeText(this, "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun openFileChooser() {
        getContent.launch("image/*")
    }
}