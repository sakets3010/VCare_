package com.example.vcare.notifications

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAkYu8png:APA91bF-cMpiyWgeQ0QeHK-nAnn2QLVahhK4TiI7BaiPUsWE3qxrNtc84bdsgBHImM30uuPI4mVHZ6MAbw3nvEU7VZrAm9WnlyKEijWjpV7AvRfs8k22g5YtpCZclKFN9sAznTCtt_RE"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender): Call<MyResponse>
}