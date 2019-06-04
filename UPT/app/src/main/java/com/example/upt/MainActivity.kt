package com.example.upt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "MyActivity"
@SuppressLint("StaticFieldLeak")
val db = FirebaseFirestore.getInstance()

class MainActivity : AppCompatActivity() {

    private var myRequestCode: Int = 1111
    private lateinit var providers : List<AuthUI.IdpConfig>
    private var numberInQueue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_sing_out.visibility = View.INVISIBLE
        btn_queue.visibility = View.INVISIBLE

        providers = Arrays.asList<AuthUI.IdpConfig>(
            AuthUI.IdpConfig.EmailBuilder().build(),  //Email builder
            AuthUI.IdpConfig.FacebookBuilder().build(),  //Facebook builder
            AuthUI.IdpConfig.GoogleBuilder().build(),    //Google builder
            AuthUI.IdpConfig.PhoneBuilder().build()    //Phone builder
        )

        btn_singIn.setOnClickListener{
            showSingInOptions()
            btn_singIn.visibility = View.INVISIBLE
            btn_sing_out.visibility = View.VISIBLE
            btn_queue.visibility = View.VISIBLE
        }

        btn_sing_out.setOnClickListener{
             AuthUI.getInstance().signOut(this@MainActivity)
                 .addOnCompleteListener{
                     btn_sing_out.isEnabled = false
                     btn_singIn.visibility = View.VISIBLE
                     showSingInOptions()
                 }
                 .addOnFailureListener{
                    e-> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                 }
        }
        btn_queue.setOnClickListener {

            updateNumberInQueue(numberInQueue.toInt())
            addUserData(numberInQueue)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == myRequestCode)
        {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK)
            {
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, ""+user!!.email, Toast.LENGTH_SHORT).show()

                btn_sing_out.isEnabled = true
                btn_queue.isEnabled = true

                db.collection("Numbers")
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                            numberInQueue = document.get("currentNumber").toString()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents.", exception)
                    }
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
            .setLogo(R.drawable.logo)
            .build(), myRequestCode)
    }

    private fun updateNumberInQueue(numberInQueue: Int){
        var localNumber = numberInQueue
        localNumber += 1

        val data = HashMap<String, Any>()
        data["currentNumber"] = localNumber

        db.collection("Numbers").document("CurrentNumbers")
            .set(data, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot: updateNumberInQueue successfully written!") }

    }

    private fun addUserData(numberInQueue: String){
        val userData = FirebaseAuth.getInstance().currentUser
        userData?.let {

            val user = HashMap<String, Any>()
            user["name"] = userData.displayName.toString()
            user["email"] = userData.email.toString()
            user["photoUrl"] = userData.photoUrl.toString()
            user["emailVerified"] = userData.isEmailVerified
            user["uid"] = userData.uid
            user["numberInQueue"] = numberInQueue

            db.collection("users").document(userData.uid)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
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
