package com.nightcheck.ui.paywall

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.nightcheck.billing.BillingManager
import com.nightcheck.billing.BillingState
import com.nightcheck.billing.PremiumViewModel
import com.nightcheck.ui.theme.LocalNightcheckColors

/**
 * Full-featured paywall bottom sheet.
 *
 * Trigger from any screen with:
 *   var showPaywall by remember { mutableStateOf(false) }
 *   if (showPaywall) {
 *       PaywallSheet(
 *           reason = PaywallReason.TaskLimit,
 *           onDismiss = { showPaywall = false }
 *       )
 *   }
 */
enum class PaywallReason {
    TaskLimit,
    NoteLimit,
    Widgets,
    AdFree,
    Generic
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallSheet(
    reason: PaywallReason = PaywallReason.Generic,
    onDismiss: () -> Unit,
    premiumViewModel: PremiumViewModel = hiltViewModel()
) {
    val activity       = LocalContext.current as? androidx.activity.ComponentActivity
    val products       by premiumViewModel.billingManager.products.collectAsStateWithLifecycle()
    val billingState   by premiumViewModel.billingManager.billingState.collectAsStateWithLifecycle()
    val isPremium      by premiumViewModel.isPremium.collectAsStateWithLifecycle()

    var selectedSku by remember { mutableStateOf(BillingManager.SKU_YEARLY) }

    // Dismiss automatically once premium activates
    LaunchedEffect(isPremium) {
        if (isPremium) onDismiss()
    }

    val sheetState = rememberModalBottomSheetState(skipPartialExpansion = true)

    ModalBottomSheet(
        onDismissRequest   = onDismiss,
        sheetState         = sheetState,
        containerColor     = MaterialTheme.colorScheme.surface,
        dragHandle         = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    ) {
        PaywallContent(
            reason        = reason,
            products      = products,
            selectedSku   = selectedSku,
            billingState  = billingState,
            onSelectSku   = { selectedSku = it },
            onPurchase    = {
                val product = products.firstOrNull { it.productId == selectedSku }
                val offer   = product?.subscriptionOfferDetails?.firstOrNull()
                if (product != null && offer != null && activity != null) {
                    premiumViewModel.billingManager.launchPurchase(
                        activity     = activity,
                        productDetails = product,
                        offerToken   = offer.offerToken
                    )
                }
            },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun PaywallContent(
    reason: PaywallReason,
    products: List<ProductDetails>,
    selectedSku: String,
    billingState: BillingState,
    onSelectSku: (String) -> Unit,
    onPurchase: () -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    val (headline, subheadline) = remember(reason) {
        when (reason) {
            PaywallReason.TaskLimit  -> "Task limit reached" to "Free accounts support up to 7 tasks. Upgrade to add unlimited tasks."
            PaywallReason.NoteLimit  -> "Note limit reached" to "Free accounts support up to 10 notes. Upgrade for unlimited notes."
            PaywallReason.Widgets    -> "Widgets are premium" to "Add NightCheck to your home screen with a Premium subscription."
            PaywallReason.AdFree     -> "Go ad-free" to "Remove all ads and enjoy a distraction-free experience."
            PaywallReason.Generic    -> "Unlock NightCheck Premium" to "Get the most out of your nightly review."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Crown icon ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(scheme.primary.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                .border(1.dp, scheme.primary.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Star,
                contentDescription = null,
                tint               = scheme.primary,
                modifier           = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text       = headline,
            style      = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color      = scheme.onSurface
            ),
            textAlign  = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = subheadline,
            style     = MaterialTheme.typography.bodyMedium.copy(color = nc.textMuted),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // ── Feature list ──────────────────────────────────────────────────
        val features = listOf(
            Icons.Default.AllInclusive  to "Unlimited tasks & notes",
            Icons.Default.SmartDisplay  to "Home screen widgets",
            Icons.Default.Block         to "No ads, ever",
            Icons.Default.Refresh       to "Recurring task support"
        )
        features.forEach { (icon, label) ->
            PremiumFeatureRow(icon = icon, label = label)
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(8.dp))

        // ── Plan selector ─────────────────────────────────────────────────
        val monthly = products.firstOrNull { it.productId == BillingManager.SKU_MONTHLY }
        val yearly  = products.firstOrNull { it.productId == BillingManager.SKU_YEARLY }

        if (monthly != null || yearly != null) {
            PlanRow(
                monthly     = monthly,
                yearly      = yearly,
                selectedSku = selectedSku,
                onSelect    = onSelectSku
            )
            Spacer(Modifier.height(20.dp))
        } else {
            // Products not loaded yet — show skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(scheme.surfaceVariant)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    color    = scheme.primary
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Purchase button ───────────────────────────────────────────────
        Button(
            onClick  = onPurchase,
            enabled  = billingState !is BillingState.Processing && products.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = scheme.primary)
        ) {
            if (billingState is BillingState.Processing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color    = scheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Start Premium",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Dismiss / restore ─────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onDismiss) {
                Text("Not now", color = nc.textMuted, fontSize = 13.sp)
            }
            TextButton(onClick = { /* restore purchases via billing */ }) {
                Text("Restore purchases", color = nc.textMuted, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            "Recurring billing. Cancel anytime in Play Store.",
            style     = MaterialTheme.typography.labelSmall.copy(color = nc.textFaint),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PremiumFeatureRow(icon: ImageVector, label: String) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(scheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = scheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun PlanRow(
    monthly: ProductDetails?,
    yearly: ProductDetails?,
    selectedSku: String,
    onSelect: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Monthly card
        if (monthly != null) {
            val monthlyPrice = monthly.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.lastOrNull()
                ?.formattedPrice ?: "$1.99"

            PlanCard(
                modifier    = Modifier.weight(1f),
                title       = "Monthly",
                price       = monthlyPrice,
                subtitle    = "per month",
                badge       = null,
                isSelected  = selectedSku == BillingManager.SKU_MONTHLY,
                onClick     = { onSelect(BillingManager.SKU_MONTHLY) }
            )
        }

        // Yearly card (recommended)
        if (yearly != null) {
            val yearlyPrice = yearly.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases?.pricingPhaseList?.lastOrNull()
                ?.formattedPrice ?: "$9.99"

            PlanCard(
                modifier   = Modifier.weight(1f),
                title      = "Yearly",
                price      = yearlyPrice,
                subtitle   = "per year",
                badge      = "SAVE 58%",
                isSelected = selectedSku == BillingManager.SKU_YEARLY,
                onClick    = { onSelect(BillingManager.SKU_YEARLY) }
            )
        }
    }
}

@Composable
private fun PlanCard(
    modifier: Modifier,
    title: String,
    price: String,
    subtitle: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val nc     = LocalNightcheckColors.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) scheme.primary.copy(alpha = 0.1f) else scheme.surfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) scheme.primary else nc.borderMuted,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() }
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color      = if (isSelected) scheme.primary else nc.textMuted
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                price,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color      = scheme.onSurface
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = nc.textFaint)
            )
        }

        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-8).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(scheme.primary)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    badge,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = scheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 9.sp
                    )
                )
            }
        }
    }
}