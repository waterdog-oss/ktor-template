package test.ktortemplate.conf

import io.ktor.application.ApplicationEnvironment
import org.koin.core.module.Module
import org.koin.dsl.module
import test.ktortemplate.core.service.TestService
import test.ktortemplate.core.service.TestServiceImpl

class DefaultEnvironmentConfigurator(private val environment: ApplicationEnvironment) :
    EnvironmentConfigurator {

    override fun buildEnvironmentConfig(): List<Module> {
        environment.log.info("Init default environment config")

        return listOf(
            initService()
        )
    }

    private fun initService() = module {
        single<TestService> { TestServiceImpl() }
    }
}
