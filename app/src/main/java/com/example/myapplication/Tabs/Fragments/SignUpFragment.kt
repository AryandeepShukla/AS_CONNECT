package com.example.myapplication.Tabs.Fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.myapplication.HomeActivity
import com.example.myapplication.R
import com.example.myapplication.Tabs.VerifyActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.util.concurrent.TimeUnit


class SignUpFragment : Fragment() {
    //variables
    private lateinit var phoneEt: EditText
    private lateinit var userNameEt: EditText
    private lateinit var emailEt: EditText
    private lateinit var passEt: EditText
    private lateinit var rePassEt: EditText
    private lateinit var suBut: Button
    private lateinit var countryCode: String
    private lateinit var phoneNumber: String
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var pass: String
    private lateinit var repass: String

    //auth firebase
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        //defining variables
        phoneEt = view.findViewById(R.id.ti_pno)
        suBut = view.findViewById(R.id.signUp_but)
        userNameEt = view.findViewById(R.id.ti_SUUsername)
        emailEt = view.findViewById(R.id.ti_SUEmail)
        passEt = view.findViewById(R.id.ti_SUPass)
        rePassEt = view.findViewById(R.id.ti_SURePass)


        phoneEt.addTextChangedListener { value ->
            suBut.isEnabled = !(value.isNullOrEmpty() || value.length < 10)
        }
        userNameEt.addTextChangedListener { value ->
            suBut.isEnabled = !(value.isNullOrEmpty() || value.length < 4 || value.length > 15)
        }
        emailEt.addTextChangedListener { value ->
            suBut.isEnabled = !(value.isNullOrEmpty() || value.length < 5 || value.length >= 50)
        }
        passEt.addTextChangedListener { value ->
            suBut.isEnabled = !(value.isNullOrEmpty() || value.length <= 8 || value.length >= 15)
        }
        rePassEt.addTextChangedListener { value ->
            suBut.isEnabled = !(value.isNullOrEmpty() || value.length <= 8 || value.length >= 15)
        }

        //check all entries and send verification code
        suBut.setOnClickListener {
            getEntries()
            if (phoneNumber.isNotEmpty() && (pass == repass) && (pass.length in 8..15) &&
                (username.length in 5..16) && (email.length in 6..50)
            ) {
                sendVerificationCode(phoneNumber)
            } else {
                if (username.length !in 5..16)
                    Toast.makeText(
                        activity,
                        "Username should contain 5-16 characters! ",
                        Toast.LENGTH_SHORT
                    ).show()
                else if (phoneNumber.isEmpty() || phoneNumber.length < 10)
                    Toast.makeText(activity, "Enter mobile number", Toast.LENGTH_SHORT).show()
                else if (email.length !in 6..50)
                    Toast.makeText(
                        activity,
                        "Enter Email containing 6-26 characters! ",
                        Toast.LENGTH_SHORT
                    ).show()
                else if (pass.length !in 8..15)
                    Toast.makeText(
                        activity,
                        "Enter Password containing 8-15 characters! ",
                        Toast.LENGTH_SHORT
                    ).show()
                else if (pass != repass)
                    Toast.makeText(activity, "Password doesn't matches", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(activity, "Check the entries! ", Toast.LENGTH_SHORT).show()
            }
        }

        //firebase
        auth = FirebaseAuth.getInstance()

        //already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "HOME INTENT!",
                Snackbar.LENGTH_SHORT
            ).show()
            startActivity(Intent(view.context, HomeActivity::class.java))
            requireActivity().finish()
        }
        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Handler().postDelayed({
                    startActivity(Intent(view.context, HomeActivity::class.java))
                    requireActivity().finish()
                }, 2500)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(view!!.context, "Verification Failed: $e", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("TAG", "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token
                //intent to verify phone no if verified then register the user
                val intent = Intent(activity, VerifyActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                intent.putExtra("storedUsernameId", username.trim())
                intent.putExtra("storedPhoneNumber", phoneNumber.trim())
                intent.putExtra("storedEmailId", email.trim())
                intent.putExtra("storedPassId", pass.trim())
                startActivity(intent)
            }
        }

        return view
    }

    private fun sendVerificationCode(number: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity()) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun getEntries() {
        try {
            countryCode = ccp.selectedCountryCodeWithPlus
            phoneNumber = countryCode + phoneEt.text.toString()
            username = userNameEt.text.toString()
            email = emailEt.text.toString()
            pass = passEt.text.toString()
            repass = rePassEt.text.toString()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Check entries :$e", Toast.LENGTH_SHORT).show()
        }
    }


}