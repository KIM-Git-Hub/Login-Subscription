package com.jaeyoung.studyapp03

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

import com.jaeyoung.studyapp03.databinding.MainPageBinding


class MainPageActivity : AppCompatActivity() {

    private var mBinding: MainPageBinding? = null
    private val binding get() = mBinding!!

    private var auth: FirebaseAuth? = null
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        mBinding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.logout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            //실행하는 액티비티가 스택에 있으면 새로 시작하지 않고 상위 스택 모두 제거.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
            auth?.signOut()
            googleSignInClient?.revokeAccess()?.addOnCompleteListener(this){

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}