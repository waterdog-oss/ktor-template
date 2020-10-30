package mobi.waterdog.rest.template

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CORS
import io.ktor.http.HttpMethod
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import mobi.waterdog.rest.template.core.httphandler.defaultRoutes

@KtorExperimentalAPI
fun Application.module() {

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        anyHost()
    }

    install(Routing) {
        defaultRoutes()
    }

    log.info("Ktor server started...")
}
