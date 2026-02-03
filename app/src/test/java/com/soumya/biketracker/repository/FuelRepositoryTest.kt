import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.soumya.biketracker.data.dao.FuelDao
import com.soumya.biketracker.data.database.AppDatabase
import com.soumya.biketracker.data.entity.FuelEntry
import com.soumya.biketracker.domain.FuelCompany
import com.soumya.biketracker.domain.FuelType
import com.soumya.biketracker.repository.FuelRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(manifest= Config.NONE)
class FuelRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FuelDao
    private lateinit var repository: FuelRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = db.fuelDao()
        repository = FuelRepository(dao)
    }

    @Test
    fun first_full_tank_has_null_mileage() = runTest {

        repository.insertFuelEntry(
            fullTank(1000, 100.0, 5.0)
        )

        val entry = dao.getAllOnce().single()
        assertNull(entry.mileage)
    }


    @Test
    fun mileage_between_two_full_tanks_is_calculated_correctly() = runTest {

        repository.insertFuelEntry(
            fullTank(1000, 100.0, 5.0)
        )
        repository.insertFuelEntry(
            fullTank(2000, 200.0, 5.0)
        )

        val second = dao.getAllOnce().last()

        // (200 - 100) / 5
        assertEquals(20.0, second.mileage!!, 0.01)
    }


    @Test
    fun partial_tanks_between_full_tanks_are_counted() = runTest {

        repository.insertFuelEntry(
            fullTank(1000, 100.0, 5.0)
        )

        repository.insertFuelEntry(
            partialTank(1100, 130.0, 3.0)
        )

        repository.insertFuelEntry(
            partialTank(1200, 160.0, 2.0)
        )

        repository.insertFuelEntry(
            fullTank(2000, 200.0, 5.0)
        )

        val full2 = dao.getAllOnce().last()

        // fuel = 3 + 2 + 5 = 10
        // distance = 200 - 100 = 100
        assertEquals(10.0, full2.mileage!!, 0.01)
    }



    @Test
    fun editing_partial_tank_recalculates_next_full_tank() = runTest {

        repository.insertFuelEntry(fullTank(1000, 100.0, 5.0))
        repository.insertFuelEntry(partialTank(1100, 130.0, 2.0))
        repository.insertFuelEntry(fullTank(2000, 200.0, 5.0))

        val partial = dao.getAllOnce().first { !it.isFullTank }

        repository.updateFuelEntry(
            oldEntry = partial,
            newEntry = partial.copy(quantity = 5.0)
        )

        val updatedFull = dao.getAllOnce().last()

        // fuel = 5 (partial) + 5 (full)
        // distance = 100
        assertEquals(10.0, updatedFull.mileage!!, 0.01)
    }


    @Test
    fun editing_previous_full_tank_quantity_does_not_affect_next_mileage() = runTest {

        repository.insertFuelEntry(fullTank(1000, 100.0, 5.0))
        repository.insertFuelEntry(fullTank(2000, 200.0, 5.0))

        val firstFull = dao.getAllOnce().first()

        repository.updateFuelEntry(
            oldEntry = firstFull,
            newEntry = firstFull.copy(quantity = 10.0)
        )

        val secondFull = dao.getAllOnce().last()

        // fuel = only current full tank = 5
        // distance = 100
        assertEquals(20.0, secondFull.mileage!!, 0.01)
    }


    @Test
    fun editing_previous_full_tank_odometer_affects_next_mileage() = runTest {

        repository.insertFuelEntry(fullTank(1000, 100.0, 5.0))
        repository.insertFuelEntry(fullTank(2000, 200.0, 5.0))

        val firstFull = dao.getAllOnce().first()

        repository.updateFuelEntry(
            oldEntry = firstFull,
            newEntry = firstFull.copy(odometer = 120.0)
        )

        val secondFull = dao.getAllOnce().last()

        // distance = 200 - 120 = 80
        // fuel = 5
        assertEquals(16.0, secondFull.mileage!!, 0.01)
    }

    @Test
    fun smaller_odometer_than_previous_entry_throws() = runTest {

        repository.insertFuelEntry(
            fullTank(1000, 200.0, 5.0)
        )

        assertFailsWith<IllegalArgumentException> {
            repository.insertFuelEntry(
                fullTank(2000, 150.0, 5.0)
            )
        }
    }

    @Test
    fun updating_f3_quantity_updates_f3_mileage_only() = runTest {

        // F1
        repository.insertFuelEntry(
            fullTank(time = 1000, odo = 100.0, qty = 5.0)
        )

        // F2
        repository.insertFuelEntry(
            fullTank(time = 2000, odo = 200.0, qty = 5.0)
        )

        // F3
        repository.insertFuelEntry(
            fullTank(time = 3000, odo = 300.0, qty = 5.0)
        )

        // F4
        repository.insertFuelEntry(
            fullTank(time = 4000, odo = 400.0, qty = 5.0)
        )

        val before = dao.getAllOnce()

        val f3Before = before[2]
        val f4Before = before[3]

        // sanity check
        assertEquals(20.0, f3Before.mileage!!, 0.01)
        assertEquals(20.0, f4Before.mileage!!, 0.01)

        // 🔧 Update F3 quantity
        val f3Updated = f3Before.copy(quantity = 10.0)

        repository.updateFuelEntry(
            oldEntry = f3Before,
            newEntry = f3Updated
        )

        val after = dao.getAllOnce()

        val f3After = after[2]
        val f4After = after[3]

        // EXPECTED:
        // distance F2→F3 = 100
        // fuel = 10
        assertEquals(10.0, f3After.mileage!!, 0.01)

        // F4 should remain unchanged
        assertEquals(20.0, f4After.mileage!!, 0.01)
    }

    @Test
    fun updating_f2_odometer_should_update_mileage_of_f2_and_f3_only() = runTest {

        // Arrange: insert 4 full tanks
        repository.insertFuelEntry(
            fullTank(time = 1000, odo = 100.0, qty = 5.0)
        )

        repository.insertFuelEntry(
            fullTank(time = 2000, odo = 200.0, qty = 5.0)
        )

        repository.insertFuelEntry(
            fullTank(time = 3000, odo = 300.0, qty = 5.0)
        )

        repository.insertFuelEntry(
            fullTank(time = 4000, odo = 400.0, qty = 5.0)
        )

        val original = dao.getAllOnce()

        val f2Old = original[1]

        // Act: update F2 odometer
        val f2New = f2Old.copy(odometer = 250.0)

        repository.updateFuelEntry(
            oldEntry = f2Old,
            newEntry = f2New
        )

        // Assert
        val updated = dao.getAllOnce()

        val f2 = updated[1]
        val f3 = updated[2]
        val f4 = updated[3]

        assertEquals(
            30.0,
            f2.mileage!!,
            0.01
        )

        assertEquals(
            10.0,
            f3.mileage!!,
            0.01
        )

        assertEquals(
            20.0,
            f4.mileage!!,
            0.01
        )
    }


    @Test
    fun updating_partial_tank_should_recalculate_only_next_full_tank() = runTest {

        // F1
        repository.insertFuelEntry(
            fullTank(time = 1000, odo = 100.0, qty = 5.0)
        )

        // P1 (partial)
        repository.insertFuelEntry(
            partialTank(time = 1500, odo = 150.0, qty = 5.0)
        )

        // F2
        repository.insertFuelEntry(
            fullTank(time = 2000, odo = 200.0, qty = 5.0)
        )

        // F3
        repository.insertFuelEntry(
            fullTank(time = 3000, odo = 300.0, qty = 5.0)
        )

        val before = dao.getAllOnce()

        val f2Before = before[2]
        val f3Before = before[3]

        // sanity
        assertEquals(10.0, f2Before.mileage!!, 0.01)
        assertEquals(20.0, f3Before.mileage!!, 0.01)

        // 🔧 Update P1 quantity
        val p1Old = before[1]
        val p1New = p1Old.copy(quantity = 10.0)

        repository.updateFuelEntry(
            oldEntry = p1Old,
            newEntry = p1New
        )

        val after = dao.getAllOnce()

        val f2After = after[2]
        val f3After = after[3]

        // F2 mileage MUST change
        assertEquals(
            6.666,
            f2After.mileage!!,
            0.01
        )

        // F3 mileage MUST remain unchanged
        assertEquals(
            20.0,
            f3After.mileage!!,
            0.01
        )
    }






    @After
    fun tearDown() {
        db.close()
    }

    private fun fullTank(
        time: Long,
        odo: Double,
        qty: Double
    ) = FuelEntry(
        dateTime = time,
        odometer = odo,
        quantity = qty,
        pricePerLitre = 100.0,
        totalCost = qty * 100,
        isFullTank = true,
        fuelCompany = FuelCompany.HP,
        fuelType = FuelType.NORMAL,
        notes = null,
        mileage = null
    )

    private fun partialTank(
        time: Long,
        odo: Double,
        qty: Double
    ) = FuelEntry(
        dateTime = time,
        odometer = odo,
        quantity = qty,
        pricePerLitre = 100.0,
        totalCost = qty * 100,
        isFullTank = false,
        fuelCompany = FuelCompany.HP,
        fuelType = FuelType.NORMAL,
        notes = null,
        mileage = null
    )

}
