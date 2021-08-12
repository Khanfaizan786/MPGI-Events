package com.example.mpgievents

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var imgSlider:ImageSlider
    private lateinit var mainToolbar:Toolbar
    private lateinit var imgGoToAdmin:ImageView
    private lateinit var imagesRef:DatabaseReference
    private lateinit var cardViewGames:CardView
    private lateinit var cardViewHackathon:CardView
    private lateinit var cardViewCampusDrives:CardView
    private lateinit var cardViewCompetitions:CardView
    private lateinit var cardViewQuiz:CardView
    private lateinit var cardViewOthers:CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_home, container, false)

        imgSlider=view.findViewById(R.id.imgSliderEvents)
        mainToolbar=view.findViewById(R.id.mainToolbar)
        imgGoToAdmin=view.findViewById(R.id.imgGoToAdmin)
        cardViewGames=view.findViewById(R.id.cardViewGames)
        cardViewHackathon=view.findViewById(R.id.cardViewHackathon)
        cardViewCampusDrives=view.findViewById(R.id.cardViewCampusDrives)
        cardViewCompetitions=view.findViewById(R.id.cardViewCompetitions)
        cardViewQuiz=view.findViewById(R.id.cardViewQuiz)
        cardViewOthers=view.findViewById(R.id.cardViewOthers)

        imagesRef=FirebaseDatabase.getInstance().reference.child("Main Events")

        if (activity!=null) {
            (activity as AppCompatActivity).setSupportActionBar(mainToolbar)
        }

        val display: Display? = activity?.windowManager?.defaultDisplay
        val size:Point= Point()
        display?.getSize(size)
        val width:Int=size.x

        val layoutParams:android.view.ViewGroup.LayoutParams=imgSlider.layoutParams
        layoutParams.height=(3*width/4)-30
        //layoutParams.width=width
        imgSlider.layoutParams=layoutParams

        val remoteImages=ArrayList<SlideModel>()
        val eventKeys=ArrayList<String>()

        val postListener=object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChildren()) {
                    remoteImages.clear()
                    for (ds in snapshot.children) {
                        remoteImages.add(SlideModel(ds.child("image").value.toString(), ds.child("title").value.toString(),ScaleTypes.FIT))
                        eventKeys.add(ds.key.toString())
                    }
                    imgSlider.setImageList(remoteImages,ScaleTypes.FIT)

                    imgSlider.setItemClickListener(object :ItemClickListener{
                        override fun onItemSelected(position: Int) {
                            val options=arrayOf("View details","Delete Event")
                            val builder=AlertDialog.Builder(activity as Context)
                            builder.setTitle("Choose an option")
                            builder.setItems(options) { _, which ->
                                if (which==0) {
                                    if (activity!=null) {
                                        activity!!.supportFragmentManager.beginTransaction().replace(R.id.frame, EventDetailsFragment()).commit()
                                    }
                                } else {
                                    builder.setTitle("Delete Event")
                                    builder.setMessage("Are you sure ?")
                                    builder.setPositiveButton("Delete") { _,_ ->
                                        val eventKey=eventKeys[position]
                                        val eventReference=FirebaseDatabase.getInstance().reference.child("Main Events")
                                        eventReference.child(eventKey).removeValue()
                                    }
                                    builder.setNegativeButton("Cancel") { _,_-> }
                                    builder.create().show()
                                }
                            }
                            builder.create().show()
                        }
                    })
                }
            }
        }
        imagesRef.addValueEventListener(postListener)

        imgGoToAdmin.setOnClickListener {
            if (activity!=null) {
                val intent=Intent(activity,AdminActivity::class.java)
                startActivity(intent)
            }
        }

        cardViewGames.setOnClickListener {
            if (activity!=null) {
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.frame, AllEventsFragment()).commit()
            }
        }

        return view
    }

}
