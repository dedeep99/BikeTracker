package com.soumya.biketracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.ui.fuel.AddFuelScreen
import com.soumya.biketracker.ui.fuel.FuelListScreen
import com.soumya.biketracker.ui.theme.BikeTrackerTheme
import com.soumya.biketracker.viewmodel.FuelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val fuelViewModel: FuelViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BikeTrackerTheme {
                val fuelEntries by fuelViewModel.allFuelEntries.collectAsState(initial = emptyList())
                var showAddFuelScreen by remember { mutableStateOf(false)}
                var selectedEntry by remember { mutableStateOf<FuelEntry?>(null) }
                Scaffold(
                    floatingActionButton = {
                        if (!showAddFuelScreen) {
                            FloatingActionButton(onClick = {
                                showAddFuelScreen = true
                            }) {
                                Text("+")
                            }
                        }
                    }
                ) { padding ->

                    if (showAddFuelScreen) {

                        androidx.activity.compose.BackHandler {
                            showAddFuelScreen = false
                            selectedEntry = null
                        }

                        AddFuelScreen(
                            viewModel = fuelViewModel,
                            existingEntry = selectedEntry,
                            onSaveSuccess = {
                                showAddFuelScreen = false
                                selectedEntry = null
                            }
                        )
                } else {
                        FuelListScreen(
                            fuelEntries = fuelEntries,
                            modifier = Modifier.padding(padding),
                            onEntryClick = { entry ->
                                Log.d("MainActivity", "Navigating to edit for id=${entry.id}")
                                selectedEntry = entry
                                showAddFuelScreen = true
                            }
                        )

                    }
                }

            }
        }

//        val testEntry = FuelEntry(
//            dateTime = System.currentTimeMillis(),
//            odometer = 1200.5,
//            pricePerLitre = 106.5,
//            quantity = 4.5,
//            totalCost = 479.25,
//            isFullTank = true,
//            fuelType = "XP95",
//            notes = "smoothest ride ever",
//            fuelCompany = "IOCL"
//        )
//        fuelViewModel.insertFuelEntry(testEntry)

        lifecycleScope.launch {
            fuelViewModel.allFuelEntries.collectLatest { list ->
                Log.d("FuelDB", "Fuel entries count: ${list.size}")
                list.forEach {
                    Log.d("FuelDB", it.toString())
                }
            }
        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BikeTrackerTheme {
        Greeting("Android")
    }
}