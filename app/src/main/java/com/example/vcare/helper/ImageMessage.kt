package com.example.vcare.helper

import java.util.*

data class ImageMessage(val imagepath:String,val time: Date,val fromId:String,val toId:String){
    constructor():this("",Date(0),"","")
}