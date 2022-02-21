package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import com.example.myapplication.R

class LoadingDialog(var activity: Activity) {
    private lateinit var dialog: AlertDialog

    fun startDialog(){
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.custom_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun dismissDialog(){
        dialog.dismiss()
    }

}