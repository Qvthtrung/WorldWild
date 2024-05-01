package com.example.animalgallery.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.animalgallery.R
import com.example.animalgallery.activity.CollectionDetail
import com.example.animalgallery.model.CollectionModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CollectionAdapter(private val context: Context,
                        private var collections: List<CollectionModel>,
                        private val listener: CollectionAdapterListener): RecyclerView.Adapter<CollectionAdapter.MyViewHolder>() {

    private lateinit var database: DatabaseReference
    private val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionAdapter.MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_collections, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionAdapter.MyViewHolder, position: Int) {
        val collection = collections[position]
        holder.collectionName.text =  collection.collectionName

        // When you click the collection
        holder.root.setOnClickListener{
            val intent = Intent(context, CollectionDetail::class.java)
            intent.putExtra("collectionKey", collection.key)
            context.startActivity(intent)

        }

        // When you click the edit button
        holder.btnEdit.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Edit Collection Name")

            val input = EditText(context)
            input.setText(collection.collectionName)
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val newName = input.text.toString()
                updateCollectionName(collection, newName)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

        // When you click the delete button
        holder.btnDelete.setOnClickListener{
            deleteCollection(collection)
        }
    }

    private fun updateCollectionName(collection: CollectionModel, newName: String) {
        database = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("collections").child(currentUserID ?: "").child(collection.key!!)
        database.child("collectionName").setValue(newName)
            .addOnSuccessListener {
                Toast.makeText(
                    context,
                    "Collection updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
                listener.onCollectionUpdated() // Call the listener to fetch collections again after updating
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to update collection",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteCollection(collection: CollectionModel) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle("Confirm Deletion")
        alertDialogBuilder.setMessage("Are you sure you want to delete this collection?")

        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            database = FirebaseDatabase.getInstance("https://worldwild-79702-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("collections").child(currentUserID ?: "").child(collection.key!!)
            database.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "Collection deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    listener.onCollectionUpdated() // Call the listener to fetch collections again after deleting
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to delete collection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.show()
    }

    override fun getItemCount(): Int {
        return collections.size
    }

    interface CollectionAdapterListener {
        fun onCollectionUpdated()
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val collectionName = itemView.findViewById<TextView>(R.id.collectionName)!!
        val root = itemView.findViewById<CardView>(R.id.root)!!
        val btnEdit = itemView.findViewById<ImageView>(R.id.btnEdit)!!
        val btnDelete = itemView.findViewById<ImageView>(R.id.btnDelete)!!
    }
}