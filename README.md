# kotlin-camunda

[![Build Status](https://travis-ci.org/ivpal/kotlin-camunda.svg?branch=master)](https://travis-ci.org/ivpal/kotlin-camunda)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/b826e035eb5f432ca85ee41fdff38fe5)](https://www.codacy.com/manual/IvanovPvl/kotlin-camunda)
[![codecov](https://codecov.io/gh/ivpal/kotlin-camunda/branch/master/graph/badge.svg)](https://codecov.io/gh/ivpal/kotlin-camunda)

> A Kotlin client for Camunda Rest Api

## Install

Simply clone this repository.

## Usage

```kotlin
runBlocking {
    val client = Client("http://localhost:8080/engine-rest")
    val (task, error) = client.externalTask.get("1")
}
```

## License

[MIT](LICENSE) © 2019 Pavel Ivanov