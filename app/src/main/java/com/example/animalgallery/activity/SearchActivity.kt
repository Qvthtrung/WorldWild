package com.example.animalgallery.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.animalgallery.adapter.CustomAdapter
import com.example.animalgallery.databinding.ActivitySearchBinding
import com.example.animalgallery.model.AnimalModelAPI
import org.json.JSONException

class SearchActivity : AppCompatActivity() {
    //For setting up StaggerGridLayout and Fetch API
    private lateinit var binding: ActivitySearchBinding
    private lateinit var customAdapter: CustomAdapter
    private lateinit var list: MutableList<AnimalModelAPI>
    private var urls: String = "https://api.unsplash.com/search/photos/?client_id=B1MEAcq9sTZhPJ3wO0tel73QZPJ6GiXYooyL8sPZ0f4&collections=4760062&query="
    private var page: Int = 1
    private var perPage: String = "&per_page=10&page="
    //For infinite scroll
    var currentItem: Int = 0
    var totalItem: Int = 0
    var scrollOut: Int = 0
    var scrolling: Boolean = false
    //For Search view
    var searchText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //StaggeredGridLayout Setup
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.searchRecyclerView.layoutManager = staggeredGridLayoutManager
        list = mutableListOf()
        customAdapter = CustomAdapter(this, list)
        binding.searchRecyclerView.adapter = customAdapter

        //Infinite scrolling
        binding.searchRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    scrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                currentItem = staggeredGridLayoutManager.childCount
                totalItem = staggeredGridLayoutManager.itemCount
                scrollOut = staggeredGridLayoutManager.findFirstVisibleItemPositions(null)[0]

                if (scrolling && (currentItem + scrollOut == totalItem)) {
                    scrolling = false
                    fetchSearchImage()
                }
            }
        })

        //SearchView Setup
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(newText: String?): Boolean {
                searchText = newText?.lowercase()
                if (newText != null){
                    list.clear()
                    page = 1
                    fetchSearchImage()
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }

        })

        //Navigation
        binding.home.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        binding.userManagement.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }

    }

    private fun fetchSearchImage() {
        val getUrls = urls+searchText+perPage+page
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, getUrls, null,
            { response ->
                try {
                    val jsonObject = response.getJSONArray("results")
                    for (i in 0 until jsonObject.length()) {
                        val imageSlot = jsonObject.getJSONObject(i)

                        //id
                        val imageId = imageSlot.getString("id")

                        //Url to appear on the gallery
                        val imageUrls = imageSlot.getJSONObject("urls")
                        val urls = imageUrls.getString("regular")

                        //Title
                        val imageTitle = imageSlot.getString("alt_description")

                        //description
                        var imageDescription = imageSlot.getString("description")
                        if (imageDescription == "null") imageDescription = "No description for this image"

                        //Download link
                        val imageDownloadLinks = imageSlot.getJSONObject("links")
                        val imageDownloadLink = imageDownloadLinks.getString("download")

                        //Tag
                        val imageTags = imageSlot.getJSONArray("tags")
                        val imageTag = imageTags.getJSONObject(2)
                        val animalTag = imageTag.getString("title")

                        //pass the image's information to the list
                        val imageModel = AnimalModelAPI(imageId, urls, imageTitle, imageDescription, imageDownloadLink, animalTag)
                        list.add(imageModel)
                    }
                    customAdapter.notifyDataSetChanged()
                    page++
                    binding.pro1.visibility = View.GONE

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
}