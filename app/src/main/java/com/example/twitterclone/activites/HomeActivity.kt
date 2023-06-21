package com.example.twitterclone.activites

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.twitterclone.R
import com.example.twitterclone.databinding.ActivityHomeBinding
import com.example.twitterclone.fragments.HomeFragment
import com.example.twitterclone.fragments.MyActivityFragment
import com.example.twitterclone.fragments.SearchFragment
import com.example.twitterclone.fragments.TwitterFragment
import com.example.twitterclone.listeners.HomeCallback
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.User
import com.example.twitterclone.util.loadUrl
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity(),HomeCallback {
    private var sectionsPagerAdapter:SectionPagerAdapter?=null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val homeFragment=HomeFragment()
    private val searchFragment = SearchFragment()
    private val myActivityFragment = MyActivityFragment()
    private val firebaseDB = FirebaseFirestore.getInstance()
    lateinit var homeBinding: ActivityHomeBinding
    lateinit var homeprogresslayout :LinearLayout
    lateinit var search:EditText
    lateinit var tabs : TabLayout
    lateinit var titleBar:TextView
    lateinit var searchBar : CardView
    lateinit var container:ViewPager
    lateinit var logo:ImageView
    private var userId=FirebaseAuth.getInstance().currentUser?.uid
    private var user: User? = null
    lateinit var fab:FloatingActionButton
    private var currentFragment:TwitterFragment = homeFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        tabs=findViewById(R.id.tabs)
        container=findViewById(R.id.container)
        titleBar=findViewById(R.id.titleBar)
        searchBar=findViewById(R.id.searchBar)
        search=findViewById(R.id.search)
        homeprogresslayout=findViewById(R.id.homeProgressLayout)
        logo=findViewById(R.id.logo)
        fab=findViewById(R.id.fab)
        sectionsPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        container.adapter = sectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        tabs.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0->{
                       titleBar.visibility=View.VISIBLE
                       titleBar.text="Home"
                       searchBar.visibility=View.GONE
                        currentFragment=homeFragment
                    }
                    1->{
                        titleBar.visibility=View.GONE
                        searchBar.visibility=View.VISIBLE
                        currentFragment=searchFragment
                    }
                    2->{
                        titleBar.visibility=View.VISIBLE
                        titleBar.text="My Activity"
                        searchBar.visibility=View.GONE
                        currentFragment=searchFragment

                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        logo.setOnClickListener {view->
            startActivity(ProfileActivity.newIntent(this))

        }
        fab.setOnClickListener {
            startActivity(TweetActivity.newIntent(this,userId,user?.username))
        }
        homeprogresslayout.setOnTouchListener { v, event ->  true}
        search.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE ||actionId == EditorInfo.IME_ACTION_SEARCH ){
                searchFragment.newHashtag(v?.text.toString())
            }
            true
        }
    }
    fun onLogout(v: View){
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    override fun onResume() {
        super.onResume()
        userId=FirebaseAuth.getInstance().currentUser?.uid
        if(userId==null){
            startActivity(LoginActivity.newIntent(this));
            finish()
        }else{


        populate()}
    }
    fun populate(){
        homeprogresslayout.visibility=View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userId!!).get()
            .addOnSuccessListener {
                homeprogresslayout.visibility=View.GONE
                user = it.toObject(User::class.java)
                user?.imageurl.let{itt->
                    logo.loadUrl(itt,R.drawable.logo)
                }
                updateFragmentUser()
            }
            .addOnFailureListener {e->
                e.printStackTrace()
                finish()
            }
    }
    fun updateFragmentUser(){
        homeFragment.setUser(user)
        myActivityFragment.setUser(user)
        searchFragment.setUser(user)
        currentFragment.updateList()
    }
    inner class SectionPagerAdapter(fm:FragmentManager):FragmentPagerAdapter(fm){
        override fun getCount() =3

        override fun getItem(position: Int): Fragment {
            return when(position){
                0->homeFragment
                1->searchFragment
                else -> myActivityFragment
            }
        }

    }
    companion object{
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }

    override fun onUserUpdated() {
        populate()
    }

    override fun onRefresh() {
        currentFragment.updateList()
    }
}


