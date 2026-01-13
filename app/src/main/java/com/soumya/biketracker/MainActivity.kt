package com.soumya.biketracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.soumya.biketracker.data.entity.FuelEntry
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    ) {
                        items(fuelEntries)
                        { entry -> Text(
                            text = "ODO: ${entry.odometer} KM | ₹${entry.totalCost}",
                            modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }

//        val testEntry = FuelEntry(
//            dateTime = System.currentTimeMillis(),
//            odometer = 1200,
//            pricePerLiter = 106.5,
//            quantity = 4.5,
//            totalCost = 479.25,
//            isFullTank = true
//        )
//        fuelViewModel.insertFuelEntry(testEntry)

        lifecycleScope.launch {
            fuelViewModel.allFuelEntries.collectLatest {
                list -> Log.d("FuelDB", "Fuel entries count: ${list.size}")
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