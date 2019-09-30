package com.github.ivpal.camunda

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.core.awaitResponseResult
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
) {
    class ListDeserializer : ResponseDeserializable<List<ExternalTask>> {
        override fun deserialize(content: String): List<ExternalTask> {
            val type = object : TypeToken<List<ExternalTask>>() {}.type
            return Gson().fromJson(content, type)
        }
    }
}

class UnitDeserializer : ResponseDeserializable<Unit> {
    override fun deserialize(content: String) {}
}

    /**
 * Represent Camunda topic
 */
data class Topic(
    val topicName: String,
    val lockDuration: Int,
    val variables: Array<String>
)

/**
 * Request object for [ExternalTaskService.fetchAndLock]
 */
data class FetchAndLockRequest(
    val workerId: String,
    val maxTasks: Int,
    val topics: Array<Topic>
)

/**
 * Represent Camunda variable
 */
data class Variable(
    val value: Any,
    val type: String
)

/**
 * Request object for [ExternalTaskService.complete]
 */
data class CompleteRequest(
    val workerId: String,
    val variables: Map<String, Variable>
)

/**
 * Request object for [ExternalTaskService.handleFailure]
 */
data class HandleFailureRequest(
    val workerId: String,
    val errorMessage: String,
    val retries: Int,
    val retryTimeout: Int
)


interface ExternalTaskService {
    /**
     * Get [ExternalTask] by [id].
     */
    suspend fun get(id: String): Result<ExternalTask, CamundaException>

    /**
     * Fetches and locks a specific number of external tasks for execution by a worker.
     */
    suspend fun fetchAndLock(request: FetchAndLockRequest): Result<List<ExternalTask>, CamundaException>

    /**
     * Complete an external task and update process variables.
     */
    suspend fun complete(id: String, request: CompleteRequest): Result<Unit, CamundaException>

    /**
     * Report a failure to execute an external task.
     */
    suspend fun handleFailure(id: String, request: HandleFailureRequest): Result<Unit, CamundaException>

    /**
     * Unlock an external task. Clears the task’s lock expiration time and worker id.
     */
    suspend fun unlock(id: String): Result<Unit, CamundaException>
}

class ExternalTaskServiceImpl : ExternalTaskService {
    override suspend fun get(id: String): Result<ExternalTask, CamundaException> {
        val (_, _, result) = "/external-task/$id".httpGet()
            .awaitResponseResult(gsonDeserializerOf(ExternalTask::class.java))
        return result.fold({ Result.success(it) }, (ExternalTaskServiceImpl)::failure)
    }

    override suspend fun fetchAndLock(request: FetchAndLockRequest): Result<List<ExternalTask>, CamundaException> {
        val (_, _, result) = "/external-task/fetchAndLock".httpPost()
            .jsonBody(request)
            .awaitResponseResult(ExternalTask.ListDeserializer())

        return result.fold({ Result.success(it) }, (ExternalTaskServiceImpl)::failure)
    }

    override suspend fun complete(id: String, request: CompleteRequest): Result<Unit, CamundaException> {
        val (_, _, result) = "/external-task/$id/complete".httpPost()
            .jsonBody(request)
            .awaitResponseResult(UnitDeserializer())

        return result.fold({ Result.success(Unit) }, (ExternalTaskServiceImpl)::failure)
    }

    override suspend fun handleFailure(id: String, request: HandleFailureRequest): Result<Unit, CamundaException> {
        val (_, _, result) = "/external-task/$id/failure".httpPost()
            .jsonBody(request)
            .awaitResponseResult(UnitDeserializer())

        return result.fold({ Result.success(Unit) }, (ExternalTaskServiceImpl)::failure)
    }

    override suspend fun unlock(id: String): Result<Unit, CamundaException> {
        val (_, _, result) = "/external-task/$id/unlock".httpPost()
            .awaitResponseResult(UnitDeserializer())

        return result.fold({ Result.success(Unit) }, (ExternalTaskServiceImpl)::failure)
    }

    companion object {
        private val gson: Gson by lazy { Gson() }

        private fun failure(e: FuelError): Result.Failure<CamundaException> {
            val errorString = String(e.errorData)
            val error = gson.fromJson(errorString, CamundaException::class.java)
            return Result.error(error)
        }
    }
}
