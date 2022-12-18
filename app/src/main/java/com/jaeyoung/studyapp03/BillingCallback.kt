package com.jaeyoung.studyapp03

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface BillingCallback { // BillingManager 모듈 구현 //결제 Callback 인터페이스
    fun onBillingConnected() // BillingClient 연결 성공 시 호출
    fun onSuccess(purchase: Purchase) // 구매 성공 시 호출 Purchase : 구매정보
    fun onFailure(responseCode: Int)// 구매 실패 시 호출 errorCode : BillingResponseCode
}

class BillingManager(private val activity: Activity, private val callback: BillingCallback) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d("6666", purchases.toString())
                confirmPurchase(purchase)
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
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d("BillingManger", "BillingClient onBillingServiceDisconnected() called")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    callback.onBillingConnected()
                } else {
                    callback.onFailure(billingResult.responseCode)
                }
            }
        })
    }

    /**
     * 콘솔에 등록한 상품 리스트를 가져온다.
     * @param sku 상품 ID String
     * @param billingType String IN_APP or SUBS
     * @param resultBlock 결과로 받을 상품정보들에 대한 처리
     */
    fun getSkuDetails(
        vararg sku: String,
        billingType: String,
        resultBlock: (List<SkuDetails>) -> Unit = {}
    ) {
        val params = SkuDetailsParams.newBuilder().setSkusList(sku.asList()).setType(billingType)

        billingClient.querySkuDetailsAsync(params.build()) { _, list ->
            CoroutineScope(Dispatchers.Main).launch {
                resultBlock(list ?: emptyList())
            }
        }
    }

    /**
     * 구매 시도
     * @param skuDetail SkuDetails 구매 할 상품
     */
    fun purchaseSku(skuDetail: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder().apply {
            setSkuDetails(skuDetail)
        }.build()

        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            callback.onFailure(responseCode)
        }
    }


    /**
     * 구독 여부 확인
     * @param sku String 구매 확인 상품 //주석 잘못담 subs or inApp
     * @param resultBlock 구매 확인 상품에 대한 처리 return Purchase
     */

    fun checkSubscribed(sku: String, resultBlock: (Purchase?) -> Unit) {


        billingClient.queryPurchasesAsync(sku) { _, purchases ->
            Log.d("1111", "1111")
            CoroutineScope(Dispatchers.Main).launch {
                for (purchase in purchases) {
                    Log.d("2222", "2222")
                    if (purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        return@launch resultBlock(purchase)
                    }
                }
                Log.d("3333", "3333")
                return@launch resultBlock(null)
            }
        }
    }


// BillingClient: getPurchase() failed. Response code: 5
    //Invalid SKU type
    //5. BILLING_RESPONSE_RESULT_DEVELOPER_ERROR: Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest


    /**
     * 구매 확인
     * @param purchase
     */
    private fun confirmPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            // 구매를 완료 했지만 확인이 되지 않은 경우 확인 처리
            val ackPurchaseParams =
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)

            CoroutineScope(Dispatchers.Main).launch {
                billingClient.acknowledgePurchase(ackPurchaseParams.build()) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        callback.onSuccess(purchase)
                    } else {
                        callback.onFailure(it.responseCode)
                    }
                }
            }
        }

    }

    /**
     * 구매 확인이 안 된 경우 다시 확인 할 수 있도록
     */
    fun onResume(type: String) {
        if (billingClient.isReady) {
            billingClient.queryPurchasesAsync(type) { _, purchases ->
                for (purchase in purchases) {
                    if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        confirmPurchase(purchase)
                    }
                }
            }
        }
    }
}







