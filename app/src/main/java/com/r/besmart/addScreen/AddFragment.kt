package com.r.besmart.addScreen

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.r.besmart.R
import com.r.besmart.databinding.FragmentAddBinding


class AddFragment : Fragment() {

    private lateinit var viewModel: AddViewModel

    //request code to pick image(s)
    private val PICK_IMAGES_CODE = 0

    private lateinit var binding: FragmentAddBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(AddViewModel::class.java)
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_add, container, false)
        viewModel.id = arguments?.getString("id")
        if(viewModel.id != null)
            viewModel.amIallowedToAnswer()
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(viewModel.id != null){
            binding.categoryTv.visibility = View.GONE
            binding.categoryText.visibility = View.GONE
            binding.line.visibility = View.GONE
        }
        if (viewModel.images!!.isEmpty()) {
            binding.imageSwitcher.visibility = View.GONE
            binding.next.visibility = View.GONE
            binding.before.visibility = View.GONE
        }
        binding.imageSwitcher.setFactory {
            ImageView(context)
        }
        binding.pickImage.setOnClickListener {
            pickImagesIntent()
        }
        binding.next.setOnClickListener {
            if (viewModel.position < viewModel.images!!.size - 1) {
                viewModel.position++
                binding.imageSwitcher.setImageURI(viewModel.images!![viewModel.position])
            } else {
                //no more images
            }
        }
        binding.before.setOnClickListener {
            if (viewModel.position > 0) {
                viewModel.position--
                binding.imageSwitcher.setImageURI(viewModel.images!![viewModel.position])
            } else {
                //no more images
            }
        }
        binding.category.setOnClickListener {
            val listItems = arrayOf("Mathematics", "Physics")
            val mBuilder = AlertDialog.Builder(context,
                R.style.DialogTheme
            )
            mBuilder.setTitle("Choose category")
            mBuilder.setSingleChoiceItems(listItems, -1) { dialogInterface, i ->
                binding.categoryText.text = listItems[i]
                viewModel.category = listItems[i]
                dialogInterface.dismiss()
            }
            // Set the neutral/cancel button click listener
            mBuilder.setNeutralButton("Cancel") { dialog, which ->
                // Do something when click the neutral button
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()

        }
        binding.submitBtn.setOnClickListener {
            val description = binding.description.text.toString().trim()
            if(description.isEmpty()){
                Toast.makeText(context, "Description needed", Toast.LENGTH_SHORT).show()
            }else if(viewModel.images!!.isEmpty()){
                Toast.makeText(context, "Image needed", Toast.LENGTH_SHORT).show()
            }else if(viewModel.category!!.isEmpty() && viewModel.id == null){
                Toast.makeText(context, "Please select category", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.d("id", viewModel.id.toString())
                Log.d("boolean", viewModel.canIAnswer.toString())
                if(viewModel.id == null){
                    Toast.makeText(context, "Uploading...please wait", Toast.LENGTH_LONG).show()
                viewModel.uploadPost(context, description)
                }

                else if(viewModel.id != null && viewModel.canIAnswer.value!!){
                    Toast.makeText(context, "Uploading...please wait", Toast.LENGTH_LONG).show()
                    viewModel.uploadPost(context, description)
                }else{
                    Toast.makeText(context, "You are not allowed to answer this post anymore", Toast.LENGTH_LONG).show()
                }
            }


        }
        viewModel.uploadedSuccessfully.observe(viewLifecycleOwner, Observer<Boolean> {success ->
            if (success && viewModel.id == null) {
                Toast.makeText(
                    context,
                    "Post uploaded",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_addFragment_to_mainFragment)
            }else if(success && viewModel.id != null){
                Toast.makeText(
                    context,
                    "Answer uploaded",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun pickImagesIntent() {
        viewModel.images!!.clear()
        viewModel.count = 0
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data!!.clipData != null) {
                    showImageAndControls()
                    //picked multiple images

                    //get number of images
                    viewModel.count = data.clipData!!.itemCount
                    for (i in 0 until viewModel.count) {
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        //add image to list
                        viewModel.images!!.add(imageUri)

                    }
                    //set first image from list to image switcher
                    binding.imageSwitcher.setImageURI(viewModel.images!![0])
                    viewModel.position = 0
                } else {
                    showImageAndControls()
                    //picked single image

                    val imageUri = data.data
                    if (imageUri != null) {
                        viewModel.images!!.add(imageUri)
                        viewModel.count = 1
                    }
                    //set image to switcher
                    binding.imageSwitcher.setImageURI(imageUri)
                    viewModel.position = 0
                }
            }
        }
    }

    private fun showImageAndControls() {
        binding.imageSwitcher.visibility = View.VISIBLE
        binding.next.visibility = View.VISIBLE
        binding.before.visibility = View.VISIBLE
    }

}


