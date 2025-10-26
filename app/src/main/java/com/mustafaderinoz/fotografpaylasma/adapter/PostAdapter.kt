package com.mustafaderinoz.fotografpaylasma.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mustafaderinoz.fotografpaylasma.databinding.RecylerRowBinding
import com.mustafaderinoz.fotografpaylasma.model.Post
import android.util.Base64 // Bu import zaten mevcuttu.


class PostAdapter(private val postList:ArrayList<Post>):RecyclerView.Adapter<PostAdapter.PostHolder>(){

    class PostHolder(val binding:RecylerRowBinding):RecyclerView.ViewHolder(binding.root){
        // ViewHolder içine ek bir şey eklemenize gerek yok.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecylerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = postList[position]

        // 1. Veri Ataması
        holder.binding.recylerEmailText.text = post.email
        holder.binding.recylerCommentText.text = post.comment

        // 2. Base64 Dönüşümü ve Hata Yönetimi
        val base64String = post.base64

        if (!base64String.isNullOrEmpty()) {
            try {

                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                holder.binding.recylerImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // Base64 çözümleme hatasını konsola yazdır.
                e.printStackTrace()
                // Opsiyonel: Hata durumunda boş bir görsel atayarak önceki görsel kalıntılarını temizleyebilirsiniz.
                // holder.binding.recylerImageView.setImageBitmap(null)
            }
        } else {
            // base64 verisi yoksa resmi temizle
            holder.binding.recylerImageView.setImageBitmap(null)
        }
    }
}