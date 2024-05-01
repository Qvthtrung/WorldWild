package com.example.animalgallery.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.animalgallery.adapter.CollectionDetailAdapter
import com.example.animalgallery.databinding.ActivityCollectionDetailBinding
import com.example.animalgallery.model.AnimalModelAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CollectionDetail : AppCompatActivity() {
    private lateinit var binding: ActivityCollectionDetailBinding

    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    //For setting up StaggerGridLayout
    private lateinit var collectionDetailAdapter: CollectionDetailAdapter
    private lateinit var list: MutableList<AnimalModelAPI>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        val bundle: Bundle? = intent.extras
        val collectionKey = bundle?.getString("collectionKey")

        //StaggeredGridLayout Setup
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        list = mutableListOf()
        collectionDetailAdapter = CollectionDetailAdapter(this, collectionKey, list)
        binding.recyclerView.adapter = collectionDetailAdapter



        fetchCollectionImages(mUser!!, collectionKey)
    }

    private fun fetchCollectionImages(mUser: FirebaseUser, collectionKey: String?) {
        if (collectionKey.isNullOrEmpty()) {
            return
        }

        val collectionRef = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("collections")
            .child(mUser.uid)
            .child(collectionKey)

        collectionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (childSnapshot in snapshot.children) {
                    // Check if child has an "id" -> it is an image
                    if (childSnapshot.hasChild("id")) {
                        val id = childSnapshot.child("id").value.toString()
                        val regular = childSnapshot.child("regular").value.toString()
                        val imageTitle = childSnapshot.child("imageTitle").value.toString()
                        val imageDescription = childSnapshot.child("imageDescription").value.toString()
                        val imageDownloadLink = childSnapshot.child("imageDownloadLink").value.toString()
                        val tag = childSnapshot.child("tag").value.toString()

                        val animalModel = AnimalModelAPI(id, regular, imageTitle, imageDescription, imageDownloadLink, tag)

                        list.add(animalModel)
                    }
                }
                collectionDetailAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu có
                Log.e("CollectionDetail", "fetchCollectionImages:onCancelled", error.toException())
            }
        })

    }

}