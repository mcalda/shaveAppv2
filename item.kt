package com.example.shave.itemLayout

import android.os.Bundle
//import android.support.v4.app.AppCompatActivity
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.shave.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.Item
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.android.synthetic.main.list_item_row.view.*

class item : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

//        val categoryName = intent.getStringExtra(CategoryActivity.CATEGORY_KEY)
//        supportActionBar?.title = categoryName

        fetchItems()
    }

    private fun fetchItems() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach {
                    Log.d("Items", it.toString())
                    val item = it.getValue(ItemList::class.java)
                    if (item != null) {
                        adapter.add(itemList(item))

                    }
                }

                itemLayout.adapter  = adapter

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class ItemList(val uid: String, val username: String, val profileImageUrl: String) {
    constructor() : this("", "", "")
}

class itemList(val item: ItemList): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.item_name.text = item.username


        Picasso.get().load(item.profileImageUrl).into(viewHolder.itemView.item_image)

    }

    override fun getLayout(): Int {
        return R.layout.list_item_row
    }
}
