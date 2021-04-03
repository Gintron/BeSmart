package com.r.besmart.profileScreen


import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.r.besmart.repository.FirebaseRepository

class AskedViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    var mPostsCollection: Query? = null

    init{
        val userid = firebaseRepository.userid.toString()
        mPostsCollection = firebaseRepository.firestoreDB.collection("Posts").whereEqualTo("userid", userid)
    }
}