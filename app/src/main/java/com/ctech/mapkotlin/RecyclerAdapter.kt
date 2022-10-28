package com.ctech.mapkotlin

import android.content.Intent
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException

internal class RecyclerViewAdapter(private var imageModel: ArrayList<ImageModel>) : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var remove: ImageView = view.findViewById(R.id.image_cancel)
        var image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = imageModel[position]
        holder.image.setImageBitmap(model.getImage())
        holder.image.rotation = model.getRotate().toFloat()

//        holder.remove.setOnClickListener(View.OnClickListener {
//            imageModel.removeAt(position)
//            notifyItemRemoved(position)
//            notifyItemRangeChanged(position, imageModel.size)
//        })

    }

    override fun getItemCount(): Int {
        return  imageModel.size
    }
}