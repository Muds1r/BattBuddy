package com.batterycalc.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batterycalc.app.BatteryCalcApp
import com.batterycalc.app.util.BatteryHelper
import com.batterycalc.app.util.BatteryInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class BatterySnapshot(
    val percent: Int,
    val isCharging: Boolean,
    val info: BatteryInfo,
    val now: Long
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BatteryCalcApp).repository
    private val snapshot = MutableStateFlow(readSnapshot(application))

    val isCharging: StateFlow<Boolean> = snapshot
        .map { it.isCharging }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), snapshot.value.isCharging)

    val batteryInfo: StateFlow<BatteryInfo> = snapshot
        .map { it.info }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), snapshot.value.info)

    val activeCharge: StateFlow<ChargeSessionUiModel?> = combine(
        repository.observeActiveCharge(),
        snapshot
    ) { session, snap ->
        if (session == null || snap.percent < 0 || !snap.isCharging) null
        else ChargeSessionUiModel.fromActive(session, snap.percent, snap.now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val chargeHistory: StateFlow<List<ChargeSessionUiModel>> =
        repository.observeCompletedCharge()
            .map { sessions -> sessions.mapNotNull { ChargeSessionUiModel.fromCompleted(it) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeUsage: StateFlow<UsageSessionUiModel?> = combine(
        repository.observeActiveDischarge(),
        snapshot
    ) { session, snap ->
        if (session == null || snap.percent < 0 || snap.isCharging) null
        else UsageSessionUiModel.fromActive(session, snap.percent, snap.now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val usageHistory: StateFlow<List<UsageSessionUiModel>> =
        repository.observeCompletedDischarge()
            .map { sessions -> sessions.mapNotNull { UsageSessionUiModel.fromCompleted(it) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        val app = getApplication<Application>()
        snapshot.update { readSnapshot(app) }
    }

    private fun readSnapshot(app: Application): BatterySnapshot {
        return BatterySnapshot(
            percent = BatteryHelper.getBatteryPercent(app),
            isCharging = BatteryHelper.isCharging(app),
            info = BatteryHelper.getBatteryInfo(app),
            now = System.currentTimeMillis()
        )
    }
}
