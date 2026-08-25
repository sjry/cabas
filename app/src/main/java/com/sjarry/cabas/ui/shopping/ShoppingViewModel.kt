package com.sjarry.cabas.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sjarry.cabas.data.MenuRepository
import com.sjarry.cabas.data.model.ShoppingList
import com.sjarry.cabas.parser.IngredientCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Les trois présentations de la liste de courses. */
enum class ShoppingView { AISLE, TOTAL, BY_RECIPE }

class ShoppingViewModel(
    private val menuRepository: MenuRepository,
) : ViewModel() {

    val shoppingList: StateFlow<ShoppingList> = menuRepository.observeShoppingList()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ShoppingList(emptyList(), emptyList()),
        )

    // « Rayon » par défaut : c'est la vue qui sert en magasin, les deux autres
    // répondent à « qu'est-ce que j'achète en tout » et « pour quelle recette ».
    private val _view = MutableStateFlow(ShoppingView.AISLE)
    val view: StateFlow<ShoppingView> = _view.asStateFlow()

    /**
     * Section « Pris » dépliée par défaut : voir ce qui est déjà dans le cabas
     * évite de le reprendre en rayon. Choix d'affichage volontairement non
     * persisté, comme le choix de vue.
     */
    private val _takenExpanded = MutableStateFlow(true)
    val takenExpanded: StateFlow<Boolean> = _takenExpanded.asStateFlow()

    fun showView(view: ShoppingView) {
        _view.value = view
    }

    fun toggleTaken() {
        _takenExpanded.value = !_takenExpanded.value
    }

    fun toggle(key: String, checked: Boolean) = viewModelScope.launch {
        menuRepository.setChecked(key, checked)
    }

    fun checkAll() = viewModelScope.launch {
        menuRepository.checkAll(shoppingList.value.total.map { it.key })
    }

    fun uncheckAll() = viewModelScope.launch { menuRepository.uncheckAll() }

    fun setCategory(name: String, category: IngredientCategory) = viewModelScope.launch {
        menuRepository.setCategory(name, category)
    }

    fun resetCategory(name: String) = viewModelScope.launch {
        menuRepository.resetCategory(name)
    }
}
