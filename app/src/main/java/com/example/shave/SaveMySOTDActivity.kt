package com.example.shave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_merge_categories.*
import kotlinx.android.synthetic.main.activity_save_my_sotd.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class SaveMySOTDActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_my_sotd)
        fetchCategorySpinners()

        save_mystod_button.setOnClickListener {
            saveMySOTD()
        }

    }

    private fun fetchCategorySpinners(){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val categoryNames = mutableListOf<String>()
                p0.children.forEach{
                    val category:Category? = it.getValue(Category::class.java)
                    if(category!= null){
                        categoryNames.add(category.name)
                    }
                }
                if(categoryNames.size>=3)
                    fetchCategoryNames(categoryNames)
                else{
                    Toast.makeText(applicationContext,"You need at least 3 different categories to create SOTD!",Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext,UserActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        })
    }
    private fun fetchCategoryNames(categoryNames:MutableList<String>){
        val options = MutableToArrayConverter(categoryNames)
        mysotd_category1_spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options)
        mysotd_category1_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fetchItemSpinner(1,mysotd_category1_spinner.selectedItem.toString())
            }
        }
        mysotd_category2_spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options)
        mysotd_category2_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fetchItemSpinner(2, mysotd_category2_spinner.selectedItem.toString())
            }
        }

        mysotd_category3_spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options)
        mysotd_category3_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                fetchItemSpinner(3,mysotd_category3_spinner.selectedItem.toString())
            }
        }
    }

    private fun fetchItemSpinner(categoryNumber:Int ,selectedCategory:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$selectedCategory/items")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val itemNames = mutableListOf<String>()
                p0.children.forEach {
                    val shavingItem:CreateItem? = it.getValue(CreateItem::class.java)
                    if(shavingItem!= null) {
                        itemNames.add(shavingItem.name)
                    }
                    fetchItemNames(categoryNumber,itemNames)
                }
            }
        })
    }

    private fun fetchItemNames(categoryNumber: Int, itemNames: MutableList<String>){
        val options = MutableToArrayConverter(itemNames)
        var spinner = mysotd_item1_spinner
        if(categoryNumber==2)
            spinner = mysotd_item2_spinner
        else if (categoryNumber == 3)
            spinner = mysotd_item3_spinner
        spinner.adapter =  ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,options)

    }



    private fun MutableToArrayConverter(categoryNames: MutableList<String>):List<String>{
        val result = ArrayList<String>()
        for (name in categoryNames){
            result.add(name)
        }
        return result
    }

    private fun saveMySOTD() {
        val currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        val formatted = currentTime.format(formatter)
        val category1 = mysotd_category1_spinner.selectedItem.toString()
        val category2 = mysotd_category2_spinner.selectedItem.toString()
        val category3 = mysotd_category3_spinner.selectedItem.toString()
        val item1 = mysotd_item1_spinner.selectedItem.toString()
        val item2 = mysotd_item2_spinner.selectedItem.toString()
        val item3 = mysotd_item3_spinner.selectedItem.toString()
        if((item1==item2 && category1==category2)||(item1==item3 && category1==category3)||(item2==item3 && category2 == category3)){
            Toast.makeText(this,"Items cannot be same! Please choose different ones.",Toast.LENGTH_SHORT).show()
            return
        }
        val newsotd = mySOTD(category1,item1,category2,item2,category3,item3,formatted)
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/mySOTD/$formatted")
        ref.setValue(newsotd)
        Toast.makeText(applicationContext,"mySOTD is succesfully saved!",Toast.LENGTH_SHORT).show()
        val intent = Intent(this,UserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

class mySOTD(val category1:String,val item1:String,val category2:String, val item2:String, val category3:String, val item3: String,val date:String) {
    constructor():this("","","","","","","")
}