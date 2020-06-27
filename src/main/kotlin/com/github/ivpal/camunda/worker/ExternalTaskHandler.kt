package com.github.ivpal.camunda.worker

import com.github.ivpal.camunda.client.ExternalTask
import com.github.ivpal.camunda.client.Variable
import com.github.kittinunf.result.Result

interface ExternalTaskHandler {
    val topics: List<String>

    val lockDuration: Long
        get() = 300

    val pollInterval: Long
        get() = 100

    val retryTimeout: Long
        get() = 1000

    suspend fun handle(task: ExternalTask): Result<Map<String, Variable>, Exception>
}
