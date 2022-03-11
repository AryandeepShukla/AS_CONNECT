package com.example.myapplication.LoginRegister

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.home.HomeActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.hbb20.CountryCodePicker
import java.util.concurrent.TimeUnit

class LoginRegisterActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    private lateinit var phoneNumber: String
    lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        loadingDialog = LoadingDialog(this)

        mAuth = FirebaseAuth.getInstance()

        val sendBtn: Button = findViewById(R.id.getOtpBut)
        val ccp: CountryCodePicker = findViewById(R.id.ccp)
        val pno: EditText = findViewById(R.id.ti_pno)

        pno.addTextChangedListener { value ->
            sendBtn.isEnabled = !(value.isNullOrEmpty() || value.length < 10)
        }

        loadingDialog = LoadingDialog(this)

        sendBtn.setOnClickListener {
            loadingDialog.startDialog()
            phoneNumber = ccp.selectedCountryCodeWithPlus + pno.text.toString()
            sendVerificationCode(phoneNumber)
        }

    }


    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(phoneNumber)
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
                val intent = Intent(this@LoginRegisterActivity, VerifyActivity::class.java)
                intent.putExtra("code", p0)
                intent.putExtra("pno", phoneNumber)
                loadingDialog.dismissDialog()
                startActivity(intent)
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                loadingDialog.dismissDialog()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                loadingDialog.dismissDialog()
                Toast.makeText(this@LoginRegisterActivity, p0.message, Toast.LENGTH_LONG).show()
            }
        }

    override fun onStart() {
        super.onStart()

        if (mAuth?.currentUser != null) {
            startActivity(Intent(this@LoginRegisterActivity, HomeActivity::class.java))
            finish()
        }
    }

    //onBackPressed
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to exit the app?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                ActivityCompat.finishAffinity(this@LoginRegisterActivity)
            }

            setNegativeButton("No") { _, _ ->
                // if user press no, then return the activity
            }
            setCancelable(true)
        }.create().show()
    }

}