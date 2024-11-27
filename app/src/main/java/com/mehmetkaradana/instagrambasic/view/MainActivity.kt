 package com.mehmetkaradana.instagrambasic.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.mehmetkaradana.instagrambasic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val currentUser = auth.currentUser

        if(currentUser != null){
            // hatırlama
            val intent = Intent(this@MainActivity,FeedActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun login(view: View){
        val email = binding.txtMail.text.toString()
        val password = binding.txtPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val intent = Intent(this@MainActivity,FeedActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(this@MainActivity, "Enter Password and Email !", Toast.LENGTH_SHORT).show()
        }
    }

    fun register(view: View){
        val email = binding.txtMail.text.toString()
        val password = binding.txtPassword.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                //succes
               val intent = Intent(this@MainActivity,FeedActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this@MainActivity, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(this@MainActivity, "Enter Email and Password !", Toast.LENGTH_LONG).show()
        }
    }
}