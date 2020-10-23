package test.ktortemplate.core.utils.log

import org.slf4j.LoggerFactory
import org.slf4j.MDC

object LogHelper {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun info(message: String) {
        val requestId = MDC.get("X-Request-Id")
        log.info("Request-Id=$requestId $message")
    }
}
