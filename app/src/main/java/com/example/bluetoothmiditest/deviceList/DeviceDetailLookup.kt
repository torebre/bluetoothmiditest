package com.example.bluetoothmiditest.deviceList

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class DeviceDetailLookup(private val recyclerView: RecyclerView):
    ItemDetailsLookup<String>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        if(view != null) {
            return (recyclerView.getChildViewHolder(view) as DeviceListAdapter.DeviceViewHolder).getItemDetails()
        }
        return null
    }


}