package com.example.twitterclone.listeners

import com.example.twitterclone.util.Tweet

interface TweetListener {
    fun onLayoutClick(tweet: Tweet?)
    fun onLike(tweet: Tweet?)
    fun onRetweet(tweet: Tweet?)
}