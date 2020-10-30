package mobi.waterdog.rest.template.core.utils.healthcheck

import kotlinx.coroutines.withTimeout
import org.koin.core.KoinComponent
import org.koin.core.inject
import mobi.waterdog.rest.template.conf.database.DatabaseConnection

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
        withTimeout(timeout) {
            HealthCheckInjector().dbc.ping()
        }
    } catch (ex: Exception) {
        false
    }
