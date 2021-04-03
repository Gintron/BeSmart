package com.r.besmart.profileScreen

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Query
import com.r.besmart.repository.FirebaseRepository


class MyAnsweredViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    var mPostsCollection: Query? = null

    init{
        val userid = firebaseRepository.userid.toString()
        mPostsCollection = firebaseRepository.firestoreDB.collection("Answers").whereEqualTo("userid", userid)
    }
}