package com.jaeyoung.studyapp03


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

import com.jaeyoung.studyapp03.databinding.ActivityMainBinding
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    var auth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    // email 검사 정규식
    private val emailValidation = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("457909892680-o9c8mhrsrh9kp8k6qdfp8qjkkjsuoqsp.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.registry.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            val loginEmail = binding.loginEmail.text.toString().trim()
            val loginPassword = binding.loginPassword.text.toString().trim()

            if(loginEmail == "" || loginPassword == ""){
                Toast.makeText(this, "아이디 혹은 비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show()
            }else if(!checkEmail(loginEmail)){
                Toast.makeText(this,"이메일 형식에 맞게 입력해 주세요",Toast.LENGTH_LONG).show()
            } else{
                signIn(loginEmail, loginPassword)
            }


        }

        binding.googleLogin.setOnClickListener {
            signInGoogle()
        }
    }

    private fun checkEmail(loginEmail: String): Boolean {
        return Pattern.matches(emailValidation, loginEmail)
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startForResult.launch(signInIntent)

    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent: Intent = result.data!!
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                }catch (e: ApiException){
                    Log.d("startForResult", e.toString())
                }
            }

        }



    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth!!.currentUser
                    movePageActivity(user)
                } else {
                    Log.d("로그인 실패", task.exception.toString())
                }
            }
    }

    private fun signIn(loginEmail: String, loginPassword: String) {
        auth?.signInWithEmailAndPassword(loginEmail, loginPassword)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                    movePageActivity(auth?.currentUser)
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    override fun onStart() {
        super.onStart()
        movePageActivity(auth?.currentUser)
    }

    private fun movePageActivity(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainPageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}