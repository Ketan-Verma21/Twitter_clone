package com.example.twitterclone.activites

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityProfileBinding
import com.example.twitterclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {
    lateinit var profileBinding: ActivityProfileBinding
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var imageurl :String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileBinding=ActivityProfileBinding.inflate(layoutInflater)
        var view = profileBinding.root
        setContentView(view)
        if(userId==null){
            finish()
        }
        profileBinding.ProfileProgressLayout.setOnTouchListener { v, event ->true  }
        profileBinding.photoIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }
        populateinfo()
    }
    fun populateinfo(){
        profileBinding.ProfileProgressLayout.visibility=View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {documentsnapshot->
                val user:User? = documentsnapshot.toObject(User::class.java)
                profileBinding.usernameET.setText(user?.username,TextView.BufferType.EDITABLE)
                profileBinding.emailET.setText(user?.email,TextView.BufferType.EDITABLE)
                imageurl=user?.imageurl
                imageurl?.let{
                    profileBinding.photoIV.loadUrl(user?.imageurl,R.drawable.logo)
                }
                profileBinding.ProfileProgressLayout.visibility=View.GONE

            }
            .addOnFailureListener {
                it.printStackTrace()
                finish()
            }
    }
    fun onApply(v: View){
        profileBinding.ProfileProgressLayout.visibility = View.VISIBLE
        val username = profileBinding.usernameET.text.toString()
        val email = profileBinding.emailET.text.toString()
        val map = HashMap<String, Any>()
        map[DATA_USER_USERNAME] = username
        map[DATA_USER_EMAIL] = email

        firebaseDB.collection(DATA_USERS).document(userId!!).update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
                profileBinding.ProfileProgressLayout.visibility = View.GONE
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && requestCode== REQUEST_CODE_PHOTO ){
            storeImage(data?.data)
        }
    }
    fun storeImage(imageuri: Uri?){
        imageuri?.let {
            Toast.makeText(this,"Uploading....",Toast.LENGTH_SHORT).show()
            profileBinding.ProfileProgressLayout.visibility=View.VISIBLE
            val filepath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filepath.putFile(imageuri).addOnSuccessListener {
                filepath.downloadUrl
                    .addOnSuccessListener {uri->
                        val url = uri.toString()
                        firebaseDB.collection(DATA_USERS).document(userId!!).update(
                            DATA_USER_IMAGE_URL,url).addOnSuccessListener {
                                imageurl = url
                                profileBinding.photoIV.loadUrl(imageurl,R.drawable.logo)
                            }
                        profileBinding.ProfileProgressLayout.visibility=View.GONE
                    }
                    .addOnFailureListener {
                    onuploadfailure()
                    }
            }.addOnFailureListener {
                onuploadfailure()
            }
        }
    }
    fun onuploadfailure(){
        Toast.makeText(this,"Image upload failed.Please try again later",Toast.LENGTH_SHORT).show()
        profileBinding.ProfileProgressLayout.visibility=View.GONE
    }
    fun onSignout(v:View){
        firebaseAuth.signOut()
        finish()
    }
    companion object{
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}