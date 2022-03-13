package com.example.myapplication.home

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.example.myapplication.LoadingDialog
import com.example.myapplication.LoginRegister.LoginRegisterActivity
import com.example.myapplication.LoginRegister.RegisterDetailsActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    //auth
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    var phone: String? = null

    lateinit var loadingDialog: LoadingDialog
    lateinit var meowNav: MeowBottomNavigation
    private var prevFrag: Int? = 1

    //user details
    var spacedPhone: String? = null
    var name: String? = null
    var username: String? = null
    var email: String? = null
    var zipCode: String? = null
    var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        loadingDialog = LoadingDialog(this)

        meowNav = findViewById(R.id.meowBottomNav)
        meowNav.add(MeowBottomNavigation.Model(1, R.drawable.ic_baseline_home_black_24))
        meowNav.add(MeowBottomNavigation.Model(2, R.drawable.user_group))
        meowNav.add(MeowBottomNavigation.Model(3, R.drawable.ic_user_24))
        meowNav.add(MeowBottomNavigation.Model(4, R.drawable.ic_logout_black_24))

        meowNav.show(1, true)
        replace(HomeFragment())
        meowNav.setOnClickMenuListener {
            when (it.id) {
                1 -> {
                    prevFrag = 1
                    replace(HomeFragment())
                }
                2 -> {
                    prevFrag = 2
                    replace(GroupFragment())
                }
                3 -> {
                    prevFrag = 3
                    replace(UserFragment())
                }
                4 -> {
                    logout()
                }
            }
        }

        //auth user
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        phone = mAuth.currentUser!!.phoneNumber.toString()

        //fetch user details and save in shared preferences
        loadingDialog.startDialog()
        if (userFetched()) {
            loadingDialog.dismissDialog()
            Toast.makeText(
                this@HomeActivity,
                "Welcome",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val databaseRef = FirebaseDatabase.getInstance()
            val ref = databaseRef.reference.child("profile")
            val currentUser = mAuth.currentUser
            phone = currentUser!!.phoneNumber.toString()

            //fetching from realtime database
            val tableRef = ref.child(phone!!)
            tableRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Error occurred while fetching the details! ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    //showing details in user profile
                    spacedPhone = phone!!.substring(0, 3) + " " + phone!!.substring(3)
                    name =
                        snapshot.child("first_name").value.toString() + " " + snapshot.child("last_name").value.toString()
                    username = snapshot.child("username").value.toString()
                    email = snapshot.child("email").value.toString()
                    zipCode = snapshot.child("zip_code").value.toString()
                    address = snapshot.child("address").value.toString()
                    savedUserPref()
                    loadingDialog.dismissDialog()
                }
            })
        }

    }

    fun savedUserPref() {
        val sharedPref = getSharedPreferences("curUser", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.apply {
            putString("fname", name)
            putString("sphone",spacedPhone)
            putString("username", username)
            putString("email", email)
            putString("zipCode", zipCode)
            putString("address", address)
            putBoolean("fetched", true)
            apply()
        }
    }

    // Extension function to replace fragment
    fun replace(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.home_frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun userFetched(): Boolean {
        val sharedPref = getSharedPreferences("curUser", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("fetched", false)
    }

    fun logout() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to logout?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                val sharedPref = context.getSharedPreferences("curUser", Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()
                mAuth.signOut()
                val intent = Intent(this@HomeActivity, LoginRegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }

            setNegativeButton("No") { _, _ ->
                meowNav.show(prevFrag!!, true)
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
                ActivityCompat.finishAffinity(this@HomeActivity)
            }

            setNegativeButton("No") { _, _ ->

            }
            setCancelable(true)
        }.create().show()
    }

}

