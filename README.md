# kotlin-camunda

[![Build Status](https://travis-ci.org/IvanovPvl/kotlin-camunda.svg?branch=master)](https://travis-ci.org/IvanovPvl/kotlin-camunda)
[![codecov](https://codecov.io/gh/IvanovPvl/kotlin-camunda/branch/master/graph/badge.svg)](https://codecov.io/gh/IvanovPvl/kotlin-camunda)

> A Kotlin client for Camunda Rest Api

## Install

Simply clone this repository.

## Usage

```kotlin
val client = Client("http://localhost:8080/engine-rest")
val (task, _) = client.externalTask.get("1")
```

## License

[MIT](LICENSE) © 2019 Pavel Ivanov