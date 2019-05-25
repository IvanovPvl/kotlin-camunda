package io.datalense.camunda

import com.github.kittinunf.fuel.core.FuelManager

/**
 * Client for communicating with Camunda
 */
class Client(path: String, timeout: Int = 5000) {
    init {
        with(FuelManager.instance) {
            basePath = path
            timeoutInMillisecond = timeout
        }
    }

    /**
     * Get [ExternalTaskService]
     */
    val externalTask: ExternalTaskService = ExternalTaskServiceImpl()
}