package com.github.ivpal.camunda.worker

import com.github.ivpal.camunda.client.Client
import com.github.ivpal.camunda.client.CompleteRequest
import com.github.ivpal.camunda.client.FetchAndLockRequest
import com.github.ivpal.camunda.client.HandleFailureRequest
import com.github.ivpal.camunda.client.Topic
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Worker(configuration: WorkerConfiguration) {
    private var handlers = mutableListOf<ExternalTaskHandler>()
    private val id: String = configuration.workerId
    private val client: Client = Client(configuration.baseUrl)

    fun addHandler(handler: ExternalTaskHandler) = handlers.add(handler)

    suspend fun run() {
        coroutineScope {
            for (handler in handlers) {
                val request = FetchAndLockRequest(
                    workerId = id,
                    maxTasks = 1,
                    topics = handler.topics.map { Topic(it, handler.lockDuration) }
                )
                launch {
                    while (true) {
                        val (tasks, _) = client.externalTask.fetchAndLock(request)
                        if (tasks != null && tasks.isNotEmpty()) {
                            val task = tasks.single()
                            val (variables, ex) = handler.handle(task)
                            if (ex != null) {
                                val failureRequest = HandleFailureRequest(
                                    id,
                                    ex.message,
                                    0,
                                    handler.retryTimeout
                                )
                                client.externalTask.handleFailure(task.id, failureRequest)
                                break
                            } else if (variables != null) {
                                val completeRequest = CompleteRequest(id, variables)
                                client.externalTask.complete(task.id, completeRequest)
                            }
                        }

                        delay(handler.pollInterval)
                    }
                }
            }
        }
    }
}
