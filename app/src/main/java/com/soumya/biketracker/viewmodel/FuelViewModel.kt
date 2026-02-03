package com.soumya.biketracker.viewmodel

import android.app.Application
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soumya.biketracker.data.database.DatabaseProvider
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.domain.FuelCompany
import com.soumya.biketracker.domain.FuelRules
import com.soumya.biketracker.domain.FuelType
import com.soumya.biketracker.repository.FuelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

class FuelViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FuelRepository
    val allFuelEntries: Flow<List<FuelEntry>>

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    /* ---------- Fuel company data ---------- */

    val fuelCompanies: List<FuelCompany> = FuelCompany.entries

    private val _selectedCompany = mutableStateOf<FuelCompany?>(null)
    val selectedCompany: State<FuelCompany?> = _selectedCompany

    val availableFuelTypes: State<List<FuelType>> =
        derivedStateOf {
            _selectedCompany.value?.let {
                FuelRules.allowedFuelTypes(it)
            } ?: emptyList()
        }

    /* ---------- Init ---------- */

    init {
        val db = DatabaseProvider.getDatabase(application)
        val fuelDao = db.fuelDao()
        repository = FuelRepository(fuelDao)
        allFuelEntries = repository.getAllFuelEntries()
    }

    /* ---------- Public API ---------- */

    fun saveFuelEntry(
        oldEntry: FuelEntry?,
        newEntry: FuelEntry
    ) {
        viewModelScope.launch {
            try {
                if (oldEntry == null) {
                    repository.insertFuelEntry(newEntry)
                } else {
                    repository.updateFuelEntry(
                        oldEntry = oldEntry,
                        newEntry = newEntry
                    )
                }

                _errorMessage.value = null
                _saveSuccess.emit(Unit)

            } catch (e: IllegalArgumentException) {
                _errorMessage.value = e.message
            }
        }
    }

    fun onCompanySelected(company: FuelCompany) {
        _selectedCompany.value = company
    }

    fun deleteFuel(entry: FuelEntry) {
        viewModelScope.launch {
            repository.deleteFuelEntry(entry)
        }
    }

    fun clearAllFuelEntries() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
