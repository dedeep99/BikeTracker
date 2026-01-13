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


class FuelViewModel(application: Application): AndroidViewModel(application) {
    private val repository: FuelRepository
    val allFuelEntries: Flow<List<FuelEntry>>

    /* ---------- Fuel company data ---------- */

    // For dropdown
    val fuelCompanies: List<FuelCompany> = FuelCompany.entries

    // Selected company (Compose state)
    private val _selectedCompany = mutableStateOf<FuelCompany?>(null)

    val selectedCompany: State<FuelCompany?> = _selectedCompany

    // Fuel types derived from selected company
    val availableFuelTypes: State<List<FuelType>> =
        derivedStateOf {
            _selectedCompany.value?.let {
                FuelRules.allowedFuelTypes(it)
            } ?: emptyList()
        }

    fun getFuelTypes(company: FuelCompany): List<FuelType> {
        return FuelRules.allowedFuelTypes(company)
    }

    /* ---------- Init ---------- */

    init {
        val db = DatabaseProvider.getDatabase(application)
        val fuelDao = db.fuelDao()
        repository = FuelRepository(fuelDao)
        allFuelEntries = repository.getAllFuelEntries()
    }

/* ---------- Events ---------- */

    fun onCompanySelected(company: FuelCompany) {
        _selectedCompany.value = company
    }

    fun insertFuelEntry(entry: FuelEntry) {
        viewModelScope.launch {
            repository.insertFuelEntry(entry)
        }
    }

    fun clearAllFuelEntries() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}