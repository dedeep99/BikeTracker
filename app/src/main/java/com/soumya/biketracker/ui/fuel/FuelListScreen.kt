package com.soumya.biketracker.ui.fuel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soumya.biketracker.data.entity.FuelEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FuelListScreen(fuelEntries:List<FuelEntry>, modifier: Modifier = Modifier, onEntryClick: (FuelEntry) -> Unit){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        items(fuelEntries){ entry ->
            FuelEntryCard(
                entry = entry,
                onClick = { onEntryClick(entry) }
            )
        }
    }
}

@Composable
fun FuelEntryCard(
    entry: FuelEntry,
    onClick: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = formatter.format(Date(entry.dateTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                android.util.Log.d("FuelClick", "Card clicked: ${entry.id}")
                onClick()
           },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "📆️ $formattedDate")
            Text(text = "Odometer: ${entry.odometer} km")
            Text(text = "Fuel: ${entry.quantity} L @ ₹%.2f".format(entry.pricePerLitre))
            Text(text = "Total: ₹${entry.totalCost}")
            Text(text = "Fuel Company: ${entry.fuelCompany}")
            Text(text = "Fuel Type: ${entry.fuelType}")
            Text(text = if(entry.isFullTank) "Type: Full Tank" else "Type: Partial")
            if (entry.isFullTank && entry.mileage != null) {
                Text(
                    text = "Mileage: %.2f km/L".format(entry.mileage)
                )
            }
            if(!entry.notes.isNullOrEmpty())
                Text(text = "Notes: ${entry.notes}")

        }
    }


}
