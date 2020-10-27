package test.ktortemplate.core.utils.log

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.response.header
import io.ktor.util.AttributeKey
import org.slf4j.MDC
import java.util.UUID

class RequestIdFeature(private val headerName: String) {
    class Configuration {
        var headerName = "X-Request-Id"
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RequestIdFeature> {
        override val key: AttributeKey<RequestIdFeature> = AttributeKey("RequestIdFeature")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): RequestIdFeature {
            val configuration = Configuration().apply(configure)
            val feature = RequestIdFeature(configuration.headerName)

            // Intercept call
            pipeline.intercept(ApplicationCallPipeline.Call) {
                feature.interceptRequestId(call)
            }

            return feature
        }
    }

    /**
     * Gets request id from request or generate new one and add it to the call response
     */
    private fun interceptRequestId(call: ApplicationCall) {
        val requestId = call.request.headers[headerName] ?: UUID.randomUUID().toString()

        // Add request id to thread MDC
        MDC.put(headerName, requestId)

        // Add request id to response
        call.response.header(headerName, requestId)
    }
}