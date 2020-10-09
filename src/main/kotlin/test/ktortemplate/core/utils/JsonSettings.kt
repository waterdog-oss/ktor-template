package test.ktortemplate.core.utils

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonSettings {
    val mapper by lazy {
        val mapper = jacksonObjectMapper()
        mapper.apply {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            setDefaultPrettyPrinter(
                DefaultPrettyPrinter().apply {
                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                    indentObjectsWith(DefaultIndenter("  ", "\n"))
                }
            )
        }
        mapper
    }
}
