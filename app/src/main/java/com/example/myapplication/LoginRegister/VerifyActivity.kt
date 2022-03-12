package com.example.myapplication.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.home.HomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class VerifyActivity : AppCompatActivity() {

    //auth
    private lateinit var mAuth: FirebaseAuth
    private lateinit var databaseRef: FirebaseDatabase

    var token: String? = null
    private var phoneNumber: String? = null
    private var verifiedLayout: LinearLayout? = null
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        //auth user
        mAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance()

        token = intent.getStringExtra("code")
        phoneNumber = intent.getStringExtra("pno")

        val pnoView: TextView = findViewById(R.id.otp_pno)
        val otpInput: EditText = findViewById(R.id.otp_view)
        val resendCode: TextView = findViewById(R.id.resend_code)
        val verifyButton: Button = findViewById(R.id.otp_verify)

        verifiedLayout = findViewById(R.id.verified_layout)
        loadingDialog = LoadingDialog(this)

        verifyButton.setOnClickListener {
            loadingDialog.startDialog()
            verifyCode(token!!, otpInput.text.toString())
        }

        pnoView.text = phoneNumber

        otpInput.addTextChangedListener {
            verifyButton.isEnabled = !(it.isNullOrEmpty() || it.length < 6)
        }

        //for resending otp code
        resendCode.setOnClickListener {
            loadingDialog.startDialog()
            otpInput.setText("")
            sendVerificationCode()
        }

    }

    private fun sendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber!!)
            .setActivity(this)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(mCallBack)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                loadingDialog.dismissDialog()
                token = p0
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {

            }

            override fun onVerificationFailed(p0: FirebaseException) {
                loadingDialog.dismissDialog()
                Toast.makeText(this@VerifyActivity, p0.message, Toast.LENGTH_LONG).show()
            }
        }

    private fun verifyCode(authToken: String, otpEntered: String) {
        val credentials = PhoneAuthProvider.getCredential(authToken, otpEntered)
        signInWithCredentials(credentials)
    }

    private fun signInWithCredentials(credentials: PhoneAuthCredential) {
        mAuth.signInWithCredential(credentials)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    verifiedLayout!!.visibility = View.VISIBLE
                    if(it.result?.additionalUserInfo?.isNewUser == true){
                        loadingDialog.dismissDialog()
                        startActivity(Intent(this@VerifyActivity, RegisterDetailsActivity::class.java))
                        finishAffinity()
                        Toast.makeText(this,"Register to continue",Toast.LENGTH_SHORT).show()
                    }else{
                        loadingDialog.dismissDialog()
                        startActivity(Intent(this@VerifyActivity, HomeActivity::class.java))
                        finishAffinity()
                        Toast.makeText(this,"Successfully Logged in!",Toast.LENGTH_SHORT).show()
                    }
                    loadingDialog.dismissDialog()
                } else {
                    //show error
                    loadingDialog.dismissDialog()
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_LONG).show()
                }
            }
    }


    //onBackPressed
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