package test.ktortemplate

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.config.ApplicationConfig
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.identity
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.routing.Routing
import io.ktor.serialization.json
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.Koin
import test.ktortemplate.conf.EnvironmentConfigurator
import test.ktortemplate.core.exception.appException
import test.ktortemplate.core.exception.defaultStatusCodes
import test.ktortemplate.core.httphandler.defaultRoutes
import test.ktortemplate.core.utils.healthcheck.Health
import test.ktortemplate.core.utils.healthcheck.liveness
import test.ktortemplate.core.utils.healthcheck.readiness
import test.ktortemplate.core.utils.json.JsonSettings

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

    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
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
        appException()
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
