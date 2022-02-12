package com.example.myapplication.Tabs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.myapplication.R
import com.example.myapplication.Tabs.Fragments.Adapters.ViewPagerAdapter
import com.example.myapplication.Tabs.Fragments.SignInFragment
import com.example.myapplication.Tabs.Fragments.SignUpFragment
import kotlinx.android.synthetic.main.activity_login_register.*
import kotlin.system.exitProcess

class LoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)
        setUpTabs()

    }

    private fun setUpTabs() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(SignInFragment(), "SIGN IN")
        adapter.addFragment(SignUpFragment(), "SIGN UP")
        view_pagerLogin.adapter = adapter
        tab_layout.setupWithViewPager(view_pagerLogin)
    }

    //onbackpressed
    override fun onBackPressed() {
        AlertDialog.Builder(this).apply {
            setTitle("Please confirm.")
            setMessage("Do you want to exit the app?")

            setPositiveButton("Yes") { _, _ ->
                // if user press yes, then finish the current activity
                ActivityCompat.finishAffinity(this@LoginRegisterActivity)
            }

            setNegativeButton("No"){_, _ ->
                // if user press no, then return the activity
            }
            setCancelable(true)
        }.create().show()
    }

}