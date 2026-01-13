package com.soumya.biketracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soumya.biketracker.data.database.DatabaseProvider
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.repository.FuelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FuelViewModel(application: Application): AndroidViewModel(application) {
    private val repository: FuelRepository
    val allFuelEntries: Flow<List<FuelEntry>>

    init {
        val db = DatabaseProvider.getDatabase(application)
        val fuelDao = db.fuelDao()
        repository = FuelRepository(fuelDao)
        allFuelEntries = repository.getAllFuelEntries()
    }

    fun insertFuelEntry(entry: FuelEntry){
        viewModelScope.launch {
            repository.insertFuelEntry(entry)
        }
    }

    fun clearAllFuelEntries(){
        viewModelScope.launch { repository.clearAll() }
    }
}