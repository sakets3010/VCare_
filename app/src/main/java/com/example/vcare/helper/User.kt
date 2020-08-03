package com.example.vcare.helper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid:String="",val username:String="",val profileImageUrl:String="",val status:Long=102L,val category:String="",val bio:String=""):Parcelable