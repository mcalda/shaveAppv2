package com.example.shave

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)



        button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_register.setOnClickListener {
            Log.d("MainActivity","try to show login activity")
            //start login activity
            finish()

        }
        select_photo_button.setOnClickListener {
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
            item_info_photo.setImageBitmap(bitmap)
            select_photo_button.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = email_register.text.toString()
        val password = password_register.text.toString()
        val username = username_register.text.toString()

        if(username.isEmpty()){
            Toast.makeText(this,"Please enter a username!",Toast.LENGTH_SHORT).show()
            return
        }
        else if(email.isEmpty() || !email.isEmailValid()) {
            Toast.makeText(this,"Please enter a valid e-mail address!",Toast.LENGTH_SHORT).show()
            return
        }
        else if(password.isEmpty()) {
            Toast.makeText(this,"Please enter a password!",Toast.LENGTH_SHORT).show()
            return
        }
        else if(selectedPhotoUri == null){
            Toast.makeText(this,"Please select a photo!",Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("MainActivity","Email is: "+ email)
        Log.d("MainActivity","Password is: $password")



        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("MainActivity", "createUserWithEmail:success")
                    Toast.makeText(baseContext, "You registered successfully!",
                        Toast.LENGTH_SHORT).show()
                    finish()

                    uploadImageToFirebaseStorage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("MainActivity", "createUserWithEmail:failure", task.exception)
                    //updateUI(null)
                }

            }
            .addOnFailureListener{
                Toast.makeText(baseContext, "Registration is failed: ${it.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }
    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri!= null) {
            val filename= UUID.randomUUID().toString()
            val ref =FirebaseStorage.getInstance().getReference("/images/$filename")
            ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveUserToFiredatabase(it.toString())
                    }
                }
        }
    }

    private fun saveUserToFiredatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_register.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","user is succesfully added to firedatabase")
            }
    }
    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }


}

class User (val uid:String, val username:String, val profileImageUrl: String){
    constructor(): this("","","")
}
