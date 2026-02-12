package br.com.webstorage.falaserio.data.repository

import br.com.webstorage.falaserio.data.local.dao.CreditsDao
import br.com.webstorage.falaserio.data.local.entity.CreditsEntity
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para CreditsRepository.
 * 
 * Valida lógica de créditos, deduções e assinaturas.
 */
class CreditsRepositoryTest {

    private lateinit var creditsDao: CreditsDao
    private lateinit var repository: CreditsRepository

    @Before
    fun setup() {
        creditsDao = mockk()
        repository = CreditsRepository(creditsDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initializeForNewUser creates entity with 3 credits for new user`() = runTest {
        // Arrange
        coEvery { creditsDao.getCreditsOnce() } returns null
        coEvery { creditsDao.insert(any()) } just Runs

        // Act
        repository.initializeForNewUser()

        // Assert
        coVerify(exactly = 1) {
            creditsDao.insert(match {
                it.available == 3 && !it.isUnlimited
            })
        }
    }

    @Test
    fun `initializeForNewUser does nothing for existing user`() = runTest {
        // Arrange
        val existingEntity = CreditsEntity(available = 5, isUnlimited = false)
        coEvery { creditsDao.getCreditsOnce() } returns existingEntity

        // Act
        repository.initializeForNewUser()

        // Assert
        coVerify(exactly = 0) { creditsDao.insert(any()) }
    }

    @Test
    fun `useCredit decreases available credits`() = runTest {
        // Arrange
        val initialEntity = CreditsEntity(available = 5, isUnlimited = false)
        coEvery { creditsDao.getCreditsOnce() } returns initialEntity
        coEvery { creditsDao.useCredit() } returns 1

        // Act
        val result = repository.useCredit()

        // Assert
        assertTrue("Should successfully use credit", result)
        coVerify(exactly = 1) { creditsDao.useCredit() }
    }

    @Test
    fun `useCredit does nothing if unlimited`() = runTest {
        // Arrange
        val unlimitedEntity = CreditsEntity(available = Int.MAX_VALUE, isUnlimited = true)
        coEvery { creditsDao.getCreditsOnce() } returns unlimitedEntity

        // Act
        val result = repository.useCredit()

        // Assert
        assertTrue("Should allow usage with unlimited", result)
        coVerify(exactly = 0) { creditsDao.useCredit() }
    }

    @Test
    fun `useCredit returns false if no credits available`() = runTest {
        // Arrange
        val emptyEntity = CreditsEntity(available = 0, isUnlimited = false)
        coEvery { creditsDao.getCreditsOnce() } returns emptyEntity

        // Act
        val result = repository.useCredit()

        // Assert
        assertFalse("Should not allow usage without credits", result)
        coVerify(exactly = 0) { creditsDao.useCredit() }
    }

    @Test
    fun `addCredits increases available credits`() = runTest {
        // Arrange
        val initialEntity = CreditsEntity(available = 5, isUnlimited = false)
        coEvery { creditsDao.getCreditsOnce() } returns initialEntity
        coEvery { creditsDao.addCredits(any()) } just Runs

        // Act
        repository.addCredits(10)

        // Assert
        coVerify(exactly = 1) { creditsDao.addCredits(10) }
    }

    @Test
    fun `setUnlimitedCredits enables unlimited`() = runTest {
        // Arrange
        val normalEntity = CreditsEntity(available = 5, isUnlimited = false)
        coEvery { creditsDao.getCreditsOnce() } returns normalEntity
        coEvery { creditsDao.setUnlimited() } just Runs

        // Act
        repository.setUnlimitedCredits()

        // Assert
        coVerify(exactly = 1) { creditsDao.setUnlimited() }
    }

    @Test
    fun `getCredits returns flow from dao`() = runTest {
        // Arrange
        val entity = CreditsEntity(available = 10, isUnlimited = false)
        every { creditsDao.getCredits() } returns flowOf(entity)

        // Act
        val result = repository.getCredits().first()

        // Assert
        assertEquals(10, result?.available)
        assertFalse(result?.isUnlimited ?: true)
    }
}
