package com.example.animalgallery.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.animalgallery.adapter.CollectionAdapter
import com.example.animalgallery.databinding.ActivityMyCollectionsBinding
import com.example.animalgallery.model.CollectionModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MyCollectionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyCollectionsBinding
    private lateinit var collectionAdapter: CollectionAdapter
    private lateinit var list: MutableList<CollectionModel>

    private lateinit var database: DatabaseReference
    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCollectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //LinearLayoutSetup
        val linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = linearLayoutManager
        list = mutableListOf()
        collectionAdapter = CollectionAdapter(this, list, object : CollectionAdapter.CollectionAdapterListener {
            override fun onCollectionUpdated() {
                fetchCollections()
            }
        })
        binding.recyclerView.adapter = collectionAdapter

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/").reference.child("collections").child(currentUserID ?: "")

        // Read data from Firebase database
        fetchCollections()

        binding.btnCreateCollection.setOnClickListener{
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Create New Collection")

            val input = EditText(this)
            alertDialogBuilder.setView(input)

            alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                val collectionName = input.text.toString().trim()

                if (collectionName.isNotEmpty()) {
                    val newCollectionRef = database.push()
                    newCollectionRef.setValue(CollectionModel(newCollectionRef.key,collectionName))
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@MyCollectionsActivity,
                                "Collection created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            fetchCollections()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@MyCollectionsActivity,
                                "Failed to create collection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(
                        this@MyCollectionsActivity,
                        "Please enter collection name",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            alertDialogBuilder.show()
        }
    }

    private fun fetchCollections() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear the existing list
                list.clear()

                // Loop through the dataSnapshot to get collections
                for (data in snapshot.children) {
                    val key = data.key
                    val collectionName = data.child("collectionName").getValue(String::class.java)
                    if (key != null && collectionName != null) {
                        // Add the collection to the list
                        list.add(CollectionModel(key, collectionName))
                    }
                }

                // Notify the adapter that the data set has changed
                collectionAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("MyCollectionsActivity", "Failed to fetch collections list: ${error.message}")
                Toast.makeText(this@MyCollectionsActivity, "Failed to fetch favorite list", Toast.LENGTH_SHORT).show()
            }
        })
    }
}