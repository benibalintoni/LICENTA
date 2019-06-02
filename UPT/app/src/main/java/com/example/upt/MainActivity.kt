package com.example.upt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "MyActivity"
@SuppressLint("StaticFieldLeak")
val db = FirebaseFirestore.getInstance()

class MainActivity : AppCompatActivity() {

    private var MY_REQUEST_CODE: Int = 1111
    lateinit var providers : List<AuthUI.IdpConfig>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        providers = Arrays.asList<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.EmailBuilder().build(),  //Email builder
            AuthUI.IdpConfig.FacebookBuilder().build(),  //Facebook builder
            AuthUI.IdpConfig.GoogleBuilder().build(),    //Google builder
            AuthUI.IdpConfig.PhoneBuilder().build()    //Phone builder
        )

        showSingInOptions()

        btn_sing_out.setOnClickListener{
             AuthUI.getInstance().signOut(this@MainActivity)
                 .addOnCompleteListener{
                     btn_sing_out.isEnabled = false
                     showSingInOptions()
                 }
                 .addOnFailureListener{
                    e-> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                 }
        }

//        add()
//        addMore()
//        read()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == MY_REQUEST_CODE)
        {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK)
            {
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, ""+user!!.email, Toast.LENGTH_SHORT).show()

                btn_sing_out.isEnabled = true
            }
            else
            {
                if (response != null) {
                    Toast.makeText(this, ""+response.error!!.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSingInOptions() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.MyTheme)
            .build(), MY_REQUEST_CODE)
    }

    private fun add(){
        // Create a new user with a first and last name
        val user = HashMap<String, Any>()
        user["first"] = "Ada"
        user["last"] = "Lovelace"
        user["born"] = 1815

        db.collection("users").document("1")
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
    private fun addMore(){
        // Create a new user with a first, middle, and last name
        val user = HashMap<String, Any>()
        user["first"] = "Alan"
        user["middle"] = "Mathison"
        user["last"] = "Turing"
        user["born"] = 1912

        db.collection("users").document("2")
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
    @SuppressLint("SetTextI18n")
    private fun read(){

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

}
