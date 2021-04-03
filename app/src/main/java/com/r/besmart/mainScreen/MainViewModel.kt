package com.r.besmart.mainScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.r.besmart.FirebaseUserLiveData
import com.r.besmart.repository.FirebaseRepository


class MainViewModel  : ViewModel(){
    val firebaseRepository = FirebaseRepository()
    enum class AuthenticationState{
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map{ user->
        if(user != null){
            AuthenticationState.AUTHENTICATED
        }else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    fun checkUserExistsInDatabase() {
        val docRef = firebaseRepository.firestoreDB.collection("user").document(firebaseRepository.userid.toString())
        docRef.get()
            .addOnSuccessListener { document->
                if(document.exists()){
                }else{
                    val user = hashMapOf(
                        "userid" to firebaseRepository.userid.toString(),
                        "username" to FirebaseAuth.getInstance().currentUser?.displayName,
                        "score" to 0
                    )
                    firebaseRepository.firestoreDB.collection("user").document(firebaseRepository.userid.toString()).set(user)
                }
            }
    }
}