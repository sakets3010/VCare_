package com.example.vcare.helper

data class ChatChannelId(var between: List<Id> = listOf())

data class ChatChannelIdWrapper(val docId: String, val channel: ChatChannelId)