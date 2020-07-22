package com.example.vcare.helper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid:String="",val username:String="",val profileImageUrl:String="",val status:String="",val typingStatus:String="no-one"):Parcelable