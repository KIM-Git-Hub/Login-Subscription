package com.jaeyoung.studuyapp03

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jaeyoung.studuyapp03.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private var auth: FirebaseAuth? = null

    private val RC_SIGN_IN = 99

    // Google Sign-In Methods
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //auth 객체 초기화
        auth = Firebase.auth

        binding.registry.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        binding.login.setOnClickListener {
            val loginEmail = binding.loginEmail.text.toString()
            val loginPassword = binding.loginPassword.text.toString()
            signIn(loginEmail, loginPassword)
        }

        //GoogleSignInClient 객체 초기화 //기본 로그인 방식 사용
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("147719625226-qpicac74ag7dmbi8sj4kqq904ggpia2v.apps.googleusercontent.com")
            //requestIdToken :필수사항이다. 사용자의 식별값(token)을 사용하겠다.
            //(App이 구글에게 요청)
            .requestEmail()
            // 사용자의 이메일을 사용하겠다.(App이 구글에게 요청)
            .build()
        //대충 이 앱에서 구글 계정을 쓰겟다는 말
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleLogin.setOnClickListener {
            //구글로 로그o
            val signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)


        }


    }

    //startAcivityResult()로 인해 다른 앱/ 액티비티가 실행된 후.
    //그 앱/액티비티의 작업이 끝나서 다시 이 액티비티로 돌아왔을 떄 할일
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                //결과 Intent(data 매개변수) 에서 구글로그인 결과 꺼내오기
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
                //정상적으로 로그인되었다면
                if (result!!.isSuccess) {
                    // Firebase 서버에 사용자 이메일정보보내기
                    val account = result.signInAccount
                    firebaseAuthWithGoogle(account)
                }else{
                    Toast.makeText(this, "sibal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        //구글로부터 로그인된 사용자의 정보(Credential)을 얻어온다.
        val credential = GoogleAuthProvider.getCredential(account?.idToken!!, null)
        //그 정보를 사용하여 Firebase 의 auth 를 실행한다.
        auth?.signInWithCredential(credential)?.addOnCompleteListener {
            //통신 완료가 된 후 무슨일을 할지
                task ->
            if (task.isSuccessful) {
                //로그인 처리
                    val user = auth?.currentUser
                Log.d("xxxx", user.toString())
                movePageActivity(auth?.currentUser)
            }else{
                //오류가 난 경우
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
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