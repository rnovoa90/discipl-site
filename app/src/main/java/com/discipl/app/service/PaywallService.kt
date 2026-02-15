package com.discipl.app.service

import android.app.Activity
import android.content.Context
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaywallService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    var isConfigured = false
        private set

    // Superwall placement IDs matching iOS
    object Placement {
        const val ONBOARDING_END = "onboarding_end"
        const val BENEFITS_LOCKED = "benefits_locked"
        const val STATS_LOCKED = "stats_locked"
        const val RELAPSE_DETAIL = "relapse_detail"
        const val MILESTONE_DAY7 = "milestone_day7"
    }

    fun configure(apiKey: String) {
        if (apiKey.isBlank()) return
        try {
            Purchases.configure(PurchasesConfiguration.Builder(context, apiKey).build())
            isConfigured = true
            startListening()
        } catch (e: Exception) {
            // RevenueCat not available
        }
    }

    private fun isSubscriptionActive(info: CustomerInfo?): Boolean {
        if (info == null) return false
        if (info.entitlements["premium"]?.isActive == true) return true
        return info.activeSubscriptions.isNotEmpty()
    }

    fun checkSubscriptionStatus() {
        if (!isConfigured) return
        Purchases.sharedInstance.getCustomerInfoWith { info ->
            _isPremium.value = isSubscriptionActive(info)
        }
    }

    fun restorePurchases(onResult: (Boolean) -> Unit) {
        if (!isConfigured) {
            onResult(false)
            return
        }
        Purchases.sharedInstance.restorePurchasesWith(
            onSuccess = { info ->
                val active = isSubscriptionActive(info)
                _isPremium.value = active
                onResult(active)
            },
            onError = { onResult(false) }
        )
    }

    fun fetchPackages(onResult: (List<Package>) -> Unit) {
        if (!isConfigured) {
            onResult(emptyList())
            return
        }
        Purchases.sharedInstance.getOfferingsWith(
            onSuccess = { offerings ->
                onResult(offerings.current?.availablePackages ?: emptyList())
            },
            onError = { onResult(emptyList()) }
        )
    }

    fun purchase(activity: Activity, pkg: Package, onResult: (Boolean) -> Unit) {
        if (!isConfigured) {
            onResult(false)
            return
        }
        Purchases.sharedInstance.purchaseWith(
            purchaseParams = com.revenuecat.purchases.PurchaseParams.Builder(activity, pkg).build(),
            onSuccess = { _, info ->
                val active = isSubscriptionActive(info)
                if (active) _isPremium.value = true
                onResult(active)
            },
            onError = { _, _ -> onResult(false) }
        )
    }

    private fun startListening() {
        Purchases.sharedInstance.updatedCustomerInfoListener =
            com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener { info ->
                _isPremium.value = isSubscriptionActive(info)
            }
    }
}
