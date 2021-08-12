package com.example.mpgievents

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.iceteck.silicompressorr.SiliCompressor
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AdminActivity : AppCompatActivity() {

    private lateinit var adminTitle:EditText
    private lateinit var cardViewAdmin:CardView
    private lateinit var btnUploadAdmin:Button
    private lateinit var imgAdminPost:ImageView
    private lateinit var staticTxtAddImg:TextView
    private lateinit var etEventDate:EditText
    private lateinit var etEventFees:EditText
    private lateinit var spinnerEventType:Spinner

    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200

    lateinit var cameraPermissions: Array<String>
    lateinit var storagePermissions: Array<String>
    private lateinit var loadingBar: ProgressDialog

    private var resultUri: Uri? = null
    private lateinit var adapterEventTypes:ArrayAdapter<String>

    private lateinit var saveCurrentDate: String
    private lateinit var saveCurrentTime: String
    private lateinit var postRandomName: String
    private var downloadUrl: String="none"
    private lateinit var currentUserId: String
    private  var TAG:String="Admin Activity"

    private lateinit var mAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        adminTitle=findViewById(R.id.adminTitle)
        cardViewAdmin=findViewById(R.id.cardViewAdmin)
        btnUploadAdmin=findViewById(R.id.btnUploadAdmin)
        imgAdminPost=findViewById(R.id.imgAdminPost)
        staticTxtAddImg=findViewById(R.id.staticTxtAddImg)
        etEventDate=findViewById(R.id.etEventDate)
        etEventFees=findViewById(R.id.etEventFees)
        spinnerEventType=findViewById(R.id.spinnerEventType)

        loadingBar= ProgressDialog(this@AdminActivity)

        cameraPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions =
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        populateSpinnerGender()

        cardViewAdmin.setOnClickListener {
            pickFromGallery()
        }

        imgAdminPost.setOnClickListener {
            //pickFromGallery()
            //Toast.makeText(this,"Clicked on Image",Toast.LENGTH_SHORT).show()
        }

        staticTxtAddImg.setOnClickListener {
            pickFromGallery()
        }

        btnUploadAdmin.setOnClickListener {
            validatePostInfo()
        }


    }

    private fun validatePostInfo() {
        val title=adminTitle.text.toString()
        val date=etEventDate.text.toString()
        val fees=etEventFees.text.toString()
        val eventType=spinnerEventType.selectedItem.toString()

        val callForDate = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd")
        saveCurrentDate = currentDate.format(callForDate.time)

        val callForTime = Calendar.getInstance()
        val currentTime = SimpleDateFormat("HH:mm:ss")
        saveCurrentTime = currentTime.format(callForTime.time)

        val currentDate2= SimpleDateFormat("dd MMM yyyy")
        val chatDate=currentDate2.format(callForDate.time)

        val currentTime2 = SimpleDateFormat("hh:mm aa")
        val chatTime = currentTime2.format(callForTime.time)

        postRandomName = saveCurrentDate + saveCurrentTime

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this@AdminActivity,"Please Enter title",Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(date)) {
            Toast.makeText(this@AdminActivity,"Please Enter Date",Toast.LENGTH_LONG).show()
        } else if (TextUtils.isEmpty(fees)) {
            Toast.makeText(this@AdminActivity,"Please Enter Registration Fees",Toast.LENGTH_LONG).show()
        } else if (eventType=="Select") {
            Toast.makeText(this@AdminActivity,"Please select event type",Toast.LENGTH_LONG).show()
        } else if (resultUri==null) {
            val builder=AlertDialog.Builder(this@AdminActivity)
            builder.setTitle("Upload without image ?")
            builder.setMessage("This event will not show on home screen because no image selected")
            builder.setPositiveButton("Upload") { _,_->
                loadingBar.setTitle("Uploading Event")
                loadingBar.setMessage("Please wait while we are uploading...!!")
                loadingBar.setCanceledOnTouchOutside(true)
                loadingBar.show()

                mAuth=FirebaseAuth.getInstance()
                val user=mAuth.currentUser
                if (user!=null) {
                    saveEventToDatabase(title,date,fees,chatDate,chatTime,postRandomName,eventType)
                } else {
                    signInAnonymously(mAuth)
                }
            }
            builder.setNegativeButton("Cancel") { _,_-> }
            builder.create().show()
        } else {
            loadingBar.setTitle("Uploading Event")
            loadingBar.setMessage("Please wait while we are uploading...!!")
            loadingBar.setCanceledOnTouchOutside(true)
            loadingBar.show()

            mAuth=FirebaseAuth.getInstance()
            val user=mAuth.currentUser
            if (user!=null) {
                saveImageToFirebaseStorage(title,resultUri!!,chatDate,chatTime,postRandomName,date,fees,eventType)
            } else {
                signInAnonymously(mAuth)
            }
        }
    }

    private fun saveEventToDatabase(
        title: String,
        date: String,
        fees: String,
        chatDate: String,
        chatTime: String,
        postRandomName: String,
        eventType: String
    ) {
        val allEventsRef = FirebaseDatabase.getInstance().reference.child("All Events").child(eventType)

        val postsMap = HashMap<String, String>()
        postsMap["date"]=date
        postsMap["fees"]=fees
        postsMap["title"] = title
        postsMap["uploadDate"] = chatDate
        postsMap["uploadTime"] = chatTime
        postsMap["randomName"] = postRandomName

        allEventsRef.child(postRandomName + title)
            .updateChildren(postsMap as Map<String, Any>).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Event uploaded successfully..!!", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                    sendUserToMainActivity()
                } else {
                    val message = it.exception?.message
                    Toast.makeText(
                        this@AdminActivity,
                        "Error occured: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingBar.dismiss()
                }
            }
    }

    private fun signInAnonymously(
        mAuth: FirebaseAuth
    ) {
        mAuth.signInAnonymously().addOnSuccessListener {
            validatePostInfo()
        }.addOnFailureListener {
            Log.e(TAG,"signInAnonymouslyFailure",it)
        }
    }

    private fun saveImageToFirebaseStorage(
        title: String,
        resultUri: Uri,
        chatDate: String,
        chatTime: String,
        postRandomName: String,
        date: String,
        fees: String,
        eventType: String
    ) {
        val storageRef=FirebaseStorage.getInstance().reference.child("Event Images")

        val filePath = storageRef.child("${resultUri.lastPathSegment}${postRandomName}")
        filePath.putFile(resultUri).addOnSuccessListener {
            Toast.makeText(
                this@AdminActivity,
                "Image uploaded successfully to storage",
                Toast.LENGTH_SHORT
            ).show()
            filePath.downloadUrl.addOnSuccessListener {
                downloadUrl = it.toString()
                Toast.makeText(this,"Yahan tk aa gye",Toast.LENGTH_LONG).show()
                savePostInfoToDatabase(title,chatDate,chatTime,downloadUrl, postRandomName,date,fees,eventType)
            }.addOnFailureListener {
                val message= it.message
                Toast.makeText(this@AdminActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }.addOnFailureListener {
            val message= it.message
            Toast.makeText(this@AdminActivity, "Error occured: $message", Toast.LENGTH_SHORT).show()
            loadingBar.dismiss()
        }
    }

    private fun savePostInfoToDatabase(
        title: String,
        chatDate: String,
        chatTime: String,
        downloadUrl: String,
        postRandomName: String,
        date: String,
        fees: String,
        eventType: String
    ) {
        val eventsImagesRef=FirebaseDatabase.getInstance().reference.child("Main Events")

        val postsMap = HashMap<String, String>()
        postsMap["date"] = chatDate
        postsMap["time"] = chatTime
        postsMap["image"] = downloadUrl
        postsMap["title"] = title
        postsMap["randomName"] = postRandomName

        eventsImagesRef.child(postRandomName + title)
            .updateChildren(postsMap as Map<String, Any>).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this@AdminActivity, "Event uploaded successfully..!!", Toast.LENGTH_SHORT).show()
                    saveEventToDatabase(title, date, fees, chatDate, chatTime, postRandomName, eventType)
                } else {
                    val message = it.exception?.message
                    Toast.makeText(
                        this@AdminActivity,
                        "Error occured: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingBar.dismiss()
                }
            }
    }

    private fun sendUserToMainActivity() {
        val intent=Intent(this@AdminActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun pickFromGallery() {
        if (!checkCameraPermission()) {
            requestCameraPermission()
            if (!checkStoragePermission()) {
                requestStoragePermission()
            }
        } else if (!checkStoragePermission()) {
            requestStoragePermission()
        } else {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(4,3)
                .start(this@AdminActivity)
        }
    }

    private fun checkCameraPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this@AdminActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this@AdminActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this@AdminActivity,
            cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this@AdminActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this@AdminActivity,
            storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted =
                        grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(
                            this@AdminActivity,
                            "Camera & Storage both permissions are necessary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val writeStorageAccepted =
                        grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (writeStorageAccepted) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(
                            this@AdminActivity,
                            "Storage permissions are necessary",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                resultUri = result.uri
                if (resultUri!=null) {
                    compressImage(resultUri!!)
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val exception = result.error
                Toast.makeText(this, "Error occured: $exception", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun compressImage(resultUri: Uri) {

        staticTxtAddImg.visibility= View.GONE

        val file: File = File(
            SiliCompressor.with(this)
            .compress(com.iceteck.silicompressorr
                .FileUtils.getPath(this,resultUri), File(this.cacheDir,"temp")
            ))
        this.resultUri=Uri.fromFile(file)
        Picasso.get().load(this.resultUri)
            .into(imgAdminPost)
    }

    private fun populateSpinnerGender() {
        adapterEventTypes=ArrayAdapter(this@AdminActivity,android.R.layout.simple_spinner_item,resources.getStringArray(
            R.array.event_types
        ))
        adapterEventTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEventType.adapter=adapterEventTypes
    }
}