package com.example.animalgallery.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.animalgallery.R
import com.example.animalgallery.activity.AnimalDetail
import com.example.animalgallery.model.AnimalModelAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CollectionDetailAdapter(private val context: Context, private val collectionKey: String?, private var animals: List<AnimalModelAPI>): RecyclerView.Adapter<CollectionDetailAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionDetailAdapter.MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_collection_images, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return animals.size
    }

    override fun onBindViewHolder(holder: CollectionDetailAdapter.MyViewHolder, position: Int) {
        val animal = animals[position]
        Glide.with(context)
            .load(animal.regular)
            .apply(
                RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_image_24)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            )
            .into(holder.image)

        // When you click the image
        holder.rlRoot.setOnClickListener {
            // when you click the image
            val intent = Intent(context, AnimalDetail::class.java)
            intent.putExtra("imageId", animal.id)
            intent.putExtra("imageUrl", animal.regular)
            intent.putExtra("imageTitle", animal.imageTitle)
            intent.putExtra("imageDescription", animal.imageDescription)
            intent.putExtra("imageDownloadLink", animal.imageDownloadLink)
            intent.putExtra("imageTag", animal.tag)
            context.startActivity(intent)
        }

        // When you click the share button
        holder.btnShare.setOnClickListener {
            val imageUri = Uri.parse(animal.imageDownloadLink)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*" // Set data type to image
                putExtra(Intent.EXTRA_STREAM, imageUri) // Share image link
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission for receiving application
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        //When you click the remove button
        holder.btnRemove.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirm Deletion")
            builder.setMessage("Are you sure you want to remove this image from the collection?")

            builder.setPositiveButton("Yes") { _, _ ->
                val imageId = animal.id
                val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
                val collectionRef = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("collections")
                    .child(currentUserID ?: "")
                    .child(collectionKey ?: "")
                    .child(imageId!!)
                collectionRef.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Image removed from collection", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show()
                        Log.e("CollectionDetailAdapter", "Failed to delete image", e)
                    }
            }

            builder.setNegativeButton("No") { dialog, _ ->
                // Do nothing
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.animalImage)!!
        val rlRoot = itemView.findViewById<RelativeLayout>(R.id.rlroot)!!
        val btnShare = itemView.findViewById<ImageView>(R.id.btnShare)!!
        val btnRemove = itemView.findViewById<ImageView>(R.id.btnRemove)!!
    }
}