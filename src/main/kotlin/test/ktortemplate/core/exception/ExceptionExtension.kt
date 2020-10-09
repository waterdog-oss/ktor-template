package test.ktortemplate.core.exception

import java.io.PrintWriter
import java.io.StringWriter

val Throwable.asString: String
    get() {
        val stringWriter = StringWriter()
        this.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }