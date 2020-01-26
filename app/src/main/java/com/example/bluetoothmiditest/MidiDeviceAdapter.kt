package com.example.bluetoothmiditest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MidiDeviceAdapter(private val deviceList: List<String>) :
    RecyclerView.Adapter<MidiDeviceAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.textView)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.row_element, parent, false)
    )


    override fun getItemCount() = deviceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = deviceList[position]
    }

}