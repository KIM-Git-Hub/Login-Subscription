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

        val createID = binding.createID.text
        val createPassword = binding.createPassword.text
        val confirmPassword = binding.confirmPassword.text

        binding.createAccountButton.setOnClickListener {

            if(createPassword.toString() != confirmPassword.toString()){
                Toast.makeText(this, "비밀번호가 일치하지 않음", Toast.LENGTH_SHORT).show()
            }else{
                val db = Firebase.firestore
                val data =
                    hashMapOf("id" to createID.toString(), "password" to createPassword.toString())
                db.collection("studyApp").document("account").set(data).addOnSuccessListener {
                    Log.d("ㅎㅎ", "ㅎㅎ")
                }.addOnFailureListener { e -> Log.d("ㅠㅠ,", "ㅠㅠ") }
            }



        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}