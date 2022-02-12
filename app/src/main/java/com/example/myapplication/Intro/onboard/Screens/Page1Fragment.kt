package com.example.myapplication.Intro.onboard.Screens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import kotlinx.android.synthetic.main.fragment_page1.view.*
import kotlinx.android.synthetic.main.fragment_page3.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Page1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Page1Fragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_page1, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        view.nextBoard1.setOnClickListener {
            viewPager?.currentItem = 1
        }

        view.skipBoard1.setOnClickListener {
            viewPager?.currentItem = 2
        }

        return view
    }

}