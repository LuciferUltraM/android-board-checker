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
import com.codemobi.boardchecker.Worksheet
import com.codemobi.boardchecker.R

class WorksheetAdapter(private  val context: Context,
                       private val dataSource: ArrayList<Worksheet>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.list_item_worksheet, parent, false)

        val textViewNo = rowView.findViewById<TextView>(R.id.textViewNo)
        val textViewName = rowView.findViewById<TextView>(R.id.textViewName)
        val textViewCreated = rowView.findViewById<TextView>(R.id.textViewCreated)

        val worksheet = getItem(position)

        textViewNo.setText("หมายเลขใบงาน : ${worksheet.number}")
        textViewName.setText("ชื่อโครงการ : ${worksheet.name}")
        textViewCreated.setText(worksheet.created.toString())

        return rowView
    }

    override fun getItem(position: Int): Worksheet {
        return dataSource.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource?.size
    }
}