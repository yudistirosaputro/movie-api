package com.yudistirosaputro.movieapi.data.repository

import androidx.paging.testing.asSnapshot
import com.yudistirosaputro.movieapi.data.error.ErrorMapper
import com.yudistirosaputro.movieapi.data.remote.TmdbService
import com.yudistirosaputro.movieapi.data.remote.auth.AuthInterceptor
import com.yudistirosaputro.movieapi.data.remote.auth.TmdbCredential
import com.yudistirosaputro.movieapi.domain.model.AppError
import com.yudistirosaputro.movieapi.domain.model.Outcome
import com.yudistirosaputro.movieapi.testing.fixture
import com.yudistirosaputro.movieapi.testing.testJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MovieRepositoryImplTest {

    private val server = MockWebServer()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.close()

    private fun repository(credential: TmdbCredential = TmdbCredential.ApiKey("test-key")): MovieRepositoryImpl {
        val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor(credential)).build()
        val service = Retrofit.Builder()
            .baseUrl(server.url("/3/"))
            .client(client)
            .addConverterFactory(testJson.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbService::class.java)
        return MovieRepositoryImpl(service, ErrorMapper(testJson), Dispatchers.Unconfined)
    }

    private fun respond(code: Int = 200, body: String) {
        server.enqueue(MockResponse.Builder().code(code).addHeader("Content-Type", "application/json").body(body).build())
    }

    @Test
    fun `genres come back from the captured response`() = runTest {
        respond(body = fixture("genres.json"))

        val result = repository().getGenres()

        val genres = (result as Outcome.Success).value
        assertEquals(19, genres.size)
        assertEquals("Action", genres.first().name)
        val request = server.takeRequest()
        assertEquals("/3/genre/movie/list", request.url.encodedPath)
        assertEquals("en-US", request.url.queryParameter("language"))
        assertEquals("test-key", request.url.queryParameter("api_key"))
    }

    @Test
    fun `empty genres list is a success with no items`() = runTest {
        respond(body = """{"genres":[]}""")
        assertEquals(Outcome.Success(emptyList<Any>()), repository().getGenres())
    }

    @Test
    fun `details map from the captured response`() = runTest {
        respond(body = fixture("movie_details.json"))

        val details = (repository().getMovieDetails(969681) as Outcome.Success).value

        assertEquals(969681, details.id)
        assertEquals("Spider-Man: Brand New Day", details.title)
        assertTrue(details.backdropUrl!!.startsWith("https://image.tmdb.org/t/p/w1280/"))
        assertEquals("/3/movie/969681", server.takeRequest().url.encodedPath)
    }

    @Test
    fun `details 404 with the TMDB error body is NotFound`() = runTest {
        respond(404, """{"success":false,"status_code":34,"status_message":"The resource you requested could not be found."}""")

        assertEquals(
            Outcome.Failure(AppError.NotFound("The resource you requested could not be found.")),
            repository().getMovieDetails(1),
        )
    }

    @Test
    fun `401 is Unauthorized`() = runTest {
        respond(401, """{"success":false,"status_code":7,"status_message":"Invalid API key: You must be granted a valid key."}""")

        val error = (repository().getGenres() as Outcome.Failure).error
        assertTrue(error is AppError.Unauthorized)
    }

    @Test
    fun `malformed json is a Parsing error, not a crash`() = runTest {
        respond(body = """{"genres": [ {"id": 28, "name": "Action" """)

        assertEquals(Outcome.Failure(AppError.Parsing), repository().getGenres())
    }

    @Test
    fun `an empty 200 body is a Parsing error`() = runTest {
        respond(body = "")

        assertEquals(Outcome.Failure(AppError.Parsing), repository().getMovieDetails(1))
    }

    @Test
    fun `503 is a Server error`() = runTest {
        respond(503, "")

        assertEquals(Outcome.Failure(AppError.Server(503, null)), repository().getMovieVideos(1))
    }

    @Test
    fun `missing api key fails without touching the network`() = runTest {
        val result = repository(TmdbCredential.Missing).getGenres()

        assertEquals(Outcome.Failure(AppError.MissingApiKey), result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `videos come from the captured response`() = runTest {
        respond(body = fixture("movie_videos.json"))

        val videos = (repository().getMovieVideos(969681) as Outcome.Success).value

        assertTrue(videos.size > 100)
        assertTrue(videos.all { it.thumbnailUrl != null })
    }

    @Test
    fun `reviews page reports the total from TMDB`() = runTest {
        respond(body = fixture("movie_reviews_page1.json"))

        val page = (repository().getReviewsPage(969681) as Outcome.Success).value

        assertEquals(18, page.totalResults)
        assertEquals(18, page.items.size)
        assertEquals("1", server.takeRequest().url.queryParameter("page"))
    }

    @Test
    fun `reviews with a null avatar map to a null url`() = runTest {
        respond(
            body = """{"page":1,"total_pages":1,"total_results":1,"results":[
                {"id":"r1","author":"A","author_details":{"avatar_path":null,"rating":null},"content":"x","created_at":"2024-01-01T00:00:00.000Z"}]}""",
        )

        val review = (repository().getReviewsPage(1) as Outcome.Success).value.items.single()
        assertNull(review.avatarUrl)
        assertNull(review.rating)
    }

    @Test
    fun `discover pages through captured pages one and two`() = runTest {
        respond(body = fixture("discover_page1.json"))
        respond(body = fixture("discover_page2.json"))

        val movies = repository().discoverMovies(genreId = 28).asSnapshot { scrollTo(25) }

        assertTrue(movies.size in 38..40)
        assertEquals(movies.size, movies.map { it.id }.toSet().size)
        val first = server.takeRequest().url
        assertEquals("28", first.queryParameter("with_genres"))
        assertEquals("popularity.desc", first.queryParameter("sort_by"))
        assertEquals("false", first.queryParameter("include_adult"))
        assertEquals("1", first.queryParameter("page"))
        assertEquals("2", server.takeRequest().url.queryParameter("page"))
    }
}
