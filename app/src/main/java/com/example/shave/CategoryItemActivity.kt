package com.example.shave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_category_item.*
import kotlinx.android.synthetic.main.activity_category_item_row.view.*

class CategoryItemActivity() : AppCompatActivity() {
    companion object{
        val ITEM_KEY = "itemkey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_item)

        val categoryName = intent.getStringExtra(CategoryActivity.CATEGORY_KEY)
        supportActionBar?.title = categoryName


        fetchItems(categoryName?:"")


        button_category_item.setOnClickListener {
            val intent = Intent(this,CreateItemActivity::class.java)
            intent.putExtra(ITEM_KEY,categoryName)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchItems(categoryName:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if(uid == ""){
            Toast.makeText(this,"An error happened!", Toast.LENGTH_SHORT).show()
            return
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/categories/$categoryName/items")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                adapter.spanCount = 3
                recyclerview_category_item.apply {
                    layoutManager = GridLayoutManager(this@CategoryItemActivity,adapter.spanCount)
                }
                p0.children.forEach{
                    val item: CreateItem? = it.getValue(CreateItem::class.java)
                    if(item!=null) {
                        adapter.add(ShavingItem(item))
                    }
                }
                recyclerview_category_item.adapter = adapter
                //adapter.setonclicklistener yapıp item bilgi sayfasına yönlendir
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}

class ShavingItem(val item:CreateItem):Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.item_name_row.text = item.name
        Picasso.get().load(item.itemPhotoUrl).into(viewHolder.itemView.item_photo_row)
    }

    override fun getLayout(): Int {
        return R.layout.activity_category_item_row
    }
}