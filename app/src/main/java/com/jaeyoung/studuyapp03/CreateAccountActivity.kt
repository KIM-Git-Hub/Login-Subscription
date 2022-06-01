package com.jaeyoung.studuyapp03

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jaeyoung.studuyapp03.databinding.CreateAccountBinding

class CreateAccountActivity : AppCompatActivity() {

    private var mBinding: CreateAccountBinding? = null
    private val binding get() = mBinding!!

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.createAccountButton.setOnClickListener {

            val createEmail = binding.createEmail.text.toString()
            val createPassword = binding.createPassword.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if (createPassword != confirmPassword) {
                Toast.makeText(this, "비밀번호가 일치하지 않음", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(createEmail, createPassword)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    private fun createAccount(createEmail: String, createPassword: String) {
        auth?.createUserWithEmailAndPassword(createEmail, createPassword)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this, "계정 생성 완료.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish() // 가입창 종료
                } else {
                    Toast.makeText(
                        this, "계정 생성 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}