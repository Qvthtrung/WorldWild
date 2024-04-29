package com.example.animalgallery.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.animalgallery.adapter.CustomAdapter
import com.example.animalgallery.databinding.ActivityFavoriteListBinding
import com.example.animalgallery.model.AnimalModelAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteListBinding

    private lateinit var mAuth: FirebaseAuth
    private var mUser: FirebaseUser? = null

    //For setting up StaggerGridLayout
    private lateinit var customAdapter: CustomAdapter
    private lateinit var list: MutableList<AnimalModelAPI>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser

        //StaggeredGridLayout Setup
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        list = mutableListOf()
        customAdapter = CustomAdapter(this, list)
        binding.recyclerView.adapter = customAdapter

        fetchFavoriteList(mUser!!)
    }

    private fun fetchFavoriteList(mUser: FirebaseUser?) {
        val favoriteRef = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("FavoriteImages").child(mUser?.uid ?: "")

        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()

                for (imageSnapshot in snapshot.children) {
                    val imageModel = imageSnapshot.getValue(AnimalModelAPI::class.java)
                    imageModel?.let {
                        list.add(it)
                    }
                }

                customAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoriteListActivity", "Failed to fetch favorite list: ${error.message}")
                Toast.makeText(this@FavoriteListActivity, "Failed to fetch favorite list", Toast.LENGTH_SHORT).show()
            }
        })
    }
}