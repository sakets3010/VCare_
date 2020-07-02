package com.example.vcare

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.vcare.Notifications.Token
import com.example.vcare.databinding.FragmentHomeBinding
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.home_list.view.*


class fragment_home : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)

        binding.homeRecycler.adapter = adapter
        binding.homeRecycler.addItemDecoration(DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener { item, view ->

            val intent = Intent(requireContext(),ChatLogActivity::class.java)

            val row = item as HomeItem

            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartner)

            startActivity(intent)
        }
        listenForNewMessage()

        updateToken(FirebaseInstanceId.getInstance().token)
        
        return binding.root
    }

    private fun updateToken(token: String?) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = token?.let { Token(it) }
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    val adapter = GroupAdapter<ViewHolder>()

    val latestMessagesMap = HashMap<String, ChatMessage>()

    private fun listenForNewMessage() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]=chatMessage
                adapter.clear()
                refreshMessages()
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]=chatMessage
                adapter.clear()
                refreshMessages()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun refreshMessages() {
        latestMessagesMap.values.forEach {

            adapter.add(HomeItem(it))
        }
    }

    class HomeItem(val chatMessage:ChatMessage): Item<ViewHolder>() {

        var chatPartner:User?=null
        override fun getLayout(): Int {
            return  R.layout.home_list
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.home_latestMessage.text=chatMessage.text
            val chatPartnerId:String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }
            else{chatPartnerId = chatMessage.fromId}
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartner = snapshot.getValue(User::class.java)
                    viewHolder.itemView.home_username.text = chatPartner?.username
                    val targetImage =  viewHolder.itemView.home_profile
                    Picasso.get().load(chatPartner?.profileImageUrl).into(targetImage)

                }


            })
        }
    }
}