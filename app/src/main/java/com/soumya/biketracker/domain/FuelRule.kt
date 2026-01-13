package com.soumya.biketracker.domain

object FuelRules {

    private val map = mapOf(
        FuelCompany.INDIAN_OIL to listOf(
            FuelType.NORMAL,
            FuelType.XP95,
            FuelType.XP100
        ),
        FuelCompany.HP to listOf(
            FuelType.NORMAL,
            FuelType.POWER,
            FuelType.POWER99,
            FuelType.POWER100
        ),
        FuelCompany.BPCL to listOf(
            FuelType.NORMAL,
            FuelType.SPEED,
            FuelType.SPEED97
        ),
        FuelCompany.SHELL to listOf(
            FuelType.NORMAL,
            FuelType.V_POWER
        ),
        FuelCompany.JIO_BP to listOf(
            FuelType.NORMAL,
            FuelType.JIO_ACTIVE,
            FuelType.JIO_PREMIUM
        )
    )

    fun allowedFuelTypes(company: FuelCompany): List<FuelType> =
        map[company] ?: emptyList()
}
