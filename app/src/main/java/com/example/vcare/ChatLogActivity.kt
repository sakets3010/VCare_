package com.example.vcare

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.example.vcare.ChatLogActivity.TypingStatus.Companion.updateTypingStatus
import com.example.vcare.HomeActivity.Status.Companion.updateStatus
import com.example.vcare.Notifications.*
import com.example.vcare.helper.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<ViewHolder>()
    var notify = false
    var apiService : ApiService?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)

        val toId = user?.uid

        toolbar_chat_log.text = user?.username

        Picasso.get().load(user?.profileImageUrl).into(chat_log_profile)

        if(user?.category=="Seeker")
        {
            category_text_chat_log.setBackgroundResource(R.drawable.rounded_bg_yellow_coloured)
            category_text_chat_log.setTextColor(Color.parseColor("#ffff00"))
        }
        category_text_chat_log.text=user?.category

        if(user?.status=="online")
        {
            online_indicator.setImageResource(R.drawable.online_color)
            typing_status_chat_log.text=getString(R.string.online)
        }
        else{
            online_indicator.setImageResource(R.drawable.hollow_circle)
            typing_status_chat_log.text=getString(R.string.away)
        }

        back_button_chat_log.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        chat_log_recycler.setHasFixedSize(true)

        chat_log_recycler.adapter = adapter

        listenForMessages()

        send_button.setOnClickListener {
            if(edittext_chat_log.text.toString()==""){
                edittext_chat_log.requestFocus()
                return@setOnClickListener
            }
            else{
                notify = true
                performSendMessage()
            }
        }
        edittext_chat_log.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(edittext_chat_log.text.toString().trim().isNotEmpty())
                    {
                       Log.d("type-indicator","update called")
                        if (toId != null)
                        {
                          updateTypingStatus(toId)
                          checkTypingStatus()
                        }
                }
                else{
                       Log.d("type-indicator","update not called")
                       updateTypingStatus("no-one")
                    }
            }


        })
        send_image.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"pick an image"),438)
        }

        apiService = Client.client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)


    }
    private fun getDateTime(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("EEE,hh:mmaa",Locale.getDefault())
            val netDate = Date(s.toLong() * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

   private fun listenForMessages() {
       adapter.notifyDataSetChanged()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId!!).child(toId!!)
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chatMessages = snapshot.getValue(ChatMessage::class.java)
                if (chatMessages != null) {
                    if (chatMessages.fromId ==FirebaseAuth.getInstance().uid)
                    {
                        val currentUser = HomeActivity.currentUser
                        adapter.add(ChatToItem(chatMessages.text,chatMessages.url,currentUser,getDateTime(chatMessages.timestamp.toString()),fromId,toId))
                    }
                    else
                    {
                        val userTo = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)
                        adapter.add(ChatFromItem(chatMessages.text,chatMessages.url,userTo,getDateTime(chatMessages.timestamp.toString())))
                    }
                }
                chat_log_recycler.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(error: DatabaseError) {
                //does nothing
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //does nothing
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //does nothing
            }


        })
    }
    private fun checkTypingStatus() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds in snapshot.children){
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val typingStatus = ds.child("typingStatus").value

                    if(uid==typingStatus)
                    {
                        typing_status_chat_log.text = getString(R.string.typing)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateStatus("online")
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
        updateTypingStatus("no-one")
    }

    class TypingStatus{
        companion object{
            fun updateTypingStatus(status:String){
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                val hashMap = HashMap<String,Any>()
                hashMap["typingStatus"] = status
                ref.updateChildren(hashMap)
            }
        }
    }

    private fun performSendMessage() {
        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)
        val toId = user?.uid
        val time  =System.currentTimeMillis()/1000
        val ref = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId!!).child(toId!!).child(time.toString())
        val toRef = FirebaseDatabase.getInstance().reference.child("user-messages").child(toId).child(fromId).child(time.toString())

        val chatMessage = ChatMessage(ref.key!!,text, fromId,
            toId,System.currentTimeMillis()/1000)

        ref.setValue(chatMessage).addOnSuccessListener {
            edittext_chat_log.text.clear()
            chat_log_recycler.scrollToPosition(adapter.itemCount -1)
        }
        toRef.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

        //fcm
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refer = FirebaseDatabase.getInstance().reference
            .child("users").child(firebaseUser!!.uid)
        refer.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                //does nothing
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                val userlocal = snapshot.getValue(User::class.java)
                if(notify)
                {
                    sendNotifications(toId,userlocal!!.username, chatMessage.text)
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
                //does nothing
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnapshot in snapshot.children){
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val token:Token? = datasnapshot.getValue(Token::class.java)
                    val fromId = FirebaseAuth.getInstance().uid
                    val chatMessage = toId?.let {
                        ChatMessage(ref.key!!,text,fromId!!,
                            it,System.currentTimeMillis()/1000)
                    }

                    val data = Data(firebaseUser!!.uid
                        ,R.mipmap.ic_launcher,
                        "${username}:${chatMessage!!.text}"
                        ,"New Message from $username"
                        ,toId)
                    val sender = Sender(data, token?.getToken().toString())

                    apiService!!.sendNotification(sender).enqueue(object:Callback<MyResponse>{

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                            //does nothing
                        }

                        override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>){
                            //does nothing
                        }
                    })
                 }
              }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==438 && resultCode ==RESULT_OK && data!!.data!==null && data !==null){
            setLoading()
            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("chat_images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId  =ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            val uploadTask:StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>>{task ->
                if(!task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener {task ->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    val user = intent.getParcelableExtra<User>(NewMessageFrag.USER_KEY)
                    val toId = user?.uid
                    val time  =System.currentTimeMillis()/1000
                    val fromId = FirebaseAuth.getInstance().uid
                    val refer = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId!!).child(toId!!).child(time.toString())
                    val toRef = FirebaseDatabase.getInstance().reference.child("user-messages").child(toId).child(fromId).child(time.toString())

                    val chatMessage = ChatMessage(refer.key!!,"", fromId,
                        toId,System.currentTimeMillis()/1000,url)

                    refer.setValue(chatMessage).addOnCompleteListener {tasklocal->
                        if(tasklocal.isSuccessful)
                        {
                            setNotLoading()
                            val firebaseUser = FirebaseAuth.getInstance().currentUser
                            val reference = FirebaseDatabase.getInstance().reference
                                .child("users").child(firebaseUser!!.uid)
                            reference.addValueEventListener(object:ValueEventListener{
                                override fun onCancelled(error: DatabaseError) {
                                    //does nothing
                                }
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val usersnap = snapshot.getValue(User::class.java)
                                    if(notify)
                                    {
                                        sendNotifications(toId,usersnap!!.username,"sent you an image")
                                    }
                                    notify = false
                                }

                            })
                        }
                    }
                    toRef.setValue((chatMessage))
                }
            }
        }
    }

    private fun setLoading() {
        image_progressBar.visibility = View.VISIBLE
    }
    private fun setNotLoading() {
        image_progressBar.visibility = View.GONE
    }

}



