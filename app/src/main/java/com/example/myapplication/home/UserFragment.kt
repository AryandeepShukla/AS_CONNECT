package com.example.myapplication.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.myapplication.LoadingDialog
import com.example.myapplication.LoginRegister.LoginRegisterActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class UserFragment : Fragment(R.layout.fragment_user) {

    private var _binding : FragmentUserBinding? = null
    private val binding get() = _binding!!

    //auth
    lateinit var mAuth: FirebaseAuth
    lateinit var currentUser: FirebaseUser
    lateinit var databaseRef: FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference

    //top views
    lateinit var loadingDialog: LoadingDialog

    //user details
    lateinit var phone: String
    lateinit var pno: TextView
    lateinit var name: TextView
    lateinit var username: TextView
    lateinit var email: TextView
    lateinit var countryZipCode: TextView
    lateinit var address: TextView
    lateinit var profilePic: ImageView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profilePic = view.findViewById(R.id.imageProfile)
        pno = view.findViewById(R.id.phoneProfile)
        name = view.findViewById(R.id.nameProfile)
        username = view.findViewById(R.id.usernameProfile)
        email = view.findViewById(R.id.emailProfile)
        countryZipCode = view.findViewById(R.id.countryZipCodeProfile)
        address = view.findViewById(R.id.addressProfile)

        loadingDialog = LoadingDialog(this.requireActivity())

        //auth user
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        phone = mAuth.currentUser!!.phoneNumber.toString()

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("images/${currentUser.phoneNumber.toString()}")
        setProfilePic()

        //database for profile
        databaseRef = FirebaseDatabase.getInstance()
        val ref = databaseRef.reference.child("profile")

        //fetching from realtime database
        loadingDialog.startDialog()
        val tableRef = ref.child(currentUser.phoneNumber.toString())
        tableRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                loadingDialog.dismissDialog()
                Toast.makeText(
                    this@UserFragment.context,
                    "Error occurred while fetching the details! ",
                    Toast.LENGTH_LONG
                ).show()
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                //showing details in user profile
                val spacedPhone = phone.substring(0, 3) + " " + phone.substring(3)
                pno.text = spacedPhone

                val fullName =
                    snapshot.child("first_name").value.toString() + " " + snapshot.child("last_name").value.toString()
                name.text = fullName

                username.text = snapshot.child("username").value.toString()

                email.text = snapshot.child("email").value.toString()

                val countryPlusCode =
                    snapshot.child("country").value.toString() + " (" + snapshot.child("zip_code").value.toString() + ")"
                countryZipCode.text = countryPlusCode

                address.text = snapshot.child("address").value.toString()
                loadingDialog.dismissDialog()
            }
        })

    }

    private fun setProfilePic() {
        loadingDialog.startDialog()
        val localFile = File.createTempFile("tempFile", ".jpg");

        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profilePic.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(
                this.context,
                "Error occurred while fetching the profile picture! ",
                Toast.LENGTH_LONG
            ).show()
        }
        loadingDialog.dismissDialog()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}