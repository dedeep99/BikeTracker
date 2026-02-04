package com.soumya.biketracker.ui.fuel

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.domain.FuelCompany
import com.soumya.biketracker.domain.FuelType
import com.soumya.biketracker.viewmodel.FuelViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelScreen(
    viewModel: FuelViewModel,
    existingEntry: FuelEntry? = null,
    onSaveSuccess: () -> Unit){

    val isEditMode = existingEntry != null

    var odometer by rememberSaveable {
        mutableStateOf(existingEntry?.odometer?.toString() ?: "")
    }

    var quantity by rememberSaveable {
        mutableStateOf(existingEntry?.quantity?.toString() ?: "")
    }

    var totalCost by rememberSaveable {
        mutableStateOf(existingEntry?.totalCost?.toString() ?: "")
    }

    var notes by rememberSaveable {
        mutableStateOf(existingEntry?.notes ?: "")
    }

    var isFullTank by rememberSaveable {
        mutableStateOf(existingEntry?.isFullTank ?: true)
    }

    var selectedDateTime by remember {
        mutableLongStateOf(existingEntry?.dateTime ?: System.currentTimeMillis())
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }


    val formatter = remember {
        SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
    }

    var selectedFuelType by remember { mutableStateOf<FuelType?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val repoErrorMessage by viewModel.errorMessage.collectAsState()



    val fuelCompanies = viewModel.fuelCompanies
    val selectedCompany = viewModel.selectedCompany.value
    val availableFuelTypes = viewModel.availableFuelTypes.value

    LaunchedEffect(selectedCompany) {
        if (!isEditMode) {
            selectedFuelType = null
        }
    }

    LaunchedEffect(existingEntry) {
        if (existingEntry != null) {
            viewModel.onCompanySelected(existingEntry.fuelCompany)
            selectedFuelType = existingEntry.fuelType
            selectedDateTime = existingEntry.dateTime
        }
    }



    LaunchedEffect(showDatePicker) {
        Log.d("AddFuelScreen", "showDatePicker = $showDatePicker")
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            onSaveSuccess()
        }
    }

    LaunchedEffect(Unit) {
        Log.d("AddFuelScreen", "Edit mode = ${existingEntry != null}")
    }



    val odometerValue = odometer.toDoubleOrNull()
    val quantityValue = quantity.toDoubleOrNull()
    val totalCostValue = totalCost.toDoubleOrNull()

    val localOdometerError =
        odometer.isNotBlank() && (odometerValue == null || odometerValue < 0.0)

    val quantityError =
        quantity.isNotBlank() && (quantityValue == null || quantityValue <= 0.0)

    val totalCostError =
        totalCost.isNotBlank() && (totalCostValue == null || totalCostValue <= 0.0)

    val repoOdometerError =
        repoErrorMessage?.contains("Odometer", ignoreCase = true) == true

    val odometerError = localOdometerError || repoOdometerError

    val repoDateTimeError =
        repoErrorMessage?.contains("date and time", ignoreCase = true) == true


    val isFormValid =
        odometerValue != null && odometerValue > 0 &&
                quantityValue != null && quantityValue > 0 &&
                totalCostValue != null && totalCostValue > 0 &&
                selectedCompany != null &&
                selectedFuelType != null

    Scaffold (
        snackbarHost = { SnackbarHost(snackbarHostState)}
    ){ padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = formatter.format(Date(selectedDateTime)),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    isError = repoDateTimeError,
                    label = { Text("Date & Time") },
                    supportingText = {
                        if (repoDateTimeError) {
                            Text(
                                text = repoErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

            }

            OutlinedTextField(
                value = odometer,
                onValueChange = {
                    odometer = it

                    // Clear repo error when user edits
                    if (repoOdometerError) {
                        viewModel.clearError()
                    }
                },
                label = { Text("Odometer (km)") },
                isError = odometerError,
                supportingText = {
                    when {
                        localOdometerError ->
                            Text("Enter a valid number")

                        repoOdometerError ->
                            Text(
                                repoErrorMessage ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )


            OutlinedTextField(
                value = totalCost,
                onValueChange = { totalCost = it },
                label = { Text("Total Cost (₹)") },
                isError = totalCostError,
                supportingText = {
                    if (totalCostError) {
                        Text("Enter a valid amount")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Fuel Quantity (L)") },
                isError = quantityError,
                supportingText = {
                    if (quantityError) {
                        Text("Enter a valid decimal value")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            FuelCompanyDropdown(
                companies = fuelCompanies,
                selectedCompany = selectedCompany,
                onCompanySelected = { viewModel.onCompanySelected(it) }
            )

            FuelTypeDropdown(
                fuelTypes = availableFuelTypes,
                selectedFuelType = selectedFuelType,
                enabled = selectedCompany != null,
                onFuelTypeSelected = { selectedFuelType = it }
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
                Text("Full tank")
            }

            if (!isFormValid) {
                Text(
                    text = "Please fill all fields with valid values",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid,
                onClick = {
                    val odo = odometerValue!!
                    val total = totalCostValue!!
                    val qty = quantityValue!!
                    if (qty <= 0 || total <= 0 || odo <= 0) {
                        return@Button
                    }

                    val pricePerLitre = total/qty

                    val entry = FuelEntry(
                        id = existingEntry?.id ?: 0L,
                        dateTime = selectedDateTime,
                        odometer = odo,
                        pricePerLitre = pricePerLitre,
                        quantity = qty,
                        totalCost = total,
                        isFullTank = isFullTank,
                        fuelCompany = selectedCompany!!,
                        fuelType = selectedFuelType!!,
                        notes = notes.ifEmpty { null },
                        mileage = existingEntry?.mileage
                    )

                    viewModel.saveFuelEntry(oldEntry = existingEntry, newEntry = entry)

                },
            ) {
                Text("Save Fuel Entry")
            }
        }
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateTime,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val endOfToday = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 23)
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }.timeInMillis

                        return utcTimeMillis <= endOfToday
                    }
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val date = datePickerState.selectedDateMillis
                        if (date != null) {
                            selectedDateTime = date
                            viewModel.clearError()
                            showDatePicker = false
                            showTimePicker = true
                        }
                    }) {
                        Text("Next")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateTime
            }

            val timePickerState = rememberTimePickerState(
                initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = calendar.get(Calendar.MINUTE),
                is24Hour = true
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        selectedDateTime = calendar.timeInMillis
                        viewModel.clearError()
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Select Time") },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }



    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelCompanyDropdown(
    companies: List<FuelCompany>,
    selectedCompany: FuelCompany?,
    onCompanySelected: (FuelCompany) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedCompany?.displayName() ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Fuel Company") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            companies.forEach { company ->
                DropdownMenuItem(
                    text = { Text(company.displayName()) },
                    onClick = {
                        onCompanySelected(company)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelTypeDropdown(
    fuelTypes: List<FuelType>,
    selectedFuelType: FuelType?,
    enabled: Boolean,
    onFuelTypeSelected: (FuelType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedFuelType?.displayName() ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Fuel Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            fuelTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName()) },
                    onClick = {
                        onFuelTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

