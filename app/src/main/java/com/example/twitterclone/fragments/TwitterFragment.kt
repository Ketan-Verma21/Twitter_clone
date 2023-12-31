package com.example.twitterclone.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.listeners.HomeCallback
import com.example.twitterclone.listeners.TweetListener
import com.example.twitterclone.listeners.TwitterListenerImpl
import com.example.twitterclone.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

abstract class TwitterFragment:Fragment() {
    protected var currentUser: User?=null
    protected val firebaseDB = FirebaseFirestore.getInstance()
    protected val userId = FirebaseAuth.getInstance().currentUser?.uid
    protected var tweetsAdapter : TweetListAdapter?=null
    protected var listener: TwitterListenerImpl?=null
    protected var callback:HomeCallback?=null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is HomeCallback){
            callback=context
        }
        else{
            throw RuntimeException(context.toString()+"must implement Homecallback")
        }
    }
    fun setUser(user:User?){
        this.currentUser=user;
        listener?.user=user
    }
    abstract fun updateList();
    override fun onResume() {
        super.onResume()
        updateList()
    }

}