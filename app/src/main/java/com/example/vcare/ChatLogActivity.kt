package com.example.vcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.vcare.Notifications.*
import com.example.vcare.helper.ApiService
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.chat_row_to.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<ViewHolder>()
    var notify = false
    var apiService : ApiService?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        supportActionBar?.title = user?.username

        chat_log_recycler.adapter = adapter

        listenForMessages()

        send_button.setOnClickListener {
            notify = true
            performSendMessage()
        }

        apiService = Client.client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)


    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    if (chatMessage.fromId ==FirebaseAuth.getInstance().uid)
                    {   val currentUser = HomeActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.text,currentUser))}
                    else
                    {   val userTo = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatFromItem(chatMessage.text,userTo)) }
                }
                chat_log_recycler.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }


        })
    }
    private fun performSendMessage() {

        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val to_ref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val chatMessage = toId?.let {
            ChatMessage(ref.key!!,text,fromId!!,
                it,System.currentTimeMillis()/1000)
        }
        ref.setValue(chatMessage).addOnSuccessListener {
            edittext_chat_log.text.clear()
            chat_log_recycler.scrollToPosition(adapter.itemCount -1)
        }
        to_ref.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest_messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest_messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

        //fcm
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refer = FirebaseDatabase.getInstance().reference
            .child("users").child(firebaseUser!!.uid)
        refer.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if(notify)
                {
                    sendNotifications(toId,user!!.username,chatMessage!!.text)
                }
                notify = false
            }


        })


    }

    private fun sendNotifications(toId: String?, username: String, text: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(toId)

        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnapshot in snapshot.children){
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val token:Token? = datasnapshot.getValue(Token::class.java)
                    val user = snapshot.getValue(User::class.java)
                    val fromId = FirebaseAuth.getInstance().uid
                    val chatMessage = toId?.let {
                        ChatMessage(ref.key!!,text,fromId!!,
                            it,System.currentTimeMillis()/1000)
                    }
                    val data = Data(firebaseUser!!.uid
                        ,R.mipmap.ic_launcher,
                        "${username}:${chatMessage!!.text}"
                        ,"New Message"
                        ,toId)
                    val sender = Sender(data, token?.getToken().toString())

                    apiService!!.sendNotification(sender).enqueue(object:Callback<MyResponse>{

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                        override fun onResponse(
                            call: Call<MyResponse>,
                            response: Response<MyResponse>
                        ) {
                            if(response.code()==200){

                                if(response.body()!!.success!==1){

                                    Toast.makeText(this@ChatLogActivity,"Failed,Nothing happened",Toast.LENGTH_SHORT).show()
                                }

                            }

                        }
                    })
                }


            }


        })
    }
}

class ChatFromItem(val text:String,val user:User?):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_row_from
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.itemView.textView_from_row.text = text
        val uri = user?.profileImageUrl
        val targetImage = viewHolder.itemView.from_profile
        Picasso.get().load(uri).into(targetImage)

    }
}class ChatToItem(val text:String, val user: User?):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_row_to
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text = text
        val uri = user?.profileImageUrl
        val targetImage = viewHolder.itemView.to_profile
        Picasso.get().load(uri).into(targetImage)
    }
}