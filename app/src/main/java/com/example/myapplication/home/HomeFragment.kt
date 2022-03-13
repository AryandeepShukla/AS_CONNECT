package com.example.myapplication.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class HomeFragment : Fragment() {

    //dailog
    lateinit var loadingDialog: LoadingDialog

    //binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    //auth
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var databaseRef: FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference


    //user details
    lateinit var phone: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(this.requireActivity())

        //auth user
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        phone = mAuth.currentUser!!.phoneNumber.toString()

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("images/${currentUser.phoneNumber.toString()}")




    }
}