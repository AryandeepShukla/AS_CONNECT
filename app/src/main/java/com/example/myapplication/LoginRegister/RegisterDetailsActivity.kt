package com.example.myapplication.LoginRegister

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.home.HomeActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_register_details.*
import java.io.ByteArrayOutputStream


class RegisterDetailsActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var databaseRef : FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference
    private lateinit var imageUri : Uri
    private var imageUrl : String? = null
    lateinit var downloadUrl : String

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
            if(!::downloadUrl.isInitialized){
                Toast.makeText(this,"DP can't be empty", Toast.LENGTH_SHORT).show()
            }
            else{
                loadingDialog.startDialog()
                try {
                    registerEntries()
                }
                catch (e:Exception){
                    loadingDialog.dismissDialog()
                    Toast.makeText(this, "Error Occurred : $e", Toast.LENGTH_LONG).show()
                }
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
            var original  = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            var baos  = ByteArrayOutputStream()
            original.compress(Bitmap.CompressFormat.JPEG, 30, baos)
            image.setImageBitmap(original)
            var imgByte = baos.toByteArray()
            uploadProfilePic(imgByte)
        }
    }

    fun uploadProfilePic(imgByte: ByteArray) {
        loadingDialog.startDialog()
        continueButton.isEnabled = false
        val randomName : String = currentUser.phoneNumber.toString()
        val riversRef: StorageReference = storageReference.child("images/$randomName")

        var uploadTask = riversRef.putBytes(imgByte)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{task ->
            if(!task.isSuccessful){
                Log.e("Error uplaoding image: ", task.exception.toString())
                Toast.makeText(this,"Failed to Upload Image!",Toast.LENGTH_SHORT).show()
            }
            return@Continuation riversRef.downloadUrl
        }).addOnCompleteListener { task ->
            continueButton.isEnabled = true
            if(task.isSuccessful){
                downloadUrl = task.result.toString()
            }
            Log.e("ImageUrl : ", downloadUrl.toString())
            Toast.makeText(this,"DP Uploaded",Toast.LENGTH_SHORT).show()
        }
        loadingDialog.dismissDialog()
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
        tableRef.child("profile_pic_url").setValue(downloadUrl)
        tableRef.child("last_name").setValue(lastName)
        tableRef.child("username").setValue(username)
        tableRef.child("email").setValue(email)
        tableRef.child("zip_code").setValue(zipCode)
        tableRef.child("address").setValue(address)
        tableRef.child("phone").setValue(currentUser.phoneNumber.toString())
        tableRef.child("uid").setValue(mAuth.currentUser?.uid!!)
        tableRef.child("registered").setValue("true")

        loadingDialog.dismissDialog()
        Toast.makeText(this,"Successfully Registered!", Toast.LENGTH_LONG).show()
        startActivity(Intent(this@RegisterDetailsActivity, HomeActivity::class.java))
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
                val sharedPref = context.getSharedPreferences("curUser", Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()
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
