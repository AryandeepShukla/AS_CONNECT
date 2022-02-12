package com.example.myapplication.Tabs.Fragments

import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_sign_in.*

class SignInFragment : Fragment() {

    private lateinit var siPnoEt: EditText
    private lateinit var userNameEt: EditText
    private lateinit var passEt: EditText
    private lateinit var siBut: Button
    private lateinit var countrycode: String
    private lateinit var phoneNumber: String
    private lateinit var username: String
    private lateinit var pass: String

    //auth firebase
    private lateinit var auth: FirebaseAuth

    //realtime database
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        siPnoEt = view.findViewById(R.id.ti_si_pno)
        userNameEt = view.findViewById(R.id.ti_SIUsername)
        passEt = view.findViewById(R.id.ti_SIPass)
        siBut = view.findViewById(R.id.signIn_but)

        siPnoEt.addTextChangedListener { value ->
            siBut.isEnabled = !(value.isNullOrEmpty() || value.length < 10)
        }
        userNameEt.addTextChangedListener { value ->
            siBut.isEnabled = !(value.isNullOrEmpty() || value.length < 4 || value.length > 15)
        }
        passEt.addTextChangedListener { value ->
            siBut.isEnabled = !(value.isNullOrEmpty() || value.length <= 8 || value.length >= 15)
        }

        //get the entries and move to readData to check them
        siBut.setOnClickListener {
            getEntries()
            if (phoneNumber.isNotEmpty() && (pass.length in 8..15) && (username.length in 5..16)) {
                readData(phoneNumber, username, pass)
            } else {
                if (phoneNumber.isEmpty() || phoneNumber.length < 10)
                    Toast.makeText(activity, "Enter mobile number", Toast.LENGTH_SHORT).show()
                else if (username.length !in 5..16)
                    Toast.makeText(
                        activity,
                        "Username should contain 5-16 characters! ",
                        Toast.LENGTH_SHORT
                    ).show()
                else if (pass.length !in 8..15)
                    Toast.makeText(
                        activity,
                        "Enter Password containing 8-15 characters! ",
                        Toast.LENGTH_SHORT
                    ).show()
                else
                    Toast.makeText(activity, "Check the entries! ", Toast.LENGTH_SHORT).show()
            }
        }

        //firebase auth
        auth = FirebaseAuth.getInstance()
        //check if user's already logged in
        checkUser()

        return view
    }



    private fun readData(phone: String, username: String, pass: String) {
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(phone).get().addOnSuccessListener {

            if (it.exists()) {
                val fetchedUsername = it.child("username").value
                val fetchedPass = it.child("pass").value

                if ((fetchedUsername == username) && (fetchedPass == pass)) {
                    try {
                        activity?.let{
                            val intent = Intent (it, HomeActivity::class.java)
                            it.startActivity(intent)
                        }
                        //startActivity(Intent(activity, HomeActivity::class.java))
                        Toast.makeText(activity, "Successfully Logged In", Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Login Failed! :$e", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(activity, "Wrong Username & Password!", Toast.LENGTH_SHORT)
                        .show()
                }

            } else {
                Toast.makeText(
                    activity,
                    "User doesn't exist! Please SIGN UP first",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }.addOnFailureListener {
            Toast.makeText(activity, "Failed! :$it", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEntries() {
        try {
            countrycode = si_ccp.selectedCountryCodeWithPlus
            phoneNumber = countrycode + siPnoEt.text.toString()
            phoneNumber = phoneNumber.trim()
            username = userNameEt.text.toString().trim()
            pass = passEt.text.toString().trim()
        } catch (e: Exception) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Number not found!",
                Snackbar.LENGTH_SHORT
            ).show()
            //(activity as LoginRegisterActivity).onBackPressed()
        }
    }
    private fun checkUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(activity, HomeActivity::class.java))
            requireActivity().finish()
        }
    }
}