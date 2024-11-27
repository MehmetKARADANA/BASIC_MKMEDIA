package com.mehmetkaradana.instagrambasic.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.mehmetkaradana.instagrambasic.R
import com.mehmetkaradana.instagrambasic.adapter.FeedRecyclerAdapter
import com.mehmetkaradana.instagrambasic.databinding.ActivityFeedBinding
import com.mehmetkaradana.instagrambasic.model.Post
import org.checkerframework.common.returnsreceiver.qual.This

class FeedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val postArrayList: ArrayList<Post> = ArrayList()
    private var feedAdapter: FeedRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //bu alttaki iki satır olmazsa toolbar çalışmıyor
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)
        auth = Firebase.auth
        db = Firebase.firestore
        //veya Firebase.getInstance()


        getDataFirestore()
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        feedAdapter = FeedRecyclerAdapter(postArrayList)
        binding.recyclerview.adapter = feedAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getDataFirestore() {
        //db.collection("Posts").orderBy()
        db.collection("Posts").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
                } else {
                    if (value != null) {
                        if (!value.isEmpty) {

                            //tekrar etme sorunu icin
                            postArrayList.clear()

                            val documents = value.documents

                            for (document in documents) {
                                //casting
                                val comment = document.get("comment") as String
                                val userEmail = document.get("userEmail") as String
                                val bitmap = document.get("bitmap") as String
                                val decodeBitmap = decodeBase64ToBitmap(bitmap)
                                //  println(comment)
                                val post = Post(userEmail, comment, decodeBitmap!!)
                                postArrayList.add(post)
                            }
                            feedAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_user, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_post) {
            val intent = Intent(this@FeedActivity, UploadActivity::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.user_logout) {
            auth.signOut()
            val intent = Intent(this@FeedActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}