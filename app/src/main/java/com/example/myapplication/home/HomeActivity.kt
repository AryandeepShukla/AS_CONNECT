package com.example.myapplication.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.myapplication.LoadingDialog
import com.example.myapplication.LoginRegister.LoginRegisterActivity
import com.example.myapplication.LoginRegister.RegisterDetailsActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var databaseRef: FirebaseDatabase
    var phone: String? = null
    var country: String? = null

    lateinit var logout: ImageView
    lateinit var profile: ImageView
    private var loadingDialog: LoadingDialog = LoadingDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //auth user
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        phone = mAuth.currentUser!!.phoneNumber.toString()
        databaseRef = FirebaseDatabase.getInstance()

        //check if user is registered or not
        getIfRegistered()
        loadingDialog.dismissDialog()

        if (intent!=null){
            country = intent.getStringExtra("country")
        }

        logout = findViewById(R.id.logoutHome)
        profile = findViewById(R.id.profile)
        logout.setOnClickListener {
            logout()
        }
        profile.setOnClickListener {
            startActivity(Intent(this@HomeActivity, UserProfileActivity::class.java))
            finish()
        }

    }

    fun logout() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to logout?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                mAuth.signOut()
                val intent = Intent(this@HomeActivity, LoginRegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }

            setNegativeButton("No") { _, _ ->
                // if user press no, then return the activity
            }
            setCancelable(true)
        }.create().show()
    }

    //onbackpressed
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to exit the app?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                ActivityCompat.finishAffinity(this@HomeActivity)
            }

            setNegativeButton("No") { _, _ ->
                // if user press no, then return the activity
            }
            setCancelable(true)
        }.create().show()
    }


    private fun getIfRegistered(){
        loadingDialog.startDialog()
        var isRegistered: Boolean = false
        val ref = databaseRef.reference.child("profile")
        ref.child(currentUser.phoneNumber.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                loadingDialog.dismissDialog()
                isRegistered = it.child("registered").value.toString().toBoolean()
            } else {
                loadingDialog.dismissDialog()
                isRegistered =false
            }
            if (!isRegistered){
                loadingDialog.dismissDialog()
                val intent = Intent(this@HomeActivity, RegisterDetailsActivity::class.java)
                intent.putExtra("country", country)
                startActivity(intent)
                finish()
            }
        }
    }
}

