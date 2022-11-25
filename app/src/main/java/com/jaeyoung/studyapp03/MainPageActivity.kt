package com.jaeyoung.studyapp03

import android.annotation.SuppressLint
import android.content.Intent

import android.os.Bundle
import android.util.Log


import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity

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


class MainPageActivity : AppCompatActivity() {



    private var mBinding: MainPageBinding? = null
    private val binding get() = mBinding!!

    private var auth: FirebaseAuth? = null

    lateinit var mAdView: AdView

    private lateinit var manager: BillingManager
    val subItemID: String = "not yet"

    private var mSkuDetails = listOf<SkuDetails>()
    set(value) {
        field = value
        getSkuDetails()
    }

    private var currentSubscription: Purchase? = null
    set(value) {
        field = value
        updateSubscriptionState()
    }

    @SuppressLint("SetTextI18n")
    private fun updateSubscriptionState() {
        currentSubscription?.let {
            binding.subState.text = "구독중: ${it.skus} "
        } ?: also {
            binding.subState.text = "구독권이 없습니다."
        }
    }

    private fun getSkuDetails() {
        var info = ""
        for (skuDetail in mSkuDetails) {
            info += "${skuDetail.title}, ${skuDetail.price} \n"
        }
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        mBinding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


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


        //구독
        manager = BillingManager(this, object: BillingCallback{
            override fun onBillingConnected() {
                manager.getSkuDetails(subItemID, billingType = BillingClient.SkuType.SUBS){ list ->
                    mSkuDetails = list
                }
                manager.checkSubscribed(subItemID){
                    currentSubscription = it
                }
            }

            override fun onSuccess(purchase: Purchase) {
                currentSubscription = purchase
            }

            override fun onFailure(responseCode: Int) {
               Toast.makeText(applicationContext, "구매 도중 오류 발생(${responseCode})", Toast.LENGTH_LONG).show()
            }
        })

        binding.btnPurchase.setOnClickListener {
            mSkuDetails.find { it.sku == subItemID }?.let { skuDetails ->
                manager.purchaseSku(skuDetails)
            } ?: also {
                Toast.makeText(this, "구매 가능 한 상품이 없습니다.", Toast.LENGTH_LONG).show()
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