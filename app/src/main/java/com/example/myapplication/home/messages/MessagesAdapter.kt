package com.example.myapplication.home.messages

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessagesAdapter(var messagesLists: List<MessagesList>, var context: Context) :
    RecyclerView.Adapter<MessagesAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.messages_adapter_layout, null))
    }

    override fun onBindViewHolder(holder: MessagesAdapter.MyViewHolder, position: Int) {
        val list2 = messagesLists[position]
        if(!list2.profilePic.isNullOrEmpty()){
            Picasso.get().load(list2.profilePic).into(holder.profilePic)
        }
        holder.name.text = list2.name
        holder.lastMessage.text = list2.lastMessage
        if(list2.unseenMessages == 0){
            holder.unseenMessage.visibility = View.INVISIBLE
        }
        else{
            holder.unseenMessage.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return messagesLists.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var profilePic : CircleImageView = itemView.findViewById(R.id.profilePic)
        var name : TextView = itemView.findViewById(R.id.name)
        var lastMessage : TextView = itemView.findViewById(R.id.lastMessage)
        var unseenMessage : TextView = itemView.findViewById(R.id.unseenMessages)
    }

}