package com.example.twitterclone.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.twitterclone.R
import com.example.twitterclone.adapters.TweetListAdapter
import com.example.twitterclone.listeners.TwitterListenerImpl
import com.example.twitterclone.util.DATA_TWEETS
import com.example.twitterclone.util.DATA_TWEET_USER_IDS
import com.example.twitterclone.util.Tweet

class MyActivityFragment : TwitterFragment() {
    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var tweetList: RecyclerView
    override fun updateList() {
        tweetList?.visibility=View.GONE
        val tweets = arrayListOf<Tweet>()
        firebaseDB.collection(DATA_TWEETS).whereArrayContains(DATA_TWEET_USER_IDS,userId!!).get()
            .addOnSuccessListener {list->
                for(document in list.documents){
                    val tweet=document.toObject(Tweet::class.java)
                    tweet?.let { tweets.add(tweet) }

                }
                val sortedList=tweets.sortedWith(compareByDescending { it.timestamp })
                tweetsAdapter?.updateTweet(sortedList)
                tweetList?.visibility=View.VISIBLE

            }
            .addOnFailureListener {
                it.printStackTrace()
                tweetList.visibility=View.VISIBLE
            }



    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_my_activity, container, false)
        swipeRefresh=view.findViewById(R.id.swipeRefresh)
        tweetList=view.findViewById(R.id.tweetList)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener= TwitterListenerImpl(tweetList,currentUser,callback)
        tweetsAdapter= TweetListAdapter(userId!!, arrayListOf())
        tweetsAdapter?.setListener(listener)
        tweetList?.apply {
            layoutManager= LinearLayoutManager(context)
            adapter=tweetsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        }
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing=false
            updateList()
        }
    }

}