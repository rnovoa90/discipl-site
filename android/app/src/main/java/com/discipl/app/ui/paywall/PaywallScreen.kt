package com.discipl.app.ui.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.discipl.app.data.db.UserProfileDao
import com.discipl.app.service.AnalyticsService
import com.discipl.app.service.PaywallService
import com.discipl.app.ui.components.PrimaryButton
import com.discipl.app.ui.theme.AppColors
import com.discipl.app.ui.theme.AppSpacing
import com.discipl.app.ui.theme.AppTypography
import com.revenuecat.purchases.Package
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PlanType { MONTHLY, YEARLY }

data class PaywallUiState(
    val language: String = "es",
    val selectedPlan: PlanType = PlanType.YEARLY,
    val isLoading: Boolean = false,
    val isFetchingPackages: Boolean = true,
    val errorMessage: String? = null,
    val packages: List<Package> = emptyList(),
    val yearlyPrice: String? = null,
    val monthlyPrice: String? = null,
    val savingsLabel: String? = null
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val paywallService: PaywallService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _state = MutableStateFlow(PaywallUiState())
    val state = _state.asStateFlow()

    private var yearlyPackage: Package? = null
    private var monthlyPackage: Package? = null

    init {
        viewModelScope.launch {
            val profile = userProfileDao.get()
            _state.value = _state.value.copy(language = profile?.language ?: "es")
            fetchPackages()
        }
    }

    private fun fetchPackages() {
        paywallService.fetchPackages { packages ->
            val yearly = packages.firstOrNull { it.packageType == com.revenuecat.purchases.PackageType.ANNUAL }
                ?: packages.firstOrNull { it.identifier == "\$rc_annual" }
            val monthly = packages.firstOrNull { it.packageType == com.revenuecat.purchases.PackageType.MONTHLY }
                ?: packages.firstOrNull { it.identifier == "\$rc_monthly" }

            yearlyPackage = yearly
            monthlyPackage = monthly

            val lang = _state.value.language
            val yearlyLabel = yearly?.let { "${it.product.price.formatted}/${if (lang == "en") "year" else "año"}" }
            val monthlyLabel = monthly?.let { "${it.product.price.formatted}/${if (lang == "en") "month" else "mes"}" }

            val savings = if (yearly != null && monthly != null) {
                val monthlyAmount = monthly.product.price.amountMicros
                val yearlyAmount = yearly.product.price.amountMicros
                val annualFromMonthly = monthlyAmount * 12
                if (annualFromMonthly > 0) {
                    val saved = ((1.0 - yearlyAmount.toDouble() / annualFromMonthly) * 100).toInt()
                    if (saved > 0) {
                        if (lang == "en") "Save $saved%" else "Ahorrá $saved%"
                    } else null
                } else null
            } else null

            _state.value = _state.value.copy(
                packages = packages,
                yearlyPrice = yearlyLabel,
                monthlyPrice = monthlyLabel,
                savingsLabel = savings,
                isFetchingPackages = false
            )
        }
    }

    fun selectPlan(plan: PlanType) {
        _state.value = _state.value.copy(selectedPlan = plan)
    }

    fun purchase(activity: Activity, onSuccess: () -> Unit) {
        val pkg = if (_state.value.selectedPlan == PlanType.YEARLY) yearlyPackage else monthlyPackage
        if (pkg == null) {
            _state.value = _state.value.copy(
                errorMessage = if (_state.value.language == "en")
                    "Products not available. Please try again later."
                else
                    "Productos no disponibles. Intentá de nuevo más tarde."
            )
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        paywallService.purchase(activity, pkg) { success ->
            _state.value = _state.value.copy(isLoading = false)
            if (success) {
                viewModelScope.launch {
                    val profile = userProfileDao.get() ?: return@launch
                    userProfileDao.update(profile.copy(isPremium = true))
                    analyticsService.subscriptionStarted(
                        plan = if (_state.value.selectedPlan == PlanType.YEARLY) "yearly" else "monthly",
                        trial = false
                    )
                }
                onSuccess()
            }
        }
    }

    fun restore(onResult: (Boolean) -> Unit) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        paywallService.restorePurchases { success ->
            _state.value = _state.value.copy(isLoading = false)
            if (success) {
                viewModelScope.launch {
                    val profile = userProfileDao.get() ?: return@launch
                    userProfileDao.update(profile.copy(isPremium = true))
                }
                onResult(true)
            } else {
                _state.value = _state.value.copy(
                    errorMessage = if (_state.value.language == "en")
                        "No active subscription found."
                    else
                        "No se encontró una suscripción activa."
                )
                onResult(false)
            }
        }
    }
}

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = AppColors.textSecondary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Header — logo
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.discipl.app.R.drawable.discipl_logo),
                contentDescription = "Discipl",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.xl.dp)
            )

            Spacer(Modifier.height(AppSpacing.lg.dp))

            // Features list
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.xl.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.Timeline,
                    text = if (state.language == "en") "Full benefits timeline" else "Línea de beneficios completa"
                )
                FeatureRow(
                    icon = Icons.Default.CalendarMonth,
                    text = if (state.language == "en") "Calendar heatmap" else "Calendario de progreso"
                )
                FeatureRow(
                    icon = Icons.Default.Share,
                    text = if (state.language == "en") "Shareable streak card" else "Tarjeta de racha compartible"
                )
                FeatureRow(
                    icon = Icons.Default.Book,
                    text = if (state.language == "en") "Relapse journal & insights" else "Diario de recaídas e insights"
                )
                FeatureRow(
                    icon = Icons.Default.Notifications,
                    text = if (state.language == "en") "All notification types" else "Todos los tipos de notificación"
                )
            }

            Spacer(Modifier.weight(1f))

            // Plan selector
            if (state.isFetchingPackages) {
                CircularProgressIndicator(
                    color = AppColors.textSecondary,
                    modifier = Modifier.padding(vertical = AppSpacing.lg.dp)
                )
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.lg.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlanCard(
                        title = if (state.language == "en") "Yearly" else "Anual",
                        price = state.yearlyPrice ?: "---",
                        savings = state.savingsLabel,
                        isSelected = state.selectedPlan == PlanType.YEARLY,
                        onClick = { viewModel.selectPlan(PlanType.YEARLY) }
                    )
                    PlanCard(
                        title = if (state.language == "en") "Monthly" else "Mensual",
                        price = state.monthlyPrice ?: "---",
                        savings = null,
                        isSelected = state.selectedPlan == PlanType.MONTHLY,
                        onClick = { viewModel.selectPlan(PlanType.MONTHLY) }
                    )
                }
            }

            // Error message
            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = AppTypography.caption,
                    color = AppColors.danger,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AppSpacing.lg.dp, vertical = AppSpacing.sm.dp)
                )
            }

            Spacer(Modifier.height(AppSpacing.md.dp))

            // CTA button
            PrimaryButton(
                text = if (state.isLoading) ""
                else if (state.language == "en") "Become a Disciplr"
                else "Convertite en Disciplr",
                onClick = {
                    activity?.let { act ->
                        viewModel.purchase(act) { onDismiss() }
                    }
                },
                enabled = !state.isLoading && !state.isFetchingPackages,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg.dp)
            )

            if (state.isLoading) {
                CircularProgressIndicator(
                    color = AppColors.accent,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 4.dp)
                )
            }

            // Restore
            TextButton(
                onClick = {
                    viewModel.restore { success ->
                        if (success) onDismiss()
                    }
                },
                enabled = !state.isLoading
            ) {
                Text(
                    text = if (state.language == "en") "Restore Purchases" else "Restaurar Compras",
                    style = AppTypography.caption,
                    color = AppColors.textSecondary
                )
            }

            Text(
                text = if (state.language == "en") "Cancel anytime. No commitment."
                else "Cancelá cuando quieras. Sin compromiso.",
                style = AppTypography.caption.copy(fontSize = 11.sp),
                color = AppColors.textSecondary.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = AppSpacing.lg.dp)
            )
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = AppColors.accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
            color = AppColors.textPrimary
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    savings: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.surface)
            .border(
                width = 2.dp,
                color = if (isSelected) AppColors.accent else android.graphics.Color.TRANSPARENT.let { androidx.compose.ui.graphics.Color.Transparent },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(AppSpacing.md.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.body.copy(fontWeight = FontWeight.Bold),
                color = AppColors.textPrimary
            )
            Text(
                text = price,
                style = AppTypography.caption,
                color = AppColors.textSecondary
            )
        }

        savings?.let {
            Text(
                text = it,
                style = AppTypography.label.copy(fontSize = 11.sp),
                color = AppColors.success,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AppColors.success.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        Icon(
            if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) AppColors.accent else AppColors.textSecondary.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
    }
}
