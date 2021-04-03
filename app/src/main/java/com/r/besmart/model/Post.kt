package com.r.besmart.model

import com.google.firebase.firestore.DocumentId

data class Post(

    @DocumentId var id:String? = null,

    var userid: String? = null,
    var time:Long?= null,
    var image: ArrayList<String>? = null,
    var description: String? = null,
    var category: String? = null,
    var accepted:Boolean? = null,
    var acceptedUser:String? = null,
    var answered:Boolean? = null,
    var answerid:String? = null
)