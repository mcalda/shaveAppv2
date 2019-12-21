package com.example.shave

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.example.shave.CategoryActivity.Companion.CATEGORY_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_merge_categories.*
import java.util.*

class MergeCategoriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_categories)
        val categoryNames = intent.getStringExtra(CATEGORY_KEY)

        val categoryNamesArray = categoryNames!!.split(", ").toTypedArray()
        fetchSpinners(categoryNamesArray)

        merge_activity_button.setOnClickListener {
            Log.d("MergeCategoryActivity",merge_activity_first_spinner.selectedItem.toString())
            performMergingAfterUpdatingPhoto()

        }
        merge_activitiy_selectphoto_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode == Activity.RESULT_OK&& data!= null) {
            Log.d("RegisterActivity","photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            merge_activity_selectphoto_view.setImageBitmap(bitmap)
            merge_activitiy_selectphoto_button.alpha = 0f
        }
    }
    private fun fetchSpinners(options:Array<String>) {


        merge_activity_first_spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options)
        merge_activity_second_spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options)

    }

    private fun performMergingAfterUpdatingPhoto(){
        val category1 =  merge_activity_first_spinner.selectedItem.toString()
        val category2 = merge_activity_second_spinner.selectedItem.toString()

        if(category1 == category2){
            Toast.makeText(this,"Please select different categories!",Toast.LENGTH_SHORT).show()
            return
        }
        if(selectedPhotoUri!= null){
            val filename= UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        performMerging(it.toString())
                    }
                }
        }
        else {
            performMerging("")
        }

    }

    private fun performMerging(value:String){
        val photoUrl = value

        val name = merge_category_name.text.toString()
        val description = merge_category_description.text.toString()
        if (name.isEmpty()){
            Toast.makeText(this,"Please enter a name!", Toast.LENGTH_SHORT).show()
            return
        }

        if(description.isEmpty()){
            Toast.makeText(this,"Please enter a description!",Toast.LENGTH_SHORT).show()
            return
        }
        val uid = FirebaseAuth.getInstance().uid ?: ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$name")

        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) {
                    val category = Category(name, description, photoUrl)
                    ref.setValue(category)
                    addFirstCategoryItems(uid,name)
                }
            }
        })

    }

    private fun addFirstCategoryItems(uid:String, name:String){
        var ref1= FirebaseDatabase.getInstance().getReference("/users/$uid/categories/${merge_activity_first_spinner.selectedItem.toString()}/items")
        var ref3 = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$name/items")
        ref1.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach{
                    val item:CreateItem? = it.getValue(CreateItem::class.java)
                    ref3 = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$name/items/${item!!.name}")
                    ref3.setValue(item)
                }
                ref1= FirebaseDatabase.getInstance().getReference("/users/$uid/categories/${merge_activity_first_spinner.selectedItem.toString()}")
                ref1.setValue(null)
                addSecondCategoryItems(uid,name)
            }

        })
    }

    private fun addSecondCategoryItems(uid:String, name:String){
        var ref3 = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$name/items")
        var ref2= FirebaseDatabase.getInstance().getReference("/users/$uid/categories/${merge_activity_second_spinner.selectedItem.toString()}/items")
        ref2.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {


                p0.children.forEach{
                    val item:CreateItem? = it.getValue(CreateItem::class.java)
                    ref3 = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$name/items/${item!!.name}")
                    ref3.setValue(item)
                }
                ref2= FirebaseDatabase.getInstance().getReference("/users/$uid/categories/${merge_activity_second_spinner.selectedItem.toString()}")
                ref2.setValue(null)

            }

        })

        Toast.makeText(this,"Categories are succesfully merged!",Toast.LENGTH_SHORT).show()
        val intent = Intent(this,UserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

}



