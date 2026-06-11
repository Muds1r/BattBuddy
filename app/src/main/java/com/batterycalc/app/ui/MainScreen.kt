package com.batterycalc.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.BatteryStd
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import com.batterycalc.app.util.BatteryInfo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.batterycalc.app.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class AppTab(val label: String) {
    Charging("Charging"),
    Usage("Usage"),
    Info("Info")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val activeCharge by viewModel.activeCharge.collectAsStateWithLifecycle()
    val chargeHistory by viewModel.chargeHistory.collectAsStateWithLifecycle()
    val activeUsage by viewModel.activeUsage.collectAsStateWithLifecycle()
    val usageHistory by viewModel.usageHistory.collectAsStateWithLifecycle()
    val isCharging by viewModel.isCharging.collectAsStateWithLifecycle()
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedTab by remember { mutableIntStateOf(if (isCharging) 0 else 1) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCharging) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            )
                            Text(
                                text = if (isCharging) "Charging" else "On battery",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = viewModel::refresh) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    AppTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab.label) },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.Charging -> Icons.Outlined.BatteryChargingFull
                                        AppTab.Usage -> Icons.Outlined.BatteryStd
                                        AppTab.Info -> Icons.Outlined.Info
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            Text(
                text = "Made by Muds1r",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    ) { padding ->
        when (AppTab.entries[selectedTab]) {
            AppTab.Charging -> ChargingTab(
                modifier = Modifier.padding(padding),
                activeSession = activeCharge,
                history = chargeHistory,
                isCharging = isCharging
            )
            AppTab.Usage -> UsageTab(
                modifier = Modifier.padding(padding),
                activeSession = activeUsage,
                history = usageHistory,
                isCharging = isCharging
            )
            AppTab.Info -> InfoTab(
                modifier = Modifier.padding(padding),
                info = batteryInfo,
                isCharging = isCharging
            )
        }
    }
}

@Composable
private fun ChargingTab(
    modifier: Modifier = Modifier,
    activeSession: ChargeSessionUiModel?,
    history: List<ChargeSessionUiModel>,
    isCharging: Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeSession != null) {
            item { LiveChargeCard(session = activeSession) }
        } else {
            item {
                EmptyState(
                    title = if (isCharging) "Starting session…" else "No active charge",
                    message = if (isCharging) {
                        "Plug event is being tracked."
                    } else {
                        "Plug in your phone to start a charging log."
                    }
                )
            }
        }
        if (history.isNotEmpty()) {
            item { SectionLabel("Logs") }
            items(history, key = { it.id }) { session ->
                ChargeHistoryRow(session = session)
            }
        }
    }
}

@Composable
private fun UsageTab(
    modifier: Modifier = Modifier,
    activeSession: UsageSessionUiModel?,
    history: List<UsageSessionUiModel>,
    isCharging: Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeSession != null) {
            item { LiveUsageCard(session = activeSession) }
        } else {
            item {
                EmptyState(
                    title = if (isCharging) "No active usage" else "No active session",
                    message = if (isCharging) {
                        "Usage tracking starts when you unplug. Your current session will appear in Logs after you plug back in."
                    } else {
                        "Unplug your phone to start tracking. BattBuddy logs automatically in the background."
                    }
                )
            }
        }
        if (history.isNotEmpty()) {
            item { SectionLabel("Logs") }
            items(history, key = { it.id }) { session ->
                UsageHistoryRow(session = session)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LiveChargeCard(session: ChargeSessionUiModel) {
    SessionHeroCard(
        label = "Since plugged in",
        mainValue = "+${session.percentGained}%",
        mainSubtitle = "charged",
        rateValue = session.chargePerHourLabel,
        rateSubtitle = "avg rate",
        stats = listOf(
            Triple("Plugged in", "${session.plugPercent}%", session.plugTimeLabel),
            Triple("Now", "${session.endPercent}%", session.endTimeLabel),
            Triple("Duration", session.durationLabel, null)
        )
    )
}

@Composable
private fun LiveUsageCard(session: UsageSessionUiModel) {
    SessionHeroCard(
        label = "Since unplugged",
        mainValue = "${session.percentDrop}%",
        mainSubtitle = "dropped",
        rateValue = session.drainPerHourLabel,
        rateSubtitle = "avg drain",
        stats = listOf(
            Triple("Unplugged", "${session.unplugPercent}%", session.unplugTimeLabel),
            Triple("Now", "${session.endPercent}%", session.endTimeLabel),
            Triple("Duration", session.durationLabel, null)
        )
    )
}

@Composable
private fun SessionHeroCard(
    label: String,
    mainValue: String,
    mainSubtitle: String,
    rateValue: String,
    rateSubtitle: String,
    stats: List<Triple<String, String, String?>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = mainValue,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = mainSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = rateValue, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = rateSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stats.forEach { (lbl, value, sub) ->
                StatCell(label = lbl, value = value, sub = sub)
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, sub: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        if (sub != null) {
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChargeHistoryRow(session: ChargeSessionUiModel) {
    HistoryRow(
        headline = "+${session.percentGained}% charged",
        rate = session.chargePerHourLabel,
        range = "${session.plugPercent}% → ${session.endPercent}%",
        duration = session.durationLabel,
        times = "${session.plugTimeLabel}  ·  ${session.endTimeLabel}"
    )
}

@Composable
private fun UsageHistoryRow(session: UsageSessionUiModel) {
    HistoryRow(
        headline = "${session.percentDrop}% drop",
        rate = session.drainPerHourLabel,
        range = "${session.unplugPercent}% → ${session.endPercent}%",
        duration = session.durationLabel,
        times = "${session.unplugTimeLabel}  ·  ${session.endTimeLabel}"
    )
}

@Composable
private fun HistoryRow(
    headline: String,
    rate: String,
    range: String,
    duration: String,
    times: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = headline, style = MaterialTheme.typography.titleMedium)
            Text(
                text = rate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = range,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = times,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoTab(
    modifier: Modifier = Modifier,
    info: BatteryInfo,
    isCharging: Boolean
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${info.levelPercent}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = if (isCharging) "Currently charging" else "On battery",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionLabel("Live readings") }
        item {
            InfoCard(
                rows = listOf(
                    "Voltage" to info.voltageLabel,
                    "Current" to info.currentLabel,
                    "Power" to info.powerLabel,
                    "Temperature" to info.temperatureLabel
                )
            )
        }

        item { SectionLabel("Battery health") }
        item {
            InfoCard(
                rows = listOf(
                    "Health status" to info.healthStatus,
                    "Capacity" to info.capacityLabel,
                    "Technology" to (info.technology ?: "—"),
                    "Cycle count" to info.cycleCountLabel
                )
            )
        }

        item {
            Text(
                text = "Readings update when you open or refresh the app. Cycle count is not available to third-party apps on Pixel — check Settings → Battery.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun InfoCard(rows: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        rows.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = value, style = MaterialTheme.typography.titleMedium)
            }
            if (index < rows.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
