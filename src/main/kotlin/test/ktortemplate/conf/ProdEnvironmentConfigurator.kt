package test.ktortemplate.conf

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.ApplicationEnvironment
import org.koin.core.module.Module
import org.koin.dsl.module
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.persistance.sql.CarRepositoryImpl
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.service.CarServiceImpl
import javax.sql.DataSource

class ProdEnvironmentConfigurator(private val environment: ApplicationEnvironment) :
    EnvironmentConfigurator {

            override fun buildEnvironmentConfig(): List<Module> {
        environment.log.info("Init Production environment config")

        return listOf(
            initServicesAndRepos(),
            initDbCore()
        )
    }

    private fun initServicesAndRepos() = module {
        single<CarService> { CarServiceImpl() }
        single<CarRepository> { CarRepositoryImpl() }
    }

    private fun initDbCore() = module {
        val className = environment.config.property("prod.datasource.driver").getString()
        val jdbcUrl = environment.config.property("prod.datasource.jdbcUrl").getString()
        val username = environment.config.property("prod.datasource.username").getString()
        val password = environment.config.property("prod.datasource.password").getString()
        val dataSource: DataSource = HikariDataSource(HikariConfig().apply {
            this.driverClassName = className
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            this.maximumPoolSize = 5
            this.isAutoCommit = true
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            this.leakDetectionThreshold = 10000
            this.poolName = "ktortemplatepool"
            this.validate()
        })

        single { DatabaseConnection(dataSource) }
    }
}
