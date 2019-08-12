package io.datalense.camunda

class ResultException(message: String) : Exception(message)

data class Error(val type: String, val message: String)

sealed class Result<out V : Any> {
    open operator fun component1(): V? = null

    open operator fun component2(): Error? = null

    abstract fun get(): V

    class Success<out V : Any>(private val value: V) : Result<V>() {
        override fun get(): V = value

        override fun component1(): V? = value
    }

    class Failure(private val error: Error) : Result<Nothing>() {
        override fun get(): Nothing = throw ResultException(error.message)

        override fun component2(): Error? = error
    }

    companion object {
        fun <V : Any> success(value: V) = Success(value)

        fun failure(error: Error) = Failure(error)
    }
}
