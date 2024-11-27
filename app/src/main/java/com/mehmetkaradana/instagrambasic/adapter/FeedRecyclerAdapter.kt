package com.mehmetkaradana.instagrambasic.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.mehmetkaradana.instagrambasic.databinding.RecyclerRowBinding
import com.mehmetkaradana.instagrambasic.model.Post
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.widget.Toast

class FeedRecyclerAdapter(private val postList: ArrayList<Post>) : RecyclerView.Adapter<FeedRecyclerAdapter.PostHolder>(){

    class PostHolder(val binding: RecyclerRowBinding):RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.recyclerEmailText.text = postList.get(position).email
        holder.binding.recyclerCommentText.text = postList.get(position).comment
        holder.binding.recyclerImageView.setImageBitmap(postList.get(position).bitmap)

    }


}