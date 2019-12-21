package com.example.shave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_category_list.*
import kotlinx.android.synthetic.main.activity_category_list_row.view.*

class CategoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)


        fetchCategories()

    }
    companion object {
        val CATEGORY_KEY = "key"
    }
    private fun fetchCategories(){
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if(uid == ""){
            Toast.makeText(this,"An error happened!",Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                val categoryNames = mutableListOf<String>()
                val adapter = GroupAdapter<GroupieViewHolder>()
                p0.children.forEach{//it iterates through each children in categories folder of specified user
                    val category:Category? = it.getValue(Category::class.java)
                    if(category!=null){
                        adapter.add(CategoryItem(category))
                        categoryNames.add(category.name)
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val categoryItem = item as CategoryItem
                    val intent = Intent(view.context,CategoryItemActivity()::class.java)
                    intent.putExtra(CATEGORY_KEY,categoryItem.category.name)
                    startActivity(intent)

                }
                category_list_merge_button.setOnClickListener {
                    val intent = Intent(this@CategoryActivity,MergeCategoriesActivity()::class.java)
                    intent.putExtra(CATEGORY_KEY,categoryNames.joinToString())
                    startActivity(intent)
                }

                recyclerview_category.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }



}

class CategoryItem(val category:Category):Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.categoryrow_name.text = category.name
        Log.d("CategoryActivity",category.name)
        viewHolder.itemView.categoryrow_description.text = category.description
        Picasso.get().load(category.categoryImageUrl).into(viewHolder.itemView.categoryrow_image)
    }

    override fun getLayout(): Int {
        return R.layout.activity_category_list_row
    }
}
