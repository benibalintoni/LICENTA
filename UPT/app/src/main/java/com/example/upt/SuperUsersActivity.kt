package com.example.upt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_super_users.*

private const val TAG = "SuperUserActivity"

class SuperUsersActivity : AppCompatActivity() {

    private var numberInLab1 : Int = 0
    private var numberInLab2 : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_super_users)

        btn_delete1.setOnClickListener {
            var userUid = ""
            var delete = false

            db.collection("Numbers").get().addOnSuccessListener { resultNumbers ->
                for (documentNumbers in resultNumbers) {
                    numberInLab1 = documentNumbers.get("numberInLab1").toString().toInt()
                }
                db.collection("users")
                    .whereEqualTo("lineNumber", 1)
                    .whereEqualTo("numberInQueue", numberInLab1)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            userUid = document.get("uid").toString()
                            if(document.exists()) delete = true
                        }
                        if(delete) {
                            db.collection("users").document(userUid)
                                .delete()
                                .addOnSuccessListener {
                                    db.collection("Numbers").document("CurrentNumbers")
                                        .update("numberInLab1", numberInLab1 + 1)
                                }
                        }
                    }
            }
        }

        btn_delete2.setOnClickListener {
            var userUid = ""
            var delete = false

            db.collection("Numbers").get().addOnSuccessListener { resultNumbers ->
                for (documentNumbers in resultNumbers) {
                    numberInLab2 = documentNumbers.get("numberInLab2").toString().toInt()
                }
                db.collection("users")
                    .whereEqualTo("lineNumber", 2)
                    .whereEqualTo("numberInQueue", numberInLab2)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            userUid = document.get("uid").toString()
                            if(document.exists()) delete = true
                        }
                        if(delete) {
                            db.collection("users").document(userUid)
                                .delete()
                                .addOnSuccessListener {
                                    db.collection("Numbers").document("CurrentNumbers")
                                        .update("numberInLab2", numberInLab2 + 1)
                                }
                        }
                    }
                    .addOnFailureListener{}
            }

        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.item4 -> {
                Toast.makeText(this, "Sign Out !", Toast.LENGTH_SHORT).show()
                AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener{

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener{
                            e-> Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                super.onOptionsItemSelected(item)
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
