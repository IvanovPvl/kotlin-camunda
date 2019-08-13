package io.datalense.camunda

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
        val errorJson = """{
            "type": "NotFoundException",
            "message": "External task not found"
        }""".trimIndent()

        wireMockServer.stubFor(get(urlMatching(".*/external-task/$id"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(errorJson)
                .withStatus(404)))

        val client = Client("http://localhost:$port/engine-rest")
        val (task, error) = client.externalTask.get(id)
        assertNull(task)
        assertNotNull(error)

        val e = Gson().fromJson(errorJson, Error::class.java)
        error.let { ex ->
            assertEquals(e.type, ex.type)
            assertEquals(e.message, ex.message)
        }
    }

    @test fun externalTaskGet_Ok() {
        val id = "1"
        val taskJson = """{
            "activityId": "",
            "activityInstanceId": "",
            "errorMessage": "",
            "errorDetails": "",
            "executionId": "",
            "id": $id,
            "lockExpirationTime": "",
            "processDefinitionId": "",
            "processDefinitionKey": "",
            "processInstanceId": "",
            "tenantId": "",
            "retries": "",
            "suspended": false,
            "workerId": "",
            "priority": 0,
            "topicName": "",
            "businessKey": ""
        }""".trimIndent()

        wireMockServer.stubFor(get(urlMatching(".*/external-task/$id"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(taskJson)))

        val client = Client("http://localhost:$port/engine-rest")
        val (ex, error) = client.externalTask.get(id)
        ex?.let { t ->
            val task = Gson().fromJson(taskJson, ExternalTask::class.java)
            with(task) {
                assertEquals(activityId, t.activityId)
                assertEquals(activityInstanceId, t.activityInstanceId)
                assertEquals(errorMessage, t.errorMessage)
                assertEquals(errorDetails, t.errorDetails)
                assertEquals(executionId, t.executionId)
                assertEquals(id, t.id)
                assertEquals(lockExpirationTime, t.lockExpirationTime)
                assertEquals(processDefinitionId, t.processDefinitionId)
                assertEquals(processDefinitionKey, t.processDefinitionKey)
                assertEquals(processInstanceId, t.processInstanceId)
                assertEquals(tenantId, t.tenantId)
                assertEquals(retries, t.retries)
                assertEquals(suspended, t.suspended)
                assertEquals(workerId, t.workerId)
                assertEquals(priority, t.priority)
                assertEquals(topicName, t.topicName)
                assertEquals(businessKey, t.businessKey)
            }
        }
        assertNull(error)
    }

    @test fun fetchAndLock_Ok() {
        val tasksJson = """[{
            "id": "1",
            "variables": {
                "variableName": {
                    "value": "some",
                    "type": "String"
                }
            }
        }]""".trimIndent()

        wireMockServer.stubFor(post(urlMatching(".*/external-task/fetchAndLock"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(tasksJson)))

        val topics = arrayOf(Topic("topicName", 1000, arrayOf("variableName")))
        val request = FetchAndLockRequest("workerId", 1, topics)
        val client = Client("http://localhost:$port/engine-rest")
        val (tasks, error) = client.externalTask.fetchAndLock(request)

        tasks?.let { t ->
            assertEquals(1, t.size)
            val task = t[0]
            assertTrue(task.variables.containsKey("variableName"))
            val variable = task.variables["variableName"]
            assertEquals("some", variable?.value)
            assertEquals("String", variable?.type)
        }
        assertNull(error)
    }

    @test fun fetchAndLock_Error() {
        val errorJson = """{
            "type": "ServerError",
            "message": "Internal server error"
        }""".trimIndent()

        wireMockServer.stubFor(post(urlMatching(".*/external-task/fetchAndLock"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(500)
                    .withBody(errorJson)))

        val client = Client("http://localhost:$port/engine-rest")
        val (tasks, error) = client.externalTask.fetchAndLock(FetchAndLockRequest("", 0, emptyArray()))
        assertNull(tasks)
        assertNotNull(error)

        val e = Gson().fromJson(errorJson, Error::class.java)
        error.let { ex ->
            assertEquals(e.type, ex.type)
            assertEquals(e.message, ex.message)
        }
    }

    @test fun complete_Ok() {
        val id = "1"
        wireMockServer.stubFor(post(urlMatching(".*/external-task/$id/complete"))
            .willReturn(aResponse().withStatus(200)))

        val client = Client("http://localhost:$port/engine-rest")
        val variables = mapOf("some" to Variable("value", "String"))
        val (response, error) = client.externalTask.complete(id, CompleteRequest("workerId", variables))
        assertEquals(Unit, response)
        assertNull(error)
    }

    @test fun complete_Error() {
        val id = "1"
        val errorJson = """{
            "type": "ServerError",
            "message": "Internal server error"
        }""".trimIndent()

        wireMockServer.stubFor(post(urlMatching(".*/external-task/$id/complete"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(500)
                    .withBody(errorJson)))

        val client = Client("http://localhost:$port/engine-rest")
        val variables = mapOf("some" to Variable("value", "String"))
        val (response, error) = client.externalTask.complete(id, CompleteRequest("workerId", variables))
        assertNull(response)

        val e = Gson().fromJson(errorJson, Error::class.java)
        error?.let { ex ->
            assertEquals(e.type, ex.type)
            assertEquals(e.message, ex.message)
        }
    }

    @test fun handleFailure_Ok() {
        val id = "1"
        wireMockServer.stubFor(post(urlMatching(".*/external-task/$id/failure"))
            .willReturn(aResponse().withStatus(204)))

        val client = Client("http://localhost:$port/engine-rest")
        val request = HandleFailureRequest("workerId", "Error", 1, 1000)
        val (response, error) = client.externalTask.handleFailure(id, request)
        assertEquals(Unit, response)
        assertNull(error)
    }

    @test fun handleFailure_Error() {
        val id = "1"
        val errorJson = """{
            "type": "ServerError",
            "message": "Internal server error"
        }""".trimIndent()

        wireMockServer.stubFor(post(urlMatching(".*/external-task/$id/failure"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(500)
                    .withBody(errorJson)))

        val client = Client("http://localhost:$port/engine-rest")
        val request = HandleFailureRequest("workerId", "Error", 1, 1000)
        val (response, error) = client.externalTask.handleFailure(id, request)
        assertNull(response)

        val e = Gson().fromJson(errorJson, Error::class.java)
        error?.let { ex ->
            assertEquals(e.type, ex.type)
            assertEquals(e.message, ex.message)
        }
    }

    @test fun handleUnlock_Ok() {
        val id = "1"
        wireMockServer.stubFor(post(urlMatching(".*/external-task/$id/unlock"))
            .willReturn(aResponse().withStatus(204)))

        val client = Client("http://localhost:$port/engine-rest")
        val (response, error) = client.externalTask.unlock(id)
        assertEquals(Unit, response)
        assertNull(error)
    }

    @test fun handleUnlock_Error() {
        val id = "1"
        val errorJson = """{
            "type": "ServerError",
            "message": "Internal server error"
        }""".trimIndent()

        wireMockServer.stubFor(post(urlMatching(".*/external-task/$id/unlock"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(500)
                    .withBody(errorJson)))

        val client = Client("http://localhost:$port/engine-rest")
        val (response, error) = client.externalTask.unlock(id)
        assertNull(response)

        val e = Gson().fromJson(errorJson, Error::class.java)
        error?.let { ex ->
            assertEquals(e.type, ex.type)
            assertEquals(e.message, ex.message)
        }
    }
}