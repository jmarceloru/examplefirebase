package com.jmarcelo.firebaseexample

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jmarcelo.firebaseexample.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var  response: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        response = registerForActivityResult(StartActivityForResult()){
            if (it.resultCode == RESULT_OK){
                val imageBitmap = it.data?.extras?.get("data") as Bitmap
                binding.imageView.setImageBitmap(imageBitmap)
                uploadPicture(imageBitmap)
            }
        }

        binding.btnTakephoto.setOnClickListener{
            throw RuntimeException("crash test")
            //dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            response.launch(takePictureIntent)
        }catch (ex: ActivityNotFoundException){
            Log.d("CAMARA","INSTALA CAMARA")
        }
    }

    private fun uploadPicture(bitmap: Bitmap){
        val storeRef = FirebaseStorage.getInstance().reference
        val imageRef = storeRef.child("imagenes/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)

        uploadTask.continueWithTask{
            if (!it.isSuccessful){
                it.exception?.let { exception->
                    throw exception
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener{
            if (it.isSuccessful){
                val downloadUri = it.result.toString()
                FirebaseFirestore.getInstance()
                    .collection("cities")
                    .document("xxnfIUBcTCU40b0okNcG")
                    .update(mapOf("imageUrl" to downloadUri))
                Log.d("CAMARA",downloadUri)
            }
        }
    }

}