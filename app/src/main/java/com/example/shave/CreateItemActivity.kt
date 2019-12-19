package com.example.shave

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shave.CategoryItemActivity.Companion.ITEM_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_item.*
import java.util.*

class CreateItemActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_item)

        val category_name = intent.getStringExtra(ITEM_KEY)
        Log.d("CreateItemActivity","category name: $category_name")

        photo_button_item.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        createitems_button.setOnClickListener {
            uploadImageThenSaveCategory(category_name?:"")
            finish()
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==0 && resultCode == Activity.RESULT_OK&& data!= null) {
            Log.d("CreateItemActivity","photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectphoto_item.setImageBitmap(bitmap)
            photo_button_item.alpha = 0f

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImageThenSaveCategory(category_name:String) {
        if(selectedPhotoUri!= null){
            val filename= UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveItemToDatabase(it.toString(),category_name )
                    }
                }
        }
    }


    private fun saveItemToDatabase(imageUrl:String, categoryName:String){
        if(categoryName!=""){
            val name = item_create_name.text.toString()
            val rating = create_item_rating_bar.rating
            val numOfItems = createitem_numberofitems_text.text.toString()
            val comments = item_create_comments.text.toString()
            if (name.isEmpty()){
                Toast.makeText(this,"Please enter a name!", Toast.LENGTH_SHORT).show()
                return
            }
            else if(rating == 0f){
                Toast.makeText(this,"Please rate the item!", Toast.LENGTH_SHORT).show()
                return
            }
            else if(numOfItems.isEmpty()){
                Toast.makeText(this,"Please enter number of items! If it's not disposable, enter zero!", Toast.LENGTH_LONG).show()
                return
            }
            else if(comments.isEmpty()){
                Toast.makeText(this,"Please enter your comments!", Toast.LENGTH_SHORT).show()
                return
            }
            val uid = FirebaseAuth.getInstance().uid ?: ""
            val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$categoryName/items/$name")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    if(!p0.exists()){
                        val item = CreateItem(name,numOfItems, rating, comments, imageUrl)
                        ref.setValue(item)
                            .addOnSuccessListener {
                                Log.d("CreateItemActivity","item is succesfully added to firedatabase")
                                Toast.makeText(applicationContext,"Item is successfully added!",Toast.LENGTH_SHORT).show()

                            }

                    }
                    else {
                        Toast.makeText(applicationContext,"The item $name already exists!",Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        }

    }

}

class CreateItem(val name:String, val number:String, val rating: Float, val comments: String, val itemPhotoUrl:String){
    constructor():this("","0",0f,"","")
}