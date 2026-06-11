package com.batterycalc.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batterycalc.app.BatteryCalcApp
import com.batterycalc.app.util.BatteryHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BatteryCalcApp).repository
    private val tick = MutableStateFlow(System.currentTimeMillis())
    private val currentPercent = MutableStateFlow(BatteryHelper.getBatteryPercent(application))
    private val _isCharging = MutableStateFlow(BatteryHelper.isCharging(application))

    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    val activeCharge: StateFlow<ChargeSessionUiModel?> = combine(
        repository.observeActiveCharge(),
        currentPercent,
        tick
    ) { session, percent, now ->
        if (session == null || percent < 0) null
        else ChargeSessionUiModel.fromActive(session, percent, now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val chargeHistory: StateFlow<List<ChargeSessionUiModel>> =
        repository.observeCompletedCharge()
            .combine(tick) { sessions, _ ->
                sessions.mapNotNull { ChargeSessionUiModel.fromCompleted(it) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeUsage: StateFlow<UsageSessionUiModel?> = combine(
        repository.observeActiveDischarge(),
        currentPercent,
        tick
    ) { session, percent, now ->
        if (session == null || percent < 0) null
        else UsageSessionUiModel.fromActive(session, percent, now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val usageHistory: StateFlow<List<UsageSessionUiModel>> =
        repository.observeCompletedDischarge()
            .combine(tick) { sessions, _ ->
                sessions.mapNotNull { UsageSessionUiModel.fromCompleted(it) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
        viewModelScope.launch {
            repository.syncChargingState(application)
            refresh()
        }
    }

    fun refresh() {
        val app = getApplication<Application>()
        currentPercent.value = BatteryHelper.getBatteryPercent(app)
        _isCharging.value = BatteryHelper.isCharging(app)
        tick.value = System.currentTimeMillis()
    }
}
