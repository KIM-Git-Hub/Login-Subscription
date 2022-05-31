package com.jaeyoung.studuyapp03

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jaeyoung.studuyapp03.databinding.CreateAccountBinding

class CreateAccountActivity : AppCompatActivity() {

    private var mBinding: CreateAccountBinding? = null
    private val binding get() = mBinding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.createAccountButton.setOnClickListener {
            /*  if(binding.createPassword.text != binding.confirmPassword.text){
                  Toast.makeText(this,"ㄴㄴ", Toast.LENGTH_SHORT).show()
              }*/

            val db = Firebase.firestore
            val data = hashMapOf("id" to "id", "password" to "password")
            db.collection("test").document("test2").set(data).addOnSuccessListener {
                Log.d("hh","hh")
            }.addOnFailureListener { e -> Log.d("ss,", "ss") }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}