package com.s8139858.assignment2.data.repository

import com.google.common.truth.Truth.assertThat
import com.s8139858.assignment2.data.mapper.EntityMapper
import com.s8139858.assignment2.data.remote.ApiService
import com.s8139858.assignment2.util.ApiResult
import com.s8139858.assignment2.util.AppError
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Integration-style unit tests for [AppRepositoryImpl]: a real Retrofit + OkHttp
 * stack talks to a local [MockWebServer] (a fake HTTP server), so the repository
 * and the network layer are tested *together* — request paths, JSON bodies and
 * error mapping — without touching the real API. This sits between a pure unit
 * test and a UI test: multiple components verified as a group.
 */
class AppRepositoryImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: AppRepositoryImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
        val apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        repository = AppRepositoryImpl(
            apiService = apiService,
            entityMapper = EntityMapper(),
            campus = "sydney",
            ioDispatcher = Dispatchers.Unconfined,
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `given valid response when login then posts credentials to campus auth and returns keypass`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"keypass":"books"}"""),
        )

        val result = repository.login("08139858", "Sulav")

        assertThat(result).isEqualTo(ApiResult.Success("books"))
        val request = mockWebServer.takeRequest()
        assertThat(request.method).isEqualTo("POST")
        assertThat(request.path).isEqualTo("/sydney/auth")
        // Leading zero must survive serialisation as a JSON string.
        assertThat(request.body.readUtf8()).isEqualTo("""{"username":"08139858","password":"Sulav"}""")
    }

    @Test
    fun `given 401 response when login then failure is invalid credentials`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val result = repository.login("08139858", "Wrong")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.InvalidCredentials))
    }

    @Test
    fun `given 400 response when login then failure is invalid credentials`() = runTest {
        // Observed behaviour of the real API for a wrong password.
        mockWebServer.enqueue(MockResponse().setResponseCode(400))

        val result = repository.login("8139858", "WrongPassword")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.InvalidCredentials))
    }

    @Test
    fun `given 404 response when login then failure is invalid credentials`() = runTest {
        // Observed behaviour of the real API for unknown users.
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        val result = repository.login("unknown", "Wrong")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.InvalidCredentials))
    }

    @Test
    fun `given 500 response when dashboard then failure is server error with code`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.getDashboard("books")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.Server(500)))
    }

    @Test
    fun `given socket timeout when login then failure is timeout`() = runTest {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val result = repository.login("08139858", "Sulav")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.Timeout))
    }

    @Test
    fun `given disconnect when dashboard then failure is no network`() = runTest {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = repository.getDashboard("books")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.NoNetwork))
    }

    @Test
    fun `given malformed auth body when login then failure is malformed response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""{"unexpected":true}"""),
        )

        val result = repository.login("08139858", "Sulav")

        assertThat(result).isEqualTo(ApiResult.Failure(AppError.MalformedResponse))
    }

    @Test
    fun `given entities json when dashboard then dto is mapped to domain in order`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(
                    """
                    {
                      "entities": [
                        {
                          "title": "1984",
                          "author": "George Orwell",
                          "publicationYear": 1949,
                          "description": "A dystopian novel."
                        }
                      ],
                      "entityTotal": 7
                    }
                    """.trimIndent(),
                ),
        )

        val result = repository.getDashboard("books")

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val data = (result as ApiResult.Success).data
        assertThat(data.entityTotal).isEqualTo(7)
        assertThat(data.entities).hasSize(1)
        val entity = data.entities[0]
        assertThat(entity.fields.map { it.label })
            .containsExactly("Title", "Author", "Publication Year").inOrder()
        assertThat(entity.fields.map { it.value })
            .containsExactly("1984", "George Orwell", "1949").inOrder()
        assertThat(entity.description).isEqualTo("A dystopian novel.")
        // Description must never leak into the field list.
        assertThat(entity.fields.map { it.label }).doesNotContain("Description")
    }
}
