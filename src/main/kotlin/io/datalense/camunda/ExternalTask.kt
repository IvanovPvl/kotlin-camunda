package io.datalense.camunda

import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson

/**
 * Represent Camunda external task
 */
data class ExternalTask(
    val activityId: String,
    val activityInstanceId: String,
    val errorMessage: String,
    val errorDetails: String,
    val executionId: String,
    val id: String,
    val lockExpirationTime: String,
    val processDefinitionId: String,
    val processDefinitionKey: String,
    val processInstanceId: String,
    val tenantId: String,
    val retries: String,
    val suspended: Boolean,
    val workerId: String,
    val priority: Int,
    val topicName: String,
    val businessKey: String,
    val variables: Map<String, Variable>
)

data class Topic(
    val topicName: String,
    val lockDuration: Int,
    val variables: Array<String>
)

data class FetchAndLockRequest(
    val workerId: String,
    val maxTasks: Int,
    val topics: Array<Topic>
)

data class Variable(
    val value: Any,
    val type: String
)

data class CompleteRequest(
    val workerId: String,
    val variables: Map<String, Variable>
)

interface ExternalTaskService {
    /**
     * Get [ExternalTask] by [id].
     */
    fun get(id: String): Result<ExternalTask>

    /**
     * Fetches and locks a specific number of external tasks for execution by a worker.
     */
    fun fetchAndLock(request: FetchAndLockRequest): Result<Array<ExternalTask>>

    /**
     * Complete an external task and update process variables.
     */
    fun complete(id: String, request: CompleteRequest): Result<Unit>
}

class ExternalTaskServiceImpl : ExternalTaskService {
    override fun get(id: String): Result<ExternalTask> {
        val (_, _, result) = "/external-task/$id".httpGet()
            .responseObject<ExternalTask>()

        return result.fold({ Result.success(it) }, { e ->
            val errorString = String(e.errorData)
            Result.failure(Gson().fromJson(errorString, Error::class.java))
        })
    }

    override fun fetchAndLock(request: FetchAndLockRequest): Result<Array<ExternalTask>> {
        val (_, _, result) = "/external-task/fetchAndLock".httpPost()
            .jsonBody(request)
            .responseObject<Array<ExternalTask>>()

        return result.fold({ Result.success(it) }, { e ->
            val errorString = String(e.errorData)
            Result.failure(Gson().fromJson(errorString, Error::class.java))
        })
    }

    override fun complete(id: String, request: CompleteRequest): Result<Unit> {
        val (_, _, result) = "/external-task/$id/complete".httpPost()
            .jsonBody(request)
            .response()

        return result.fold({ Result.success(Unit) }, { e ->
            val errorString = String(e.errorData)
            Result.failure(Gson().fromJson(errorString, Error::class.java))
        })
    }
}