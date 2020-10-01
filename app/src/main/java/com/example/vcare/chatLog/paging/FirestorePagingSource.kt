package com.example.vcare.chatLog.paging

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

    companion object {
        const val PAGING_LIMIT = 20L
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, ChatMessage> {
        return try {

            val currentPage =
                params.key ?: db.collection("ChatChannels").document(docId).collection("Messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(PAGING_LIMIT)
                    .get()
                    .await()

            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            val nextPage = db.collection("ChatChannels").document(docId).collection("Messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(PAGING_LIMIT)
                .startAfter(lastDocumentSnapshot)
                .get()
                .await()

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