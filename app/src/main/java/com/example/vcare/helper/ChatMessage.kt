package com.example.vcare.helper

data class ChatMessage(val text: String="image", val fromId:String="", val toId:String="", val timestamp: Long = 0L, val url:String = "",val status:Boolean = false)
