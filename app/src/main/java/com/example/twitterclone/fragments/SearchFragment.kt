package com.example.twitterclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.twitterclone.R
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.databinding.FragmentSearchBinding
import com.example.twitterclone.listeners.TweetListener
import com.example.twitterclone.listeners.TwitterListenerImpl
import com.example.twitterclone.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : TwitterFragment() {
    lateinit var followHashtag:ImageView

    lateinit var tweetList:RecyclerView
    private var currentHashtag=""
    lateinit var swipeRefresh:SwipeRefreshLayout
    private var hashTagFollowed = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_search, container, false)
        followHashtag =view.findViewById(R.id.followHashtag)
        tweetList=view.findViewById(R.id.tweetList)
        swipeRefresh=view.findViewById(R.id.swipeRefresh)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener=TwitterListenerImpl(tweetList, currentUser,callback)
        tweetsAdapter = TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        tweetList.apply{
            layoutManager=LinearLayoutManager(context)
            adapter=tweetsAdapter
            addItemDecoration(DividerItemDecoration(context,DividerItemDecoration.VERTICAL))
        }
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing=false
            updateList()
        }
        followHashtag.setOnClickListener {
            followHashtag.isClickable=false
            val followed = currentUser?.followHashtags
            if(hashTagFollowed){
                followed?.remove(currentHashtag)
            }else{

                followed?.add(currentHashtag)

            }
            updateFollowDrawable()
            firebaseDB.collection(DATA_USERS).document(userId).update(DATA_USER_HASHTAGS,followed)
                .addOnSuccessListener {
                    followHashtag.isClickable=true;
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    followHashtag.isClickable=true;
                }

        }
    }


    fun newHashtag(term: String){
        currentHashtag=term
        followHashtag.visibility = View.VISIBLE

        updateList()

    }
    override fun updateList(){
        tweetList?.visibility=View.GONE
        firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_HASHTAGS,currentHashtag).get()
            .addOnSuccessListener {list->
                tweetList?.visibility=View.VISIBLE
                val tweets = arrayListOf<Tweet>()
                for(document in list.documents){
                    val tweet = document.toObject(Tweet::class.java)
                    tweet?.let{
                        tweets.add(it)
                    }
                }
                val sortedTweets=tweets.sortedWith(compareByDescending{it.timestamp})
                tweetsAdapter?.updateTweet(sortedTweets)

            }
            .addOnFailureListener { e->
                e.printStackTrace()
            }
        updateFollowDrawable()
    }
    fun updateFollowDrawable(){
        hashTagFollowed=currentUser?.followHashtags?.contains(currentHashtag)==true
        context?.let{
            if(hashTagFollowed){
                followHashtag.setImageDrawable(ContextCompat.getDrawable(it,R.drawable.follow))
            }
            else{
                followHashtag.setImageDrawable(ContextCompat.getDrawable(it,R.drawable.follow_inactive))
            }
        }
    }

}