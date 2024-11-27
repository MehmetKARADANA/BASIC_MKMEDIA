package com.mehmetkaradana.instagrambasic.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mehmetkaradana.instagrambasic.R
import com.mehmetkaradana.instagrambasic.databinding.ActivityUploadBinding
import org.checkerframework.common.returnsreceiver.qual.This
import java.io.ByteArrayOutputStream
import java.io.IOException

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

        auth = Firebase.auth
        db = Firebase.firestore


    }

    fun upload(view: View){
        //firebase storage modülünü(?) kullanmadığım için bitmap olarak firestorea kaydetmeyi deneyeceğim
        if(selectedPicture != null && selectedBitmap != null){

            val postmap = hashMapOf<String,Any>()
            postmap.put("userEmail",auth.currentUser!!.email.toString())
            postmap.put("comment",binding.txtComment.text.toString())
            postmap.put("date",Timestamp.now())
            postmap.put("bitmap",encodeBitmapToBase64(selectedBitmap!!))

            db.collection("Posts").add(postmap).addOnCompleteListener{
                if(it.isComplete && it.isSuccessful){
                    finish()
                }
            }.addOnFailureListener{
                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }

    }

    fun selectImage(view: View){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",
                        View.OnClickListener {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun registerLauncher(){
        //Arc bir aktivite başlatıp souç almak için sözleşmeler sunar farklı işlemler için farklı sözleşmler saplar
        //burada Startactivityresul aşağıda requestpermission
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
               if(intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                   try {
                       if(Build.VERSION.SDK_INT >=28){
                           val source = ImageDecoder.createSource(
                               this@UploadActivity.contentResolver,selectedPicture!!
                           )
                           selectedBitmap = ImageDecoder.decodeBitmap(source)
                           val smallBitmap = makeSmallerBitmap(selectedBitmap!!,500)
                           binding.imageView.setImageBitmap(smallBitmap)
                       }else {
                           selectedBitmap = MediaStore.Images.Media.getBitmap(this@UploadActivity.contentResolver,selectedPicture)
                           val smallBitmap = makeSmallerBitmap(selectedBitmap!!,500)
                           binding.imageView.setImageBitmap(smallBitmap)
                       }
                   }catch (e :IOException){
                       e.printStackTrace()
                   }
            }

        }}
            //izin isiyor ve sonucu döndürüyor activity result
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            if(result){
                //permission granted
                val intentToGallery =Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permissison denied
                Toast.makeText(this@UploadActivity,"Permission needed !",Toast.LENGTH_LONG).show()
            }

        }

    }
    fun makeSmallerBitmap(image: Bitmap,maximumSize: Int): Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio > 1) {
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else {
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}