package com.r.besmart.addScreen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.r.besmart.model.Answer
import com.r.besmart.model.Apply
import com.r.besmart.model.Post
import com.r.besmart.repository.FirebaseRepository
import java.io.ByteArrayOutputStream
import java.io.IOException


class AddViewModel : ViewModel() {
    var id: String? = null
    var images: ArrayList<Uri>? = null
    var category: String? = null
    private val _canIAnswer = MutableLiveData<Boolean>()
    val canIAnswer: LiveData<Boolean>
        get() = _canIAnswer
    //current position of selected image
    var position = 0;

    //number of images
    var count = 0

    private val firebaseRepository = FirebaseRepository()

    private var storageReference = FirebaseStorage.getInstance().getReference()

    private var imageList: ArrayList<String>? = null
    var number: Int = 0

    private val _uploadedSuccessfully = MutableLiveData<Boolean>()
    val uploadedSuccessfully: LiveData<Boolean>
        get() = _uploadedSuccessfully


    init {
        _canIAnswer.value = false
        category = ""
        images = ArrayList()
        imageList = ArrayList()
    }

    fun uploadPost(context: Context?, description: String) {
        images!!.forEach { image ->
            val imageRef = storageReference.child(
                System.currentTimeMillis().toString()
                        + "." + image.lastPathSegment
            )

            var exif: ExifInterface? = null
            val inputStream = context!!.getContentResolver().openInputStream(image)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    exif = ExifInterface(inputStream!!)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val orientation = exif!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            val actualImage1 =
                BitmapFactory.decodeStream(
                    context.getContentResolver()?.openInputStream(image)
                )
            val bmRotated = rotateBitmap(actualImage1, orientation)
            val baos = ByteArrayOutputStream()
            bmRotated!!.compress(Bitmap.CompressFormat.JPEG, 30, baos)
            val finalImage = baos.toByteArray()


            val uploadTask = imageRef.putBytes(finalImage)

            uploadTask.addOnSuccessListener {
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener { uri ->
                    imageList!!.add(uri.toString())
                    number++
                    if (number == count) {

                        if (id == null) {
                            val post = Post(
                                "blabla",
                                firebaseRepository.userid.toString(),
                                System.currentTimeMillis(),
                                imageList,
                                description,
                                category,
                                false,
                                null,
                                false,
                                null
                            )
                            firebaseRepository.firestoreDB.collection("Posts").add(post)
                            _uploadedSuccessfully.value = true
                            imageList!!.clear()
                            number = 0
                        } else {
                            amIallowedToAnswer()
                            if(_canIAnswer.value!!) {
                                val answer = Answer(
                                    id,
                                    firebaseRepository.userid!!.toString(),
                                    System.currentTimeMillis(),
                                    imageList,
                                    description
                                )
                                firebaseRepository.firestoreDB.collection("Answers").add(answer)
                                    .addOnSuccessListener {
                                        firebaseRepository.firestoreDB.collection("Posts")
                                            .document(id!!).update("answered", true)
                                        firebaseRepository.firestoreDB.collection("Posts")
                                            .document(id!!).update("answerid", it.id)
                                    }

                                _uploadedSuccessfully.value = true
                                imageList!!.clear()
                                number = 0
                                count = 0
                            }
                        }
                    }
                }
            }
            uploadTask.addOnFailureListener {
                number = 0
                _uploadedSuccessfully.value = false
            }

            uploadTask.addOnCanceledListener {
                number = 0
                _uploadedSuccessfully.value = false
            }
        }

    }
    fun amIallowedToAnswer(){
        firebaseRepository.firestoreDB.collection("Posts")
            .document(id!!)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                val post = snapshot!!.toObject<Post>()
                _canIAnswer.value = post!!.acceptedUser == firebaseRepository.userid
            }
    }
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap? {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> return bitmap
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return try {
            val bmRotated = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
            bitmap.recycle()
            bmRotated
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }
    }
}