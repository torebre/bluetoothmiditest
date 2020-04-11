package com.example.bluetoothmiditest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceAdapter(
    private val deviceList: List<Pair<String, String>>,
    private val connectHandler: ConnectHandler
) :
    RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {


    class ViewHolder(
        view: View, private val deviceList: List<Pair<String, String>>,
        private val connectHandler: ConnectHandler
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val textView: TextView = view.findViewById(R.id.textView)

        init {
            textView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            connectHandler.deviceEntryClicked(deviceList[adapterPosition].second)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.row_element, parent, false),
        deviceList, connectHandler
    )

    override fun getItemCount() = deviceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = deviceList[position].let {
            """${it.first} (${it.second})"""
        }
    }


}

interface ConnectHandler {

    fun deviceEntryClicked(macAddress: String)

}