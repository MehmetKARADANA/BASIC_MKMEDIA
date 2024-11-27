package com.mehmetkaradana.instagrambasic.model

import android.graphics.Bitmap
import android.provider.ContactsContract.CommonDataKinds.Email

data class Post(val email: String,val comment : String,val bitmap : Bitmap)