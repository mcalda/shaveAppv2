package com.example.shave

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_category_item.*

class CategoryItemActivity() : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_item)

        val categoryName = intent.getStringExtra(CategoryActivity.CATEGORY_KEY)
        supportActionBar?.title = categoryName

        val adapter = GroupAdapter<GroupieViewHolder>()
        adapter.spanCount = 3
        adapter.add(ShavingItem())
        adapter.add(ShavingItem())
        adapter.add(ShavingItem())

        adapter.add(ShavingItem())
        adapter.add(ShavingItem())
        adapter.add(ShavingItem())

        recyclerview_category_item.apply {
            layoutManager = GridLayoutManager(this@CategoryItemActivity,adapter.spanCount)
        }
        recyclerview_category_item.adapter = adapter
    }
}

class ShavingItem():Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

    }

    override fun getLayout(): Int {
        return R.layout.activity_category_item_row
    }
}