package com.jaeyoung.studyapp03

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*

interface BillingCallback { // BillingManager 모듈 구현 //결제 Callback 인터페이스
    fun onBillingConnected() // BillingClient 연결 성공 시 호출
    fun onSuccess(purchase: Purchase) // 구매 성공 시 호출 Purchase : 구매정보
    fun onFailure(responseCode: Int)// 구매 실패 시 호출 errorCode : BillingResponseCode
}

class BillingManager(private val activity: Activity, private val callback: BillingCallback) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener{ billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (i in purchases) {
                confirmPurchase(i)
            }
        } else {
            callback.onFailure(billingResult.responseCode)
        }
    }

    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    //생성자 시그니쳐만 정의할 수 있고 초기화 로직 (내부 프로퍼티에 할당)을 작성할 수 가 없습니다.
    //코틀린에서 주 생성자에 어떠한 코드도 추가될 수 없습니다. 따라서 초기화 시에 필요한 작업(유효성 검증 등)을 하기 위해 코틀린에서는 init 블록을 지원합니다.
    init {
        billingClient.startConnection(object: BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
             Log.d("BillingManger", "BillingClient onBillingServiceDisconnected() called")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK){
                    callback.onBillingConnected()
                }else{
                    callback.onFailure(billingResult.responseCode)
                }
            }
        })
    }

    private fun confirmPurchase(purchases: Purchase) {

    }
}







