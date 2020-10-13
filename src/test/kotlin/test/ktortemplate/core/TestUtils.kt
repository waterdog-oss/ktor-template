package test.ktortemplate.core

import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import test.ktortemplate.containers.PgSQLContainerFactory
import test.ktortemplate.module

@KtorExperimentalAPI
val testDatabaseConfigs: ApplicationConfig = MapApplicationConfig(
    Pair("datasource.driver", PgSQLContainerFactory.instance.driverClassName),
    Pair("datasource.jdbcUrl", PgSQLContainerFactory.instance.jdbcUrl),
    Pair("datasource.username", PgSQLContainerFactory.instance.username),
    Pair("datasource.password", PgSQLContainerFactory.instance.password),
    Pair("datasource.pool.defaultIsolation", "TRANSACTION_REPEATABLE_READ"),
    Pair("datasource.pool.maxPoolSize", "5")
)

@KtorExperimentalAPI
fun <R> testApp(test: TestApplicationEngine.() -> R): R {
    return withTestApplication(
        {
            module(testDatabaseConfigs)
        },
        test
    )
}
