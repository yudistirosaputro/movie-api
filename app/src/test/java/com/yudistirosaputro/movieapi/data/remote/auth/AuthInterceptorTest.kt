package com.yudistirosaputro.movieapi.data.remote.auth

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun send(credential: TmdbCredential) {
        server.enqueue(MockResponse.Builder().code(200).body("{}").build())
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(credential)).build()
        client.newCall(Request.Builder().url(server.url("/3/genre/movie/list?language=en-US")).build()).execute().close()
    }

    @Test
    fun `v3 key is appended as the api_key query parameter`() {
        send(TmdbCredential.ApiKey("fake-v3-key-for-tests"))

        val request = server.takeRequest()
        assertEquals("fake-v3-key-for-tests", request.url.queryParameter("api_key"))
        assertEquals("en-US", request.url.queryParameter("language"))
        assertNull(request.headers["Authorization"])
    }

    @Test
    fun `v4 token is sent as a bearer header and never in the url`() {
        send(TmdbCredential.BearerToken("eyJtoken.payload.sig"))

        val request = server.takeRequest()
        assertEquals("Bearer eyJtoken.payload.sig", request.headers["Authorization"])
        assertNull(request.url.queryParameter("api_key"))
    }

    @Test
    fun `json is requested`() {
        send(TmdbCredential.ApiKey("key"))

        assertEquals("application/json", server.takeRequest().headers["Accept"])
    }

    @Test(expected = MissingApiKeyException::class)
    fun `missing credential fails before any request is sent`() {
        try {
            send(TmdbCredential.Missing)
        } finally {
            assertEquals(0, server.requestCount)
        }
    }
}
