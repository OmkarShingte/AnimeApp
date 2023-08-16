package com.sportsintercative.contentapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sportsintercative.contentapp.R
import com.sportsintercative.contentapp.models.DeviceInfo

class DevicesAdapter(private val dataSet: ArrayList<DeviceInfo>) : RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define the views within each item
        // Example: val textView: TextView = view.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for the individual item view
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.textView).text = "(${dataSet[position].distance})  ${ dataSet[position].name } = ${dataSet[position].address}"
        // Bind the data to the views within each item
        // Example: holder.textView.text = dataSet[position]
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        return dataSet.size
    }
}