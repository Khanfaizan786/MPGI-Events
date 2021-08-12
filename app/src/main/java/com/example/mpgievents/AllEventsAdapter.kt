package com.example.mpgievents

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AllEventsAdapter(val context: Context, private val eventList:ArrayList<Events>): RecyclerView.Adapter<AllEventsAdapter.EventViewHolder>() {
    class EventViewHolder(view: View):RecyclerView.ViewHolder(view) {
        val txtGameEventName:TextView=view.findViewById(R.id.txtGameEventName)
        val txtEventDate:TextView=view.findViewById(R.id.txtEventDate)
        val txtEventFees:TextView=view.findViewById(R.id.txtEventFees)
        val txtTeamsRegistered:TextView=view.findViewById(R.id.txtTeamsRegistered)
        val btnRegisterGameEvent:Button=view.findViewById(R.id.btnRegisterGameEvent)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.all_games_layout, parent,false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event=eventList[position]

        val date="Date - ${event.date}"
        val fees="Registration Fees - ${event.fees}"
        val registeredNo="Teams registered - 0"

        holder.txtGameEventName.text=event.title
        holder.txtEventDate.text=date
        holder.txtEventFees.text=fees
        holder.txtTeamsRegistered.text=registeredNo

        holder.btnRegisterGameEvent.setOnClickListener {
            val intent= Intent(context,PaymentActivity::class.java)
            context.startActivity(intent)
        }
    }
}