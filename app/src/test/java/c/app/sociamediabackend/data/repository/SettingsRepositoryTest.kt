package c.app.sociamediabackend.data.repository

import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.model.UserSettings
import c.app.sociamediabackend.ui.auth.FakeApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun getSettings_returnsDefaultUserSettings() = runBlocking {
        val fakeApi = FakeApiService()
        val repository = SettingsRepository(fakeApi)

        val result = repository.getSettings()

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertTrue(data.success)
        assertEquals(UserSettings(), data.settings)
    }

    @Test
    fun updateSettings_returnsUpdatedUserSettings() = runBlocking {
        val fakeApi = FakeApiService()
        val repository = SettingsRepository(fakeApi)

        val result = repository.updateSettings(mapOf("isPrivateAccount" to true))

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertTrue(data.success)
        assertEquals(true, data.settings?.isPrivateAccount)
    }
}
