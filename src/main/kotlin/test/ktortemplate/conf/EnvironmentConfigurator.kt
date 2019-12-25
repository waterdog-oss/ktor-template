package test.ktortemplate.conf

import org.koin.core.module.Module

interface EnvironmentConfigurator {

    fun buildEnvironmentConfig(): List<Module>
}
