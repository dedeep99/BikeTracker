package com.soumya.biketracker.ui.fuel

import com.soumya.biketracker.domain.FuelCompany
import com.soumya.biketracker.domain.FuelType

fun FuelCompany.displayName(): String = when (this) {
    FuelCompany.INDIAN_OIL -> "Indian Oil"
    FuelCompany.HP -> "HP"
    FuelCompany.BPCL -> "BPCL"
    FuelCompany.SHELL -> "Shell"
    FuelCompany.JIO_BP -> "Jio-bp"
}

fun FuelType.displayName(): String = when (this) {
    FuelType.NORMAL -> "Normal"
    FuelType.XP95 -> "XP95"
    FuelType.XP100 -> "XP100"
    FuelType.SPEED -> "Speed"
    FuelType.SPEED97 -> "Speed 97"
    FuelType.POWER -> "Power"
    FuelType.POWER99 -> "Power 99"
    FuelType.POWER100 -> "Power 100"
    FuelType.V_POWER -> "V-Power"
    FuelType.JIO_ACTIVE -> "Jio-bp Active"
    FuelType.JIO_PREMIUM -> "Jio-bp Premium"
}
