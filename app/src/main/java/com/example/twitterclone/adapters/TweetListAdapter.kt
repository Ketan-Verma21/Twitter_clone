package com.example.twitterclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.twitterclone.R
import com.example.twitterclone.listeners.TweetListener
import com.example.twitterclone.util.Tweet
import com.example.twitterclone.util.getDate
import com.example.twitterclone.util.loadUrl

class TweetListAdapter(val userId:String , val tweets:ArrayList<Tweet>):RecyclerView.Adapter<TweetListAdapter.TweetViewHolder>() {
    private var listener:TweetListener? = null;
    fun setListener(listener: TweetListener?){
        this.listener=listener;
    }
    fun updateTweet(newTweets:List<Tweet>){
        tweets.clear()
        tweets.addAll(newTweets)
        notifyDataSetChanged()
    }
    class TweetViewHolder(v: View) : RecyclerView.ViewHolder(v){
        private val layout = v.findViewById<ViewGroup>(R.id.tweetLayout)
        private val username = v.findViewById<TextView>(R.id.tweetUsername)
        private val text = v.findViewById<TextView>(R.id.tweetText)
        private val date = v.findViewById<TextView>(R.id.tweetDate)
        private val likeCount = v.findViewById<TextView>(R.id.tweetLikeCount)
        private val reTweetCount = v.findViewById<TextView>(R.id.tweetRetweetCount)
        private val image = v.findViewById<ImageView>(R.id.tweetImage)
        private val like = v.findViewById<ImageView>(R.id.tweetLike)
        private val retweet = v.findViewById<ImageView>(R.id.tweetRetweet)


        fun bind(userId: String , tweet: Tweet , listener: TweetListener?){
            username.text=tweet.username
            text.text = tweet.text
            if(tweet.imageurl.isNullOrEmpty()){
                image.visibility=View.GONE
            }else{
                image.visibility=View.VISIBLE
                image.loadUrl(tweet.imageurl)
            }
            date.text = getDate(tweet.timestamp)
            likeCount.text = tweet.likes?.size.toString()
            reTweetCount.text = tweet.userIds?.size?.minus(1).toString()
            layout.setOnClickListener { listener?.onLayoutClick(tweet) }
            like.setOnClickListener { listener?.onLike(tweet) }
            retweet.setOnClickListener { listener?.onRetweet(tweet) }
            if(tweet.likes?.contains(userId)==true){
                like.setImageDrawable(ContextCompat.getDrawable(like.context,R.drawable.like))
            }else{
                like.setImageDrawable(ContextCompat.getDrawable(like.context,R.drawable.like_inactive))
            }
            if(tweet.userIds?.get(0).equals(userId)){
                retweet.setImageDrawable(ContextCompat.getDrawable(like.context,R.drawable.original))
                retweet.isClickable=false
            }else if (tweet.userIds?.contains(userId)==true){
                retweet.setImageDrawable(ContextCompat.getDrawable(like.context,R.drawable.retweet))
            }else{
                retweet.setImageDrawable(ContextCompat.getDrawable(like.context,R.drawable.retweet_inactive))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= TweetViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.item_tweet,parent,false)

            )

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        holder.bind(userId,tweets[position],listener)
    }

    override fun getItemCount() = tweets.size
}