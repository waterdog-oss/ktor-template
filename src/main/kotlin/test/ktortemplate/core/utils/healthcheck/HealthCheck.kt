package test.ktortemplate.core.utils.healthcheck

import kotlinx.coroutines.withTimeout
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection

internal class HealthCheckInjector : KoinComponent {
    val dbc: DatabaseConnection by inject()
}

fun Health.Configuration.liveness(timeoutMs: Long) {
    readyCheck("database") { checkDatabase(timeoutMs) }
    liveCheck("alive") { true }
}

fun Health.Configuration.readiness(timeoutMs: Long) {
    readyCheck("database") { checkDatabase(timeoutMs) }
}

private suspend fun checkDatabase(timeout: Long): Boolean =
    try {
        val result = withTimeout(timeout) {
            try {
                HealthCheckInjector().dbc.ping()
                true
            } catch (ex: Exception) {
                println("Exception in query: ${ex.javaClass.name}")
                false
            }
        }

        println("Returning result: $result")
        result
    } catch (ex: Exception) {
        println("Caught ${ex.javaClass.name}. Returning false")
        false
    }
