package mobi.waterdog.rest.template.tests

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.config.ApplicationConfig
import io.ktor.features.CORS
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.callIdMdc
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.identity
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.routing.Routing
import io.ktor.serialization.json
import io.ktor.util.KtorExperimentalAPI
import mobi.waterdog.rest.template.exception.defaultExceptionHandler
import mobi.waterdog.rest.template.exception.defaultStatusCodes
import mobi.waterdog.rest.template.healthcheck.Health
import mobi.waterdog.rest.template.log.SemiStructuredLogFormatter
import mobi.waterdog.rest.template.tests.conf.EnvironmentConfigurator
import mobi.waterdog.rest.template.tests.core.httphandler.defaultRoutes
import mobi.waterdog.rest.template.tests.core.utils.healthcheck.liveness
import mobi.waterdog.rest.template.tests.core.utils.healthcheck.readiness
import mobi.waterdog.rest.template.tests.core.utils.json.JsonSettings
import org.koin.ktor.ext.Koin
import java.util.UUID

@KtorExperimentalAPI
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
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
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
