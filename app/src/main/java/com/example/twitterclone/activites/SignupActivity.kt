package com.example.twitterclone.activites

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.twitterclone.databinding.ActivitySignupBinding
import com.example.twitterclone.util.DATA_USERS
import com.example.twitterclone.util.User
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    lateinit var signupBinding: ActivitySignupBinding
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user:String? = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signupBinding=ActivitySignupBinding.inflate(layoutInflater)
        var view = signupBinding.root
        setContentView(view)
        setTextChangeListener(signupBinding.usernameET,signupBinding.usernameTIL)
        setTextChangeListener(signupBinding.emailET,signupBinding.emailTIL)
        setTextChangeListener(signupBinding.passwordET,signupBinding.passwordTIL)
        signupBinding.SignupProgressLayout.setOnTouchListener { v, event ->  true}

    }
    fun setTextChangeListener(et: EditText, til: TextInputLayout){
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled=false
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }
    fun onsignup(v: View){
        var proceed = true;
        if(signupBinding.usernameET.text.isNullOrEmpty()){
            signupBinding.usernameTIL.error="Username is required"
            signupBinding.usernameTIL.isErrorEnabled=true
            proceed=false
        }
        if(signupBinding.emailET.text.isNullOrEmpty()){
            signupBinding.emailTIL.error="Email is required"
            signupBinding.emailTIL.isErrorEnabled=true
            proceed=false

        }
        if(signupBinding.passwordET.text.isNullOrEmpty()){
            signupBinding.passwordTIL.error="Password is required"
            signupBinding.passwordTIL.isErrorEnabled=true
            proceed=false;
        }
        if(proceed){
            signupBinding.SignupProgressLayout.visibility=View.VISIBLE;
            firebaseAuth.createUserWithEmailAndPassword(signupBinding.emailET.text.toString(),signupBinding.passwordET.toString())
                .addOnCompleteListener {task->
                    if(!task.isSuccessful){
                        Toast.makeText(this@SignupActivity,"Signup Error: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT)
                            .show()
                    }else{
                        val email:String = signupBinding.emailET.text.toString()
                        val name:String = signupBinding.usernameET.text.toString()
                        val user = User(email,name,"", arrayListOf(), arrayListOf())
                        firebaseDB.collection(DATA_USERS).document(firebaseAuth.uid!!).set(user)
                    }
                    signupBinding.SignupProgressLayout.visibility=View.GONE
                }
                .addOnFailureListener { e->
                    e.printStackTrace()
                    signupBinding.SignupProgressLayout.visibility=View.GONE
                }
        }

    }
    fun gotologin(v:View){
        startActivity(LoginActivity.newIntent(this))
        finish()
    }
    companion object{
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener( firebaseAuthListener)
    }
}