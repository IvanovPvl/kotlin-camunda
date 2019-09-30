package com.github.ivpal.camunda

import com.github.kittinunf.fuel.core.FuelManager

/**
 * Client for communicating with Camunda.
 */
class Client(path: String, timeout: Int = 5000) {
    init {
        with(FuelManager.instance) {
            basePath = path
            timeoutInMillisecond = timeout
        }
    }

    /**
     * Get [ExternalTaskService].
     */
    val externalTask: ExternalTaskService = ExternalTaskServiceImpl()
}

data class Error(val type: String, val message: String)

class CamundaException(val type: String, override val message: String) : Exception(message)
