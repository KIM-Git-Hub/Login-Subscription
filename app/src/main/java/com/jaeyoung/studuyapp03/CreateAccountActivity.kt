package com.jaeyoung.studuyapp03

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jaeyoung.studuyapp03.databinding.CreateAccountBinding

class CreateAccountActivity: AppCompatActivity() {

    private var mBinding : CreateAccountBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = CreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountButton.setOnClickListener {
            if(binding.createPassword.text != binding.confirmPassword.text){
                Toast.makeText(this,"ㄴㄴ", Toast.LENGTH_SHORT).show()
            }


        }
    }



    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}