package test.ktortemplate.core.httphandler

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import test.ktortemplate.core.model.Const
import test.ktortemplate.core.model.Expr
import test.ktortemplate.core.model.NotANumber
import test.ktortemplate.core.model.Sum
import test.ktortemplate.core.model.TestInstantSerialization
import test.ktortemplate.core.model.TestSealedClass
import java.time.Instant

fun Route.testSerializationRoutes() {

    get("/sealed") {
        val expr: Expr = when (call.request.queryParameters["type"]) {
            "const" -> Const(call.request.queryParameters["value"]!!.toDouble())
            "sum" -> Sum(
                Const(call.request.queryParameters["value"]!!.toDouble()),
                Const(call.request.queryParameters["value"]!!.toDouble())
            )
            else -> NotANumber()
        }

        call.respond(TestSealedClass(expr))
    }

    get("/instant") {
        val time = Instant.ofEpochMilli(call.request.queryParameters["time"]?.toLong() ?: Instant.now().toEpochMilli())
        call.respond(TestInstantSerialization(time))
    }
}
