package io.datalense.camunda

import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet

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
    val businessKey: String
)

interface ExternalTaskService {
    /**
     * Get [ExternalTask] by [id]
     */
    fun get(id: String): ExternalTask?
}

class ExternalTaskServiceImpl : ExternalTaskService {
    override fun get(id: String): ExternalTask? {
        val (_, _, result) = "/external-task/$id".httpGet()
            .responseObject<ExternalTask>()

        val (task, _) = result
        return task
    }
}