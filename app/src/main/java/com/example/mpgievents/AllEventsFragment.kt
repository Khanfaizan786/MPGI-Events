package com.example.mpgievents

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*


class AllEventsFragment : Fragment() {

    private lateinit var recyclerAllEvents:RecyclerView
    private var eventList= arrayListOf<Events>()
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var eventsAdapter: AllEventsAdapter
    private lateinit var eventsRef:DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_events, container, false)

        recyclerAllEvents=view.findViewById(R.id.recyclerAllEvents)
        layoutManager = LinearLayoutManager(activity)

        eventsRef=FirebaseDatabase.getInstance().reference.child("All Events").child("Games")

        if (activity!=null) {
            eventsAdapter = AllEventsAdapter(activity as Context, eventList)
        }

        eventsRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                val message=error.message
                if (activity!=null) {
                    Toast.makeText(activity,"Error occurred: $message", Toast.LENGTH_LONG).show()
                }
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()
                for (ds in snapshot.children) {
                    val event:Events?=ds.getValue(Events::class.java)
                    if (event!=null) {
                        eventList.add(event)
                    }
                }
                if (activity!=null) {
                    Toast.makeText(activity,"${eventList.size}",Toast.LENGTH_LONG).show()
                    eventsAdapter = AllEventsAdapter(activity as Context, eventList)
                    recyclerAllEvents.adapter = eventsAdapter
                    recyclerAllEvents.layoutManager = layoutManager
                }
            }
        })

        return view
    }
}
