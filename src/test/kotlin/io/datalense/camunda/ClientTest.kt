package io.datalense.camunda

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test as test

class ClientTest {
    companion object {
        val port = Random.nextInt(30000)
        lateinit var wireMockServer: WireMockServer
    }

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(port)
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @test fun externalTaskGet_NotFound() {
        val id = "1"
        wireMockServer.stubFor(get(urlMatching(".*/external-task/$id"))
            .willReturn(aResponse().withStatus(404)))

        val client = Client("http://localhost:$port/engine-rest")
        val task = client.externalTask.get(id)
        assertNull(task)
    }

    @test fun externalTaskGet_Ok() {
        val id = "1"
        val taskJson = """{
            "id": $id
        }""".trimIndent()

        wireMockServer.stubFor(get(urlMatching(".*/external-task/$id"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(taskJson)))

        val client = Client("http://localhost:$port/engine-rest")
        val task = client.externalTask.get(id)
        assertEquals(Gson().fromJson(taskJson, ExternalTask::class.java).id, task?.id)
    }
}