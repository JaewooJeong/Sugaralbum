/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sugarmount.sugaralbum.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sugarmount.sugaralbum.R
import com.sugarmount.sugaralbum.model.ImageResData
import java.io.File

class ImageAdapter(
    private val items: List<ImageResData>,
    private val size: Int,
    private val onClick: (ImageResData) -> Unit,
    private val onCounterUpdate: (ImageResData) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        var itemCounter = 0
        private var selectedItem: Drawable? = null
        private var deselectedItem: Drawable? = null
    }

    /* ViewHolder for ImageResData, takes in the inflated view and the onClick behavior. */
    inner class ImageViewHolder(itemView: View, val onClick: (ImageResData) -> Unit, val OnCounterUpdate: (ImageResData) -> Unit)  :
        RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.counter_tv)
        val imageView: ImageView = itemView.findViewById(R.id.thumbnail_iv)
        val tintView: View = itemView.findViewById(R.id.tint_v)
        private var currentImage: ImageResData? = null

        init {
            selectedItem = ContextCompat.getDrawable(itemView.context, R.drawable.image_counter_selected)
            deselectedItem = ContextCompat.getDrawable(itemView.context, R.drawable.image_counter_not_selected)

            itemView.setOnClickListener {
                currentImage?.let {
                    it.checked = !it.checked

                    if(it.checked)
                        itemCounter++
                    else
                        itemCounter--

                    if(it.selectOrder != null) {
                        getItemList().stream().filter { filterData -> filterData.selectOrder != null }.forEach { data ->
                            run {
                                if(data.selectOrder != null) {
                                    if (data.selectOrder > it.selectOrder) {
                                        data.selectOrder -= 1
                                        OnCounterUpdate(data)
                                    }
                                }
                            }
                        }
                    }

                    if(it.checked)
                        it.selectOrder = itemCounter
                    else
                        it.selectOrder = null

                    textView.text = if (it.checked) it.selectOrder.toString() else ""
                    textView.background = if (it.checked) selectedItem else deselectedItem

                    if(it.checked) tintView.visibility = View.VISIBLE
                    else tintView.visibility = View.GONE

                    onClick(it)
                }
            }
        }

        /* Bind ImageResData name and image. */
        fun bind(image: ImageResData) {
            currentImage = image

            Glide.with(itemView.context)
                .load(currentImage!!.contentUri)
                .thumbnail(0.1f)
                .into(imageView)

//            Glide.with(imageView.context)
//                .load(currentImage!!.contentUri)
//                .into(imageView)
        }
    }

    /* Creates and inflates view and return ImageResDataViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_cell, parent, false)

        val params = (view.layoutParams as GridLayoutManager.LayoutParams)
        params.height = size
        params.width = size
        view.layoutParams = params

        return ImageViewHolder(view, onClick, onCounterUpdate)
    }

    /* Gets current ImageResData and uses it to bind view. */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val image = items[position]
        val viewHolder = holder as ImageViewHolder
        viewHolder.bind(image)

        if(image.checked) holder.tintView.visibility = View.VISIBLE
        else holder.tintView.visibility = View.GONE

        holder.textView.text = if (image.checked) image.selectOrder.toString() else ""
        holder.textView.background = if (image.checked) selectedItem else deselectedItem
    }

    override fun getItemCount(): Int = items.size

    fun getItemList(): List<ImageResData> = items
}

object ImageDiffCallback : DiffUtil.ItemCallback<ImageResData>() {
    override fun areItemsTheSame(oldItem: ImageResData, newItem: ImageResData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ImageResData, newItem: ImageResData): Boolean {
        return oldItem._id == newItem._id
    }
}