package com.omie.desafio.feature.sales.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omie.desafio.feature.sales.domain.usecase.GetSaleUseCase
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
class SaleDetailViewModel @Inject constructor(
    private val getSale: GetSaleUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(SaleDetailState())
    val state: StateFlow<SaleDetailState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SaleDetailEvent>()
    val events: SharedFlow<SaleDetailEvent> = _events

    init {
        val saleId = savedStateHandle.get<Long>("saleId") ?: 0L
        viewModelScope.launch {
            getSale(saleId).collect { sale ->
                _state.update { it.copy(sale = sale, isLoading = false) }
            }
        }
    }

    fun onAction(action: SaleDetailAction) {
        when (action) {
            SaleDetailAction.OnBackClick -> emitEvent(SaleDetailEvent.NavigateBack)
        }
    }

    private fun emitEvent(event: SaleDetailEvent) {
        viewModelScope.launch { _events.emit(event) }
    }
}
