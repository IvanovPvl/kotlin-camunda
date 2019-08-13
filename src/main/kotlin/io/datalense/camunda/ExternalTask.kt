package io.datalense.camunda

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
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
    fun get(id: String): Result<ExternalTask, CamundaException>

    /**
     * Fetches and locks a specific number of external tasks for execution by a worker.
     */
    fun fetchAndLock(request: FetchAndLockRequest): Result<Array<ExternalTask>, CamundaException>

    /**
     * Complete an external task and update process variables.
     */
    fun complete(id: String, request: CompleteRequest): Result<Unit, CamundaException>

    /**
     * Report a failure to execute an external task.
     */
    fun handleFailure(id: String, request: HandleFailureRequest): Result<Unit, CamundaException>

    /**
     * Unlock an external task. Clears the task’s lock expiration time and worker id.
     */
    fun unlock(id: String): Result<Unit, CamundaException>
}

class ExternalTaskServiceImpl : ExternalTaskService {
    override fun get(id: String): Result<ExternalTask, CamundaException> {
        val (_, _, result) = "/external-task/$id".httpGet()
            .responseObject<ExternalTask>()

        return result.fold({ Result.success(it) }, (ExternalTaskServiceImpl)::failure)
    }

    override fun fetchAndLock(request: FetchAndLockRequest): Result<Array<ExternalTask>, CamundaException> {
        val (_, _, result) = "/external-task/fetchAndLock".httpPost()
            .jsonBody(request)
            .responseObject<Array<ExternalTask>>()

        return result.fold({ Result.success(it) }, (ExternalTaskServiceImpl)::failure)
    }

    override fun complete(id: String, request: CompleteRequest): Result<Unit, CamundaException> {
        val (_, _, result) = "/external-task/$id/complete".httpPost()
            .jsonBody(request)
            .response()

        return result.fold({ Result.success(Unit) }, (ExternalTaskServiceImpl)::failure)
    }

    override fun handleFailure(id: String, request: HandleFailureRequest): Result<Unit, CamundaException> {
        val (_, _, result) = "/external-task/$id/failure".httpPost()
            .jsonBody(request)
            .response()

        return result.fold({ Result.success(Unit) }, (ExternalTaskServiceImpl)::failure)
    }

    override fun unlock(id: String): Result<Unit, CamundaException> {
        val (_, _, result) = "/external-task/$id/unlock".httpPost().response()
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
