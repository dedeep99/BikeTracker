package com.soumya.biketracker.ui.fuel

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.soumya.biketracker.data.entity.FuelEntry
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddFuelScreen(onSave: (FuelEntry) -> Unit){
    var odometer by remember { mutableStateOf("") }
    var totalCost by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isFullTank by remember { mutableStateOf(true) }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val fuelCompanies = listOf("Indian Oil", "HP", "BPCL", "Shell", "Reliance")
    val fuelTypes = listOf("Normal", "XP95", "Power", "Premium")
    val isFormValid = odometer.isNotBlank() && totalCost.isNotBlank() && quantity.isNotBlank() && company.isNotBlank() && fuelType.isNotBlank()
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val formattedDateTime = formatter.format(Date(selectedDateTime))

    val calendar = Calendar.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Date & Time: $formattedDateTime",
            modifier = Modifier.fillMaxWidth().clickable{
                val currentCal = Calendar.getInstance().apply {
                    timeInMillis = selectedDateTime
                }

                DatePickerDialog(context, { _, year, month, dayOfMonth ->
                    currentCal.set(Calendar.YEAR, year)
                    currentCal.set(Calendar.MONTH, month)
                    currentCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    TimePickerDialog(context, { _, hour, minute ->
                        currentCal.set(Calendar.HOUR_OF_DAY, hour)
                        currentCal.set(Calendar.MINUTE, minute)
                        selectedDateTime = currentCal.timeInMillis
                    },
                        currentCal.get(Calendar.HOUR_OF_DAY),
                        currentCal.get(Calendar.MINUTE),
                        false
                    ).show()
                    },
                    currentCal.get(Calendar.YEAR),
                    currentCal.get(Calendar.MONTH),
                    currentCal.get(Calendar.DAY_OF_MONTH)
                ).show()
                }.padding(12.dp),

            style = MaterialTheme . typography . bodyMedium
        )
        TextField(
            value = odometer,
            onValueChange = { odometer = it },
            label = { Text("Odometer (KM)") })
        TextField(
            value = totalCost,
            onValueChange = { totalCost = it },
            label = { Text("Total amount paid (₹)") })
        TextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Fuel quantity (L)") })
        TextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Fuel company") })
        TextField(
            value = fuelType,
            onValueChange = { fuelType = it },
            label = { Text("Fuel type") })
        TextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (Optional)") })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
            Text("Full tank")
        }

        Button(
            onClick = {

                val odo = odometer.toDouble()
                val total = totalCost.toDouble()
                val qty = quantity.toDouble()

                if (qty <= 0 || total <= 0 || odo <= 0) {
                    return@Button
                }

                val pricePerLitre = total/qty

                val entry = FuelEntry(
                    dateTime = selectedDateTime,
                    odometer = odo,
                    pricePerLitre = pricePerLitre,
                    quantity = qty,
                    totalCost = total,
                    isFullTank = isFullTank,
                    fuelCompany = company,
                    fuelType = fuelType,
                    notes = notes.ifEmpty { null }
                )

                onSave(entry)
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Fuel Entry")
        }
    }
}