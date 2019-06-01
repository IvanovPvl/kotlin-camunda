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
        assertEquals(e, error)
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
        val (ex, _) = client.externalTask.get(id)
        ex?.let { t ->
            val task = Gson().fromJson(taskJson, ExternalTask::class.java)
            assertEquals(task.activityId, t.activityId)
            assertEquals(task.activityInstanceId, t.activityInstanceId)
            assertEquals(task.errorMessage, t.errorMessage)
            assertEquals(task.errorDetails, t.errorDetails)
            assertEquals(task.executionId, t.executionId)
            assertEquals(task.id, t.id)
            assertEquals(task.lockExpirationTime, t.lockExpirationTime)
            assertEquals(task.processDefinitionId, t.processDefinitionId)
            assertEquals(task.processDefinitionKey, t.processDefinitionKey)
            assertEquals(task.processInstanceId, t.processInstanceId)
            assertEquals(task.tenantId, t.tenantId)
            assertEquals(task.retries, t.retries)
            assertEquals(task.suspended, t.suspended)
            assertEquals(task.workerId, t.workerId)
            assertEquals(task.priority, t.priority)
            assertEquals(task.topicName, t.topicName)
            assertEquals(task.businessKey, t.businessKey)
        }

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
        val tasks = client.externalTask.fetchAndLock(request)

        tasks?.let { t ->
            assertEquals(1, t.size)
            val task = t[0]
            assertTrue(task.variables.containsKey("variableName"))
            val variable = task.variables["variableName"]
            assertEquals("some", variable?.value)
            assertEquals("String", variable?.type)
        }
    }
}