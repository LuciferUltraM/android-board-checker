package com.codemobi.boardchecker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.codemobi.boardchecker.Photo
import com.codemobi.boardchecker.R

class PhotoAdapter(private  val context: Context,
                   private val dataSource: ArrayList<Photo>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.list_item_photo, parent, false)

        val imageView = rowView.findViewById<ImageView>(R.id.imageView)
        val textViewNo = rowView.findViewById<TextView>(R.id.textViewNo)
        val textViewCreated = rowView.findViewById<TextView>(R.id.textViewCreated)

        val photo = getItem(position)

        Glide.with(context).load(photo.fileURL).into(imageView)
        textViewNo.setText("No. ${photo.runningNumber}")
        textViewCreated.setText(photo.created.toString())
        textViewCreated.visibility = View.GONE

        return rowView
    }

    override fun getItem(position: Int): Photo {
        return dataSource.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource?.size
    }
}