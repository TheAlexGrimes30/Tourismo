package com.example.turismo

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.turismo.models.Item

class ItemAdapter(
    private val items: MutableList<Item>,
    private val onItemDeleted: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeImage: ImageView = itemView.findViewById(R.id.placeImage)
        val placeName: TextView = itemView.findViewById(R.id.placeName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.placeName.text = item.title

        Glide.with(holder.itemView.context)
            .load(item.imagePath)
            .placeholder(R.drawable.image1)
            .into(holder.placeImage)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("place_name", item.title)
            intent.putExtra("image_url", item.imagePath)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeItem(position: Int) {
        val item = items[position]
        items.removeAt(position)
        notifyItemRemoved(position)
        onItemDeleted(item)
    }
}