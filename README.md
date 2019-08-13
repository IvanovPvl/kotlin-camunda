# kotlin-camunda

[![Build Status](https://travis-ci.org/ivpal/kotlin-camunda.svg?branch=master)](https://travis-ci.org/IvanovPvl/kotlin-camunda)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bd46b7067cf24727b10ee2638403cb05)](https://www.codacy.com/app/IvanovPvl/result)
[![codecov](https://codecov.io/gh/ivpal/kotlin-camunda/branch/master/graph/badge.svg)](https://codecov.io/gh/IvanovPvl/kotlin-camunda)

> A Kotlin client for Camunda Rest Api

## Install

Simply clone this repository.

## Usage

```kotlin
val client = Client("http://localhost:8080/engine-rest")
val (task, error) = client.externalTask.get("1")
```

## License

[MIT](LICENSE) © 2019 Pavel Ivanov