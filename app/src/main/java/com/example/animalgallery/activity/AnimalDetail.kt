package com.example.animalgallery.activity

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.animalgallery.R
import com.example.animalgallery.adapter.CustomAdapter
import com.example.animalgallery.databinding.ActivityAnimalDetailBinding
import com.example.animalgallery.model.AnimalModelAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONException

class AnimalDetail : AppCompatActivity() {
    private lateinit var binding: ActivityAnimalDetailBinding
    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null
    private lateinit var customAdapter: CustomAdapter
    private lateinit var list: MutableList<AnimalModelAPI>
    private var urls: String =
        "https://api.unsplash.com/search/photos/?client_id=B1MEAcq9sTZhPJ3wO0tel73QZPJ6GiXYooyL8sPZ0f4&collections=4760062,3330452&query="
    private var page: String = "&page=1"
    private var perPage: String = "&per_page=9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        //StaggeredGridLayout Setup
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.detailRecyclerView.layoutManager = staggeredGridLayoutManager
        list = mutableListOf()
        customAdapter = CustomAdapter(this, list)
        binding.detailRecyclerView.adapter = customAdapter

        //Getting images information
        val bundle: Bundle? = intent.extras
        val imageId = bundle?.getString("imageId")
        val imageUrl = bundle?.getString("imageUrl")
        val imageTitle = bundle?.getString("imageTitle")
        val imageDescription = bundle?.getString("imageDescription")
        val imageDownloadLink = bundle?.getString("imageDownloadLink")
        val tag = bundle?.getString("imageTag")

        //Pass Image
        Glide.with(this)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_baseline_image_24)
            )
            .into(binding.animalDetailImage)

        //Pass Title
        binding.imageTitle.text = imageTitle

        //Pass Description
        binding.imageDescription.text = imageDescription

        // Set favorite icon based on whether the image is in the favorite list
        setFavoriteIcon(imageId)

        //Download Button
        binding.downloadButton.setOnClickListener {
            val request = DownloadManager.Request(Uri.parse(imageDownloadLink))
                .setTitle("Image Download") // Downloading Action Title
                .setDescription("Downloading image...") // Downloading Action Description
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Set notification when finish downloading
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "image.jpg"
                ) // Set directory where image is saved
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }

        //Share Button
        binding.shareButton.setOnClickListener {
            val imageUri = Uri.parse(imageDownloadLink) // Change String to Uri

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*" // Set data type to image
                putExtra(Intent.EXTRA_STREAM, imageUri) //
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission for receiving application
            }
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        //Add to favorite button
        binding.addToFavoriteButton.setOnClickListener {
            // Create Reference to database
            val favoriteRef =
                FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("FavoriteImages").child(
                    FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )

            // Create object to store image data
            val favoriteImage = AnimalModelAPI(
                imageId, imageUrl, imageTitle, imageDescription,
                imageDownloadLink!!, tag!!
            )

            // Check if image is already in favorite list
            val imageIdToDelete = imageId ?: ""
            favoriteRef.child(imageIdToDelete).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // button will delete if already in favorite list
                    favoriteRef.child(imageIdToDelete).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT)
                                .show()
                            binding.addToFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "AnimalDetail",
                                "Failed to remove from favorites: ${exception.message}"
                            )
                            Toast.makeText(
                                this,
                                "Failed to remove from favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Button will add if not in favorite list
                    favoriteRef.child(imageIdToDelete).setValue(favoriteImage)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                            binding.addToFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_checked_24)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "AnimalDetail",
                                "Failed to add to favorites: ${exception.message}"
                            )
                            Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }.addOnFailureListener { exception ->
                Log.e(
                    "AnimalDetail",
                    "Failed to check if image exists in favorites: ${exception.message}"
                )
                Toast.makeText(
                    this,
                    "Failed to check if image exists in favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //Add to collection button
        binding.btnAddToCollection.setOnClickListener {
            showCollectionsDialog()
        }

        //Related Image
        fetchImage(tag)
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            binding.addToFavoriteButton.isEnabled = false
            binding.addToFavoriteButton.setOnClickListener {
                Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFavoriteIcon(imageId: String?) {
        val favoriteRef =
            FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("FavoriteImages")
                .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")

        favoriteRef.child(imageId ?: "").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Image is in favorite list, set favorite icon to a different drawable
                binding.addToFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_checked_24)
            } else {
                // Image is not in favorite list, keep the default favorite icon
                binding.addToFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
            }
        }.addOnFailureListener { exception ->
            Log.e(
                "AnimalDetail",
                "Failed to check if image exists in favorites: ${exception.message}"
            )
            Toast.makeText(this, "Failed to check if image exists in favorites", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun fetchImage(tag: String?) {
        val getUrls = urls + tag + page + perPage
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, getUrls, null,
            { response ->
                try {
                    val jsonObject = response.getJSONArray("results")
                    for (i in 0 until jsonObject.length()) {
                        val imageSlot = jsonObject.getJSONObject(i)
                        //Id
                        val imageId = imageSlot.getString("id")

                        //Url to appear on the gallery
                        val imageUrls = imageSlot.getJSONObject("urls")
                        val urls = imageUrls.getString("regular")

                        //Title
                        val imageTitle = imageSlot.getString("alt_description")

                        //description
                        var imageDescription = imageSlot.getString("description")
                        if (imageDescription == "null") imageDescription =
                            "No description for this image"

                        //Download link
                        val imageDownloadLinks = imageSlot.getJSONObject("links")
                        val imageDownloadLink = imageDownloadLinks.getString("download")

                        //Tag
                        val imageTags = imageSlot.getJSONArray("tags")
                        val imageTag = imageTags.getJSONObject(2)
                        val animalTag = imageTag.getString("title")

                        //pass the image's information to the list
                        val imageModel = AnimalModelAPI(
                            imageId,
                            urls,
                            imageTitle,
                            imageDescription,
                            imageDownloadLink,
                            animalTag
                        )
                        list.add(imageModel)
                    }
                    customAdapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            },
            { _ ->
            }
        )
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    //Add image to collection
    private fun showCollectionsDialog() {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        val database =
            FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("collections").child(currentUserID ?: "")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val collectionNames = mutableListOf<String>()
                val collectionKeys = mutableListOf<String>()

                for (data in snapshot.children) {
                    val key = data.key
                    val collectionName = data.child("collectionName").getValue(String::class.java)
                    if (key != null && collectionName != null) {
                        collectionNames.add(collectionName)
                        collectionKeys.add(key)
                    }
                }

                val adapter = object : ArrayAdapter<String>(
                    this@AnimalDetail,
                    android.R.layout.simple_list_item_1,
                    collectionNames
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        val textView = view.findViewById<TextView>(android.R.id.text1)

                        val typeface = ResourcesCompat.getFont(context, R.font.acme)
                        textView.typeface = typeface
                        view.setBackgroundResource(R.drawable.item_bg)

                        return view
                    }
                }

                val dialogBuilder = AlertDialog.Builder(this@AnimalDetail)
                dialogBuilder.setTitle("Choose a Collection")
                dialogBuilder.setAdapter(adapter) { _, which ->
                    addToCollection(collectionKeys[which])
                }
                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                dialogBuilder.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnimalDetail", "Failed to fetch collections list: ${error.message}")
                Toast.makeText(
                    this@AnimalDetail,
                    "Failed to fetch collections list",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun addToCollection(collectionKey: String) {
        val imageId = intent.getStringExtra("imageId")
        val imageUrl = intent.getStringExtra("imageUrl")
        val imageTitle = intent.getStringExtra("imageTitle")
        val imageDescription = intent.getStringExtra("imageDescription")
        val imageDownloadLink = intent.getStringExtra("imageDownloadLink")
        val tag = intent.getStringExtra("imageTag")

        val database =
            FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("collections")
                .child(FirebaseAuth.getInstance().currentUser?.uid ?: "").child(collectionKey)
                .child(imageId ?: "")

        val animalModel = AnimalModelAPI(
            imageId,
            imageUrl,
            imageTitle,
            imageDescription,
            imageDownloadLink ?: "",
            tag ?: ""
        )
        database.setValue(animalModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Image added to collection", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("AnimalDetail", "Failed to add image to collection: ${exception.message}")
                Toast.makeText(this, "Failed to add image to collection", Toast.LENGTH_SHORT).show()
            }
    }
}