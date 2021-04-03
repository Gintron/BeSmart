package com.r.besmart.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.r.besmart.model.User

class FirebaseRepository {
    var firestoreDB = FirebaseFirestore.getInstance()
    var userid = FirebaseAuth.getInstance().currentUser?.uid
    var name = FirebaseAuth.getInstance().currentUser?.displayName
    var score = 0
    //save user to firebase
    fun getUserInfo() {
        val docRef = firestoreDB.collection("user").document(userid!!)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val user = documentSnapshot.toObject<User>()
            if (user != null) {
                score = user.score!!
            }
        }
    }
}