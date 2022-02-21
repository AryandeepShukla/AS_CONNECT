package com.example.myapplication.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.myapplication.LoadingDialog
import com.example.myapplication.LoginRegister.LoginRegisterActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var databaseRef: FirebaseDatabase

    //top views
    lateinit var home: ImageView
    lateinit var logout: ImageView
    lateinit var loadingDialog: LoadingDialog

    //user details
    lateinit var phone: String
    lateinit var pno: TextView
    lateinit var name: TextView
    lateinit var username: TextView
    lateinit var email: TextView
    lateinit var countryZipCode: TextView
    lateinit var address: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        pno = findViewById(R.id.phoneProfile)
        name = findViewById(R.id.nameProfile)
        username = findViewById(R.id.usernameProfile)
        email = findViewById(R.id.emailProfile)
        countryZipCode = findViewById(R.id.countryZipCodeProfile)
        address = findViewById(R.id.addressProfile)

        loadingDialog = LoadingDialog(this)

        //auth user
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        phone = mAuth.currentUser!!.phoneNumber.toString()

        //database for profile
        databaseRef = FirebaseDatabase.getInstance()
        loadingDialog.startDialog()
        val ref = databaseRef.reference.child("profile")

        //fetching from realtime database
        val tableRef = ref.child(currentUser.phoneNumber.toString())
        tableRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UserProfileActivity,
                    "Error occurred while fetching the details! ",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                //showing details in user profile
                val spacedPhone = phone.substring(0, 3) + " " + phone.substring(3)
                pno.text = spacedPhone

                val fullName =
                    snapshot.child("first_name").value.toString() + " " + snapshot.child("last_name").value.toString()
                name.text = fullName

                username.text = snapshot.child("username").value.toString()

                email.text = snapshot.child("email").value.toString()

                val countryPlusCode =
                    snapshot.child("country").value.toString()  + " (" + snapshot.child("zip_code").value.toString() +")"
                countryZipCode.text = countryPlusCode

                address.text = snapshot.child("address").value.toString()
                loadingDialog.dismissDialog()
            }
        })

        //top views
        home = findViewById(R.id.homeProfile)
        logout = findViewById(R.id.logoutProfile)

        home.setOnClickListener {
            startActivity(Intent(this@UserProfileActivity,HomeActivity::class.java))
            finish()
        }
        logout.setOnClickListener {
            logout()
        }

    }

    fun logout() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to logout?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                mAuth.signOut()
                val intent = Intent(this@UserProfileActivity, LoginRegisterActivity::class.java)
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
                ActivityCompat.finishAffinity(this@UserProfileActivity)
            }

            setNegativeButton("No") { _, _ ->
                // if user press no, then return the activity
            }
            setCancelable(true)
        }.create().show()
    }


}