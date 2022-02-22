package com.example.myapplication.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class UserProfileActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var databaseRef: FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri

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
    lateinit var profilePic: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        profilePic = findViewById(R.id.imageProfile)
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

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("images/${currentUser.phoneNumber.toString()}")
        setProfilePic()

        //database for profile
        databaseRef = FirebaseDatabase.getInstance()
        val ref = databaseRef.reference.child("profile")

        //fetching from realtime database
        loadingDialog.startDialog()
        val tableRef = ref.child(currentUser.phoneNumber.toString())
        tableRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissDialog()
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
                    snapshot.child("country").value.toString() + " (" + snapshot.child("zip_code").value.toString() + ")"
                countryZipCode.text = countryPlusCode

                address.text = snapshot.child("address").value.toString()
                loadingDialog.dismissDialog()

            }
        })

        //top views
        home = findViewById(R.id.homeProfile)
        logout = findViewById(R.id.logoutProfile)

        home.setOnClickListener {
            startActivity(Intent(this@UserProfileActivity, HomeActivity::class.java))
            finish()
        }
        logout.setOnClickListener {
            logout()
        }

    }

    private fun setProfilePic() {
        loadingDialog.startDialog()
        val localFile = File.createTempFile("tempFile", ".jpg");

        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profilePic.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(
                this@UserProfileActivity,
                "Error occurred while fetching the profile picture! ",
                Toast.LENGTH_LONG
            ).show()
        }
        loadingDialog.dismissDialog()
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