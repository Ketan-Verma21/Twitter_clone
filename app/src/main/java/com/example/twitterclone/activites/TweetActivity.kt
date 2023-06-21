package com.example.twitterclone.activites

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityTweetBinding
import com.example.twitterclone.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class TweetActivity : AppCompatActivity() {
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private var imageurl:String?=null
    private var userId:String?=null
    private var userName:String?=null
    lateinit var tweetBinding: ActivityTweetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tweetBinding=ActivityTweetBinding.inflate(layoutInflater)
        var view = tweetBinding.root
        setContentView(view)
        if(intent.hasExtra(PARAM_USER_NAME) && intent.hasExtra(PARAM_USER_ID)){
            userId=intent.getStringExtra(PARAM_USER_ID)
            userName=intent.getStringExtra(PARAM_USER_NAME)
        }else{
            Toast.makeText(this,"Error creating tweet",Toast.LENGTH_SHORT).show()
            finish()
        }
        tweetBinding.tweetProgressLayout.setOnTouchListener { v, event ->  true}
    }
    fun addImage(v:View){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== REQUEST_CODE_PHOTO ){
            storeImage(data?.data)
        }
    }
    fun storeImage(imageuri: Uri?){
        imageuri?.let {
            Toast.makeText(this,"Uploading....",Toast.LENGTH_SHORT).show()
            tweetBinding.tweetProgressLayout.visibility=View.VISIBLE
            val filepath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filepath.putFile(imageuri).addOnSuccessListener {
                filepath.downloadUrl
                    .addOnSuccessListener {uri->
                        imageurl = uri.toString()
                        tweetBinding.tweetImage.loadUrl(imageurl,R.drawable.logo)
                        tweetBinding.tweetProgressLayout.visibility=View.GONE
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
        tweetBinding.tweetProgressLayout.visibility=View.GONE
    }
    fun postTweet(v:View){
        tweetBinding.tweetProgressLayout.visibility=View.VISIBLE
        val text:String = tweetBinding.tweetText.text.toString()
        val hashtags=getHashtags(text)
        val tweetId = firebaseDB.collection(DATA_TWEETS).document()
        val tweet = Tweet(tweetId.id, arrayListOf(userId!!),userName,text,imageurl,System.currentTimeMillis(),hashtags,
            arrayListOf()
        )
        tweetId.set(tweet)
            .addOnCompleteListener { finish() }
            .addOnFailureListener { e->
                e.printStackTrace()
                tweetBinding.tweetProgressLayout.visibility=View.GONE
                Toast.makeText(this,"Failed to post the tweet",Toast.LENGTH_SHORT).show()
            }
    }
    fun getHashtags(source: String): ArrayList<String> {
        val hashtags = arrayListOf<String>()
        var text = source

        while (text.contains("#")) {
            var hashtag = ""
            val hash = text.indexOf("#")
            text = text.substring(hash + 1)

            val firstSpace = text.indexOf(" ")
            val firstHash = text.indexOf("#")

            if(firstSpace == -1 && firstHash == -1) {
                hashtag = text.substring(0)
            } else if (firstSpace != -1 && firstSpace < firstHash) {
                hashtag = text.substring(0, firstSpace)
                text = text.substring(firstSpace + 1)
            } else {
                hashtag = text.substring(0, firstHash)
                text = text.substring(firstHash)
            }

            if(!hashtag.isNullOrEmpty()) {
                hashtags.add(hashtag)
            }
        }

        return hashtags
    }
    companion object{
        val PARAM_USER_ID="UserId"
        val PARAM_USER_NAME="UserName"
        fun newIntent(context: Context, userId:String? , userName:String?) : Intent{
            val intent = Intent(context, TweetActivity::class.java)
            intent.putExtra(PARAM_USER_NAME,userName)
            intent.putExtra(PARAM_USER_ID,userId)
            return intent
        }
    }
}