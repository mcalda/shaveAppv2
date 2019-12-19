package com.example.shave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.shave.CategoryItemActivity.Companion.ITEM_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_shave_item_info.*

class ShaveItemInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shave_item_info)
        val itemPath = intent.getStringExtra(ITEM_KEY) ?: ""

        fetchItemInfo(itemPath)

        item_info_num_inc_button.setOnClickListener {
            var leftOver = item_info_numleft_textview.text.toString().toInt()
            leftOver++
            item_info_numleft_textview.text = leftOver.toString()


        }

        item_info_num_dec_button.setOnClickListener {
            var leftOver = item_info_numleft_textview.text.toString().toInt()
            leftOver--
            if(leftOver>= 0){
                item_info_numleft_textview.text = leftOver.toString()
            }
        }

        item_info_update_button.setOnClickListener {
            updateInfo(itemPath)
        }

        item_info_delete_button.setOnClickListener {
            deleteInfo(itemPath)
        }

    }

    private fun fetchItemInfo(path:String){
        if(path!= ""){
            val uid = FirebaseAuth.getInstance().uid ?: ""

            if(uid == ""){
                Toast.makeText(this,"An error happened!", Toast.LENGTH_SHORT).show()
                return
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$path")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    val item: CreateItem = p0.getValue(CreateItem::class.java) ?: CreateItem()
                    item_info_ratingbar.rating = item.rating
                    item_info_comments_textview.text = item.comments
                    item_info_numleft_textview.text = item.number
                    Picasso.get().load(item.itemPhotoUrl).into(item_info_photo)
                    supportActionBar?.title = item.name
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }
    }

    private fun updateInfo(path:String) {
        if(path!="") {
            val uid = FirebaseAuth.getInstance().uid ?: ""

            if(uid == ""){
                Toast.makeText(this,"An error happened!", Toast.LENGTH_SHORT).show()
                return
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$path")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    val item: CreateItem = p0.getValue(CreateItem::class.java) ?: CreateItem()
                    val newItem = CreateItem(item.name, item_info_numleft_textview.text.toString() ,item_info_ratingbar.rating,item.comments, item.itemPhotoUrl)
                    ref.setValue(newItem)
                    Toast.makeText(applicationContext,"Successfully updated!",Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }
    }

    private fun deleteInfo(path:String) {
        if (path == "") {
            Toast.makeText(this,"An error happened!", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().uid ?: ""

        if(uid == ""){
            Toast.makeText(this,"An error happened!", Toast.LENGTH_SHORT).show()
            return
        }


        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Do you really want to delete this item?")
        alertDialog.setIcon(R.mipmap.ic_launcher)

        alertDialog.setPositiveButton("YES"){dialog, _ ->
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$path")
            ref.setValue(null)
            Toast.makeText(this,"Successfully deleted!",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            dialog.dismiss()
        }

        alertDialog.setNegativeButton("NO"){dialog, _ ->

            dialog.dismiss()
        }

        alertDialog.show()
    }
}
