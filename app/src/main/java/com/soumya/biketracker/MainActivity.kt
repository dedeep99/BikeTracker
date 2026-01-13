package com.soumya.biketracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.ui.theme.BikeTrackeTheme
import com.soumya.biketracker.viewmodel.FuelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val fuelViewModel: FuelViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BikeTrackeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        val testEntry = FuelEntry(
            dateTime = System.currentTimeMillis(),
            odometer = 1200,
            pricePerLiter = 106.5,
            quantity = 4.5,
            totalCost = 479.25,
            isFullTank = true
        )
        fuelViewModel.insertFuelEntry(testEntry)

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
    BikeTrackeTheme {
        Greeting("Android")
    }
}