package com.s8139858.assignment2.data.repository

import com.s8139858.assignment2.data.mapper.EntityMapper
import com.s8139858.assignment2.data.remote.ApiService
import com.s8139858.assignment2.data.remote.dto.AuthRequest
import com.s8139858.assignment2.di.IoDispatcher
import com.s8139858.assignment2.domain.model.DashboardData
import com.s8139858.assignment2.domain.repository.AppRepository
import com.s8139858.assignment2.util.ApiResult
import com.s8139858.assignment2.util.MalformedResponseException
import com.s8139858.assignment2.util.safeApiCall
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

/**
 * Default [AppRepository] implementation backed by the nit3213 API.
 *
 * Repository pattern: ViewModels depend only on the [AppRepository] interface,
 * so this class is the single place that knows where data comes from (network)
 * and how exceptions become [AppError]s. Business logic stays in the ViewModels.
 *
 * Everything it needs arrives through constructor injection — the service, the
 * mapper, the campus path segment and the IO dispatcher — so unit tests inject
 * a fake ApiService (MockWebServer) and a test dispatcher instead of the network.
 */
@Singleton
class AppRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val entityMapper: EntityMapper,
    @Named("campus") private val campus: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AppRepository {

    override suspend fun login(username: String, password: String): ApiResult<String> =
        safeApiCall(ioDispatcher) {
            val response = apiService.login(campus, AuthRequest(username = username, password = password))
            response.keypass?.takeIf { it.isNotBlank() } ?: throw MalformedResponseException()
        }

    override suspend fun getDashboard(keypass: String): ApiResult<DashboardData> =
        safeApiCall(ioDispatcher) {
            val response = apiService.getDashboard(keypass)
            DashboardData(
                entities = entityMapper.mapAll(response),
                entityTotal = response.entityTotal ?: response.entities?.size ?: 0,
            )
        }
}
