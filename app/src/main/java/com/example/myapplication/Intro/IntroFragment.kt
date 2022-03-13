package com.example.myapplication.Intro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_intro.*
import kotlinx.android.synthetic.main.fragment_view_pager.*

class IntroFragment : Fragment() {

    lateinit var popIn: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_intro, container, false)


        //popin animation for as_meet logo
        Handler(Looper.myLooper()!!).postDelayed({
            popIn = AnimationUtils.loadAnimation(context, R.anim.pop_in)
        },500)

        //navigating
        Handler(Looper.myLooper()!!).postDelayed({
            if (onBoardingFinished()){
                findNavController().navigate(R.id.action_introFragment_to_loginRegisterActivity)
            }else{
                findNavController().navigate(R.id.action_introFragment_to_viewPagerFragment)
            }
        },3500)

        // Inflate the layout for this fragment
        return view
    }


    private fun onBoardingFinished():Boolean{
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished",false)
    }


}