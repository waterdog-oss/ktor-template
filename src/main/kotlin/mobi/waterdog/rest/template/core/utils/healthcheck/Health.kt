/**
 * Adapted from https://github.com/zensum/ktor-health-check
 */
package mobi.waterdog.rest.template.core.utils.healthcheck

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.path
import io.ktor.response.respondText
import io.ktor.util.AttributeKey

// A check is a nullary function returning
// a boolean indicating the success of the check.
private typealias Check = suspend () -> Boolean

// A checkmap is simply a map of names to Check functions.
private typealias CheckMap = MutableMap<String, Check>

// A CheckMap can be converted to a function returning
// results for each of the checks.
private fun CheckMap.toFunction(): suspend () -> Map<String, Boolean> = {
    mapValues { it.value() }
}

// We use a version of Kubernetes recommendations: liveness and readiness
private const val LIVE_CHECK_URL = "liveness"
private const val READY_CHECK_URL = "readiness"

private fun normalizeURL(url: String) = url.trim('/').also {
    require(url.trim('/').isNotBlank()) {
        "The passed in URL must be more than one" +
            " character not counting a leading slash"
    }
}

class Health private constructor(private val cfg: Configuration) {
    fun addInterceptor(pipeline: ApplicationCallPipeline) {
        val checks = cfg.getChecksWithFunctions()
        if (checks.isEmpty()) return
        val lengths = checks.keys.map { it.length }
        val maxL = lengths.maxOrNull()!!
        val minL = lengths.minOrNull()!!
        pipeline.intercept(ApplicationCallPipeline.Call) {
            val path = call.request.path().trim('/')
            if (path.length > maxL || path.length < minL) {
                return@intercept
            }
            val check = checks[path] ?: return@intercept
            val (status, json) = healthCheck(check)
            call.respondText(json, ContentType.Application.Json, status)
            finish()
        }
    }

    class Configuration internal constructor() {
        private val checks: MutableMap<String, CheckMap> = mutableMapOf()
        private var noLive = false
        private var noReady = false

        internal fun getChecksWithFunctions() =
            checks.mapValues { (_, v) -> v.toFunction() }

        private fun ensureDisableUnambiguous(url: String) {
            checks[url]?.let {
                if (it.isNotEmpty()) {
                    throw AssertionError(
                        "Cannot disable a check which " +
                            "has been assigned functions"
                    )
                }
            }
        }

        /**
         * Calling this disables the default live check on /liveness
         */
        fun disableLiveCheck() {
            noLive = true
            ensureDisableUnambiguous(LIVE_CHECK_URL)
        }

        /**
         * Calling this disabled the default ready check on /readiness
         */
        fun disableReadyCheck() {
            noReady = true
            ensureDisableUnambiguous(READY_CHECK_URL)
        }

        /**
         * Adds a check function to a custom check living at the specified URL
         */
        private fun addCheckToUrl(url: String, name: String, check: Check) {
            val urlCheck: CheckMap = checks.getOrPut(normalizeURL(url), { mutableMapOf() })
            urlCheck[name] = check
        }

        /**
         * Add a health check giving it a name
         */
        fun liveCheck(name: String, check: Check) {
            addCheckToUrl(LIVE_CHECK_URL, name, check)
        }

        /**
         * Add a ready check giving it a name
         */
        fun readyCheck(name: String, check: Check) {
            addCheckToUrl(READY_CHECK_URL, name, check)
        }

        internal fun ensureWellKnown() {
            if (!noLive) {
                checks[READY_CHECK_URL]
            }
            if (!noReady) {
                checks[LIVE_CHECK_URL]
            }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, Health> {
        override val key = AttributeKey<Health>("Health")
        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ) = Health(
            Configuration()
                .apply(configure)
                .apply { ensureWellKnown() }
        ).apply { addInterceptor(pipeline) }
    }
}
