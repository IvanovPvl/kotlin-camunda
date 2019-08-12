package io.datalense.camunda

import org.junit.jupiter.api.assertThrows
import kotlin.test.*
import org.junit.jupiter.api.Test as test

class ResultTest {
    @test fun success() {
        val v = 1
        val result = Result.success(1)
        val (r, e) = result
        assertNotNull(r)
        assertEquals(v, r)
        assertNull(e)
        assertEquals(v, result.get())
    }

    @test fun failure() {
        val error = Error("type", "message")
        val result = Result.failure(error)
        val (r, e) = result
        assertNull(r)
        assertNotNull(e)
        assertEquals(error, e)
        assertThrows<ResultException>(error.message) { result.get() }
    }
}