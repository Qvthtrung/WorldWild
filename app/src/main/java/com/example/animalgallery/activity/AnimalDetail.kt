package com.example.animalgallery.activity

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
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
import org.json.JSONException

class AnimalDetail : AppCompatActivity() {
    private lateinit var binding: ActivityAnimalDetailBinding
    private lateinit var customAdapter: CustomAdapter
    private lateinit var list: MutableList<AnimalModelAPI>
    private var urls: String = "https://api.unsplash.com/search/photos/?client_id=B1MEAcq9sTZhPJ3wO0tel73QZPJ6GiXYooyL8sPZ0f4&collections=4760062,3330452&query="
    private var page: String = "&page=1"
    private var perPage: String = "&per_page=9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //StaggeredGridLayout Setup
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.detailRecyclerView.layoutManager = staggeredGridLayoutManager
        list = mutableListOf()
        customAdapter = CustomAdapter(this, list)
        binding.detailRecyclerView.adapter = customAdapter

        //Getting images information
        val bundle: Bundle? = intent.extras
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
                    .placeholder(R.drawable.ic_baseline_image_24))
            .into(binding.animalDetailImage)

        //Pass Title
        binding.imageTitle.text = imageTitle

        //Pass Description
        binding.imageDescription.text = imageDescription

        //Download Button
        binding.downloadButton.setOnClickListener{
            val request = DownloadManager.Request(Uri.parse(imageDownloadLink))
                .setTitle("Image Download") // Downloading Action Title
                .setDescription("Downloading image...") // Downloading Action Description
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Set notification when finish downloading
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.jpg") // Set directory where image is saved
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }

        //related image
        fetchImage(tag)
    }

    private fun fetchImage(tag: String?) {
        val getUrls = urls+tag+page+perPage
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, getUrls, null,
            { response ->
                try {
                    val jsonObject = response.getJSONArray("results")
                    for (i in 0 until jsonObject.length()) {
                        val imageSlot = jsonObject.getJSONObject(i)

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
                        val imageModel = AnimalModelAPI(urls, imageTitle, imageDescription, imageDownloadLink, animalTag)
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
}