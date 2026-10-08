package com.yudistirosaputro.movieapi.data.remote.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TmdbCredentialTest {

    private val v3Key = "fake-v3-key-for-tests"
    private val v4Token = "eyJfake.test.token"

    @Test
    fun `null, empty and blank values are missing`() {
        assertEquals(TmdbCredential.Missing, TmdbCredential.from(null))
        assertEquals(TmdbCredential.Missing, TmdbCredential.from(""))
        assertEquals(TmdbCredential.Missing, TmdbCredential.from("   "))
        assertEquals(TmdbCredential.Missing, TmdbCredential.from("\"\""))
    }

    @Test
    fun `anything that is not a JWT is a v3 api key`() {
        val credential = TmdbCredential.from(v3Key)
        assertTrue(credential is TmdbCredential.ApiKey)
        assertEquals(v3Key, (credential as TmdbCredential.ApiKey).value)
    }

    @Test
    fun `a JWT is a v4 bearer token`() {
        val credential = TmdbCredential.from(v4Token)
        assertTrue(credential is TmdbCredential.BearerToken)
        assertEquals(v4Token, (credential as TmdbCredential.BearerToken).value)
    }

    @Test
    fun `surrounding quotes and whitespace from gradle properties are stripped`() {
        assertEquals(v4Token, (TmdbCredential.from("  \"$v4Token\" ") as TmdbCredential.BearerToken).value)
        assertEquals(v3Key, (TmdbCredential.from("'$v3Key'") as TmdbCredential.ApiKey).value)
    }

    @Test
    fun `toString never reveals the secret`() {
        assertFalse(TmdbCredential.from(v3Key).toString().contains(v3Key))
        assertFalse(TmdbCredential.from(v4Token).toString().contains(v4Token))
    }
}
