package com.example.animalgallery.activity

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.animalgallery.R
import com.example.animalgallery.databinding.ActivityAnimalDetailBinding

class AnimalDetail : AppCompatActivity() {
    private lateinit var binding: ActivityAnimalDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle: Bundle? = intent.extras
        val imageUrl = bundle!!.getString("imageUrl")
        val imageTitle = bundle.getString("imageTitle")
        val imageDescription = bundle.getString("imageDescription")
        val imageDownloadLink = bundle.getString("imageDownloadLink")

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
    }
}