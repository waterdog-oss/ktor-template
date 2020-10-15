package test.ktortemplate.conf

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.ApplicationEnvironment
import org.jetbrains.exposed.sql.SchemaUtils
import org.koin.core.module.Module
import org.koin.dsl.module
import test.ktortemplate.conf.database.DatabaseConnection
import test.ktortemplate.core.persistance.CarRepository
import test.ktortemplate.core.persistance.PartRepository
import test.ktortemplate.core.persistance.sql.CarMappingsTable
import test.ktortemplate.core.persistance.sql.CarRepositoryImpl
import test.ktortemplate.core.persistance.sql.PartMappingsTable
import test.ktortemplate.core.persistance.sql.PartRepositoryImpl
import test.ktortemplate.core.service.CarService
import test.ktortemplate.core.service.CarServiceImpl
import javax.sql.DataSource

class DevEnvironmentConfigurator(private val environment: ApplicationEnvironment) :
    EnvironmentConfigurator {

    override fun buildEnvironmentConfig(): List<Module> {
        environment.log.info("Init dev environment config")

        return listOf(
            initDbCore(),
            initServicesAndRepos()
        )
    }

    private fun initServicesAndRepos() = module {
        single<CarRepository> { CarRepositoryImpl() }
        single<PartRepository> { PartRepositoryImpl() }
        single<CarService> { CarServiceImpl() }
    }

    private fun initDbCore() = module {
        val dataSource: DataSource = HikariDataSource(
            HikariConfig().apply {
                driverClassName = environment.config.property("dev.datasource.driver").getString()
                jdbcUrl = environment.config.property("dev.datasource.jdbcUrl").getString()
                username = "user"
                password = "test"
                maximumPoolSize = 5
                isAutoCommit = true
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                leakDetectionThreshold = 10000
                poolName = "ktortemplatepool"
                validate()
            }
        )

        val databaseConnection = DatabaseConnection(dataSource)
        single { databaseConnection }

        bootstrapDatabase(databaseConnection)
    }

    private fun bootstrapDatabase(dbc: DatabaseConnection) {
        dbc.query {
            SchemaUtils.create(CarMappingsTable)
            SchemaUtils.create(PartMappingsTable)
        }
    }
}
