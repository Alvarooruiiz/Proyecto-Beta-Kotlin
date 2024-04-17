package com.example.proyectobetakotlin.Images

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectobetakotlin.R

class RecyclerAdapterImages(private val context: Context, private val imagesList: ArrayList<Bitmap>) :
    RecyclerView.Adapter<RecyclerAdapterImages.ImageViewHolder>() {

    private var onImageClickListener: OnImageClickListener? = null
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_user_recycler, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = imagesList[position]
        holder.imageView.setImageBitmap(image)

        holder.imageView.setOnClickListener {
            onImageClickListener?.onImageClick(position)
        }

        // Cambiar el fondo de la vista si está seleccionada
        holder.itemView.isSelected = selectedPosition == position
    }

    fun setOnImageClickListener(onImageClickListener: OnImageClickListener) {
        this.onImageClickListener = onImageClickListener
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }

    fun setSelectedPosition(selectedPosition: Int) {
        this.selectedPosition = selectedPosition
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivImagesUser)
    }
}

