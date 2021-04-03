package com.r.besmart.detailScreen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.toObject
import com.r.besmart.model.Apply
import com.r.besmart.model.Post
import com.r.besmart.repository.FirebaseRepository
import java.lang.Exception


class DetailViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()

    var position = 0

    private val _loaded = MutableLiveData<Boolean>()
    val loaded: LiveData<Boolean>
        get() = _loaded

    var post = MutableLiveData<Post>()

    var postID: String? = null
    var applyID = ""
    var category: String? = null

    var images: ArrayList<Uri>? = null
    var apply = Apply()

    private val _submitAnswerButton = MutableLiveData<Boolean>()
    val submitAnswerButton: LiveData<Boolean>
        get() = _submitAnswerButton

    private val _viewAnswerButton = MutableLiveData<Boolean>()
    val viewAnswerButton: LiveData<Boolean>
        get() = _viewAnswerButton

    private val _isappliedForSolving = MutableLiveData<Boolean>()
    val isappliedForSolving: LiveData<Boolean>
        get() = _isappliedForSolving

    private val _isAnswered = MutableLiveData<Boolean>()
    val isAnswered: LiveData<Boolean>
        get() = _isAnswered


    init {
        _submitAnswerButton.value = false
        _viewAnswerButton.value = false
        _isappliedForSolving.value = false
        images = ArrayList()
        postID = String()
        category = String()
    }

    fun getPostInfo() {
        val docRef =
            firebaseRepository.firestoreDB.collection("Posts").document(postID!!.toString())
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val post1 = documentSnapshot.toObject<Post>()

            post.value = post1!!

            _loaded.value = true

        }
    }
    fun isAnsweredF(){
        firebaseRepository.firestoreDB.collection("Posts")
            .document(postID!!)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val post = value!!.toObject<Post>()
                _isAnswered.value = post!!.answered!!
            }
    }
    fun applyUserForSolvingProblem() {
        firebaseRepository.getUserInfo()
        val apply = hashMapOf(
            "userid" to firebaseRepository.userid,
            "username" to firebaseRepository.name,
            "time" to System.currentTimeMillis(),
            "rating" to firebaseRepository.score,
            "accepted" to false,
            "postid" to post.value!!.id
        )
        firebaseRepository.firestoreDB.collection("Apply").add(apply)
    }

    fun acceptUser(id: String) {
        firebaseRepository.firestoreDB.collection("Apply").document(id)
            .update("accepted", true)
        firebaseRepository.firestoreDB.collection("Posts").document(postID!!)
            .update("accepted", true)

        firebaseRepository.firestoreDB.collection("Apply").document(id)
            .get().addOnSuccessListener { documentSnapshot ->
                val apply = documentSnapshot.toObject<Apply>()

                val userid = apply!!.userid
                firebaseRepository.firestoreDB.collection("Posts").document(postID!!)
                    .update("acceptedUser",userid)
            }
    }

    fun haveIAppliedForThisPost() {
        firebaseRepository.firestoreDB.collection("Apply")
            .whereEqualTo("postid", post.value!!.id)
            .whereEqualTo("userid", firebaseRepository.userid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot!!.isEmpty) {
                    _isappliedForSolving.value = false
                } else {
                    _isappliedForSolving.value = true
                    // go through all the results
                    for (doc in snapshot) {
                        applyID = doc.id
                        apply = doc.toObject<Apply>()
                    }
                }
            }
    }

    fun amIAccepted() {
        firebaseRepository.firestoreDB.collection("Apply")
            .whereEqualTo("postid", post.value!!.id)
            .whereEqualTo("userid", firebaseRepository.userid).whereEqualTo("accepted", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                _submitAnswerButton.value = !snapshot!!.isEmpty
            }

    }
    fun haveIacceptedAnyAnswer(){
        firebaseRepository.firestoreDB.collection("Posts")
            .document(postID!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val apply = snapshot!!.toObject<Apply>()
                _viewAnswerButton.value = apply!!.accepted!!

            }
    }

    fun deleteApply() {
        getPostInfo()
        if (post.value!!.acceptedUser == firebaseRepository.userid) {
            Log.d("USERID", firebaseRepository.userid.toString())
            Log.d("POSTID", postID!!)
            firebaseRepository.firestoreDB.collection("Posts").document(postID!!)
                .update("accepted", false)
            firebaseRepository.firestoreDB.collection("Posts").document(postID!!)
                .update("answered", false)
            firebaseRepository.firestoreDB.collection("Posts").document(postID!!)
                .update("acceptedUser", null)
            firebaseRepository.firestoreDB.collection("Posts").document(postID!!)
                .update("answerid", null)
            try{firebaseRepository.firestoreDB.collection("Answers").document(post.value!!.answerid!!)
                .delete()
            }catch (e:Exception){}
        }
        firebaseRepository.firestoreDB.collection("Apply").document(applyID)
            .delete()

    }

    fun increment() {
        if (position < post.value!!.image!!.size - 1) {
            position++
        }
    }

    fun decrement() {
        if (position > 0) {
            position--
        }
    }
}