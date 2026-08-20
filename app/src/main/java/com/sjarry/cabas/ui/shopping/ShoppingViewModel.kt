package com.sjarry.cabas.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.model.ShoppingList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Les deux présentations demandées de la liste de courses. */
enum class ShoppingView { TOTAL, BY_RECIPE }

class ShoppingViewModel(
    private val menuRepository: MenuRepository,
) : ViewModel() {

    val shoppingList: StateFlow<ShoppingList> = menuRepository.observeShoppingList()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ShoppingList(emptyList(), emptyList()),
        )

    private val _view = MutableStateFlow(ShoppingView.TOTAL)
    val view: StateFlow<ShoppingView> = _view.asStateFlow()

    fun showView(view: ShoppingView) {
        _view.value = view
    }

    fun toggle(key: String, checked: Boolean) = viewModelScope.launch {
        menuRepository.setChecked(key, checked)
    }

    fun checkAll() = viewModelScope.launch {
        menuRepository.checkAll(shoppingList.value.total.map { it.key })
    }

    fun uncheckAll() = viewModelScope.launch { menuRepository.uncheckAll() }
}
