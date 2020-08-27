package com.example.vcare.chatLog

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vcare.Notifications.*
import com.example.vcare.R
import com.example.vcare.helper.*
import com.example.vcare.home.HomeActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatLogViewmodel:ViewModel() {
    private var status:MutableLiveData<User> = MutableLiveData()
    private lateinit var docId:String
    private lateinit var Id:String
    var notify = false
    val adapter = GroupAdapter<ViewHolder>()

    fun evaluateStatus(user:User?):LiveData<User>{
        if (user != null) {
            Firebase.firestore.collection("Users").document(user.uid).addSnapshotListener { snap, _ ->
                val details = snap?.toObject(User::class.java)
                status.value = details
            }
        }
        return status
    }

    fun returnUser(user: User?):User?{
        return user
    }

    private fun getDateTime(s: String): String? {
        return try {
            val sdf = SimpleDateFormat("EEE,hh:mmaa", Locale.getDefault())
            val netDate = Date(s.toLong() * 1000)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

     fun listenForMessages(user:User) {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user.uid
        val db = Firebase.firestore
        val betweenList = mutableListOf(Id(fromId),Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }
        db.collection("ChatChannels").whereEqualTo("between", sortedList).get().addOnSuccessListener{documents ->
            for (document in documents) {
                docId = document.id
                adapter.clear()
                listener(docId,user)
            }
        }
            .addOnFailureListener { exception ->
                Log.w("ChatLogActivity", "Error getting documents: ", exception)
            }
    }
     var isScrollable:MutableLiveData<Boolean> = MutableLiveData()
     private fun listener(docId: String, user: User){
        FirebaseAuth.getInstance().uid
         user.uid
        Firebase.firestore.collection("ChatChannels").document(docId).collection("Messages").orderBy("timestamp",
            Query.Direction.ASCENDING).addSnapshotListener { snap, _ ->
            if (snap != null) {
                adapter.clear()
                for(doc in snap.documents){
                    Log.d("listen","called")
                    val chatMessages = doc.toObject(ChatMessage::class.java)
                    Log.d("listen","chatmessage:$chatMessages")
                    if (chatMessages != null) {
                        if (chatMessages.fromId == FirebaseAuth.getInstance().uid) {
                            val currentUser = HomeActivity.currentUser
                            Log.d("listen","inside display loop")
                            adapter.add(
                                ChatToItem(
                                chatMessages.text,
                                chatMessages.url,
                                currentUser,
                                getDateTime(chatMessages.timestamp.toString())
                            )
                            )


                        } else {
                            adapter.add(ChatFromItem(chatMessages.text,chatMessages.url,user,getDateTime(chatMessages.timestamp.toString())))
                        }
                    }
                    isScrollable.value = true
                }
            }
        }
    }
    fun updateTypingStatus(status:Long){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val db = Firebase.firestore
        db.collection("Users").document(uid).update(
            mapOf(
                "status" to status
            )
        )
    }
    fun performSendMessage(user: User?,text:String,apiService: ApiService?) {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
        val time = System.currentTimeMillis() / 1000
        val chatMessage = ChatMessage(
            text, fromId!!,
            toId!!, time
        )
        val betweenList = mutableListOf(Id(fromId),Id(toId))
        val sortedList = betweenList.sortedBy { it.Id }
        val firebaseDB = Firebase.firestore
        firebaseDB.collection("ChatChannels").whereEqualTo("between", sortedList)
            .get().addOnSuccessListener { documents ->
                if(!(documents.isEmpty)){
                    for(document in documents){
                        if (document.exists()) {
                            Id = document.id
                            addMessage(Id,chatMessage,user)
                            return@addOnSuccessListener
                        }
                    }
                }
                firebaseDB.collection("ChatChannels").add(ChatChannelId(sortedList)).addOnSuccessListener { doc->
                    Id = doc.id
                    addMessage(Id,chatMessage,user)
                    return@addOnSuccessListener
                }
            }
        //fcm
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore
        db.collection("Users").document(firebaseUser!!.uid).addSnapshotListener{ snapshot, e ->
            if (e != null) {
                Log.d("ChatLogActivity", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val userlocal = snapshot.toObject(User::class.java)
                if(notify)
                {
                    sendNotifications(toId,userlocal!!.username,chatMessage.text,apiService!!)
                }
                notify = false

            } else {
                Log.d("ChatLogActivity", "Current data: null")
            }
        }
    }
    private fun addMessage(Id: String,chatMessage: ChatMessage,user: User) {
        Firebase.firestore.collection("ChatChannels").document(Id).
        collection("Messages").add(chatMessage).addOnSuccessListener {
            listenForMessages(user)
        }
    }
     private fun sendNotifications(toId: String?, username: String, text: String, apiService:ApiService) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(toId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                //does nothing
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnapshot in snapshot.children){
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val token: Token? = datasnapshot.getValue(Token::class.java)
                    val fromId = FirebaseAuth.getInstance().uid
                    val chatMessage = toId?.let {
                        ChatMessage(text,fromId!!,
                            it,System.currentTimeMillis()/1000)
                    }

                    val data = Data(firebaseUser!!.uid
                        , R.mipmap.ic_launcher,
                        "${username}:${chatMessage!!.text}"
                        ,"New Message from $username"
                        ,toId)
                    val sender = Sender(data, token?.getToken().toString())

                    apiService.sendNotification(sender).enqueue(object: Callback<MyResponse> {

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
    fun imageMessage(data:Intent, user:User, apiService: ApiService){
        val fileUri = data.data
        val storageReference = FirebaseStorage.getInstance().reference.child("chat_images")
        val ref = FirebaseDatabase.getInstance().reference
        val messageId  =ref.push().key
        val filePath = storageReference.child("$messageId.jpg")

        val uploadTask: StorageTask<*>
        uploadTask = filePath.putFile(fileUri!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if(!task.isSuccessful){
                task.exception?.let{
                    throw it
                }
            }
            return@Continuation filePath.downloadUrl
        }).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                val toId = user.uid
                val time  =System.currentTimeMillis()/1000
                val fromId = FirebaseAuth.getInstance().uid
                val chatMessage = ChatMessage(fromId = fromId!!,
                    toId = toId,timestamp = time,url = url)
                val firebaseDB = Firebase.firestore
                val betweenList = mutableListOf(Id(fromId),Id(toId))
                val sortedList = betweenList.sortedBy { it.Id }
                val db = Firebase.firestore
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                firebaseDB.collection("ChatChannels").whereEqualTo("between",sortedList)
                    .get().addOnSuccessListener{ documents->
                        if(documents!==null){
                            for (document in documents){
                                firebaseDB.collection("ChatChannels").document(document.id).
                                collection("Messages").add(chatMessage).addOnSuccessListener {
                                    if (firebaseUser != null) {
                                        db.collection("Users").document(firebaseUser.uid).addSnapshotListener { snapshot, e ->
                                            if (e != null) {
                                                Log.w("ChatLogActivity", "Listen failed.", e)
                                                return@addSnapshotListener
                                            }
                                            if (snapshot != null && snapshot.exists()) {
                                                val usersnap = snapshot.toObject(User::class.java)
                                                if(notify)
                                                {
                                                    sendNotifications(toId,usersnap!!.username,"sent you an image",
                                                        apiService
                                                    )
                                                }
                                                notify = false
                                            } else {
                                                Log.d("ChatLogActivity", "Current data: null")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            firebaseDB.collection("ChatChannels").add(ChatChannelId(sortedList)).addOnSuccessListener {documentReference->
                                firebaseDB.collection("ChatChannels").document(documentReference.id).
                                collection("Messages").add(chatMessage).addOnSuccessListener {
                                    if (firebaseUser != null) {
                                        db.collection("Users").document(firebaseUser.uid).addSnapshotListener { snapshot, e ->
                                            if (e != null) {
                                                Log.w("ChatLogActivity", "Listen failed.", e)
                                                return@addSnapshotListener
                                            }
                                            if (snapshot != null && snapshot.exists()) {
                                                val usersnap = snapshot.toObject(User::class.java)
                                                if(notify)
                                                {
                                                    sendNotifications(toId,usersnap!!.username,"sent you an image",
                                                        apiService
                                                    )
                                                }
                                                notify = false
                                            } else {
                                                Log.d("ChatLogActivity", "Current data: null")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }