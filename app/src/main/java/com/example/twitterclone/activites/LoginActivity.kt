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
import com.example.twitterclone.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user:String? = firebaseAuth.currentUser?.uid
        user?.let {
            startActivity(HomeActivity.newIntent(this))
            finish()
        }
    }
    lateinit var loginBinding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding=ActivityLoginBinding.inflate(layoutInflater)
        var view = loginBinding.root
        setContentView(view)
        setTextChangeListener(loginBinding.emailET,loginBinding.emailTIL)
        setTextChangeListener(loginBinding.passwordET,loginBinding.passwordTIL)
        loginBinding.loginProgressLayout.setOnTouchListener { v, event ->  true}
    }
    fun onLogin(v: View){
        var proceed = true;
        if(loginBinding.emailET.text.isNullOrEmpty()){
            loginBinding.emailTIL.error="Email is required"
            loginBinding.emailTIL.isErrorEnabled=true
            proceed=false

        }
        if(loginBinding.passwordET.text.isNullOrEmpty()){
            loginBinding.passwordTIL.error="Password is required"
            loginBinding.passwordTIL.isErrorEnabled=true
            proceed=false;
        }
        if(proceed){
            loginBinding.loginProgressLayout.visibility=View.VISIBLE;
            firebaseAuth.signInWithEmailAndPassword(loginBinding.emailET.text.toString(),loginBinding.passwordET.text.toString())
                .addOnCompleteListener {task->
                    if(!task.isSuccessful){
                        loginBinding.loginProgressLayout.visibility=View.GONE
                        Toast.makeText(this@LoginActivity,"Login Error: ${task.exception?.localizedMessage}",Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener { e->
                    e.printStackTrace()
                    loginBinding.loginProgressLayout.visibility=View.GONE
                }
        }
    }
    fun setTextChangeListener(et:EditText , til:TextInputLayout){
        et.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til.isErrorEnabled=false
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }
    fun goToSignup(v:View){
        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener( firebaseAuthListener)
    }
    companion object{
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }
}