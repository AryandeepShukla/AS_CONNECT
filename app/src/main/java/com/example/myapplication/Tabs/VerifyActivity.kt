package com.example.myapplication.Tabs

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.HomeActivity
import com.example.myapplication.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mukesh.OtpView
import kotlinx.android.synthetic.main.activity_verify.*
import java.util.concurrent.TimeUnit

class VerifyActivity : AppCompatActivity() {

    private lateinit var resendCode: TextView
    private lateinit var otpLayout: LinearLayout
    private lateinit var verifiedLayout: LinearLayout
    private lateinit var otpEt: OtpView
    private lateinit var otpBut: Button

    //auth firebase
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    private lateinit var storedVerificationId: String
    private lateinit var phoneNumber: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks


    //realtime database
    private lateinit var database: DatabaseReference

    //user values from intent
    private lateinit var username: String
    private lateinit var phone: String
    private lateinit var email: String
    private lateinit var pass: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        //defining variables
        resendCode = findViewById(R.id.resend_code)
        otpLayout = findViewById(R.id.otp_layout)
        verifiedLayout = findViewById(R.id.verified_layout)
        otpBut = findViewById(R.id.otp_verify)
        otpEt = findViewById(R.id.otp_view)

        otpEt.addTextChangedListener {
            when (otpEt.length()) {
                6 ->
                    otpBut.isEnabled = true
                else ->
                    otpBut.isEnabled = false
            }
        }
        otpEt.setOtpCompletionListener {
            when (otpEt.length()) {
                6 ->
                    otpBut.isEnabled = true
                else ->
                    otpBut.isEnabled = false
            }
        }

        //for resending otp code
        resendCode.setOnClickListener {
            otpEt.setText("")
            verifiedLayout.visibility = View.INVISIBLE
            sendVerificationcode(phoneNumber)
        }

        //firebase
        auth = FirebaseAuth.getInstance()

        storedVerificationId = intent.getStringExtra("storedVerificationId").toString()
        phoneNumber = intent.getStringExtra("storedPhoneNumber").toString()
        otp_pno.text = phoneNumber

        //check otp length and then recognize token with otp to verify
        otpBut.setOnClickListener {
            val otp = otpEt.text.toString().trim()
            if (otp.isNotEmpty()) {
                val credential: PhoneAuthCredential =
                    PhoneAuthProvider.getCredential(storedVerificationId, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(applicationContext, "Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                verifiedLayout.visibility = View.VISIBLE
                Handler().postDelayed({
                    startActivity(Intent(this@VerifyActivity, HomeActivity::class.java))
                    finish()
                }, 2500)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(applicationContext, "Verification Failed: " + e, Toast.LENGTH_LONG)
                    .show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("TAG", "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //register user as phone no verified
                    registerUser()
                    //now open verifies animation layout
                    verifiedLayout.visibility = View.VISIBLE
                    otpBut.isEnabled = false
                    Handler().postDelayed({
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }, 2500)
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(applicationContext, "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun sendVerificationcode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun registerUser() {
        //get user details from intent
        username = intent.getStringExtra("storedUsernameId").toString()
        phone = intent.getStringExtra("storedPhoneNumber").toString()
        email = intent.getStringExtra("storedEmailId").toString()
        pass = intent.getStringExtra("storedPassId").toString()

        database = FirebaseDatabase.getInstance().getReference("Users")

        val User = User(username, phone, email, pass)

        database.child(phone).setValue(User).addOnSuccessListener {

            Toast.makeText(this, "Successfully Registered!.. Welcome: $username", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            Toast.makeText(this, "Failed: $it", Toast.LENGTH_SHORT).show()

        }
    }

    //onbackpressed
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to exit the app?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                ActivityCompat.finishAffinity(this@VerifyActivity)
            }

            setNegativeButton("No") { _, _ ->
                // if user press no, then return the activity
            }
            setCancelable(true)
        }.create().show()
    }

}