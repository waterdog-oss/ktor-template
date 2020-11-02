package mobi.waterdog.rest.template.core.httphandler

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.defaultRoutes() {

    get("") {
        call.respond("Hello World")
    }
}
