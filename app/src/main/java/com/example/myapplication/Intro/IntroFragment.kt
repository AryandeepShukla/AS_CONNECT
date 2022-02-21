package com.example.myapplication.Intro

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_intro.*
import kotlinx.android.synthetic.main.fragment_view_pager.*

class IntroFragment : Fragment() {

    lateinit var popIn: Animation
    lateinit var topAnim: Animation
    lateinit var logoAnim: Animation
    lateinit var bottomAnim: Animation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //popin animation for as_meet logo
        Handler(Looper.myLooper()!!).postDelayed({
            app_name.visibility = View.VISIBLE
            popIn = AnimationUtils.loadAnimation(context, R.anim.pop_in)
            app_name.startAnimation(popIn)
        },800)

        //animations for navigation of splash screen to viewpager
        Handler(Looper.myLooper()!!).postDelayed({
            topAnim = AnimationUtils.loadAnimation(context, R.anim.top_anime)
            bottomAnim = AnimationUtils.loadAnimation(context, R.anim.bottom_anime)
            logoAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out_anime)

            app_name.startAnimation(topAnim)
            logo.startAnimation(logoAnim)
            tag_text.startAnimation(bottomAnim)
        },2500)

        //navigating
        Handler(Looper.myLooper()!!).postDelayed({
            if (onBoardingFinished()){
                findNavController().navigate(R.id.action_introFragment_to_loginRegisterActivity)
            }else{
                findNavController().navigate(R.id.action_introFragment_to_viewPagerFragment)
            }
        },3500)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    private fun onBoardingFinished():Boolean{
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished",false)
    }


}