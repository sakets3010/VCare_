package com.example.vcare.chatLog.paging

import android.util.Log
import androidx.paging.PagingSource
import com.example.vcare.helper.ChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await


class FirestorePagingSource(
    private val db: FirebaseFirestore,
    private val docId: String
) : PagingSource<QuerySnapshot, ChatMessage>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, ChatMessage> {
        return try {
            Log.d("return","docId:$docId")
            val currentPage = params.key ?: db.collection("ChatChannels").document(docId).collection("Messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            Log.d("return","chatmessage1:${currentPage.toObjects(ChatMessage::class.java)}")
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            Log.d("return","last_visible:${lastDocumentSnapshot}")

            val nextPage = db.collection("ChatChannels").document(docId).collection("Messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()

            Log.d("return","chatmessage2:${currentPage.toObjects(ChatMessage::class.java)}")
            LoadResult.Page(
                data = currentPage.toObjects(ChatMessage::class.java),
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}