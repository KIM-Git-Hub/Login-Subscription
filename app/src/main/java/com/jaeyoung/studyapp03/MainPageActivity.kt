package com.jaeyoung.studyapp03

import android.content.Intent

import android.os.Bundle
import android.util.Log


import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.android.billingclient.api.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

import com.google.common.collect.ImmutableList
import com.google.firebase.auth.FirebaseAuth


import com.jaeyoung.studyapp03.databinding.MainPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainPageActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private val tag = "MainPageActivity"

    private var mBinding: MainPageBinding? = null
    private val binding get() = mBinding!!

    private var auth: FirebaseAuth? = null

    lateinit var mAdView: AdView

    private lateinit var textViewOneTimePayment: TextView

    //(결제) 전역변수
    lateinit var billingClient: BillingClient
    private var skuDetailsList: List<SkuDetails> =
        mutableListOf() //'SkuDetails' is deprecated. Deprecated in Java
    private var productDetailsList: List<ProductDetails> = mutableListOf()
    private lateinit var consumeListener: ConsumeResponseListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        mBinding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textViewOneTimePayment = binding.textViewOneTimePayment
        initBillingClient()

        binding.signOut.setOnClickListener {
            signOut()
        }

        binding.revokeAccess.setOnClickListener {
            revokeAccess()
        }

        //모바일 광고 sdk 초기화
        MobileAds.initialize(this)
        //광고 띄우기
        mAdView = binding.adViewBanner
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        setListener()

    }





    //먼저 통신을 지원하는 인터페이스인 billingClient를 초기화하고 구글 결제 서버와 연결한다.
    //
    //이 작업은 view가 create되는 시점에 해도 무방하다.
    //
    //연결을 성공했따면 querySkuDetail이라는, 결제가능 목록 리스트를 호출할 것이다.

    // Billing Client 초기화 ->
    // BillingClient : 결제 라이브러리 통신 인터페이스
    private fun initBillingClient() {
        billingClient =
            BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                //연결이 종료될 시 재시도 요망
                Log.d(tag, "연결 실패")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    //연결 성공
                    Log.d(tag, "연결 성공")
                    //Suspend 함수는 반드시 코루틴 내부에서 실행
                    CoroutineScope(Dispatchers.Main).launch {
                        querySkuDetails()
                        //코루틴을 쓴 이유는 서버에서 온 연결 성공을 받고, 그 결과값이 와야 querySkuDetails 을 사용하기 위해서이다.
                    }

                }
            }

        })
       consumeListener = ConsumeResponseListener { billingResult, purchaseToken ->
            Log.d(tag, "billingResult.responseCode : ${billingResult.responseCode}")
            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(tag, "소모 성공")
            } else {
                Log.d(tag, "소모 실패")
            }
        }
    }

    //구매 가능 목록을 호출
     fun querySkuDetails() {
        Log.d(tag, "querySkuDetails")

        //5.0 마이그레이션
        val tempParam = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ImmutableList.of(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("studyapp03").setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(tempParam) { billingResult, mutableList ->
            productDetailsList = mutableList
        }
        //ImmutableList 는 구글에서 제공하는 불변 컬렉션이다.
//이후에 빌드된 param 으로 queryProductDetailsAsync 를 호출한다. billingResult = 0 으로 정상적으로 호출됐다면 mutableList 에 구매가능 목록이 호출된다.
    }

    private fun setListener() {
        //버튼을 눌렀을때의 결제 시도 프로세스
        textViewOneTimePayment.setOnClickListener {
            Log.d(tag, "click")
            val flowProductDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetailsList[0]).build()
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(flowProductDetailsParams)).build()

            val responseCode = billingClient.launchBillingFlow(this, flowParams).responseCode
            Log.d(tag, responseCode.toString())
            Log.d(tag, BillingClient.BillingResponseCode.OK.toString())
//lowDetailParams를 통해 먼저 BillingFlowParams.ProductDetailParams 객체를 만들어야 한다.. 왜냐면..
//
//BillingFlowParams는 setProductDetailsParamsList 만 제공하고, 여기에서 요구하는 객체는 List<BillingFlowParams.ProductDetailParams>이기 때문이다.. 따라서 우리는 결제목록을 불러왔다면, 거기서 결제하고 싶은 상품을 BillingFlowParams.ProductDetailParams로 우선적으로 만들고, 그것을 listOf를 통해 리스트화 해서 넣어야한다. 이후에 billingCilent.launchBillingFlow(this, flowParams)를 통해 구글 결제창을 띄운다.
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchase: MutableList<Purchase>?) {
        Log.d(tag, billingResult.responseCode.toString())
        if(billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchase != null){
            for (i in purchase){
                Log.d(tag, "구매 성공")
                val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(i.purchaseToken)

            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    private fun signOut() {
        val intent = Intent(this, MainActivity::class.java)
        //실행하는 액티비티가 스택에 있으면 새로 시작하지 않고 상위 스택 모두 제거.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
        auth?.signOut()
    }

    private fun revokeAccess() {
        val intent = Intent(this, MainActivity::class.java)
        //실행하는 액티비티가 스택에 있으면 새로 시작하지 않고 상위 스택 모두 제거.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        Toast.makeText(this, "회원탈퇴 성공", Toast.LENGTH_SHORT).show()
        auth?.currentUser?.delete()
        auth?.signOut()
    }


}