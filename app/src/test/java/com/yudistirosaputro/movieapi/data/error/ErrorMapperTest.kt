package com.yudistirosaputro.movieapi.data.error

import com.yudistirosaputro.movieapi.data.remote.auth.MissingApiKeyException
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.AppException
import com.yudistirosaputro.movieapi.testing.testJson
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMapperTest {

    private val mapper = ErrorMapper(testJson)

    private fun http(code: Int, body: String = "") =
        HttpException(Response.error<Any>(code, body.toResponseBody("application/json".toMediaType())))

    private fun tmdbBody(code: Int, message: String) = """{"success":false,"status_code":$code,"status_message":"$message"}"""

    @Test
    fun `401 maps to Unauthorized with the TMDB status message`() {
        val error = mapper.map(http(401, tmdbBody(7, "Invalid API key: You must be granted a valid key.")))
        assertEquals(AppError.Unauthorized("Invalid API key: You must be granted a valid key."), error)
    }

    @Test
    fun `404 maps to NotFound`() {
        assertEquals(
            AppError.NotFound("The resource you requested could not be found."),
            mapper.map(http(404, tmdbBody(34, "The resource you requested could not be found."))),
        )
    }

    @Test
    fun `429 maps to RateLimited`() {
        assertEquals(AppError.RateLimited(null), mapper.map(http(429)))
    }

    @Test
    fun `5xx maps to Server with its code`() {
        assertEquals(AppError.Server(500, null), mapper.map(http(500)))
        assertEquals(AppError.Server(503, "Service unavailable"), mapper.map(http(503, tmdbBody(9, "Service unavailable"))))
    }

    @Test
    fun `other http codes map to Unknown`() {
        assertEquals(AppError.Unknown(400, null), mapper.map(http(400)))
    }

    @Test
    fun `an unparseable error body is ignored`() {
        assertEquals(AppError.Server(502, null), mapper.map(http(502, "<html>Bad gateway</html>")))
    }

    @Test
    fun `connectivity failures map to NoInternet`() {
        assertEquals(AppError.NoInternet, mapper.map(UnknownHostException("api.themoviedb.org")))
        assertEquals(AppError.NoInternet, mapper.map(ConnectException()))
        assertEquals(AppError.NoInternet, mapper.map(IOException("connection reset")))
    }

    @Test
    fun `timeouts map to Timeout`() {
        assertEquals(AppError.Timeout, mapper.map(SocketTimeoutException()))
        assertEquals(AppError.Timeout, mapper.map(InterruptedIOException("timeout")))
    }

    @Test
    fun `serialization failures map to Parsing`() {
        assertEquals(AppError.Parsing, mapper.map(SerializationException("Unexpected JSON token")))
    }

    @Test
    fun `a missing key maps to MissingApiKey even though it is an IOException`() {
        assertEquals(AppError.MissingApiKey, mapper.map(MissingApiKeyException()))
    }

    @Test
    fun `an AppException keeps its error`() {
        assertEquals(AppError.Timeout, mapper.map(AppException(AppError.Timeout)))
    }

    @Test
    fun `anything else maps to Unknown`() {
        assertEquals(AppError.Unknown(), mapper.map(IllegalStateException("boom")))
    }
}
