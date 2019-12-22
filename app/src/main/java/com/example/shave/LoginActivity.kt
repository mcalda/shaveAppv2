package com.example.shave

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity :AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        button_login.setOnClickListener {
            val email = email_login.text.toString()
            val password = password_login.text.toString()
            if(email.isEmpty()) {
                Toast.makeText(this,"Please enter an e-mail address",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(password.isEmpty()) {
                Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }


            Log.d("LoginActivity","Attempt to login with email and password")
            auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("LoginActivity", "signInWithEmail:success")
                        val user = auth.currentUser
                        val intent = Intent(this,UserActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                    // ...
                }
        }

        to_register_text.setOnClickListener {
            finish()
        }
        activity_login_password_reset_text.setOnClickListener {
            val intent = Intent(this,PasswordResetActivity::class.java)
            startActivity(intent)
        }

    }
}