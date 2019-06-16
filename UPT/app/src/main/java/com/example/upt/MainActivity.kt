package com.example.upt

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.textView2
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "MyActivity"
@SuppressLint("StaticFieldLeak")
val db = FirebaseFirestore.getInstance()

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private var myRequestCode: Int = 1111
    private lateinit var providers : List<AuthUI.IdpConfig>
    private var numberInQueue = 0
    private lateinit var notificationManager : NotificationManager
    private lateinit var  notificationChanel : NotificationChannel
    private lateinit var builder : Notification.Builder
    private val channelId = "com.example.upt"
    private val description = "Test Notification"
    private var succesfulySignIn : Boolean = false
    private var inQueue :Int = 0
    private var lineNumber :Int = 0
    private var persInLab :Int = 0
    private var persInQueue :Int = 0

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

        listenToDiffs()

        btn_queue.setOnClickListener {
            performeQueueOperations()
            btn_queue.visibility = View.INVISIBLE
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun notifyQueue(message : String){

        val intent = Intent(this, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChanel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChanel.enableLights(true)
            notificationChanel.lightColor = Color.RED
            notificationChanel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChanel)

            builder = Notification.Builder(this, channelId)
                .setContentTitle("Este Randul Tau!")
                .setContentText(message)
                .setSmallIcon(R.drawable.my_great_logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.logo))
                .setContentIntent(pendingIntent)
        }
        else{
            builder = Notification.Builder(this)
                .setContentTitle("Este Randul Tau!")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.logo))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
    }

    private fun listenToDiffs() {
        super.onStart()

        db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> Log.d(TAG, "New user: ${dc.document.data}")
                        DocumentChange.Type.MODIFIED -> Log.d(TAG, "Modified user: ${dc.document.data}")
                        DocumentChange.Type.REMOVED -> {
                            if (dc.document.get("lineNumber").toString() == lineNumber.toString()) {
                                db.collection("Numbers")
                                    .get()
                                    .addOnSuccessListener { resultNumbers ->
                                        for (documentNumbers in resultNumbers) {
                                            persInLab = if (lineNumber == 1) {
                                                documentNumbers.get("numberInLab1").toString().toInt()
                                            } else {
                                                documentNumbers.get("numberInLab2").toString().toInt()
                                            }
                                        }
                                        persInQueue = inQueue - (persInLab + 1)
                                        if(persInQueue <= 0) {
                                            textInAateptare.text = "0"
                                            notifyQueue("Te rugam sa te prezinti in laboratorul $lineNumber")
                                            text_turn.visibility = View.VISIBLE
                                        }
                                        else textInAateptare.text = persInQueue.toString()
                                        textInAateptare.text = persInQueue.toString()
                                    }
                                Log.d(TAG, "Removed user: ${dc.document.data}")
                            }
                        }
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.item1 -> {
                Toast.makeText(this, "Site-ul oficial UPT", Toast.LENGTH_SHORT).show()
                val url = "http://www.upt.ro/"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
                super.onOptionsItemSelected(item)
            }
            R.id.item2 -> {
                Toast.makeText(this, "Automatica si calculatoare", Toast.LENGTH_SHORT).show()
                val url = "http://www.ac.upt.ro/"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
                super.onOptionsItemSelected(item)
            }
            R.id.item3 -> {
                Toast.makeText(this, "Informatii admitere AC", Toast.LENGTH_SHORT).show()
                val url = "http://www.ac.upt.ro/admitere_studenti.php#top"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
                super.onOptionsItemSelected(item)
            }
            R.id.item4 -> {
                Toast.makeText(this, "Sign Out !", Toast.LENGTH_SHORT).show()
                AuthUI.getInstance().signOut(this@MainActivity)
                    .addOnCompleteListener{
                        setVisibility(false)
                        showSingInOptions()
                    }
                    .addOnFailureListener{
                            e-> Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                super.onOptionsItemSelected(item)
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == myRequestCode)
        {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK)
            {
                getUserData()
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, ""+user!!.email, Toast.LENGTH_SHORT).show()
                btn_queue.visibility = View.VISIBLE
                setVisibility(false)
                succesfulySignIn = true
                db.collection("SuperUsers")
                    .get()
                    .addOnSuccessListener { resultUsers ->
                        for (document in resultUsers) {
                            if(user.uid == document.get("uid"))
                            {
                                val intent = Intent(this, SuperUsersActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                btn_queue.isEnabled = true
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

    private fun performeQueueOperations() {
        db.collection("users").get().addOnSuccessListener { resultUsers ->
            var userAlreadyExists = false
            for(documentUsers in resultUsers)
            {
                if(FirebaseAuth.getInstance().currentUser!!.uid == documentUsers.get("uid"))
                {
                    userAlreadyExists = true
                    break
                }
            }
            if(!userAlreadyExists)
            {
                db.collection("Numbers").get().addOnSuccessListener { resultNumbers ->
                    for (documentNumbers in resultNumbers) {
                        numberInQueue = documentNumbers.get("currentNumber").toString().toInt() + 1
                    }

                    val userData = FirebaseAuth.getInstance().currentUser
                    userData?.let {

                        val user = HashMap<String, Any>()
                        user["name"] = userData.displayName.toString()
                        user["email"] = userData.email.toString()
                        user["photoUrl"] = userData.photoUrl.toString()
                        user["emailVerified"] = userData.isEmailVerified
                        user["uid"] = userData.uid

                        lineNumber = numberInQueue % 2 + 1
                        inQueue = numberInQueue / 2
                        if (lineNumber == 2) inQueue += 1

                        user["lineNumber"] = lineNumber
                        user["numberInQueue"] = inQueue

                        db.collection("Numbers")
                            .get()
                            .addOnSuccessListener { resultNumbers ->
                                for (documentNumbers in resultNumbers) {
                                    persInLab = if(lineNumber == 1) {
                                        documentNumbers.get("numberInLab1").toString().toInt()
                                    } else {
                                        documentNumbers.get("numberInLab2").toString().toInt()
                                    }
                                }
                            }

                        db.collection("users").document(userData.uid)
                            .set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot successfully written!")

                                db.collection("Numbers").document("CurrentNumbers").update("currentNumber", numberInQueue)
                            }
                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                        setVisibility(true)
                        persInQueue = inQueue - (persInLab + 1)
                        if(persInQueue <= 0){
                            notifyQueue("Te rugam sa te prezinti in laboratorul $lineNumber")
                            textInAateptare.text = "0"
                            text_turn.visibility = View.VISIBLE
                        }
                        else textInAateptare.text = persInQueue.toString()
                        textNumarRand.text = inQueue.toString()
                        textNumarCoada.text = lineNumber.toString()

                    }
                }
            }
            else
            {
                setVisibility(true)
            }
        }
    }

    private fun setVisibility(visibility : Boolean){
       if(visibility) {
           textNumarRand.visibility = View.VISIBLE
           textUniversitatea.visibility = View.VISIBLE
           textView1.visibility = View.VISIBLE
           textView2.visibility = View.VISIBLE
           textNumarCoada.visibility = View.VISIBLE
           textInAateptare.visibility = View.VISIBLE
           textPers.visibility = View.VISIBLE
       }
       else
       {
           textNumarRand.visibility = View.INVISIBLE
           textUniversitatea.visibility = View.INVISIBLE
           textView1.visibility = View.INVISIBLE
           textView2.visibility = View.INVISIBLE
           textNumarCoada.visibility = View.INVISIBLE
           textInAateptare.visibility = View.INVISIBLE
           textPers.visibility = View.INVISIBLE
           text_turn.visibility = View.INVISIBLE
       }
    }

    private fun getUserData(){
        db.collection("users").get().addOnSuccessListener { resultUsers ->
            for(documentUsers in resultUsers) {
                if (FirebaseAuth.getInstance().currentUser!!.uid == documentUsers.get("uid")) {
                    btn_queue.visibility = View.INVISIBLE
                    lineNumber = documentUsers.get("lineNumber").toString().toInt()
                    inQueue = documentUsers.get("numberInQueue").toString().toInt()

                    db.collection("Numbers")
                        .get()
                        .addOnSuccessListener { resultNumbers ->
                            for (documentNumbers in resultNumbers) {
                                persInLab = if(lineNumber == 1) {
                                    documentNumbers.get("numberInLab1").toString().toInt()
                                } else {
                                    documentNumbers.get("numberInLab2").toString().toInt()
                                }
                            }
                            persInQueue = inQueue - (persInLab + 1)
                            if(persInQueue <= 0) {
                                notifyQueue("Te rugam sa te prezinti in laboratorul $lineNumber")
                                textInAateptare.text = "0"
                                text_turn.visibility = View.VISIBLE
                            }
                            else textInAateptare.text = persInQueue.toString()
                            textNumarRand.text = inQueue.toString()
                            textNumarCoada.text = lineNumber.toString()
                            setVisibility(true)
                        }
                }
            }
        }
    }
}
