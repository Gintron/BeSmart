package com.r.besmart.detailScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.toObject
import com.r.besmart.model.Answer
import com.r.besmart.repository.FirebaseRepository

class ViewAnswerViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()
    var answer = MutableLiveData<Answer>()
    var position = 0
    var answerid:String? = null

    private val _loaded = MutableLiveData<Boolean>()
    val loaded: LiveData<Boolean>
        get() = _loaded

    fun getAnswer(){
        val docRef =
            firebaseRepository.firestoreDB.collection("Answers").document(answerid!!)
        docRef.get().addOnSuccessListener {documentSnapshot->
            val answer1 = documentSnapshot.toObject<Answer>()
                answer.value = answer1!!
            _loaded.value = true
        }
    }

    fun increment() {
        if (position < answer.value!!.image!!.size - 1) {
            position++
        }
    }

    fun decrement() {
        if (position > 0) {
            position--
        }
    }
}