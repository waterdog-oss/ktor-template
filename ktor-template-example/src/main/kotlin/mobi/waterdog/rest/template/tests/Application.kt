package mobi.waterdog.rest.template.tests

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.identity
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.routing.Routing
import mobi.waterdog.rest.template.exception.defaultExceptionHandler
import mobi.waterdog.rest.template.exception.defaultStatusCodes
import mobi.waterdog.rest.template.healthcheck.Health
import mobi.waterdog.rest.template.log.SemiStructuredLogFormatter
import mobi.waterdog.rest.template.tests.conf.EnvironmentConfigurator
import mobi.waterdog.rest.template.tests.core.httphandler.defaultRoutes
import mobi.waterdog.rest.template.tests.core.utils.healthcheck.liveness
import mobi.waterdog.rest.template.tests.core.utils.healthcheck.readiness
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings
import org.koin.ktor.plugin.Koin
import java.util.UUID

fun Application.module(configOverrides: ApplicationConfig? = null) {

    val modules = EnvironmentConfigurator(environment.config, configOverrides).getDependencyInjectionModules()

    install(DefaultHeaders)
    install(Compression) {
        gzip {
            priority = 100.0
        }
        identity {
            priority = 10.0
        }
        deflate {
            priority = 1.0
        }
    }

    // Installs call logging and request tracing
    val callIdHeader = SemiStructuredLogFormatter.REQUEST_ID_HEADER
    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        callIdMdc(callIdHeader)
    }
    install(CallId) {
        generate { it.request.headers[callIdHeader] ?: UUID.randomUUID().toString() }
        replyToHeader(callIdHeader)
    }

    install(ContentNegotiation) {
        json(
            contentType = ContentType.Application.Json,
            json = JsonSettings.mapper
        )
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }

    install(Koin) {
        modules(modules)
    }

    install(Routing) {
        defaultRoutes()
    }

    install(StatusPages) {
        defaultExceptionHandler()
        defaultStatusCodes()
    }

    install(Health) {
        val dbTimeout = environment.config
            .propertyOrNull("healthcheck.readiness.database.timeoutMillis")?.getString()?.toLong()
            ?: 3000L
        liveness(dbTimeout)
        readiness(dbTimeout)
    }

    log.info("Ktor server started...")
}
