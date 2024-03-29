package com.jaeyoung.studyapp03

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.jaeyoung.studyapp03.databinding.MainPageBinding


class MainPageActivity : AppCompatActivity() {


    private var mBinding: MainPageBinding? = null
    private val binding get() = mBinding!!
    private var auth: FirebaseAuth? = null
    private lateinit var mAdView: AdView
    private lateinit var manager: BillingManager

    val subsItemID: String = "studyapp"

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
        manager = BillingManager(this, object : BillingCallback {
            override fun onBillingConnected() {
                manager.getSkuDetails(
                    subsItemID,
                    billingType = BillingClient.SkuType.SUBS
                ) { list ->
                    mSkuDetails = list
                }

                manager.checkSubscribed(BillingClient.SkuType.SUBS) {
                    currentSubscription = it

                }
            }


            override fun onSuccess(purchase: Purchase) {
                currentSubscription = purchase

            }

            override fun onFailure(responseCode: Int) {
                Toast.makeText(
                    applicationContext,
                    "購入処理中にエラーが発生しました。(${responseCode})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        binding.btnPurchase.setOnClickListener {
            mSkuDetails.find { it.sku == subsItemID }?.let { skuDetails ->
                manager.purchaseSku(skuDetails)
            } ?: also {
                Toast.makeText(this, "購入できる商品がありません。", Toast.LENGTH_SHORT).show()
            }
        }


        binding.reloadButton.setOnClickListener {
            reloadActivity()

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }

    override fun onResume() {
        super.onResume()
        manager.onResume(BillingClient.SkuType.SUBS)
    }


    private fun signOut() {
        val intent = Intent(this, MainActivity::class.java)
        //실행하는 액티비티가 스택에 있으면 새로 시작하지 않고 상위 스택 모두 제거.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        Toast.makeText(this, "ログアウト", Toast.LENGTH_SHORT).show()
        auth?.signOut()
        finish() //현재 액티비티 종료
    }

    private fun revokeAccess() {
        val intent = Intent(this, MainActivity::class.java)
        //실행하는 액티비티가 스택에 있으면 새로 시작하지 않고 상위 스택 모두 제거.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        Toast.makeText(this, "アカウント削除", Toast.LENGTH_SHORT).show()
        auth?.currentUser?.delete()
        auth?.signOut()
        finish()
    }

    private fun getSkuDetails() {
        var info = ""
        for (skuDetail in mSkuDetails) {
            info += "${skuDetail.title}, ${skuDetail.price} \n"
        }
        Toast.makeText(this, info, Toast.LENGTH_SHORT).show()
    }

    private fun reloadActivity() {
        finish() //인텐트 종료
        overridePendingTransition(0, 0) //인텐트 효과 없애기
        val intent = intent //인텐트
        startActivity(intent) //액티비티 열기
        overridePendingTransition(0, 0) //인텐트 효과 없애기
    }


    @SuppressLint("SetTextI18n")
    private fun updateSubscriptionState() {
        currentSubscription?.let {
            binding.subState.text = " 会員 [${resources.getString(R.string.app_name)}]"
            binding.adViewBanner.visibility = View.GONE
        } ?: also {
            binding.subState.text = "会員ではありません。"
            binding.adViewBanner.visibility = View.VISIBLE
        }


    }


}