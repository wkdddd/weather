package com.example.weather

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaceAdapter(var placeList: List<Place>) :
    RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val placeName: TextView = view.findViewById(R.id.placename)
        val addressName: TextView = view.findViewById(R.id.placeAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.place_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val place = placeList[position]
            val intent = Intent("com.example.weather.ACTION_START").apply {
                putExtra(WeatherActivity2.LOCATION_LNG, place.location.lng)
                putExtra(WeatherActivity2.PLACE_NAME, place.name)

                putExtra(WeatherActivity2.LOCATION_LAT, place.location.lat)
            }

            parent.getContext().startActivity(intent)
//            WeatherActivity2.start(parent.context,place.name)
        }

        return viewHolder
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.addressName.text = place.formatted_address
    }

    override fun getItemCount() = placeList.size

}
