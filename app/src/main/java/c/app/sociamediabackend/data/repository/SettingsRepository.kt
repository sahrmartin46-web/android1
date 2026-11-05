package c.app.sociamediabackend.data.repository

import c.app.sociamediabackend.data.api.ApiService
import c.app.sociamediabackend.data.api.NetworkResult
import c.app.sociamediabackend.data.api.safeApiCall
import c.app.sociamediabackend.data.model.*

class SettingsRepository(private val apiService: ApiService) {

    suspend fun getSettings(): NetworkResult<UserSettingsResponse> {
        return safeApiCall { apiService.getSettings() }
    }

    suspend fun updateSettings(updateData: Map<String, Any>): NetworkResult<UserSettingsResponse> {
        return safeApiCall { apiService.updateSettings(updateData) }
    }

    suspend fun changePassword(oldPass: String, newPass: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.changePassword(mapOf("oldPassword" to oldPass, "newPassword" to newPass)) }
    }

    suspend fun switchAccountType(targetRole: String): NetworkResult<AuthResponse> {
        return safeApiCall { apiService.switchAccountType(mapOf("targetRole" to targetRole)) }
    }

    suspend fun getSessions(): NetworkResult<UserSessionsResponse> {
        return safeApiCall { apiService.getSessions() }
    }

    suspend fun revokeSession(sessionId: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.revokeSession(sessionId) }
    }

    suspend fun getBlockedUsers(): NetworkResult<BlockedUsersResponse> {
        return safeApiCall { apiService.getBlockedUsers() }
    }

    suspend fun blockUser(targetUserId: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.blockUser(mapOf("targetUserId" to targetUserId)) }
    }

    suspend fun unblockUser(targetUserId: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.unblockUser(targetUserId) }
    }

    suspend fun getKeywordFilters(): NetworkResult<KeywordFiltersResponse> {
        return safeApiCall { apiService.getKeywordFilters() }
    }

    suspend fun addKeywordFilter(keyword: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.addKeywordFilter(mapOf("keyword" to keyword)) }
    }

    suspend fun deleteKeywordFilter(keyword: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.deleteKeywordFilter(keyword) }
    }

    suspend fun exportUserData(): NetworkResult<DataExportResponse> {
        return safeApiCall { apiService.exportUserData() }
    }

    suspend fun deactivateAccount(): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.deactivateAccount() }
    }

    suspend fun deleteAccount(): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.deleteAccount() }
    }

    suspend fun getBankAccounts(): NetworkResult<BankAccountsResponse> {
        return safeApiCall { apiService.getBankAccounts() }
    }

    suspend fun addBankAccount(data: Map<String, Any>): NetworkResult<BankAccountsResponse> {
        return safeApiCall { apiService.addBankAccount(data) }
    }

    suspend fun deleteBankAccount(id: String): NetworkResult<GenericResponse> {
        return safeApiCall { apiService.deleteBankAccount(id) }
    }
}
