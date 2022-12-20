package com.jaeyoung.studyapp03

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jaeyoung.studyapp03.databinding.CreateAccountBinding
import java.util.regex.Pattern


class CreateAccountActivity : AppCompatActivity() {

    private var mBinding: CreateAccountBinding? = null
    private val binding get() = mBinding!!

    private var auth: FirebaseAuth? = null

    // email 검사 정규식
    private val emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.createAccountButton.setOnClickListener {

            val createEmail = binding.createEmail.text.toString().trim()
            val createPassword = binding.createPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()


            if(!checkEmail(createEmail)){
                binding.checkEmailNotice.visibility = View.VISIBLE
            }else{
                binding.checkEmailNotice.visibility = View.INVISIBLE
            }

            if (createPassword.length < 6) {
                binding.passwordMoreThan6Notice.visibility = View.VISIBLE
            }else{
                binding.passwordMoreThan6Notice.visibility = View.INVISIBLE
            }

            if (createPassword != confirmPassword) {
                binding.confirmPasswordNotice.visibility = View.VISIBLE
            }else{
                binding.confirmPasswordNotice.visibility = View.INVISIBLE
            }

            if(createEmail.isEmpty() && createPassword.isEmpty()){
                binding.checkEmailNotice.visibility = View.VISIBLE
                binding.passwordMoreThan6Notice.visibility = View.VISIBLE
            }else if(createEmail.isEmpty()){
                binding.checkEmailNotice.visibility = View.VISIBLE
            }else if (createPassword.isEmpty()){
                binding.passwordMoreThan6Notice.visibility = View.VISIBLE
            }else{
                createAccount(createEmail, createPassword)
            }






        }
    }

    private fun checkEmail(loginEmail: String): Boolean {
        return Pattern.matches(emailValidation, loginEmail)
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