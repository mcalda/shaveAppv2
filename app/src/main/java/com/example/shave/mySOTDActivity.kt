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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_my_sotd.*
import kotlinx.android.synthetic.main.activity_my_sotd_row.view.*

class mySOTDActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_sotd)
        fetchSOTDs()

        mysotd_add_new_button.setOnClickListener {
            val intent = Intent(this,SaveMySOTDActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchSOTDs() {
        val uid = FirebaseAuth.getInstance().uid ?: ""

        if(uid == ""){
            Toast.makeText(this,"An error happened!", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid/mySOTD/")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                p0.children.forEach {
                    val mySotdItem = it.getValue(mySOTD::class.java)
                    if(mySotdItem!=null) {
                        Log.d("mySOTDActivity","Do I work? ${mySotdItem.category1}")
                        adapter.add(MySotdItem(mySotdItem))
                    }
                }
                adapter.setOnItemClickListener{item, view ->
                    val mysotdItem = item as MySotdItem
                    val message: String = "MySOTD of ${mysotdItem.mysotd.date} are ${mysotdItem.mysotd.item1}, ${mysotdItem.mysotd.item2} and ${mysotdItem.mysotd.item3}! Come and use shave app!"
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message)
                    shareIntent.type= "text/plain"

                    startActivity(Intent.createChooser(shareIntent,"Share with: "))

                }
                recyclerview_mysotd.adapter = adapter
            }
        })
    }
}

class MySotdItem(val mysotd:mySOTD): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.my_sotd_row_date.text = mysotd.date
        Log.d("mySOTDActivity",mysotd.date)
        viewHolder.itemView.mysotd_row_item1.text = "-Category: "+mysotd.category1 +"    Item: " +mysotd.item1
        viewHolder.itemView.mysotd_row_item2.text = "-Category: "+mysotd.category2 +"    Item: " +mysotd.item2
        viewHolder.itemView.mysotd_row_item3.text = "-Category: "+mysotd.category3 +"    Item: " +mysotd.item3
    }

    override fun getLayout(): Int {
        return R.layout.activity_my_sotd_row
    }
}
