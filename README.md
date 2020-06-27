# kotlin-camunda

[![Build Status](https://travis-ci.org/ivpal/kotlin-camunda.svg?branch=master)](https://travis-ci.org/ivpal/kotlin-camunda)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/b826e035eb5f432ca85ee41fdff38fe5)](https://www.codacy.com/manual/IvanovPvl/kotlin-camunda)
[![codecov](https://codecov.io/gh/ivpal/kotlin-camunda/branch/master/graph/badge.svg)](https://codecov.io/gh/ivpal/kotlin-camunda)

> A Kotlin client for Camunda Rest Api

## Install

Simply clone this repository.

## Usage

```kotlin
suspend fun main() {
    val configuration = WorkerConfiguration("workerId", "http://localhost:8080/engine-rest")
    val worker = Worker(configuration)
    worker.addHandler(SimpleHandler())
    worker.run()
}

class SimpleHandler : ExternalTaskHandler {
    override val topics = listOf("greeting")

    override suspend fun handle(task: ExternalTask): Result<Map<String, Variable>, Exception> {
        val name = task.variables["name"]?.value as String
        return Result.of(
            mapOf("reply" to Variable("Hello, $name", "String"))
        )
    }
}
```

## License

[MIT](LICENSE) © 2019 - 2020 Pavel Ivanov