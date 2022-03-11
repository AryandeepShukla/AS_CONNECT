package com.example.myapplication.LoginRegister

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.home.HomeActivity
import com.example.myapplication.home.UserFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_register_details.*


class RegisterDetailsActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var databaseRef : FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference
    private lateinit var imageUri : Uri
    private var imageUrl : String? = null

    //upper views
    lateinit var phoneNumber : TextView
    lateinit var back : ImageView
    lateinit var loadingDialog: LoadingDialog

    //entries
    lateinit var firstNameET : EditText
    lateinit var lastNameET : EditText
    lateinit var usernameET : EditText
    lateinit var emailET : EditText
    lateinit var zipCodeET : EditText
    lateinit var addressET : EditText
    lateinit var firstName : String
    lateinit var lastName : String
    lateinit var username : String
    lateinit var email : String
    lateinit var zipCode : String
    lateinit var address : String
    lateinit var image : ImageView
    lateinit var imgUploadButton : Button
    lateinit var continueButton : Button

    private var mRequestQueue : RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_details)

        loadingDialog = LoadingDialog(this)
        mRequestQueue = Volley.newRequestQueue(this);

        //auth user
        mAuth= FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        //database for profile
        databaseRef = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        //upper views
        phoneNumber = findViewById(R.id.phoneNo)
        back = findViewById(R.id.backIV)

        //entries
        firstNameET = findViewById(R.id.firstNameEt)
        lastNameET = findViewById(R.id.lastNameEt)
        usernameET = findViewById(R.id.usernameEt)
        emailET = findViewById(R.id.emailEt)
        zipCodeET = findViewById(R.id.zipCodeEt)
        addressET = findViewById(R.id.addressEt)
        image = findViewById(R.id.imageUpload)
        imgUploadButton = findViewById(R.id.imageUploadButton)
        continueButton = findViewById(R.id.buttonContinue)

        //get pno of user
        val pno = currentUser.phoneNumber.toString()
        phoneNumber.text = pno.substring(0,3) + " " + pno.substring(3)

        back.setOnClickListener {
            onBackPressed()
        }

        imageUploadButton.setOnClickListener {
            pickFromGallery()
        }

        continueButton.setOnClickListener {
            loadingDialog.startDialog()
            try {
                uploadProfilePic()
            }
            catch (e:Exception){
                loadingDialog.dismissDialog()
                Toast.makeText(this, "Error Occurred : $e", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun pickFromGallery(){
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK && data != null){
            imageUri = data.data!!
            image.setImageURI(imageUri)
        }
    }

    fun uploadProfilePic(){
        val randomName : String = currentUser.phoneNumber.toString()
        val riversRef: StorageReference = storageReference.child("images/$randomName")
        riversRef.putFile(imageUri)
            .addOnSuccessListener {
                image.setImageURI(null)
                // Get a URL to the uploaded content
                val urlTask : Task<Uri> = it.storage.downloadUrl
                while (!urlTask.isSuccessful);
                val downloadUrl : Uri = urlTask.result
                imageUrl = downloadUrl.toString()
                Log.e("ImageUrl : ", imageUrl.toString())
                Toast.makeText(this,"Profile Uploaded",Toast.LENGTH_SHORT).show()
                registerEntries()
            }
            .addOnFailureListener {
                loadingDialog.dismissDialog()
                Toast.makeText(this,"Failed to Upload Image : $it",Toast.LENGTH_SHORT).show()
            }
    }

    fun registerEntries(){
        //read from edit text
        firstName = firstNameET.text.toString()
        lastName = lastNameET.text.toString()
        username = usernameET.text.toString()
        email = emailET.text.toString()
        zipCode = zipCodeET.text.toString()
        address = addressET.text.toString()

        //realtime database
        val ref = databaseRef.reference.child("profile")
        val tableRef = ref.child(currentUser.phoneNumber.toString())
        tableRef.child("first_name").setValue(firstName)
        tableRef.child("profile_pic_url").setValue(imageUrl)
        tableRef.child("last_name").setValue(lastName)
        tableRef.child("username").setValue(username)
        tableRef.child("email").setValue(email)
        tableRef.child("zip_code").setValue(zipCode)
        tableRef.child("address").setValue(address)
        tableRef.child("phone").setValue(currentUser.phoneNumber.toString())
        tableRef.child("uid").setValue(mAuth.currentUser?.uid!!)
        tableRef.child("registered").setValue("true")

        Toast.makeText(this,"Successfully Registered!", Toast.LENGTH_LONG).show()

        val intent = Intent(this@RegisterDetailsActivity, HomeActivity::class.java)
        loadingDialog.dismissDialog()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finishAffinity()
    }

    fun logout() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to logout?")

            setPositiveButton("Yes") { _, _ ->
                //changed registered to false
                loadingDialog.startDialog()

                // if user press yes, then finish the current activity
                mAuth.signOut()
                loadingDialog.dismissDialog()
                val intent = Intent(this@RegisterDetailsActivity, LoginRegisterActivity::class.java)
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
        logout()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog.dismissDialog()
    }

}
