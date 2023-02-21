package com.jaeyoung.studyapp03


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.startActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
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
            .requestIdToken(getString(R.string.default_web_client_id))
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
                Toast.makeText(this, "メールアドレスやパスワードを入力してください。", Toast.LENGTH_SHORT).show()
            }else if(!checkEmail(loginEmail)){
                Toast.makeText(this,"メールアドレスの形式が正しくありません。",Toast.LENGTH_LONG).show()
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
            }else{
                Log.d("ssss", result.resultCode.toString())
                Log.d("ssss", result.toString())
            }

        }



    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth!!.currentUser
                    movePageActivity(user)
                    Toast.makeText(this, "ログイン", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("로그인 실패", task.exception.toString())
                    Toast.makeText(this, "ログイン失敗", Toast.LENGTH_SHORT).show()
                }
                Log.d("googleLoginFail", task.exception?.message.toString())
            }
    }

    private fun signIn(loginEmail: String, loginPassword: String) {
        auth?.signInWithEmailAndPassword(loginEmail, loginPassword)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "ログイン", Toast.LENGTH_SHORT).show()
                    movePageActivity(auth?.currentUser)
                } else {
                    Toast.makeText(this, "ログイン失敗", Toast.LENGTH_SHORT).show()
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