package mobi.waterdog.rest.template.tests.core.utils.healthcheck

import kotlinx.coroutines.withTimeout
import mobi.waterdog.rest.template.database.DatabaseConnection
import mobi.waterdog.rest.template.healthcheck.Health
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
