package com.ats.apple.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ats.apple.DeviceDetailsActivity
import com.ats.apple.R
import com.ats.apple.util.types.Beacon


class BeaconAdapter(context: Context, beacons: List<Beacon>) :
    RecyclerView.Adapter<BeaconAdapter.DeviceViewHolder>() {
    private val context: Context
    private val beacons: List<Beacon>

    init {
        this.context = context
        this.beacons = beacons
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view, context, beacons)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {

        holder.nameView.text = beacons[position].name
        holder.serialNumberView.text = beacons[position].serialNumber
        holder.rssiView.text = beacons[position].rssi
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_card_background))

    }

    override fun getItemCount(): Int {
        return beacons.size
    }

    class DeviceViewHolder(view: View, context: Context, beacons: List<Beacon>) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        val nameView: TextView
        val serialNumberView: TextView
        val rssiView: TextView
        var beacons: List<Beacon>
        var context: Context

        init {
            this.beacons = beacons
            this.context = context
            view.setOnClickListener(this)
            nameView = view.findViewById(R.id.device_name)
            serialNumberView = view.findViewById(R.id.device_serialnumber)
            rssiView = view.findViewById(R.id.device_rssi)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            val beacon: Beacon = beacons[position]
            val intent = Intent(context, DeviceDetailsActivity::class.java)
            intent.putExtra("name", beacon.name)
            intent.putExtra("address", beacon.address)
            intent.putExtra("rssi", beacon.rssi)
            intent.putExtra("uuids", beacon.uuids)
            intent.putExtra("serialNumber", beacon.serialNumber)
            context.startActivity(intent)
        }
    }
}