package com.example.myapplication.home

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.LoadingDialog
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.home.messages.MessagesAdapter
import com.example.myapplication.home.messages.MessagesList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    lateinit var userProfilePic: ImageView
    lateinit var imgString: String
    //recyclerView
    lateinit var messagesRecyclerView: RecyclerView
    val messagesLists = arrayListOf<MessagesList>()

    //user details
    lateinit var phone: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        loadingDialog = LoadingDialog(this.requireActivity())
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView)
        userProfilePic = view.findViewById(R.id.userProfilePic)

        loadingDialog.startDialog()
        //auth user
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        phone = mAuth.currentUser!!.phoneNumber.toString()
        databaseRef = FirebaseDatabase.getInstance()


        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference.child("images/${currentUser.phoneNumber.toString()}")

        messagesRecyclerView.setHasFixedSize(true)
        messagesRecyclerView.layoutManager = LinearLayoutManager(context)

        val ref = databaseRef.reference.child("profile")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for (sp in snapshot.children) {
                    val getMobile : String = sp.key!!
                    if (getMobile != phone){
                        val name : String = sp.child("username").value.toString()
                        val dpUrl = sp.child("profile_pic_url").value.toString()
                        val messagesList = MessagesList(name, getMobile, "", dpUrl, 0)
                        messagesLists.add(messagesList)
                    }
                }
                messagesRecyclerView.adapter = MessagesAdapter(messagesLists, context!!)
            }
        })
        //setting up image
        val imgSharedPref = requireActivity().applicationContext.getSharedPreferences("pic", Context.MODE_PRIVATE)
        imgString = imgSharedPref.getString("imgPath", null).toString()
        val bm = BitmapFactory.decodeFile(imgString)
        userProfilePic.setImageBitmap(bm)

        loadingDialog.dismissDialog()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }
}