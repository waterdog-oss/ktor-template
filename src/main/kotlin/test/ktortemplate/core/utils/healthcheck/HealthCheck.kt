package test.ktortemplate.core.utils.healthcheck

import org.koin.core.KoinComponent
import org.koin.core.inject
import test.ktortemplate.conf.database.DatabaseConnection

internal class ProbesInjector : KoinComponent {
    val dbc: DatabaseConnection by inject()
}

fun Health.Configuration.liveness() {
    liveCheck("alive") { true }
}

fun Health.Configuration.readiness() {
    readyCheck("database") { ProbesInjector().dbc.ping() }
    readyCheck("algorithm") { true }
}