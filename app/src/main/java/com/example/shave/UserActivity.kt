package com.example.shave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user2.*

class UserActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user2)
        verifyUserIsLoggedIn()
        fetchUser()

        logout_button_user.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        newcategory_button_user.setOnClickListener {
            val intent = Intent(this,CreateCategoryActivity::class.java)
            startActivity(intent)
        }
        categories_button_user.setOnClickListener {
            val intent = Intent(this,CategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        if(uid == ""){
            val intent = Intent(this,RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

    }
    private fun fetchUser() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                val user = p0.getValue(User::class.java) ?: User()
                message_text_touser.text = "Perfect day to shave, ${user.username}!"
                Picasso.get().load(user.profileImageUrl).into(item_info_photo)
                Log.d("UserActivity","image is loaded"+user.profileImageUrl)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }

}
