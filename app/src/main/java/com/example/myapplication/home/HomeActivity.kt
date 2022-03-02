package com.example.myapplication.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    lateinit var databaseRef: FirebaseDatabase
    var phone: String? = null

    private var loadingDialog: LoadingDialog = LoadingDialog(this)
    lateinit var meowNav : MeowBottomNavigation
    private var prevFrag : Int? = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        meowNav = findViewById(R.id.meowBottomNav)
        meowNav.add(MeowBottomNavigation.Model(1, R.drawable.ic_baseline_home_black_24))
        meowNav.add(MeowBottomNavigation.Model(2, R.drawable.user_group))
        meowNav.add(MeowBottomNavigation.Model(3, R.drawable.ic_user_24))
        meowNav.add(MeowBottomNavigation.Model(4, R.drawable.ic_logout_black_24))

        meowNav.show(1, true)
        replace(HomeFragment())
        meowNav.setOnClickMenuListener {
            when(it.id){
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
        databaseRef = FirebaseDatabase.getInstance()

        //check if user is registered or not
        getIfRegistered()
        loadingDialog.dismissDialog()

    }

    // Extension function to replace fragment
    fun replace(fragment:Fragment){
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.home_frame,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun logout() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to logout?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
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


    private fun getIfRegistered(){
        loadingDialog.startDialog()
        var isRegistered: Boolean = false
        val ref = databaseRef.reference.child("profile")
        ref.child(currentUser.phoneNumber.toString()).get().addOnSuccessListener {
            if (it.exists()) {
                loadingDialog.dismissDialog()
                isRegistered = it.child("registered").value.toString().toBoolean()
            } else {
                loadingDialog.dismissDialog()
                isRegistered =false
            }
            if (!isRegistered){
                loadingDialog.dismissDialog()
                val intent = Intent(this@HomeActivity, RegisterDetailsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }
    }
}

