package com.impostorparty.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.impostorparty.app.BuildConfig
import com.impostorparty.app.R
import com.impostorparty.app.ui.components.PartyScaffold
import com.impostorparty.app.ui.components.PartySectionCard
import com.impostorparty.app.ui.components.PromoBannerSlot
import com.impostorparty.app.ui.theme.PartyDimens

@Composable
fun CreditsScreen(
    adsEnabled: Boolean,
    bannerAdUnitId: String?,
    removeAdsPriceLabel: String?,
    onOpenRemoveAdsSettings: () -> Unit,
    onBack: () -> Unit,
) {
    val bannerReservedHeight = 88.dp

    PartyScaffold(
        title = stringResource(R.string.credits_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = PartyDimens.SpaceLg, bottom = PartyDimens.SpaceXxl + bannerReservedHeight),
                verticalArrangement = Arrangement.spacedBy(PartyDimens.SpaceMd),
            ) {
                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.credits_creator_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.credits_creator_body), style = MaterialTheme.typography.bodyLarge)
                }

                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.credits_version_title), style = MaterialTheme.typography.titleMedium)
                    Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodyLarge)
                }

                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.credits_technology_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.credits_technology_body), style = MaterialTheme.typography.bodyMedium)
                }

                PartySectionCard(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.credits_privacy_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.credits_privacy_body), style = MaterialTheme.typography.bodyMedium)
                }
            }

            PromoBannerSlot(
                adsEnabled = adsEnabled,
                adUnitId = bannerAdUnitId,
                removeAdsPriceLabel = removeAdsPriceLabel,
                onRemoveAdsClick = onOpenRemoveAdsSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            )
        }
    }
}
