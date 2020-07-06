package com.example.vcare.helper

data class ChatMessage(val id:String,val text: String="text",val fromId:String,val toId:String,val timestamp: Long,val url:String = ""){
    constructor():this("","","","",-1,"")
}