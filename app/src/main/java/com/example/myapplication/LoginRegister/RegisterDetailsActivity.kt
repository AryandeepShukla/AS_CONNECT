package com.example.myapplication.LoginRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class RegisterDetailsActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var databaseRef : FirebaseDatabase
    lateinit var loadingDialog: LoadingDialog

    //upper views
    lateinit var phoneNumber : TextView
    lateinit var back : ImageView

    //entries
    lateinit var firstNameET : EditText
    lateinit var lastNameET : EditText
    lateinit var usernameET : EditText
    lateinit var emailET : EditText
    lateinit var zipCodeET : EditText
    lateinit var addressET : EditText
    lateinit var continueButton : Button
    lateinit var firstName : String
    lateinit var lastName : String
    lateinit var username : String
    lateinit var email : String
    lateinit var zipCode : String
    lateinit var address : String
    lateinit var country : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_details)

        loadingDialog = LoadingDialog(this)

        //auth user
        mAuth= FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        //database for profile
        databaseRef = FirebaseDatabase.getInstance()

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
        continueButton = findViewById(R.id.buttonContinue)


        //get pno of user
        val pno = currentUser.phoneNumber.toString()
        phoneNumber.text = pno.substring(0,3) + " " + pno.substring(3)

        back.setOnClickListener {
            onBackPressed()
        }

        continueButton.setOnClickListener {
            loadingDialog.startDialog()
            try {
                registerEntries()
                Toast.makeText(this, "Successfully Registered!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterDetailsActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            catch (e:Exception){
                Toast.makeText(this, "Error Occurred : $e", Toast.LENGTH_LONG).show()
            }
            loadingDialog.dismissDialog()
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
        country = intent.getStringExtra("country").toString()

        //realtime database
        val ref = databaseRef.reference.child("profile")

        val tableRef = ref.child(currentUser.phoneNumber.toString())
        tableRef.child("first_name").setValue(firstName)
        tableRef.child("last_name").setValue(lastName)
        tableRef.child("username").setValue(username)
        tableRef.child("email").setValue(email)
        tableRef.child("zip_code").setValue(zipCode)
        tableRef.child("address").setValue(address)
        tableRef.child("country").setValue(country)
        tableRef.child("phone").setValue(currentUser.phoneNumber.toString())
        tableRef.child("uid").setValue(mAuth.currentUser?.uid!!)
        tableRef.child("registered").setValue("true")

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
                val intent = Intent(this@RegisterDetailsActivity, LoginRegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                loadingDialog.dismissDialog()
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


}