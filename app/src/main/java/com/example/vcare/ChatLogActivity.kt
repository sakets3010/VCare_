package com.example.vcare

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import com.example.vcare.ChatLogActivity.typingStatus.Companion.updateTypingStatus
import com.example.vcare.HomeActivity.Companion.currentUser
import com.example.vcare.HomeActivity.Status.Companion.updateStatus
import com.example.vcare.Notifications.*
import com.example.vcare.helper.ApiService
import com.example.vcare.helper.ChatMessage
import com.example.vcare.helper.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_row_from.view.*
import kotlinx.android.synthetic.main.chat_row_to.*
import kotlinx.android.synthetic.main.chat_row_to.view.*
import kotlinx.android.synthetic.main.chat_row_to.view.textView_to_row
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IndexOutOfBoundsException
import kotlin.coroutines.coroutineContext

class ChatLogActivity : AppCompatActivity() {
    var mChatList:List<ChatMessage>?=null
    val adapter = GroupAdapter<ViewHolder>()
    var notify = false
    var apiService : ApiService?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("chat-list","oncreate called")



        setContentView(R.layout.activity_chat_log)

        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        val toId = user?.uid

        supportActionBar?.title = user?.username

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

                if(edittext_chat_log.text.toString().trim().isNotEmpty()){
                    Log.d("type-indicator","update called")
                    if (toId != null) {
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

   private fun listenForMessages() {
       adapter.notifyDataSetChanged()
       val listMessages = mutableListOf<ChatMessage>()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val time = System.currentTimeMillis()/1000
        val ref = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId!!).child(toId!!)
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chatMessages = snapshot.getValue(ChatMessage::class.java)
                if (chatMessages != null) {
                    if (chatMessages.fromId ==FirebaseAuth.getInstance().uid)
                    {
                        val currentUser = HomeActivity.currentUser
                        adapter.add(ChatToItem(chatMessages.text,chatMessages.url,currentUser,chatMessages.timestamp.toString(),fromId,toId))
                    }
                    else
                    {
                        val userTo = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatFromItem(chatMessages.text,chatMessages.url,userTo))

                    }
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
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }


        })
    }
    private fun checkTypingStatus() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for(ds in snapshot.children){

                    val typingStatus = ds.child("typingStatus").getValue()

                    if(typingStatus==fromId)
                    {
                        TODO()
                       //animation goes here
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

    class typingStatus(){
        companion object{
            fun updateTypingStatus(status:String){
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

                val hashMap = HashMap<String,Any>()
                hashMap["typingStatus"] = status
                Log.d("type-indicator","status:${status}")
                ref!!.updateChildren(hashMap)
            }
        }
    }

    private fun performSendMessage() {

        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid
        val time  =System.currentTimeMillis()/1000
        val ref = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId!!).child(toId!!).child(time.toString())
        val to_ref = FirebaseDatabase.getInstance().reference.child("user-messages").child(toId).child(fromId).child(time.toString())

        val chatMessage = toId?.let {
            ChatMessage(ref.key!!,text,fromId!!,
                it,System.currentTimeMillis()/1000)
        }

        ref.setValue(chatMessage).addOnSuccessListener {
            edittext_chat_log.text.clear()
            chat_log_recycler.scrollToPosition(adapter.itemCount -1)
        }
        to_ref.setValue(chatMessage)



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
                        ,"New Message from ${username}"
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

                                }

                            }

                        }
                    })
                }


            }


        })
    }




    class ChatFromItem(val text:String="",val url:String="",val user:User?):Item<ViewHolder>(){

        override fun getLayout(): Int {
            return R.layout.chat_row_from
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {


            viewHolder.setIsRecyclable(false)
            if(url=="")
            {viewHolder.itemView.textView_from_row.text = text}
            else if(url !== "")
            {
                viewHolder.itemView.textView_from_row.visibility=View.GONE
                viewHolder.itemView.image_from_cover.visibility=View.VISIBLE
                viewHolder.itemView.image_from.visibility = View.VISIBLE
                Picasso.get().load(url).into(viewHolder.itemView.image_from)

                viewHolder.itemView.image_from.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Image","Cancel"
                    )

                    val builder:AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options,DialogInterface.OnClickListener {dialog, which ->
                        if (which==0){
                            val intent = Intent(builder.context,ViewFullImageActivity::class.java)
                            intent.putExtra("url",url)
                            builder.context.startActivity(intent)
                        }

                    })
                    builder.show()
                }
            }


            val uri = user?.profileImageUrl
            val targetImage = viewHolder.itemView.from_profile
            Picasso.get().load(uri).into(targetImage)

        }
    }


    class ChatToItem(val text:String="",val urlimg:String="", val user: User?,time:String,from:String,to:String):Item<ViewHolder>(){

        var fromId:String
        var toId:String
        var time:String

        init {
            this.fromId = from
            this.toId = to
            this.time = time

        }
        override fun getLayout(): Int {
            return R.layout.chat_row_to
        }

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.setIsRecyclable(false)
            if(urlimg=="")
            {
                viewHolder.itemView.textView_to_row.text =text
                viewHolder.itemView.textView_to_row.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete Message","Cancel"
                    )

                    val builder:AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options,DialogInterface.OnClickListener {dialog, which ->
                        if(which == 0){
                            deleteMessage(position,viewHolder)

                        }
                    })
                    builder.show()
                }}
            else if(urlimg !== "")
            {
                viewHolder.itemView.textView_to_row.visibility=View.GONE
                viewHolder.itemView.image_to_cover.visibility=View.VISIBLE
                viewHolder.itemView.image_to.visibility = View.VISIBLE
                Picasso.get().load(urlimg).into(viewHolder.itemView.image_to)
                viewHolder.itemView.image_to.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Image","Delete Message","Cancel"
                    )

                    val builder:AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
                    builder.setTitle("what now?")
                    builder.setItems(options,DialogInterface.OnClickListener {dialog, which ->
                        if (which==0){
                            val intent = Intent(builder.context,ViewFullImageActivity::class.java)
                            intent.putExtra("url",urlimg)
                            builder.context.startActivity(intent)
                        }
                        else if(which == 1){
                            deleteMessage(position,viewHolder)
                        }
                    })
                    builder.show()
                }
            }

            val uri = user?.profileImageUrl
            val targetImage = viewHolder.itemView.to_profile
            Picasso.get().load(uri).into(targetImage)
        }

        @SuppressLint("SetTextI18n")
        private fun deleteMessage(position: Int, viewHolder: ViewHolder) {
            val hashMap = HashMap<String,Any>()
            val progressBar = ProgressDialog(viewHolder.itemView.context)
            progressBar.setMessage("deleting message..")
            progressBar.show()
            val deletionrefupdate = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId).child(toId).child(time)
            val to_deletionrefupdate = FirebaseDatabase.getInstance().reference.child("user-messages").child(toId).child(fromId).child(time)
            if(urlimg=="")
            {
                hashMap["text"] = "This message was deleted"

            }
            else{
                hashMap["url"] = ""
                hashMap["text"] = "This message was deleted"
            }
            deletionrefupdate.updateChildren(hashMap)
            to_deletionrefupdate.updateChildren(hashMap)
            notifyChanged()






            Toast.makeText(viewHolder.itemView.context,"deleted message",Toast.LENGTH_SHORT).show()

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==438 && resultCode ==RESULT_OK && data!!.data!==null && data !==null){
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Loading Image..")
            progressBar.show()

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
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                    val toId = user?.uid
                    val time  =System.currentTimeMillis()/1000
                    val fromId = FirebaseAuth.getInstance().uid
                    val refer = FirebaseDatabase.getInstance().reference.child("user-messages").child(fromId!!).child(toId!!).child(time.toString())
                    val to_ref = FirebaseDatabase.getInstance().reference.child("user-messages").child(toId).child(fromId).child(time.toString())

                    val chatMessage = toId?.let {
                        ChatMessage(refer.key!!,"",fromId!!,
                            it,System.currentTimeMillis()/1000,url)
                    }

                    refer.setValue(chatMessage).addOnCompleteListener {task->
                        if(task.isSuccessful)
                        {
                            progressBar.dismiss()
                            val firebaseUser = FirebaseAuth.getInstance().currentUser

                            val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

                            val toId = user?.uid

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
                                        sendNotifications(toId,user!!.username,"sent you an image")
                                    }
                                    notify = false
                                }


                            })


                        }
                    }
                    to_ref.setValue((chatMessage))

                }
            }
        }
    }
}



