package com.r.besmart.model



data class Answer(

    var id:String? = null,
    var userid: String? = null,
    var time:Long?= null,
    var image: ArrayList<String>? = null,
    var description: String? = null,
    var categoryP: String? = null,
    var imageP: ArrayList<String>? = null,
    var descriptionP: String? = null
)