package test.ktortemplate.core.utils.healthcheck

import kotlinx.coroutines.withTimeout
import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection

internal class HealthCheckInjector : KoinComponent {
    val dbc: DatabaseConnection by inject()
}

fun Health.Configuration.liveness() = liveCheck("alive") { true }

fun Health.Configuration.readiness(timeout: Long) {
    readyCheck("database") {
        try {
            withTimeout(timeout) {
                HealthCheckInjector().dbc.ping()
            }
        } catch (e: Exception) {
            false
        }
    }
    readyCheck("algorithm") { true }
}
