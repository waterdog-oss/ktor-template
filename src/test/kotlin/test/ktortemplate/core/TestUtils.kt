package test.ktortemplate.core

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.identity
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.routing.Routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import org.koin.ktor.ext.Koin
import test.ktortemplate.conf.DefaultEnvironmentConfigurator
import test.ktortemplate.core.httphandler.defaultRoutes
import test.ktortemplate.core.utils.JsonSettings

fun Application.testModule() {
    val modules = DefaultEnvironmentConfigurator(environment).buildEnvironmentConfig()

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

    install(CallLogging)
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(JsonSettings.mapper))
    }
    install(Koin) {
        modules(modules)
    }
    install(Routing) {
        defaultRoutes()
    }
}

fun <R> testApp(test: TestApplicationEngine.() -> R): R {
    return withTestApplication({
        testModule()
    }, test)
}
