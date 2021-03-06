package com.example.shave

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_category.*
import java.util.*

class CreateCategoryActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)


        create_activitiy_selectphoto_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        button_create_category.setOnClickListener {
            uploadImageThenSaveCategory()
        }


    }



    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==0 && resultCode == Activity.RESULT_OK&& data!= null) {
            Log.d("RegisterActivity","photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            create_activity_selectphoto_view.setImageBitmap(bitmap)
            create_activitiy_selectphoto_button.alpha = 0f

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun uploadImageThenSaveCategory() {
        if(selectedPhotoUri!= null){
            val name = create_category_name.text.toString()
            val description = create_category_description.text.toString()
            if (name.isEmpty()){
                Toast.makeText(this,"Please enter a name!", Toast.LENGTH_SHORT).show()
                return
            }


            if(description.isEmpty()){
                Toast.makeText(this,"Please enter a description!",Toast.LENGTH_SHORT).show()
                return
            }
            val filename= UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveCategory(it.toString())
                    }
                }
        }
        else{
            Toast.makeText(this,"Please select a photo!",Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveCategory(categoryImageUrl:String){
        val name = create_category_name.text.toString()
        val description = create_category_description.text.toString()
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$name")

        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if(!p0.exists()){
                    val category = Category(name,description, categoryImageUrl)
                    ref.setValue(category)
                        .addOnSuccessListener {
                            Log.d("CreateCategoryActivity","category is succesfully added to firedatabase")
                            Toast.makeText(applicationContext,"Category is successfully added!",Toast.LENGTH_SHORT).show()
                        }

                }
                else {
                    Toast.makeText(applicationContext,"The category $name already exists!",Toast.LENGTH_SHORT).show()
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


        finish()
    }


}


class Category(val name:String, val description:String, val categoryImageUrl: String) {
    constructor():this("","","")
}