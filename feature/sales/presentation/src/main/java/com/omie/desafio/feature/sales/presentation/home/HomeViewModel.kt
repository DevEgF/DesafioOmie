package com.omie.desafio.feature.sales.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omie.desafio.core.domain.RemoteConfigProvider
import com.omie.desafio.feature.sales.domain.usecase.GetSalesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSales: GetSalesUseCase,
    private val remoteConfigProvider: RemoteConfigProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events

    init {
        viewModelScope.launch {
            getSales().collect { sales ->
                _state.update {
                    it.copy(
                        sales = sales,
                        totalValueCents = sales.sumOf { sale -> sale.totalValueCents },
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnNewSaleClick -> emitEvent(HomeEvent.NavigateToNewSale)
            HomeAction.OnManageProductsClick -> emitEvent(HomeEvent.NavigateToProducts)
            is HomeAction.OnSaleClick -> {
                if (remoteConfigProvider.isSaleDetailEnabled()) {
                    emitEvent(HomeEvent.NavigateToSaleDetail(action.saleId))
                }
            }
            HomeAction.OnDeveloperModeClick -> emitEvent(HomeEvent.NavigateToDeveloperMode)
        }
    }

    private fun emitEvent(event: HomeEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
