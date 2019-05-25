# kotlin-camunda

> A Kotlin client for Camunda Rest Api

## Install

Simply clone this repository.

## Usage

```kotlin
val client = Client("http://localhost:8080/engine-rest")
val task = client.externalTask.get("1")
```

## License

[MIT](LICENSE) © 2019 Pavel Ivanov